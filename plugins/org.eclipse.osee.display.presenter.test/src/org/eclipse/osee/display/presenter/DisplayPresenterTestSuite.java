/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.display.presenter;

import org.eclipse.osee.display.presenter.internal.ArtifactPresenterTest;
import org.eclipse.osee.display.presenter.internal.AsyncSearchHandlerTest;
import org.eclipse.osee.display.presenter.internal.SearchResultConverterTest;
import org.eclipse.osee.display.presenter.internal.SearchResultsPresenterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   ArtifactFilterTest.class,
   ArtifactPresenterTest.class,
   AsyncSearchHandlerTest.class,
   SearchResultConverterTest.class,
   SearchResultsPresenterTest.class,
   UtilityTest.class})
/**
 * @author John Misinco
 */
public class DisplayPresenterTestSuite {
   //Test Suite class
}
