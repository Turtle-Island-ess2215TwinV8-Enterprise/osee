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
package org.eclipse.osee.orcs.prototype;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationType;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.data.ITransaction;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.TransactionRecord;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.ArtifactWriteable;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.junit.Assert;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactIsAGraphNodeTest {

   // Version 3
   public void main() throws OseeCoreException {
      Api api = getApi();

      ActiveView view = api.getActiveView(CoreBranches.COMMON);

      Artifact parent = (Artifact) view.createQuery().andGuidsOrHrids("GUID").getResults().getExactlyOne();
      List<Artifact> requirements = view.graph(parent).getRelated(CoreRelationTypes.CodeRequirement_Requirement);

      // TRANSACTION
      Transaction tx = view.createTransaction(whoamI(), "TX COMMENT");
      WArtifact folder = tx.createArtifact(CoreArtifactTypes.Folder, "Requirements");

      folder.setParent(parent);
      folder.addChildren(requirements);

      Assert.assertNull(view.graph(parent).getChildren());

      Assert.assertEquals(parent, folder.getParent());
      Assert.assertEquals(folder, tx.asWriteable(parent).getChildren().get(0));

      tx.commit();

      Assert.assertEquals(folder, view.graph(parent).getChildren());
   }

   ////////////////////////////////////////////////

   private static interface Api {

      ActiveView getActiveView(IOseeBranch branch) throws OseeCoreException;

      HistoricalView getView(IOseeBranch branch, ITransaction txNumber) throws OseeCoreException;

   }

   private static interface View extends CanQuery {
      IOseeBranch getBranch();

      TransactionRecord getTransaction();
   }

   private static interface HistoricalView extends View {

      Callable<ChangeSet> compare(View other);

   }

   private static interface ActiveView extends HistoricalView {
      Transaction createTransaction(Artifact userArtifact, String comment) throws OseeCoreException;

      GraphNode<Artifact> graph(Artifact artifact);
   }

   private static interface Transaction {

      TransactionRecord commit() throws OseeCoreException;

      WArtifact asWriteable(Artifact artifact);

      WArtifact createArtifact(IArtifactType type, String name) throws OseeCoreException;

   }

   private static interface CanQuery {
      QueryBuilder createQuery();
   }

   private static interface ChangeSet {

   }

   private static interface Artifact extends ArtifactReadable {

      List<RelationType> getValidRelationTypes() throws OseeCoreException;

      Collection<IRelationTypeSide> getExistingRelationTypes();

   }

   private static interface WArtifact extends ArtifactWriteable, Artifact, GraphNode<WArtifact> {

      void setParent(Artifact parent) throws OseeCoreException;

      void addChild(Artifact child) throws OseeCoreException;

      void addChildren(List<? extends Artifact> children);

      void relate(IRelationType type, Artifact other) throws OseeCoreException;

      void unrelate(IRelationType type, Artifact other) throws OseeCoreException;

      void setRationale(IRelationType type, Artifact other, String value) throws OseeCoreException;

   }

   private static interface GraphNode<T extends Artifact> {
      T getParent();

      List<T> getChildren();

      List<T> getRelated(IRelationTypeSide relationTypeSide) throws OseeCoreException;
   }

   ////////////////////////////////////////////////
   public Api getApi() {
      return null;
   }

   public Artifact whoamI() {
      return null;
   }
}
