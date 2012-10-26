/*
 * Created on Aug 1, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.impl.internal.workitem.notes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workflow.notes.IAtsNoteItem;
import org.eclipse.osee.ats.api.workflow.notes.IAtsNoteService;
import org.eclipse.osee.ats.api.workflow.notes.NoteType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.Strings;

public class AtsNoteServiceImpl implements IAtsNoteService {

   public static AtsNoteServiceImpl instance;

   public AtsNoteServiceImpl() {
      instance = this;
   }

   /**
    * Display Note Table; If state == null, only display non-state notes Otherwise, show only notes associated with
    * state
    * 
    * @throws OseeCoreException
    */
   @Override
   public String getTable(String state, IAtsWorkItem workItem) throws OseeCoreException {
      ArrayList<IAtsNoteItem> showNotes = new ArrayList<IAtsNoteItem>();
      List<IAtsNoteItem> noteItems = getNoteItems(workItem);

      for (IAtsNoteItem item : noteItems) {
         if (state == null && item.getState().equals("")) {
            showNotes.add(item);
         } else if (state != null && ("ALL".equals(state) || item.getState().equals(state))) {
            showNotes.add(item);
         }
      }
      if (showNotes.isEmpty()) {
         return "";
      }
      return buildTable(showNotes);
   }

   private String buildTable(List<IAtsNoteItem> showNotes) {
      StringBuilder builder = new StringBuilder();
      builder.append(AHTML.beginMultiColumnTable(100, 1));
      builder.append(AHTML.addHeaderRowMultiColumnTable(Arrays.asList("Type", "State", "Message", "User", "Date")));
      DateFormat dateFormat = getDateFormat();
      for (IAtsNoteItem note : showNotes) {
         IAtsUser user = note.getUser();
         String name = "";
         if (user != null) {
            name = user.getName();
            if (!Strings.isValid(name)) {
               name = user.getName();
            }
         }
         builder.append(AHTML.addRowMultiColumnTable(String.valueOf(note.getType()),
            (note.getState().isEmpty() ? "," : note.getState()), (note.getMsg().equals("") ? "," : note.getMsg()),
            name, dateFormat.format(note.getDate())));
      }
      builder.append(AHTML.endMultiColumnTable());
      return builder.toString();
   }

   public DateFormat getDateFormat() {
      return new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.US);
   }

   @Override
   public List<IAtsNoteItem> getNoteItems(IAtsWorkItem workItem) throws OseeCoreException {
      try {
         String xml = AtsNoteStoreService.get().getNoteXml(workItem);
         if (Strings.isValid(xml)) {
            return NoteItem.fromXml(xml, AtsNoteStoreService.get().getNoteId(workItem));
         }
      } catch (Exception ex) {
         throw new OseeWrappedException(ex);
      }
      return Collections.emptyList();
   }

   @Override
   public void addNote(IAtsWorkItem workItem, NoteType type, String state, String msg, IAtsUser user) throws OseeCoreException {
      addNote(workItem, type, state, msg, new Date(), user);
   }

   @Override
   public void addNoteItem(IAtsWorkItem workItem, IAtsNoteItem noteItem) throws OseeCoreException {
      addNote(workItem, noteItem.getType(), noteItem.getState(), noteItem.getMsg(), noteItem.getDate(),
         noteItem.getUser());
   }

   @Override
   public void addNote(IAtsWorkItem workItem, NoteType type, String state, String msg, Date date, IAtsUser user) throws OseeCoreException {
      if (!AtsNoteStoreService.get().isNoteable(workItem)) {
         return;
      }
      NoteItem logItem = new NoteItem(type, state, String.valueOf(date.getTime()), user, msg);
      List<IAtsNoteItem> logItems = getNoteItems(workItem);
      if (logItems.isEmpty()) {
         logItems = new ArrayList<IAtsNoteItem>();
         logItems.add(logItem);
      } else {
         logItems.add(logItem);
      }
      saveNoteItems(workItem, logItems);
   }

   @Override
   public void saveNoteItems(IAtsWorkItem workItem, List<IAtsNoteItem> items) throws OseeCoreException {
      try {
         String xml = NoteItem.toXml(items);
         AtsNoteStoreService.get().saveNoteXml(workItem, xml);
      } catch (Exception ex) {
         throw new OseeWrappedException("Can't create ats note document", ex);
      }
   }

}
