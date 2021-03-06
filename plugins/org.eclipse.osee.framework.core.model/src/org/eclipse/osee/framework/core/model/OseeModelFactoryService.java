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
package org.eclipse.osee.framework.core.model;

import org.eclipse.osee.framework.core.model.type.ArtifactTypeFactory;
import org.eclipse.osee.framework.core.model.type.AttributeTypeFactory;
import org.eclipse.osee.framework.core.model.type.OseeEnumTypeFactory;
import org.eclipse.osee.framework.core.model.type.RelationTypeFactory;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryService;

/**
 * @author Roberto E. Escobar
 */
public class OseeModelFactoryService implements IOseeModelFactoryService {

   private final BranchFactory branchFactory;
   private final TransactionRecordFactory txFactory;
   private final ArtifactTypeFactory artTypeFactory;
   private final AttributeTypeFactory attrTypeFactory;
   private final RelationTypeFactory relTypeFactory;
   private final OseeEnumTypeFactory oseEnumTypeFactory;

   public OseeModelFactoryService(BranchFactory branchFactory, TransactionRecordFactory txFactory, ArtifactTypeFactory artTypeFactory, AttributeTypeFactory attrTypeFactory, RelationTypeFactory relTypeFactory, OseeEnumTypeFactory oseEnumTypeFactory) {
      this.branchFactory = branchFactory;
      this.txFactory = txFactory;
      this.artTypeFactory = artTypeFactory;
      this.attrTypeFactory = attrTypeFactory;
      this.relTypeFactory = relTypeFactory;
      this.oseEnumTypeFactory = oseEnumTypeFactory;
   }

   @Override
   public BranchFactory getBranchFactory() {
      return branchFactory;
   }

   @Override
   public TransactionRecordFactory getTransactionFactory() {
      return txFactory;
   }

   @Override
   public ArtifactTypeFactory getArtifactTypeFactory() {
      return artTypeFactory;
   }

   @Override
   public AttributeTypeFactory getAttributeTypeFactory() {
      return attrTypeFactory;
   }

   @Override
   public RelationTypeFactory getRelationTypeFactory() {
      return relTypeFactory;
   }

   @Override
   public OseeEnumTypeFactory getOseeEnumTypeFactory() {
      return oseEnumTypeFactory;
   }
}
