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

package org.eclipse.osee.framework.ui.skynet.widgets.util;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.WidgetOption;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.skynet.core.attribute.ArtifactReferenceAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.attribute.BranchReferenceAttribute;
import org.eclipse.osee.framework.skynet.core.attribute.WordAttribute;
import org.eclipse.osee.framework.skynet.core.utility.AttributeTypeToXWidgetName;

/**
 * @author Donald G. Dunne
 */
public class DefaultAttributeXWidgetProvider implements IAttributeXWidgetProvider {

   private XWidgetRendererItem createDynamicXWidgetLayout(IAttributeType attributeType, int minOccurrence) {
      XWidgetRendererItem defaultData = new XWidgetRendererItem(null);
      defaultData.setName(attributeType.getName());
      defaultData.setStoreName(attributeType.getName());
      defaultData.setToolTip(attributeType.getDescription());
      if (minOccurrence > 0) {
         defaultData.getXOptionHandler().add(WidgetOption.REQUIRED_FOR_COMPLETION);
      }
      defaultData.getXOptionHandler().add(WidgetOption.HORIZONTAL_LABEL);
      return defaultData;
   }

   public static boolean useMultiLineWidget(IAttributeType attributeType) throws OseeCoreException {
      return AttributeTypeManager.isBaseTypeCompatible(WordAttribute.class, attributeType) || attributeType.equals(CoreAttributeTypes.RelationOrder) || attributeType.equals(CoreAttributeTypes.PlainTextContent);
   }

   @Override
   public List<XWidgetRendererItem> getDynamicXWidgetLayoutData(IAttributeType attributeType) throws OseeCoreException {
      List<XWidgetRendererItem> xWidgetLayoutData = new ArrayList<XWidgetRendererItem>();

      XWidgetRendererItem defaultData =
         createDynamicXWidgetLayout(attributeType, AttributeTypeManager.getMinOccurrences(attributeType));
      xWidgetLayoutData.add(defaultData);

      String xWidgetName;
      try {
         xWidgetName = AttributeTypeToXWidgetName.getXWidgetName(attributeType);
         if (useMultiLineWidget(attributeType)) {
            xWidgetName = "XStackedDam";
            defaultData.getXOptionHandler().add(WidgetOption.NOT_EDITABLE);
         } else if (AttributeTypeManager.isBaseTypeCompatible(BranchReferenceAttribute.class, attributeType)) {
            xWidgetName = "XBranchSelectWidgetWithSave";
         } else if (AttributeTypeManager.isBaseTypeCompatible(ArtifactReferenceAttribute.class, attributeType)) {
            xWidgetName = "XListDropViewerWithSave";
         }
      } catch (OseeCoreException ex) {
         xWidgetName = "XTextDam";
         StringBuilder builder = new StringBuilder();
         builder.append("Unable to determine base type for attribute type");
         builder.append(String.format("[%s]", attributeType));
         builder.append(Lib.exceptionToString(ex));
         defaultData.setDefaultValue(builder.toString());
      }

      defaultData.setXWidgetName(xWidgetName);
      defaultData.getXOptionHandler().add(WidgetOption.FILL_HORIZONTALLY);
      defaultData.getXOptionHandler().add(WidgetOption.NO_DEFAULT_VALUE);

      return xWidgetLayoutData;
   }

}
