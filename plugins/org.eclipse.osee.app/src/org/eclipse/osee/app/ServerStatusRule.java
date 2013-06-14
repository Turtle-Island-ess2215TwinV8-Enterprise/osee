/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.app;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Donald G. Dunne
 */
public final class ServerStatusRule extends AppendableRule {

   public ServerStatusRule(String ruleName) {
      super(ruleName);
   }

   private List<String> getPorts() {
      // TODO How should we get valid set of servers to check.  Potentially make rest call to server to return server_lookup table.
      return Arrays.asList("8080", "12000", "12002", "8019", "8091", "8021", "12004");
   }

   @Override
   public void applyTo(Appendable appendable) throws IOException {
      appendable.append("<ul>");
      for (String port : getPorts()) {
         appendable.append("<li>Server ");
         appendable.append(port);
         appendable.append(" is <strong id=\"serverStatus\">???</strong></li>");
      }
      appendable.append("</ul>");
   }
}