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
package org.eclipse.osee.framework.skynet.core.test.commit;

import junit.framework.Assert;
import org.eclipse.osee.framework.skynet.core.commit.ChangePair;
import org.eclipse.osee.framework.skynet.core.commit.CommitItem;
import org.eclipse.osee.framework.skynet.core.commit.CommitItem.GammaKind;

/**
 * @author Roberto E. Escobar
 */
public class CommitUtil {

   private CommitUtil() {
   }

   public static void checkChange(String message, ChangePair expected, ChangePair actual) {
      Assert.assertEquals(message, expected.getGammaId(), actual.getGammaId());
      Assert.assertEquals(message, expected.getModType(), actual.getModType());
   }

   public static CommitItem createItem(int itemId, ChangePair base, ChangePair first, ChangePair current, ChangePair destination, ChangePair net) {
      GammaKind[] kinds = GammaKind.values();
      return createItem(kinds[itemId % kinds.length], itemId, base, first, current, destination, net);
   }

   public static CommitItem createItem(GammaKind gammaKind, int itemId, ChangePair base, ChangePair first, ChangePair current, ChangePair destination, ChangePair net) {
      CommitItem change = new CommitItem(current.getGammaId(), current.getModType());
      change.setItemId(itemId);

      change.setKind(gammaKind);

      if (base != null) {
         change.getBase().setModType(base.getModType());
         change.getBase().setGammaId(base.getGammaId());
      }
      if (first != null) {
         change.getFirst().setGammaId(first.getGammaId());
         change.getFirst().setModType(first.getModType());
      }
      if (destination != null) {
         change.getDestination().setGammaId(destination.getGammaId());
         change.getDestination().setModType(destination.getModType());
      }
      if (net != null) {
         change.getNet().setGammaId(net.getGammaId());
         change.getNet().setModType(net.getModType());
      }
      return change;
   }
}
