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
package org.eclipse.osee.ats.api.view;

import java.util.List;
import org.eclipse.osee.display.api.data.ViewId;
import org.eclipse.osee.display.api.view.SearchCriteriaView;

/**
 * @author John Misinco
 */
public interface AtsSearchCriteriaView extends SearchCriteriaView {

   public interface AtsSearchCriteriaListener {
      void onProgramSelected(ViewId program);

      void onInit();
   }

   void setPrograms(List<ViewId> programs);

   void setBuilds(List<ViewId> builds);

   void setSelectedProgram(ViewId program);

   void setSelectedBuild(ViewId build);

   ViewId getSelectedProgram();

   ViewId getSelectedBuild();

   void clearPrograms();

   void clearBuilds();

   void setAtsCriteriaViewListener(AtsSearchCriteriaListener listener);

}
