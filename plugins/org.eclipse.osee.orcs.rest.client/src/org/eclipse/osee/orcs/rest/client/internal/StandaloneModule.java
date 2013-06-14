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
package org.eclipse.osee.orcs.rest.client.internal;

import org.eclipse.osee.framework.core.services.URIProvider;
import org.eclipse.osee.orcs.rest.client.OseeClient;
import org.eclipse.osee.orcs.rest.client.OseeClientConfig;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * @author Roberto E. Escobar
 */
public class StandaloneModule extends AbstractModule {

   private final OseeClientConfig config;

   public StandaloneModule(OseeClientConfig config) {
      this.config = config;
   }

   @Override
   protected void configure() {
      bindConstant().annotatedWith(OseeServerAddress.class).to(config.getServerAddress());
      bindConstant().annotatedWith(OseeHttpProxyAddress.class).to(config.getProxyAddress());

      bind(OseeClient.class).to(OseeClientImpl.class);
      bind(WebClientProvider.class).to(StandadloneWebClientProvider.class);
      bind(URIProvider.class).to(StandadloneUriProviderImpl.class);

      TypeListener listener = new TypeListener() {

         @Override
         public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
            encounter.register(new InjectionListener<I>() {

               @Override
               public void afterInjection(I injectee) {
                  OseeClientImpl client = (OseeClientImpl) injectee;
                  client.start();
               }
            });
         }
      };
      bindListener(subtypeOf(OseeClientImpl.class), listener);
   }

   private static Matcher<? super TypeLiteral<?>> subtypeOf(Class<?> superclass) {
      return new SubTypeOfMatcher(TypeLiteral.get(OseeClientImpl.class));
   }

   private static final class SubTypeOfMatcher extends AbstractMatcher<TypeLiteral<?>> {
      private final TypeLiteral<OseeClientImpl> superType;

      public SubTypeOfMatcher(TypeLiteral<OseeClientImpl> superType) {
         super();
         this.superType = superType;
      }

      @Override
      public boolean matches(TypeLiteral<?> subType) {
         return subType.equals(superType) || superType.getRawType().isAssignableFrom(subType.getRawType());
      }

   }
}
