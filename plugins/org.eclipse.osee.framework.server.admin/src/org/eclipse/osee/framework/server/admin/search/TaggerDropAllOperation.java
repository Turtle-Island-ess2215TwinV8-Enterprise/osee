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
package org.eclipse.osee.framework.server.admin.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.operation.AbstractOperation;
import org.eclipse.osee.framework.core.operation.OperationLogger;
import org.eclipse.osee.framework.database.core.ConnectionHandler;
import org.eclipse.osee.framework.server.admin.internal.Activator;

/**
 * @author Roberto E. Escobar
 */
public final class TaggerDropAllOperation extends AbstractOperation {

   public TaggerDropAllOperation(OperationLogger logger) {
      super("Drop All Search Tags", Activator.PLUGIN_ID, logger);
   }

   @Override
   protected void doWork(IProgressMonitor monitor) throws OseeCoreException {
      ConnectionHandler.runPreparedUpdate("TRUNCATE osee_search_tags");
   }
}