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

package org.eclipse.osee.framework.ui.skynet.widgets.dialog;

import java.io.File;
import java.io.IOException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class HtmlDialog extends MessageDialog {
   protected Browser b;
   private LocationListener listener;
   private final String html;

   public HtmlDialog(String title, String message, String html) {
      super(Display.getCurrent().getActiveShell(), title, null, message, SWT.NONE, new String[] {"OK", "Cancel"}, 0);
      this.html = html;
   }

   /**
    * Add listener to browser widget.
    * 
    * @param listener
    */
   public void addLocationListener(LocationListener listener) {
      this.listener = listener;
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite c = (Composite) super.createDialogArea(parent);
      b = new Browser(c, SWT.BORDER);
      GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
      b.setLayoutData(gd);
      b.setText(html);
      b.setSize(500, 500);
      if (listener != null) b.addLocationListener(listener);
      b.setMenu(pageOverviewGetPopup());

      return c;
   }

   public Menu pageOverviewGetPopup() {
      Menu menu = new Menu(b.getShell());
      MenuItem item = new MenuItem(menu, SWT.NONE);
      item.setText("View Source");
      item.addSelectionListener(new SelectionAdapter() {

         public void widgetSelected(SelectionEvent e) {
            String file = System.getProperty("user.home") + File.separator + "out.html";
            try {
               Lib.writeStringToFile(html, new File(file));
            } catch (IOException ex) {
               OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
            Program.launch(file);
         }
      });
      return menu;
   }

}
