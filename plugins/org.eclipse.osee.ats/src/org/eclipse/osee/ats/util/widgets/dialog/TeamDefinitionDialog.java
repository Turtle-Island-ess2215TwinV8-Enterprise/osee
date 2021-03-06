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
package org.eclipse.osee.ats.util.widgets.dialog;

import java.util.Collection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osee.ats.api.data.AtsArtifactTypes;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.util.ArtifactNameSorter;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class TeamDefinitionDialog extends org.eclipse.ui.dialogs.ListDialog {

   public TeamDefinitionDialog(String title, String message) {
      super(Displays.getActiveShell());
      this.setTitle(title);
      this.setMessage(message);
      this.setContentProvider(new ArrayContentProvider() {
         @SuppressWarnings({"rawtypes", "unchecked"})
         @Override
         public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Collection) {
               Collection list = (Collection) inputElement;
               return list.toArray(new IAtsTeamDefinition[list.size()]);
            }
            return super.getElements(inputElement);
         }
      });
      setLabelProvider(new LabelProvider() {
         @Override
         public String getText(Object element) {
            if (element instanceof IAtsTeamDefinition) {
               return ((IAtsTeamDefinition) element).getName();
            }
            return "Unknown element type";
         }

         @Override
         public Image getImage(Object element) {
            if (element instanceof IAtsTeamDefinition) {
               return ArtifactImageManager.getImage(AtsArtifactTypes.TeamDefinition);
            }
            return null;
         }

      });
   }

   @Override
   protected Control createDialogArea(Composite container) {
      Control c = super.createDialogArea(container);
      getTableViewer().setSorter(new ArtifactNameSorter());
      return c;
   }

}
