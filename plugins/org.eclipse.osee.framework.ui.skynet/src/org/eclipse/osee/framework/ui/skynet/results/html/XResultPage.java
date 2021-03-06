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
package org.eclipse.osee.framework.ui.skynet.results.html;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.results.XResultDataUI;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.Dialogs;
import org.eclipse.swt.program.Program;

/**
 * @author Donald G. Dunne
 */
public class XResultPage {

   public static enum Manipulations {
      NONE, // 
      HRID_CMD_HYPER,
      // Replace all HRID strings with hyperlinks; ATS=<hrid> opens Action editor
      // ART=<hrid> opens Artifact editor, BOTH=<hrid> allows either
      ERROR_RED, // Make all "Error" strings red
      WARNING_YELLOW, // Make all "Warning" strings yellow
      CONVERT_NEWLINES, // Convert all \n to <br>
      HTML_MANIPULATIONS, // Do all except converting newlines
      RAW_HTML, // Just display in simple html page
      ERROR_WARNING_HEADER, // Shows Errors: 4 Warnings: 23 count at top of page
      ALL,
      ERROR_WARNING_FROM_SEARCH // Performs search for error and warning count instead of using logError and logWarning counts
   };
   public enum HyperType {
      ATS,
      ART,
      BOTH
   };

   private String title;
   private String html;
   private final String id; // Used to add and remove to menu item
   private String manipulatedHtml;
   private Set<Manipulations> manipulations = new HashSet<Manipulations>();
   private int numWarnings = Integer.MAX_VALUE;
   private int numErrors = Integer.MAX_VALUE;

   private final Pattern ATS_WPN_PAGE_HSRID_REGEX = Pattern.compile("([A-Z]{3,4})=(.*?):([A-Z0-9]{5})");
   private final Pattern ATS_HSRID_REGEX = Pattern.compile("([A-Z]{3,4})=([A-Z0-9]{5})");

   /**
    * Create and display result page with all Manipulations available
    */
   public XResultPage(String title, String text) {
      this(title, text, Manipulations.ALL);
   }

   /**
    * Create and display result page with given Manipulations
    * 
    * @param title title of the page (include date/time due or something unique due to multi-page view of results)
    * @param html html to display (minus manipulations). this html MUST already handle new lines (eg
    * text.replaceAll("\n",AHTML.newLine())) or use the CONVERT_NEWLINES manipultion
    * @param manipulations manipulations desired for the input HTML
    */
   public XResultPage(String title, String html, Manipulations... manipulations) {
      this.title = title;
      this.html = html;
      id = GUID.create();
      for (Manipulations man : manipulations) {
         switch (man) {
            case ALL:
               this.manipulations.add(Manipulations.HRID_CMD_HYPER);
               this.manipulations.add(Manipulations.ERROR_RED);
               this.manipulations.add(Manipulations.CONVERT_NEWLINES);
               this.manipulations.add(Manipulations.WARNING_YELLOW);
               break;
            case HTML_MANIPULATIONS:
               this.manipulations.add(Manipulations.HRID_CMD_HYPER);
               this.manipulations.add(Manipulations.ERROR_RED);
               this.manipulations.add(Manipulations.WARNING_YELLOW);
               break;
            default:
               this.manipulations.add(man);
               break;
         }
      }
   }

   public int getNumWarnings() {
      if (numWarnings == Integer.MAX_VALUE) {
         if (manipulations.contains(Manipulations.WARNING_YELLOW)) {
            numWarnings = Lib.numOccurances(html, "Warning:");
         }
         return 0;
      } else {
         return numWarnings;
      }
   }

   public int getNumErrors() {
      if (numErrors == Integer.MAX_VALUE) {
         if (manipulations.contains(Manipulations.WARNING_YELLOW)) {
            numErrors = Lib.numOccurances(html, "Error:");
         }
         return 0;
      } else {
         return numErrors;
      }
   }

   public static String getCmdValue(HyperType type, String hrid) {
      return String.format("%s=%s", type.name(), hrid);
   }

   /**
    * @param name that will show hyperlinked
    * @param hrid value that will be returned upon selection of hyperlink
    * @return cmd value to put in HTML for processing by result page
    */
   public static String getCmdValue(HyperType type, String name, String hrid) {
      return String.format("%s=%s:%s", type.name(), name, hrid);
   }

   public String getId() {
      return id;
   }

   public String getErrorWarningHtml() {
      int numErrors = getNumErrors();
      int numWarnings = getNumWarnings();
      return String.format("%s <b>Errors</b>: %d  <b>Warnings</b>: %d%s<br/><br/>",
         getErrorWarningColorPre(numErrors, numWarnings), numErrors, numWarnings,
         getErrorWarningColorPost(numErrors, numWarnings));
   }

   private String getErrorWarningColorPre(int numErrors, int numWarnings) {
      if (numErrors > 0) {
         return "<font color=\"red\">";
      } else if (numWarnings > 0) {
         return "<font color=\"yellow\">";
      }
      return "";
   }

   private String getErrorWarningColorPost(int numErrors, int numWarnings) {
      if (numErrors > 0 || numWarnings > 0) {
         return "</font>";
      }
      return "";
   }

   public String getManipulatedHtml() {
      return getManipulatedHtml(manipulations);
   }

   public String getManipulatedHtml(Collection<Manipulations> manipulations) {
      if (manipulatedHtml == null) {
         String str =
            (manipulations.contains(Manipulations.ERROR_WARNING_HEADER) ? getErrorWarningHtml() : "") + getText();
         if (manipulations.contains(Manipulations.RAW_HTML)) {
            str = AHTML.simplePage(str);
         } else {
            if (manipulations.contains(Manipulations.CONVERT_NEWLINES)) {
               str = str.replaceAll("\n", AHTML.newline());
            }
            if (manipulations.contains(Manipulations.HRID_CMD_HYPER)) {
               // System.err.println("match " + line);
               // Match getText so it doesn't mess up replace
               // Retireve all ATS=WPN_PAGE:HSRID matches
               Matcher m = ATS_WPN_PAGE_HSRID_REGEX.matcher(str);
               Set<String> cmdNameHrids = new HashSet<String>();
               while (m.find()) {
                  cmdNameHrids.add(m.group());
               }
               // Retrieve all ATS=Name:HRSID matches and replace with hyperlinking
               for (String cmdNameHrid : cmdNameHrids) {
                  String value = cmdNameHrid;
                  value = value.replaceAll("^.*?=", "");
                  String name = value;
                  name = name.replaceAll(":.*$", "");
                  String hrid = value;
                  hrid = hrid.replaceAll("^.*:", "");
                  if (cmdNameHrid.startsWith(HyperType.BOTH.name())) {
                     String replaceStr = hrid + " (" + XResultDataUI.getHyperlinkForAction("ATS-" + name, hrid);
                     replaceStr += "  " + XResultDataUI.getHyperlinkForArtifactEditor("AE-" + name, hrid);
                     replaceStr += ")";
                     str = str.replaceAll(cmdNameHrid, replaceStr);
                  } else if (cmdNameHrid.startsWith(HyperType.ATS.name())) {
                     str = str.replaceAll(cmdNameHrid, XResultDataUI.getHyperlinkForAction(name, hrid));
                  } else if (cmdNameHrid.startsWith(HyperType.ART.name())) {
                     str = str.replaceAll(cmdNameHrid, XResultDataUI.getHyperlinkForArtifactEditor(name, hrid));
                  }
               }
               // Retrieve all ATS=HRSID matches and replace with hyperlinking
               m = ATS_HSRID_REGEX.matcher(str);
               Set<String> cmdHrids = new HashSet<String>();
               while (m.find()) {
                  cmdHrids.add(m.group());
               }
               for (String cmdHrid : cmdHrids) {
                  String hrid = cmdHrid;
                  hrid = hrid.replaceAll("^.*?=", "");
                  if (cmdHrid.startsWith(HyperType.BOTH.name())) {
                     String replaceStr = hrid + " (" + XResultDataUI.getHyperlinkForAction("ATS", hrid);
                     replaceStr += "  " + XResultDataUI.getHyperlinkForArtifactEditor("AE", hrid);
                     replaceStr += ")";
                     str = str.replaceAll(cmdHrid, replaceStr);
                  } else if (cmdHrid.startsWith(HyperType.ATS.name())) {
                     str = str.replaceAll(cmdHrid, XResultDataUI.getHyperlinkForAction(hrid, hrid));
                  } else if (cmdHrid.startsWith(HyperType.ART.name())) {
                     str = str.replaceAll(cmdHrid, XResultDataUI.getHyperlinkForArtifactEditor(hrid, hrid));
                  }
               }
            }
            if (manipulations.contains(Manipulations.ERROR_RED)) {
               str = str.replaceAll("Error:", AHTML.color("red", "Error:"));
            }
            if (manipulations.contains(Manipulations.WARNING_YELLOW)) {
               str = str.replaceAll("Warning:", AHTML.color("orange", "Warning:"));
            }
         }
         manipulatedHtml = str;
      }
      return manipulatedHtml;
   }

   public String getText() {
      return html;
   }

   public void setHtml(String html) {
      this.html = html;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public Set<Manipulations> getManipulations() {
      return manipulations;
   }

   public void setManipulations(Set<Manipulations> manipulations) {
      this.manipulations = manipulations;
   }

   public void handleExport() {
      Dialogs.exportHtmlTableDialog(title, html, true);
   }

   public void saveToFile() {
      saveToFile(null);
   }

   public void saveToFile(String filename) {
      if (manipulatedHtml == null) {
         getManipulatedHtml();
      }
      if (filename == null) {
         Dialogs.saveHtmlDialog(manipulatedHtml, true);
      } else {
         try {
            Lib.writeStringToFile(manipulatedHtml, new File(filename));
         } catch (IOException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }
         Program.launch(filename);
      }
   }

   public void setNumErrors(int numErrors) {
      this.numErrors = numErrors;
   }

   public void setNumWarnings(int numWarnings) {
      this.numWarnings = numWarnings;
   }

}
