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
package org.eclipse.osee.ats.core.workflow;

import java.util.List;
import org.eclipse.osee.ats.core.workflow.WorkPageAdapter;
import org.eclipse.osee.ats.core.workflow.WorkPageType;

public class TwoStates extends WorkPageAdapter {

   public static TwoStates Endorse = new TwoStates("Endorse", WorkPageType.Working);
   public static TwoStates Cancelled = new TwoStates("Cancelled", WorkPageType.Cancelled);
   public static TwoStates Completed = new TwoStates("Completed", WorkPageType.Completed);

   public TwoStates(String pageName, WorkPageType workPageType) {
      super(TwoStates.class, pageName, workPageType);
   }

   public static TwoStates valueOf(String pageName) {
      return WorkPageAdapter.valueOfPage(TwoStates.class, pageName);
   }

   public static List<TwoStates> values() {
      return WorkPageAdapter.pages(TwoStates.class);
   }

}