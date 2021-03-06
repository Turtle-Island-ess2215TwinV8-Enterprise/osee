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
package org.eclipse.osee.ats.client.demo;

import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.data.TokenFactory;

/**
 * @author Donald G. Dunne
 */
public final class DemoSawBuilds {

   public static final IOseeBranch SAW_Bld_1 = TokenFactory.createBranch("AyH_f2sSKy3l07fIvAAA", "SAW_Bld_1");
   public static final IOseeBranch SAW_Bld_2 = TokenFactory.createBranch("AyH_f2sSKy3l07fIvBBB", "SAW_Bld_2");
   public static final IOseeBranch SAW_Bld_3 = TokenFactory.createBranch("AyH_f2sSKy3l07fIvCCC", "SAW_Bld_3");

   private DemoSawBuilds() {
      // Constants
   }
}