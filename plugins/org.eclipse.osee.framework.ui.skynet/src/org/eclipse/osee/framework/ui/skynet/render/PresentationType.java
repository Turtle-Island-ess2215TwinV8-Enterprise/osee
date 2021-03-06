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
package org.eclipse.osee.framework.ui.skynet.render;

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;

/**
 * @author Megumi Telles
 */
public enum PresentationType {
   GENERALIZED_EDIT, // open using general editor (i.e. artifact editor)
   SPECIALIZED_EDIT, // open using application specific editor
   DIFF,
   F5_DIFF,
   PREVIEW, // open read-only using application specific editor
   MERGE,
   DEFAULT_OPEN, // up to the renderer to determine what is used for default
   GENERAL_REQUESTED, // this is the case where default open is selected and the preference "Default Presentation opens in Artifact Editor if applicable" is true
   PRODUCE_ATTRIBUTE; // used in conjunction with renderAttribute()

   public boolean matches(PresentationType... presentationTypes) throws OseeCoreException {
      Conditions.checkExpressionFailOnTrue(presentationTypes.length == 0, "presentationTypes to match cannot be empty");
      boolean result = false;
      for (PresentationType presentationType : presentationTypes) {
         if (this == presentationType) {
            result = true;
            break;
         }
      }
      return result;
   }
}