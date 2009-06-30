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

package org.eclipse.nebula.widgets.xviewer.customize;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.util.internal.XViewerLib;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Provides UI for displaying/entering search string
 * 
 * @author Andrew M. Finkbeiner
 */
public class SearchDataUI {

   private Text searchText;
   private Label searchLabel;
   private final XViewer xViewer;
   private Matcher match;
   private boolean search = false;
   private Button regularExpression;
   private boolean regex;
   private final boolean searchRealTime;

   public SearchDataUI(XViewer xViewer, boolean searchRealTime) {
      this.xViewer = xViewer;
      this.searchRealTime = searchRealTime;
   }

   public void createWidgets(Composite bar) {

      //	  ExpandBar bar = new ExpandBar(comp, SWT.V_SCROLL);

      Label label = new Label(bar, SWT.NONE);
      label.setText("Search:");
      label.setToolTipText("Type string and press enter to filter.\nClear field to un-filter.");
      GridData gd = new GridData(SWT.RIGHT, SWT.NONE, false, false);
      label.setLayoutData(gd);

      searchText = new Text(bar, SWT.SINGLE | SWT.BORDER);

      gd = new GridData(SWT.RIGHT, SWT.NONE, false, false);
      gd.widthHint = 100;
      searchText.setLayoutData(gd);

      searchText.addKeyListener(new KeyListener() {
         /*
          * (non-Javadoc)
          * 
          * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
          */
         public void keyPressed(KeyEvent e) {
         }

         /*
          * (non-Javadoc)
          * 
          * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
          */
         public void keyReleased(KeyEvent e) {
            // System.out.println(e.keyCode);
            if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR || searchRealTime) {
               //               xViewer.getCustomizeMgr().setSearchText(searchText.getText());
               String newText = searchText.getText();
               if (newText.trim().length() == 0) {
                  search = false;
                  match = Pattern.compile(searchText.getText()).matcher("");
               } else {
                  regex = true;
                  if (!regularExpression.getSelection()) {
                     regex = false;
                     newText = newText.replace("*", ".*");
                     newText = ".*" + newText + ".*";
                  }
                  match =
                        Pattern.compile(newText, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE).matcher(
                              "");
                  search = true;
               }
               xViewer.refresh();
            }
         }
      });

      searchLabel = new Label(bar, SWT.NONE);
      searchLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));
      searchLabel.setImage(XViewerLib.getImage("clear.gif"));

      regularExpression = new Button(bar, SWT.CHECK);
      regularExpression.setText("RE");
      regularExpression.setToolTipText("Enable Regular Expression Search");
      regularExpression.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));
      searchLabel.addListener(SWT.MouseUp, new Listener() {
         /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
             */
         public void handleEvent(Event event) {
            searchText.setText("");
            search = false;
            match = Pattern.compile(searchText.getText()).matcher("");
            xViewer.refresh();
         }
      });
   }

   public void dispose() {
   }

   public void clear() {
      searchText.setText("");
      xViewer.getCustomizeMgr().setFilterText("");
   }

   public void getStatusLabelAddition(StringBuffer sb) {
      if (searchText != null && !searchText.getText().equals("")) {
         sb.append("[Text Search]");
      }
   }

   /**
    * @param text
    * @return true if matched
    */
   public boolean match(String textString) {
      if (search) {
         if (regex) {
            match.reset(textString);
            return match.matches();
         } else {
            match.reset(textString);
            return match.matches();
         }
      } else {
         return false;
      }
   }

   /**
    * @return true if in search mode
    */
   public boolean isSearch() {
      return search;
   }

}
