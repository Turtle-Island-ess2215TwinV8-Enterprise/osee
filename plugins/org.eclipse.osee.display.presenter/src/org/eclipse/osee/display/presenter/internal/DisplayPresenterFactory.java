/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.display.presenter.internal;

import java.util.Locale;
import org.eclipse.osee.display.mvp.event.EventBus;
import org.eclipse.osee.display.mvp.presenter.AbstractPresenter;
import org.eclipse.osee.display.mvp.presenter.AbstractPresenterFactory;
import org.eclipse.osee.display.mvp.presenter.CreatePresenterException;
import org.eclipse.osee.display.mvp.presenter.Presenter;
import org.eclipse.osee.display.mvp.view.View;
import org.eclipse.osee.display.presenter.ArtifactProvider;
import org.eclipse.osee.display.presenter.ArtifactProviderImpl;
import org.eclipse.osee.display.presenter.AsyncSearchHandler;
import org.eclipse.osee.display.presenter.PresenterApplicationContext;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.ApplicationContext;
import org.eclipse.osee.orcs.Graph;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.search.QueryFactory;

/**
 * @author John Misinco
 */
public class DisplayPresenterFactory extends AbstractPresenterFactory {

   protected OrcsApi orcsApi;
   protected Log logger;
   protected ExecutorAdmin executorAdmin;

   public void setOrcsApi(OrcsApi orcsApi) {
      this.orcsApi = orcsApi;
   }

   @Override
   public void setLogger(Log logger) {
      this.logger = logger;
   }

   public void setExecutorAdmin(ExecutorAdmin executorAdmin) {
      this.executorAdmin = executorAdmin;
   }

   protected ArtifactProvider getArtifactProvider() {
      ApplicationContext context = new PresenterApplicationContext();
      QueryFactory queryFactory = orcsApi.getQueryFactory(context);
      Graph graph = orcsApi.getGraph(context);
      return new ArtifactProviderImpl(logger, executorAdmin, queryFactory, graph);
   }

   @Override
   public boolean canCreate(Class<? extends Presenter<? extends View, ? extends EventBus>> presenterType) {
      boolean result = false;
      if (implementsType(ArtifactPresenter.class, presenterType)) {
         result = true;
      } else if (implementsType(SearchResultsPresenter.class, presenterType)) {
         result = true;
      }
      return result;
   }

   @SuppressWarnings({"rawtypes", "unchecked", "unused"})
   @Override
   public <T extends Presenter<? extends View, ? extends EventBus>> T createPresenter(Class<? extends Presenter<? extends View, ? extends EventBus>> presenterType, Locale locale) throws CreatePresenterException {
      AbstractPresenter presenter = null;
      if (implementsType(ArtifactPresenter.class, presenterType)) {
         ArtifactProvider provider = getArtifactProvider();
         presenter = new ArtifactPresenter(provider);
      } else if (implementsType(SearchResultsPresenter.class, presenterType)) {
         ArtifactProvider provider = getArtifactProvider();
         SearchResultConverter converter = new SearchResultConverter(provider);
         AsyncSearchHandler listener = new AsyncSearchHandler(converter, logger);
         presenter = new SearchResultsPresenter(provider, listener);
      }
      presenter.setLogger(getLogger());
      return (T) presenter;
   }
}
