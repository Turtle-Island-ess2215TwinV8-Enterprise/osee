/*
 * Created on Mar 21, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.model;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.osee.framework.core.enums.WidgetOption;
import org.eclipse.osee.framework.core.util.WidgetOptionHandler;
import org.junit.Assert;
import org.junit.Test;

public class WidgetOptionHandlerTest {

   @Test
   public void testWidgetOptionHandler() {
      WidgetOptionHandler handler = new WidgetOptionHandler();

      handler.add(WidgetOption.ADD_DEFAULT_VALUE);
      Assert.assertTrue(handler.contains(WidgetOption.ADD_DEFAULT_VALUE));
      Assert.assertFalse(handler.contains(WidgetOption.FILL_HORIZONTALLY));
   }

   @Test
   public void testAddWidgetOptionArray() {
      WidgetOptionHandler handler = new WidgetOptionHandler();
      handler.add(Arrays.asList(WidgetOption.ADD_DEFAULT_VALUE, WidgetOption.FILL_HORIZONTALLY));

      Assert.assertTrue(handler.contains(WidgetOption.ADD_DEFAULT_VALUE));
      Assert.assertTrue(handler.contains(WidgetOption.FILL_HORIZONTALLY));

      handler = new WidgetOptionHandler(WidgetOption.ADD_DEFAULT_VALUE, WidgetOption.FILL_HORIZONTALLY);

      Assert.assertTrue(handler.contains(WidgetOption.ADD_DEFAULT_VALUE));
      Assert.assertTrue(handler.contains(WidgetOption.FILL_HORIZONTALLY));
   }

   @Test
   public void testGetCollection() {
      Collection<WidgetOption> collection =
         WidgetOptionHandler.getCollection(WidgetOption.ADD_DEFAULT_VALUE, WidgetOption.FILL_HORIZONTALLY);
      Assert.assertTrue(collection.contains(WidgetOption.ADD_DEFAULT_VALUE));
      Assert.assertTrue(collection.contains(WidgetOption.FILL_HORIZONTALLY));
      Assert.assertFalse(collection.contains(WidgetOption.NOT_REQUIRED_FOR_COMPLETION));
   }

   @Test
   public void testAdd_ENABLED() {
      WidgetOptionHandler handler = new WidgetOptionHandler();
      handler.add(WidgetOption.ENABLED);
      Assert.assertTrue(handler.contains(WidgetOption.ENABLED));
      handler.add(WidgetOption.NOT_ENABLED);
      Assert.assertFalse(handler.contains(WidgetOption.ENABLED));
      Assert.assertTrue(handler.contains(WidgetOption.NOT_ENABLED));
   }

   @Test
   public void testAdd_FUTURE_DATE_REQUIRED() {
      WidgetOptionHandler handler = new WidgetOptionHandler();
      handler.add(WidgetOption.FUTURE_DATE_REQUIRED);
      Assert.assertTrue(handler.contains(WidgetOption.FUTURE_DATE_REQUIRED));
      handler.add(WidgetOption.NOT_FUTURE_DATE_REQUIRED);
      Assert.assertFalse(handler.contains(WidgetOption.FUTURE_DATE_REQUIRED));
      Assert.assertTrue(handler.contains(WidgetOption.NOT_FUTURE_DATE_REQUIRED));
   }

   @Test
   public void testAdd_VERTICAL_LABEL() {
      WidgetOptionHandler handler = new WidgetOptionHandler();
      handler.add(WidgetOption.VERTICAL_LABEL);
      Assert.assertTrue(handler.contains(WidgetOption.VERTICAL_LABEL));
      handler.add(WidgetOption.HORIZONTAL_LABEL);
      Assert.assertFalse(handler.contains(WidgetOption.VERTICAL_LABEL));
      Assert.assertTrue(handler.contains(WidgetOption.HORIZONTAL_LABEL));
   }

   @Test
   public void testAdd_REQUIRED_FOR_COMPLETION() {
      WidgetOptionHandler handler = new WidgetOptionHandler();
      handler.add(WidgetOption.REQUIRED_FOR_COMPLETION);
      Assert.assertTrue(handler.contains(WidgetOption.REQUIRED_FOR_COMPLETION));
      handler.add(WidgetOption.NOT_REQUIRED_FOR_COMPLETION);
      Assert.assertFalse(handler.contains(WidgetOption.REQUIRED_FOR_COMPLETION));
      Assert.assertTrue(handler.contains(WidgetOption.NOT_REQUIRED_FOR_COMPLETION));
   }

   @Test
   public void testAdd_EDITABLE() {
      WidgetOptionHandler handler = new WidgetOptionHandler();
      handler.add(WidgetOption.EDITABLE);
      Assert.assertTrue(handler.contains(WidgetOption.EDITABLE));
      handler.add(WidgetOption.NOT_EDITABLE);
      Assert.assertFalse(handler.contains(WidgetOption.EDITABLE));
      Assert.assertTrue(handler.contains(WidgetOption.NOT_EDITABLE));
   }

   @Test
   public void testAdd_FILL() {
      WidgetOptionHandler handler = new WidgetOptionHandler();
      handler.add(WidgetOption.FILL_HORIZONTALLY);
      handler.add(WidgetOption.FILL_VERTICALLY);
      Assert.assertTrue(handler.contains(WidgetOption.FILL_HORIZONTALLY));
      Assert.assertTrue(handler.contains(WidgetOption.FILL_VERTICALLY));
      handler.add(WidgetOption.FILL_NONE);
      Assert.assertTrue(handler.contains(WidgetOption.FILL_NONE));
      Assert.assertFalse(handler.contains(WidgetOption.FILL_VERTICALLY));
      Assert.assertFalse(handler.contains(WidgetOption.FILL_HORIZONTALLY));
   }

   @Test
   public void testSetWidgetOptionArray() {
      WidgetOptionHandler handler = new WidgetOptionHandler();
      handler.add(Arrays.asList(WidgetOption.ADD_DEFAULT_VALUE, WidgetOption.FILL_HORIZONTALLY));
      Assert.assertTrue(handler.getWidgetOptions().contains(WidgetOption.ADD_DEFAULT_VALUE));
   }

   @Test
   public void testToString() {
      WidgetOptionHandler handler = new WidgetOptionHandler();
      handler.add(Arrays.asList(WidgetOption.ADD_DEFAULT_VALUE, WidgetOption.FILL_HORIZONTALLY));
      Assert.assertTrue(handler.toString().equals("[ADD_DEFAULT_VALUE, FILL_HORIZONTALLY]") || handler.toString().equals(
         "[FILL_HORIZONTALLY, ADD_DEFAULT_VALUE]"));
   }

}
