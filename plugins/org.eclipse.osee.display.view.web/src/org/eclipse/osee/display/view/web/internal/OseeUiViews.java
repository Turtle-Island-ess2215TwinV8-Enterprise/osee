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
package org.eclipse.osee.display.view.web.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.vaadin.widgets.HasViews;
import org.eclipse.osee.vaadin.widgets.Navigator;

/**
 * @author Roberto E. Escobar
 */
public class OseeUiViews implements HasViews {

   @Override
   public List<Class<? extends Navigator.View>> getViews() {
      // Dynamic View Registration?
      List<Class<? extends Navigator.View>> views = new ArrayList<Class<? extends Navigator.View>>();
      return views;
   }
}