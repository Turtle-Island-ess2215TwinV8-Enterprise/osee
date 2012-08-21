/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.impl.internal.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.api.workdef.IAtsLayoutItem;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsStepPageDefinition;

/**
 * @author Donald G. Dunne
 */
public class StepPageDefinition extends AbstractWorkDefItem implements IAtsStepPageDefinition {

   private final List<IAtsLayoutItem> layoutItems = new ArrayList<IAtsLayoutItem>(5);
   private String description = null;

   public StepPageDefinition(String name) {
      super(name);
   }

   @Override
   public List<IAtsLayoutItem> getLayoutItems() {
      return layoutItems;
   }

   @Override
   public String toString() {
      return String.format("[%s]", getName());
   }

   /**
    * Returns fully qualified name of <work definition name>.<this state name
    */

   @Override
   public String getFullName() {
      return getName();
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((getFullName() == null) ? 0 : getFullName().hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      IAtsStateDefinition other = (IAtsStateDefinition) obj;
      if (getFullName() == null) {
         if (other.getFullName() != null) {
            return false;
         } else {
            return false;
         }
      } else if (!getFullName().equals(other.getFullName())) {
         return false;
      }
      return true;
   }

   @Override
   public String getDescription() {
      return description;
   }

   @Override
   public void setDescription(String description) {
      this.description = description;
   }

}
