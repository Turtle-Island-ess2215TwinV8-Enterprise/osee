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
public class GraphTest {

   // Version 1
   public void main() throws OseeCoreException {
      Api api = getApi();

      HeadView view = api.getHeadView(CoreBranches.COMMON);

      ArtifactReadable parent = view.createQuery().andGuidsOrHrids("GUID").getResults().getExactlyOne();
      List<ArtifactReadable> requirements =
         view.graph().getRelated(CoreRelationTypes.CodeRequirement_Requirement, parent);

      // TRANSACTION
      Transaction tx = view.createTransaction(whoamI(), "TX COMMENT");

      ArtifactWriteable folder = tx.createArtifact(CoreArtifactTypes.Folder, "Requirements");

      tx.addChild(parent, folder);
      tx.addChildren(folder, requirements);

      Assert.assertEquals(folder, tx.graph().getChildren(parent));
      Assert.assertNull(view.graph().getChildren(parent));

      tx.commit();

      Assert.assertEquals(folder, view.graph().getChildren(parent));
   }

   ////////////////////////////////////////////////

   private static interface Api {

      HeadView getHeadView(IOseeBranch branch) throws OseeCoreException;

      HistoricalView getView(IOseeBranch branch, ITransaction txNumber) throws OseeCoreException;

   }

   private static interface View extends CanQuery {
      //
   }

   private static interface HistoricalView extends View {

      Callable<ChangeSet> compare(View other);

   }

   private static interface HeadView extends HistoricalView {
      Transaction createTransaction(ArtifactReadable userArtifact, String comment) throws OseeCoreException;

      Graph graph();
   }

   private static interface Transaction {

      Graph graph();

      TransactionRecord commit() throws OseeCoreException;

      ArtifactWriteable createArtifact(IArtifactType type, String name) throws OseeCoreException;

      void addChild(ArtifactReadable parent, ArtifactReadable child) throws OseeCoreException;

      void addChildren(ArtifactReadable parent, List<ArtifactReadable> children);

      void relate(IRelationType type, ArtifactReadable side1, ArtifactReadable side2) throws OseeCoreException;

      void unrelate(IRelationType type, ArtifactReadable side1, ArtifactReadable side2) throws OseeCoreException;

      void setRationale(IRelationType type, ArtifactReadable side1, ArtifactReadable side2, String value) throws OseeCoreException;
   }

   private static interface CanQuery {
      QueryBuilder createQuery();
   }

   private static interface Graph {
      List<ArtifactReadable> getParent(ArtifactReadable art1);

      List<ArtifactReadable> getChildren(ArtifactReadable art1);

      List<ArtifactReadable> getRelated(IRelationTypeSide relationTypeSide, ArtifactReadable art1) throws OseeCoreException;

      List<RelationType> getValidRelationTypes(ArtifactReadable art) throws OseeCoreException;

      Collection<IRelationTypeSide> getExistingRelationTypes(ArtifactReadable art);
   }

   private static interface ChangeSet {

   }

   ////////////////////////////////////////////////
   public Api getApi() {
      return null;
   }

   public ArtifactReadable whoamI() {
      return null;
   }
}
