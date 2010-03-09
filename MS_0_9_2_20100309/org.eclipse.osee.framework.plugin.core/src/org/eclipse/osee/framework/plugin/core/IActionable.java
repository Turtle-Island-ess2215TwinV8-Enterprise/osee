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
package org.eclipse.osee.framework.plugin.core;

/**
 * @author Donald G. Dunne
 */
public interface IActionable {

   /**
    * Provide description over and above the viewpart name and version to be used by ATS
    * 
    * @return description
    */
   public String getActionDescription();

}
