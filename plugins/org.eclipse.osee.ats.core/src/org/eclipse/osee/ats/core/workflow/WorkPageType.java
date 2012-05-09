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
package org.eclipse.osee.ats.core.workflow;

/**
 * @author Donald G. Dunne
 */
public enum WorkPageType {
   Working,
   Completed,
   Cancelled;

   public boolean isCompletedPage() {
      return this == Completed;
   }

   public boolean isCompletedOrCancelledPage() {
      return isCompletedPage() || isCancelledPage();
   }

   public boolean isCancelledPage() {
      return this == Cancelled;
   }

   public boolean isWorkingPage() {
      return this == Working;
   }
}