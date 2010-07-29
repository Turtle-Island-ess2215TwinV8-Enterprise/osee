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

import java.util.Collection;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.Result;

/**
 * @author Donald G. Dunne
 */
public class XListDam extends XList implements IAttributeWidget {

   private Artifact artifact;
   private String attributeTypeName;

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   /**
    * @param displayLabel
    */
   public XListDam(String displayLabel) {
      super(displayLabel);
   }

   @Override
   public String getAttributeType() {
      return attributeTypeName;
   }

   @Override
   public void setAttributeType(Artifact artifact, String attrName) throws OseeCoreException {
      this.artifact = artifact;
      this.attributeTypeName = attrName;
      super.setSelected(getStoredStrs());
   }

   @Override
   public void saveToArtifact() throws OseeCoreException {
      artifact.setAttributeValues(attributeTypeName, getSelectedStrs());
   }

   public Collection<String> getStoredStrs() throws OseeCoreException {
      return artifact.getAttributesToStringList(attributeTypeName);
   }

   @Override
   public Result isDirty() throws OseeCoreException {
      try {
         Collection<String> enteredValues = getSelectedStrs();
         Collection<String> storedValues = getStoredStrs();
         if (!Collections.isEqual(enteredValues, storedValues)) {
            return new Result(true, attributeTypeName + " is dirty");
         }
      } catch (NumberFormatException ex) {
         // do nothing
      }
      return Result.FalseResult;
   }

   @Override
   public void revert() throws OseeCoreException {
      setAttributeType(artifact, attributeTypeName);
   }

}
