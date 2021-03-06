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
package org.eclipse.osee.coverage.demo.examples.import01.com.screenB;

import java.util.logging.Level;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Donald G. Dunne
 */
public class ScreenBButton1 extends Button {

   public Image image;

   public ScreenBButton1(Composite parent, int style, Image image) {
      super(parent, style);
   }

   @Override
   public String getText() {
      try {
         if (getStyle() == 4) { // 1, 1, TestUnit2|TestUnit3
            return "Navigate Here"; // 1, 2, TestUnit2|TestUnit3
         } else {
            return "Navigate There"; // 1, 3, TestUnit2|TestUnit3
         }
      } catch (Exception ex) {
         OseeLog.log(ScreenBButton1.class, Level.SEVERE, ex); // 1, 4, n
      }
      return "Navigate";
   }

   @Override
   public void setImage(Image image) {
      this.image = image; // 2, 1, TestUnit2|TestUnit3
   }

   @Override
   public void setText(String string) {
      super.setText(string); // 3, 1, n
   }

   @Override
   public Image getImage() {
      try {
         if (getStyle() == 4) { // 4, 1, TestUnit2|TestUnit3
            return this.image; // 4, 2, n
         } else {
            return super.getImage(); // 4, 3, TestUnit2|TestUnit3
         }
      } catch (IllegalArgumentException ex) {
         OseeLog.log(ScreenBButton1.class, Level.SEVERE, ex); // 4, 4, n

      } catch (Exception ex) {
         OseeLog.log(ScreenBButton1.class, Level.SEVERE, ex); // 4, 5, n
      }
      return null; // 4, 6, n
   }

}
