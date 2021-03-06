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
package org.eclipse.osee.orcs.db.mocks;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.eclipse.osee.executor.admin.ExecutionCallback;
import org.eclipse.osee.executor.admin.ExecutorAdmin;

/**
 * @author Roberto E. Escobar
 */
public class MockExecutorAdmin implements ExecutorAdmin {

   @Override
   public <T> Future<T> schedule(Callable<T> callable, ExecutionCallback<T> callback) throws Exception {
      return null;
   }

   @Override
   public <T> Future<T> schedule(String id, Callable<T> callable, ExecutionCallback<T> callback) throws Exception {
      return null;
   }

   @Override
   public <T> Future<T> schedule(Callable<T> callable) throws Exception {
      return null;
   }

   @Override
   public <T> Future<T> schedule(String id, Callable<T> callable) throws Exception {
      return null;
   }

   @Override
   public int cancelTasks(String id) throws Exception {
      return 0;
   }

}
