/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.integration.tests.ats.actions;

import org.eclipse.osee.ats.actions.NewGoal;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.enums.DeletionFlag;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.transaction.TransactionManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author Donald G. Dunne
 */
public class NewGoalTest extends AbstractAtsActionRunTest {

   @BeforeClass
   @AfterClass
   public static void cleanup() throws OseeCoreException {
      SkynetTransaction transaction =
         TransactionManager.createTransaction(AtsUtil.getAtsBranch(), NewGoalTest.class.getSimpleName());
      for (Artifact art : ArtifactQuery.getArtifactListFromName(NewGoalTest.class.getSimpleName(),
         AtsUtil.getAtsBranchToken(), DeletionFlag.EXCLUDE_DELETED)) {
         art.deleteAndPersist(transaction);
      }
      transaction.execute();
   }

   @Override
   public NewGoal createAction() {
      NewGoal action = new NewGoal();
      action.setTitleOverride(getClass().getSimpleName());
      return action;
   }

}
