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
package org.eclipse.osee.ats.api.data;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.TokenFactory;

/**
 * @author Donald G. Dunne
 */
public final class AtsArtifactTypes {

   // @formatter:off
   public static final IArtifactType Action = TokenFactory.createArtifactType(0x0000000000000043L, "Action");
   public static final IArtifactType ActionableItem = TokenFactory.createArtifactType(0x0000000000000045L, "Actionable Item");
   public static final IArtifactType DecisionReview = TokenFactory.createArtifactType(0x0000000000000042L, "Decision Review");
   public static final IArtifactType PeerToPeerReview = TokenFactory.createArtifactType(0x0000000000000041L, "PeerToPeer Review");
   public static final IArtifactType Task = TokenFactory.createArtifactType(0x000000000000004AL, "Task");
   public static final IArtifactType AbstractWorkflowArtifact = TokenFactory.createArtifactType(0x0000000000000047L, "Abstract State Machine Artifact");
   public static final IArtifactType ReviewArtifact = TokenFactory.createArtifactType(0x0000000000000040L, "Abstract Review Artifact");
   public static final IArtifactType TeamDefinition = TokenFactory.createArtifactType(0x0000000000000044L, "Team Definition");
   public static final IArtifactType TeamWorkflow = TokenFactory.createArtifactType(0x0000000000000049L, "Team Workflow");
   public static final IArtifactType Version = TokenFactory.createArtifactType(0x0000000000000046L, "Version");
   public static final IArtifactType Goal = TokenFactory.createArtifactType(0x0000000000000048L, "Goal");
   public static final IArtifactType AtsArtifact = TokenFactory.createArtifactType(0x000000000000003FL, "ats.Ats Artifact");
   public static final IArtifactType WorkDefinition = TokenFactory.createArtifactType(0x000000000000003EL, "Work Definition");
   // @formatter:on

   private AtsArtifactTypes() {
      // Constants
   }
}