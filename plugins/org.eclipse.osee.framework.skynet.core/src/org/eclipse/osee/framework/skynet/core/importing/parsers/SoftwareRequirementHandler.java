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
package org.eclipse.osee.framework.skynet.core.importing.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.importing.RoughArtifact;
import org.eclipse.osee.framework.skynet.core.importing.RoughArtifactKind;

/**
 * @author Robert A. Fisher
 */
public abstract class SoftwareRequirementHandler extends WordOutlineExtractorDelegate {
   private static final Pattern partitionPattern = Pattern.compile("\\[([\\w\\(|\\)]+)\\]");

   private final IAttributeType attributeType;

   protected SoftwareRequirementHandler(IAttributeType attributeType) {
      super();
      this.attributeType = attributeType;
   }

   @Override
   public void processHeadingText(RoughArtifact roughArtifact, String headingText) throws OseeCoreException {
      StringBuilder text = new StringBuilder(headingText);
      Matcher matcher = partitionPattern.matcher(text);
      boolean isRequirement = false;

      while (matcher.find()) {
         isRequirement = true;

         roughArtifact.addAttribute(attributeType, matcher.group(1).trim());
         text.delete(matcher.start(), matcher.end());
         matcher.reset(text);
      }

      if (!isRequirement) {
         roughArtifact.setRoughArtifactKind(RoughArtifactKind.SECONDARY);
      }

      roughArtifact.addAttribute("Name", text.toString().trim());
   }
}
