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
package org.eclipse.osee.ats.core.client.internal.config;

import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.ats.api.version.IAtsVersionService;
import org.eclipse.osee.ats.core.config.IVersionFactory;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.HumanReadableId;

/**
 * @author Donald G. Dunne
 */
public class VersionFactory implements IVersionFactory {

   private final IAtsVersionService versionService;

   public VersionFactory(IAtsVersionService versionService) {
      super();
      this.versionService = versionService;
   }

   @Override
   public IAtsVersion createVersion(String title) {
      return createVersion(title, GUID.create(), HumanReadableId.generate());
   }

   @Override
   public IAtsVersion createVersion(String title, String guid, String humanReadableId) {
      return new Version(versionService, title, guid, humanReadableId);
   }

}
