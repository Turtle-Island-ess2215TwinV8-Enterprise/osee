/*******************************************************************************
 * Copyright (c) 2012 Boeing.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.ats.core.client.config.TeamDefinitionArtifact;
import org.eclipse.osee.ats.core.client.version.VersionArtifact;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.OperationLogger;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

public class TaskConfigurationQueryOperation extends AbstractOperation {
   private final Class<?>[] KEYS = {VersionArtifact.class, TeamDefinitionArtifact.class};

   private final Set<Class<?>> keyTypes;
   private final Artifact version;
   private final Map<String, Artifact> taskConfigurationStorage;

   public TaskConfigurationQueryOperation(String operationName, OperationLogger logger, Artifact versionArtifact, Map<String, Artifact> storage) {
      super(operationName, "org.eclipse.osee.ats.config.TaskConfigurationQuery", logger);
      this.version = versionArtifact;
      this.keyTypes = new HashSet<Class<?>>(Arrays.asList(KEYS));
      this.taskConfigurationStorage = storage;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      if (monitor == null) {
         monitor = new NullProgressMonitor();
      }

      try {
         for (Artifact taskCreationNode : version.getChildren()) {
            if (CoreArtifactTypes.UniversalGroup.equals(taskCreationNode.getArtifactType())) {
               List<Artifact> allKeyNodes =
                  taskCreationNode.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Members);

               Set<Artifact> firstFoundPair = getKeys(allKeyNodes.iterator(), new HashSet<Artifact>(2));
               if (firstFoundPair.size() == 2) {
                  Artifact[] nodes = firstFoundPair.toArray(new Artifact[firstFoundPair.size()]);
                  Artifact key1 = nodes[0];
                  Artifact key2 = nodes[1];

                  String guiName =
                     String.format("[%s]:[%s] --- [%s]:[%s]", key1.getArtifactTypeName(), key1.getName(),
                        key2.getArtifactTypeName(), key2.getName());

                  taskConfigurationStorage.put(guiName, taskCreationNode);
               }
            }
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.INFO, "Unable to retrieve TASK_CONFIGURATION artifacts");
      }

      monitor.done();
   }

   /**
    * Pulls first found keys from child node and returns it as a set. <br/>
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
}
