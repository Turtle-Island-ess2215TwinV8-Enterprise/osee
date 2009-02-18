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
package org.eclipse.osee.framework.ui.data.model.editor.command;

import org.eclipse.gef.commands.Command;
import org.eclipse.osee.framework.ui.data.model.editor.model.DataType;

/**
 * @author Roberto E. Escobar
 */
public class ChangeNameCommand extends Command {

   private DataType element;
   private String newName, oldName;

   public ChangeNameCommand(DataType dataType, String newName) {
      super("Change Name");
      element = dataType;
      oldName = dataType.getName();
      this.newName = newName.trim();
   }

   public boolean canExecute() {
      return element != null;
   }

   public void execute() {
      element.setName(newName);
   }

   public void undo() {
      element.setName(oldName);
   }

}
