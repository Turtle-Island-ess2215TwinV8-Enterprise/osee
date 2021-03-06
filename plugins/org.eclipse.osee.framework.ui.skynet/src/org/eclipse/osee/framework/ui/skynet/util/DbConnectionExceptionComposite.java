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
package org.eclipse.osee.framework.ui.skynet.util;

import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.ui.plugin.OseeUiActivator;
import org.eclipse.osee.framework.ui.swt.ExceptionComposite;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Donald G. Dunne
 */
public class DbConnectionExceptionComposite extends ExceptionComposite {

   public DbConnectionExceptionComposite(Composite parent, Exception ex) {
      super(parent, ex);
   }

   /**
    * Tests the DB Connection and returns true if ok. If exceptions and parent != null, the
    * DbConnectionExceptionComposite will be displayed in parent giving exception information.
    */
   public static boolean dbConnectionIsOk(Composite parent) {
      //      try {
      //         ConnectionHandler.getConnection();
      //      } catch (Exception ex) {
      //         if (parent != null) new DbConnectionExceptionComposite(parent, ex);
      //      }
      Result result = OseeUiActivator.areOSEEServicesAvailable();
      if (result.isFalse()) {
         new DbConnectionExceptionComposite(parent, new Exception(
            "OSEE Service(s) Unavailable:\n\t" + result.getText().replaceAll("\n", "\n\t")));
      }
      return result.isTrue();
   }
}
