/*
 * Created on Mar 7, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.skynet.core.util;

import java.util.Arrays;
import java.util.List;

/**
 * @author Donald G. Dunne
 */
public class Requirements {
   public static String SOFTWARE_REQUIREMENT = "Software Requirement";
   public static String SOFTWARE_REQUIREMENTS = "Software Requirements";
   public static String INDIRECT_SOFTWARE_REQUIREMENT = "Indirect Software Requirement";
   public static String SYSTEM_REQUIREMENT = "System Requirement";
   public static String SYSTEM_REQUIREMENTS = "System Requirements";
   public static String SUBSYSTEM_REQUIREMENT = "Subsystem Requirement";
   public static String SUBSYSTEM_REQUIREMENTS = "Subsystem Requirements";
   public static String COMPONENT = "Component";
   public static String TEST_SCRIPT = "Test Script";
   public static String TEST_PROCEDURE = "Test Procedure";
   public static String SUBSYSTEM = "Subsystem";
   public static String PARTITION = "Partition";
   public static String CSCI = "CSCI";

   public static List<String> Software_RequirementTypes =
         Arrays.asList(new String[] {SOFTWARE_REQUIREMENT, INDIRECT_SOFTWARE_REQUIREMENT});
}
