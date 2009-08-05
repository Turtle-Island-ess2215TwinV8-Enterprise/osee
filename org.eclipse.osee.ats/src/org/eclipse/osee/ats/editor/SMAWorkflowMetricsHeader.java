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

import java.util.logging.Level;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.world.WorldXViewerFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.event.FrameworkTransactionData;
import org.eclipse.osee.framework.skynet.core.event.IFrameworkTransactionEventListener;
import org.eclipse.osee.framework.skynet.core.event.OseeEventManager;
import org.eclipse.osee.framework.skynet.core.event.Sender;
import org.eclipse.osee.framework.ui.plugin.util.Displays;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.FontManager;
import org.eclipse.osee.framework.ui.skynet.XFormToolkit;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Donald G. Dunne
 */
public class SMAWorkflowMetricsHeader extends Composite implements IFrameworkTransactionEventListener {

   private final SMAManager smaMgr;
   private Label percentLabel, estHoursLabel, hoursSpentLabel, remainHoursLabel;

   public SMAWorkflowMetricsHeader(Composite parent, XFormToolkit toolkit, SMAManager smaMgr) throws OseeCoreException {
      super(parent, SWT.NONE);
      this.smaMgr = smaMgr;
      try {

         toolkit.adapt(this);
         setLayout(ALayout.getZeroMarginLayout(8, false));
         setLayoutData(new GridData());

         Label label = toolkit.createLabel(this, "Total Percent: ", SWT.NONE);
         SMAEditor.setLabelFonts(label, FontManager.getDefaultLabelFont());
         percentLabel = toolkit.createLabel(this, "", SWT.NONE);
         label.setToolTipText("Calculation: sum of percent for all states (including all tasks and reviews) / # statusable states");
         percentLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

         label = toolkit.createLabel(this, "Total Estimated Hours: ", SWT.NONE);
         SMAEditor.setLabelFonts(label, FontManager.getDefaultLabelFont());
         estHoursLabel = toolkit.createLabel(this, "", SWT.NONE);
         label.setToolTipText("Calculation: sum estimated hours for workflow and all tasks and reviews");
         estHoursLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

         label = toolkit.createLabel(this, "Total Hours Spent: ", SWT.NONE);
         SMAEditor.setLabelFonts(label, FontManager.getDefaultLabelFont());
         hoursSpentLabel = toolkit.createLabel(this, "", SWT.NONE);
         hoursSpentLabel.setToolTipText("Calculation: sum of all hours spent for all tasks, reviews and in each state");
         label.setToolTipText("Calculation: sum of all hours spent for all tasks, reviews and in each state");

         label = toolkit.createLabel(this, "Remaining Hours: ", SWT.NONE);
         SMAEditor.setLabelFonts(label, FontManager.getDefaultLabelFont());
         label.setToolTipText(WorldXViewerFactory.Remaining_Hours_Col.getDescription());
         remainHoursLabel = toolkit.createLabel(this, "", SWT.NONE);

         refresh();

         OseeEventManager.addListener(this);
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }
   }

   private void refresh() throws OseeCoreException {
      if (percentLabel.isDisposed()) {
         OseeEventManager.removeListener(this);
         return;
      }
      try {
         if (!percentLabel.isDisposed())
            percentLabel.setText(String.valueOf(smaMgr.getSma().getPercentCompleteSMATotal()));
         if (estHoursLabel != null && !estHoursLabel.isDisposed())
            estHoursLabel.setText(String.valueOf(AtsUtil.doubleToI18nString(smaMgr.getSma().getEstimatedHoursTotal())));
         if (hoursSpentLabel != null && !hoursSpentLabel.isDisposed())
            hoursSpentLabel.setText(String.valueOf(AtsUtil.doubleToI18nString(smaMgr.getSma().getHoursSpentSMATotal())));
         if (hoursSpentLabel != null && !hoursSpentLabel.isDisposed()) {
            Result result = smaMgr.getSma().isWorldViewRemainHoursValid();
            if (result.isFalse())
               remainHoursLabel.setText("Error" + result.getText());
            else
               remainHoursLabel.setText(String.valueOf(AtsUtil.doubleToI18nString(smaMgr.getSma().getWorldViewRemainHours())));
         }
      } catch (Exception ex) {
         OseeLog.log(AtsPlugin.class, Level.SEVERE, ex);
      }

      percentLabel.update();
      layout();
   }

   @Override
   public void handleFrameworkTransactionEvent(Sender sender, FrameworkTransactionData transData) throws OseeCoreException {
      if (smaMgr.isInTransition())
         return;
      if (transData.branchId != AtsUtil.getAtsBranch().getBranchId())
         return;
      Displays.ensureInDisplayThread(new Runnable() {
         @Override
         public void run() {
            try {
               refresh();
            } catch (OseeCoreException ex) {
               OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      });
   }

   @Override
   public void dispose() {
      super.dispose();
      OseeEventManager.removeListener(this);
   }

}
