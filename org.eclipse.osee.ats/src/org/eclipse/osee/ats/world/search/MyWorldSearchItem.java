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
package org.eclipse.osee.ats.world.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.osee.ats.artifact.ActionArtifact;
import org.eclipse.osee.ats.util.AtsRelation;
import org.eclipse.osee.framework.skynet.core.SkynetAuthentication;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.relation.CoreRelationEnumeration;
import org.eclipse.osee.framework.skynet.core.relation.RelationManager;

/**
 * @author Donald G. Dunne
 */
public class MyWorldSearchItem extends UserSearchItem {

   public MyWorldSearchItem(String name) {
      this(name, null);
   }

   public MyWorldSearchItem() {
      super("My World", null);
   }

   public MyWorldSearchItem(String name, User user) {
      super(name, user);
   }

   public Collection<Artifact> searchIt(User user) throws Exception {
      List<Artifact> assigned =
            RelationManager.getRelatedArtifacts(Arrays.asList(SkynetAuthentication.getUser()), 1,
                  CoreRelationEnumeration.Users_Artifact);

      List<Artifact> artifacts =
            RelationManager.getRelatedArtifacts(assigned, 4, AtsRelation.SmaToTask_Sma,
                  AtsRelation.TeamWorkflowToReview_Team, AtsRelation.ActionToWorkflow_Action);

      List<Artifact> artifactsToReturn = new ArrayList<Artifact>(artifacts.size());
      for (Artifact artifact : artifacts) {
         if (artifact instanceof ActionArtifact) {
            artifactsToReturn.add(artifact);
         }
      }

      return artifactsToReturn;
   }
}