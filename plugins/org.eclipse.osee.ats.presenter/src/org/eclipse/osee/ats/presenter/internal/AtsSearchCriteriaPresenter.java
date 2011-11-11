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
package org.eclipse.osee.ats.presenter.internal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.ats.api.view.AtsSearchCriteriaView;
import org.eclipse.osee.ats.api.view.AtsSearchCriteriaView.AtsSearchCriteriaListener;
import org.eclipse.osee.display.api.data.SearchCriteria;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.mvp.BindException;
import org.eclipse.osee.display.presenter.SearchCriteriaPresenter;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.data.ReadableArtifact;

/**
 * @author John Misinco
 */
public class AtsSearchCriteriaPresenter extends SearchCriteriaPresenter<AtsSearchCriteriaView> implements AtsSearchCriteriaListener {

   private final AtsArtifactProvider artifactProvider;

   public AtsSearchCriteriaPresenter(AtsArtifactProvider artifactProvider) {
      this.artifactProvider = artifactProvider;
   }

   @Override
   public void onProgramSelected(ViewId program) {
      Collection<ReadableArtifact> relatedBuilds;
      if (program != null) {
         try {
            relatedBuilds = artifactProvider.getBuilds(program.getGuid());
         } catch (Exception ex) {
            setErrorMessage("Error in selectProgram", ex);
            return;
         }
         List<ViewId> builds = new LinkedList<ViewId>();
         if (relatedBuilds != null) {
            for (ReadableArtifact build : relatedBuilds) {
               builds.add(new ViewId(build.getGuid(), build.getName()));
            }
         }
         getView().clearBuilds();
         getView().setBuilds(builds);
         getView().setSearchAllowed(true);
      }
   }

   @Override
   public void onInit() {
      List<ViewId> programIds = new LinkedList<ViewId>();
      Collection<ReadableArtifact> programArts;
      try {
         programArts = artifactProvider.getPrograms();
      } catch (OseeCoreException ex) {
         setErrorMessage("Error in addProgramsToSearchHeader", ex);
         return;
      }
      if (programArts != null) {
         for (ReadableArtifact program : programArts) {
            programIds.add(new ViewId(program.getGuid(), program.getName()));
         }
      }
      getView().clearPrograms();
      getView().setPrograms(programIds);
      getView().setSearchAllowed(false);
   }

   @Override
   public void bind() throws BindException {
      if (getView() == null) {
         throw new BindException("View was null");
      }
      getView().setSearchCriteriaListener(this);
      getView().setAtsCriteriaViewListener(this);
   }

   @Override
   public void onSearchClicked() {
      String buildGuid = getView().getSelectedBuild().getGuid();
      String branchGuid = getBaselineBranch(buildGuid);
      SearchCriteria searchCriteria = getView().getCriteria();
      searchCriteria.setCriteria(SearchCriteria.BRANCH_KEY, branchGuid);
      getEventBus().sendExecuteSearch(searchCriteria);
   }

   protected String getBaselineBranch(String buildGuid) {
      String branchGuid = "";

      try {
         branchGuid = artifactProvider.getBaselineBranchGuid(buildGuid);
      } catch (Exception ex) {
         setErrorMessage("Error in getBaselineBranch", ex);
         return "";
      }
      return branchGuid;
   }

   public void setSelectedProgram(ViewId program) {
      onInit();
      getView().setSelectedProgram(program);
      onProgramSelected(program);
   }

   public void setSelectedBuild(ViewId build) {
      ViewId program = getView().getSelectedProgram();
      onProgramSelected(program);
      getView().setSelectedBuild(build);
   }

}
