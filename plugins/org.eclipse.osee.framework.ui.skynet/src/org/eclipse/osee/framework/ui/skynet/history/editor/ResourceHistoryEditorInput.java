/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.history.editor;

import java.util.logging.Level;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.internal.Activator;
import org.eclipse.osee.framework.ui.skynet.preferences.EditorsPreferencePage;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

public class ResourceHistoryEditorInput implements IEditorInput, IPersistableElement {

   private final String artGuid;
   private final String branchGuid;
   private final String artName;

   public ResourceHistoryEditorInput(Artifact artifact) {
      this(artifact.getName(), artifact.getGuid(), artifact.getBranchGuid());
   }

   public ResourceHistoryEditorInput(String artName, String artGuid, String branchGuid) {
      this.artName = artName;
      this.artGuid = artGuid;
      this.branchGuid = branchGuid;
   }

   @Override
   public boolean exists() {
      return true;
   }

   public Image getImage() {
      return ImageManager.getImage(FrameworkImage.DB_ICON_BLUE_EDIT);
   }

   @Override
   public ImageDescriptor getImageDescriptor() {
      return ImageManager.getImageDescriptor(FrameworkImage.DB_ICON_BLUE_EDIT);
   }

   @Override
   public String getName() {
      return String.format("Resource History: " + artName);
   }

   @Override
   public IPersistableElement getPersistable() {
      try {
         if (EditorsPreferencePage.isCloseChangeReportEditorsOnShutdown()) {
            return null;
         } else {
            return this;
         }
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex.toString(), ex);
      }
      return null;
   }

   @Override
   public String getToolTipText() {
      return getName();
   }

   @SuppressWarnings("rawtypes")
   @Override
   public Object getAdapter(Class adapter) {
      return null;
   }

   @Override
   public boolean equals(Object object) {
      boolean result = false;
      if (object instanceof ResourceHistoryEditorInput) {
         ResourceHistoryEditorInput other = (ResourceHistoryEditorInput) object;
         result = artGuid.equals(other.artGuid) && branchGuid.equals(other.branchGuid);
      }
      return result;
   }

   @Override
   public int hashCode() {
      return artGuid.hashCode();
   }

   @Override
   public String getFactoryId() {
      return ResourceHistoryEditorInputFactory.ID;
   }

   @Override
   public void saveState(IMemento memento) {
      ResourceHistoryEditorInputFactory.saveState(memento, this);
   }

   public String getArtGuid() {
      return artGuid;
   }

   public String getBranchGuid() {
      return branchGuid;
   }

   public String getArtName() {
      return artName;
   }

}
