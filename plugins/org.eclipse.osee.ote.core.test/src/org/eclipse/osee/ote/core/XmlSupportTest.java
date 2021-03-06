/*
 * Created on Jun 10, 2013
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ote.core;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Michael P. Masterson
 */
public class XmlSupportTest {

   /**
    * Test method for {@link org.eclipse.osee.ote.core.XmlSupport#convertNonPrintableCharacers(java.lang.String)}.
    */
   @Test
   public void testConvertNonPrintableCharacers() {
      String testString = "abc\n\r\0\t 123\f&<>";
      String expected = "abc\n\r ASCII=0 \t 123 ASCII=12  ampersand  less-than  greater-than ";

      String actual = XmlSupport.convertNonPrintableCharacers(testString);
      Assert.assertEquals(expected, actual);
   }

}
