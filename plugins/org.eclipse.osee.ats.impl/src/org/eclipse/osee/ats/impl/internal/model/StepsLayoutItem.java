/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.impl.internal.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.api.workdef.IAtsStepPageDefinition;
import org.eclipse.osee.ats.api.workdef.IAtsStepsLayoutItem;

/**
 * @author Donald G. Dunne
 */
public class StepsLayoutItem extends LayoutItem implements IAtsStepsLayoutItem {

   private final List<IAtsStepPageDefinition> pageDefs = new ArrayList<IAtsStepPageDefinition>(5);

   public StepsLayoutItem(String name) {
      super(name);
   }

   @Override
   public List<IAtsStepPageDefinition> getStepPageDefinitions() {
      return pageDefs;
   }

   @Override
   public String toString() {
      return "Steps " + pageDefs.size();
   }
}
