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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class UrlUtility {
   public static void main(String[] args) throws Exception {
      Document doc = Jsoup.connect("http://www.boeing.com").get();
      System.out.println(doc.html());
   }
}