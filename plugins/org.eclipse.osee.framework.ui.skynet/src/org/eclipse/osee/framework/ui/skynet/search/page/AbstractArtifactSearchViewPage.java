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
package org.eclipse.osee.framework.ui.skynet.search.page;

import org.eclipse.osee.framework.ui.skynet.search.AbstractArtifactSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractArtifactSearchViewPage extends AbstractTextSearchViewPage {

   @Override
   public AbstractArtifactSearchResult getInput() {
      return (AbstractArtifactSearchResult) super.getInput();
   }

}
