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
package org.eclipse.osee.ats.hyper;

import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.swt.graphics.Image;

public class ArtifactHyperItem extends HyperViewItem {

   private final Artifact artifact;

   public ArtifactHyperItem(Artifact artifact) {
      super(artifact.getName(), ArtifactImageManager.getImage(artifact));
      this.artifact = artifact;
      setGuid(artifact.getGuid());
   }

   @Override
   public String getToolTip() {
      String tt = "Type: " + artifact.getArtifactTypeName() + "\n\n" + "Title: " + artifact.getName();
      return tt;
   }

   @Override
   public String getTitle() {
      return artifact.getArtifactTypeName() + "\n" + artifact.getName();
   }

   @Override
   public Image getMarkImage() {
      return null;
   }

   public Artifact getArtifact() {
      return artifact;
   }

   @Override
   public String getShortTitle() {
      String title = getTitle().replaceFirst("State", "");
      return title;
   }

}
