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
package org.eclipse.osee.ats.workdef.editor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.commandHandlers.renderer.handlers.AbstractEditorHandler;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;

/**
 * @author Donald G. Dunne
 */
public class AtsWorkDefinitionDslHandler extends AbstractEditorHandler {

   @Override
   public Object executeWithException(ExecutionEvent event) throws OseeCoreException {
      if (!artifacts.isEmpty()) {
         AtsWorkDefinitionDslRenderer renderer = new AtsWorkDefinitionDslRenderer();
         renderer.open(artifacts, PresentationType.SPECIALIZED_EDIT);
      }
      return null;
   }
}