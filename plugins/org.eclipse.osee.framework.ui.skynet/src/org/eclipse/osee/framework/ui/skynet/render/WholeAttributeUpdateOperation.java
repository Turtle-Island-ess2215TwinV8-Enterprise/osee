/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.ui.skynet.render;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.SkynetGuiPlugin;

/**
 * @author Ryan D. Brooks
 */
public class WholeAttributeUpdateOperation extends AbstractOperation {
   private final Artifact artifact;
   private final IAttributeType attributeType;
   private final File file;

   public WholeAttributeUpdateOperation(File file, Artifact artifact, IAttributeType attributeType) {
      super("Native Artifact Update", SkynetGuiPlugin.PLUGIN_ID);
      this.artifact = artifact;
      this.attributeType = attributeType;
      this.file = file;
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws Exception {
      InputStream stream = null;
      try {
         stream = new BufferedInputStream(new FileInputStream(file));
         artifact.setSoleAttributeFromStream(attributeType, stream);
         artifact.persist();
      } finally {
         Lib.close(stream);
      }
   }
}