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
package org.eclipse.osee.framework.server.admin.management;

import java.util.Arrays;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.server.admin.BaseServerCommand;
import org.eclipse.osee.framework.server.admin.internal.Activator;

/**
 * @author Roberto E. Escobar
 */
public class RemoveServerVersionWorker extends BaseServerCommand {

   protected RemoveServerVersionWorker() {
      super("Remove Version");
   }

   @Override
   protected void doCommandWork(IProgressMonitor monitor) throws Exception {
      String versionToRemove = getCommandInterpreter().nextArgument();
      Activator.getInstance().getApplicationServerManager().removeSupportedVersion(versionToRemove);
      StringBuffer buffer = new StringBuffer();
      buffer.append("Osee Application Server: ");
      buffer.append(Arrays.deepToString(Activator.getInstance().getApplicationServerManager().getSupportedVersions()));
      buffer.append("\n");
      println(buffer.toString());
   }
}
