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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
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
import org.eclipse.ui.forms.widgets.FormToolkit;

public class TaskConfiguration extends AbstractBlam {
   private static final String TASK_CONFIGURATION = TaskConfiguration.class.getSimpleName();
   private static final String TASK_CONFIGURATION_ARTIFACTS = "Task Configuration Artifacts";
   private static final String PROGRAM = "Program";
   private static final String VERSION_ARTIFACTS = "Version Artifacts";

   private XListViewer taskConfigurationArtifacts;
   private XListViewer versionsWidget;
   private IAtsProgram program;
   private VersionArtifact version;

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
         monitor.beginTask(workStatement, 2);
         taskConfiguration =
            ArtifactTypeManager.addArtifact(CoreArtifactTypes.UniversalGroup, version.getBranch(), TASK_CONFIGURATION);
         monitor.worked(1);
         version.addChild(taskConfiguration);
         monitor.worked(1);
         taskConfiguration.addAttribute(CoreAttributeTypes.Description, String.format(
            "%s artifact relates 2 key artifacts to determine automatic task creation for a particular Branch",
            TASK_CONFIGURATION));

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
         taskConfigurationArtifacts.addSelectionChangedListener(new TaskConfigurationChangedListener());
      }
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("Define");
   }

   private class ProgramChangedListener implements ISelectionChangedListener {
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
               versionsWidget.setInput(versionArtifacts);
               version = null;
               taskConfigurationArtifacts.setInput(null);
            } catch (OseeCoreException ex) {
               System.out.println("Error while processing. Exception:" + ex);
            }
         }
      }
   };

   private class VersionChangedListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();

         Iterator<?> iter = selection.iterator();
         if (iter.hasNext()) {
            version = (VersionArtifact) iter.next();

            Map<String, Artifact> nameTotaskCreation = new HashMap<String, Artifact>();
            try {
               for (Artifact taskCreationNode : version.getChildren()) {
                  if (taskCreationNode.isOfType(CoreArtifactTypes.UniversalGroup)) {
                     List<Artifact> artifactKeys = getKeysFromTaskCreationNode(taskCreationNode);
                     if (artifactKeys.size() == 2) {
                        Artifact key1 = artifactKeys.get(0);
                        Artifact key2 = artifactKeys.get(1);

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

            taskConfigurationArtifacts.setInput(nameTotaskCreation.keySet());
         }
      }
   };

   /**
    * Pulls first found keys from child and returns it as a list
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
   private List<Artifact> getKeysFromTaskCreationNode(Artifact child) throws OseeCoreException {
      List<Artifact> result = new ArrayList<Artifact>();

      Set<Class<?>> typeSet = new CopyOnWriteArraySet<Class<?>>();
      typeSet.add(VersionArtifact.class);
      typeSet.add(TeamDefinitionArtifact.class);

      List<Artifact> keys = child.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Members);
      for (Artifact key : keys) {
         for (Class<?> type : typeSet) {
            if (type.equals(key.getClass())) {
               result.add(key);
               typeSet.remove(type);
            }
         }
      }
      return result;
   }

   private class TaskConfigurationChangedListener implements ISelectionChangedListener {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelectionProvider().getSelection();
      }
   };
}