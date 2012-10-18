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
package org.eclipse.osee.ats;

import java.util.Collection;
import java.util.Collections;
import org.eclipse.osee.framework.core.operation.IOperation;
import org.eclipse.osee.framework.core.operation.OperationLogger;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;

/**
 * @author Ryan D. Brooks
 * @author David W. Miller
 */
public class PortBlam extends AbstractBlam {

   @Override
   public IOperation createOperation(VariableMap variableMap, OperationLogger logger) throws Exception {
      String items = variableMap.getString("Porting IDs");
      return new PortListOperation(logger, items);
   }

   @Override
   public Collection<String> getCategories() {
      return Collections.singletonList("Admin");
   }

   @Override
   public String getXWidgetsXml() {
      return "<xWidgets><XWidget xwidgetType=\"XText\" fill=\"Vertically\" displayName=\"Porting IDs\" /></xWidgets>";
   }
}