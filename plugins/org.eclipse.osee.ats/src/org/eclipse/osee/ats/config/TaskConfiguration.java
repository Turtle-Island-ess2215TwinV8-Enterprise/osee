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
package org.eclipse.osee.ats.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osee.ats.core.client.config.IAtsProgram;
import org.eclipse.osee.ats.core.client.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.client.version.VersionArtifact;
import org.eclipse.osee.ats.util.widgets.XTeamDefinitionCombo;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XListDropViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.util.DynamicXWidgetLayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class TaskConfiguration extends AbstractBlam {
   private static final String PROGRAM = "Program";
   private static final String VERSION_ARTIFACTS = "Version Artifacts";

   private XListDropViewer versionsWidget;
   private XTeamDefinitionCombo xTeamDefinitionCombo;

   private IAtsProgram program;
   private VersionArtifact version;

   public TaskConfiguration() {
      super(null,
         "Creates new TaskCreation artifact on selected Version artifacts, associates via Parent-Child relationship.",
         BlamUiSource.FILE);
   }

   @Override
   public void runOperation(VariableMap map, IProgressMonitor monitor) throws Exception {
      if (monitor == null) {
         monitor = new NullProgressMonitor();
      }

      String workStatement = String.format("Created \"Task Creation\" artifact on Version Artifact:[%s]", version);
      monitor.beginTask(workStatement, 2);
      Artifact artifact =
         ArtifactTypeManager.addArtifact(CoreArtifactTypes.TaskCreation, version.getBranch(), "Task Creation");
      monitor.worked(1);
      version.addChild(artifact);
      monitor.worked(1);
      version.persist(String.format("[%s] added Task configuration to [%s]", getClass().getSimpleName(), version));
      monitor.done();
   }

   @Override
   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, DynamicXWidgetLayout dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) {
      if (xWidget instanceof XComboViewer && PROGRAM.equals(xWidget.getLabel())) {
         XComboViewer viewer = (XComboViewer) xWidget;
         viewer.addSelectionChangedListener(new ProgramSelectionListener());
      } else if (xWidget instanceof XListDropViewer && VERSION_ARTIFACTS.equals(xWidget.getLabel())) {
         versionsWidget = (XListDropViewer) xWidget;
         versionsWidget.addSelectionChangedListener(new VersionSelectionListener());
      }
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("Define");
   }

   private class VersionSelectionListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();

         Iterator<?> iter = selection.iterator();
         if (iter.hasNext()) {
            version = (VersionArtifact) iter.next();
         }
      }
   };

   private class ProgramSelectionListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();

         Iterator<?> iter = selection.iterator();
         if (iter.hasNext()) {
            program = (IAtsProgram) iter.next();
            try {
               TeamDefinitionArtifact teamDefinitionArtifact = program.getTeamDefHoldingVersions();
               final Collection<VersionArtifact> versionArtifacts = new ArrayList<VersionArtifact>();
               if (teamDefinitionArtifact != null) {
                  versionArtifacts.addAll(teamDefinitionArtifact.getVersionsArtifacts());
               }
               Displays.ensureInDisplayThread(new Runnable() {
                  @Override
                  public void run() {
                     versionsWidget.setInput(versionArtifacts);
                  }
               });
            } catch (OseeCoreException ex) {
               System.out.println("Error while processing. Exception:" + ex);
            }
         }
      }
   };
}