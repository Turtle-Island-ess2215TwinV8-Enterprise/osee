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
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.ats.api.user.IAtsUser;
import org.eclipse.osee.ats.api.workdef.IAtsStateDefinition;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.ats.editor.SMAWorkFlowSection;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Donald G. Dunne
 */
public abstract class AtsStateItem implements IAtsStateItem {

   public final static String ALL_STATE_IDS = "ALL";
   private final String name;

   public AtsStateItem(String name) {
      this.name = name;
   }

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

   @Override
   public String getName() {
      return name;
   }

   @SuppressWarnings("unused")
   @Override
   public Collection<IAtsUser> getOverrideTransitionToAssignees(AbstractWorkflowArtifact awa, String decision) throws OseeCoreException {
      return null;
   }

   @SuppressWarnings("unused")
   @Override
   public String getOverrideTransitionToStateName(SMAWorkFlowSection section) throws OseeCoreException {
      return null;
   }

   @SuppressWarnings("unused")
   @Override
   public List<XWidget> getDynamicXWidgetsPostBody(AbstractWorkflowArtifact sma, String stateName) throws OseeCoreException {
      return Collections.emptyList();
   }

   @SuppressWarnings("unused")
   @Override
   public List<XWidget> getDynamicXWidgetsPreBody(AbstractWorkflowArtifact sma, String stateName) throws OseeCoreException {
      return Collections.emptyList();
   }

   @SuppressWarnings("unused")
   @Override
   public void xWidgetCreated(XWidget xWidget, FormToolkit toolkit, IAtsStateDefinition stateDefinition, Artifact art, boolean isEditable) throws OseeCoreException {
      // provided for subclass implementation
   }

   @SuppressWarnings("unused")
   @Override
   public void widgetModified(XWidget xWidget, FormToolkit toolkit, IAtsStateDefinition stateDefinition, Artifact art, boolean isEditable) throws OseeCoreException {
      // provided for subclass implementation
   }

   @SuppressWarnings("unused")
   @Override
   public Result xWidgetCreating(XWidget xWidget, FormToolkit toolkit, IAtsStateDefinition stateDefinition, Artifact art, boolean isEditable) throws OseeCoreException {
      return Result.TrueResult;
   }

   @SuppressWarnings("unused")
   @Override
   public boolean isAccessControlViaAssigneesEnabledForBranching() throws OseeCoreException {
      return false;
   }

   @Override
   public String getFullName() {
      return getClass().getName();
   }

   @Override
   public String toString() {
      return getName();
   }
}