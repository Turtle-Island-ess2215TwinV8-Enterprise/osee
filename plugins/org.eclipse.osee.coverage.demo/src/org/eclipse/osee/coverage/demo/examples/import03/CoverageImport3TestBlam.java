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
package org.eclipse.osee.coverage.demo.examples.import03;

import java.util.Arrays;
import org.eclipse.osee.coverage.demo.examples.CoverageImportTestBlam;

/**
 * Add PowerUnit1.initAdded to end; Change getColumnCount.line1 from TestUnit2 to TestUnit3
 * 
 * @author Donald G. Dunne
 */
public class CoverageImport3TestBlam extends CoverageImportTestBlam {

   public static String NAME = "Test Import 3";

   public CoverageImport3TestBlam() {
      super(NAME, Arrays.asList(
      //
         "import03/epu/PowerUnit1.java", "import01/epu/PowerUnit2.java", "import02/epu/PowerUnit3.java"
      //
      ));
   }

}