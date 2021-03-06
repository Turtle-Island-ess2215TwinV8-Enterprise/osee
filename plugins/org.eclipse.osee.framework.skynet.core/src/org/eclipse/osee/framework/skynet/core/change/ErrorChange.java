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
package org.eclipse.osee.framework.skynet.core.change;

import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.skynet.core.revision.LoadChangeType;

/**
 * @author Megumi Telles
 */
public final class ErrorChange extends Change {
   private static final String ERROR_STRING = "!Error -";

   private final String errorMessage;
   private final String name;

   public ErrorChange(IOseeBranch branch, int artId, String exception) {
      super(branch, 0, artId, null, null, false, null, null);
      this.errorMessage = String.format("%s %s", ERROR_STRING, exception);
      this.name =
         String.format("%s ArtID: %s BranchGuid: %s - %s", ERROR_STRING, getArtId(),
            (branch == null ? null : branch.getGuid()), exception);
   }

   @Override
   public String getIsValue() {
      return errorMessage;
   }

   @Override
   public int getItemId() {
      return 0;
   }

   @Override
   public String getItemKind() {
      return errorMessage;
   }

   @Override
   public int getItemTypeId() {
      return 0;
   }

   @Override
   public String getItemTypeName() {
      return errorMessage;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public String getWasValue() {
      return errorMessage;
   }

   @SuppressWarnings("rawtypes")
   @Override
   public Object getAdapter(Class adapter) {
      return null;
   }

   @Override
   public LoadChangeType getChangeType() {
      return null;
   }

   @Override
   public Class<? extends IChangeWorker> getWorker() {
	   return null;
   }

}
