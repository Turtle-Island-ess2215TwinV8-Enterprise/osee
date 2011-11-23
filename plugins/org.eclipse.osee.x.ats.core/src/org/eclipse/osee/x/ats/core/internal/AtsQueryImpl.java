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
package org.eclipse.osee.x.ats.core.internal;

import java.util.Collection;
import java.util.concurrent.Callable;
import org.eclipse.osee.framework.core.data.ResultSet;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.eclipse.osee.x.ats.AtsException;
import org.eclipse.osee.x.ats.data.WorkUnit;
import org.eclipse.osee.x.ats.query.AtsQuery;
import org.eclipse.osee.x.ats.query.WorkUnitType;

/**
 * @author Shawn F. Cook
 */
public class AtsQueryImpl implements AtsQuery {

   private final QueryFactory factory;

   public AtsQueryImpl(QueryFactory factory) {
      this.factory = factory;
   }

   @Override
   public AtsQuery andProductId(String id) {
      return null;
   }

   @Override
   public AtsQuery andVersionId(String id) {
      return null;
   }

   @Override
   public AtsQuery andProductName(String productName) {
      return null;
   }

   @Override
   public AtsQuery andVersionName(String versionName) {
      return null;
   }

   @Override
   public AtsQuery andStates(String... states) {
      return null;
   }

   @Override
   public AtsQuery andWorkUnitId(String... id) {
      return null;
   }

   @Override
   public AtsQuery andWorkUnitIds(Collection<String>... ids) {
      return null;
   }

   @Override
   public AtsQuery andStates(Collection<String>... states) {
      return null;
   }

   @Override
   public AtsQuery andWorkType(WorkUnitType... type) {
      return null;
   }

   @Override
   public AtsQuery resetToDefaults() {
      return null;
   }

   @Override
   public <T extends WorkUnit> Callable<ResultSet<T>> searchWorkUnits() throws AtsException {
      return null;
   }
}
