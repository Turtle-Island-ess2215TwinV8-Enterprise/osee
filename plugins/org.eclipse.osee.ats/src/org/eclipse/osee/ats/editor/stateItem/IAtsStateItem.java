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
package org.eclipse.osee.ats.editor.stateItem;

import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.ats.editor.SMAWorkFlowSection;
import org.eclipse.osee.ats.workflow.AtsWorkPage;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Donald G. Dunne
 */
public interface IAtsStateItem {

   public Result pageCreated(FormToolkit toolkit, AtsWorkPage page, StateMachineArtifact sma, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException;

   public Result xWidgetCreating(XWidget xWidget, FormToolkit toolkit, AtsWorkPage page, Artifact art, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException;

   public void xWidgetCreated(XWidget xWidget, FormToolkit toolkit, AtsWorkPage page, Artifact art, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException;

   public void widgetModified(SMAWorkFlowSection section, XWidget xWidget) throws OseeCoreException;

   public String getOverrideTransitionToStateName(SMAWorkFlowSection section) throws OseeCoreException;

   public Collection<User> getOverrideTransitionToAssignees(SMAWorkFlowSection section) throws OseeCoreException;

   public String getDescription() throws OseeCoreException;

   public String getBranchShortName(StateMachineArtifact sma) throws OseeCoreException;

   public boolean isAccessControlViaAssigneesEnabledForBranching() throws OseeCoreException;

   public Collection<String> getIds() throws OseeCoreException;

   /**
    * @return Result of operation. If Result.isFalse(), transition will not continue and Result.popup will occur.
    * @throws Exception
    */
   public Result transitioning(StateMachineArtifact sma, String fromState, String toState, Collection<User> toAssignees) throws OseeCoreException;

   public void transitioned(StateMachineArtifact sma, String fromState, String toState, Collection<User> toAssignees, SkynetTransaction transaction) throws OseeCoreException;

   /**
    * @return Result of operation. If Result.isFalse(), commit will not continue and Result.popup will occur.
    * @throws Exception
    */
   public Result committing(StateMachineArtifact sma) throws OseeCoreException;

   public List<XWidget> getDynamicXWidgetsPostBody(StateMachineArtifact sma) throws OseeCoreException;

   public List<XWidget> getDynamicXWidgetsPreBody(StateMachineArtifact sma) throws OseeCoreException;

}
