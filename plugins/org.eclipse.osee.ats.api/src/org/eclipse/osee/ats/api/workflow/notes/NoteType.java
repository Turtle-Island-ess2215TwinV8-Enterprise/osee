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
package org.eclipse.osee.ats.api.workflow.notes;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;

/**
 * @author Donald G. Dunne
 */
public enum NoteType {
   Comment,
   Question,
   Error,
   Other;

   public static NoteType getType(String type) throws OseeArgumentException {
      for (NoteType e : NoteType.values()) {
         if (e.name().equals(type)) {
            return e;
         }
      }
      throw new OseeArgumentException("Unhandled NoteType");
   }

   public static List<String> getNames() {
      List<String> names = new ArrayList<String>();
      for (NoteType e : NoteType.values()) {
         names.add(e.name());
      }
      return names;
   }

};
