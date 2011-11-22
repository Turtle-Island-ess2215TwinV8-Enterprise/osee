/*
 * Created on Oct 24, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.actions;

import org.eclipse.osee.ats.core.AtsTestUtil;
import org.eclipse.osee.ats.core.util.AtsUtilCore;
import org.eclipse.osee.ats.shared.AtsRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;

/**
 * @author Donald G. Dunne
 */
public class OpenVersionArtifactActionTest extends AbstractAtsActionRunTest {

   @Override
   public OpenVersionArtifactAction createAction() throws OseeCoreException {
      SkynetTransaction transaction = new SkynetTransaction(AtsUtilCore.getAtsBranch(), getClass().getSimpleName());
      AtsTestUtil.getTeamWf().addRelation(AtsRelationTypes.TeamWorkflowTargetedForVersion_Version,
         AtsTestUtil.getVerArt1());
      AtsTestUtil.getVerArt1().persist(transaction);
      AtsTestUtil.getTeamWf().persist(transaction);
      transaction.execute();
      return new OpenVersionArtifactAction(AtsTestUtil.getTeamWf());
   }
}
