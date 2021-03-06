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
package org.eclipse.osee.orcs.core.ds;

import java.util.List;
import org.eclipse.osee.framework.core.data.Identity;

public interface ArtifactTransactionData extends Identity<String>, OrcsVisitable {

   @Override
   String getGuid();

   ArtifactData getArtifactData();

   List<AttributeData> getAttributeData();

   List<RelationData> getRelationData();

}
