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
package org.eclipse.osee.orcs.core.internal.transaction;

import java.util.List;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.orcs.core.ds.ArtifactTransactionData;
import org.eclipse.osee.orcs.core.ds.TransactionResult;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.data.ArtifactWriteable;

public interface TxDataManager {

   ArtifactWriteable getOrAddWrite(ArtifactReadable readable) throws OseeCoreException;

   void addWrite(ArtifactWriteable writeable) throws OseeCoreException;

   int size();

   void onCommitStart() throws OseeCoreException;

   void onCommitRollback() throws OseeCoreException;

   void onCommitSuccess(TransactionResult result) throws OseeCoreException;

   void onCommitEnd() throws OseeCoreException;

   List<ArtifactTransactionData> getChanges() throws OseeCoreException;
}
