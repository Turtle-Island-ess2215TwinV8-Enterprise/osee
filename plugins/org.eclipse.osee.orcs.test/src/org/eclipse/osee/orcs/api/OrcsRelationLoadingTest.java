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
package org.eclipse.osee.orcs.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.enums.Operator;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.ApplicationContext;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.OrcsIntegrationRule;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.GraphReadable;
import org.eclipse.osee.orcs.db.mock.OseeDatabase;
import org.eclipse.osee.orcs.db.mock.OsgiService;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.junit.Assert;
import org.junit.Rule;

/**
 * Test Case for {@link OrcsApi}
 * 
 * @author Andrew M. Finkbeiner
 */
public class OrcsRelationLoadingTest {

   @Rule
   public OrcsIntegrationRule osgi = new OrcsIntegrationRule(this);

   @Rule
   public OseeDatabase db = new OseeDatabase("osee.demo.h2");

   @OsgiService
   private OrcsApi orcsApi;

   @org.junit.Test
   public void testSearchById() throws Exception {
      ApplicationContext context = null; // TODO use real application context

      QueryFactory queryFactory = orcsApi.getQueryFactory(context);
      GraphReadable graph = orcsApi.getGraph(context);
      checkRelationsForCommonBranch(orcsApi, queryFactory, graph, context);
      checkRelationsForSawBranch(orcsApi, queryFactory, graph, context);

   }

   private void checkRelationsForCommonBranch(OrcsApi oseeApi, QueryFactory queryFactory, GraphReadable graph, ApplicationContext context) throws OseeCoreException {
      QueryBuilder builder = queryFactory.fromBranch(CoreBranches.COMMON).andLocalIds(Arrays.asList(6, 7, 8));
      ResultSet<ArtifactReadable> resultSet = builder.getResults();
      List<ArtifactReadable> moreArts = resultSet.getList();

      Assert.assertEquals(3, moreArts.size());
      Assert.assertEquals(3, builder.getCount());

      Map<Integer, ArtifactReadable> lookup = creatLookup(moreArts);
      ArtifactReadable art6 = lookup.get(6);
      ArtifactReadable art7 = lookup.get(7);
      ArtifactReadable art8 = lookup.get(8);

      //art 6 has no relations
      Assert.assertEquals(0, graph.getExistingRelationTypes(art6).size());
      //art 7 has 3 
      //      REL_LINK_ID    REL_LINK_TYPE_ID     A_ART_ID    B_ART_ID    RATIONALE   GAMMA_ID    TX_CURRENT     MOD_TYPE    BRANCH_ID   TRANSACTION_ID    GAMMA_ID  
      //      1  219   7  8     53
      //      3  219   7  15    54
      //      2  219   1  7     52
      Assert.assertEquals(2, graph.getExistingRelationTypes(art7).size());
      Assert.assertEquals(2,
         graph.getRelatedArtifacts(CoreRelationTypes.Default_Hierarchical__Child, art7).getList().size());
      Assert.assertEquals(1,
         graph.getRelatedArtifacts(CoreRelationTypes.Default_Hierarchical__Parent, art7).getList().size());

      //art8 has 
      //      REL_LINK_ID    REL_LINK_TYPE_ID     A_ART_ID    B_ART_ID    RATIONALE   GAMMA_ID    TX_CURRENT     MOD_TYPE    BRANCH_ID   TRANSACTION_ID    GAMMA_ID
      //      7  233   8  20    62
      //      8  233   8  21    63
      //      4  233   8  17    74
      //      6  233   8  19    76
      //      5  233   8  18    78
      //      1  219   7  8     53
      Assert.assertEquals(2, graph.getExistingRelationTypes(art8).size());
      Assert.assertEquals(1,
         graph.getRelatedArtifacts(CoreRelationTypes.Default_Hierarchical__Parent, art8).getList().size());
      Assert.assertEquals(5, graph.getRelatedArtifacts(CoreRelationTypes.Users_User, art8).getList().size());

   }

   private void checkRelationsForSawBranch(OrcsApi oseeApi, QueryFactory queryFactory, GraphReadable graph, ApplicationContext context) throws OseeCoreException {
      QueryBuilder builder =
         queryFactory.fromBranch(oseeApi.getBranchCache().getByName("SAW_Bld_1").iterator().next()).and(
            CoreAttributeTypes.Name, Operator.EQUAL, "Design Constraints");
      ResultSet<ArtifactReadable> resultSet = builder.getResults();
      List<ArtifactReadable> moreArts = resultSet.getList();

      Assert.assertFalse(moreArts.isEmpty());
      ArtifactReadable artifact = moreArts.iterator().next();

      //art 7 has no relations

      //artifact has 3 children and 1 parent

      Assert.assertEquals(2, graph.getExistingRelationTypes(artifact).size());
      Assert.assertEquals(3,
         graph.getRelatedArtifacts(CoreRelationTypes.Default_Hierarchical__Child, artifact).getList().size());
      Assert.assertEquals(1,
         graph.getRelatedArtifacts(CoreRelationTypes.Default_Hierarchical__Parent, artifact).getList().size());
   }

   Map<Integer, ArtifactReadable> creatLookup(List<ArtifactReadable> arts) {
      Map<Integer, ArtifactReadable> lookup = new HashMap<Integer, ArtifactReadable>();
      for (ArtifactReadable artifact : arts) {
         lookup.put(artifact.getLocalId(), artifact);
      }
      return lookup;
   }
}
