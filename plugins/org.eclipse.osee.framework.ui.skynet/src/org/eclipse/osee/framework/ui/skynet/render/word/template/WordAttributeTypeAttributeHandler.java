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

package org.eclipse.osee.framework.ui.skynet.render.word.template;

import java.util.Collection;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.xml.Xml;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.ui.skynet.render.word.WordMLProducer;

/**
 * @author Andrew M. Finkbeiner
 */
public final class WordAttributeTypeAttributeHandler implements ITemplateAttributeHandler {

   @Override
   public void process(WordMLProducer wordMl, Artifact artifact, TemplateAttribute templateAttribute) throws OseeCoreException {
      Collection<Attribute<Object>> attributes =
         artifact.getAttributes(AttributeTypeManager.getType(templateAttribute.getName()));

      if (!attributes.isEmpty()) {
         Attribute<Object> attribute = attributes.iterator().next();

         if (templateAttribute.hasLabel()) {
            wordMl.addParagraph(templateAttribute.getLabel());
         }

         wordMl.addWordMl(Xml.escape(attribute.toString()));
         wordMl.resetListValue();
      }
   }

   @Override
   public boolean canHandle(Artifact artifact, TemplateAttribute attribute) throws OseeCoreException {
      boolean goodAttributeType = attribute.equals(CoreAttributeTypes.WordTemplateContent);
      boolean goodArtifact = artifact.isAttributeTypeValid(CoreAttributeTypes.WordTemplateContent);
      return goodAttributeType && goodArtifact;
   }
}