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

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osee.ats.core.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.version.VersionArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.shared.VersionLockedType;
import org.eclipse.osee.ats.shared.VersionReleaseType;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.ArtifactViewerSorter;
import org.eclipse.osee.framework.ui.skynet.widgets.XListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class ActionTeamVersionListDialog extends ActionTeamListDialog {

   XListViewer versionList = new XListViewer("Version");
   VersionArtifact selectedVersion = null;

   public ActionTeamVersionListDialog(Active active) {
      super(active);
   }

   @Override
   protected Control createDialogArea(Composite container) {

      super.createDialogArea(container);
      getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
         @Override
         public void selectionChanged(SelectionChangedEvent event) {
            try {
               Collection<Object> objs = new HashSet<Object>();
               if (getTableViewer().getSelection().isEmpty()) {
                  return;
               }
               IStructuredSelection sel = (IStructuredSelection) getTableViewer().getSelection();
               for (Artifact art : ((TeamDefinitionArtifact) sel.iterator().next()).getVersionsFromTeamDefHoldingVersions(
                  VersionReleaseType.Both, VersionLockedType.Both)) {
                  objs.add(art);
               }
               versionList.setInput(objs);
            } catch (Exception ex) {
               OseeLog.log(Activator.class, Level.SEVERE, ex);
            }
         }
      });

      Composite comp = new Composite(container, SWT.NONE);
      comp.setLayout(new GridLayout(1, false));
      comp.setLayoutData(new GridData(GridData.FILL_BOTH));

      versionList.setLabelProvider(new VersionArtifactLabelProvider());
      versionList.setContentProvider(new ArrayContentProvider());
      versionList.setSorter(new ArtifactViewerSorter());
      versionList.setGrabHorizontal(true);
      versionList.setMultiSelect(false);
      versionList.createWidgets(comp, 2);
      versionList.addSelectionListener(new SelectionListener() {
         @Override
         public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
         }

         @Override
         public void widgetSelected(SelectionEvent e) {
            if (versionList.getSelected().isEmpty()) {
               selectedVersion = null;
            } else {
               selectedVersion = (VersionArtifact) versionList.getSelected().iterator().next();
            }
         };
      });

      return container;
   }

   /**
    * @return the selectedVersion
    */
   public VersionArtifact getSelectedVersion() {
      return selectedVersion;
   }

}
