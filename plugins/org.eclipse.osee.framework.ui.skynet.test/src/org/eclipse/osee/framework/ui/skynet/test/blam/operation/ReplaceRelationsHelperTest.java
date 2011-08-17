/*
 * Created on Jun 21, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.test.blam.operation;

import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.ui.skynet.replace.ReplaceUtil;
import org.junit.Assert;

/**
 * Test case for {@link ReplaceUtil}
 * 
 * @author Jeff C. Phillips
 */
public class ReplaceRelationsHelperTest {

   @org.junit.Test
   public void testAddArtifactGuidToAttrOrder() {
      String guid = GUID.create();
      String beforeGuid = GUID.create();
      String relationOrder = beforeGuid + "," + "123344443";
      Assert.assertTrue(ReplaceUtil.addArtifactGuidBeforeToRelationOrder(guid, beforeGuid, relationOrder).contains(
         guid));
   }

   @org.junit.Test
   public void testAddArtifactGuidToAttrOrderFailure() {
      String guid = GUID.create();
      String beforeGuid = GUID.create();
      String relationOrder = "123344443";
      Assert.assertFalse(ReplaceUtil.addArtifactGuidBeforeToRelationOrder(guid, beforeGuid, relationOrder).contains(
         guid));
   }

   @org.junit.Test
   public void testRemoveArtifactGuidFromAttrOrder() {
      String guid = GUID.create();
      String beforeGuid = GUID.create();
      String relationOrder = beforeGuid + "," + guid;
      Assert.assertFalse(ReplaceUtil.removeArtifactGuidFromRelationOrder(guid, relationOrder).contains(guid));
   }

   @org.junit.Test
   public void testRemoveArtifactGuidFromAttrOrderStartComma() {
      String guid = GUID.create();
      String beforeGuid = GUID.create();
      String relationOrder = "," + beforeGuid + guid;
      String returnString = ReplaceUtil.removeArtifactGuidFromRelationOrder(guid, relationOrder);
      Assert.assertFalse(returnString.contains(guid));
      Assert.assertTrue(returnString.equals(beforeGuid));
   }

   @org.junit.Test
   public void testRemoveArtifactGuidFromAttrOrderExtraMiddleComma() {
      String guid = GUID.create();
      String beforeGuid = GUID.create();
      String endGuid = GUID.create();
      String relationOrder = beforeGuid + guid + ", ," + endGuid;
      String returnString = ReplaceUtil.removeArtifactGuidFromRelationOrder(guid, relationOrder);
      Assert.assertFalse(returnString.contains(guid));
      Assert.assertTrue(returnString.equals(beforeGuid + "," + endGuid));
   }

   @org.junit.Test
   public void testRemoveArtifactGuidFromAttrOrderEndComma() {
      String guid = GUID.create();
      String beforeGuid = GUID.create();
      String relationOrder = beforeGuid + "," + guid + ",";
      String returnString = ReplaceUtil.removeArtifactGuidFromRelationOrder(guid, relationOrder);
      Assert.assertFalse(returnString.contains(guid));
      Assert.assertTrue(returnString.equals(beforeGuid));
   }

   @org.junit.Test
   public void testGetPreviousArtifactGuiOrder() {
      String guid = GUID.create();
      String beforeGuid = GUID.create();
      String relationOrder = beforeGuid + "," + guid + ",";
      String returnString = ReplaceUtil.getBeforeOrderGuid(relationOrder, guid).getFirst();
      Assert.assertTrue(returnString.equals(beforeGuid));
   }
}