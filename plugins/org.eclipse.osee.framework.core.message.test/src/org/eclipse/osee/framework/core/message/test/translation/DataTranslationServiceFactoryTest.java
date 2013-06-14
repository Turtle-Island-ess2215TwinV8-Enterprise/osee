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
package org.eclipse.osee.framework.core.message.test.translation;

import org.eclipse.osee.framework.core.enums.CoreTranslatorId;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.message.internal.DataTranslationService;
import org.eclipse.osee.framework.core.message.internal.DataTranslationServiceFactory;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCacheStoreRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCacheUpdateResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCommitRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCommitResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCreationRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.BranchCreationResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.CacheUpdateRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.ChangeReportRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.ChangeReportResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.DatastoreInitRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.OseeImportModelRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.OseeImportModelResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.SearchRequestTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.SearchResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.TableDataTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.TransactionCacheUpdateResponseTranslator;
import org.eclipse.osee.framework.core.message.internal.translation.TransactionRecordTranslator;
import org.eclipse.osee.framework.core.services.IBranchCacheService;
import org.eclipse.osee.framework.core.services.IOseeModelFactoryService;
import org.eclipse.osee.framework.core.translation.IDataTranslationService;
import org.eclipse.osee.framework.core.translation.ITranslator;
import org.eclipse.osee.framework.core.translation.ITranslatorId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test Case for {@link DataTranslationServiceFactory}
 * 
 * @author Roberto E. Escobar
 */
public class DataTranslationServiceFactoryTest {

   @Mock
   private IBranchCacheService branchService;
   @Mock
   private IOseeModelFactoryService modelService;

   @Before
   public void setUp() {
      MockitoAnnotations.initMocks(this);
   }

   @Test
   public void testServiceCreation() throws OseeCoreException {
      DataTranslationService srvc = new DataTranslationService();
      srvc.setModelFactory(modelService);
      srvc.setBranchCacheService(branchService);
      srvc.start();

      checkExists(srvc, TransactionRecordTranslator.class, CoreTranslatorId.TRANSACTION_RECORD);

      checkExists(srvc, BranchCreationRequestTranslator.class, CoreTranslatorId.BRANCH_CREATION_REQUEST);
      checkExists(srvc, BranchCreationResponseTranslator.class, CoreTranslatorId.BRANCH_CREATION_RESPONSE);

      checkExists(srvc, BranchCommitRequestTranslator.class, CoreTranslatorId.BRANCH_COMMIT_REQUEST);
      checkExists(srvc, BranchCommitResponseTranslator.class, CoreTranslatorId.BRANCH_COMMIT_RESPONSE);

      checkExists(srvc, ChangeReportRequestTranslator.class, CoreTranslatorId.CHANGE_REPORT_REQUEST);
      checkExists(srvc, ChangeReportResponseTranslator.class, CoreTranslatorId.CHANGE_REPORT_RESPONSE);

      checkExists(srvc, CacheUpdateRequestTranslator.class, CoreTranslatorId.OSEE_CACHE_UPDATE_REQUEST);
      checkExists(srvc, BranchCacheUpdateResponseTranslator.class, CoreTranslatorId.BRANCH_CACHE_UPDATE_RESPONSE);
      checkExists(srvc, BranchCacheStoreRequestTranslator.class, CoreTranslatorId.BRANCH_CACHE_STORE_REQUEST);
      checkExists(srvc, TransactionCacheUpdateResponseTranslator.class, CoreTranslatorId.TX_CACHE_UPDATE_RESPONSE);

      checkExists(srvc, OseeImportModelRequestTranslator.class, CoreTranslatorId.OSEE_IMPORT_MODEL_REQUEST);
      checkExists(srvc, OseeImportModelResponseTranslator.class, CoreTranslatorId.OSEE_IMPORT_MODEL_RESPONSE);
      checkExists(srvc, TableDataTranslator.class, CoreTranslatorId.TABLE_DATA);

      checkExists(srvc, DatastoreInitRequestTranslator.class, CoreTranslatorId.OSEE_DATASTORE_INIT_REQUEST);

      checkExists(srvc, SearchRequestTranslator.class, CoreTranslatorId.SEARCH_REQUEST);
      checkExists(srvc, SearchResponseTranslator.class, CoreTranslatorId.SEARCH_RESPONSE);

      srvc.stop();
   }

   private void checkExists(IDataTranslationService service, Class<? extends ITranslator<?>> expected, ITranslatorId key) throws OseeCoreException {
      ITranslator<?> actual = service.getTranslator(key);
      Assert.assertNotNull(actual);
      Assert.assertEquals(expected, actual.getClass());
   }

}
