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
package org.eclipse.osee.coverage.demo.examples;

import org.eclipse.osee.coverage.CoverageManager;
import org.eclipse.osee.coverage.ICoverageImporter;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;

/**
 * @author Donald G. Dunne
 */
public class CoverageImportTestNavigateItem extends XNavigateItemAction {

   private final ICoverageImporter coverageImporter;

   public CoverageImportTestNavigateItem(XNavigateItem parent, ICoverageImporter coverageImporter) {
      super(parent, coverageImporter.getName());
      this.coverageImporter = coverageImporter;
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) throws Exception {
      CoverageManager.importCoverage(coverageImporter);
   }

}
