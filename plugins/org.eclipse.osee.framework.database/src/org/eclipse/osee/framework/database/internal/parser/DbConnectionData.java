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
package org.eclipse.osee.framework.database.internal.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Roberto E. Escobar
 */
public class DbConnectionData implements Serializable {

   private static final long serialVersionUID = -7153773982095739831L;

   public enum DescriptionFields {
      id;
   }

   public enum UrlAttributes {
      Entry;
   }

   public enum ConnectionFields {
      Url,
      Driver,
      UrlAttributes,
      Property;
   }

   private String id;
   private String databaseDriver;
   private String databaseUrl;
   private final ArrayList<String> attributesList;
   private final Properties properties;

   public DbConnectionData() {
      attributesList = new ArrayList<String>();
      properties = new Properties();
      this.databaseDriver = "";
      this.databaseUrl = "";
      this.id = "";
   }

   public String getDBDriver() {
      return databaseDriver;
   }

   public String getRawUrl() {
      return databaseUrl;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public void addAttribute(String attribute) {
      attributesList.add(attribute);
   }

   public void setDBDriver(String driver) {
      databaseDriver = driver;
   }

   public void setRawUrl(String url) {
      databaseUrl = url;
   }

   public String getAttributes() {
      StringBuilder toReturn = new StringBuilder();
      for (String temp : attributesList) {
         toReturn.append(String.format("%s;", temp));
      }
      return toReturn.toString();
   }

   @Override
   public String toString() {
      return "DbConnection: " + id + "\n" + " Driver: " + databaseDriver + "\n" + " RawUrl: " + databaseUrl + "\n" + "Attributes: " + getAttributes() + "\n";
   }

   public Properties getProperties() {
      return properties;
   }
}
