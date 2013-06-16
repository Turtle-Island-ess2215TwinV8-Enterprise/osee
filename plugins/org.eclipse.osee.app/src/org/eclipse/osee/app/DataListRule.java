/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.app;

import java.io.IOException;

/**
 * @author Ryan D. Brooks
 */
public final class DataListRule extends AppendableRule {
   private final String[][] options;
   private final String listId;

   public DataListRule(String ruleName, String[][] options, String listId) {
      super(ruleName);
      this.options = options;
      this.listId = listId;
   }

   public DataListRule(String[][] options, String listId) {
      this(listId, options, listId);
   }

   @Override
   public void applyTo(Appendable appendable) throws IOException {
      appendable.append("\n<datalist id=\"");
      appendable.append(listId);
      appendable.append("\">\n");
      for (String[] option : options) {
         appendable.append("<option value=\"");
         appendable.append(option[0]);
         appendable.append("\" >");
         appendable.append(option[1]);
         appendable.append("</option>\n");
      }
      appendable.append("</datalist>\n");
   }
}
