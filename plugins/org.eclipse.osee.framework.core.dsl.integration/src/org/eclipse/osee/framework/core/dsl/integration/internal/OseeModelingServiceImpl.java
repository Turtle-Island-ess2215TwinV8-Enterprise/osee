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
package org.eclipse.osee.framework.core.dsl.integration.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.compare.diff.metamodel.ComparisonResourceSnapshot;
import org.eclipse.emf.compare.diff.metamodel.DiffFactory;
import org.eclipse.osee.framework.core.dsl.integration.CreateOseeTypeChangesReportOperation;
import org.eclipse.osee.framework.core.dsl.integration.EMFCompareOperation;
import org.eclipse.osee.framework.core.dsl.integration.ModelUtil;
import org.eclipse.osee.framework.core.dsl.integration.OseeToXtextOperation;
import org.eclipse.osee.framework.core.dsl.integration.OseeTypeCache;
import org.eclipse.osee.framework.core.dsl.integration.XTextToOseeTypeOperation;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDsl;
import org.eclipse.osee.framework.core.dsl.oseeDsl.OseeDslFactory;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeWrappedException;
import org.eclipse.osee.framework.core.message.IOseeModelingService;
import org.eclipse.osee.framework.core.message.OseeImportModelRequest;
import org.eclipse.osee.framework.core.message.OseeImportModelResponse;
import org.eclipse.osee.framework.core.message.TableData;
import org.eclipse.osee.framework.core.operation.CompositeOperation;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.Operations;
import org.eclipse.osee.framework.core.services.IOseeCachingService;
import org.eclipse.osee.framework.core.services.IOseeCachingServiceFactory;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryService;
import org.eclipse.osee.framework.jdk.core.util.Lib;

/**
 * @author Roberto E. Escobar
 */
public class OseeModelingServiceImpl implements IOseeModelingService {

	private final IOseeModelFactoryService modelFactoryService;
	private final IOseeCachingService systemCachingService;
	private final IOseeCachingServiceFactory cachingFactoryService;
	private final OseeDslFactory modelFactory;

	public OseeModelingServiceImpl(IOseeModelFactoryService modelFactoryService, IOseeCachingService systemCachingService, IOseeCachingServiceFactory cachingFactoryService, OseeDslFactory dslFactory) {
		this.modelFactoryService = modelFactoryService;
		this.systemCachingService = systemCachingService;
		this.cachingFactoryService = cachingFactoryService;
		this.modelFactory = dslFactory;

	}

	@Override
	public void exportOseeTypes(IProgressMonitor monitor, OutputStream outputStream) throws OseeCoreException {
		OseeTypeCache cache =
					new OseeTypeCache(systemCachingService.getArtifactTypeCache(),
								systemCachingService.getAttributeTypeCache(), systemCachingService.getRelationTypeCache(),
								systemCachingService.getEnumTypeCache());

		OseeDsl model = modelFactory.createOseeDsl();

		IOperation operation = new OseeToXtextOperation(cache, modelFactory, model);
		Operations.executeWorkAndCheckStatus(operation, monitor, -1);
		try {
			ModelUtil.saveModel(model, "osee:/oseeTypes_" + Lib.getDateTimeString() + ".osee", outputStream, false);
		} catch (IOException ex) {
			throw new OseeWrappedException(ex);
		}
	}

	@Override
	public void importOseeTypes(IProgressMonitor monitor, boolean isInitializing, OseeImportModelRequest request, OseeImportModelResponse response) throws OseeCoreException {
		String modelName = request.getModelName();
		if (!modelName.endsWith(".osee")) {
			modelName += ".osee";
		}
		OseeDsl inputModel = ModelUtil.loadModel("osee:/" + modelName, request.getModel());

		IOseeCachingService tempCacheService = cachingFactoryService.createCachingService();
		OseeTypeCache tempCache =
					new OseeTypeCache(tempCacheService.getArtifactTypeCache(), tempCacheService.getAttributeTypeCache(),
								tempCacheService.getRelationTypeCache(), tempCacheService.getEnumTypeCache());

		List<TableData> reportData = new ArrayList<TableData>();
		ComparisonResourceSnapshot comparisonSnapshot = DiffFactory.eINSTANCE.createComparisonResourceSnapshot();
		OseeDsl baseModel = modelFactory.createOseeDsl();
		OseeDsl modifiedModel = modelFactory.createOseeDsl();

		List<IOperation> ops = new ArrayList<IOperation>();

		if (request.isCreateCompareReport()) {
			ops.add(new OseeToXtextOperation(tempCache, modelFactory, baseModel));
		}

		ops.add(new XTextToOseeTypeOperation(modelFactoryService, tempCache, tempCacheService.getBranchCache(),
					inputModel));
		if (request.isCreateTypeChangeReport()) {
			ops.add(new CreateOseeTypeChangesReportOperation(tempCache, reportData));
		}
		if (request.isCreateCompareReport()) {
			ops.add(new OseeToXtextOperation(tempCache, modelFactory, baseModel));
			ops.add(new EMFCompareOperation(baseModel, modifiedModel, comparisonSnapshot));
		}
		IOperation operation = new CompositeOperation("Import Osee Types", Activator.PLUGIN_ID, ops);
		Operations.executeWorkAndCheckStatus(operation, monitor, -1);

		if (request.isPersistAllowed()) {
			// TODO Make this call transaction based
			tempCache.storeAllModified();
			response.setPersisted(true);
			if (isInitializing) {
				systemCachingService.clearAll();
			}
			systemCachingService.reloadAll();
		} else {
			response.setPersisted(false);
		}
		response.setReportData(reportData);

		if (request.isCreateCompareReport()) {
			response.setComparisonSnapshotModelName("osee_compare.diff");
			String modelString =
						ModelUtil.modelToString(comparisonSnapshot, "osee:/osee_compare.diff",
									Collections.<String, Boolean> emptyMap());
			response.setComparisonSnapshotModel(modelString);
		}
	}
	//      Map<String, OseeTypeModel> changedModels = new HashMap<String, OseeTypeModel>();
	//      doSubWork(new OseeToXtextOperation(modifiedCache, changedModels), monitor, 0.20);
	//
	//      OseeTypeCache storeCache = createEmptyCache();
	//      storeCache.ensurePopulated();
	//      Map<String, OseeTypeModel> baseModels = new HashMap<String, OseeTypeModel>();
	//      doSubWork(new OseeToXtextOperation(storeCache, baseModels), monitor, 0.20);
	//
	//      OseeTypeModel changedModel = null;
	//      OseeTypeModel baseModel = null;
	//      for (String key : changedModels.keySet()) {
	//         changedModel = changedModels.get(key);
	//         baseModel = baseModels.get(key);
	//      }
	//   }

}