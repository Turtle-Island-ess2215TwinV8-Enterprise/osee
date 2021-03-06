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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.framework.core.message.PurgeBranchRequest;
import org.eclipse.osee.framework.core.message.internal.translation.PurgeBranchRequestTranslator;
import org.eclipse.osee.framework.core.message.test.mocks.DataAsserts;
import org.eclipse.osee.framework.core.translation.ITranslator;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test Case for {@link PurgeBranchRequestTranslator}
 * 
 * @author Megumi Telles
 * @author Jeff C. Phillips
 */
@RunWith(Parameterized.class)
public class PurgeBranchRequestTranslatorTest extends BaseTranslatorTest<PurgeBranchRequest> {

   public PurgeBranchRequestTranslatorTest(PurgeBranchRequest data, ITranslator<PurgeBranchRequest> translator) {
      super(data, translator);
   }

   @Override
   protected void checkEquals(PurgeBranchRequest expected, PurgeBranchRequest actual) {
      DataAsserts.assertEquals(expected, actual);
   }

   @Parameters
   public static Collection<Object[]> data() {
      List<Object[]> data = new ArrayList<Object[]>();
      ITranslator<PurgeBranchRequest> translator = new PurgeBranchRequestTranslator();

      for (int index = 1; index <= 2; index++) {
         data.add(new Object[] {new PurgeBranchRequest(index * 3, false), translator});
      }
      return data;
   }
}
