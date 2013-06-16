/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.core.internal.column.ev;

import org.eclipse.osee.ats.api.ev.IAtsEarnedValueServiceProvider;
import org.eclipse.osee.ats.api.ev.IAtsWorkPackage;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

/**
 * @author Donald G. Dunne
 */
public class WorkPackageTypeUtility extends AbstractRelatedWorkPackageColumn {

   public WorkPackageTypeUtility(IAtsEarnedValueServiceProvider earnedValueServiceProvider) {
      super(earnedValueServiceProvider);
   }

   @Override
   protected String getColumnValue(IAtsWorkPackage workPkg) {
      try {
         return workPkg.getWorkPackageType().name();
      } catch (OseeCoreException ex) {
         return AbstractRelatedWorkPackageColumn.CELL_ERROR_PREFIX + " - " + ex.getLocalizedMessage();
      }
   }

   @Override
   public String getDescription() {
      return "Provides Work Package Type from the selected Work Package related to the selected workflow.";
   }

}