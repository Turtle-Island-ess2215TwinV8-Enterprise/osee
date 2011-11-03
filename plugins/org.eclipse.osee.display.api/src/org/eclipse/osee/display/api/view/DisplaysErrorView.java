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
package org.eclipse.osee.display.api.view;

/**
 * @author John Misinco
 */
public interface DisplaysErrorView {
   public enum MsgType {
      MSGTYPE_ERROR,
      MSGTYPE_WARNING;
   }

   void setErrorMessage(String shortMsg, String longMsg, MsgType msgType);

}
