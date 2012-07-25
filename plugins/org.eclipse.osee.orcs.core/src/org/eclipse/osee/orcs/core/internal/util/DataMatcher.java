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
package org.eclipse.osee.orcs.core.internal.util;

import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Roberto E. Escobar
 */
public abstract class DataMatcher<T> {

   public abstract boolean accept(T data) throws OseeCoreException;

   private abstract static class BooleanOperation<T> extends DataMatcher<T> {
      protected final DataMatcher<T> filter1;
      protected final DataMatcher<T> filter2;

      public BooleanOperation(DataMatcher<T> filter1, DataMatcher<T> filter2) {
         this.filter1 = filter1;
         this.filter2 = filter2;
      }
   }

   public DataMatcher<T> or(DataMatcher<T> anotherFilter) {
      return new BooleanOperation<T>(this, anotherFilter) {
         @Override
         public boolean accept(T data) throws OseeCoreException {
            return filter1.accept(data) || filter2.accept(data);
         }
      };
   }

   public DataMatcher<T> and(DataMatcher<T> anotherFilter) {
      return new BooleanOperation<T>(this, anotherFilter) {
         @Override
         public boolean accept(T data) throws OseeCoreException {
            return filter1.accept(data) && filter2.accept(data);
         }
      };
   }
}