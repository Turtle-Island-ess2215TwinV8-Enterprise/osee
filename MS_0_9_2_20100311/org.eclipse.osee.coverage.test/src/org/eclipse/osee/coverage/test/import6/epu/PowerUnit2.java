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
package org.eclipse.osee.coverage.test.import6.epu;

import java.util.logging.Level;
import org.eclipse.osee.coverage.internal.Activator;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

/**
 * @author Donald G. Dunne
 */
public class PowerUnit2 extends Table {

   public Image image;

   /**
    * @param parent
    * @param style
    */
   public PowerUnit2(Composite parent, int style, Image image) {
      super(parent, style);
   }

   public Image getImage() {
      try {
         if (getStyle() == 4) { // 1, 1, TestUnit2
            return this.image; // 1, 2, n
         } else {
            return this.image; // 1, 3, TestUnit2
         }
      } catch (IllegalArgumentException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex); // 1, 4, n

      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex); // 1, 5, n
      }
      return null; // 1, 6, n
   }

   @Override
   public void clearAll() {
      System.out.println("clear All"); // 2, 1, TestUnit2
   }

   @Override
   public Point computeSize(int wHint, int hHint, boolean changed) {
      if (getStyle() == 4) { // 3, 1, TestUnit2
         return new Point(3, 2); // 3, 2, n
      } else {
         return super.computeSize(wHint, hHint, changed); // 3, 3, TestUnit2
      }
   }

   @Override
   public int getColumnCount() {
      return super.getColumnCount(); // 4, 1, TestUnit2
   }

}
