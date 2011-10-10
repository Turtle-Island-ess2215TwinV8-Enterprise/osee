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
package org.eclipse.osee.orcs.core.internal.attribute;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.logger.Log;
import org.eclipse.osee.orcs.core.ds.AttributeContainer;
import org.eclipse.osee.orcs.core.ds.AttributeRow;
import org.eclipse.osee.orcs.core.mocks.MockLog;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test Case for {@link AttributeRowMapper}
 * 
 * @author Roberto E. Escobar
 */
public class AttributeRowMapperTest {

   @Test
   @Ignore
   public void test() throws OseeCoreException {
      Log logger = new MockLog();
      AttributeFactory factory = null;
      AttributeRow row = new AttributeRow();
      Map<Integer, ? extends AttributeContainer> attributeContainers = new HashMap<Integer, AttributeContainer>();
      AttributeRowMapper mapper = new AttributeRowMapper(logger, factory, attributeContainers);
      mapper.onRow(row);
   }
}