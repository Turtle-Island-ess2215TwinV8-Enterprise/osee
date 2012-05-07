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

import java.rmi.activation.Activator;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osee.ats.core.client.config.IAtsProgram;
import org.eclipse.osee.ats.core.client.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.client.version.VersionArtifact;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.operation.NullOperationLogger;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XListViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.util.DynamicXWidgetLayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * TODO:
 * <ul>
 * <li>refactor threads into "workers" with input and output</li>
 * <li>after you hit play, reset certain widgets</li>
 * </ul>
 */
public class TaskConfiguration extends AbstractBlam {
   private static final String TASK_CONFIGURATION = TaskConfiguration.class.getSimpleName();
   private static final String TASK_CONFIGURATION_ARTIFACTS = "Task Configuration Artifacts";
   private static final String AVAILABLE_TEAM_DEFINITIONS = "Available Team Definitions";
   private static final String AVAILABLE_VERSION_ARTIFACTS = "Available Version Artifacts";
   private static final String PROGRAM = "Program";
   private static final String VERSION_ARTIFACTS = "Version Artifacts";
   private static final String DELETE_CHECKBOX =
      "Delete mode (selected Task Configuration artifact(s) will be deleted)";

   private XListViewer taskConfigurationArtifacts;
   private XComboViewer programWidget;
   private XListViewer versionsWidget;
   private XListViewer availableTeamDefinitionsWidget;
   private XListViewer availableOtherVersionsWidget;
   private IAtsProgram program;
   private VersionArtifact version;

   private TeamDefinitionArtifact selectedTeamDefinition;
   private VersionArtifact selectedVersion;

   private final Set<Artifact> selectedTaskConfigurationArtifacts;

   public TaskConfiguration() {
      super(null, String.format(
         "Creates new %s artifact on selected Version artifacts, associates via Parent-Child relationship.",
         TASK_CONFIGURATION), BlamUiSource.FILE);
      selectedTaskConfigurationArtifacts = new HashSet<Artifact>();
   }

   @Override
   public void runOperation(VariableMap map, IProgressMonitor monitor) throws Exception {
      if (monitor == null) {
         monitor = new NullProgressMonitor();
      }

      boolean deleteTaskConfiguration = map.getBoolean(DELETE_CHECKBOX);
      boolean success = false;

      if (version != null) {

         String workStatement =
            String.format("%s \"%s\" artifact", deleteTaskConfiguration ? "Deleted" : "Created", TASK_CONFIGURATION);
         monitor.beginTask(workStatement, deleteTaskConfiguration ? 1 : 3);

         if (deleteTaskConfiguration) {
            if (selectedTaskConfigurationArtifacts.isEmpty()) {
               throw new IllegalArgumentException("You must select some task configuration artifacts to delete.");
            }
            for (Artifact taskConfiguration : selectedTaskConfigurationArtifacts) {
               taskConfiguration.delete();
               taskConfiguration.persist(String.format("[%s] removed Task configuration from [%s]",
                  getClass().getSimpleName(), version));
            }
            success = true;
         } else {
            if (selectedTeamDefinition == null || selectedVersion == null) {
               monitor.setCanceled(true);
               throw new IllegalArgumentException("Missing selections of Version and TeamDefinition");
            }

            Artifact taskConfiguration =
               ArtifactTypeManager.addArtifact(CoreArtifactTypes.UniversalGroup, version.getBranch(),
                  TASK_CONFIGURATION);
            monitor.worked(1);

            version.addChild(taskConfiguration);
            monitor.worked(1);

            taskConfiguration.addAttribute(CoreAttributeTypes.Description, String.format(
               "%s artifact relates 2 key artifacts to determine automatic task creation for a particular Branch",
               TASK_CONFIGURATION));
            monitor.worked(1);

            taskConfiguration.addRelation(CoreRelationTypes.Universal_Grouping__Members, selectedTeamDefinition);
            taskConfiguration.addRelation(CoreRelationTypes.Universal_Grouping__Members, selectedVersion);

            version.persist(String.format("[%s] added Task configuration to [%s]", getClass().getSimpleName(), version));
            success = true;
         }

         //redraw available option widgets
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               versionsWidget.setInput(null);
               version = null;
               availableOtherVersionsWidget.setInput(null);
               selectedVersion = null;
               availableTeamDefinitionsWidget.setInput(null);
               selectedTeamDefinition = null;
               taskConfigurationArtifacts.setInput(null);
               selectedTaskConfigurationArtifacts.clear();
            }
         });
      } else {
         throw new OseeStateException("Version artifact not selected");
      }
      monitor.done();

      AWorkbench.popup("Result", success ? "Added Task Configuration" : "Failed adding Task Configuration");
   }

   @Override
   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, DynamicXWidgetLayout dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) {
      String label = xWidget.getLabel();
      if (PROGRAM.equalsIgnoreCase(label)) {
         programWidget = (XComboViewer) xWidget;
         programWidget.addSelectionChangedListener(new ProgramChangedListener());
      } else if (VERSION_ARTIFACTS.equalsIgnoreCase(label)) {
         versionsWidget = (XListViewer) xWidget;
         versionsWidget.addSelectionChangedListener(new VersionChangedListener());
      } else if (TASK_CONFIGURATION_ARTIFACTS.equalsIgnoreCase(label)) {
         taskConfigurationArtifacts = (XListViewer) xWidget;
      } else if (AVAILABLE_TEAM_DEFINITIONS.equalsIgnoreCase(label)) {
         availableTeamDefinitionsWidget = (XListViewer) xWidget;
         availableTeamDefinitionsWidget.addSelectionChangedListener(new AvailTeamDefinitionSelectedListener());
      } else if (AVAILABLE_VERSION_ARTIFACTS.equalsIgnoreCase(label)) {
         availableOtherVersionsWidget = (XListViewer) xWidget;
         availableOtherVersionsWidget.addSelectionChangedListener(new AvailableVersionArtifacts());
      } else if (DELETE_CHECKBOX.equalsIgnoreCase(label)) {
         xWidget.addXModifiedListener(new XModifiedListener() {
            @Override
            public void widgetModified(XWidget widget) {
               availableTeamDefinitionsWidget.setVisible(!availableTeamDefinitionsWidget.isVisible());
               availableOtherVersionsWidget.setVisible(!availableOtherVersionsWidget.isVisible());
            }
         });
      }
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("Define");
   }

   private class ProgramChangedListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(final SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();
         final Iterator<?> iter = selection.iterator();
         new Thread(new Runnable() {
            @Override
            public void run() {
               if (iter.hasNext()) {
                  program = (IAtsProgram) iter.next();

                  final Set<Artifact> versionArtifacts = getVersions(program);
                  Displays.ensureInDisplayThread(new Runnable() {
                     @Override
                     public void run() {
                        version = null;
                        versionsWidget.setInput(versionArtifacts);
                        taskConfigurationArtifacts.setInput(null);

                        availableTeamDefinitionsWidget.setInput(null);
                        selectedTeamDefinition = null;
                        availableOtherVersionsWidget.setInput(null);
                        selectedVersion = null;
                     }
                  });
               }
            }
         }).start();
      }
   };

   private class VersionChangedListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();
         final Iterator<?> iter = selection.iterator();
         new Thread(new Runnable() {
            @Override
            public void run() {
               if (iter.hasNext()) {
                  version = (VersionArtifact) iter.next();

                  final Map<String, Artifact> stringTotaskCreationMap = new HashMap<String, Artifact>();
                  Operations.executeAsJob(new TaskConfigurationQuery("Search for task configuration setup...",
                     NullOperationLogger.getSingleton(), version, stringTotaskCreationMap), true);

                  final ISelectionChangedListener taskCreationListener = new ISelectionChangedListener() {
                     private final Map<String, Artifact> map = stringTotaskCreationMap;

                     @Override
                     public void selectionChanged(SelectionChangedEvent event) {
                        IStructuredSelection selection =
                           (IStructuredSelection) event.getSelectionProvider().getSelection();
                        Iterator<?> iter = selection.iterator();
                        if (iter.hasNext()) {
                           String name = (String) iter.next();
                           Artifact value = map.get(name);
                           if (value != null) {
                              selectedTaskConfigurationArtifacts.add(value);
                           }
                        }
                     }
                  };

                  final Set<Artifact> versionArtifacts = getVersions(TaskConfiguration.this.program);
                  versionArtifacts.remove(version);
                  final Set<Artifact> teamDefinitons = getTeamDefinitions(TaskConfiguration.this.program);

                  Displays.ensureInDisplayThread(new Runnable() {
                     @Override
                     public void run() {
                        taskConfigurationArtifacts.setInput(stringTotaskCreationMap.keySet());
                        availableTeamDefinitionsWidget.setInput(teamDefinitons);
                        availableOtherVersionsWidget.setInput(versionArtifacts);
                        taskConfigurationArtifacts.addSelectionChangedListener(taskCreationListener);
                     }
                  });
               }
            }
         }).start();
      }
   };

   private class AvailTeamDefinitionSelectedListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();
         final Iterator<?> iter = selection.iterator();
         if (iter.hasNext()) {
            final Set<Artifact> result = new HashSet<Artifact>();
            new Thread(new Runnable() {
               @Override
               public void run() {
                  selectedTeamDefinition = (TeamDefinitionArtifact) iter.next();

                  Operations.executeAsJob(new FilterVersionOperation("Filtering Version Artifacts", result,
                     selectedTeamDefinition, version), true);

                  Displays.ensureInDisplayThread(new Runnable() {
                     @Override
                     public void run() {
                        availableOtherVersionsWidget.setInput(result);
                        selectedVersion = null;
                     }
                  });
               }
            });
         }
      }
   }

   private class AvailableVersionArtifacts implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();
         final Iterator<?> iter = selection.iterator();
         if (iter.hasNext()) {
            selectedVersion = (VersionArtifact) iter.next();
         }
      }
   }

   private Set<Artifact> getTeamDefinitions(IAtsProgram program) {
      final Set<Artifact> teamDefs = new HashSet<Artifact>();
      try {
         TeamDefinitionArtifact holdingTeamDef = program.getTeamDefHoldingVersions();
         if (holdingTeamDef != null) {
            teamDefs.addAll(holdingTeamDef.getDescendants());
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.INFO,
            String.format("Unable to retrieve artifacts in %s", ProgramChangedListener.class.getSimpleName()));
      }
      return teamDefs;
   }

   private Set<Artifact> getVersions(IAtsProgram program) {
      Set<Artifact> versions = new HashSet<Artifact>();
      try {
         Collection<VersionArtifact> pulled = program.getTeamDefHoldingVersions().getVersionsArtifacts();
         if (!pulled.isEmpty()) {
            versions.addAll(Collections.castAll(Artifact.class, pulled));
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, String.format(
            "Unable to get version artifacts [%s].getVersionsSet() for program: [%s], Exception:[%s]",
            TaskConfiguration.class.getSimpleName(), program, ex));
      }
      return versions;
   }
}