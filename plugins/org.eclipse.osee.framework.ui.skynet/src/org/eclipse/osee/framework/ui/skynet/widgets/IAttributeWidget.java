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

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * Used by XWidgets that perform external data storage
 * 
 * @author Roberto E. Escobar
 */
public interface IAttributeWidget extends IArtifactStoredWidget {

   /**
    * Set attributeType used as storage for this widget
    * 
    * @throws OseeCoreException
    */
   public void setAttributeType(Artifact artifact, String attributeTypeName) throws OseeCoreException;

   /**
    * Get attributeType used as storage for this widget
    */
   public String getAttributeType();

}