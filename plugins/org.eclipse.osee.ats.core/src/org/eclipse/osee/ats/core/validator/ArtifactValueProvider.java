/*
 * Created on May 19, 2011
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.eclipse.osee.ats.core.workdef.WidgetDefinition;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.type.AttributeType;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.attribute.DateAttribute;

/**
 * @author Donald G. Dunne
 */
public class ArtifactValueProvider implements IValueProvider {

   private final Artifact artifact;
   private final String attributeTypeName;

   public ArtifactValueProvider(Artifact artifact, WidgetDefinition widgetDef) {
      this.artifact = artifact;
      this.attributeTypeName = widgetDef.getAtrributeName();
   }

   public ArtifactValueProvider(Artifact artifact, IAttributeType attributeType) {
      this.artifact = artifact;
      this.attributeTypeName = attributeType.getName();
   }

   @Override
   public boolean isEmpty() throws OseeCoreException {
      AttributeType attributeType = getAtributeType();
      if (attributeType != null) {
         return artifact.getAttributeCount(attributeType) == 0;
      }
      return true;
   }

   @Override
   public Collection<String> getValues() throws OseeCoreException {
      AttributeType attributeType = getAtributeType();
      if (attributeType != null) {
         return artifact.getAttributesToStringList(attributeType);
      }
      return Collections.emptyList();
   }

   public AttributeType getAtributeType() throws OseeCoreException {
      if (Strings.isValid(attributeTypeName)) {
         AttributeType attrType = AttributeTypeManager.getType(attributeTypeName);
         return attrType;
      }
      return null;
   }

   @Override
   public String getName() {
      return artifact.getName();
   }

   @Override
   public Collection<Date> getDateValues() throws OseeCoreException {
      AttributeType attributeType = getAtributeType();
      if (attributeType != null) {
         List<Date> dates = new ArrayList<Date>();
         for (Attribute<?> attr : artifact.getAttributes(attributeType)) {
            if (attr instanceof DateAttribute) {
               dates.add(((DateAttribute) attr).getValue());
            }
         }
         return dates;
      }
      return Collections.emptyList();

   }

}