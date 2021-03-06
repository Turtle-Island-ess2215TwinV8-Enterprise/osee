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
package org.eclipse.osee.ats.client.demo;

import java.util.Arrays;
import java.util.List;
import org.eclipse.osee.framework.core.data.IUserToken;
import org.eclipse.osee.framework.core.data.TokenFactory;

/**
 * @author Donald G. Dunne
 */
public final class DemoUsers {

   // @formatter:off
   public static final IUserToken Joe_Smith = TokenFactory.createUserToken("ABNRvbZxXHICYklfslwA", "Joe Smith", "", "Joe Smith", false, false, false);
   public static final IUserToken Kay_Jones = TokenFactory.createUserToken("ABNRvuB8x3VARkkn3YAA", "Kay Jones", "", "Kay Jones", false, false, false);
   public static final IUserToken Jason_Michael = TokenFactory.createUserToken("ABNRvuHWtXAdxbG3mUAA", "Jason Michael", "", "Jason Michael", true, false, false);
   public static final IUserToken Alex_Kay = TokenFactory.createUserToken("ABNRvuKDIWOcPDe4X0wA", "Alex Kay", "", "Alex Kay", true, false, false);
   public static final IUserToken Inactive_Steve = TokenFactory.createUserToken("ABNRvuRG6jKwKnEoX4gA", "Inactive Steve", "", "Inactive Steve", false, false, false);
   public static List<IUserToken> values = Arrays.asList(Joe_Smith, Kay_Jones, Jason_Michael, Alex_Kay, Inactive_Steve);
   // @formatter:on

   private DemoUsers() {
      // Constants
   }

   public static List<IUserToken> values() {
      return values;
   }

}
