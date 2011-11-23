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
package org.eclipse.osee.x.ats;

import java.util.List;
import org.eclipse.osee.x.ats.data.Action;
import org.eclipse.osee.x.ats.data.Goal;
import org.eclipse.osee.x.ats.data.HasTasks;
import org.eclipse.osee.x.ats.data.Product;
import org.eclipse.osee.x.ats.data.Review;
import org.eclipse.osee.x.ats.data.Task;
import org.eclipse.osee.x.ats.data.Version;

/**
 * @author Roberto E. Escobar
 */
public interface AtsGraph {

   List<Task> getTasks(Review review) throws AtsException;

   List<Task> getTasks(Goal goal) throws AtsException;

   List<Task> getTasks(Action action) throws AtsException;

   HasTasks getContainer(Task task) throws AtsException;

   List<Version> getVersions(Product product) throws AtsException;

   Version getTargetedVersion(Action action) throws AtsException;

   Product getProduct(Action action) throws AtsException;

   List<Product> getProducts() throws AtsException;

}
