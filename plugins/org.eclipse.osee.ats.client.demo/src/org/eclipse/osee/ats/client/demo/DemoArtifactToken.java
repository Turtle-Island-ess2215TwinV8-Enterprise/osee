/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.client.demo;

import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.framework.core.data.IArtifactToken;
import org.eclipse.osee.framework.core.data.TokenFactory;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;

/**
 * @author Donald G. Dunne
 */
public final class DemoArtifactToken {

   public static final IArtifactToken SAW_Test_AI = TokenFactory.createArtifactToken("ABirZS0j81dpAiAyqUwA",
      "SAW Test", AtsArtifactTypes.ActionableItem);

   public static IArtifactToken Process_Team = TokenFactory.createArtifactToken("At2WHxBtYhx4Nxrck6gA", "Process_Team",
      AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken Tools_Team = TokenFactory.createArtifactToken("At2WHxCFyQPidx78iuAA", "Tools_Team",
      AtsArtifactTypes.TeamDefinition);

   public static IArtifactToken SAW_HW = TokenFactory.createArtifactToken("At2WHxCeMCHfcr02EkAA", "SAW_HW",
      AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken SAW_Code = TokenFactory.createArtifactToken("At2WHxC2lxLOGB0YiuQA", "SAW_Code",
      AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken SAW_Test = TokenFactory.createArtifactToken("At2WHxDuXkCIJFEtQ0AA", "SAW_Test",
      AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken SAW_SW_Design = TokenFactory.createArtifactToken("At2WHxEGxl7nWuqx7FQA",
      "SAW_SW_Design", AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken SAW_Requirements = TokenFactory.createArtifactToken("At2WHxEfLXfCLytmLlAA",
      "SAW_Requirements", AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken SAW_SW = TokenFactory.createArtifactToken("At2WHxFk5VVE2cafF5AA", "SAW_SW",
      CoreArtifactTypes.Folder);

   // SAW_SW Versions
   public static IArtifactToken SAW_Bld_1 = TokenFactory.createArtifactToken("A8msa8LTDG36oWAnq3QA", "SAW_Bld_1",
      AtsArtifactTypes.Version);
   public static IArtifactToken SAW_Bld_2 = TokenFactory.createArtifactToken("A8YqcqyKh3HCkcHfEVwA", "SAW_Bld_2",
      AtsArtifactTypes.Version);
   public static IArtifactToken SAW_Bld_3 = TokenFactory.createArtifactToken("A8msa8LrcxhyrUTsbuwA", "SAW_Bld_3",
      AtsArtifactTypes.Version);

   public static IArtifactToken CIS_SW = TokenFactory.createArtifactToken("At2WHxF7jmUa8jXR3iwA", "CIS_SW",
      AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken CIS_Code = TokenFactory.createArtifactToken("At2WHxGo4A1nnGWYjgwA", "CIS_Code",
      AtsArtifactTypes.TeamDefinition);
   public static IArtifactToken CIS_Test = TokenFactory.createArtifactToken("At2WHxHZrl0bKPA6uUgA", "CIS_Test",
      AtsArtifactTypes.TeamDefinition);

   public static IArtifactToken Facilities_Team = TokenFactory.createArtifactToken("At2WHxIMOz66yR56eRAA",
      "Facilities_Team", CoreArtifactTypes.Folder);

   public static IArtifactToken DemoPrograms = TokenFactory.createArtifactToken("Awsk_RtnczAchcuSxagA",
      "Demo Programs", CoreArtifactTypes.Artifact);

   private DemoArtifactToken() {
      // Constants
   }
}
