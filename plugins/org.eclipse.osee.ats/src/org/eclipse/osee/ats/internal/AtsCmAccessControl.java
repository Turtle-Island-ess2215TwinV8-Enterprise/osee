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
package org.eclipse.osee.ats.internal;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.osee.ats.access.AtsBranchAccessManager;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.data.IAccessContextId;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.model.IBasicArtifact;
import org.eclipse.osee.framework.core.services.CmAccessControl;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * ATS currently only provides access control for artifacts on a Team Workflow's working branch.
 * 
 * @author Roberto E. Escobar
 * @author Donald G. Dunne
 */
public class AtsCmAccessControl implements CmAccessControl {

   private final AtsBranchAccessManager atsBranchAccessManager;

   public AtsCmAccessControl(AtsBranchAccessManager atsBranchObjectManager) {
      this.atsBranchAccessManager = atsBranchObjectManager;
   }

   @Override
   public boolean isApplicable(IBasicArtifact<?> user, Object object) {
      boolean result = false;
      if (object != null) {
         if (object instanceof Artifact && !((Artifact) object).getBranch().equals(AtsUtil.getAtsBranchToken())) {
            result = atsBranchAccessManager.isApplicable(((Artifact) object).getBranch());
         }
         if (object instanceof IOseeBranch) {
            result = atsBranchAccessManager.isApplicable((IOseeBranch) object);
         }
      }
      return result;
   }

   @Override
   public Collection<? extends IAccessContextId> getContextId(IBasicArtifact<?> user, Object object) {
      if (object != null) {
         if (object instanceof Artifact && !((Artifact) object).getBranch().equals(AtsUtil.getAtsBranchToken())) {
            return atsBranchAccessManager.getContextId(((Artifact) object).getBranch());
         }
         if (object instanceof IOseeBranch) {
            return atsBranchAccessManager.getContextId((IOseeBranch) object);
         }
      }
      return Collections.emptyList();
   }
}
