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
package org.eclipse.nebula.widgets.xviewer.customize;

import java.util.List;
import org.eclipse.nebula.widgets.xviewer.util.XViewerException;

/**
 * Methods to implement if this XViewer allows the user to save local/global customizations
 * 
 * @author Donald G. Dunne
 */
public interface IXViewerCustomizations {

   public void saveCustomization(CustomizeData custData) throws Exception;

   public List<CustomizeData> getSavedCustDatas() throws Exception;

   public CustomizeData getUserDefaultCustData() throws XViewerException;

   public boolean isCustomizationUserDefault(CustomizeData custData);

   public void setUserDefaultCustData(CustomizeData newCustData, boolean set) throws Exception;

   public void deleteCustomization(CustomizeData custData) throws Exception;

   public boolean isCustomizationPersistAvailable();
}
