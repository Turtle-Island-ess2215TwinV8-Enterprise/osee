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
package org.eclipse.osee.framework.skynet.core.httpRequests;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.OseeServerContext;
import org.eclipse.osee.framework.core.enums.BranchState;
import org.eclipse.osee.framework.core.enums.CoreTranslatorId;
import org.eclipse.osee.framework.core.enums.Function;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.message.ChangeBranchStateRequest;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.util.HttpProcessor.AcquireResult;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.HttpClientMessage;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEvent;
import org.eclipse.osee.framework.skynet.core.event.model.BranchEventType;
import org.eclipse.osee.framework.skynet.core.internal.Activator;

/**
 * @author Megumi Telles
 * @author Ryan D. Brooks
 */
public class UpdateBranchStateHttpRequestOperation extends AbstractOperation {
   private final int branchId;
   private final String branchGuid;
   private final BranchState branchState;

   public UpdateBranchStateHttpRequestOperation(int branchId, String branchGuid, BranchState branchState) {
      super("Update branch state " + branchGuid, Activator.PLUGIN_ID);
      this.branchId = branchId;
      this.branchGuid = branchGuid;
      this.branchState = branchState;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws OseeCoreException {
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("function", Function.UPDATE_BRANCH_STATE.name());

      ChangeBranchStateRequest requestData = new ChangeBranchStateRequest(branchId, branchState);
      AcquireResult response =
         HttpClientMessage.send(OseeServerContext.BRANCH_CONTEXT, parameters, CoreTranslatorId.CHANGE_BRANCH_STATE,
            requestData, null);

      if (response.wasSuccessful()) {
         BranchManager.refreshBranches();
         OseeEventManager.kickBranchEvent(getClass(), new BranchEvent(BranchEventType.StateUpdated, branchGuid));
      }
   }
}
