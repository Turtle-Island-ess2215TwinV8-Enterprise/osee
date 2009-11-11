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
package org.eclipse.osee.framework.manager.servlet;

import java.io.IOException;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.osee.framework.core.data.Function;
import org.eclipse.osee.framework.core.server.OseeHttpServlet;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.manager.servlet.data.HttpBranchCommitInfo;
import org.eclipse.osee.framework.manager.servlet.data.HttpBranchCreationInfo;

/**
 * @author Andrew M Finkbeiner
 */
public class BranchManagerServlet extends OseeHttpServlet {

   private static final long serialVersionUID = 226986283540461526L;

   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      try {
         Function function;
         resp.setStatus(HttpServletResponse.SC_ACCEPTED);
         resp.setContentType("text/plain");

         function = Function.fromString(req.getParameter("function"));
         switch (function) {
            case BRANCHCOMMIT:
               HttpBranchCommitInfo commitInfo = new HttpBranchCommitInfo(req);
               MasterServletActivator.getInstance().getBranchCommit().commitBranch(null, commitInfo.getUser(),
                     commitInfo.getSourceBranch(), commitInfo.getDestinationBranch(),
                     commitInfo.isArchiveSourceBranch());
               break;
            case CHANGEREPORT:
               break;
            case CREATEFULLBRANCH:
               HttpBranchCreationInfo info = new HttpBranchCreationInfo(req);
               int branchId = -1;
               branchId =
                     MasterServletActivator.getInstance().getBranchCreation().createBranch(info.getBranch(),
                           info.getAuthorId(), info.getCreationComment(),
                           info.getPopulateBaseTxFromAddressingQueryId(), info.getDestinationBranchId());
               if (branchId == -1) {
                  resp.getWriter().write("Unknown Error during branch creation.");
               } else {
                  resp.getWriter().write(Integer.toString(branchId));
               }
         }

      } catch (Exception ex) {
         OseeLog.log(MasterServletActivator.class, Level.SEVERE, String.format(
               "Failed to respond to a branch servlet request [%s]", req.toString()), ex);
         resp.setContentType("text/plain");
         resp.getWriter().write(Lib.exceptionToString(ex));
      }
      resp.getWriter().flush();
      resp.getWriter().close();
   }
}
