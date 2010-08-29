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
package org.eclipse.osee.framework.jini.service.core;

import net.jini.entry.AbstractEntry;

public abstract class FormmatedEntry extends AbstractEntry {

   private static final long serialVersionUID = -8845417112982132038L;

   public abstract String getFormmatedString();
}
