/*
 * Created on May 20, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.results;

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.results.html.XResultBrowserHyperCmd;
import org.eclipse.osee.framework.ui.skynet.results.html.XResultPage;
import org.eclipse.osee.framework.ui.skynet.results.html.XResultPage.Manipulations;

/**
 * @author Donald G. Dunne
 */
public class XResultDataUI {

   /*
    * Creates hyperlink using name. Default editor will open hrid for branchId given
    */
   public static String getHyperlink(String name, String hrid, int branchId) {
      return AHTML.getHyperlink(
         XResultBrowserHyperCmd.getHyperCmdStr(XResultBrowserHyperCmd.openArtifactBranch, hrid + "(" + branchId + ")"),
         name);
   }

   public static String getHyperlinkUrlExternal(String name, String url) {
      return AHTML.getHyperlink(XResultBrowserHyperCmd.getHyperCmdStr(XResultBrowserHyperCmd.browserExternal, url),
         name);
   }

   public static String getHyperlinkUrlInternal(String name, String url) {
      return AHTML.getHyperlink(XResultBrowserHyperCmd.getHyperCmdStr(XResultBrowserHyperCmd.browserInternal, url),
         name);
   }

   public static String getHyperlinkForArtifactEditor(String name, String hrid) {
      return AHTML.getHyperlink(XResultBrowserHyperCmd.getHyperCmdStr(XResultBrowserHyperCmd.openArtifactEditor, hrid),
         name);
   }

   public static String getHyperlinkForAction(String name, String hrid) {
      return AHTML.getHyperlink(XResultBrowserHyperCmd.getHyperCmdStr(XResultBrowserHyperCmd.openAction, hrid), name);
   }

   public static String getHyperlinkForAction(Artifact artifact) {
      return getHyperlinkForAction(artifact.getHumanReadableId(), artifact);
   }

   public static String getHyperlinkForAction(String name, Artifact art) {
      return AHTML.getHyperlink(
         XResultBrowserHyperCmd.getHyperCmdStr(XResultBrowserHyperCmd.openAction, art.getGuid()), name);
   }

   /*
    * Creates hyperlink using hrid as name. Default editor will open.
    */
   public static String getHyperlink(Artifact art) {
      return getHyperlink(art.getHumanReadableId(), art.getHumanReadableId(), art.getBranch().getId());
   }

   public static String getHyperlinkWithName(Artifact art) {
      return getHyperlink(art.toStringWithId(), art.getHumanReadableId(), art.getBranch().getId());
   }

   /*
    * Creates hyperlink using name. Default editor will open.
    */
   public static String getHyperlink(String name, Artifact art) {
      return getHyperlink(name, art.getHumanReadableId(), art.getBranch().getId());
   }

   public static void report(XResultData resultData, final String title) {
      report(resultData, title, Manipulations.ALL);
   }

   public static void report(XResultData resultData, final String title, final Manipulations... manipulations) {
      final String html = getReport(resultData, title, manipulations).getManipulatedHtml();
      ResultsEditor.open("Results", title, html);
   }

   public static XResultPage getReport(XResultData resultData, final String title) {
      return getReport(resultData, title, Manipulations.ALL);
   }

   public static XResultPage getReport(XResultData resultData, final String title, Manipulations... manipulations) {
      return new XResultPage(title + " - " + DateUtil.getMMDDYYHHMM(),
         (resultData.toString().equals("") ? "Nothing Logged" : resultData.toString()), manipulations);
   }

   public static void runExample() {
      runExample("This is my report title");
   }

   public static void runExample(String title) {
      try {
         XResultData rd = new XResultData();
         rd.log("This is just a normal log message");
         rd.logWarning("This is a warning");
         rd.logError("This is an error");

         rd.log("\n\nExample of hyperlinked hrid: " + getHyperlink(UserManager.getUser()));

         rd.log("Example of hyperlinked artifact different hyperlink string: " + getHyperlink("Different string",
            UserManager.getUser()));

         rd.log("Example of hyperlinked hrid on another branch: " + getHyperlink(
            UserManager.getUser().getHumanReadableId(), UserManager.getUser().getHumanReadableId(),
            BranchManager.getCommonBranch().getId()));
         rd.addRaw(AHTML.newline());
         rd.addRaw("Example of hyperlink that opens external browser " + getHyperlinkUrlExternal("Google",
            "http://www.google.com") + AHTML.newline());
         rd.addRaw("Example of hyperlink that opens internal browser " + getHyperlinkUrlInternal("Google",
            "http://www.google.com") + AHTML.newline());

         rd.log("\n\nHere is a nice table");
         rd.addRaw(AHTML.beginMultiColumnTable(95, 1));
         rd.addRaw(AHTML.addHeaderRowMultiColumnTable(new String[] {"Type", "Title", "Status"}));
         for (int x = 0; x < 3; x++) {
            rd.addRaw(AHTML.addRowMultiColumnTable(new String[] {"Type " + x, "Title " + x, x + ""}));
         }
         rd.addRaw(AHTML.addRowMultiColumnTable(new String[] {
            "Error / Warning in table ",
            "Error: this is error",
            "Warning: this is warning"}));
         rd.addRaw(AHTML.endMultiColumnTable());
         report(rd, "This is my report title");
      } catch (OseeCoreException ex) {
         OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

}