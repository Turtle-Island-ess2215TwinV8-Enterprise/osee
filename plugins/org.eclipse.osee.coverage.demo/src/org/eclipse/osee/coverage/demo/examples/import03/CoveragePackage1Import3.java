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

import org.eclipse.osee.coverage.demo.examples.CpSelectAndImportItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;

/**
 * @author Donald G. Dunne
 */
public class CoveragePackage1Import3 extends CpSelectAndImportItem {

   public CoveragePackage1Import3(XNavigateItem parent) {
      super(
         parent,
         "Open CP 1 - Import 3 - Add PowerUnit1.initAdded to end; Change getColumnCount.line1 from TestUnit2 to TestUnit3",
         CoverageImport3TestBlam.NAME);
   }

}
