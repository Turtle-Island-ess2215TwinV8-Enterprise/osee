/*******************************************************************************
 * Copyright(c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.manager.servlet.function;

import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osee.framework.core.data.BranchCommitRequest;
import org.eclipse.osee.framework.core.data.BranchCommitResponse;
import org.eclipse.osee.framework.core.enums.CoreTranslatorId;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.services.IDataTranslationService;
import org.eclipse.osee.framework.core.services.IOseeBranchServiceProvider;
import org.eclipse.osee.framework.core.services.IOseeDataTranslationProvider;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.manager.servlet.internal.Activator;

/**
 * @author Megumi Telles
 */
public class CreateCommitFunction extends AbstractOperation {
   private final HttpServletRequest req;
   private final HttpServletResponse resp;
   private final IOseeBranchServiceProvider branchServiceProvider;
   private final IOseeDataTranslationProvider dataTranslatorProvider;

   public CreateCommitFunction(HttpServletRequest req, HttpServletResponse resp, IOseeBranchServiceProvider branchServiceProvider, IOseeDataTranslationProvider dataTranslatorProvider) {
      super("Commit Branch", Activator.PLUGIN_ID);
      this.req = req;
      this.resp = resp;
      this.branchServiceProvider = branchServiceProvider;
      this.dataTranslatorProvider = dataTranslatorProvider;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      IDataTranslationService service = dataTranslatorProvider.getTranslatorService();
      BranchCommitRequest data = service.convert(req.getInputStream(), CoreTranslatorId.BRANCH_COMMIT_REQUEST);

      BranchCommitResponse responseData = new BranchCommitResponse();
      branchServiceProvider.getBranchService().commitBranch(new NullProgressMonitor(), data, responseData);

      resp.setStatus(HttpServletResponse.SC_ACCEPTED);
      resp.setContentType("text/xml");
      resp.setCharacterEncoding("UTF-8");
      InputStream inputStream = service.convertToStream(responseData, CoreTranslatorId.BRANCH_COMMIT_RESPONSE);
      Lib.inputStreamToOutputStream(inputStream, resp.getOutputStream());
   }
}
