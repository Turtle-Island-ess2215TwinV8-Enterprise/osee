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
package org.eclipse.osee.framework.core.message.internal;

import org.eclipse.osee.framework.core.enums.CoreTranslatorId;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.message.internal.translation.ArtifactTypeCacheUpdateResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.AttributeTypeCacheUpdateResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCacheStoreRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCacheUpdateResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchChangeArchivedStateRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchChangeStateRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchChangeTypeRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCommitRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCommitResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCreationRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCreationResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.CacheUpdateRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.ChangeItemTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.ChangeReportRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.ChangeReportResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.ChangeVersionTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.OseeEnumTypeCacheUpdateResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.OseeImportModelRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.OseeImportModelResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.PurgeBranchRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.RelationTypeCacheUpdateResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.TableDataTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.TransactionCacheUpdateResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.TransactionRecordTranslator;
import org.eclipse.osee.framework.core.services.IOseeCachingServiceProvider;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryServiceProvider;
import org.eclipse.osee.framework.core.translation.IDataTranslationService;

/**
 * @author Roberto E. Escobar
 * @author Jeff C. Phillips
 */
public class DataTranslationServiceFactory {

   public DataTranslationServiceFactory() {
   }

   public IDataTranslationService createService(IOseeCachingServiceProvider cachingService, IOseeModelFactoryServiceProvider factoryProvider) throws OseeCoreException {
      IDataTranslationService service = new DataTranslationService();

      // service.addTranslator(new BasicArtifactTranslator(),
      // CoreTranslatorId.ARTIFACT_METADATA);
      // service.addTranslator(new BranchTranslator(service, factoryProvider),
      // CoreTranslatorId.BRANCH);

      // service.addTranslator(new ArtifactTypeTranslator(service,
      // factoryProvider), CoreTranslatorId.ARTIFACT_TYPE);
      // service.addTranslator(new AttributeTypeTranslator(service,
      // factoryProvider), CoreTranslatorId.ATTRIBUTE_TYPE);
      // service.addTranslator(new RelationTypeTranslator(service,
      // factoryProvider), CoreTranslatorId.RELATION_TYPE);
      // service.addTranslator(new OseeEnumEntryTranslator(factoryProvider),
      // CoreTranslatorId.OSEE_ENUM_ENTRY);
      // service.addTranslator(new OseeEnumTypeTranslator(service,
      // factoryProvider), CoreTranslatorId.OSEE_ENUM_TYPE);

      service.addTranslator(new TransactionRecordTranslator(factoryProvider), CoreTranslatorId.TRANSACTION_RECORD);

      service.addTranslator(new BranchCreationRequestTranslator(), CoreTranslatorId.BRANCH_CREATION_REQUEST);
      service.addTranslator(new BranchCreationResponseTranslator(), CoreTranslatorId.BRANCH_CREATION_RESPONSE);

      service.addTranslator(new BranchCommitRequestTranslator(), CoreTranslatorId.BRANCH_COMMIT_REQUEST);
      service.addTranslator(new BranchCommitResponseTranslator(service), CoreTranslatorId.BRANCH_COMMIT_RESPONSE);

      service.addTranslator(new BranchChangeTypeRequestTranslator(), CoreTranslatorId.CHANGE_BRANCH_TYPE);
      service.addTranslator(new BranchChangeStateRequestTranslator(), CoreTranslatorId.CHANGE_BRANCH_STATE);
      service.addTranslator(new BranchChangeArchivedStateRequestTranslator(),
            CoreTranslatorId.CHANGE_BRANCH_ARCHIVE_STATE);

      service.addTranslator(new ChangeVersionTranslator(), CoreTranslatorId.CHANGE_VERSION);
      service.addTranslator(new ChangeItemTranslator(service), CoreTranslatorId.CHANGE_ITEM);
      service.addTranslator(new ChangeReportRequestTranslator(), CoreTranslatorId.CHANGE_REPORT_REQUEST);
      service.addTranslator(new ChangeReportResponseTranslator(service), CoreTranslatorId.CHANGE_REPORT_RESPONSE);
      service.addTranslator(new PurgeBranchRequestTranslator(), CoreTranslatorId.PURGE_BRANCH_REQUEST);

      service.addTranslator(new CacheUpdateRequestTranslator(), CoreTranslatorId.OSEE_CACHE_UPDATE_REQUEST);

      service.addTranslator(new BranchCacheUpdateResponseTranslator(), CoreTranslatorId.BRANCH_CACHE_UPDATE_RESPONSE);
      service.addTranslator(new BranchCacheStoreRequestTranslator(), CoreTranslatorId.BRANCH_CACHE_STORE_REQUEST);
      service.addTranslator(new TransactionCacheUpdateResponseTranslator(factoryProvider),
            CoreTranslatorId.TX_CACHE_UPDATE_RESPONSE);

      service.addTranslator(new ArtifactTypeCacheUpdateResponseTranslator(),
            CoreTranslatorId.ARTIFACT_TYPE_CACHE_UPDATE_RESPONSE);

      service.addTranslator(new AttributeTypeCacheUpdateResponseTranslator(factoryProvider),
            CoreTranslatorId.ATTRIBUTE_TYPE_CACHE_UPDATE_RESPONSE);

      service.addTranslator(new RelationTypeCacheUpdateResponseTranslator(),
            CoreTranslatorId.RELATION_TYPE_CACHE_UPDATE_RESPONSE);

      service.addTranslator(new OseeEnumTypeCacheUpdateResponseTranslator(),
            CoreTranslatorId.OSEE_ENUM_TYPE_CACHE_UPDATE_RESPONSE);

      service.addTranslator(new OseeImportModelRequestTranslator(), CoreTranslatorId.OSEE_IMPORT_MODEL_REQUEST);
      service.addTranslator(new OseeImportModelResponseTranslator(service), CoreTranslatorId.OSEE_IMPORT_MODEL_RESPONSE);
      service.addTranslator(new TableDataTranslator(), CoreTranslatorId.TABLE_DATA);
      return service;
   }
}