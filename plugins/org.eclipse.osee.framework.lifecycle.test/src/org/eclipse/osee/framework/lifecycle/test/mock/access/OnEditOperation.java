/*
 * Created on Jun 17, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.lifecycle.test.mock.access;

import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.IBasicArtifact;
import org.eclipse.osee.framework.lifecycle.AbstractLifecycleOperation;
import org.eclipse.osee.framework.lifecycle.LifecycleService;

public class OnEditOperation extends AbstractLifecycleOperation {

   public OnEditOperation(LifecycleService service, IBasicArtifact<?> userArtifact, Collection<IBasicArtifact<?>> artsToCheck) {
      super(service, new ChangeMgmtChkPoint(userArtifact, artsToCheck), "On Edit Op", "TestBundle");
   }

   @Override
   protected void doCoreWork(IProgressMonitor monitor) throws Exception {
      System.out.println("I am going to do some edit ...");
   }

}