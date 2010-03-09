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
package org.eclipse.osee.ote.service;

import java.io.Serializable;

import org.eclipse.osee.connection.service.IServiceConnector;

/**
 * @author Ken J. Aguilar
 */
public class OteServiceProperties {
   private final IServiceConnector connector;

   private static final String NA = "N.A.";
   private boolean debug = false;

   public OteServiceProperties(IServiceConnector connector) {
      this.connector = connector;
   }

   /**
    * @return the name
    */
   public String getName() {
      return (String) connector.getProperty("name", NA);
   }

   /**
    * @return the station
    */
   public String getStation() {
      return (String) connector.getProperty("station", NA);
   }

   /**
    * @return the type
    */
   public String getType() {
      return (String) connector.getProperty("type", NA);
   }

   /**
    * @return the mode
    */
   public String getMode() {
      return (String) connector.getProperty("mode", NA);
   }

   /**
    * @return the version
    */
   public String getVersion() {
      return (String) connector.getProperty("version", NA);
   }

   /**
    * @return the group
    */
   public String getGroup() {
      return (String) connector.getProperty("groups", NA);
   }

   /**
    * @return the comment
    */
   public String getComment() {
      return (String) connector.getProperty("comment", NA);
   }

   /**
    * @return the dateStart
    */
   public String getDateStarted() {
      return connector.getProperty("date", NA).toString();
   }

   public String getUserList() {
      return connector.getProperty("user_list", NA).toString();
   }

   public void printStats() {
      if (debug) {
         System.out.printf("test service found:\n\tname: %s\n\tstation: %s\n\ttype: %s\n\tcomment: %s\n\t%s\n",
               getName(), getStation(), getType(), getComment(), getGroup());
      }
   }
   
   public Serializable getProperty(String name) {
	   return connector.getProperty(name, null);
   }

   public String getOwner() {
      return (String) connector.getProperty("owner", NA);
   }
}
