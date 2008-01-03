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
package org.eclipse.osee.ats.hyper;

import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osee.ats.ActionDebug;
import org.eclipse.osee.ats.AtsPlugin;
import org.eclipse.osee.ats.actions.wizard.ArtifactSelectWizard;
import org.eclipse.osee.ats.artifact.ATSArtifact;
import org.eclipse.osee.ats.artifact.StateMachineArtifact;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.ats.world.search.MultipleHridSearchItem;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.event.LocalTransactionEvent;
import org.eclipse.osee.framework.skynet.core.event.RemoteTransactionEvent;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.skynet.core.event.TransactionEvent;
import org.eclipse.osee.framework.skynet.core.event.TransactionEvent.EventData;
import org.eclipse.osee.framework.skynet.core.relation.IRelationLink;
import org.eclipse.osee.framework.skynet.core.relation.RelationLinkGroup;
import org.eclipse.osee.framework.ui.plugin.event.Event;
import org.eclipse.osee.framework.ui.plugin.event.IEventReceiver;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.ats.IActionable;
import org.eclipse.osee.framework.ui.skynet.ats.OseeAts;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ArtifactHyperView extends HyperView implements IEventReceiver, IPartListener, IActionable, IPerspectiveListener2 {

   public static String VIEW_ID = "org.eclipse.osee.ats.hyper.ArtifactHyperView";
   public static ArtifactHyperItem topAHI;
   public static Artifact currentArtifact;
   public ActionDebug debug = new ActionDebug(false, "ArtifactHyperView");
   private SkynetEventManager eventManager;
   private Action pinAction;

   public ArtifactHyperView() {
      super();
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(this);
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(this);
      setNodeColor(whiteColor);
      setCenterColor(whiteColor);
      eventManager = SkynetEventManager.getInstance();
      defaultZoom.pcRadius += defaultZoom.pcRadiusFactor * 2;
      defaultZoom.uuRadius += defaultZoom.uuRadiusFactor * 2;
      defaultZoom.xSeparation += defaultZoom.xSeparationFactor;
   }

   public static ArtifactHyperView getArtifactHyperView() {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      try {
         return (ArtifactHyperView) page.showView(ArtifactHyperView.VIEW_ID);
      } catch (PartInitException e1) {
         MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Launch Error",
               "Couldn't Get OSEE Artifact Hyper View " + e1.getMessage());
      }
      return null;
   }

   public static void openArtifact(Artifact artifact) throws PartInitException {
      getArtifactHyperView().load(artifact);
   }

   @Override
   public void createPartControl(Composite top) {
      super.createPartControl(top);
      OseeAts.addBugToViewToolbar(this, this, AtsPlugin.getInstance(), VIEW_ID, "ATS Action View");
   }

   @Override
   public void handleItemDoubleClick(HyperViewItem hvi) {
      currentArtifact = ((ArtifactHyperItem) hvi).getArtifact();
      display();
   }

   public void load(Artifact artifact) {
      currentArtifact = artifact;
      display();
   }

   @Override
   public void display() {
      try {
         eventManager.unRegisterAll(this);
         if (currentArtifact == null) return;
         eventManager.register(RemoteTransactionEvent.class, this);
         eventManager.register(LocalTransactionEvent.class, this);
         topAHI = new ArtifactHyperItem(currentArtifact);
         // System.out.println("Artifact "+currentArtifact.getArtifactTypeNameShort());
         int x = 0;
         for (RelationLinkGroup grp : currentArtifact.getLinkManager().getGroups()) {
            debug.report("relation " + grp.getDescriptor().getName());

            for (IRelationLink link : grp.getGroupSide()) {
               // Don't process link if onlyShowRel is populated and doesn't contain link name
               if (onlyShowRelations.size() > 0) {
                  if (!onlyShowRelations.contains(link.getLinkDescriptor().getName())) continue;
                  x++;
                  if (x == 4) x = 0;
               }

               Artifact otherArt = link.getArtifactB();
               int otherOrder = link.getBOrder();
               int thisOrder = link.getAOrder();
               if (otherArt.equals(currentArtifact)) {
                  otherArt = link.getArtifactA();
                  otherOrder = link.getAOrder();
                  thisOrder = link.getBOrder();
               }
               if (!otherArt.isDeleted()) {
                  ArtifactHyperItem ahi = new ArtifactHyperItem(otherArt);
                  String tip = grp.getDescriptor().getName();
                  if (!link.getRationale().equals("")) tip += "(" + link.getRationale() + ")";
                  ahi.setRelationToolTip(tip);
                  String label =
                        (isShowOrder() ? "(" + thisOrder + ") " : "") + grp.getDescriptor().getShortName() + (isShowOrder() ? "(" + otherOrder + ") " : "");
                  if (!link.getRationale().equals("")) label += "(" + link.getRationale() + ")";
                  ahi.setRelationLabel(label);
                  ahi.setLink(link);
                  ahi.setRelationDirty(link.isDirty());
                  switch (x) {
                     case 0:
                        topAHI.addBottom(ahi);
                        break;
                     case 1:
                        topAHI.addLeft(ahi);
                        break;
                     case 2:
                        topAHI.addTop(ahi);
                        break;
                     case 3:
                        topAHI.addRight(ahi);
                        break;
                     default:
                        break;
                  }
               }
            }
            x++;
            if (x == 4) x = 0;
         }
         create(topAHI);
         center();
      } catch (SQLException ex) {
         clear();
      }
   }

   @Override
   protected void handleRefreshButton() {
      display();
   }

   @Override
   public void dispose() {
      super.dispose();
      eventManager.unRegisterAll(this);
   }

   public void handleWindowChange() {
      if (pinAction.isChecked()) return;
      if (!this.getSite().getPage().isPartVisible(this)) return;
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      if (page != null) {
         IEditorPart editor = page.getActiveEditor();
         if (editor != null && (editor instanceof SMAEditor)) {
            currentArtifact = (ATSArtifact) ((SMAEditor) editor).getSmaMgr().getSma();
            load(currentArtifact);
         }
      }
   }

   public void partActivated(IWorkbenchPart part) {
      handleWindowChange();
   }

   public void partBroughtToTop(IWorkbenchPart part) {
      handleWindowChange();
   }

   public void partClosed(IWorkbenchPart part) {
      if (part.equals(this))
         dispose();
      else
         handleWindowChange();
   }

   public void partDeactivated(IWorkbenchPart part) {
      handleWindowChange();
   }

   public void partOpened(IWorkbenchPart part) {
      handleWindowChange();
   }

   @Override
   protected void createActions() {
      super.createActions();

      pinAction = new Action("Pin Viewer", Action.AS_CHECK_BOX) {

         public void run() {
         }
      };
      pinAction.setToolTipText("Keep viewer from updating based on open Actions.");
      pinAction.setImageDescriptor(AtsPlugin.getInstance().getImageDescriptor("pinEditor.gif"));

      Action openArtAction = new Action("Open Artifact") {

         public void run() {
            handleOpenArtifact();
         }
      };
      openArtAction.setToolTipText("Open Artifact");
      openArtAction.setImageDescriptor(AtsPlugin.getInstance().getImageDescriptor("artView.gif"));

      Action openByIdAction = new Action("Open by Id", IAction.AS_PUSH_BUTTON) {

         public void run() {
            MultipleHridSearchItem gsi = new MultipleHridSearchItem();
            try {
               Collection<Artifact> arts = gsi.performSearchGetResults(true, false);
               if (arts.size() == 0) {
                  AWorkbench.popup("ERROR", "No Artifacts Found");
                  return;
               }
               load(arts.iterator().next());
            } catch (Exception ex) {
               logger.log(Level.SEVERE, ex.toString(), ex);
            }
         }
      };
      openByIdAction.setToolTipText("Open by Id");

      IMenuManager mm = getViewSite().getActionBars().getMenuManager();
      mm.add(new Separator());
      mm.add(openByIdAction);

      IActionBars bars = getViewSite().getActionBars();
      IToolBarManager tbm = bars.getToolBarManager();
      tbm.add(new Separator());
      // tbm.add(homeAction);
      tbm.add(openArtAction);
      tbm.add(pinAction);
   }

   public void handleOpenArtifact() {

      ArtifactSelectWizard selWizard = new ArtifactSelectWizard();
      WizardDialog dialog =
            new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), selWizard);
      dialog.create();
      if (dialog.open() == 0) {
         load(selWizard.getSelectedArtifact());
      }
   }

   public String getActionDescription() {
      if (currentArtifact != null && currentArtifact.isDeleted()) return String.format("Current Artifact - %s - %s",
            currentArtifact.getGuid(), currentArtifact.getDescriptiveName());
      return "";
   }

   public void onEvent(Event event) {

      if (event instanceof TransactionEvent) {
         EventData ed = ((TransactionEvent) event).getEventData(currentArtifact);
         if (ed.isRemoved()) {
            clear();
         } else if (ed.getAvie() != null && ed.getAvie().getOldVersion().equals(currentArtifact)) {
            currentArtifact = (StateMachineArtifact) ed.getAvie().getNewVersion();
            display();
         } else if (ed.getAvie() != null || ed.isModified() || ed.isRelChange()) {
            display();
         }
      } else
         logger.log(Level.SEVERE, "Unexpected event => " + event);
   }

   public boolean runOnEventInDisplayThread() {
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage,
    *      org.eclipse.ui.IPerspectiveDescriptor)
    */
   public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
      handleWindowChange();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage,
    *      org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
    */
   public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
      handleWindowChange();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage,
    *      org.eclipse.ui.IPerspectiveDescriptor, org.eclipse.ui.IWorkbenchPartReference,
    *      java.lang.String)
    */
   public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
      handleWindowChange();
   }

}
