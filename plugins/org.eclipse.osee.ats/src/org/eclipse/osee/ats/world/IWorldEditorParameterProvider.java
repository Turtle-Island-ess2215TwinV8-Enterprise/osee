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

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IDynamicWidgetLayoutListener;
import org.eclipse.osee.framework.ui.skynet.widgets.util.XWidgetRendererItem;

/**
 * @author Donald G. Dunne
 */
public interface IWorldEditorParameterProvider extends IWorldEditorProvider {

   public String getParameterXWidgetXml() throws OseeCoreException;

   public IDynamicWidgetLayoutListener getDynamicWidgetLayoutListener();

   public String[] getWidgetOptions(XWidgetRendererItem xWidgetData);

   public boolean isSaveButtonAvailable();

   public void handleSaveButtonPressed();

}
