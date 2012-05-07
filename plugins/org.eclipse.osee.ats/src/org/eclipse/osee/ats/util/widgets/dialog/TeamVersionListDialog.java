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
package org.eclipse.osee.ats.util.widgets.dialog;

import java.util.ArrayList;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osee.ats.core.client.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.client.config.TeamDefinitionManager;
import org.eclipse.osee.ats.core.client.version.VersionLockedType;
import org.eclipse.osee.ats.core.client.version.VersionReleaseType;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.ArtifactViewerSorter;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactDescriptiveLabelProvider;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboViewer;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * @author Donald G. Dunne
 */
public class TeamVersionListDialog extends SelectionDialog {

   XComboViewer teamCombo = new XComboViewer("Team", SWT.READ_ONLY);
   XComboViewer versionCombo = new XComboViewer("Version", SWT.READ_ONLY);
   Artifact selectedVersion = null;
   TeamDefinitionArtifact selectedTeamDef = null;
   private final Active active;
   private final TeamDefinitionArtifact teamDef;

   public TeamVersionListDialog(Active active) {
      this(null, active);
   }

   public TeamVersionListDialog(TeamDefinitionArtifact teamDef, Active active) {
      super(Displays.getActiveShell());
      this.teamDef = teamDef;
      this.active = active;
      setTitle("Select Version");
      setMessage("Select Version");
   }

   @Override
   protected Control createDialogArea(Composite container) {

      ArrayList<Object> objs = new ArrayList<Object>();
      try {
         for (Artifact art : TeamDefinitionManager.getTeamReleaseableDefinitions(active)) {
            objs.add(art);
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

      Composite comp = new Composite(container, SWT.NONE);
      comp.setLayout(new GridLayout(2, false));
      comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

      if (teamDef == null) {
         teamCombo.setInput(objs);
         teamCombo.setLabelProvider(new ArtifactDescriptiveLabelProvider());
         teamCombo.setContentProvider(new ArrayContentProvider());
         teamCombo.setSorter(new ArtifactViewerSorter());
         teamCombo.setGrabHorizontal(true);
         teamCombo.createWidgets(comp, 2);
         teamCombo.getCombo().setVisibleItemCount(20);
         teamCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
               widgetSelected(e);
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
               ArrayList<Object> objs = new ArrayList<Object>();
               try {
                  selectedTeamDef = (TeamDefinitionArtifact) teamCombo.getSelected();
                  for (Artifact pda : selectedTeamDef.getVersionsArtifacts(VersionReleaseType.Both,
                     VersionLockedType.Both)) {
                     objs.add(pda);
                  }
                  versionCombo.setInput(objs);
               } catch (Exception ex) {
                  OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
               }
            };
         });
      } else {
         selectedTeamDef = teamDef;
      }

      versionCombo.setLabelProvider(new ArtifactDescriptiveLabelProvider());
      versionCombo.setContentProvider(new ArrayContentProvider());
      versionCombo.setSorter(new ArtifactViewerSorter());
      versionCombo.setGrabHorizontal(true);
      versionCombo.createWidgets(comp, 2);
      versionCombo.getCombo().setVisibleItemCount(20);
      versionCombo.addSelectionListener(new SelectionListener() {
         @Override
         public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
         }

         @Override
         public void widgetSelected(SelectionEvent e) {
            selectedVersion = (Artifact) versionCombo.getSelected();
         };
      });
      if (teamDef != null) {
         objs = new ArrayList<Object>();
         try {
            for (Artifact pda : teamDef.getVersionsArtifacts(VersionReleaseType.Both, VersionLockedType.Both)) {
               objs.add(pda);
            }
            versionCombo.setInput(objs);
         } catch (Exception ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
      }

      return container;
   }

   public Artifact getSelectedVersion() {
      return selectedVersion;
   }

   /**
    * @return the selectedTeamDef
    */
   public TeamDefinitionArtifact getSelectedTeamDef() {
      return selectedTeamDef;
   }

}
