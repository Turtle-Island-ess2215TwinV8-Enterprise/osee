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
package org.eclipse.osee.framework.core.model.tabledataframework.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.framework.core.model.tabledataframework.KeyColumn;

/**
 * @author Shawn F. Cook
 */
public class KeyColumn_AtoG implements KeyColumn {
   List<String> listAtoG = new ArrayList<String>(Arrays.asList("A", "B", "C", "D", "E", "F", "G"));
   private Integer currentIndex;
   private Integer nextIndex;

   public KeyColumn_AtoG() {
      reset();
   }

   @Override
   public boolean hasNext() {
      return nextIndex < listAtoG.size();
   }

   @Override
   public Object next() {
      currentIndex = nextIndex;
      nextIndex++;
      return getCurrent();
   }

   @Override
   public void remove() {
      //Do nothing
   }

   @Override
   public Object getCurrent() {
      String currentValue = listAtoG.get(currentIndex);
      return currentValue;
   }

   @Override
   public List<Object> getAll() {
      List<Object> retList = new ArrayList<Object>();
      retList.addAll(listAtoG);
      return retList;
   }

   @Override
   public void reset() {
      currentIndex = null;
      nextIndex = 0;
   }

}
