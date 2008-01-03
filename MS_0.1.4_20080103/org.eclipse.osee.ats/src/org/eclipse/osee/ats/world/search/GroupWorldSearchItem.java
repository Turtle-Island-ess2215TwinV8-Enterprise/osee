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
package org.eclipse.osee.ats.world.search;

import java.sql.SQLException;
import java.util.Collection;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.UniversalGroup;
import org.eclipse.osee.framework.skynet.core.relation.RelationSide;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.GroupListDialog;
import org.eclipse.swt.widgets.Display;

/**
 * @author Donald G. Dunne
 */
public class GroupWorldSearchItem extends WorldSearchItem {

   private Artifact group;
   private Artifact selectedGroup;
   private final String groupName;

   public GroupWorldSearchItem(String displayName, String groupName) {
      super(displayName);
      this.groupName = groupName;
   }

   public GroupWorldSearchItem() {
      this("Groups Search", null);
   }

   public String getGroupSearchName() {
      if (group != null)
         return group.getDescriptiveName();
      else {
         if (selectedGroup != null) return selectedGroup.getDescriptiveName();
      }
      return "";
   }

   @Override
   public String getSelectedName() {
      return String.format("%s - %s", super.getSelectedName(), getGroupSearchName());
   }

   public void getProduct() {
      if (groupName == null) return;
      if (group == null) group = UniversalGroup.getGroups(groupName).iterator().next();
      if (group == null) throw new IllegalArgumentException("Can't Find Universal Group for " + getName());
   }

   @Override
   public Collection<Artifact> performSearch() throws SQLException, IllegalArgumentException {
      getProduct();
      if (getSearchGroup() == null) return EMPTY_SET;
      Collection<Artifact> arts = getSearchGroup().getArtifacts(RelationSide.UNIVERSAL_GROUPING__MEMBERS);
      if (cancelled) return EMPTY_SET;
      return arts;
   }

   private Artifact getSearchGroup() {
      if (group != null) return group;
      if (selectedGroup != null) return selectedGroup;
      return null;
   }

   @Override
   public void performUI() {
      if (groupName != null) return;
      if (group != null) return;
      GroupListDialog gld = new GroupListDialog(Display.getCurrent().getActiveShell());
      int result = gld.open();
      if (result == 0) {
         selectedGroup = (Artifact) gld.getSelection();
         return;
      } else {
         selectedGroup = null;
         cancelled = true;
      }
   }

}
