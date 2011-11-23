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
package org.eclipse.osee.x.ats.core.internal.data;

import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.x.ats.data.Version;

public class VersionImpl extends AbstractAtsData implements Version {

   public VersionImpl(ReadableArtifact proxiedObject) {
      super(proxiedObject);
   }

}
