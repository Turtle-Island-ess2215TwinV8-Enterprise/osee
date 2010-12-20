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
package org.eclipse.osee.framework.jdk.core.test.mock;

import java.util.Collection;
import org.eclipse.osee.framework.jdk.core.util.io.FileChangeEvent;
import org.eclipse.osee.framework.jdk.core.util.io.IFileWatcherListener;

/**
 * @author Roberto E. Escobar
 */
public class MockFileWatcherListener implements IFileWatcherListener {

   private int fileModifiedCallCount = 0;
   private int handleExceptionCallCount = 0;
   private Exception ex;
   private Collection<FileChangeEvent> fileChangeEvents;

   @Override
   public void filesModified(Collection<FileChangeEvent> fileChangeEvents) {
      fileModifiedCallCount++;
      this.fileChangeEvents = fileChangeEvents;
      synchronized (this) {
         notify();
      }
   }

   @Override
   public void handleException(Exception ex) {
      handleExceptionCallCount++;
      this.ex = ex;
      synchronized (this) {
         notify();
      }
   }

   public int getFileModifiedCallCount() {
      return fileModifiedCallCount;
   }

   public int getHandleExceptionCallCount() {
      return handleExceptionCallCount;
   }

   public Exception getEx() {
      return ex;
   }

   public Collection<FileChangeEvent> getFileChangeEvents() {
      return fileChangeEvents;
   }

   public void clear() {
      fileModifiedCallCount = 0;
      handleExceptionCallCount = 0;
      ex = null;
      fileChangeEvents = null;
   }
}