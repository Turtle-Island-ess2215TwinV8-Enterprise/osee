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
package org.eclipse.osee.coverage.merge;

/**
 * @author Donald G. Dunne
 */
public enum MergeType {

   // This will result in adding this and all children with all attributes
   Add,
   // This will result in adding this and all children with all attributes
   Add_With_Moves,
   // This will result in deleting this child and re-ordering
   Delete,
   // This will result in adding this and all children with all attributes
   CI_Add,
   // This will result in changing existing coverage method to new one imported
   CI_Method_Update,
   // This will result in deleting this child and re-ordering
   CI_Delete,
   // This will result in changing name of package to match import, clearing rationale and setting coverage method
   CI_Renamed,
   // This will result in changing name of package to match import, clearing rationale and setting coverage method
   CI_Moved,
   // Group option containing Add, Rename of coverage items
   CI_Changes,
   // Group option that contains deletions and re-order items
   CI_Test_Units_Update,
   // Group option that contains just test unit name updates
   Delete_And_Reorder,
   // Just update package item's order with import item's order
   Moved_Due_To_Delete,
   // This will result in updating the package item method number of the imported method number
   Moved_Due_To_Add,
   // Used to display "Nothing to Import" message
   Error__Message,
   // Shows that a item is un-mergeable and needs to be handled manually or have the merge case added
   Error__UnMergable;

   public boolean isError() {
      return toString().startsWith("Error__");
   }
}
