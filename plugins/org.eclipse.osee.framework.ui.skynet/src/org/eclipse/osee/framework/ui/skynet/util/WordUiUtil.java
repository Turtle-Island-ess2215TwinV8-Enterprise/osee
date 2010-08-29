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
package org.eclipse.osee.framework.ui.skynet.util;

import java.util.Collection;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.results.XResultData;
import org.eclipse.osee.framework.ui.swt.Displays;

/**
 * @author Megumi Telles
 */
public final class WordUiUtil {

   public static void displayErrorMessageDialog(final String title, final String message) {
      Displays.pendInDisplayThread(new Runnable() {
         @Override
         public void run() {
            MessageDialog.openError(Displays.getActiveShell(), title, message);
         }
      });
   }

   public static void displayWarningMessageDialog(final String title, final String message) {
      Displays.pendInDisplayThread(new Runnable() {
         @Override
         public void run() {
            MessageDialog.openWarning(Displays.getActiveShell(), title, message);
         }
      });
   }

   public static void displayTrackedChangesOnArtifacts(final Collection<Artifact> artifacts) {
      if (!artifacts.isEmpty()) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               XResultData rd = new XResultData();
               rd.logWarning("\nYou chose to diff changes and the following Artifacts were detected to have tracked changes on.");
               rd.log("Please make sure to accept/reject all tracked changes and comment references.\n");
               rd.addRaw(AHTML.beginMultiColumnTable(60, 1));
               rd.addRaw(AHTML.addHeaderRowMultiColumnTable(new String[] {"Artifact Name", "HRID"}));
               for (Artifact artifact : artifacts) {
                  rd.addRaw(AHTML.addRowMultiColumnTable(new String[] {
                     artifact.toString(),
                     XResultData.getHyperlink(artifact)}));
               }
               rd.addRaw(AHTML.endMultiColumnTable());
               rd.report("Artifacts With Tracked Changes");
            }
         });
      }
   }

   public static void displayUnhandledArtifacts(final Collection<Artifact> artifacts, final String warningString) {
      if (!artifacts.isEmpty()) {
         Displays.ensureInDisplayThread(new Runnable() {
            @Override
            public void run() {
               XResultData rd = new XResultData();
               rd.logWarning("\nYou chose to preview/edit artifacts that could not be handled: ");
               rd.log(warningString + "\n");
               rd.addRaw(AHTML.beginMultiColumnTable(60, 1));
               rd.addRaw(AHTML.addHeaderRowMultiColumnTable(new String[] {"Artifact Name", "HRID"}));
               for (Artifact artifact : artifacts) {
                  rd.addRaw(AHTML.addRowMultiColumnTable(new String[] {
                     artifact.toString(),
                     XResultData.getHyperlink(artifact)}));
               }
               rd.addRaw(AHTML.endMultiColumnTable());
               rd.report("Unhandled Artifacts");
            }
         });
      }
   }

}
