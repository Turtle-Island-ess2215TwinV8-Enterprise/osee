/*******************************************************************************
 * Copyright (c) 2012 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.framework.core.enums;


/**
 * @author John Misinco
 */
public interface OptionVisitor {

   void asCaseType(CaseType option);

   void asTokenOrderType(TokenOrderType option);

   void asMatchTokenCountType(MatchTokenCountType option);

   void asTokenDelimiterMatch(TokenDelimiterMatch option);
}
