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
package org.eclipse.osee.ats.api.workflow.notes;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osee.ats.api.IAtsWorkItem;

/**
 * @author Donald G. Dunne
 */
public interface IAtsNoteStore {

   String getNoteXml(IAtsWorkItem workItem);

   IStatus saveNoteXml(IAtsWorkItem workItem, String xml);

   String getNoteTitle(IAtsWorkItem workItem);

   String getNoteId(IAtsWorkItem workItem);

   boolean isNoteable(IAtsWorkItem workItem);
}
