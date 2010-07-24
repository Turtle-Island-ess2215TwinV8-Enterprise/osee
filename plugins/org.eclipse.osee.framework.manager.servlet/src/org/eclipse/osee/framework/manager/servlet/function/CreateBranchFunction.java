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
import org.eclipse.osee.framework.branch.management.IOseeBranchService;
import org.eclipse.osee.framework.core.enums.CoreTranslatorId;
import org.eclipse.osee.framework.core.message.BranchCreationRequest;
import org.eclipse.osee.framework.core.message.BranchCreationResponse;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.LogProgressMonitor;
import org.eclipse.osee.framework.core.translation.IDataTranslationService;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.manager.servlet.internal.Activator;

/**
 * @author Jeff C. Phillips
 */
public class CreateBranchFunction extends AbstractOperation {

	private final HttpServletRequest req;
	private final HttpServletResponse resp;
	private final IOseeBranchService branchService;
	private final IDataTranslationService translationService;

	public CreateBranchFunction(HttpServletRequest req, HttpServletResponse resp, IOseeBranchService branchService, IDataTranslationService translationService) {
		super("Create Branch", Activator.PLUGIN_ID);
		this.req = req;
		this.resp = resp;
		this.branchService = branchService;
		this.translationService = translationService;
	}

	@Override
	protected void doWork(IProgressMonitor monitor) throws Exception {

		BranchCreationRequest creationRequest =
					translationService.convert(req.getInputStream(), CoreTranslatorId.BRANCH_CREATION_REQUEST);

		BranchCreationResponse creationResponse = new BranchCreationResponse(-1);
		IOperation subOp = branchService.createBranch(new LogProgressMonitor(), creationRequest, creationResponse);
		doSubWork(subOp, monitor, 0.80);

		resp.setStatus(HttpServletResponse.SC_ACCEPTED);
		resp.setContentType("text/xml");
		resp.setCharacterEncoding("UTF-8");
		InputStream inputStream =
					translationService.convertToStream(creationResponse, CoreTranslatorId.BRANCH_CREATION_RESPONSE);
		Lib.inputStreamToOutputStream(inputStream, resp.getOutputStream());
		monitor.worked(calculateWork(0.20));
	}
}