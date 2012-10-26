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

package org.eclipse.osee.ats.impl.internal.workitem.notes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workflow.notes.IAtsNoteItem;
import org.eclipse.osee.ats.api.workflow.notes.NoteType;
import org.eclipse.osee.ats.impl.internal.user.AtsUserServiceImpl;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.xml.Jaxp;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Donald G. Dunne
 */
public class NoteItem implements IAtsNoteItem {

   private Date date;
   private final String state;
   private String msg;
   private IAtsUser user;
   private NoteType type = NoteType.Other;
   protected final static String LOG_ITEM_TAG = "Item";
   private final static String ATS_NOTE_TAG = "AtsNote";

   public NoteItem(NoteType type, String state, String date, IAtsUser user, String msg) {
      Long l = Long.valueOf(date);
      this.date = new Date(l.longValue());
      this.state = Strings.intern(state);
      this.msg = msg;
      this.user = user;
      this.type = type;
   }

   public NoteItem(String type, String state, String date, IAtsUser user, String msg) throws OseeCoreException {
      this(NoteType.getType(type), state, date, user, msg);
   }

   @Override
   public Date getDate() {
      return date;
   }

   public void setDate(Date date) {
      this.date = date;
   }

   @Override
   public String getMsg() {
      return msg;
   }

   public void setMsg(String msg) {
      this.msg = msg;
   }

   @Override
   public String toString() {
      return String.format("Note: %s from %s%s on %s - %s", type, user.getName(), toStringState(),
         DateUtil.getMMDDYYHHMM(date), msg);
   }

   private String toStringState() {
      return (state.isEmpty() ? "" : " for \"" + state + "\"");
   }

   @Override
   public IAtsUser getUser() {
      return user;
   }

   @Override
   public NoteType getType() {
      return type;
   }

   public void setType(NoteType type) {
      this.type = type;
   }

   @Override
   public String toHTML() {
      return toString().replaceFirst("^Note: ", "<b>Note:</b>");
   }

   @Override
   public String getState() {
      return state;
   }

   public void setUser(IAtsUser user) {
      this.user = user;
   }

   public static List<IAtsNoteItem> fromXml(String xml, String hrid) throws OseeCoreException {
      List<IAtsNoteItem> logItems = new ArrayList<IAtsNoteItem>();
      if (Strings.isValid(xml)) {
         try {
            NodeList nodes = Jaxp.readXmlDocument(xml).getElementsByTagName(LOG_ITEM_TAG);
            for (int i = 0; i < nodes.getLength(); i++) {
               Element element = (Element) nodes.item(i);
               IAtsUser user = AtsUserServiceImpl.instance.getUser(element.getAttribute("userId"));
               NoteItem item = new NoteItem(element.getAttribute("type"), element.getAttribute("state"), // NOPMD by b0727536 on 9/29/10 8:52 AM
                  element.getAttribute("date"), user, element.getAttribute("msg"));
               logItems.add(item);
            }
         } catch (IOException ex) {
            throw new OseeWrappedException(ex, "Error parsing notes for [%s]", hrid);
         } catch (SAXException ex) {
            throw new OseeWrappedException(ex, "Error parsing notes for [%s]", hrid);
         } catch (ParserConfigurationException ex) {
            throw new OseeWrappedException(ex, "Error parsing notes for [%s]", hrid);
         }

      }
      return logItems;
   }

   public static String toXml(List<? extends IAtsNoteItem> items) throws OseeCoreException {
      try {
         Document doc = Jaxp.newDocumentNamespaceAware();
         Element rootElement = doc.createElement(ATS_NOTE_TAG);
         doc.appendChild(rootElement);
         for (IAtsNoteItem item : items) {
            Element element = doc.createElement(NoteItem.LOG_ITEM_TAG);
            element.setAttribute("type", item.getType().name());
            element.setAttribute("state", item.getState());
            element.setAttribute("date", String.valueOf(item.getDate().getTime()));
            element.setAttribute("userId", item.getUser().getUserId());
            element.setAttribute("msg", item.getMsg());
            rootElement.appendChild(element);
         }
         return Jaxp.getDocumentXml(doc);
      } catch (ParserConfigurationException ex) {
         throw new OseeWrappedException("Can't create ats note document", ex);
      } catch (DOMException ex) {
         throw new OseeWrappedException("Can't create ats note document", ex);
      } catch (TransformerException ex) {
         throw new OseeWrappedException("Can't create ats note document", ex);
      }
   }

}