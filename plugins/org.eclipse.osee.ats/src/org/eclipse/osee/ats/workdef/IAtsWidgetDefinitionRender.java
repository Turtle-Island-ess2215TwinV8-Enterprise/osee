/*
 * Created on Aug 23, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.workdef;

import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.WidgetOptionHandler;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;

public interface IAtsWidgetDefinitionRender extends IAtsWidgetDefinition {

   String getStoreName();

   XWidget getXWidget() throws OseeCoreException;

   Artifact getArtifact();

   void setToolTip(String description);

   WidgetOptionHandler getWidgetOptionHandler();

   boolean isHeightSet();
}
