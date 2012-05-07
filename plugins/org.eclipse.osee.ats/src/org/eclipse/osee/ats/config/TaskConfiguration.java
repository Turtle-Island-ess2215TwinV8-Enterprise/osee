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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTypeManager;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XListViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.util.DynamicXWidgetLayout;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * TODO: refactor threads into "workers" with input and output
 */
public class TaskConfiguration extends AbstractBlam {
   private static final String TASK_CONFIGURATION = TaskConfiguration.class.getSimpleName();
   private static final String TASK_CONFIGURATION_ARTIFACTS = "Task Configuration Artifacts";
   private static final String TEAM_DEFINITIONS = "Available Team Definitions";
   private static final String AVAILABLE_VERSION_ARTIFACTS = "Available Version Artifacts";
   private static final String PROGRAM = "Program";
   private static final String VERSION_ARTIFACTS = "Version Artifacts";
   private static final String DELETE_CHECKBOX =
      "Delete mode (selected Task Configuration artifact(s) will be deleted)";

   private XListViewer taskConfigurationArtifacts;
   private XListViewer versionsWidget;
   private XListViewer availableTeamDefinitionsWidget;
   private XListViewer availableOtherVersionsWidget;
   //private IAtsProgram program;
   private VersionArtifact version;

   private static Set<Class<?>> keyTypes;
   static {
      keyTypes = new HashSet<Class<?>>(2);
      keyTypes.add(VersionArtifact.class);
      keyTypes.add(TeamDefinitionArtifact.class);
   }

   public TaskConfiguration() {
      super(null, String.format(
         "Creates new %s artifact on selected Version artifacts, associates via Parent-Child relationship.",
         TASK_CONFIGURATION), BlamUiSource.FILE);
   }

   @Override
   public void runOperation(VariableMap map, IProgressMonitor monitor) throws Exception {
      if (monitor == null) {
         monitor = new NullProgressMonitor();
      }

      Artifact taskConfiguration = null;
      if (version != null) {
         String workStatement =
            String.format("Created \"%s\" artifact on Version Artifact:[%s]", TASK_CONFIGURATION, version);
         monitor.beginTask(workStatement, 3);

         taskConfiguration =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.UniversalGroup, version.getBranch(), TASK_CONFIGURATION);
         monitor.worked(1);

         version.addChild(taskConfiguration);
         monitor.worked(1);

         taskConfiguration.addAttribute(CoreAttributeTypes.Description, String.format(
            "%s artifact relates 2 key artifacts to determine automatic task creation for a particular Branch",
            TASK_CONFIGURATION));
         monitor.worked(1);

         version.persist(String.format("[%s] added Task configuration to [%s]", getClass().getSimpleName(), version));
      } else {
         throw new OseeStateException("Version artifact not selected");
      }
      monitor.done();
   }

   @Override
   public void widgetCreated(XWidget xWidget, FormToolkit toolkit, Artifact art, DynamicXWidgetLayout dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) {
      String label = xWidget.getLabel();
      if (PROGRAM.equalsIgnoreCase(label)) {
         XComboViewer viewer = (XComboViewer) xWidget;
         viewer.addSelectionChangedListener(new ProgramChangedListener());
      } else if (VERSION_ARTIFACTS.equalsIgnoreCase(label)) {
         versionsWidget = (XListViewer) xWidget;
         versionsWidget.addSelectionChangedListener(new VersionChangedListener());
      } else if (TASK_CONFIGURATION_ARTIFACTS.equalsIgnoreCase(label)) {
         taskConfigurationArtifacts = (XListViewer) xWidget;
         //taskConfigurationArtifacts.addSelectionChangedListener(new TaskConfigurationChangedListener());
      } else if (TEAM_DEFINITIONS.equalsIgnoreCase(label)) {
         availableTeamDefinitionsWidget = (XListViewer) xWidget;
         //teamDefinitionsWidget.addSelectionChangedListener(new VersionChangedListener());
      } else if (AVAILABLE_VERSION_ARTIFACTS.equalsIgnoreCase(label)) {
         availableOtherVersionsWidget = (XListViewer) xWidget;
         //otherVersionsWidget.addSelectionChangedListener(new VersionChangedListener());
      } else if (DELETE_CHECKBOX.equalsIgnoreCase(label)) {
         xWidget.addXModifiedListener(new XModifiedListener() {
            @Override
            public void widgetModified(XWidget widget) {
               availableTeamDefinitionsWidget.setVisible(!availableTeamDefinitionsWidget.isVisible());
               availableOtherVersionsWidget.setVisible(!availableOtherVersionsWidget.isVisible());

               Control control = availableOtherVersionsWidget.getControl();
               ScrolledForm sf = null;

               while (sf == null && control != null) {
                  if (control instanceof ScrolledForm) {
                     sf = (ScrolledForm) control;
                  } else {
                     control = control.getParent();
                  }
               }

               availableOtherVersionsWidget.getControl().getParent().getParent().layout();

               if (sf != null) {
                  sf.reflow(true);
               }

               //               Section section =
               //                  (Section) availableOtherVersionsWidget.getControl().getParent().getParent().getParent();
               //               section.setExpanded(true);
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
                  final IAtsProgram atsProgram = (IAtsProgram) iter.next();
                  try {
                     final Collection<VersionArtifact> versionArtifacts = new ArrayList<VersionArtifact>();

                     TeamDefinitionArtifact holdingTeamDef = atsProgram.getTeamDefHoldingVersions();
                     final List<Artifact> teamDefs = new ArrayList<Artifact>();

                     if (holdingTeamDef != null) {
                        teamDefs.addAll(holdingTeamDef.getDescendants());
                        versionArtifacts.addAll(holdingTeamDef.getVersionsArtifacts());
                     }

                     Displays.ensureInDisplayThread(new Runnable() {
                        @Override
                        public void run() {
                           //program = atsProgram;
                           version = null;
                           versionsWidget.setInput(versionArtifacts);
                           availableTeamDefinitionsWidget.setInput(teamDefs);
                           taskConfigurationArtifacts.setInput(null);
                        }
                     });
                  } catch (OseeCoreException ex) {
                     System.out.println("Error while processing. Exception:" + ex);
                  }
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
                  final Map<String, Artifact> nameTotaskCreation = new HashMap<String, Artifact>();
                  try {
                     for (Artifact taskCreationNode : version.getChildren()) {
                        if (taskCreationNode.isOfType(CoreArtifactTypes.UniversalGroup)) {
                           List<Artifact> allKeyNodes =
                              taskCreationNode.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Members);

                           Set<Artifact> resultNodes = new HashSet<Artifact>(2);
                           getKeys(allKeyNodes.iterator(), resultNodes);

                           //List<Artifact> artifactKeys = getKeysFromTaskCreationNode(taskCreationNode);

                           //for loop, since they provide their end type...
                           if (resultNodes.size() == 2) {
                              Artifact key1 = resultNodes.get(0);
                              Artifact key2 = resultNodes.get(1);

                              String guiName =
                                 String.format("[%s]:[%s] -- [%s]:[%s]", key1.getArtifactTypeName(), key1.getName(),
                                    key2.getArtifactTypeName(), key2.getName());

                              nameTotaskCreation.put(guiName, taskCreationNode);
                           }
                        }
                     }
                  } catch (OseeCoreException ex) {
                     OseeLog.log(Activator.class, Level.INFO,
                        String.format("Unable to retrieve %s artifacts", TASK_CONFIGURATION));
                  }
                  Displays.ensureInDisplayThread(new Runnable() {
                     @Override
                     public void run() {
                        taskConfigurationArtifacts.setInput(nameTotaskCreation.keySet());
                     }
                  });
               }
            }
         }).start();
      }
   };

   /**
    * Pulls first found keys from child node and returns it as a list. <br/>
    * Will loop till it finds first 2 or runs out of member nodes.
    *
    * <pre>
    *                              (Key 1) Version Artifact
    *                             /
    *    (Task Creation) child --`.
    *                           \  \
    *                           |    (Key 2) Team Definition
    *                           |
    *                           \
    *                            (Key n) Artifact
    * </pre>
    *
    * @throws OseeCoreException
    */
   private List<Artifact> getKeysFromTaskCreationNode(Artifact taskCreationNode) throws OseeCoreException {
      List<Artifact> result = new ArrayList<Artifact>();

      Set<Class<?>> typeSet = new HashSet<Class<?>>();
      typeSet.add(VersionArtifact.class);
      typeSet.add(TeamDefinitionArtifact.class);

      List<Artifact> keys = taskCreationNode.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Members);
      Class<?> foundType = null;
      for (int i = 0; result.size() < 3 && i < keys.size(); i++) { //method
         Artifact key = keys.get(i);
         for (Class<?> type : typeSet) { //method
            if (type.equals(key.getClass())) {
               foundType = type;
               break;
            }
         }

         if (foundType != null) {
            result.add(key);
            typeSet.remove(foundType); //pop
            foundType = null;
         }

      }
      return result;
   }

   private Set<Artifact> getKeys(Iterator<Artifact> nodeIter, Set<Artifact> foundNodes) {
      handleNode(nodeIter.next(), foundNodes);
      return nodeIter.hasNext() && foundNodes.size() < 3 ? getKeys(nodeIter, foundNodes) : foundNodes;
   }

   private void handleNode(Artifact artifact, Set<Artifact> storage) {
      if (keyTypes.contains(artifact.getClass())) {
         storage.add(artifact);
      }
   }

   private class TaskConfigurationChangedListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();
      }
   };
}