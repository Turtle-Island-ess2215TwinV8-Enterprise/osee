package org.eclipse.osee.ats.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osee.ats.review.ReviewPerspective;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Our sample action implements workbench action delegate. The action proxy will be created by the workbench and shown
 * in the UI. When the user tries to use the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class OpenReviewPerspectiveAction implements IWorkbenchWindowActionDelegate {

   public OpenReviewPerspectiveAction() {
      // do nothing
   }

   /**
    * The action has been activated. The argument of the method represents the 'real' action sitting in the workbench
    * UI.
    * 
    * @see IWorkbenchWindowActionDelegate#run
    */
   @Override
   public void run(IAction action) {
      AWorkbench.openPerspective(ReviewPerspective.ID);
   }

   /**
    * Selection in the workbench has been changed. We can change the state of the 'real' action here if we want, but
    * this can only happen after the delegate has been created.
    * 
    * @see IWorkbenchWindowActionDelegate#selectionChanged
    */
   @Override
   public void selectionChanged(IAction action, ISelection selection) {
      // do nothing
   }

   /**
    * We can use this method to dispose of any system resources we previously allocated.
    * 
    * @see IWorkbenchWindowActionDelegate#dispose
    */
   @Override
   public void dispose() {
      // do nothing
   }

   /**
    * We will cache window object in order to be able to provide parent shell for the message dialog.
    * 
    * @see IWorkbenchWindowActionDelegate#init
    */
   @Override
   public void init(IWorkbenchWindow window) {
      // do nothing
   }
}