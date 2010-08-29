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
package org.eclipse.osee.ats.artifact;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.ats.NoteType;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.framework.core.data.SystemUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeStateException;
import org.eclipse.osee.framework.core.exception.UserNotInDatabase;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.jdk.core.util.xml.Jaxp;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Donald G. Dunne
 */
public class ATSNote {
   private final WeakReference<Artifact> artifactRef;
   private boolean enabled = true;
   private static String ATS_NOTE_TAG = "AtsNote";
   private static String LOG_ITEM_TAG = "Item";

   public ATSNote(Artifact artifact) {
      this.artifactRef = new WeakReference<Artifact>(artifact);
   }

   public Artifact getArtifact() throws OseeStateException {
      if (artifactRef.get() == null) {
         throw new OseeStateException("Artifact has been garbage collected");
      }
      return artifactRef.get();
   }

   public void addNote(NoteType type, String state, String msg, User user) {
      addNote(type, state, msg, new Date(), user);
   }

   public void addNoteItem(NoteItem noteItem) {
      addNote(noteItem.getType(), noteItem.getState(), noteItem.getMsg(), noteItem.getDate(), noteItem.getUser());
   }

   public void addNote(NoteType type, String state, String msg, Date date, User user) {
      if (!enabled) {
         return;
      }
      NoteItem logItem = new NoteItem(type, state, date.getTime() + "", user, msg);
      List<NoteItem> logItems = getNoteItems();
      if (logItems.isEmpty()) {
         logItems = Arrays.asList(logItem);
      } else {
         logItems.add(logItem);
      }
      saveNoteItems(logItems);
   }

   public static List<NoteItem> getNoteItems(String str, String hrid) {
      List<NoteItem> logItems = new ArrayList<NoteItem>();
      try {
         if (Strings.isValid(str)) {
            NodeList nodes = Jaxp.readXmlDocument(str).getElementsByTagName(LOG_ITEM_TAG);
            for (int i = 0; i < nodes.getLength(); i++) {
               Element element = (Element) nodes.item(i);
               try {
                  User user = UserManager.getUserByUserId(element.getAttribute("userId"));
                  NoteItem item =
                     new NoteItem(element.getAttribute("type"), element.getAttribute("state"),
                        element.getAttribute("date"), user, element.getAttribute("msg"));
                  logItems.add(item);
               } catch (UserNotInDatabase ex) {
                  OseeLog.log(AtsPlugin.class, Level.SEVERE, String.format("Error parsing notes for [%s]", hrid), ex);
                  NoteItem item =
                     new NoteItem(element.getAttribute("type"), element.getAttribute("state"),
                        element.getAttribute("date"), UserManager.getUser(SystemUser.Guest),
                        element.getAttribute("msg"));
                  logItems.add(item);
               }
            }
         }
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return logItems;
   }

   public List<NoteItem> getNoteItems() {
      try {
         String xml = getArtifact().getSoleAttributeValue(AtsAttributeTypes.StateNotes, "");
         if (Strings.isValid(xml)) {
            return getNoteItems(xml, getArtifact().getHumanReadableId());
         }
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return Collections.emptyList();
   }

   public void saveNoteItems(List<NoteItem> items) {
      try {
         Document doc = Jaxp.newDocument();
         Element rootElement = doc.createElement(ATS_NOTE_TAG);
         doc.appendChild(rootElement);
         for (NoteItem item : items) {
            Element element = doc.createElement(LOG_ITEM_TAG);
            element.setAttribute("type", item.getType().name());
            element.setAttribute("state", item.getState());
            element.setAttribute("date", item.getDate().getTime() + "");
            element.setAttribute("userId", item.getUser().getUserId());
            element.setAttribute("msg", item.getMsg());
            rootElement.appendChild(element);
         }
         getArtifact().setSoleAttributeValue(AtsAttributeTypes.StateNotes, Jaxp.getDocumentXml(doc));
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, "Can't create ats note document", ex);
      }
   }

   /**
    * Display Note Table; If state == null, only display non-state notes Otherwise, show only notes associated with
    * state
    * 
    */
   public String getTable(String state) {
      ArrayList<NoteItem> showNotes = new ArrayList<NoteItem>();
      List<NoteItem> noteItems = getNoteItems();
      try {
         if (!getArtifact().isAttributeTypeValid(AtsAttributeTypes.StateNotes)) {
            return "";
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
         return "";
      }

      for (NoteItem li : noteItems) {
         if (state == null) {
            if (li.getState().equals("")) {
               showNotes.add(li);
            }
         } else if (state.equals("ALL") || li.getState().equals(state)) {
            showNotes.add(li);
         }
      }
      if (showNotes.isEmpty()) {
         return "";
      }
      StringBuilder builder = new StringBuilder();
      builder.append("<TABLE BORDER=\"1\" cellspacing=\"1\" cellpadding=\"3%\" width=\"100%\"><THEAD><TR><TH>Type</TH><TH>State</TH>" + "<TH>Message</TH><TH>User</TH><TH>Date</TH></THEAD></TR>");
      for (NoteItem note : showNotes) {
         User user = note.getUser();
         String name = "";
         if (user != null) {
            name = user.getName();
            if (!Strings.isValid(name)) {
               name = user.getName();
            }
         }
         builder.append("<TR>");
         builder.append("<TD>" + note.getType() + "</TD>");
         builder.append("<TD>" + (note.getState().equals("") ? "," : note.getState()) + "</TD>");
         builder.append("<TD>" + (note.getMsg().equals("") ? "," : note.getMsg()) + "</TD>");

         if (user != null && user.isMe()) {
            builder.append("<TD bgcolor=\"#CCCCCC\">" + name + "</TD>");
         } else {
            builder.append("<TD>" + name + "</TD>");
         }

         builder.append("<TD>" + new SimpleDateFormat("MM/dd/yyyy h:mm a").format(note.getDate()) + "</TD>");
         builder.append("</TR>");
      }
      builder.append("</TABLE>");
      return builder.toString();
   }

   public boolean isEnabled() {
      return enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

}