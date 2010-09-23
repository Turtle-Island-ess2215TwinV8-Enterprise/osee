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
package org.eclipse.osee.ats.internal.workflow;

import java.util.Date;
import java.util.Set;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.artifact.AtsAttributeTypes;
import org.eclipse.osee.ats.artifact.log.LogItem;
import org.eclipse.osee.ats.artifact.log.LogType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.IWorkPage;

/**
 * @author Donald G. Dunne
 */
public class XCurrentStateDam extends XStateAssigneesDam {

   public XCurrentStateDam(AbstractWorkflowArtifact sma) throws OseeCoreException {
      super(sma, AtsAttributeTypes.CurrentState);
   }

   @Override
   public void onAttributeTypeSet() throws OseeCoreException {
      super.set(getArtifact().getSoleAttributeValue(getAttributeType(), ""));
   }

   public SMAState getState() throws OseeCoreException {
      Set<SMAState> states = getStates();
      if (states.size() != 1) {
         throw new OseeArgumentException("Must be one current state.  Found %d for %s", states.size(),
            getArtifact().getGuid());
      }
      return states.iterator().next();
   }

   @Override
   public void setState(SMAState state) throws OseeCoreException {
      AbstractWorkflowArtifact sma = getArtifact();
      sma.setSoleAttributeValue(getAttributeType(), state.toXml());
   }

   public void updateMetrics(double additionalHours, int percentComplete, boolean logMetrics) throws OseeCoreException {
      SMAState currState = getState();
      currState.setHoursSpent(currState.getHoursSpent() + additionalHours);
      currState.setPercentComplete(percentComplete);
      setState(currState);
      if (logMetrics) {
         logMetrics(getArtifact().getStateMgr().getCurrentState(), UserManager.getUser(), new Date());
      }
   }

   public void setMetrics(double hours, int percentComplete, boolean logMetrics, User user, Date date) throws OseeCoreException {
      SMAState currState = getState();
      currState.setHoursSpent(hours);
      currState.setPercentComplete(percentComplete);
      setState(currState);
      if (logMetrics) {
         logMetrics(getArtifact().getStateMgr().getCurrentState(), user, date);
      }
   }

   public static void logMetrics(AbstractWorkflowArtifact sma, String percent, String hours, IWorkPage state, User user, Date date) throws OseeCoreException {
      LogItem logItem =
         new LogItem(LogType.Metrics, date, user, state.getPageName(), String.format("Percent %s Hours %s", percent,
            hours), sma.getHumanReadableId());
      sma.getLog().addLogItem(logItem);
   }
}