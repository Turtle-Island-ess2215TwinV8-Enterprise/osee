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
package org.eclipse.osee.display.presenter.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.IRelationTypeSide;
import org.eclipse.osee.framework.core.data.Identity;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.enums.RelationTypeMultiplicity;
import org.eclipse.osee.framework.core.model.type.RelationType;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;

/**
 * @author John Misinco
 */
public class MockArtifact implements ReadableArtifact {

   private final Map<IRelationTypeSide, List<ReadableArtifact>> relationMap =
      new HashMap<IRelationTypeSide, List<ReadableArtifact>>();
   private final List<RelationType> validRelationTypes = new LinkedList<RelationType>();

   private final HashCollection<IAttributeType, String> attributes = new HashCollection<IAttributeType, String>();

   private final String name, guid;
   private final IArtifactType type;
   private final IOseeBranch branch;
   private ReadableArtifact parent;

   public MockArtifact(String guid, String name) {
      this(guid, name, CoreArtifactTypes.Artifact, CoreBranches.COMMON);
   }

   public MockArtifact(String guid, String name, IArtifactType type, IOseeBranch branch) {
      this.guid = guid;
      this.name = name;
      this.type = type;
      this.branch = branch;
      addAttribute(CoreAttributeTypes.Name, name);
   }

   public ReadableArtifact getParent() {
      return parent;
   }

   public void setParent(ReadableArtifact parent) {
      this.parent = parent;
   }

   public void addAttribute(IAttributeType type, String value) {
      attributes.put(type, value);
   }

   public Collection<RelationType> getValidRelationTypes() {
      return validRelationTypes;
   }

   public void addRelationType(RelationType relationType) {
      if (!validRelationTypes.contains(relationType)) {
         validRelationTypes.add(relationType);
      }
   }

   @SuppressWarnings("unchecked")
   public Collection<ReadableArtifact> getRelatedArtifacts(IRelationTypeSide side) {
      return (Collection<ReadableArtifact>) (relationMap.containsKey(side) ? relationMap.get(side) : Collections.emptyList());
   }

   public void addRelation(IRelationTypeSide relation, ReadableArtifact artifact) {
      List<ReadableArtifact> artList = relationMap.get(relation);
      if (artList == null) {
         artList = new LinkedList<ReadableArtifact>();
         relationMap.put(relation, artList);
      }
      artList.add(artifact);
      RelationType type =
         new RelationType(relation.getGuid(), relation.getName(), "sideA", "sideB", CoreArtifactTypes.Artifact,
            CoreArtifactTypes.Artifact, RelationTypeMultiplicity.MANY_TO_MANY, "");
      addRelationType(type);
   }

   @Override
   public int getGammaId() {
      return 0;
   }

   @Override
   public ModificationType getModificationType() {
      return null;
   }

   @Override
   public int getId() {
      return 0;
   }

   @Override
   public IOseeBranch getBranch() {
      return branch;
   }

   @Override
   public String getHumanReadableId() {
      return null;
   }

   @Override
   public int getTransactionId() {
      return 0;
   }

   @Override
   public IArtifactType getArtifactType() {
      return type;
   }

   @Override
   public Collection<IAttributeType> getAttributeTypes() {
      return attributes.keySet();
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> List<ReadableAttribute<T>> getAttributes(IAttributeType attributeType) {
      Collection<String> values = attributes.getValues(attributeType);
      List<ReadableAttribute<T>> toReturn = null;
      if (values != null && !values.isEmpty()) {
         toReturn = new LinkedList<ReadableAttribute<T>>();
         for (String value : values) {
            ReadableAttribute<T> attr = (ReadableAttribute<T>) new MockAttribute(attributeType, value);
            toReturn.add(attr);
         }
      } else {
         toReturn = Collections.emptyList();
      }
      return toReturn;
   }

   @Override
   public String getSoleAttributeAsString(IAttributeType attributeType) {
      return null;
   }

   @Override
   public String getGuid() {
      return guid;
   }

   @Override
   public boolean matches(Identity<?>... identities) {
      return false;
   }

   @Override
   public String getName() {
      return name;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T> List<ReadableAttribute<T>> getAttributes() {
      List<ReadableAttribute<T>> toReturn = new ArrayList<ReadableAttribute<T>>();
      for (Entry<IAttributeType, Collection<String>> entry : attributes.entrySet()) {
         for (String value : entry.getValue()) {
            toReturn.add((ReadableAttribute<T>) new MockAttribute(entry.getKey(), value));
         }
      }
      return toReturn;
   }

   public void clearRelations() {
      validRelationTypes.clear();
      relationMap.clear();
   }

   @Override
   public String getSoleAttributeAsString(IAttributeType attributeType, String defaultValue) {
      return null;
   }

   @Override
   public boolean isOfType(IArtifactType... otherTypes) {
      for (IArtifactType type : otherTypes) {
         if (this.type == type) {
            return true;
         }
      }
      return false;
   }

}