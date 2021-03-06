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
package org.eclipse.osee.coverage.demo.examples.import01;

import java.util.Arrays;
import org.eclipse.osee.coverage.demo.examples.CoverageImportTestBlam;

/**
 * @author Donald G. Dunne
 */
public class CoverageImport1TestBlam extends CoverageImportTestBlam {

   public static String NAME = "Test Import 1";

   public CoverageImport1TestBlam() {
      super(NAME, Arrays.asList(
      //
         "com/screenA/ComScrnAButton1.java", "com/screenA/ComScrnAButton2.java",
         //
         "com/screenB/ScreenBButton1.java", "com/screenB/ScreenBButton2.java", "com/screenB/ScreenBButton3.java",

         //
         "epu/PowerUnit1.java", "epu/PowerUnit2.java",
         //
         "apu/AuxPowerUnit1.java", "apu/AuxPowerUnit2.java",

         //
         "nav/NavigationButton1.java", "nav/NavigationButton2.java", "nav/NavigationButton3.java"
      //
      ));
   }

}