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
package org.eclipse.osee.framework.skynet.core.attribute;

import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Ryan D. Brooks
 */
public class BooleanAttribute extends CharacterBackedAttribute<Boolean> {
   public static final String[] booleanChoices = new String[] {"yes", "no"};

   public BooleanAttribute(AttributeType attributeType, Artifact artifact) {
      super(attributeType, artifact);
   }

   public Boolean getValue() {
      return getAttributeDataProvider().getValueAsString().equals(booleanChoices[0]);
   }

   public void setValue(Boolean value) {
      getAttributeDataProvider().setValue(value ? booleanChoices[0] : booleanChoices[1]);
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.attribute.Attribute#getDisplayableString()
    */
   @Override
   public String getDisplayableString() {
      String toDisplay = getAttributeDataProvider().getDisplayableString();
      return toDisplay;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.attribute.Attribute#setFromString(java.lang.String)
    */
   @Override
   public void setFromString(String value) throws Exception {
      boolean result = value != null && value.equalsIgnoreCase(BooleanAttribute.booleanChoices[0]);
      setValue(Boolean.valueOf(result));
   }

   @Override
   public String toString() {
      return getAttributeType().getName() + " - " + getValue();
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.skynet.core.attribute.Attribute#initializeDefaultValue()
    */
   @Override
   public void initializeDefaultValue() {
      getAttributeDataProvider().setValue(getAttributeType().getDefaultValue());
   }
}