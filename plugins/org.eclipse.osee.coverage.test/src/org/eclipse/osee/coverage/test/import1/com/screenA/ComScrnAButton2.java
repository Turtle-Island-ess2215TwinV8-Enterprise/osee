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
package org.eclipse.osee.coverage.test.import1.com.screenA;

import java.util.logging.Level;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Donald G. Dunne
 */
public class ComScrnAButton2 extends Button {

   public Image image;

   /**
    * @param parent
    * @param style
    */
   public ComScrnAButton2(Composite parent, int style, Image image) {
      super(parent, style);
   }

   @Override
   public String getText() {
      try {
         if (getStyle() == 4) { // 1, 1, TestUnit2
            return "Navigate Here"; // 1, 2, TestUnit4
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex); // 1, 3, n
      }
      return "Navigate"; // 1, 4, n
   }

   @Override
   public Image getImage() {
      try {
         if (getStyle() == 4) { // 2, 1, TestUnit3
            return this.image; // 2, 2, n
         } else {
            return super.getImage(); // 2, 3, TestUnit4|TestUnit1
         }
      } catch (IllegalArgumentException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex); // 2, 4, n
      }
      return null; // 2, 6, n
   }

}
