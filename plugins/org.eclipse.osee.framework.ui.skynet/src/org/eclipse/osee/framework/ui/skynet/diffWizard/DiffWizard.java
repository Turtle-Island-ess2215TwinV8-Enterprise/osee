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
package org.eclipse.osee.framework.ui.skynet.diffWizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.conflict.Conflict;
import org.eclipse.osee.framework.ui.skynet.mergeWizard.EditWFCAttributeWizardPage;
import org.eclipse.osee.framework.ui.skynet.widgets.xmerge.XMergeLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Theron Virgin
 */
public class DiffWizard extends Wizard {
   public static final String TITLE = "How would you like to resolve this conflict?";
   public static final String INDENT = "     ";
   public static final String SOURCE_TITLE = "Source Value:";
   public static final String DEST_TITLE = "Destination value:";
   public static final String ART_TEXT = "Artifact: ";
   public static final String TYPE_TEXT = "Attribute type: ";

   private DiffWizardPage diffWizardPage;
   private final Conflict conflict;

   public DiffWizard(Conflict conflict) {
      this.conflict = conflict;
   }

   @Override
   public void addPages() {
      diffWizardPage = new DiffWizardPage(conflict);
      addPage(diffWizardPage);
   }

   @Override
   public boolean performFinish() {
      return diffWizardPage.closingPage();
   }

   @Override
   public boolean canFinish() {
      return true;
   }

   public boolean getResolved() {
      return true;
   }

   @Override
   public IWizardPage getStartingPage() {
      return getPage(DiffWizardPage.TITLE);
   }

   @Override
   public IWizardPage getPreviousPage(IWizardPage page) {
      return null;
   }

   @Override
   public boolean performCancel() {
      return super.performCancel();
   }

   public void setResolution() throws OseeCoreException {
      if (getContainer() != null) {
         IWizardPage page = getContainer().getCurrentPage();
         Image image = XMergeLabelProvider.getMergeImage(conflict);
         if (page instanceof DiffWizardPage) {
            ((DiffWizardPage) page).setResolution(image);
         } else if (page instanceof EditWFCAttributeWizardPage) {
            ((EditWFCAttributeWizardPage) page).setResolution(image);
         }
      }

   }

}
