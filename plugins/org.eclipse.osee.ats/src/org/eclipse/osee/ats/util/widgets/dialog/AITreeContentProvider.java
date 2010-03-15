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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.ats.artifact.ActionableItemArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.utility.Artifacts;

public class AITreeContentProvider implements ITreeContentProvider {

   private final Active active;

   public AITreeContentProvider(Active active) {
      super();
      this.active = active;
   }

   @SuppressWarnings("unchecked")
   public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof Collection) {
         return ((Collection) parentElement).toArray();
      } else if (parentElement instanceof ActionableItemArtifact) {
         try {
            ActionableItemArtifact ai = (ActionableItemArtifact) parentElement;
            return AtsUtil.getActive(Artifacts.getChildrenOfTypeSet(ai, ActionableItemArtifact.class, false), active,
                  ActionableItemArtifact.class).toArray();
         } catch (Exception ex) {
         }
      }
      return new Object[] {};
   }

   public Object getParent(Object element) {
      try {
         if (element instanceof ActionableItemArtifact) {
            return ((ActionableItemArtifact) element).getParent();
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
      return null;
   }

   public boolean hasChildren(Object element) {
      return getChildren(element).length > 0;
   }

   public Object[] getElements(Object inputElement) {
      return getChildren(inputElement);
   }

   public void dispose() {
   }

   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
   }

}
