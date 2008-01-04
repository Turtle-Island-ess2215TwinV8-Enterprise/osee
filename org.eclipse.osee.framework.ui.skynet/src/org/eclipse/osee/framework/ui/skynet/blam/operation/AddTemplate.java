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
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap;
import org.eclipse.osee.framework.ui.skynet.render.PresentationType;
import org.eclipse.osee.framework.ui.skynet.render.RendererManager;
import org.eclipse.osee.framework.ui.skynet.render.WordRenderer;

/**
 * @author Ryan D. Brooks
 */
public class AddTemplate implements BlamOperation {

   private static final List<String> xWidgets =
         Arrays.asList("<XWidget xwidgetType=\"XBranchListViewer\" displayName=\"Branch\" />",
               "<XWidget xwidgetType=\"XText\" displayName=\"Presentation Type\" />",
               "<XWidget xwidgetType=\"XText\" displayName=\"Bundle Name\" />",
               "<XWidget xwidgetType=\"XText\" displayName=\"Template Name\" />",
               "<XWidget xwidgetType=\"XText\" displayName=\"Template Path\" />");

   /**
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#runOperation(org.eclipse.osee.framework.ui.skynet.blam.BlamVariableMap,
    *      org.eclipse.core.runtime.IProgressMonitor)
    */
   public void runOperation(BlamVariableMap variableMap, IProgressMonitor monitor) throws Exception {
      String presentationType = variableMap.getString("Presentation Type"); // PREVIEW
      String bundleName = variableMap.getString("Bundle Name"); // org.eclipse.osee.framework.ui.skynet
      String templateName = variableMap.getString("Template Name"); // My Template
      String templatePath = variableMap.getString("Template Path"); // support/templates/My_Template.xml
      Branch branch = variableMap.getBranch("Branch");

      WordRenderer wordRenderer =
            (WordRenderer) RendererManager.getInstance().getRendererById("org.eclipse.osee.framework.ui.skynet.word");
      wordRenderer.addTemplate(PresentationType.valueOf(presentationType), bundleName, templateName, templatePath,
            branch);
   }

   /*
    * (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#getXWidgetXml()
    */
   public List<String> getXWidgetXml() {
      return xWidgets;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.framework.ui.skynet.blam.operation.BlamOperation#getDescriptionUsage()
    */
   public String getDescriptionUsage() {
      return "Select parameters below and click the play button at the top right.";
   }
}