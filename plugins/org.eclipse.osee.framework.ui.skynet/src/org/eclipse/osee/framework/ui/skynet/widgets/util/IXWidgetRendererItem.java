/*
 * Created on Aug 24, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.ui.skynet.widgets.util;

import org.eclipse.osee.framework.core.util.WidgetOptionHandler;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

public interface IXWidgetRendererItem {

   String getXWidgetName();

   String getName();

   String getStoreName();

   String getDefaultValue();

   String getToolTip();

   Artifact getArtifact();

   Object getObject();

   WidgetOptionHandler getWidgetOptionHandler();

   IXWidgetOptionResolver getOptionResolver();
}
