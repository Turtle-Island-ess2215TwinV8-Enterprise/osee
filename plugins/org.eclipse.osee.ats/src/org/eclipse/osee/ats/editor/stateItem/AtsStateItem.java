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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.ats.artifact.AbstractWorkflowArtifact;
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
public abstract class AtsStateItem implements IAtsStateItem {

   public final static String ALL_STATE_IDS = "ALL";

   @SuppressWarnings("unused")
   @Override
   public Result committing(AbstractWorkflowArtifact sma) throws OseeCoreException {
      return Result.TrueResult;
   }

   @SuppressWarnings("unused")
   @Override
   public String getBranchShortName(AbstractWorkflowArtifact sma) throws OseeCoreException {
      return null;
   }

   protected String getId() {
      return null;
   }

   @SuppressWarnings("unused")
   @Override
   public Collection<String> getIds() throws OseeCoreException {
      if (getId() == null) {
         return Collections.emptyList();
      }
      return Arrays.asList(getId());
   }

   @SuppressWarnings("unused")
   @Override
   public Collection<User> getOverrideTransitionToAssignees(SMAWorkFlowSection section) throws OseeCoreException {
      return null;
   }

   @SuppressWarnings("unused")
   @Override
   public String getOverrideTransitionToStateName(SMAWorkFlowSection section) throws OseeCoreException {
      return null;
   }

   @SuppressWarnings("unused")
   @Override
   public List<XWidget> getDynamicXWidgetsPostBody(AbstractWorkflowArtifact sma) throws OseeCoreException {
      return Collections.emptyList();
   }

   @SuppressWarnings("unused")
   @Override
   public List<XWidget> getDynamicXWidgetsPreBody(AbstractWorkflowArtifact sma) throws OseeCoreException {
      return Collections.emptyList();
   }

   @SuppressWarnings("unused")
   @Override
   public Result pageCreated(FormToolkit toolkit, AtsWorkPage page, AbstractWorkflowArtifact sma, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      return Result.TrueResult;
   }

   @SuppressWarnings("unused")
   @Override
   public void transitioned(AbstractWorkflowArtifact sma, String fromState, String toState, Collection<User> toAssignees, SkynetTransaction transaction) throws OseeCoreException {
      // provided for subclass implementation
   }

   @SuppressWarnings("unused")
   @Override
   public Result transitioning(AbstractWorkflowArtifact sma, String fromState, String toState, Collection<User> toAssignees) throws OseeCoreException {
      return Result.TrueResult;
   }

   @SuppressWarnings("unused")
   @Override
   public void widgetModified(SMAWorkFlowSection section, XWidget xWidget) throws OseeCoreException {
      // provided for subclass implementation
   }

   @SuppressWarnings("unused")
   @Override
   public void xWidgetCreated(XWidget xWidget, FormToolkit toolkit, AtsWorkPage page, Artifact art, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      // provided for subclass implementation
   }

   @SuppressWarnings("unused")
   @Override
   public Result xWidgetCreating(XWidget xWidget, FormToolkit toolkit, AtsWorkPage page, Artifact art, XModifiedListener xModListener, boolean isEditable) throws OseeCoreException {
      return Result.TrueResult;
   }

   @SuppressWarnings("unused")
   @Override
   public boolean isAccessControlViaAssigneesEnabledForBranching() throws OseeCoreException {
      return false;
   }
}