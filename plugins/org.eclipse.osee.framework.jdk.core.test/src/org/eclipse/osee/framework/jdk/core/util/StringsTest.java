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
package org.eclipse.osee.framework.jdk.core.util;

import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class StringsTest {

   @org.junit.Test
   public void testTruncate() {
      String name = "Now is the time forall good men";
      Assert.assertEquals(31, name.length());

      Assert.assertEquals(20, Strings.truncate(name, 20).length());
      String withDots = Strings.truncate(name, 20, true);

      Assert.assertEquals(20, Strings.truncate(withDots, 20).length());
      Assert.assertEquals(withDots, "Now is the time f...");
   }

   @Test
   public void testUnQuote() {
      String actual = Strings.unquote(null);
      Assert.assertNull(actual);

      actual = Strings.unquote("");
      Assert.assertEquals("", actual);

      actual = Strings.unquote("hello");
      Assert.assertEquals("hello", actual);

      actual = Strings.unquote("\"hello\"");
      Assert.assertEquals("hello", actual);
   }

   @Test
   public void testQuote() {
      String actual = Strings.quote(null);
      Assert.assertNull(actual);

      actual = Strings.quote("");
      Assert.assertEquals("", actual);

      actual = Strings.quote("hello");
      Assert.assertEquals("\"hello\"", actual);
   }
}