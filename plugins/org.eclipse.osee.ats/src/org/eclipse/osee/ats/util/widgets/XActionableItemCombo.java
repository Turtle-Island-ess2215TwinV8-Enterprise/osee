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
package org.eclipse.osee.ats.util.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.osee.ats.api.ai.IAtsActionableItem;
import org.eclipse.osee.ats.core.config.ActionableItemSorter;
import org.eclipse.osee.ats.core.config.ActionableItems;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.widgets.XComboViewer;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Donald G. Dunne
 */
public class XActionableItemCombo extends XComboViewer {
   public static final String WIDGET_ID = XActionableItemCombo.class.getSimpleName();
   private IAtsActionableItem selectedAi = null;

   public XActionableItemCombo() {
      super("Actionable Item", SWT.READ_ONLY);
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      super.createControls(parent, horizontalSpan);

      Collection<IAtsActionableItem> teamDefs = null;
      try {
         teamDefs = ActionableItems.getActionableItems(Active.Active);
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, "Error loading actionable items", ex);
      }

      List<IAtsActionableItem> sortedAiArts = new ArrayList<IAtsActionableItem>();
      sortedAiArts.addAll(teamDefs);
      Collections.sort(sortedAiArts, new ActionableItemSorter());
      getComboViewer().setInput(sortedAiArts);
      ArrayList<Object> defaultSelection = new ArrayList<Object>();
      defaultSelection.add("--select--");
      setSelected(defaultSelection);
      addXModifiedListener(new XModifiedListener() {

         @Override
         public void widgetModified(XWidget widget) {
            selectedAi = (IAtsActionableItem) getSelected();
         }
      });
   }

   public IAtsActionableItem getSelectedAi() {
      return selectedAi;
   }

}
