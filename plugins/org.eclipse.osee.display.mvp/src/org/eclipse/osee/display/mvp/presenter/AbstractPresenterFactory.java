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
package org.eclipse.osee.display.mvp.presenter;

import java.util.Collection;
import org.eclipse.osee.display.mvp.event.EventBus;
import org.eclipse.osee.display.mvp.view.View;
import org.eclipse.osee.logger.Log;

/**
 * @author Roberto E. Escobar
 */
public abstract class AbstractPresenterFactory implements PresenterFactory {

   private Log logger;

   public void setLogger(Log logger) {
      this.logger = logger;
   }

   public Log getLogger() {
      return logger;
   }

   @Override
   public boolean canCreate(Class<? extends Presenter<? extends View, ? extends EventBus>> presenterType) {
      return true;
   }

   protected boolean implementsType(Class<? extends Presenter<? extends View, ? extends EventBus>> concreteType, Class<? extends Presenter<? extends View, ? extends EventBus>> interfaceType) {
      return interfaceType.isAssignableFrom(concreteType);
   }

   protected Class<? extends AbstractPresenter<?, ?>> findPresenter(Collection<Class<? extends AbstractPresenter<?, ?>>> supportedTypes, Class<? extends Presenter<?, ?>> presenterType) {
      Class<? extends AbstractPresenter<?, ?>> toReturn = null;
      for (Class<? extends AbstractPresenter<?, ?>> presenterClazz : supportedTypes) {
         if (implementsType(presenterClazz, presenterType)) {
            toReturn = presenterClazz;
            break;
         }
      }
      return toReturn;
   }
}
