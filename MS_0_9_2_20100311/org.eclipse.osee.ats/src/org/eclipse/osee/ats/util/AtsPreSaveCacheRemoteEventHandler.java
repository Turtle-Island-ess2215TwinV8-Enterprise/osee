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
package org.eclipse.osee.ats.util;

import java.util.logging.Level;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.FrameworkTransactionData;
import org.eclipse.osee.framework.skynet.core.event.IFrameworkTransactionEventListener;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.Sender;
import org.eclipse.osee.framework.skynet.core.utility.DbUtil;

/**
 * This class handles updating ATS state machine artifacts based on remote events that change the assignees. Without
 * this, the client will think it changed the assignees if the artifact is saved after the remote modified event.
 * 
 * @author Donald G. Dunne
 */
public class AtsPreSaveCacheRemoteEventHandler implements IFrameworkTransactionEventListener {

   private static AtsPreSaveCacheRemoteEventHandler instance = new AtsPreSaveCacheRemoteEventHandler();

   public static AtsPreSaveCacheRemoteEventHandler getInstance() {
      return instance;
   }

   private AtsPreSaveCacheRemoteEventHandler() {
      if (DbUtil.isDbInit()) return;
      OseeLog.log(AtsPlugin.class, Level.INFO, "Starting ATS Pre-Save Remote Event Handler");
      OseeEventManager.addListener(this);
   }

   public void dispose() {
      OseeEventManager.removeListener(this);
   }

   @Override
   public void handleFrameworkTransactionEvent(Sender sender, FrameworkTransactionData transData) throws OseeCoreException {
      if (DbUtil.isDbInit()) return;
      if (transData.branchId != AtsUtil.getAtsBranch().getId()) return;
      for (Artifact artifact : transData.cacheChangedArtifacts) {
         if (artifact instanceof StateMachineArtifact) {
            ((StateMachineArtifact) artifact).initalizePreSaveCache();
         }
      }
   }

}
