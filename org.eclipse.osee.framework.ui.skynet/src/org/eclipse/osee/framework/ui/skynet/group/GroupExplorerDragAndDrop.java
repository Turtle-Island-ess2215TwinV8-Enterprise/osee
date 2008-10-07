/*
 * Created on Oct 7, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.group;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osee.framework.db.connection.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactData;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactTransfer;
import org.eclipse.osee.framework.skynet.core.artifact.BranchPersistenceManager;
import org.eclipse.osee.framework.skynet.core.artifact.UniversalGroup;
import org.eclipse.osee.framework.skynet.core.relation.CoreRelationEnumeration;
import org.eclipse.osee.framework.skynet.core.transaction.AbstractSkynetTxTemplate;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;
import org.eclipse.osee.framework.ui.skynet.util.OSEELog;
import org.eclipse.osee.framework.ui.skynet.util.SkynetDragAndDrop;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Donald G. Dunne
 */
public class GroupExplorerDragAndDrop extends SkynetDragAndDrop {

   boolean isFeedbackAfter = false;
   private final TreeViewer treeViewer;
   private final String viewId;
   private boolean isCtrlPressed = false;

   public GroupExplorerDragAndDrop(TreeViewer treeViewer, String viewId) {
      super(treeViewer.getTree(), viewId);
      this.treeViewer = treeViewer;
      this.viewId = viewId;
      treeViewer.getTree().addKeyListener(new keySelectedListener());
   }
   private class keySelectedListener implements KeyListener {
      public void keyPressed(KeyEvent e) {
         isCtrlPressed = (e.keyCode == SWT.CONTROL);
      }

      public void keyReleased(KeyEvent e) {
         if (e.keyCode == 'a' && e.stateMask == SWT.CONTROL) {
            treeViewer.getTree().selectAll();
         }
         if (e.keyCode == 'x' && e.stateMask == SWT.CONTROL) {
            expandAll((IStructuredSelection) treeViewer.getSelection());
         }
         isCtrlPressed = !(e.keyCode == SWT.CONTROL);
      }
   }

   private void expandAll(IStructuredSelection selection) {
      Iterator<?> iter = selection.iterator();
      while (iter.hasNext()) {
         treeViewer.expandToLevel(iter.next(), TreeViewer.ALL_LEVELS);
      }
   }

   @Override
   public Artifact[] getArtifacts() {
      IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
      Iterator<?> i = selection.iterator();
      List<Artifact> artifacts = new ArrayList<Artifact>();
      while (i.hasNext()) {
         Object object = i.next();
         if (object instanceof GroupExplorerItem) {
            artifacts.add(((GroupExplorerItem) object).getArtifact());
         }
      }
      return artifacts.toArray(new Artifact[artifacts.size()]);
   }

   @Override
   public void performDragOver(DropTargetEvent event) {
      if (!ArtifactTransfer.getInstance().isSupportedType(event.currentDataType)) {
         event.detail = DND.DROP_NONE;
         return;
      }
      final ArtifactData artData = ArtifactTransfer.getInstance().nativeToJava(event.currentDataType);
      if (artData == null) {
         event.detail = DND.DROP_NONE;
         return;
      }
      for (Artifact art : artData.getArtifacts()) {
         if (art.getArtifactTypeName().equals(UniversalGroup.ARTIFACT_TYPE_NAME)) {
            event.detail = DND.DROP_NONE;
            return;
         }
      }

      Tree tree = treeViewer.getTree();
      TreeItem dragOverTreeItem = tree.getItem(treeViewer.getTree().toControl(event.x, event.y));

      event.feedback = DND.FEEDBACK_EXPAND;
      event.detail = DND.DROP_NONE;

      // Set as COPY if drag item over group (copy versus move will be determined on drop
      if (dragOverTreeItem != null && ((GroupExplorerItem) dragOverTreeItem.getData()).isUniversalGroup()) {
         event.detail = DND.DROP_COPY;
         tree.setInsertMark(null, false);
      }
      // Handle re-ordering within same group
      else if (dragOverTreeItem != null && !((GroupExplorerItem) dragOverTreeItem.getData()).isUniversalGroup()) {
         GroupExplorerItem dragOverGroupItem = (GroupExplorerItem) dragOverTreeItem.getData();
         IStructuredSelection selectedItem = (IStructuredSelection) treeViewer.getSelection();
         Object obj = selectedItem.getFirstElement();
         if (obj instanceof GroupExplorerItem) {
            GroupExplorerItem droppingGroupItem = (GroupExplorerItem) obj;

            // the group to move must belong to the same group as the member to insert before/after
            if ((dragOverGroupItem.getParentItem()).equals(droppingGroupItem.getParentItem())) {
               if (isFeedbackAfter) {
                  event.feedback = DND.FEEDBACK_INSERT_AFTER;
               } else {
                  event.feedback = DND.FEEDBACK_INSERT_BEFORE;
               }
               event.detail = DND.DROP_MOVE;
            }
         } else {
            if (isFeedbackAfter) {
               event.feedback = DND.FEEDBACK_INSERT_AFTER;
            } else {
               event.feedback = DND.FEEDBACK_INSERT_BEFORE;
            }
            event.detail = DND.DROP_COPY;
         }
      } else {
         tree.setInsertMark(null, false);
      }
   }

   @Override
   public void operationChanged(DropTargetEvent event) {
      if (!isCtrlPressed(event)) {
         isFeedbackAfter = false;
      }
   }

   private boolean isCtrlPressed(DropTargetEvent event) {
      boolean ctrPressed = (event.detail == 1);

      if (ctrPressed) {
         isFeedbackAfter = true;
      }
      return ctrPressed;
   }

   @Override
   public void performDrop(DropTargetEvent event) {
      try {
         TreeItem dragOverTreeITem = treeViewer.getTree().getItem(treeViewer.getTree().toControl(event.x, event.y));

         // This should always be true as all items are Group Explorer Items
         if (dragOverTreeITem.getData() instanceof GroupExplorerItem) {
            final GroupExplorerItem dragOverExplorerItem = (GroupExplorerItem) dragOverTreeITem.getData();

            // Drag item dropped ON universal group item 
            if (dragOverExplorerItem.isUniversalGroup()) {

               // Drag item came from inside Group Explorer
               if (event.data instanceof ArtifactData) {
                  // If event originated outside, it's a copy event;
                  // OR if event is inside and ctrl is down, this is a copy; add items to group
                  if (!((ArtifactData) event.data).getSource().equals(viewId) || (((ArtifactData) event.data).getSource().equals(
                        viewId) && isCtrlPressed)) {
                     copyArtifactsToGroup(event, dragOverExplorerItem);
                  }
                  // Else this is a move
                  else {
                     IStructuredSelection selectedItem = (IStructuredSelection) treeViewer.getSelection();
                     Iterator<?> iterator = selectedItem.iterator();
                     final Set<Artifact> insertArts = new HashSet<Artifact>();
                     while (iterator.hasNext()) {
                        Object obj = iterator.next();
                        if (obj instanceof GroupExplorerItem) {
                           insertArts.add(((GroupExplorerItem) obj).getArtifact());
                        }
                     }
                     GroupExplorerItem parentUnivGroupItem =
                           ((GroupExplorerItem) selectedItem.getFirstElement()).getParentItem();
                     final Artifact parentArtifact = parentUnivGroupItem.getArtifact();
                     final Artifact targetArtifact = dragOverExplorerItem.getArtifact();

                     AbstractSkynetTxTemplate relateArtifactTx =
                           new AbstractSkynetTxTemplate(BranchPersistenceManager.getDefaultBranch()) {

                              @Override
                              protected void handleTxWork() throws OseeCoreException, SQLException {
                                 for (Artifact artifact : insertArts) {
                                    // Remove item from old group
                                    parentArtifact.deleteRelation(CoreRelationEnumeration.UNIVERSAL_GROUPING__MEMBERS,
                                          artifact);
                                    // Add items to new group
                                    targetArtifact.addRelation(CoreRelationEnumeration.UNIVERSAL_GROUPING__MEMBERS,
                                          artifact);
                                 }
                                 parentArtifact.persistAttributesAndRelations();
                                 targetArtifact.persistAttributesAndRelations();
                              }
                           };

                     try {
                        relateArtifactTx.execute();
                     } catch (Exception ex) {
                        OSEELog.logException(SkynetGuiPlugin.class, ex, true);
                     }
                  }
               }
            }
            // Drag item dropped before or after group member
            else if (!dragOverExplorerItem.isUniversalGroup()) {

               if (event.data instanceof ArtifactData) {

                  GroupExplorerItem parentUnivGroupItem = null;
                  // Drag item came from inside Group Explorer
                  if (((ArtifactData) event.data).getSource().equals(viewId)) {
                     IStructuredSelection selectedItem = (IStructuredSelection) treeViewer.getSelection();
                     Iterator<?> iterator = selectedItem.iterator();
                     Set<Artifact> insertArts = new HashSet<Artifact>();
                     while (iterator.hasNext()) {
                        Object obj = iterator.next();
                        if (obj instanceof GroupExplorerItem) {
                           insertArts.add(((GroupExplorerItem) obj).getArtifact());
                        }
                     }
                     parentUnivGroupItem = ((GroupExplorerItem) selectedItem.getFirstElement()).getParentItem();
                     insertArts.toArray(new Artifact[insertArts.size()]);

                     Artifact parentArtifact = parentUnivGroupItem.getArtifact();
                     Artifact targetArtifact = dragOverExplorerItem.getArtifact();

                     for (Artifact art : insertArts) {
                        parentArtifact.setRelationOrder(targetArtifact, isFeedbackAfter,
                              CoreRelationEnumeration.UNIVERSAL_GROUPING__MEMBERS, art);
                        targetArtifact = art;
                     }
                     parentArtifact.persistRelations();
                  }
                  // Drag item came from outside Group Explorer
                  else {
                     List<Artifact> insertArts = Arrays.asList(((ArtifactData) event.data).getArtifacts());
                     parentUnivGroupItem = dragOverExplorerItem.getParentItem();
                     insertArts.toArray(new Artifact[insertArts.size()]);

                     Artifact parentArtifact = parentUnivGroupItem.getArtifact();
                     Artifact targetArtifact = dragOverExplorerItem.getArtifact();

                     for (Artifact art : insertArts) {
                        parentArtifact.addRelation(targetArtifact, isFeedbackAfter,
                              CoreRelationEnumeration.UNIVERSAL_GROUPING__MEMBERS, art, "");
                        targetArtifact = art;
                     }
                     parentArtifact.persistRelations();
                  }
               }
            }
            treeViewer.refresh(dragOverExplorerItem);
         }

         isFeedbackAfter = false;
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }
   }

   public void copyArtifactsToGroup(DropTargetEvent event, final GroupExplorerItem dragOverExplorerItem) {
      // Items dropped on Group; simply add items to group
      final Artifact[] artsToRelate = ((ArtifactData) event.data).getArtifacts();
      boolean alreadyRelated = true;
      for (Artifact artifact : artsToRelate) {
         if (!dragOverExplorerItem.contains(artifact)) {
            alreadyRelated = false;
            break;
         }
      }
      if (alreadyRelated) {
         AWorkbench.popup("ERROR", "Artifact(s) already related.");
         return;
      }
      AbstractSkynetTxTemplate relateArtifactTx =
            new AbstractSkynetTxTemplate(BranchPersistenceManager.getDefaultBranch()) {

               @Override
               protected void handleTxWork() throws OseeCoreException, SQLException {
                  for (Artifact art : artsToRelate) {
                     if (!dragOverExplorerItem.contains(art)) {
                        dragOverExplorerItem.getArtifact().addRelation(
                              CoreRelationEnumeration.UNIVERSAL_GROUPING__MEMBERS, art);
                     }
                  }
                  dragOverExplorerItem.getArtifact().persistRelations();
               }
            };

      try {
         relateArtifactTx.execute();
      } catch (Exception ex) {
         OSEELog.logException(SkynetGuiPlugin.class, ex, true);
      }

   }
}
