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
package org.eclipse.osee.orcs.db.internal.change;

import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.model.change.ChangeItem;

/**
 * @author Roberto E. Escobar
 */
public class MockChangeItem extends ChangeItem {

   public MockChangeItem(int itemId, int itemTypeId, int artId, long currentSourceGammaId, ModificationType currentSourceModType) {
      super(itemId, itemTypeId, artId, currentSourceGammaId, currentSourceModType);
   }
}