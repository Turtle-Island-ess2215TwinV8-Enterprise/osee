/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.model.internal.fields;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.AbstractOseeField;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Compare;

/**
 * @author Roberto E. Escobar
 */
public final class ArtifactTypeAttributesField extends AbstractOseeField<Map<IOseeBranch, Collection<AttributeType>>> {

   private final Map<IOseeBranch, Collection<AttributeType>> validityMap;

   public ArtifactTypeAttributesField(Map<IOseeBranch, Collection<AttributeType>> validityMap) {
      super();
      this.validityMap = validityMap;
   }

   @Override
   public Map<IOseeBranch, Collection<AttributeType>> get() {
      return new HashMap<IOseeBranch, Collection<AttributeType>>(validityMap);
   }

   public void put(IOseeBranch branch, Collection<AttributeType> attributes) {
      Collection<AttributeType> current = validityMap.get(branch);
      validityMap.put(branch, attributes);
      if (Compare.isDifferent(current, attributes)) {
         isDirty = true;
      }
   }

   @Override
   public void set(Map<IOseeBranch, Collection<AttributeType>> attributeTypeMap) throws OseeCoreException {
      Conditions.checkNotNull(attributeTypeMap, "attribute type map input");
      boolean isDifferent = Compare.isDifferent(get(), attributeTypeMap);
      if (isDifferent) {
         validityMap.clear();
         for (Entry<IOseeBranch, Collection<AttributeType>> entry : attributeTypeMap.entrySet()) {
            // Ensure we are using a hash set - don't use putAll
            set(entry.getKey(), entry.getValue());
         }
      }
      isDirty |= isDifferent;
   }

   private void set(IOseeBranch branch, Collection<AttributeType> attributeTypes) throws OseeCoreException {
      Conditions.checkNotNull(branch, "branch");
      Conditions.checkNotNull(attributeTypes, "attribute types list");

      if (attributeTypes.isEmpty()) {
         validityMap.remove(branch);
      } else {
         Collection<AttributeType> cachedItems = validityMap.get(branch);
         if (cachedItems == null) {
            cachedItems = new HashSet<AttributeType>(attributeTypes);
            validityMap.put(branch, cachedItems);
         } else {
            cachedItems.clear();
            cachedItems.addAll(attributeTypes);
         }
      }
   }
}