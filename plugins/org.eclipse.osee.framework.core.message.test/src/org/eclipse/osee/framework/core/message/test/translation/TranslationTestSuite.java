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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   BranchCommitRequestTranslatorTest.class,
   BranchCommitResponseTranslatorTest.class,
   BranchCreationRequestTranslatorTest.class,
   BranchCreationResponseTranslatorTest.class,
   CacheUpdateRequestTranslatorTest.class,
   ChangeReportResponseTranslatorTest.class,
   DatastoreInitRequestTranslatorTest.class,
   DataTranslationServiceFactoryTest.class,
   DataTranslationServiceTest.class,
   OseeImportModelRequestTranslatorTest.class,
   OseeImportModelResponseTranslatorTest.class,
   SearchRequestTranslatorTest.class,
   SearchResponseTranslatorTest.class,
   TableDataTranslatorTest.class,
   TransactionCacheUpdateResponseTranslatorTest.class,
   TransactionRecordTranslatorTest.class,
   PurgeBranchRequestTranslatorTest.class,
   BranchChangeTypeRequestTranslatorTest.class,
   BranchChangeStateRequestTranslatorTest.class,
   BranchChangeArchivedStateRequestTranslatorTest.class})
/**
 * @author Roberto E. Escobar
 */
public class TranslationTestSuite {
   // Test Suite
}
