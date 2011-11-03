/*
 * Created on Nov 8, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.presenter.internal;

import java.util.Locale;
import org.eclipse.osee.display.mvp.event.EventBus;
import org.eclipse.osee.display.mvp.presenter.AbstractPresenter;
import org.eclipse.osee.display.mvp.presenter.AbstractPresenterFactory;
import org.eclipse.osee.display.mvp.presenter.CreatePresenterException;
import org.eclipse.osee.display.mvp.presenter.Presenter;
import org.eclipse.osee.display.mvp.view.View;
import org.eclipse.osee.display.presenter.PresenterApplicationContext;
import org.eclipse.osee.executor.admin.ExecutorAdmin;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.ApplicationContext;
import org.eclipse.osee.orcs.Graph;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.search.QueryFactory;

public class AtsPresenterFactory extends AbstractPresenterFactory {

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

   protected AtsArtifactProvider getArtifactProvider() {
      ApplicationContext context = new PresenterApplicationContext();
      QueryFactory queryFactory = orcsApi.getQueryFactory(context);
      Graph graph = orcsApi.getGraph(context);
      return new AtsArtifactProviderImpl(logger, executorAdmin, queryFactory, graph);
   }

   @Override
   public boolean canCreate(Class<? extends Presenter<? extends View, ? extends EventBus>> presenterType) {
      boolean result = false;
      if (implementsType(AtsSearchCriteriaPresenter.class, presenterType)) {
         result = true;
      }
      return result;
   }

   @SuppressWarnings({"rawtypes", "unchecked", "unused"})
   @Override
   public <T extends Presenter<? extends View, ? extends EventBus>> T createPresenter(Class<? extends Presenter<? extends View, ? extends EventBus>> presenterType, Locale locale) throws CreatePresenterException {
      AbstractPresenter presenter = null;
      if (implementsType(AtsSearchCriteriaPresenter.class, presenterType)) {
         AtsArtifactProvider provider = getArtifactProvider();
         presenter = new AtsSearchCriteriaPresenter(provider);
      }
      presenter.setLogger(getLogger());
      return (T) presenter;
   }
}
