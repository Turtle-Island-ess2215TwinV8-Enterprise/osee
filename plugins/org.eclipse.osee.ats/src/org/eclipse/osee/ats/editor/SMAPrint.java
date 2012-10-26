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

package org.eclipse.osee.ats.editor;

import java.util.Arrays;
import org.eclipse.jface.action.Action;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.core.workflow.AtsWorkItemService;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;
import org.eclipse.osee.framework.ui.skynet.results.html.XResultPage.Manipulations;

/**
 * @author Donald G. Dunne
 */
public class SMAPrint extends Action {

   private final AbstractWorkflowArtifact sma;
   boolean includeTaskList = true;

   public SMAPrint(AbstractWorkflowArtifact sma) {
      super();
      this.sma = sma;
   }

   @Override
   public void run() {
      try {
         XResultData resultData = getResultData();

         XResultData rd = new XResultData();
         rd.addRaw(AHTML.beginMultiColumnTable(100, 1));
         rd.addRaw(AHTML.addRowMultiColumnTable(new String[] {XResultDataUI.getReport(resultData, "").getManipulatedHtml(
            Arrays.asList(Manipulations.NONE))}));
         rd.addRaw(AHTML.endMultiColumnTable());
         XResultDataUI.report(rd, "Print Preview of " + sma.getName(), Manipulations.RAW_HTML);

      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }

   }

   public XResultData getResultData() throws OseeCoreException {
      return AtsWorkItemService.get().getPrintHtml(sma);
   }

   public boolean isIncludeTaskList() {
      return includeTaskList;
   }

   public void setIncludeTaskList(boolean includeTaskList) {
      this.includeTaskList = includeTaskList;
   }

}
