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

import java.util.ArrayList;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

/**
 * @author Jeff C. Phillips
 */
public class XAttributeTypeListViewer extends XTypeListViewer {
   private static final String NAME = "XAttributeTypeListViewer";

   public XAttributeTypeListViewer(String keyedBranchName, String defaultValue) {
      super(NAME);

      try {
         setContentProvider(new DefaultBranchContentProvider(new AttributeContentProvider(),
            BranchManager.getSystemRootBranch()));
         ArrayList<Object> input = new ArrayList<Object>(1);
         input.add(resolveBranch(keyedBranchName));

         setInput(input);

         try {
            if (defaultValue != null) {
               AttributeType attributeType = AttributeTypeManager.getType(defaultValue);
               setDefaultSelected(attributeType);
            }
         } catch (Exception ex) {
            OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
         }
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }
}