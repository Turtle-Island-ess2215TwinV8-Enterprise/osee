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
package org.eclipse.osee.framework.skynet.core.utility;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.internal.Activator;

/**
 * @author Ken J. Aguilar
 */
public class FileWatcher extends TimerTask {
   private final long interval;
   private final Timer timer = new Timer();

   protected final HashMap<File, Long> filesToWatch = new HashMap<File, Long>(128);
   private final HashSet<IFileWatcherListener> listeners = new HashSet<IFileWatcherListener>();

   public FileWatcher(long time, TimeUnit unit) {
      interval = unit.toMillis(time);
   }

   /**
    * Starts the file watcher monitoring of the file system
    */
   public void start() {
      timer.schedule(this, interval, interval);
   }

   /**
    * adds a {@link File} to the files to be monitored. This method can be called before or after the {@link #start()}
    * method is called.
    */
   public synchronized void addFile(File file) {
      filesToWatch.put(file, file.lastModified());
   }

   /**
    * removes a {@link File} from the set of files to be monitored. This method can be called before or after the
    * {@link #start()} method is called.
    * 
    * @return returns the last know timestamp of the file before it was removed or null if it was never being monitored
    * in the first place
    */
   public synchronized Long removeFile(File file) {
      return filesToWatch.remove(file);
   }

   /**
    * registers a listener who will be notified of file change events. This method can be called before or after the
    * {@link #start()} method is called.
    */
   public synchronized void addListener(IFileWatcherListener listener) {
      listeners.add(listener);
   }

   /**
    * unregisters a listener from receiving file change events. This method can be called before or after the
    * {@link #start()} method is called.
    */
   public synchronized void removeListener(IFileWatcherListener listener) {
      listeners.remove(listener);
   }

   @Override
   public synchronized void run() {
      try {
         LinkedList<FileChangeEvent> fileChangeEvents = new LinkedList<FileChangeEvent>();
         for (Map.Entry<File, Long> entry : filesToWatch.entrySet()) {
            Long latestLastModified = entry.getKey().lastModified();
            Long storedLastModified = entry.getValue();
            if (!storedLastModified.equals(latestLastModified)) {
               entry.setValue(latestLastModified);
               if (storedLastModified == 0) {
                  // created
                  assert entry.getKey().exists() : "file doesn't exist";
                  fileChangeEvents.add(new FileChangeEvent(entry.getKey(), FileChangeType.CREATED));
               } else if (latestLastModified == 0) {
                  // deleted
                  assert !entry.getKey().exists() : "file still exist";
                  fileChangeEvents.add(new FileChangeEvent(entry.getKey(), FileChangeType.DELETED));
               } else {
                  // modified
                  assert entry.getKey().exists() : "file doesn't exist";
                  fileChangeEvents.add(new FileChangeEvent(entry.getKey(), FileChangeType.MODIFIED));
               }

            }
         }
         if (!fileChangeEvents.isEmpty()) {
            // there is at least one file change event, notify listeners
            for (IFileWatcherListener listener : listeners) {
               listener.filesModified(fileChangeEvents);
            }
         }
      } catch (Exception ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
      }
   }

   public void dispose() {
      timer.cancel();
      listeners.clear();
      filesToWatch.clear();
   }

}