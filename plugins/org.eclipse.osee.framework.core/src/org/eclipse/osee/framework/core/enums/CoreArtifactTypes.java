/*******************************************************************************
 * Copyright (c) 2009 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.enums;

import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.NamedIdentity;

/**
 * @author Ryan D. Brooks
 */
public class CoreArtifactTypes extends NamedIdentity implements IArtifactType {
   public static final CoreArtifactTypes AbstractSoftwareRequirement =
         new CoreArtifactTypes("ABNAYPwV6H4EkjQ3+QQA", "Abstract Software Requirement");
   public static final CoreArtifactTypes AbstractTestResult =
         new CoreArtifactTypes("ATkaanWmHH3PkhGNVjwA", "Abstract Test Result");
   public static final CoreArtifactTypes Artifact = new CoreArtifactTypes("AAMFDh6S7gRLupAMwywA", "Artifact");
   public static final CoreArtifactTypes CodeUnit = new CoreArtifactTypes("AAMFDkEh216dzK1mTZgA", "Code Unit");
   public static final CoreArtifactTypes Folder = new CoreArtifactTypes("AAMFDg_wmiYHHY5swJwA", "Folder");
   public static final CoreArtifactTypes GeneralData = new CoreArtifactTypes("AAMFDhQXfyb2m+jCwlwA", "General Data");
   public static final CoreArtifactTypes GeneralDocument =
         new CoreArtifactTypes("AAMFDhCjkTvP+VBpBCQA", "General Document");
   public static final CoreArtifactTypes GlobalPreferences =
         new CoreArtifactTypes("AAMFDho2kBqyoOZEw+gA", "Global Preferences");
   public static final CoreArtifactTypes Heading = new CoreArtifactTypes("AAMFDhEzni8FpFb5yHwA", "Heading");
   public static final CoreArtifactTypes IndirectSoftwareRequirement =
         new CoreArtifactTypes("AAMFDiC7HRQMqr5S0QwA", "Indirect Software Requirement");
   public static final CoreArtifactTypes Requirement = new CoreArtifactTypes("ABM_vxEEowY+8i2_q9gA", "Requirement");
   public static final CoreArtifactTypes RootArtifact = new CoreArtifactTypes("AAMFDhHDqlbzKcIxcsAA", "Root Artifact");
   public static final CoreArtifactTypes SoftwareRequirement =
         new CoreArtifactTypes("AAMFDiAwhRFXwIyapJAA", "Software Requirement");
   public static final CoreArtifactTypes SystemRequirement =
         new CoreArtifactTypes("AAMFDiSTcDGdUd9+tHAA", "System Requirement");
   public static final CoreArtifactTypes SoftwareRequirementDrawing =
         new CoreArtifactTypes("ABNClhgUfwj6A3EAArQA", "Software Requirement Drawing");
   public static final CoreArtifactTypes SoftwareRequirementFunction =
         new CoreArtifactTypes("ABNBwZMdFgEDTVQ7pTAA", "Software Requirement Function");
   public static final CoreArtifactTypes SoftwareRequirementProcedure =
         new CoreArtifactTypes("ABNBLPY4LnIKtcON0mgA", "Software Requirement Procedure");
   public static final CoreArtifactTypes DirectSoftwareRequirement =
         new CoreArtifactTypes("BtMwyalHkHkrRo7D0aAA", "Direct Software Requirement");
   public static final CoreArtifactTypes SubsystemRequirement =
         new CoreArtifactTypes("AAMFDiN9KiAkhuLqOhQA", "Subsystem Requirement");
   public static final CoreArtifactTypes TestInformationSheet =
         new CoreArtifactTypes("AAMFDjnM3wQxCjwatKAA", "Test Information Sheet");
   public static final CoreArtifactTypes TestPlanElement =
         new CoreArtifactTypes("ATi_kUpvPBiW2upYC_wA", "Test Plan Element");
   public static final CoreArtifactTypes TestProcedure =
         new CoreArtifactTypes("AAMFDjsjiGhoWpqM4PQA", "Test Procedure");
   public static final CoreArtifactTypes TestProcedureNative =
         new CoreArtifactTypes("AAMFDiWs_HdDJTbPPQgA", "Test Procedure Native");
   public static final CoreArtifactTypes TestProcedureWML =
         new CoreArtifactTypes("AAMFDiUeCG3KWx5XqeQA", "Test Procedure WML");
   public static final CoreArtifactTypes TestResultNative =
         new CoreArtifactTypes("ATkaanWmHH3PkhGNVjwA", "Test Result Native");
   public static final CoreArtifactTypes TestResultWML =
         new CoreArtifactTypes("ATk6NKFFmD_zg1b_eaQA", "Test Result WML");
   public static final CoreArtifactTypes TestUnit = new CoreArtifactTypes("ABM2d6uxUw66aSdo0LwA", "Test Unit");
   public static final CoreArtifactTypes UniversalGroup =
         new CoreArtifactTypes("AAMFDhLY2TnADPA_EQQA", "Universal Group");
   public static final CoreArtifactTypes User = new CoreArtifactTypes("AAMFDhmr+Dqqe5pn3kAA", "User");
   public static final CoreArtifactTypes UserGroup = new CoreArtifactTypes("AAMFDhrEbXqZKPfWkwAA", "User Group");
   public static final CoreArtifactTypes WorkItemDefinition =
         new CoreArtifactTypes("ABNM6pIn_x_3e6a3aIgA", "Work Item Definition");
   public static final CoreArtifactTypes WorkFlowDefinition =
         new CoreArtifactTypes("AAMFDh16eQ1GIHPWlYQA", "Work Flow Definition");
   public static final CoreArtifactTypes WorkPageDefinition =
         new CoreArtifactTypes("AAMFDhzuyizN4qu7tXgA", "Work Page Definition");
   public static final CoreArtifactTypes WorkRuleDefinition =
         new CoreArtifactTypes("AAMFDhxjHC2RUV2RkcQA", "Work Rule Definition");
   public static final CoreArtifactTypes WorkWidgetDefinition =
         new CoreArtifactTypes("AAMFDh4IVzqPgVTpLrwA", "Work Widget Definition");
   public static final CoreArtifactTypes XViewerGlobalCustomization =
         new CoreArtifactTypes("AAMFDhtN7T4of30iYhAA", "XViewer Global Customization");

   private CoreArtifactTypes(String guid, String name) {
      super(guid, name);
   }
}