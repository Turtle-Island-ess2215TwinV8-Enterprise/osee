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
package org.eclipse.osee.ats.impl.internal.workitem.note;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.api.workflow.notes.IAtsNoteItem;
import org.eclipse.osee.ats.api.workflow.notes.NoteType;
import org.eclipse.osee.ats.impl.internal.user.AtsUserServiceImpl;
import org.eclipse.osee.ats.impl.internal.workitem.notes.NoteItem;
import org.eclipse.osee.ats.mocks.MockAtsUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Donald G. Dunne
 */
public class NoteItemTest {

   private static final MockAtsUser joe = new MockAtsUser("joe");

   @Test
   public void testNoteItemNoteTypeStringStringUserString() {
      Date date = new Date();
      IAtsNoteItem item = new NoteItem(NoteType.Comment, "Implement", String.valueOf(date.getTime()), joe, "my msg");
      validate(item, date);
   }

   public static void validate(IAtsNoteItem item, Date date) {
      Assert.assertEquals(NoteType.Comment, item.getType());
      Assert.assertEquals("Implement", item.getState());
      Assert.assertEquals(joe, item.getUser());
      Assert.assertEquals("my msg", item.getMsg());
   }

   public static NoteItem getTestNoteItem(Date date) {
      return new NoteItem(NoteType.Comment, "Implement", String.valueOf(date.getTime()), joe, "my msg");
   }

   @Test
   public void testNoteItemStringStringStringUserString() throws OseeCoreException {
      Date date = new Date();
      IAtsNoteItem item =
         new NoteItem(NoteType.Comment.name(), "Implement", String.valueOf(date.getTime()), joe, "my msg");
      validate(item, date);
   }

   @Test
   public void testToString() {
      Date date = new Date();
      IAtsNoteItem item = getTestNoteItem(date);

      Assert.assertEquals(
         "Note: Comment from " + joe.getName() + " for \"Implement\" on " + DateUtil.getMMDDYYHHMM(date) + " - my msg",
         item.toString());
   }

   @Test
   public void testToXmlFromXml() throws OseeCoreException {
      Date date = new Date();
      NoteItem item = getTestNoteItem(date);
      NoteItem item2 =
         new NoteItem(NoteType.Question.name(), "Analyze", String.valueOf(date.getTime()), joe, "another message");
      AtsUserServiceImpl.instance.addUser(joe);
      String xml = NoteItem.toXml(Arrays.asList(item, item2));
      Assert.assertEquals(
         "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><AtsNote>" + //
         "<Item date=\"" + date.getTime() + "\" msg=\"my msg\" state=\"Implement\" type=\"Comment\" userId=\"" + joe.getUserId() + "\"/>" + //
         "<Item date=\"" + date.getTime() + "\" msg=\"another message\" state=\"Analyze\" type=\"Question\" userId=\"" + joe.getUserId() + "\"/></AtsNote>",
         xml);

      List<IAtsNoteItem> items = NoteItem.fromXml(xml, "ASDF4");
      validate(items.iterator().next(), date);

      IAtsNoteItem fromXmlItem2 = items.get(1);
      Assert.assertEquals(NoteType.Question, fromXmlItem2.getType());
      Assert.assertEquals("Analyze", fromXmlItem2.getState());
      Assert.assertEquals(joe, fromXmlItem2.getUser());
      Assert.assertEquals("another message", fromXmlItem2.getMsg());

   }

   @Test
   public void testToHTML() {
      Date date = new Date();
      IAtsNoteItem item = getTestNoteItem(date);

      Assert.assertEquals(
         "<b>Note:</b>Comment from " + joe.getName() + " for \"Implement\" on " + DateUtil.getMMDDYYHHMM(date) + " - my msg",
         item.toHTML());
   }

}
