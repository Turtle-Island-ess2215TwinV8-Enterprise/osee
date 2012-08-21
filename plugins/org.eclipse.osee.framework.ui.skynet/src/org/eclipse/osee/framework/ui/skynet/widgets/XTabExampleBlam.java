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
package org.eclipse.osee.framework.ui.skynet.widgets;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;

/**
 * @author Donald G. Dunne
 */
public class XTabExampleBlam extends AbstractBlam {

   public XTabExampleBlam() {
      // do nothing
   }

   @Override
   public void runOperation(final VariableMap variableMap, IProgressMonitor monitor) {
      AWorkbench.popup("Nothing to do; this is example only");
   }

   @Override
   public String getXWidgetsXml() {
      return "<xWidgets>" +
      //
      "<XWidget xwidgetType=\"XText\" displayName=\"Some simple XText widget\" />" +
      //
      "<XWidget xwidgetType=\"XText\" beginTabFolder=\"Commit Manager\" beginTabItem=\"Port\" displayName=\"XText on first tab\" />" +
      //
      "<XWidget xwidgetType=\"XButton\" displayName=\"XButton on first tab\" />" +
      //
      "<XWidget xwidgetType=\"XListDropViewer\" endTabItem=\"true\" beginTabItem=\"Edit\" displayName=\"A drop viewer on 2nd tab\" />" +
      //
      "<XWidget xwidgetType=\"XButton\" endTabFolder=\"true\" endTabItem=\"true\" displayName=\"XButton on second tab\" />" +
      //
      "<XWidget xwidgetType=\"XText\" displayName=\"Simple XText outside of tabbed folder\" horizontalLabel=\"true\" />" +
      //
      "</xWidgets>";
   }

   @Override
   public String getDescriptionUsage() {
      return "Shows an example of how to use the Tabbed Folder XWidget flags.";
   }

   @Override
   public String getName() {
      return "XTab Example Blam";
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("Util");
   }

}