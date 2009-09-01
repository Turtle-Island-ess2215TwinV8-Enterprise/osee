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
package org.eclipse.osee.framework.branch.management.servlet;

import javax.servlet.http.HttpServletRequest;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;

/**
 * @author Andrew M Finkbeiner
 */
class HttpBranchCreationInfo {
   private int parentBranchId;
   private int parentTransactionId;
   private final String branchName;
   private final String creationComment;
   private final int associatedArtifactId;
   private final int authorId;
   private final String staticBranchName;
   private final BranchType branchType;
   private final String branchGuid;

   public HttpBranchCreationInfo(HttpServletRequest req) throws OseeArgumentException {
      branchGuid = req.getParameter("branchGuid");
      String parentBranchIdStr = req.getParameter("parentBranchId");
      if (parentBranchIdStr == null) {
         throw new OseeArgumentException("A 'parentBranchId' parameter must be specified");
      } else {
         parentBranchId = Integer.parseInt(parentBranchIdStr);
      }

      String branchTypeStr = req.getParameter("branchType");
      if (branchTypeStr == null) {
         throw new OseeArgumentException("A 'branchTypeStr' parameter must be specified");
      }
      branchType = BranchType.valueOf(branchTypeStr);

      String parentTransactionIdStr = req.getParameter("parentTransactionId");
      if (parentTransactionIdStr == null) {
         throw new OseeArgumentException("A 'parentTransactionId' parameter must be specified");
      } else {
         parentTransactionId = Integer.parseInt(parentTransactionIdStr);
      }

      branchName = req.getParameter("branchName");//required
      if (branchName == null || branchName.length() == 0) {
         throw new OseeArgumentException("A 'branchName' parameter must be specified");
      }
      creationComment = req.getParameter("creationComment");//required
      if (creationComment == null || creationComment.length() == 0) {
         throw new OseeArgumentException("A 'creationComment' parameter must be specified");
      }
      String associatedArtifactIdStr = req.getParameter("associatedArtifactId");
      if (associatedArtifactIdStr == null) {
         throw new OseeArgumentException("A 'associatedArtifactId' parameter must be specified");
      }
      associatedArtifactId = Integer.parseInt(associatedArtifactIdStr);
      String authorIdStr = req.getParameter("authorId");
      if (authorIdStr == null) {
         throw new OseeArgumentException("A 'authorIdStr' parameter must be specified");
      }
      authorId = Integer.parseInt(authorIdStr);
      staticBranchName = req.getParameter("staticBranchName");
   }

   /**
    * @return the parentBranchId
    */
   public int getParentBranchId() {
      return parentBranchId;
   }

   /**
    * @return the branchName
    */
   public String getBranchName() {
      return branchName;
   }

   /**
    * @return the creationComment
    */
   public String getCreationComment() {
      return creationComment;
   }

   /**
    * @return the associatedArtifactId
    */
   public int getAssociatedArtifactId() {
      return associatedArtifactId;
   }

   /**
    * @return the authorId
    */
   public int getAuthorId() {
      return authorId;
   }

   /**
    * @return the staticBranchName
    */
   public String getStaticBranchName() {
      return staticBranchName;
   }

   /**
    * @return the systemRootBranch
    */
   public BranchType getBranchType() {
      return branchType;
   }

   /**
    * @return the branchGuid
    */
   public String getBranchGuid() {
      return branchGuid;
   }

   /**
    * @return the parentTransactionId
    */
   public int getParentTransactionId() {
      return parentTransactionId;
   }
}