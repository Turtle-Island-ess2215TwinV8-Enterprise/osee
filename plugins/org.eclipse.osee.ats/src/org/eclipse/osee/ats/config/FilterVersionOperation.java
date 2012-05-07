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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.nebula.widgets.xviewer.Activator;
import org.eclipse.osee.ats.core.client.config.TeamDefinitionArtifact;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

public class FilterVersionOperation extends AbstractOperation {

   private final Artifact teamDefinition;
   private final Artifact[] additional;

   public FilterVersionOperation(String operationName, Set<Artifact> result, Artifact teamDefinition, Artifact... additionalToBeRemoved) {
      super(operationName, Activator.PLUGIN_ID);
      this.teamDefinition = teamDefinition;
      this.additional = additionalToBeRemoved;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      final Set<Artifact> filteredVersions = new HashSet<Artifact>(getVersions(TaskConfiguration.this.program));
      try {
         this.teamDefinition = (TeamDefinitionArtifact) iter.next();
         for (Artifact taskConf : this.teamDefinition.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Group)) {
            List<Artifact> keys = taskConf.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Members);
            for (int i = 0; i < keys.size(); i++) {
               if (keys.get(i).equals(this.teamDefinition)) {
                  filteredVersions.remove(keys.get(keys.size() - 1 - i));
                  break;
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE,
            FilterVersionOperation.class.getSimpleName() + " unable to update versions. Ex: " + ex);
      }
      filteredVersions.remove(version);
   }

}
