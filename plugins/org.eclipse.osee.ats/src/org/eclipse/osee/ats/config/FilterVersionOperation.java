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

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.nebula.widgets.xviewer.Activator;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * <p>
 * From a set of all artifacts A, operation removes any found artifacts B, A - B, found in traversal of following
 * relationship. <br>
 * Given a Team Definition artifact, operation get its related < {@code CoreRelationTypes.Universal_Grouping__Group}
 * artifacts (usually its Task Configuration artifacts) and
 * </p>
 *
 * <pre>
 *
 *
 *
 * </pre>
 */
public class FilterVersionOperation extends AbstractOperation {

   private final Artifact teamDefinition;
   private final Artifact[] additionalToFilter;
   private final Set<Artifact> allVersions;
   private final Set<Artifact> result;

   public FilterVersionOperation(String operationName, Set<Artifact> allVersions, Set<Artifact> result, Artifact teamDefinition, Artifact... additionalToBeRemoved) {
      super(operationName, FilterVersionOperation.class.getName());
      this.allVersions = allVersions;
      this.result = result;
      this.teamDefinition = teamDefinition;
      this.additionalToFilter = additionalToBeRemoved;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      if (monitor == null) {
         monitor = new NullProgressMonitor();
      }

      result.addAll(this.allVersions);

      try {
         for (Artifact taskConf : teamDefinition.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Group)) {
            List<Artifact> keys = taskConf.getRelatedArtifacts(CoreRelationTypes.Universal_Grouping__Members);
            for (int i = 0; i < keys.size(); i++) {
               if (keys.get(i).equals(teamDefinition)) {
                  result.remove(keys.get(keys.size() - 1 - i));
                  break;
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE,
            FilterVersionOperation.class.getSimpleName() + " unable to update versions. Ex: " + ex);
      }

      for (Artifact version : additionalToFilter) {
         result.remove(version);
      }

      monitor.done();
   }

}
