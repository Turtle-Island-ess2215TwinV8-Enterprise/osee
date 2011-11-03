/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.display.api.view;

import java.util.List;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.mvp.view.View;

/**
 * @author John Misinco
 */
public interface ArtifactView extends View {

   public interface ArtifactViewListener {
      void onRelationTypeSelected();

      void onRelatedArtifactSelected();
   }

   void setName(String name);

   void setBreadCrumbs(List<ViewId> crumbs);

   void clearAll();

   void clearRelations();

   void addRelationType(ViewId relationType);

   void setSideATitle(String name);

   void setSideBTitle(String name);

   void addSideAValue(ViewId id);

   void addSideBValue(ViewId id);

   ViewId getSelectedRelationType();

   ViewId getSelectedRelatedArtifact();

   void addAttribute(String type, String value);

   void clearAttributes();

   void setViewListener(ArtifactViewListener listener);
}
