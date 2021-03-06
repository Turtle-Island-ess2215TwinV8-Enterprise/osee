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
package org.eclipse.osee.executor.admin;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author Roberto E. Escobar
 */
public interface ExecutorAdmin {

   //   ExecutorService getDefaultExecutor() throws Exception;
   //
   //   ExecutorService getExecutor(String name) throws Exception;
   //
   //   <T> Callable<T> addCallback(Callable<T> callable, ExecutionCallback<T> callback);

   <T> Future<T> schedule(Callable<T> callable, ExecutionCallback<T> callback) throws Exception;

   <T> Future<T> schedule(String id, Callable<T> callable, ExecutionCallback<T> callback) throws Exception;

   <T> Future<T> schedule(Callable<T> callable) throws Exception;

   <T> Future<T> schedule(String id, Callable<T> callable) throws Exception;

   int cancelTasks(String id) throws Exception;
}
