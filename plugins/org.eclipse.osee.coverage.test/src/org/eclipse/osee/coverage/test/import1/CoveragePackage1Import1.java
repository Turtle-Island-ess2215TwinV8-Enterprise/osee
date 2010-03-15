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
package org.eclipse.osee.coverage.test.import1;

import org.eclipse.osee.coverage.test.util.CpCreateAndImportItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;

/**
 * @author Donald G. Dunne
 */
public class CoveragePackage1Import1 extends CpCreateAndImportItem {

   public CoveragePackage1Import1(XNavigateItem parent) {
      super(parent, "Open CP 1 - Import 1 - New CP", CoverageImport1TestBlam.NAME);
   }
}
