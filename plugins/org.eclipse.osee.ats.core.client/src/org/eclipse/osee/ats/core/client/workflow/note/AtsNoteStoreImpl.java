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
package org.eclipse.osee.ats.core.client.workflow.note;

import java.util.logging.Level;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.ats.api.IAtsWorkItem;
import org.eclipse.osee.ats.api.data.AtsAttributeTypes;
import org.eclipse.osee.ats.api.workflow.notes.IAtsNoteStore;
import org.eclipse.osee.ats.core.client.internal.Activator;
import org.eclipse.osee.ats.core.client.util.WorkItemUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class AtsNoteStoreImpl implements IAtsNoteStore {

   public AtsNoteStoreImpl() {
   }

   @Override
   public String getNoteXml(IAtsWorkItem workItem) {
      try {
         return getArtifact(workItem).getSoleAttributeValue(AtsAttributeTypes.StateNotes, "");
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return "getLogXml exception " + ex.getLocalizedMessage();
      }
   }

   @Override
   public IStatus saveNoteXml(IAtsWorkItem workItem, String xml) {
      try {
         getArtifact(workItem).setSoleAttributeValue(AtsAttributeTypes.StateNotes, xml);
         return Status.OK_STATUS;
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "saveLogXml exception " + ex.getLocalizedMessage());
      }
   }

   public Artifact getArtifact(IAtsWorkItem workItem) throws OseeCoreException {
      return WorkItemUtil.get(workItem);
   }

   @Override
   public String getNoteTitle(IAtsWorkItem workItem) {
      try {
         return "History for \"" + getArtifact(workItem).getArtifactTypeName() + "\" - " + getArtifact(workItem).getHumanReadableId() + " - titled \"" + getArtifact(
            workItem).getName() + "\"";
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return "getLogTitle exception " + ex.getLocalizedMessage();
      }
   }

   @Override
   public String getNoteId(IAtsWorkItem workItem) {
      try {
         return getArtifact(workItem).getHumanReadableId();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return "unknown";
   }

   @Override
   public boolean isNoteable(IAtsWorkItem workItem) {
      try {
         return getArtifact(workItem).isAttributeTypeValid(AtsAttributeTypes.StateNotes);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
      return false;
   }

}
