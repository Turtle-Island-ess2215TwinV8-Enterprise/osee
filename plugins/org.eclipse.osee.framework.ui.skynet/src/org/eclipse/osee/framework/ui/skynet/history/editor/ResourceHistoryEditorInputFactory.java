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
package org.eclipse.osee.framework.ui.skynet.history.editor;

import java.util.logging.Level;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.util.SkynetViews;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * The factory which is capable of recreating class file editor inputs stored in a memento.
 */
public class ResourceHistoryEditorInputFactory implements IElementFactory {

   public final static String ID =
      "org.eclipse.osee.framework.ui.skynet.history.editor.ResourceHistoryEditorInputFactory"; //$NON-NLS-1$
   private final static String ARTIFACT_NAME_KEY = "org.eclipse.osee.framework.ui.skynet.history.artName"; //$NON-NLS-1$
   private final static String ARTIFACT_GUID_KEY = "org.eclipse.osee.framework.ui.skynet.history.artGuid"; //$NON-NLS-1$
   private final static String ARTIFACT_BRANCH_GUID_KEY = "org.eclipse.osee.framework.ui.skynet.history.branchGuid"; //$NON-NLS-1$

   @Override
   public IAdaptable createElement(IMemento memento) {
      ResourceHistoryEditorInput toReturn = null;
      try {
         if (memento != null) {
            if (SkynetViews.isSourceValid(memento)) {
               String artName = memento.getString(ARTIFACT_NAME_KEY);
               String branchGuid = memento.getString(ARTIFACT_BRANCH_GUID_KEY);
               String artGuid = memento.getString(ARTIFACT_GUID_KEY);
               toReturn = new ResourceHistoryEditorInput(artName, artGuid, branchGuid);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.WARNING, "Resource History error on init", ex);
      }
      return toReturn;
   }

   public static void saveState(IMemento memento, ResourceHistoryEditorInput input) {
      memento.putString(ARTIFACT_NAME_KEY, input.getArtName());
      memento.putString(ARTIFACT_GUID_KEY, input.getArtGuid());
      memento.putString(ARTIFACT_BRANCH_GUID_KEY, input.getBranchGuid());
      SkynetViews.addDatabaseSourceId(memento);
   }
}
