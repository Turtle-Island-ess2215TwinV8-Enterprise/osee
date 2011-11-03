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
package org.eclipse.osee.display.presenter.internal;

import java.util.LinkedList;
import java.util.List;
import junit.framework.Assert;
import org.eclipse.osee.display.api.data.SearchResultMatch;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.presenter.mocks.MockArtifact;
import org.eclipse.osee.display.presenter.mocks.MockArtifactProvider;
import org.eclipse.osee.display.presenter.mocks.MockAttribute;
import org.eclipse.osee.display.presenter.mocks.MockMatch;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.junit.Test;

/**
 * @author John Misinco
 */
public class SearchResultConverterTest {

   @Test
   public void testConvertToViewId() {
      MockArtifact toConvert = new MockArtifact("mockGuid", "mockName");
      SearchResultConverter converter = new SearchResultConverter(null);
      ViewId result = converter.convertToViewId(toConvert);
      Assert.assertEquals("mockGuid", result.getGuid());
      Assert.assertEquals("mockName", result.getName());
      Assert.assertEquals(CoreBranches.COMMON.getGuid(), result.getAttribute("branch"));
   }

   @Test
   public void testGetCrumbs() throws OseeCoreException {
      MockArtifact child = new MockArtifact("childGuid", "childName");
      MockArtifact parent = new MockArtifact("parentGuid", "parentName");
      MockArtifact grandParent = new MockArtifact("grandParentGuid", "grandParentName");
      child.setParent(parent);
      parent.setParent(grandParent);
      MockArtifactProvider provider = new MockArtifactProvider();
      provider.addArtifact(child);
      SearchResultConverter converter = new SearchResultConverter(provider);
      List<ViewId> crumbs = converter.getCrumbs(child, true);

      Assert.assertEquals(2, crumbs.size());
      Assert.assertEquals("parentGuid", crumbs.get(0).getGuid());
      Assert.assertEquals("parentName", crumbs.get(0).getName());
      Assert.assertEquals("grandParentGuid", crumbs.get(1).getGuid());
      Assert.assertEquals("grandParentName", crumbs.get(1).getName());

      crumbs = converter.getCrumbs(child, false);
      Assert.assertEquals(0, crumbs.size());
   }

   @Test
   public void testGetSearchResultMatches() throws OseeCoreException {
      MockArtifact art = new MockArtifact("mockGuid", "mockName");
      MockAttribute attr1 = new MockAttribute(CoreAttributeTypes.Active, "true");
      MockAttribute attr2 = new MockAttribute(CoreAttributeTypes.Name, "mockName");
      MockAttribute attr3 = new MockAttribute(CoreAttributeTypes.City, "Mesa");

      List<ReadableAttribute<?>> attrs = new LinkedList<ReadableAttribute<?>>();
      attrs.add(attr1);
      attrs.add(attr2);
      attrs.add(attr3);
      MockMatch mockMatch = new MockMatch(art, attrs);

      SearchResultConverter converter = new SearchResultConverter(null);
      List<SearchResultMatch> searchResultMatches = converter.getSearchResultMatches(mockMatch, true);

      Assert.assertEquals(searchResultMatches.size(), 3);
      Assert.assertEquals(CoreAttributeTypes.Active.getName(), searchResultMatches.get(0).getAttributeType());
      Assert.assertEquals(CoreAttributeTypes.Name.getName(), searchResultMatches.get(1).getAttributeType());
      Assert.assertEquals(CoreAttributeTypes.City.getName(), searchResultMatches.get(2).getAttributeType());

      searchResultMatches = converter.getSearchResultMatches(mockMatch, false);
      Assert.assertEquals(0, searchResultMatches.size());
   }

   @Test
   public void testGetTypeName() {
      MockArtifact testArt = new MockArtifact("mockGuid", "mockName", CoreArtifactTypes.CodeUnit, CoreBranches.COMMON);
      SearchResultConverter converter = new SearchResultConverter(null);
      String typeName = converter.getTypeName(testArt);
      Assert.assertEquals(CoreArtifactTypes.CodeUnit.getName(), typeName);
   }
}
