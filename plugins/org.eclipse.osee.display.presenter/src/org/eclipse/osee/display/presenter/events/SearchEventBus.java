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
package org.eclipse.osee.display.presenter.events;

import org.eclipse.osee.display.api.data.SearchCriteria;
import org.eclipse.osee.display.api.view.SearchCriteriaView.SearchStatus;
import org.eclipse.osee.display.mvp.event.EventBus;
import org.eclipse.osee.display.mvp.event.annotation.RouteTo;
import org.eclipse.osee.display.presenter.SearchCriteriaPresenter;
import org.eclipse.osee.display.presenter.internal.ArtifactPresenter;
import org.eclipse.osee.display.presenter.internal.SearchResultsPresenter;

/**
 * @author John Misinco
 */
public interface SearchEventBus extends EventBus {

   @RouteTo({SearchResultsPresenter.class})
   public void sendExecuteSearch(SearchCriteria message);

   @RouteTo({SearchCriteriaPresenter.class})
   public void sendSearchStatus(SearchStatus message);

   @RouteTo({ArtifactPresenter.class})
   public void sendResultSelected(String artifactGuid, String branchGuid);

}
