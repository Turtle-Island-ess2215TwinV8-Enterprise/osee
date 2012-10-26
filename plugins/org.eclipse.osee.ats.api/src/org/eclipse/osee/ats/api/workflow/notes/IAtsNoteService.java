/*
 * Created on Aug 1, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.workflow.notes;

import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;

public interface IAtsNoteService {

   public String getTable(String state, IAtsWorkItem workItem) throws OseeCoreException;

   public List<IAtsNoteItem> getNoteItems(IAtsWorkItem workItem) throws OseeCoreException;

   public void addNote(IAtsWorkItem workItem, NoteType type, String state, String msg, IAtsUser user) throws OseeCoreException;

   public void addNoteItem(IAtsWorkItem workItem, IAtsNoteItem noteItem) throws OseeCoreException;

   public void addNote(IAtsWorkItem workItem, NoteType type, String state, String msg, Date date, IAtsUser user) throws OseeCoreException;

   public void saveNoteItems(IAtsWorkItem workItem, List<IAtsNoteItem> items) throws OseeCoreException;

}
