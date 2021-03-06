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
package org.eclipse.osee.coverage.model;

import java.util.Date;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.util.AHTML;
import org.eclipse.osee.framework.jdk.core.util.DateUtil;
import org.eclipse.osee.framework.jdk.core.util.GUID;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.KeyValueArtifact;

public class CoveragePackage extends CoveragePackageBase {

   Date creationDate;
   IWorkProductTaskProvider workProductTaskProvider;

   public CoveragePackage(String guid, String name, CoverageOptionManager coverageOptionManager, IWorkProductTaskProvider workProductTaskProvider) {
      this(guid, name, new Date(), coverageOptionManager, workProductTaskProvider);
   }

   public CoveragePackage(String name, CoverageOptionManager coverageOptionManager, IWorkProductTaskProvider workProductTaskProvider) {
      this(GUID.create(), name, coverageOptionManager, workProductTaskProvider);
   }

   public CoveragePackage(String guid, String name, Date runDate, CoverageOptionManager coverageOptionManager, IWorkProductTaskProvider workProductTaskProvider) {
      super(guid, name, coverageOptionManager);
      this.workProductTaskProvider = workProductTaskProvider;
      this.workProductTaskProvider.setCoveragePackage(this);
      this.creationDate = runDate;
   }

   public void clearCoverageUnits() {
      coverageUnits.clear();
   }

   public Date getRunDate() {
      return creationDate;
   }

   @Override
   public void getOverviewHtmlHeader(XResultData xResultData) {
      xResultData.log(AHTML.bold("Coverage Package " + getName() + " as of " + DateUtil.getMMDDYYHHMM(new Date())) + AHTML.newline());
   }

   public void setCreationDate(Date creationDate) {
      this.creationDate = creationDate;
   }

   @Override
   public ICoverage getCoverage(String guid) {
      for (ICoverage coverage : getChildren(true)) {
         if (coverage.getGuid().equals(guid)) {
            return coverage;
         }
      }
      return null;
   }

   @Override
   public void saveKeyValues(KeyValueArtifact keyValueArtifact) {
      keyValueArtifact.setValue("date", String.valueOf(creationDate.getTime()));
   }

   @Override
   public void loadKeyValues(KeyValueArtifact keyValueArtifact) {
      if (Strings.isValid(keyValueArtifact.getValue("date"))) {
         Date date = new Date();
         date.setTime(new Long(keyValueArtifact.getValue("date")).longValue());
         setCreationDate(date);
      }
   }

   @Override
   public Date getDate() {
      return getRunDate();
   }

   @Override
   public String getWorkProductTaskStr() {
      return "";
   }

   public IWorkProductTaskProvider getWorkProductTaskProvider() {
      return workProductTaskProvider;
   }

   @Override
   public int hashCode() {
      return super.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return super.equals(obj);
   }

   @Override
   public String toString() {
      return getName();
   }

   @Override
   public String toStringNoPackage() {
      return getName();
   }

}
