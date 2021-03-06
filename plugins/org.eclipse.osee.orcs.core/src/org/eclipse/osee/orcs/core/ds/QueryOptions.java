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
package org.eclipse.osee.orcs.core.ds;

/**
 * @author Roberto E. Escobar
 */
public class QueryOptions extends Options {

   private boolean includeCache = true;
   private boolean includeTypeInheritance = false;

   public QueryOptions() {
      super();
   }

   @Override
   public void reset() {
      super.reset();
      includeCache = true;
      includeTypeInheritance = false;
   }

   public boolean isCacheIncluded() {
      return includeCache;
   }

   public boolean isTypeInheritanceIncluded() {
      return includeTypeInheritance;
   }

   public void setIncludeCache(boolean enabled) {
      includeCache = enabled;
   }

   public void setIncludeTypeInheritance(boolean enabled) {
      includeTypeInheritance = enabled;
   }

   @Override
   public QueryOptions clone() {
      QueryOptions clone = new QueryOptions();
      clone.setIncludeDeleted(this.areDeletedIncluded());
      clone.setFromTransaction(this.getFromTransaction());
      clone.includeCache = this.includeCache;
      clone.includeTypeInheritance = this.includeTypeInheritance;
      return clone;
   }

   @Override
   public String toString() {
      return "QueryOptions [includeCache=" + includeCache + ", includeTypeInheritance=" + includeTypeInheritance + " [" + super.toString() + "]]";
   }
}
