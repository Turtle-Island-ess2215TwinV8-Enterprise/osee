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
package org.eclipse.osee.orcs.core.internal.attribute.primitives;

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.core.annotations.OseeAttribute;
import org.eclipse.osee.orcs.core.internal.attribute.CharacterBackedAttribute;

/**
 * @author Ryan D. Brooks
 */
@OseeAttribute("BooleanAttribute")
public class BooleanAttribute extends CharacterBackedAttribute<Boolean> {
   public static final String[] booleanChoices = new String[] {"yes", "no"};

   @Override
   public Boolean getValue() throws OseeCoreException {
      return getDataProxy().getValueAsString().equals(booleanChoices[0]);
   }

   @Override
   public boolean subClassSetValue(Boolean value) throws OseeCoreException {
      return getDataProxy().setValue(value ? booleanChoices[0] : booleanChoices[1]);
   }

   @Override
   protected Boolean convertStringToValue(String value) {
      return value != null && value.equalsIgnoreCase(booleanChoices[0]);
   }
}