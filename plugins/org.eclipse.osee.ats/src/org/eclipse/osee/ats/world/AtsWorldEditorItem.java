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
package org.eclipse.osee.ats.world;

import java.util.Arrays;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.osee.ats.actions.DuplicateWorkflowViaWorldEditorAction;
import org.eclipse.osee.ats.export.AtsExportManager;

/**
 * @author Donald G. Dunne
 */
public class AtsWorldEditorItem extends AtsWorldEditorItemBase {

   @Override
   public List<? extends Action> getWorldEditorMenuActions(IWorldEditorProvider worldEditorProvider, WorldEditor worldEditor) {
      return Arrays.asList(new AtsExportManager(worldEditor), new DuplicateWorkflowViaWorldEditorAction(
         worldEditor.getWorldComposite().getWorldXViewer()));
   }

}
