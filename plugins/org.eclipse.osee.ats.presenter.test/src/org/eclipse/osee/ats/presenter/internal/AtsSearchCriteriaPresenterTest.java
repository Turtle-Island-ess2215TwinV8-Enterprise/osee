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
import junit.framework.Assert;
import org.eclipse.osee.ats.api.view.AtsSearchCriteriaView;
import org.eclipse.osee.display.api.data.SearchCriteria;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.mvp.MessageType;
import org.eclipse.osee.display.presenter.mocks.MockArtifact;
import org.eclipse.osee.display.presenter.mocks.MockArtifactProvider;
import org.eclipse.osee.display.presenter.mocks.MockSearchEventBus;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.junit.Test;

/**
 * @author John Misinco
 */
public class AtsSearchCriteriaPresenterTest {

   @Test
   public void testOnInit() {
      MockAtsArtifactProvider provider = new MockAtsArtifactProvider();
      AtsSearchCriteriaPresenter presenter = new AtsSearchCriteriaPresenter(provider);
      MockAtsSearchCriteriaView view = new MockAtsSearchCriteriaView();
      presenter.setView(view);
      presenter.onInit();

      Assert.assertEquals(provider.getPrograms().size(), view.getPrograms().size());
      Assert.assertTrue(view.isClearProgramsCalled());
   }

   @Test
   public void testOnProgramSelected() {
      MockAtsArtifactProvider provider = new MockAtsArtifactProvider();
      AtsSearchCriteriaPresenter presenter = new AtsSearchCriteriaPresenter(provider);
      MockAtsSearchCriteriaView view = new MockAtsSearchCriteriaView();
      presenter.setView(view);

      ReadableArtifact first = provider.getPrograms().iterator().next();
      ViewId program = new ViewId(first.getGuid(), first.getName());
      presenter.onProgramSelected(program);

      Assert.assertEquals(provider.getBuilds(first.getGuid()).size(), view.getBuilds().size());
      Assert.assertTrue(view.isClearBuildsCalled());
   }

   @Test
   public void testOnSearchClicked() {
      MockAtsArtifactProvider provider = new MockAtsArtifactProvider();
      AtsSearchCriteriaPresenter presenter = new AtsSearchCriteriaPresenter(provider);
      MockAtsSearchCriteriaView view = new MockAtsSearchCriteriaView();
      presenter.setView(view);

      MockSearchEventBus eventBus = new MockSearchEventBus();
      presenter.setEventBus(eventBus);

      SearchCriteria criteria = new SearchCriteria();
      view.setCriteria(criteria);
      ReadableArtifact program = provider.getPrograms().iterator().next();
      ReadableArtifact build = provider.getBuilds(program.getGuid()).iterator().next();
      ViewId buildId = new ViewId(build.getGuid(), build.getName());
      view.setSelectedBuild(buildId);

      presenter.onSearchClicked();

      String expectedBranchGuid = provider.getBaselineBranchGuid(build.getGuid());
      Assert.assertEquals(expectedBranchGuid, eventBus.getSearchCriteria().getCriteria(SearchCriteria.BRANCH_KEY));
   }

   @Test
   public void testSetSelectedProgram() {
      MockAtsArtifactProvider provider = new MockAtsArtifactProvider();
      AtsSearchCriteriaPresenter presenter = new AtsSearchCriteriaPresenter(provider);
      MockAtsSearchCriteriaView view = new MockAtsSearchCriteriaView();
      presenter.setView(view);

      ReadableArtifact progArtifact = provider.getPrograms().iterator().next();
      ViewId program = new ViewId(progArtifact.getGuid(), progArtifact.getName());
      presenter.setSelectedProgram(program);

      Assert.assertEquals(program.getGuid(), view.getSelectedProgram().getGuid());
      Assert.assertEquals(program.getName(), view.getSelectedProgram().getName());
      Assert.assertEquals(provider.getBuilds(program.getGuid()).size(), view.getBuilds().size());
   }

   @Test
   public void testSetSelectedBuild() {
      MockAtsArtifactProvider provider = new MockAtsArtifactProvider();
      AtsSearchCriteriaPresenter presenter = new AtsSearchCriteriaPresenter(provider);
      MockAtsSearchCriteriaView view = new MockAtsSearchCriteriaView();
      presenter.setView(view);

      ReadableArtifact progArtifact = provider.getPrograms().iterator().next();
      ViewId program = new ViewId(progArtifact.getGuid(), progArtifact.getName());
      presenter.setSelectedProgram(program);

      ReadableArtifact buildArtifact = provider.getBuilds(program.getGuid()).iterator().next();
      ViewId build = new ViewId(buildArtifact.getGuid(), buildArtifact.getName());
      presenter.setSelectedBuild(build);

      Assert.assertEquals(build, view.getSelectedBuild());
   }

   public class MockAtsSearchCriteriaView implements AtsSearchCriteriaView {

      private List<ViewId> programs, builds;
      private boolean clearProgramsCalled = false, clearBuildsCalled = false;
      private SearchCriteria searchCriteria;
      private ViewId selectedBuild;
      private ViewId selectedProgram;

      @Override
      public void setSelectedBuild(ViewId selectedBuild) {
         this.selectedBuild = selectedBuild;
      }

      public List<ViewId> getBuilds() {
         return builds;
      }

      public boolean isClearBuildsCalled() {
         return clearBuildsCalled;
      }

      public List<ViewId> getPrograms() {
         return programs;
      }

      public boolean isClearProgramsCalled() {
         return clearProgramsCalled;
      }

      @Override
      public void setSearchAllowed(boolean allowed) {
         //
      }

      @Override
      public boolean isSearchAllowed() {
         return false;
      }

      @Override
      public SearchCriteria getCriteria() {
         return searchCriteria;
      }

      @Override
      public void setCriteria(SearchCriteria criteria) {
         this.searchCriteria = criteria;
      }

      @Override
      public void setStatus(SearchStatus status) {
         //
      }

      @Override
      public SearchStatus getStatus() {
         return null;
      }

      @Override
      public void setSearchCriteriaListener(SearchCriteriaListener listener) {
         //
      }

      @Override
      public void setPrograms(List<ViewId> programs) {
         this.programs = programs;
      }

      @Override
      public void setBuilds(List<ViewId> builds) {
         this.builds = builds;
      }

      @Override
      public void clearPrograms() {
         clearProgramsCalled = true;
         if (programs != null) {
            programs.clear();
         }
      }

      @Override
      public void clearBuilds() {
         clearBuildsCalled = true;
         if (builds != null) {
            builds.clear();
         }
      }

      @Override
      public void setAtsCriteriaViewListener(AtsSearchCriteriaListener listener) {
         //
      }

      @Override
      public Log getLogger() {
         return null;
      }

      @Override
      public void setLogger(Log logger) {
         //
      }

      @Override
      public void displayMessage(String caption) {
         //
      }

      @Override
      public void displayMessage(String caption, String description, MessageType messageType) {
         //
      }

      @Override
      public void dispose() {
         //
      }

      @Override
      public boolean isDisposed() {
         return false;
      }

      @Override
      public Object getContent() {
         return null;
      }

      @Override
      public ViewId getSelectedProgram() {
         return selectedProgram;
      }

      @Override
      public ViewId getSelectedBuild() {
         return selectedBuild;
      }

      @Override
      public void setSelectedProgram(ViewId program) {
         selectedProgram = program;
      }

      @Override
      public void updateCriteria(String key, Object value) {
         //
      }

      @Override
      public void createControl() {
         //
      }

   }

   public class MockAtsArtifactProvider extends MockArtifactProvider implements AtsArtifactProvider {

      private final HashCollection<MockArtifact, MockArtifact> programsAndBuilds =
         new HashCollection<MockArtifact, MockArtifact>();

      public MockAtsArtifactProvider() {
         createProgramsAndBuilds();
      }

      private void createProgramsAndBuilds() {
         MockArtifact program1 = new MockArtifact("prg1Guid_18H74Zqo3gA", "program1");
         MockArtifact program2 = new MockArtifact("prg2Guid_DC2cxIwhWwA", "program2");
         MockArtifact program3 = new MockArtifact("prg3Guid_ALnf3ohtbQA", "program3");
         MockArtifact build1 = new MockArtifact("bld1Guid_BwTPQWRIagA", "build1");
         MockArtifact build2 = new MockArtifact("bld2Guid_DkYoCyCF6gA", "build2");
         MockArtifact build3 = new MockArtifact("bld3Guid_31DjLanu7gA", "build3");
         MockArtifact build4 = new MockArtifact("bld4Guid_H2oLkW5W3QA", "build4");
         programsAndBuilds.put(program1, build1);
         programsAndBuilds.put(program1, build2);
         programsAndBuilds.put(program2, build3);
         programsAndBuilds.put(program3, build1);
         programsAndBuilds.put(program3, build3);
         programsAndBuilds.put(program3, build4);
      }

      @Override
      public Collection<ReadableArtifact> getPrograms() {
         return new LinkedList<ReadableArtifact>(programsAndBuilds.keySet());
      }

      @Override
      public Collection<ReadableArtifact> getBuilds(String programGuid) {
         List<ReadableArtifact> toReturn = null;
         for (MockArtifact program : programsAndBuilds.keySet()) {
            if (program.getGuid().equals(programGuid)) {
               toReturn = new LinkedList<ReadableArtifact>(programsAndBuilds.getValues(program));
               break;
            }
         }
         return toReturn;
      }

      @Override
      public String getBaselineBranchGuid(String buildArtGuid) {
         return buildArtGuid + "_branch";
      }
   }
}
