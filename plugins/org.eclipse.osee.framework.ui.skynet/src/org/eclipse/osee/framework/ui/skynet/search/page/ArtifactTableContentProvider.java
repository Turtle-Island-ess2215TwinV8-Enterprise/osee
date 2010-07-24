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
package org.eclipse.osee.framework.ui.skynet.search.page;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osee.framework.ui.skynet.search.AbstractArtifactSearchResult;

/**
 * @author Roberto E. Escobar
 */
public class ArtifactTableContentProvider implements IStructuredContentProvider, IArtifactSearchContentProvider {

   private final Object[] EMPTY_ARR = new Object[0];

   private ArtifactSearchPage fPage;
   private AbstractArtifactSearchResult fResult;

   public ArtifactTableContentProvider(ArtifactSearchPage page) {
      fPage = page;
   }

   public void dispose() {
      // nothing to do
   }

   public Object[] getElements(Object inputElement) {
      if (inputElement instanceof AbstractArtifactSearchResult) {
         int elementLimit = getElementLimit();
         Object[] elements = ((AbstractArtifactSearchResult) inputElement).getElements();
         if (elementLimit != -1 && elements.length > elementLimit) {
            Object[] shownElements = new Object[elementLimit];
            System.arraycopy(elements, 0, shownElements, 0, elementLimit);
            return shownElements;
         }
         return elements;
      }
      return EMPTY_ARR;
   }

   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      if (newInput instanceof AbstractArtifactSearchResult) {
         fResult = (AbstractArtifactSearchResult) newInput;
      }
   }

   public void elementsChanged(Object[] updatedElements) {
      TableViewer viewer = getViewer();
      int elementLimit = getElementLimit();
      boolean tableLimited = elementLimit != -1;
      for (int i = 0; i < updatedElements.length; i++) {
         if (fResult.getMatchCount(updatedElements[i]) > 0) {
            if (viewer.testFindItem(updatedElements[i]) != null)
               viewer.update(updatedElements[i], null);
            else {
               if (!tableLimited || viewer.getTable().getItemCount() < elementLimit) viewer.add(updatedElements[i]);
            }
         } else
            viewer.remove(updatedElements[i]);
      }
   }

   private int getElementLimit() {
      return fPage.getElementLimit().intValue();
   }

   private TableViewer getViewer() {
      return (TableViewer) fPage.getViewer();
   }

   public void clear() {
      getViewer().refresh();
   }
}
