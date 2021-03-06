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
package org.eclipse.osee.framework.skynet.core.utility;

import java.util.Date;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;

/**
 * @author Donald G. Dunne
 */
public class ElapsedTime {

   Date startDate;
   Date endDate;
   private String name;

   public ElapsedTime(String name) {
      start(name);
   }

   public void start(String name) {
      this.name = name;
      startDate = new Date();
      System.err.println("\n" + name + " - start " + DateUtil.getTimeStamp());
   }

   public void logPoint(String pointName) {
      System.err.println("\n" + name + " - [" + pointName + "] " + DateUtil.getTimeStamp());
   }

   public static enum Units {
      SEC,
      MSEC
   }

   public String end() {
      return end(Units.SEC);
   }

   public String end(Units units) {
      endDate = new Date();
      long diff = endDate.getTime() - startDate.getTime();
      String str =
         String.format("%s - elapsed %d %s - start %s - end %s", name, (units == Units.SEC ? (diff / 1000) : diff),
            units.name(), DateUtil.getDateStr(startDate, DateUtil.HHMMSSSS),
            DateUtil.getDateStr(endDate, DateUtil.HHMMSSSS));
      System.err.println(str);
      return str;
   }
}
