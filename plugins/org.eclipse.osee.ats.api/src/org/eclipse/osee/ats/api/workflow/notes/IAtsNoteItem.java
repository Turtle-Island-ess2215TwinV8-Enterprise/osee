/*
 * Created on Jul 25, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.api.workflow.notes;

import java.util.Date;
import org.eclipse.osee.ats.api.user.IAtsUser;

public interface IAtsNoteItem {

   public abstract Date getDate();

   public abstract String getMsg();

   @Override
   public abstract String toString();

   public abstract IAtsUser getUser();

   public abstract NoteType getType();

   public abstract String toHTML();

   public abstract String getState();

}