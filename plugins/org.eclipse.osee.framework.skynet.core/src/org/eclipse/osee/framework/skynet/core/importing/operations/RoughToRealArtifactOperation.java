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
package org.eclipse.osee.framework.skynet.core.importing.operations;

import static org.eclipse.osee.framework.core.enums.CoreRelationTypes.Default_Hierarchical__Parent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.IRelationSorterId;
import org.eclipse.osee.framework.core.enums.RelationOrderBaseTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.RelationType;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.importing.RoughArtifact;
import org.eclipse.osee.framework.skynet.core.importing.RoughRelation;
import org.eclipse.osee.framework.skynet.core.importing.resolvers.IArtifactImportResolver;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationTypeManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Roberto E. Escobar
 */
public class RoughToRealArtifactOperation extends AbstractOperation {
   private final SkynetTransaction transaction;
   private final RoughArtifactCollector rawData;
   private final IArtifactImportResolver artifactResolver;
   private final Map<RoughArtifact, Artifact> roughToRealArtifact;
   private final Artifact destinationArtifact;
   private final IRelationSorterId importArtifactOrder;
   private final boolean deleteUnmatchedArtifacts;
   private Collection<Artifact> unmatchedArtifacts;

   public RoughToRealArtifactOperation(SkynetTransaction transaction, Artifact destinationArtifact, RoughArtifactCollector rawData, IArtifactImportResolver artifactResolver, boolean deleteUnmatchedArtifacts) {
      super("Materialize Artifacts", Activator.PLUGIN_ID);
      this.rawData = rawData;
      this.transaction = transaction;
      this.artifactResolver = artifactResolver;
      this.destinationArtifact = destinationArtifact;
      this.importArtifactOrder = RelationOrderBaseTypes.USER_DEFINED;
      this.roughToRealArtifact = new HashMap<RoughArtifact, Artifact>();
      this.deleteUnmatchedArtifacts = deleteUnmatchedArtifacts;
      roughToRealArtifact.put(rawData.getParentRoughArtifact(), this.destinationArtifact);
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      monitor.setTaskName("Creating Artifacts");
      this.unmatchedArtifacts = destinationArtifact.getDescendants();
      int totalItems = rawData.getRoughArtifacts().size() + rawData.getRoughRelations().size();
      int unitOfWork = calculateWork((double) totalItems / getTotalWorkUnits());

      for (RoughArtifact roughArtifact : rawData.getParentRoughArtifact().getChildren()) {
         Artifact child = createArtifact(monitor, roughArtifact, destinationArtifact);
         if (child != null && !child.hasParent()) {
            destinationArtifact.addChild(importArtifactOrder, child);
         }
         monitor.worked(unitOfWork);
      }

      monitor.setTaskName("Creating Relations");
      for (RoughRelation roughRelation : rawData.getRoughRelations()) {
         createRelation(monitor, roughRelation);
         monitor.worked(unitOfWork);
      }

      if (deleteUnmatchedArtifacts && !unmatchedArtifacts.isEmpty()) {
         System.out.println("Will delete:");
         for (Artifact toDelete : unmatchedArtifacts) {
            System.out.println(String.format("\t%s (%s)", toDelete.getName(), toDelete.getGuid()));
            toDelete.deleteAndPersist(transaction);
         }
      }
   }

   private Artifact createArtifact(IProgressMonitor monitor, RoughArtifact roughArtifact, Artifact realParent) throws OseeCoreException {
      Artifact realArtifact = roughToRealArtifact.get(roughArtifact);
      if (realArtifact != null) {
         return realArtifact;
      }

      realArtifact = artifactResolver.resolve(roughArtifact, transaction.getBranch(), realParent, destinationArtifact);
      unmatchedArtifacts.remove(realArtifact);

      for (RoughArtifact childRoughArtifact : roughArtifact.getChildren()) {
         Artifact childArtifact = createArtifact(monitor, childRoughArtifact, realArtifact);
         if (areValid(realArtifact, childArtifact)) {
            removeOtherParent(childArtifact, realArtifact);
            if (!childArtifact.hasParent()) {
               realArtifact.addChild(importArtifactOrder, childArtifact);
            }
         }
      }

      if (realArtifact != null) {
         realArtifact.persist(transaction);
      }
      return realArtifact;
   }

   private void removeOtherParent(Artifact child, Artifact parent) throws OseeCoreException {
      if (hasDifferentParent(child, parent)) {
         child.deleteRelations(Default_Hierarchical__Parent);
         child.persist(transaction);
      }
   }

   private boolean hasDifferentParent(Artifact art, Artifact parent) throws OseeCoreException {
      return art.hasParent() && !art.getParent().equals(parent);
   }

   // TODO move these two functions into jdk.core or find existing library functions
   private boolean isValid(Artifact art) {
      return art != null && !art.isDeleted();
   }

   private boolean areValid(Artifact... artifacts) {
      boolean returnValue = true;
      for (Artifact art : artifacts) {
         returnValue &= isValid(art);
      }
      return returnValue;
   }

   private void createRelation(IProgressMonitor monitor, RoughRelation roughRelation) throws OseeCoreException {
      RelationType relationType = RelationTypeManager.getType(roughRelation.getRelationTypeName());
      Artifact aArt = ArtifactQuery.getArtifactFromId(roughRelation.getAartifactGuid(), transaction.getBranch());
      Artifact bArt = ArtifactQuery.getArtifactFromId(roughRelation.getBartifactGuid(), transaction.getBranch());

      if (aArt == null || bArt == null) {
         OseeLog.log(Activator.class, Level.WARNING,
               "The relation of type " + roughRelation.getRelationTypeName() + " could not be created.");
         if (aArt == null) {
            OseeLog.log(Activator.class, Level.WARNING,
                  "The artifact with guid: " + roughRelation.getAartifactGuid() + " does not exist.");
         }
         if (bArt == null) {
            OseeLog.log(Activator.class, Level.WARNING,
                  "The artifact with guid: " + roughRelation.getBartifactGuid() + " does not exist.");
         }
      } else {
         try {
            monitor.subTask(aArt.getName() + " <--> " + bArt.getName());
            monitor.worked(1);
            RelationManager.addRelation(importArtifactOrder, relationType, aArt, bArt, roughRelation.getRationale());
            aArt.persist(transaction);
         } catch (IllegalArgumentException ex) {
            OseeLog.log(Activator.class, Level.WARNING, ex.getLocalizedMessage());
         }
      }
   }
}
