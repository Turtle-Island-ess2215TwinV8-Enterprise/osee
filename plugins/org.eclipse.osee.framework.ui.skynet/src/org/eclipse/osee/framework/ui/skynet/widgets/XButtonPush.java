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
package org.eclipse.osee.framework.ui.skynet.widgets;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.ui.swt.ALayout;
import org.eclipse.osee.framework.ui.swt.CursorManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Donald G. Dunne
 */
public class XButtonPush extends XWidget {

   protected Button button;
   private Composite parent;
   private Composite bComp;
   protected boolean selected = false;
   private boolean labelAfter = true;
   private Image image;

   public XButtonPush(String displayLabel, String xmlRoot) {
      super(displayLabel, xmlRoot);
   }

   public XButtonPush(String displayLabel) {
      this(displayLabel, "");
   }

   public XButtonPush(String displayLabel, Image image) {
      this(displayLabel, "");
      this.image = image;
   }

   @Override
   public Button getControl() {
      return button;
   }

   /**
    * Create Check Widgets. Widgets Created: Label: "text entry" horizonatalSpan takes up 2 columns; horizontalSpan must
    * be >=2
    */
   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      if (horizontalSpan < 2) {
         horizontalSpan = 2;
      }
      this.parent = parent;

      bComp = new Composite(parent, SWT.NONE);
      bComp.setLayout(ALayout.getZeroMarginLayout(2, false));
      bComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      if (toolkit != null) {
         toolkit.adapt(bComp);
      }

      // Create Text Widgets
      if (isDisplayLabel() && !labelAfter) {
         labelWidget = new Label(bComp, SWT.NONE);
         labelWidget.setText(getLabel() + ":");
      }

      if (toolkit != null) {
         button = toolkit.createButton(bComp, "", SWT.PUSH);
      } else {
         button = new Button(bComp, SWT.PUSH);
      }
      GridData gd2 = new GridData(GridData.BEGINNING);
      button.setLayoutData(gd2);
      button.addListener(SWT.MouseUp, new Listener() {
         @Override
         public void handleEvent(Event event) {
            validate();
            notifyXModifiedListeners();
         }
      });
      GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
      gd.horizontalSpan = horizontalSpan - 1;

      if (isDisplayLabel() && labelAfter) {
         labelWidget = new Label(bComp, SWT.NONE);
         labelWidget.setText(getLabel());
      }
      // Nice to allow user to select label or icon to kick-off action
      if (labelWidget != null) {
         labelWidget.addListener(SWT.MouseUp, new Listener() {
            @Override
            public void handleEvent(Event event) {
               notifyXModifiedListeners();
            }
         });
         labelWidget.setCursor(CursorManager.getCursor(SWT.CURSOR_HAND));
      }
      if (getToolTip() != null) {
         button.setToolTipText(getToolTip());
         if (labelWidget != null) {
            labelWidget.setToolTipText(getToolTip());
         }
      }
      button.setLayoutData(gd);
      updateCheckWidget();
      button.setEnabled(isEditable());
      button.setText(getLabel());
      if (image != null) {
         button.setImage(image);
      }
      button.setCursor(new Cursor(null, SWT.CURSOR_HAND));

   }

   @Override
   public void dispose() {
      labelWidget.dispose();
      button.dispose();
      bComp.dispose();
      if (parent != null && !parent.isDisposed()) {
         parent.layout();
      }
   }

   @Override
   public void setFocus() {
      return;
   }

   @Override
   public String getXmlData() {
      return "";
   }

   @Override
   public String getReportData() {
      return getXmlData();
   }

   @Override
   public void setXmlData(String set) {
      if (set.equals("true")) {
         set(true);
      } else {
         set(false);
      }
   }

   private void updateCheckWidget() {
      validate();
   }

   public void set(boolean selected) {
      this.selected = selected;
      updateCheckWidget();
   }

   @Override
   public void refresh() {
      updateCheckWidget();
   }

   @Override
   public IStatus isValid() {
      return Status.OK_STATUS;
   }

   @Override
   public String toHTML(String labelFont) {
      return AHTML.getLabelStr(labelFont, getLabel() + ": ") + selected;
   }

   /**
    * If set, label will be displayed after the button NOTE: Has to be set before call to createWidgets
    */
   public void setLabelAfter(boolean labelAfter) {
      this.labelAfter = labelAfter;
   }

   public Button getbutton() {
      return button;
   }

   public boolean isSelected() {
      return selected;
   }

   @Override
   public Object getData() {
      return Boolean.valueOf(isSelected());
   }

   public void setImage(Image image) {
      this.image = image;
   }

}