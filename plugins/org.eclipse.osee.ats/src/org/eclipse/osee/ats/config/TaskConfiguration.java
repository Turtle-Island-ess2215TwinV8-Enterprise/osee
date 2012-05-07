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
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
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
import org.eclipse.ui.forms.widgets.FormToolkit;

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
   private IAtsProgram program;
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
         availableTeamDefinitionsWidget.addSelectionChangedListener(new AvailTeamDefinitionSelectedListener());
      } else if (AVAILABLE_VERSION_ARTIFACTS.equalsIgnoreCase(label)) {
         availableOtherVersionsWidget = (XListViewer) xWidget;
         //otherVersionsWidget.addSelectionChangedListener(new VersionChangedListener());
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

                  final Collection<VersionArtifact> versionArtifacts = new ArrayList<VersionArtifact>();
                  final List<Artifact> teamDefs = new ArrayList<Artifact>();

                  try {
                     TeamDefinitionArtifact holdingTeamDef = program.getTeamDefHoldingVersions();
                     if (holdingTeamDef != null) {
                        teamDefs.addAll(holdingTeamDef.getDescendants());
                        versionArtifacts.addAll(holdingTeamDef.getVersionsArtifacts());
                     }
                  } catch (OseeCoreException ex) {
                     System.out.println("Error while processing. Exception:" + ex);
                  }

                  Displays.ensureInDisplayThread(new Runnable() {
                     @Override
                     public void run() {
                        version = null;
                        versionsWidget.setInput(versionArtifacts);
                        availableTeamDefinitionsWidget.setInput(teamDefs);
                        availableOtherVersionsWidget.setInput(versionArtifacts);
                        taskConfigurationArtifacts.setInput(null);
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

                  final Map<String, Artifact> nameTotaskCreation = new HashMap<String, Artifact>();
                  try {
                     for (Artifact taskCreationNode : version.getChildren()) {
                        if (taskCreationNode.isOfType(CoreArtifactTypes.UniversalGroup)) {
                           List<Artifact> allKeyNodes =
                              taskCreationNode.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Members);

                           Set<Artifact> result = getKeys(allKeyNodes.iterator(), new HashSet<Artifact>(2));
                           Artifact[] nodes = result.toArray(new Artifact[result.size()]);
                           if (nodes.length == 2) {
                              Artifact key1 = nodes[0];
                              Artifact key2 = nodes[1];

                              String guiName =
                                 String.format("[%s]:[%s] --- [%s]:[%s]", key1.getArtifactTypeName(), key1.getName(),
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

   private class AvailTeamDefinitionSelectedListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();
         final Iterator<?> iter = selection.iterator();
         new Thread(new Runnable() {
            @Override
            public void run() {
               if (iter.hasNext()) {

                  final Set<VersionArtifact> availVersions = getVersionsSet(TaskConfiguration.this.program);

                  try {
                     //----- start candidate for recursive code ------
                     TeamDefinitionArtifact availTeamDefintion = (TeamDefinitionArtifact) iter.next();
                     List<Artifact> taskConfigurations =
                        availTeamDefintion.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Group); //path step 1
                     if (!taskConfigurations.isEmpty()) {
                        for (Artifact artifact : taskConfigurations) {
                           List<Artifact> versionList =
                              artifact.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Members); //path step 2
                           if (!versionList.isEmpty()) {
                              for (Artifact artifact2 : versionList) {
                                 if (artifact2 instanceof VersionArtifact) {
                                    availVersions.remove(artifact2);
                                 }
                              }
                           }
                        }
                     }
                     //----- end candidate for recursive code ------

                  } catch (Exception ex) {
                     OseeLog.log(
                        Activator.class,
                        Level.SEVERE,
                        AvailTeamDefinitionSelectedListener.class.getSimpleName() + " unable to update versions. Ex:" + ex);
                  }

                  Displays.ensureInDisplayThread(new Runnable() {
                     @Override
                     public void run() {
                        availableOtherVersionsWidget.setInput(availVersions);
                     }
                  });
               }
            }
         }).start();
      }
   };

   /**
    * Just traverse to the end and return first specific type
    */
   private List<Artifact> navigatePath(Artifact startNode, Class<?> searchedItem, IRelationTypeSide[] path, int pathIndex) throws OseeCoreException {
      if (pathIndex < path.length) {
         return navigatePath(startNode.getRelatedArtifacts(path[pathIndex]).iterator().next(), searchedItem, path,
            pathIndex + 1);
      }
      return startNode.getRelatedArtifacts(path[path.length - 1]);
   }

   /**
    * Pulls first found keys from child node and returns it as a list. <br/>
    * Will loop till it finds first 2 or runs out of member nodes.
    *
    * <pre>
    *                                (Key 1) Version Artifact
    *                               /
    *    (Task Creation) child ---.`
    *                           \  \
    *                           |    (Key 2) Team Definition
    *                           |
    *                           \
    *                            (Key n) Artifact
    * </pre>
    *
    * TODO protect against loops & seen elements
    *
    * @throws OseeCoreException
    */
   private Set<Artifact> getKeys(Iterator<Artifact> nodeIter, Set<Artifact> foundNodes) {
      if (nodeIter.hasNext()) {
         handleNode(nodeIter.next(), foundNodes);
      }
      return nodeIter.hasNext() && foundNodes.size() < 3 ? getKeys(nodeIter, foundNodes) : foundNodes;
   }

   private void handleNode(Artifact artifact, Set<Artifact> storage) {
      if (keyTypes.contains(artifact.getClass())) {
         storage.add(artifact);
      }
   }

   private Set<VersionArtifact> getVersionsSet(IAtsProgram program) {
      Set<VersionArtifact> versions = new HashSet<VersionArtifact>();
      try {
         Collection<VersionArtifact> pulled = program.getTeamDefHoldingVersions().getVersionsArtifacts();
         if (!pulled.isEmpty()) {
            versions.addAll(pulled);
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, String.format(
            "Unable to get version artifacts [%s].getVersionsSet() for program: [%s], Excetion:[%s]",
            TaskConfiguration.class.getSimpleName(), program, ex));
      }
      return versions;
   }

   private class TaskConfigurationChangedListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();
      }
   };
}