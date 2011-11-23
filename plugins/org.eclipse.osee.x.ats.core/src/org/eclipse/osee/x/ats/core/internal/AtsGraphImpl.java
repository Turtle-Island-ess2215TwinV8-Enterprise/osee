/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.x.ats.core.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.shared.AtsArtifactTypes;
import org.eclipse.osee.ats.shared.AtsRelationTypes;
import org.eclipse.osee.framework.core.data.Identity;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.Graph;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.x.ats.AtsException;
import org.eclipse.osee.x.ats.AtsGraph;
import org.eclipse.osee.x.ats.core.internal.data.ActionImpl;
import org.eclipse.osee.x.ats.core.internal.data.ProductImpl;
import org.eclipse.osee.x.ats.core.internal.data.TaskImpl;
import org.eclipse.osee.x.ats.core.internal.data.VersionImpl;
import org.eclipse.osee.x.ats.data.Action;
import org.eclipse.osee.x.ats.data.Goal;
import org.eclipse.osee.x.ats.data.HasTasks;
import org.eclipse.osee.x.ats.data.Product;
import org.eclipse.osee.x.ats.data.Review;
import org.eclipse.osee.x.ats.data.Task;
import org.eclipse.osee.x.ats.data.Version;

/**
 * @author Roberto E. Escobar
 */
public class AtsGraphImpl implements AtsGraph {

   private final Graph graph;
   private final QueryFactory queryFactory;

   public AtsGraphImpl(QueryFactory queryFactory, Graph graph) {
      this.queryFactory = queryFactory;
      this.graph = graph;
   }

   @Override
   public List<Task> getTasks(Review review) throws AtsException {
      return getTasksHelper(review);
   }

   @Override
   public List<Task> getTasks(Goal goal) throws AtsException {
      return getTasksHelper(goal);
   }

   @Override
   public List<Task> getTasks(Action action) throws AtsException {
      return getTasksHelper(action);
   }

   private List<Task> getTasksHelper(Identity<String> identity) throws AtsException {
      String actionId = identity.getGuid();
      List<Task> toReturn = new ArrayList<Task>();
      try {
         QueryBuilder builder = queryFactory.fromBranch(CoreBranches.COMMON);
         ReadableArtifact artifact = builder.andGuidsOrHrids(actionId).getResults().getExactlyOne();
         List<ReadableArtifact> artTasks = graph.getRelatedArtifacts(artifact, AtsRelationTypes.SmaToTask_Task);
         for (ReadableArtifact art : artTasks) {
            toReturn.add(new TaskImpl(art));
         }
      } catch (OseeCoreException ex) {
         throw new AtsException(ex);
      }
      return toReturn;
   }

   @Override
   public HasTasks getContainer(Task task) throws AtsException {
      HasTasks container = null;
      TaskImpl impl = (TaskImpl) task;
      try {
         ReadableArtifact proxiedObject =
            graph.getRelatedArtifact(impl.getProxiedObject(), AtsRelationTypes.SmaToTask_Task);
         container = new ActionImpl(proxiedObject, this);
      } catch (OseeCoreException ex) {
         throw new AtsException(ex);
      }
      return container;
   }

   @Override
   public List<Version> getVersions(Product product) throws AtsException {
      List<Version> versions = new ArrayList<Version>();
      ProductImpl impl = (ProductImpl) product;
      try {
         List<ReadableArtifact> artifacts =
            graph.getRelatedArtifacts(impl.getProxiedObject(), AtsRelationTypes.TeamDefinitionToVersion_Version);
         for (ReadableArtifact art : artifacts) {
            versions.add(new VersionImpl(art));
         }
      } catch (OseeCoreException ex) {
         throw new AtsException(ex);
      }
      return versions;
   }

   @Override
   public Version getTargetedVersion(Action action) throws AtsException {
      return null;
   }

   @Override
   public Product getProduct(Action action) throws AtsException {
      return null;
   }

   @Override
   public List<Product> getProducts() throws AtsException {
      List<Product> products = new ArrayList<Product>();
      try {
         QueryBuilder builder = queryFactory.fromBranch(CoreBranches.COMMON);
         builder.andIsOfType(AtsArtifactTypes.TeamDefinition);
         List<ReadableArtifact> artifacts = builder.getResults().getList();
         for (ReadableArtifact art : artifacts) {
            products.add(new ProductImpl(art, this));
         }
      } catch (OseeCoreException ex) {
         throw new AtsException(ex);
      }
      return products;
   }
}
