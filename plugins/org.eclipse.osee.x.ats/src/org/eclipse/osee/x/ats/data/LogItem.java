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
package org.eclipse.osee.x.ats.data;

import java.util.Date;
import org.eclipse.osee.ats.shared.LogType;

/**
 * @author Roberto E. Escobar
 */
public interface LogItem {

   Date getDate();

   String getUserId();

   String getMessage();

   LogType getType();

   String getState();

}
