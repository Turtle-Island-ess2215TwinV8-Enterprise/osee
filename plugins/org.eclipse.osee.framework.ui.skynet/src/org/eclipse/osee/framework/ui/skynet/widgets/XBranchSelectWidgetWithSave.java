/*******************************************************************************
 * Copyright (c) 2012 Boeing.
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
import java.util.List;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.data.IOseeBranch;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

public class XBranchSelectWidgetWithSave extends XBranchSelectWidget implements IAttributeWidget {

   private Artifact artifact;
   private IAttributeType attributeType;

   public XBranchSelectWidgetWithSave(String label) {
      super(label);
      addXModifiedListener(new DirtyListener());
   }

   public List<IOseeBranch> getStored() throws OseeCoreException {
      return artifact.getAttributeValues(attributeType);
   }

   @Override
   public Artifact getArtifact() {
      return artifact;
   }

   @Override
   public void saveToArtifact() throws OseeCoreException {
      artifact.setAttributeFromValues(attributeType, Arrays.asList(getSelection()));
   }

   @Override
   public void revert() throws OseeCoreException {
      setAttributeType(getArtifact(), getAttributeType());
   }

   @Override
   public Result isDirty() {
      if (isEditable()) {
         try {
            Collection<IOseeBranch> storedValues = getStored();
            Collection<IOseeBranch> widgetInput = Arrays.asList(getSelection());
            if (!Collections.isEqual(widgetInput, storedValues)) {
               return new Result(true, getAttributeType() + " is dirty");
            }
         } catch (OseeCoreException ex) {
            // Do nothing
         }
      }
      return Result.FalseResult;
   }

   @Override
   public void setAttributeType(Artifact artifact, IAttributeType attributeTypeName) throws OseeCoreException {
      this.artifact = artifact;
      this.attributeType = attributeTypeName;
      List<IOseeBranch> storedBranchReference = getStored();
      if (!storedBranchReference.isEmpty()) {
         setSelection(storedBranchReference.get(0));
      }
   }

   @Override
   public IAttributeType getAttributeType() {
      return attributeType;
   }

   private class DirtyListener implements XModifiedListener {
      @Override
      public void widgetModified(XWidget widget) {
         isDirty();
      }
   }

}