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
public class GraphNodeTest {

   // API Version 2
   public void main() throws OseeCoreException {
      Api api = getApi();

      ActiveView view = api.getActiveView(CoreBranches.COMMON);

      Artifact parent = (Artifact) view.createQuery().andGuidsOrHrids("GUID").getResults().getExactlyOne();
      List<Artifact> requirements = view.graph(parent).getRelated(CoreRelationTypes.CodeRequirement_Requirement);

      // TRANSACTION
      Transaction tx = view.createTransaction(whoamI(), "TX COMMENT");

      WArtifact folder = tx.createArtifact(CoreArtifactTypes.Folder, "Requirements");

      tx.graph(parent).addChild(folder);
      tx.graph(folder).addChildren(requirements);

      Assert.assertNull(view.graph(parent).getChildren());
      Assert.assertEquals(folder, tx.graph(parent).getChildren());

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

      GraphNode graph(Artifact artifact);
   }

   private static interface Transaction {

      WGraphNode graph(Artifact artifact);

      TransactionRecord commit() throws OseeCoreException;

      WArtifact createArtifact(IArtifactType type, String name) throws OseeCoreException;

   }

   private static interface CanQuery {
      QueryBuilder createQuery();
   }

   //   private static interface Graph {
   //
   //      List<GraphNode<Artifact>> getParent(Artifact artifact);
   //
   //      List<GraphNode<Artifact>> getChildren(Artifact artifact);
   //
   //      List<GraphNode<Artifact>> getRelated(IRelationTypeSide relationTypeSide, Artifact artifact) throws OseeCoreException;
   //
   //   }

   private static interface ChangeSet {

   }

   private static interface Artifact extends ArtifactReadable {

      List<RelationType> getValidRelationTypes() throws OseeCoreException;

      Collection<IRelationTypeSide> getExistingRelationTypes();

   }

   private static interface WArtifact extends ArtifactWriteable, Artifact {

   }

   private static interface GraphNode {
      List<Artifact> getParent();

      List<Artifact> getChildren();

      List<Artifact> getRelated(IRelationTypeSide relationTypeSide) throws OseeCoreException;
   }

   private static interface WGraphNode extends GraphNode {
      void addChild(Artifact child) throws OseeCoreException;

      void addChildren(List<? extends Artifact> children);

      void relate(IRelationType type, Artifact side2) throws OseeCoreException;

      void unrelate(IRelationType type, Artifact side2) throws OseeCoreException;

      void setRationale(IRelationType type, Artifact side2, String value) throws OseeCoreException;
   }

   ////////////////////////////////////////////////
   public Api getApi() {
      return null;
   }

   public Artifact whoamI() {
      return null;
   }
}
