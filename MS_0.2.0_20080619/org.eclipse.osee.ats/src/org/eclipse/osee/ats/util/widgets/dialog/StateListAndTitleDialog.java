/*
 * Created on Jul 1, 2008
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.util.widgets.dialog;

import java.util.Collection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XText;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class StateListAndTitleDialog extends StateListDialog {

   String reviewTitle;
   XText titleText;

   /**
    * @param title
    * @param message
    * @param values
    */
   public StateListAndTitleDialog(String title, String message, Collection<String> values) {
      super(title, message, values);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.ui.dialogs.ListDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
    */
   @Override
   protected Control createDialogArea(Composite container) {
      Composite comp = new Composite(container, SWT.NONE);
      comp.setLayout(new GridLayout());
      comp.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
      titleText = new XText("Review Title");
      titleText.createWidgets(comp, 1);
      titleText.getStyledText().setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
      titleText.addXModifiedListener(new XModifiedListener() {
         /* (non-Javadoc)
          * @see org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener#widgetModified(org.eclipse.osee.framework.ui.skynet.widgets.XWidget)
          */
         @Override
         public void widgetModified(XWidget widget) {
            reviewTitle = titleText.get();
         }
      });

      Control control = super.createDialogArea(container);
      getTableViewer().setSorter(new ViewerSorter() {
         @SuppressWarnings("unchecked")
         @Override
         public int compare(Viewer viewer, Object e1, Object e2) {
            return getComparator().compare((String) e1, (String) e2);
         }
      });

      return control;
   }

   /**
    * @return the reviewTitle
    */
   public String getReviewTitle() {
      return reviewTitle;
   }

}
