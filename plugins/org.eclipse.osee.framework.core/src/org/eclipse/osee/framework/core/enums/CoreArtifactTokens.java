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
package org.eclipse.osee.framework.core.enums;

import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.TokenFactory;

public final class CoreArtifactTokens {

   // @formatter:off
   public static IArtifactToken DefaultHierarchyRoot = TokenFactory.createArtifactToken("AEslkN+d4hWXjQvnZ1gA", "Default Hierarchy Root", CoreArtifactTypes.RootArtifact);
   public static IArtifactToken GroupRoot = TokenFactory.createArtifactToken("AExdLMeOTGhhPY4CyvQA", "Root Artifact", CoreArtifactTypes.UniversalGroup);
   // @formatter:on

   private CoreArtifactTokens() {
      // Constants
   }
}
