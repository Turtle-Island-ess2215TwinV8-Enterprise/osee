/*******************************************************************************
 * Copyright (c) 2004, 2007 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.test.cases;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.navigate.NavigateView;
import org.eclipse.osee.ats.navigate.SearchNavigateItem;
import org.eclipse.osee.ats.task.TaskEditor;
import org.eclipse.osee.ats.task.TaskXViewer;
import org.eclipse.osee.ats.test.util.CustomizeDemoTableTestUtil;
import org.eclipse.osee.ats.test.util.DemoTestUtil;
import org.eclipse.osee.ats.test.util.NavigateTestUtil;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.world.search.TaskSearchWorldSearchItem;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.UniversalGroup;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.IDynamicWidgetLayoutListener;
import org.eclipse.osee.support.test.util.DemoSawBuilds;

/**
 * @author Donald G. Dunne
 */
public class AtsNavigateItemsToTaskEditorTest {

   @org.junit.Test
   public void testDemoDatabase() throws Exception {
      DemoTestUtil.setUpTest();
   }

   @org.junit.Test
   public void testMyTasksEditor() throws Exception {
      TaskEditor.closeAll();
      // Place holder for future navigate items opening TaskEditor
      //      XNavigateItem item = NavigateTestUtil.getAtsNavigateItem("My Tasks (Editor)");
      //      handleGeneralDoubleClickAndTestResults(item, TaskArtifact.class, DemoDbTasks.getNumTasks());
   }

   @org.junit.Test
   public void testTaskSearch() throws Exception {
      Collection<TeamDefinitionArtifact> selectedUsers =
            TeamDefinitionArtifact.getTeamTopLevelDefinitions(Active.Active);
      TaskEditor.closeAll();
      XNavigateItem item = NavigateTestUtil.getAtsNavigateItem("Task Search");
      assertTrue(((SearchNavigateItem) item).getWorldSearchItem() instanceof TaskSearchWorldSearchItem);
      handleGeneralDoubleClickAndTestResults(item, TaskSearchWorldSearchItem.class, 0,
            TableLoadOption.DontCopySearchItem);
      runGeneralTaskSearchOnCompletedCancelledTest(item, true, 14);
      runGeneralTaskSearchOnCompletedCancelledTest(item, false, 0);
      runGeneralTaskSearchOnTeamTest(item, selectedUsers, 0);
      selectedUsers.clear();
      List<String> teamDefs = new ArrayList<String>();
      teamDefs.add("SAW Code");
      Set<TeamDefinitionArtifact> tda = TeamDefinitionArtifact.getTeamDefinitions(teamDefs);
      runGeneralTaskSearchOnTeamTest(item, tda, 14);
      runGeneralTaskSearchOnAssigneeTest(item, "Joe Smith", 14);
      runGeneralTaskSearchOnVersionTest(item, DemoSawBuilds.SAW_Bld_1.getName(), 0);
      runGeneralTaskSearchOnVersionTest(item, DemoSawBuilds.SAW_Bld_2.getName(), 14);
      selectedUsers.clear();
      runGeneralTaskSearchOnTeamTest(item, selectedUsers, 14);
      runGeneralTaskSearchOnAssigneeTest(item, "Kay Jones", 8);

   }

   public void runGeneralTaskSearchTest(XNavigateItem item, int expectedNum) throws Exception {
      TaskEditor editor = getSingleEditorOrFail();
      editor.getTaskActionPage().reSearch();
      Collection<Artifact> arts = editor.getLoadedArtifacts();
      NavigateTestUtil.testExpectedVersusActual(item.getName(), expectedNum, arts.size());
   }

   public void runGeneralTaskSearchOnAssigneeTest(XNavigateItem item, String assignee, int expectedNum) throws Exception {
      TaskEditor editor = getSingleEditorOrFail();
      ((TaskSearchWorldSearchItem) editor.getTaskActionPage().getDynamicWidgetLayoutListener()).setSelectedUser(UserManager.getUserByName(assignee));
      runGeneralTaskSearchTest(item, expectedNum);
   }

   public void runGeneralTaskSearchOnTeamTest(XNavigateItem item, Collection<TeamDefinitionArtifact> selectedUsers, int expectedNum) throws Exception {
      // need to set team selected users
      TaskEditor editor = getSingleEditorOrFail();
      IDynamicWidgetLayoutListener dwl = editor.getTaskActionPage().getDynamicWidgetLayoutListener();
      ((TaskSearchWorldSearchItem) dwl).setSelectedTeamDefinitions(selectedUsers);
      runGeneralTaskSearchTest(item, expectedNum);
   }

   public void runGeneralTaskSearchOnVersionTest(XNavigateItem item, String versionString, int expectedNum) throws Exception {
      TaskEditor editor = getSingleEditorOrFail();
      ((TaskSearchWorldSearchItem) editor.getTaskActionPage().getDynamicWidgetLayoutListener()).setVersion(versionString);
      runGeneralTaskSearchTest(item, expectedNum);
   }

   public void runGeneralTaskSearchOnCompletedCancelledTest(XNavigateItem item, boolean selected, int expectedNum) throws Exception {
      Artifact groupArt =
            ArtifactQuery.getArtifactFromTypeAndName(UniversalGroup.ARTIFACT_TYPE_NAME, "Test Group",
                  AtsUtil.getAtsBranch());
      Set<Artifact> selectedUsers = new HashSet<Artifact>();
      TaskEditor editor = getSingleEditorOrFail();
      ((TaskSearchWorldSearchItem) editor.getTaskActionPage().getDynamicWidgetLayoutListener()).setIncludeCompletedCancelledCheckbox(selected);
      if (selected) {
         // select the group
         selectedUsers.add(groupArt);
         ((TaskSearchWorldSearchItem) editor.getTaskActionPage().getDynamicWidgetLayoutListener()).setSelectedGroups(selectedUsers);
      } else {
         // clear the group selected
         ((TaskSearchWorldSearchItem) editor.getTaskActionPage().getDynamicWidgetLayoutListener()).handleSelectedGroupsClear();
      }
      runGeneralTaskSearchTest(item, expectedNum);
   }

   public void handleGeneralDoubleClickAndTestResults(XNavigateItem item, Class<?> clazz, int numOfType, TableLoadOption tableLoadOption) throws OseeCoreException {
      NavigateView.getNavigateView().handleDoubleClick(item, TableLoadOption.ForcePend, TableLoadOption.NoUI,
            tableLoadOption);
      TaskEditor taskEditor = getSingleEditorOrFail();
      assertTrue(taskEditor != null);
      Collection<Artifact> arts = taskEditor.getLoadedArtifacts();
      NavigateTestUtil.testExpectedVersusActual(item.getName(), arts, clazz, numOfType);
   }

   public TaskEditor getSingleEditorOrFail() throws OseeCoreException {
      // Retrieve results from opened editor and test
      Collection<TaskEditor> editors = TaskEditor.getEditors();
      assertTrue("Expecting 1 editor open, currently " + editors.size(), editors.size() == 1);

      return editors.iterator().next();
   }

   public TaskXViewer getXViewer() throws OseeCoreException {
      return getSingleEditorOrFail().getTaskActionPage().getTaskComposite().getTaskXViewer();
   }

   public void handleTableCustomization() throws OseeCoreException {
      // add all columns
      CustomizeDemoTableTestUtil cdialog = new CustomizeDemoTableTestUtil(getXViewer());
      cdialog.createDialogArea(getSingleEditorOrFail().getTaskActionPage().getTaskComposite());
      cdialog.handleAddAllItemButtonClick();
   }

}
