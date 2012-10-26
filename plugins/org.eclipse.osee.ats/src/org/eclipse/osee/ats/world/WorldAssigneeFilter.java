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
package org.eclipse.osee.ats.world;

import java.util.regex.Pattern;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.core.client.util.AtsUsersClient;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.workflow.AtsAssigneeService;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;

/**
 * @author Donald G. Dunne
 */
public class WorldAssigneeFilter extends ViewerFilter {

   Pattern p;

   public WorldAssigneeFilter() throws OseeCoreException {
      p = Pattern.compile(AtsUsersClient.getUser().getName());
   }

   @Override
   public boolean select(Viewer viewer, Object parentElement, Object element) {
      try {
         if (element instanceof IAtsWorkItem) {
            return p.matcher(AtsAssigneeService.get().getAssigneeStr((IAtsWorkItem) element)).find();
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return true;
   }

}
