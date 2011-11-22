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
package org.eclipse.osee.x.ats.query;

import java.util.Collection;
import java.util.concurrent.Callable;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.x.ats.AtsException;
import org.eclipse.osee.x.ats.data.WorkUnit;

/**
 * @author Roberto E. Escobar
 */
public interface AtsQuery {

   AtsQuery andProductId(String id);

   AtsQuery andVersionId(String id);

   AtsQuery andProductName(String productName);

   AtsQuery andVersionName(String versionName);

   AtsQuery andWorkUnitId(String... id);

   AtsQuery andWorkUnitIds(Collection<String>... ids);

   AtsQuery andStates(String... states);

   AtsQuery andStates(Collection<String>... states);

   AtsQuery andWorkType(WorkUnitType... type);

   /**
    * Resets query to default settings.
    */
   AtsQuery resetToDefaults();

   <T extends WorkUnit> Callable<ResultSet<T>> searchWorkUnits() throws AtsException;

}
