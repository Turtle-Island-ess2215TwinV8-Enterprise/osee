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
package org.eclipse.osee.framework.server.admin.management;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.server.admin.BaseServerCommand;
import org.eclipse.osgi.framework.console.CommandInterpreter;

/**
 * @author Ryan D. Brooks
 */
public class GarbageCollectionCommand extends BaseServerCommand {

   public GarbageCollectionCommand(CommandInterpreter ci) {
      super("Run Garbage Collection", ci);
   }

   @Override
   protected void doCommandWork(IProgressMonitor monitor) throws Exception {
      System.gc();
   }
}