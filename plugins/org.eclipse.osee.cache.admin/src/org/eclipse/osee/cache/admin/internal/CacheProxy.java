/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.cache.admin.internal;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.eclipse.osee.cache.admin.Cache;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeExceptions;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * @author John Misinco
 * @author Roberto E. Escobar
 */
public class CacheProxy<K, V> implements Cache<K, V> {

   private final com.google.common.cache.Cache<K, V> proxied;

   public CacheProxy(com.google.common.cache.Cache<K, V> proxied) {
      this.proxied = proxied;
   }

   @Override
   public V getIfPresent(K key) {
      return proxied.getIfPresent(key);
   }

   @Override
   public Iterable<V> getAllPresent() {
      Iterable<? extends K> keys = getAllKeysPresent();
      return getIfPresent(keys).values();
   }

   @Override
   public Iterable<? extends K> getAllKeysPresent() {
      return Iterables.unmodifiableIterable(proxied.asMap().keySet());
   }

   @Override
   public Iterable<? extends K> getAllKeys() {
      return getAllKeysPresent();

   }

   @Override
   public Map<K, V> getIfPresent(Iterable<? extends K> keys) {
      return proxied.getAllPresent(keys);
   }

   @Override
   public Map<K, V> get(Iterable<? extends K> keys) throws OseeCoreException {
      ImmutableMap<K, V> items = null;
      try {
         items = proxied.getAllPresent(keys);
      } catch (InvalidCacheLoadException ex) {
         OseeExceptions.wrapAndThrow(ex);
      } catch (UncheckedExecutionException ex) {
         OseeExceptions.wrapAndThrow(ex);
      } catch (ExecutionError ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return items;
   }

   @Override
   public Iterable<V> getAll() throws OseeCoreException {
      Iterable<? extends K> allKeys = getAllKeys();
      return get(allKeys).values();
   }

   @Override
   public V get(K key) throws OseeCoreException {
      V toReturn = null;
      try {
         toReturn = proxied.getIfPresent(key);
      } catch (InvalidCacheLoadException ex) {
         OseeExceptions.wrapAndThrow(ex);
      } catch (UncheckedExecutionException ex) {
         OseeExceptions.wrapAndThrow(ex);
      } catch (ExecutionError ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return toReturn;
   }

   @Override
   public void refresh(K key) {
      // Does nothing;
   }

   @Override
   public void invalidateAll() {
      proxied.invalidateAll();
   }

   @Override
   public void invalidate(Iterable<? extends K> keys) {
      proxied.invalidateAll(keys);
   }

   @Override
   public void invalidate(K key) {
      proxied.invalidate(key);
   }

   @Override
   public long size() {
      return proxied.size();
   }

   @Override
   public V get(K key, Callable<? extends V> callable) throws OseeCoreException {
      V toReturn = null;
      try {
         toReturn = proxied.get(key, callable);
      } catch (ExecutionException ex) {
         OseeExceptions.wrapAndThrow(ex);
      } catch (InvalidCacheLoadException ex) {
         OseeExceptions.wrapAndThrow(ex);
      } catch (UncheckedExecutionException ex) {
         OseeExceptions.wrapAndThrow(ex);
      } catch (ExecutionError ex) {
         OseeExceptions.wrapAndThrow(ex);
      }
      return toReturn;
   }

   @Override
   public boolean isEmpty() {
      return proxied.size() == 0;
   }

}