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
package org.eclipse.osee.ats.navigate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.artifact.TaskableStateMachineArtifact;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.artifact.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.artifact.VersionArtifact;
import org.eclipse.osee.ats.artifact.VersionArtifact.VersionReleaseType;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.widgets.ReviewManager;
import org.eclipse.osee.ats.util.widgets.XHyperlabelTeamDefinitionSelection;
import org.eclipse.osee.ats.world.WorldEditor;
import org.eclipse.osee.ats.world.WorldEditorParameterSearchItem;
import org.eclipse.osee.ats.world.search.TeamWorldSearchItem;
import org.eclipse.osee.ats.world.search.TeamWorldSearchItem.ReleasedOption;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.widgets.XCheckBox;
import org.eclipse.osee.framework.ui.skynet.widgets.XCombo;
import org.eclipse.osee.framework.ui.skynet.widgets.XMembersCombo;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayout;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayoutData;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.IDynamicWidgetLayoutListener;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Donald G. Dunne
 */
public class TeamWorkflowSearchWorkflowSearchItem extends WorldEditorParameterSearchItem {

   protected XHyperlabelTeamDefinitionSelection teamCombo = null;
   protected XCombo releasedCombo = null;
   protected XCombo versionCombo = null;
   protected XMembersCombo assigneeCombo;
   protected XCheckBox includeCompletedCancelledCheckbox;
   protected XCheckBox showFlatCheckbox;

   public TeamWorkflowSearchWorkflowSearchItem(String name) throws OseeArgumentException {
      super(name, AtsImage.TEAM_WORKFLOW);
   }

   public TeamWorkflowSearchWorkflowSearchItem() throws OseeArgumentException {
      this("Team Workflow Search");
   }

   public TeamWorkflowSearchWorkflowSearchItem(TeamWorkflowSearchWorkflowSearchItem editTeamWorkflowSearchItem) throws OseeArgumentException {
      super(editTeamWorkflowSearchItem, AtsImage.TEAM_WORKFLOW);
   }

   @Override
   public TeamWorkflowSearchWorkflowSearchItem copy() throws OseeArgumentException {
      return new TeamWorkflowSearchWorkflowSearchItem(this);
   }

   public TeamWorkflowSearchWorkflowSearchItem copyProvider() throws OseeArgumentException {
      return new TeamWorkflowSearchWorkflowSearchItem(this);
   }

   @Override
   public String getParameterXWidgetXml() throws OseeCoreException {
      return "<xWidgets>" +
      //
      "<XWidget xwidgetType=\"XHyperlabelTeamDefinitionSelection\" displayName=\"Team Definitions(s)\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget xwidgetType=\"XCombo()\" beginComposite=\"10\" displayName=\"Version\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget xwidgetType=\"XCombo(Both,Released,UnReleased)\" displayName=\"Released\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget xwidgetType=\"XMembersCombo\" displayName=\"Assignee\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget xwidgetType=\"XCheckBox\" displayName=\"Include Completed/Cancelled\" defaultValue=\"false\" labelAfter=\"true\" horizontalLabel=\"true\"/>" +
      //
      "<XWidget xwidgetType=\"XCheckBox\" displayName=\"Show Flat\" defaultValue=\"false\" labelAfter=\"true\" horizontalLabel=\"true\" toolTip=\"Show Tasks/Reviews flattened instead of hierarchcial\"/>" +
      //
      "</xWidgets>";
   }

   @Override
   public Collection<? extends Artifact> performSearchGetResults(SearchType searchType) throws OseeCoreException {
      Collection<Artifact> artifacts =
            new TeamWorldSearchItem("", getSelectedTeamDefinitions(), isIncludeCompletedCancelledCheckbox(), false,
                  false, getSelectedVersionArtifact(), getSelectedUser(), getSelectedReleased()).performSearchGetResults(false);
      return filterShowFlat(artifacts);
   }

   protected Collection<? extends Artifact> filterShowFlat(Collection<Artifact> artifacts) throws OseeCoreException {
      if (!isShowFlatCheckbox()) {
         return artifacts;
      }
      Set<Artifact> results = new HashSet<Artifact>(artifacts);
      for (Artifact artifact : artifacts) {
         if (artifact instanceof TaskableStateMachineArtifact) {
            results.addAll(((TaskableStateMachineArtifact) artifact).getTaskArtifacts());
         }
         if (artifact instanceof TeamWorkFlowArtifact) {
            results.addAll(ReviewManager.getReviews((TeamWorkFlowArtifact) artifact));
         }
      }
      return results;
   }

   @Override
   public String getSelectedName(SearchType searchType) throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      Collection<TeamDefinitionArtifact> teamDefs = getSelectedTeamDefinitions();
      if (teamDefs.size() > 0) {
         sb.append(" - Teams: " + org.eclipse.osee.framework.jdk.core.util.Collections.toString(",", teamDefs));
      }
      if (getSelectedVersionArtifact() != null) {
         sb.append(" - Version: " + getSelectedVersionArtifact());
      }
      ReleasedOption releaseOption = getSelectedReleased();
      if (releaseOption != null && releaseOption != ReleasedOption.Both) {
         sb.append(" - ReleasedOption: " + releaseOption);
      }
      if (getSelectedUser() != null) {
         sb.append(" - Assignee: " + getSelectedUser());
      }
      if (isIncludeCompletedCancelledCheckbox()) {
         sb.append(" - Include Completed/Cancelled");
      }
      return Strings.truncate(getName() + sb.toString(), WorldEditor.TITLE_MAX_LENGTH, true);
   }

   @Override
   public void widgetCreated(XWidget widget, FormToolkit toolkit, Artifact art, DynamicXWidgetLayout dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) throws OseeCoreException {
      if (widget.getLabel().equals("Assignee")) {
         assigneeCombo = (XMembersCombo) widget;
      }
      if (widget.getLabel().equals("Include Completed/Cancelled")) {
         includeCompletedCancelledCheckbox = (XCheckBox) widget;
      }
      if (widget.getLabel().equals("Show Flat")) {
         showFlatCheckbox = (XCheckBox) widget;
      }
      if (widget.getLabel().equals("Version")) {
         versionCombo = (XCombo) widget;
         versionCombo.getComboBox().setVisibleItemCount(25);
         widget.setToolTip("Select Team to populate Version list");
      }
      if (widget.getLabel().equals("Released")) {
         releasedCombo = (XCombo) widget;
      }
      if (widget.getLabel().equals("Team Definitions(s)")) {
         teamCombo = (XHyperlabelTeamDefinitionSelection) widget;
         teamCombo.addXModifiedListener(new XModifiedListener() {
            @Override
            public void widgetModified(XWidget widget) {
               if (versionCombo != null) {
                  try {
                     Collection<TeamDefinitionArtifact> teamDefArts = getSelectedTeamDefinitions();
                     if (teamDefArts.size() == 0) {
                        versionCombo.setDataStrings(new String[] {});
                        return;
                     }
                     TeamDefinitionArtifact teamDefHoldingVersions =
                           teamDefArts.iterator().next().getTeamDefinitionHoldingVersions();
                     if (teamDefHoldingVersions == null) {
                        versionCombo.setDataStrings(new String[] {});
                        return;
                     }
                     Collection<String> names =
                           Artifacts.artNames(teamDefHoldingVersions.getVersionsArtifacts(VersionReleaseType.Both));
                     if (names.size() == 0) {
                        versionCombo.setDataStrings(new String[] {});
                        return;
                     }
                     versionCombo.setDataStrings(names.toArray(new String[names.size()]));
                  } catch (Exception ex) {
                     OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
                  }
               }
            }
         });
      }
   }

   protected User getSelectedUser() {
      if (assigneeCombo == null) return null;
      return assigneeCombo.getUser();
   }

   public void setSelectedUser(User user) {
      if (assigneeCombo != null) assigneeCombo.set(user);
   }

   protected boolean isIncludeCompletedCancelledCheckbox() {
      if (includeCompletedCancelledCheckbox == null) return false;
      return includeCompletedCancelledCheckbox.isSelected();
   }

   public void includeCompletedCancelledCheckbox(boolean selected) {
      if (includeCompletedCancelledCheckbox != null) includeCompletedCancelledCheckbox.set(selected);
   }

   protected boolean isShowFlatCheckbox() {
      if (showFlatCheckbox == null) return false;
      return showFlatCheckbox.isSelected();
   }

   public void includeShowFlatCheckbox(boolean selected) {
      if (showFlatCheckbox != null) showFlatCheckbox.set(selected);
   }

   protected VersionArtifact getSelectedVersionArtifact() throws OseeCoreException {
      if (versionCombo == null) return null;
      String versionStr = versionCombo.get();
      if (versionStr == null || versionStr.equals("")) return null;
      Collection<TeamDefinitionArtifact> teamDefs = getSelectedTeamDefinitions();
      if (teamDefs.size() > 0) {
         TeamDefinitionArtifact teamDefHoldingVersions = teamDefs.iterator().next().getTeamDefinitionHoldingVersions();
         if (teamDefHoldingVersions == null) return null;
         for (VersionArtifact versionArtifact : teamDefHoldingVersions.getVersionsArtifacts(VersionReleaseType.Both)) {
            if (versionArtifact.getName().equals(versionStr)) {
               return versionArtifact;
            }
         }
      }
      return null;
   }

   public void setVersion(String versionStr) {
      if (versionCombo != null && versionCombo.getInDataStrings() != null) {
         // should check if the version combo was populated
         if (versionCombo.getInDataStrings().length > 0) {
            versionCombo.set(versionStr);
         }
      }
   }

   public Collection<TeamDefinitionArtifact> getSelectedTeamDefinitions() throws OseeCoreException {
      if (teamCombo == null) return java.util.Collections.emptyList();
      return teamCombo.getSelectedTeamDefintions();
   }

   public void setSelectedTeamDefinitions(Collection<TeamDefinitionArtifact> selectedTeamDefs) {
      if (teamCombo != null) {
         teamCombo.setSelectedTeamDefs(selectedTeamDefs);
         teamCombo.notifyXModifiedListeners();
      }
   }

   protected ReleasedOption getSelectedReleased() throws OseeCoreException {
      if (releasedCombo == null || releasedCombo.get() == null || releasedCombo.get().equals("")) {
         return ReleasedOption.Both;
      }
      return ReleasedOption.valueOf(releasedCombo.get());
   }

   public void setSelectedReleased(ReleasedOption option) {
      if (releasedCombo != null) releasedCombo.set(option.toString());
   }

   @Override
   public void createXWidgetLayoutData(DynamicXWidgetLayoutData layoutData, XWidget widget, FormToolkit toolkit, Artifact art, XModifiedListener modListener, boolean isEditable) throws OseeCoreException {
   }

   @Override
   public void widgetCreating(XWidget widget, FormToolkit toolkit, Artifact art, DynamicXWidgetLayout dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) throws OseeCoreException {
   }

   @Override
   public Result isParameterSelectionValid() throws OseeCoreException {
      try {
         boolean selected = false;
         Collection<TeamDefinitionArtifact> teamDefs = getSelectedTeamDefinitions();
         if (teamDefs.size() > 0) {
            selected = true;
         }
         VersionArtifact verArt = getSelectedVersionArtifact();
         if (verArt != null) {
            selected = true;
         }
         User user = getSelectedUser();
         if (user != null) {
            selected = true;
         }
         boolean includeCompleted = isIncludeCompletedCancelledCheckbox();
         if (!selected) {
            return new Result("You must select at least Team, Version or Assignee.");
         }
         if (user != null && includeCompleted) {
            return new Result("Assignee and Include Completed are not compatible selections.");
         }
         if (user != null && includeCompleted && verArt == null && teamDefs.size() == 0) {
            return new Result("You must select at least Team or Version with Include Completed.");
         }
         return Result.TrueResult;
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         return new Result("Exception: " + ex.getLocalizedMessage());
      }
   }

   @Override
   public IDynamicWidgetLayoutListener getDynamicWidgetLayoutListener() {
      return null;
   }

   @Override
   public VersionArtifact getTargetedVersionArtifact() throws OseeCoreException {
      if (versionCombo == null) return null;
      return getSelectedVersionArtifact();
   }

}
