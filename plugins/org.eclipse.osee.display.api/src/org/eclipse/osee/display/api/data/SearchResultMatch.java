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
package org.eclipse.osee.display.api.data;

import java.util.List;

/**
 * @author Shawn F. Cook
 */
public class SearchResultMatch {

   private final String attributeType;
   private final int numberOfMatches;
   private final List<StyledText> data;

   public SearchResultMatch(String attributeType, int numberOfMatches, List<StyledText> data) {
      this.attributeType = attributeType;
      this.numberOfMatches = numberOfMatches;
      this.data = data;
   }

   public String getAttributeType() {
      return attributeType;
   }

   public int getManyMatches() {
      return numberOfMatches;
   }

   public List<StyledText> getData() {
      return data;
   }

}