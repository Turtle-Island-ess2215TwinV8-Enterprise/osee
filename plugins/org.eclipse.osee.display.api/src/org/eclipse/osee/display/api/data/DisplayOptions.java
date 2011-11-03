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

/**
 * @author John Misinco
 */
public class DisplayOptions {

   private boolean verboseResults;
   private int resultsPerPage;

   public DisplayOptions(boolean verboseResults, int resultsPerPage) {
      this.verboseResults = verboseResults;
      this.resultsPerPage = resultsPerPage;
   }

   public boolean isVerboseResults() {
      return verboseResults;
   }

   public int getResultsPerPage() {
      return resultsPerPage;
   }

   public void setVerboseResults(boolean verboseResults) {
      this.verboseResults = verboseResults;
   }

   public void setResultsPerPage(int resultsPerPage) {
      this.resultsPerPage = resultsPerPage;
   }
}
