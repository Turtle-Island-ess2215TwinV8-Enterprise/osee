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

import org.eclipse.osee.framework.core.exception.AttributeDoesNotExist;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

public class XIntegerDam extends XInteger implements IAttributeWidget {

   private Artifact artifact;
   private String attributeTypeName;

   public XIntegerDam(String displayLabel) {
      super(displayLabel);
   }

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   @Override
   public String getAttributeType() {
      return attributeTypeName;
   }

   @Override
   public void setAttributeType(Artifact artifact, String attrName) throws OseeCoreException {
      this.artifact = artifact;
      this.attributeTypeName = attrName;
      try {
         Integer value = artifact.getSoleAttributeValue(attributeTypeName);
         super.set(value.toString());
      } catch (AttributeDoesNotExist ex) {
         super.set("");
      }
   }

   @Override
   public void saveToArtifact() {
      try {
         if (text == null || text.equals("")) {
            artifact.deleteSoleAttribute(attributeTypeName);
         } else {
            Integer enteredValue = getInteger();
            artifact.setSoleAttributeValue(attributeTypeName, enteredValue);
         }
      } catch (NumberFormatException ex) {
         // do nothing
      } catch (Exception ex) {
         OseeLog.log(SkynetGuiPlugin.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   @Override
   public Result isDirty() throws OseeCoreException {
      try {
         Integer enteredValue = getInteger();
         Integer storedValue = artifact.getSoleAttributeValue(attributeTypeName);
         if (enteredValue.doubleValue() != storedValue.doubleValue()) {
            return new Result(true, attributeTypeName + " is dirty");
         }
      } catch (AttributeDoesNotExist ex) {
         if (!get().equals("")) {
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
