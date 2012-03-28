/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.coverage.util;

import java.util.Collection;
import org.eclipse.osee.coverage.model.CoverageImport;
import org.eclipse.osee.coverage.model.CoverageOptionManager;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public class NotSaveable implements ISaveable {

   @Override
   public Result isEditable() {
      return new Result("Not Editable");
   }

   @Override
   public Result save(String saveName, CoverageOptionManager coverageOptionManager) {
      return new Result("Not Saveable");
   }

   @Override
   public Result save(Collection<ICoverage> coverages, String saveName) {
      return new Result("Not Saveable");
   }

   @Override
   public Result saveImportRecord(SkynetTransaction transaction, CoverageImport coverageImport) {
      return new Result("Not Saveable");
   }

   @Override
   public Branch getBranch() {
      return null;
   }

}
