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
package org.eclipse.osee.ats.util.widgets.port;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.nebula.widgets.xviewer.XViewer;
import org.eclipse.nebula.widgets.xviewer.XViewerColumn;
import org.eclipse.osee.ats.api.data.AtsRelationTypes;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class PortXViewer extends XViewer {

   private final XPortTableWidget xPortTableWidget;

   public PortXViewer(Composite parent, int style, XPortTableWidget xPortTableWidget) {
      super(parent, style, new PortXManagerFactory());
      this.xPortTableWidget = xPortTableWidget;
   }

   @Override
   public void updateMenuActionsForTable() {
      MenuManager mm = getMenuManager();

      mm.insertBefore(MENU_GROUP_PRE, new Separator());
   }

   @Override
   public void dispose() {
      getLabelProvider().dispose();
   }

   public List<Object> getSelectedArtifacts() {
      List<Object> arts = new ArrayList<Object>();
      TreeItem items[] = getTree().getSelection();
      if (items.length > 0) {
         for (TreeItem item : items) {
            arts.add(item.getData());
         }
      }
      return arts;
   }

   public XPortTableWidget getXPortTableWidget() {
      return xPortTableWidget;
   }

   @Override
   public void handleDoubleClick() {
      Object selected = getSelectedArtifacts().iterator().next();
      if (selected instanceof TeamWorkFlowArtifact) {
         TeamWorkFlowArtifact teamArt = (TeamWorkFlowArtifact) selected;
         AtsUtil.openATSArtifact(teamArt);
      }
   }

   @Override
   public boolean handleLeftClickInIconArea(TreeColumn treeColumn, TreeItem treeItem) {
      XViewerColumn xCol = (XViewerColumn) treeColumn.getData();
      if (xCol.equals(PortXManagerFactory.Remove_Col)) {
         Object input = getInput();
         Collection colInput = null;
         if (input instanceof Collection) {
            colInput = (Collection<TeamWorkFlowArtifact>) getInput();
         } else {
            colInput = new ArrayList<TeamWorkFlowArtifact>();
         }
         colInput.remove(treeItem.getData());
         setInput(colInput);
         xPortTableWidget.refresh();
         try {
            xPortTableWidget.getTeamArt().setRelations(AtsRelationTypes.Port_From, colInput);
            xPortTableWidget.getTeamArt().persist("Port Team Workflow Removed");
         } catch (OseeCoreException ex) {
            OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
         }

      }
      return false;
   }

}
