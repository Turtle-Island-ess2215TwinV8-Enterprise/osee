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
package org.eclipse.osee.orcs.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.framework.core.data.IArtifactType;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.type.MatchLocation;
import org.eclipse.osee.orcs.ApplicationContext;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.OrcsIntegrationRule;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.AttributeReadable;
import org.eclipse.osee.orcs.db.mock.OseeDatabase;
import org.eclipse.osee.orcs.db.mock.OsgiService;
import org.eclipse.osee.orcs.search.CaseType;
import org.eclipse.osee.orcs.search.Match;
import org.eclipse.osee.orcs.search.Operator;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.orcs.search.StringOperator;
import org.eclipse.osee.orcs.utility.MatchComparator;
import org.eclipse.osee.orcs.utility.NameComparator;
import org.eclipse.osee.orcs.utility.SortOrder;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Roberto E. Escobar
 */
public class OrcsQueryTest {

   @Rule
   public OrcsIntegrationRule osgi = new OrcsIntegrationRule(this);

   @Rule
   public OseeDatabase db = new OseeDatabase("osee.demo.h2");

   private final boolean includeMatchLocationTests = false;

   @OsgiService
   private OrcsApi orcsApi;

   @Test
   public void testQueries() throws OseeCoreException {
      ApplicationContext context = null; // TODO use real application context
      QueryFactory factory = orcsApi.getQueryFactory(context);

      checkQueryByIds(factory);

      checkQueryArtifactType(factory);
      checkQueryArtifactTypeInheritance(factory);
      checkQueryArtifactTypesNoInheritance(factory);

      checkQueryAttributeValue(factory);
      checkQueryArtifactTypeAndNameValue(factory);

      checkQueryAttributeKeyword(factory);
   }

   private void checkQueryByIds(QueryFactory factory) throws OseeCoreException {
      QueryBuilder builder = factory.fromBranch(CoreBranches.COMMON).andGuidsOrHrids("AEmLGXnw0WaGLxcK5qwA");
      Assert.assertEquals(1, builder.getCount());

      ArtifactReadable artifact = builder.getResults().getExactlyOne();
      Assert.assertEquals("AEmLGXnw0WaGLxcK5qwA", artifact.getGuid());
   }

   private void checkQueryArtifactType(QueryFactory factory) throws OseeCoreException {
      QueryBuilder builder = factory.fromBranch(CoreBranches.COMMON).andIsOfType(CoreArtifactTypes.Folder);

      Assert.assertEquals(5, builder.getCount());

      List<ArtifactReadable> artifacts = builder.getResults().getList();
      Assert.assertEquals(5, artifacts.size());

      checkContainsTypes(artifacts, CoreArtifactTypes.Folder);

      Collections.sort(artifacts, new NameComparator(SortOrder.ASCENDING));

      Iterator<ArtifactReadable> iterator = artifacts.iterator();
      Assert.assertEquals("Action Tracking System", iterator.next().getName());
      Assert.assertEquals("Config", iterator.next().getName());
      Assert.assertEquals("Document Templates", iterator.next().getName());
      Assert.assertEquals("User Groups", iterator.next().getName());
      Assert.assertEquals("Work Definitions", iterator.next().getName());

      if (includeMatchLocationTests) {
         List<Match<ArtifactReadable, AttributeReadable<?>>> matches = builder.getMatches().getList();
         Assert.assertEquals(5, matches.size());

         Iterator<Match<ArtifactReadable, AttributeReadable<?>>> matchIterator = matches.iterator();
         checkMatch(matchIterator.next(), "Action Tracking System");
         checkMatch(matchIterator.next(), "Config");
         checkMatch(matchIterator.next(), "Document Templates");
         checkMatch(matchIterator.next(), "User Groups");
         checkMatch(matchIterator.next(), "Work Definitions");
      }
   }

   private void checkQueryArtifactTypeInheritance(QueryFactory factory) throws OseeCoreException {
      QueryBuilder builder =
         factory.fromBranch(TestBranches.SAW_Bld_1).andIsOfType(CoreArtifactTypes.AbstractSoftwareRequirement);//

      builder.excludeTypeInheritance();
      Assert.assertEquals(0, builder.getCount());

      ArtifactReadable artifact = builder.getResults().getOneOrNull();
      Assert.assertNull(artifact);

      builder.includeTypeInheritance();
      Assert.assertEquals(24, builder.getCount());

      List<ArtifactReadable> artifacts = builder.getResults().getList();
      Assert.assertEquals(24, artifacts.size());

      checkContainsTypes(artifacts, CoreArtifactTypes.SoftwareRequirement);
   }

   private void checkQueryArtifactTypesNoInheritance(QueryFactory factory) throws OseeCoreException {
      QueryBuilder builder = factory.fromBranch(CoreBranches.COMMON);
      builder.excludeTypeInheritance();
      builder.andIsOfType(CoreArtifactTypes.OseeTypeDefinition, CoreArtifactTypes.Folder);

      Assert.assertEquals(10, builder.getCount());

      List<ArtifactReadable> artifacts = builder.getResults().getList();
      Assert.assertEquals(10, artifacts.size());

      checkContainsTypes(artifacts, CoreArtifactTypes.OseeTypeDefinition, CoreArtifactTypes.Folder);

      Collections.sort(artifacts, new NameComparator(SortOrder.ASCENDING));

      Iterator<ArtifactReadable> iterator = artifacts.iterator();
      Assert.assertEquals("Action Tracking System", iterator.next().getName());
      Assert.assertEquals("Config", iterator.next().getName());
      Assert.assertEquals("Document Templates", iterator.next().getName());
      Assert.assertEquals("User Groups", iterator.next().getName());
      Assert.assertEquals("Work Definitions", iterator.next().getName());

      Assert.assertEquals("org.eclipse.osee.ats.OseeTypes_ATS", iterator.next().getName());
      Assert.assertEquals("org.eclipse.osee.ats.config.demo.OseeTypes_Demo", iterator.next().getName());
      Assert.assertEquals("org.eclipse.osee.coverage.OseeTypes_Coverage", iterator.next().getName());
      Assert.assertEquals("org.eclipse.osee.framework.skynet.core.OseeTypes_Framework", iterator.next().getName());
      Assert.assertEquals("org.eclipse.osee.ote.define.OseeTypesOTE", iterator.next().getName());
   }

   private void checkQueryAttributeValue(QueryFactory factory) throws OseeCoreException {
      QueryBuilder builder = factory.fromBranch(CoreBranches.COMMON);
      builder.and(CoreAttributeTypes.Name, Operator.EQUAL, "Action Tracking System");

      Assert.assertEquals(1, builder.getCount());

      ArtifactReadable artifact = builder.getResults().getExactlyOne();

      Assert.assertEquals("Action Tracking System", artifact.getName());
      Assert.assertEquals(CoreArtifactTypes.Folder, artifact.getArtifactType());

      if (includeMatchLocationTests) {
         Match<ArtifactReadable, AttributeReadable<?>> result = builder.getMatches().getExactlyOne();

         Assert.assertEquals(artifact, result.getItem());
         checkMatch(result, "Action Tracking System", CoreAttributeTypes.Name);

         AttributeReadable<?> attr = result.getElements().iterator().next();
         List<MatchLocation> location = result.getLocation(attr);
         Assert.assertEquals(1, location.size());

         MatchLocation loc1 = location.iterator().next();
         Assert.assertEquals(0, loc1.getStartPosition());
         Assert.assertEquals("Action Tracking System".length(), loc1.getEndPosition());
      }
   }

   private void checkQueryArtifactTypeAndNameValue(QueryFactory factory) throws OseeCoreException {
      //////////////////////
      QueryBuilder builder = factory.fromBranch(TestBranches.SAW_Bld_1);
      builder.and(CoreAttributeTypes.Name, Operator.EQUAL, "%Requirement%");

      Assert.assertEquals(7, builder.getCount());
      List<ArtifactReadable> artifacts = builder.getResults().getList();
      Assert.assertEquals(7, artifacts.size());
      checkContainsTypes(artifacts, CoreArtifactTypes.Folder, CoreArtifactTypes.SubsystemRequirement,
         CoreArtifactTypes.SystemRequirement);

      builder.andIsOfType(CoreArtifactTypes.Folder);
      Assert.assertEquals(4, builder.getCount());

      List<ArtifactReadable> folders = builder.getResults().getList();
      Assert.assertEquals(4, folders.size());
      Collections.sort(folders, new NameComparator(SortOrder.ASCENDING));
      Iterator<ArtifactReadable> folderIterator = folders.iterator();
      Assert.assertEquals("Hardware Requirements", folderIterator.next().getName());
      Assert.assertEquals("Software Requirements", folderIterator.next().getName());
      Assert.assertEquals("Subsystem Requirements", folderIterator.next().getName());
      Assert.assertEquals("System Requirements", folderIterator.next().getName());
      checkContainsTypes(folders, CoreArtifactTypes.Folder);

      //////////////////////
      QueryBuilder builder1 = factory.fromBranch(TestBranches.SAW_Bld_1);
      builder1.and(CoreAttributeTypes.Name, Operator.EQUAL, "%Requirement%");
      builder1.andIsOfType(CoreArtifactTypes.SubsystemRequirement);
      Assert.assertEquals(1, builder1.getCount());
      List<ArtifactReadable> subSystemReqs = builder1.getResults().getList();
      Assert.assertEquals(1, subSystemReqs.size());
      Assert.assertEquals("Subsystem Requirements", subSystemReqs.get(0).getName());
      checkContainsTypes(subSystemReqs, CoreArtifactTypes.SubsystemRequirement);

      //////////////////////
      QueryBuilder builder2 = factory.fromBranch(TestBranches.SAW_Bld_1);
      builder2.and(CoreAttributeTypes.Name, Operator.EQUAL, "%Requirement%");
      builder2.includeTypeInheritance();
      builder2.andIsOfType(CoreArtifactTypes.Requirement);
      Assert.assertEquals(3, builder2.getCount());

      List<ArtifactReadable> requirements = builder2.getResults().getList();
      Assert.assertEquals(3, requirements.size());
      Collections.sort(requirements, new NameComparator(SortOrder.ASCENDING));
      Iterator<ArtifactReadable> reqIterator = requirements.iterator();
      Assert.assertEquals("Performance Requirements", reqIterator.next().getName());
      Assert.assertEquals("Safety Requirements", reqIterator.next().getName());
      Assert.assertEquals("Subsystem Requirements", reqIterator.next().getName());
      checkContainsTypes(subSystemReqs, CoreArtifactTypes.SubsystemRequirement, CoreArtifactTypes.SystemRequirement);
   }

   private void checkQueryAttributeKeyword(QueryFactory factory) throws OseeCoreException {
      QueryBuilder builder = factory.fromBranch(TestBranches.SAW_Bld_1);
      builder.and(CoreAttributeTypes.Name, StringOperator.TOKENIZED_ANY_ORDER, CaseType.IGNORE_CASE, "REQUIREMENTS");

      Assert.assertEquals(7, builder.getCount());
      List<ArtifactReadable> requirements = builder.getResults().getList();
      Assert.assertEquals(7, requirements.size());
      checkContainsTypes(requirements, CoreArtifactTypes.Folder, CoreArtifactTypes.SubsystemRequirement,
         CoreArtifactTypes.SystemRequirement);
      Collections.sort(requirements, new NameComparator(SortOrder.ASCENDING));
      Iterator<ArtifactReadable> reqIterator = requirements.iterator();
      Assert.assertEquals("Hardware Requirements", reqIterator.next().getName());
      Assert.assertEquals("Performance Requirements", reqIterator.next().getName());
      Assert.assertEquals("Safety Requirements", reqIterator.next().getName());
      Assert.assertEquals("Software Requirements", reqIterator.next().getName());
      Assert.assertEquals("Subsystem Requirements", reqIterator.next().getName());
      Assert.assertEquals("Subsystem Requirements", reqIterator.next().getName());
      Assert.assertEquals("System Requirements", reqIterator.next().getName());

      //      if (includeMatchLocationTests) {
      List<Match<ArtifactReadable, AttributeReadable<?>>> matches = builder.getMatches().getList();
      Assert.assertEquals(7, matches.size());

      Collections.sort(matches, new MatchComparator(SortOrder.ASCENDING));

      // @formatter:off
      Iterator<Match<ArtifactReadable, AttributeReadable<?>>> matchIterator = matches.iterator();
      checkMatchSingleAttribute(matchIterator.next(), "Hardware Requirements", CoreAttributeTypes.Name, "Requirements");
      checkMatchSingleAttribute(matchIterator.next(), "Performance Requirements", CoreAttributeTypes.Name, "Requirements");
      checkMatchSingleAttribute(matchIterator.next(), "Safety Requirements", CoreAttributeTypes.Name, "Requirements");
      checkMatchSingleAttribute(matchIterator.next(), "Software Requirements", CoreAttributeTypes.Name, "Requirements");
      checkMatchSingleAttribute(matchIterator.next(), "Subsystem Requirements", CoreAttributeTypes.Name, "Requirements");
      checkMatchSingleAttribute(matchIterator.next(), "Subsystem Requirements", CoreAttributeTypes.Name, "Requirements");
      checkMatchSingleAttribute(matchIterator.next(), "System Requirements", CoreAttributeTypes.Name, "Requirements");
      // @formatter:on

      QueryBuilder builder1 = factory.fromBranch(TestBranches.SAW_Bld_1);
      builder1.and(CoreAttributeTypes.Name, StringOperator.TOKENIZED_ANY_ORDER, CaseType.MATCH_CASE, "REQUIREMENTS");
      Assert.assertEquals(0, builder1.getCount());
   }

   private static void checkContainsTypes(Collection<ArtifactReadable> arts, IArtifactType... types) throws OseeCoreException {
      List<IArtifactType> expected = Arrays.asList(types);
      for (ArtifactReadable art : arts) {
         Assert.assertTrue(String.format("artifact type [%s] not found", art.getArtifactType()),
            expected.contains(art.getArtifactType()));
      }
   }

   private static void checkMatchSingleAttribute(Match<ArtifactReadable, AttributeReadable<?>> match, String artName, IAttributeType types, String matched) throws OseeCoreException {
      Assert.assertEquals(artName, match.getItem().getName());

      AttributeReadable<?> attribute = match.getElements().iterator().next();
      Assert.assertEquals(types, attribute.getAttributeType());
      List<MatchLocation> locations = match.getLocation(attribute);
      Assert.assertEquals(1, locations.size());

      MatchLocation location = locations.get(0);
      String value = String.valueOf(attribute.getValue());
      Assert.assertEquals(matched, value.substring(location.getStartPosition() - 1, location.getEndPosition()));
   }

   private static void checkMatch(Match<ArtifactReadable, AttributeReadable<?>> match, String artName, IAttributeType... types) throws OseeCoreException {
      Assert.assertEquals(artName, match.getItem().getName());
      if (types.length > 0) {
         Assert.assertEquals(types.length, match.getElements().size());

         Iterator<AttributeReadable<?>> iterator = match.getElements().iterator();
         for (int index = 0; index < types.length; index++) {
            Assert.assertEquals(types[index], iterator.next().getAttributeType());
         }
      }
   }
}