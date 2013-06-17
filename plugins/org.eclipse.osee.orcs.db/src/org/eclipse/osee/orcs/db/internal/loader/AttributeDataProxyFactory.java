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
package org.eclipse.osee.orcs.db.internal.loader;

import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Conditions;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.orcs.core.ds.DataProxy;
import org.eclipse.osee.orcs.core.ds.DataProxyFactory;
import org.eclipse.osee.orcs.core.ds.ProxyDataFactory;

/**
 * @author Roberto E. Escobar
 */
public class AttributeDataProxyFactory implements ProxyDataFactory {

   private final DataProxyFactoryProvider proxyProvider;

   public AttributeDataProxyFactory(DataProxyFactoryProvider proxyProvider) {
      super();
      this.proxyProvider = proxyProvider;
   }

   @Override
   public DataProxy createProxy(String dataProxyFactoryId, boolean isEnumOrBoolean, long typeUuid, String value, String uri) throws OseeCoreException {
      if (dataProxyFactoryId.contains(".")) {
         dataProxyFactoryId = Lib.getExtension(dataProxyFactoryId);
      }

      DataProxyFactory factory = proxyProvider.getFactory(dataProxyFactoryId);
      Conditions.checkNotNull(factory, "DataProxyFactory", "Unable to find data proxy factory for [%s]",
         dataProxyFactoryId);

      String checkedValue = intern(isEnumOrBoolean, value);
      DataProxy proxy = factory.createInstance(dataProxyFactoryId);
      proxy.setData(checkedValue, uri);
      return proxy;
   }

   private String intern(boolean isEnumOrBoolean, String original) {
      String value = original;
      if (isEnumOrBoolean) {
         value = intern(value);
      }
      return value;
   }

   protected String intern(String value) {
      return Strings.intern(value);
   }

   @Override
   public DataProxy createProxy(String dataProxyFactoryId, boolean isEnumOrBoolean, long typeUuid, Object... data) throws OseeCoreException {
      Conditions.checkNotNull(data, "data");
      Conditions.checkExpressionFailOnTrue(data.length < 2, "Data must have at least [2] elements - size was [%s]",
         data.length);

      String value = (String) data[0];
      String uri = (String) data[1];
      return createProxy(dataProxyFactoryId, isEnumOrBoolean, typeUuid, value, uri);
   }
}
