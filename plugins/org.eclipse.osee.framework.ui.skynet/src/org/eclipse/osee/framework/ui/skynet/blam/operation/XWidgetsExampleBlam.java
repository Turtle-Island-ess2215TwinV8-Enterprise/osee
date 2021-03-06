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
package org.eclipse.osee.framework.ui.skynet.blam.operation;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;

/**
 * @author Donald G. Dunne
 * @author Karol M. Wilk
 */
public class XWidgetsExampleBlam extends AbstractBlam {

   private static final String description =
      "This BLAM provides an example of all available XWidgets for use by developers of BLAMs and other UIs";

   public XWidgetsExampleBlam() {
      super(null, description, BlamUiSource.FILE);
   }

   @Override
   public void runOperation(VariableMap variableMap, IProgressMonitor monitor) throws Exception {
      logf("Nothing to do here, this is only an example BLAM");
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("Util");
   }
}