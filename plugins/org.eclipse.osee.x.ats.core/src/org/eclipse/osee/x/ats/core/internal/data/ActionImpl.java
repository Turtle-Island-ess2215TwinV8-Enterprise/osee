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
package org.eclipse.osee.x.ats.core.internal.data;

import java.util.Collection;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.x.ats.AtsException;
import org.eclipse.osee.x.ats.AtsGraph;
import org.eclipse.osee.x.ats.data.Action;
import org.eclipse.osee.x.ats.data.Product;
import org.eclipse.osee.x.ats.data.Task;
import org.eclipse.osee.x.ats.data.Version;
import org.eclipse.osee.x.ats.data.WorkHistory;

public class ActionImpl extends AbstractAtsData implements Action {

   private final AtsGraph atsGraph;

   public ActionImpl(ReadableArtifact proxiedObject, AtsGraph atsGraph) {
      super(proxiedObject);
      this.atsGraph = atsGraph;
   }

   @Override
   public Version getTargetedVersion() {
      return null;
   }

   @Override
   public Product getProduct() {
      return null;
   }

   @Override
   public WorkHistory getHistory() {
      return null;
   }

   @Override
   public Collection<Task> getTasks() throws AtsException {
      return atsGraph.getTasks(this);
   }

}
