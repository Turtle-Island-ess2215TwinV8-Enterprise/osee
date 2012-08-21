/*
 * Created on Aug 23, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.workdef;

import java.util.List;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetConstraint;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.ats.workflow.ATSXWidgetOptionResolver;
import org.eclipse.osee.framework.core.enums.WidgetOption;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.WidgetOptionHandler;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.util.FrameworkXWidgetProvider;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IXWidgetOptionResolver;
import org.eclipse.osee.framework.ui.skynet.widgets.util.IXWidgetRendererItem;

public class SwtAtsWidgetDefinitionRenderer implements IAtsWidgetDefinitionRender, IXWidgetRendererItem {

   private final IAtsWidgetDefinition widgetDef;
   private final WidgetOptionHandler optionHandler = new WidgetOptionHandler();
   private XWidget xWidget;

   public SwtAtsWidgetDefinitionRenderer(IAtsWidgetDefinition widgetDef) {
      this.widgetDef = widgetDef;
   }

   @Override
   public String getStoreName() {
      return widgetDef.getAtrributeName();
   }

   @Override
   public XWidget getXWidget() throws OseeCoreException {
      if (xWidget == null) {
         xWidget = FrameworkXWidgetProvider.getInstance().createXWidget(this);
      }
      return xWidget;
   }

   @Override
   public Artifact getArtifact() {
      return null;
   }

   @Override
   public void setToolTip(String description) {
   }

   @Override
   public String getName() {
      return widgetDef.getName();
   }

   @Override
   public String getUsename() {
      return widgetDef.getName();
   }

   @Override
   public String getToolTip() {
      return widgetDef.getToolTip();
   }

   @Override
   public String getDescription() {
      return widgetDef.getDescription();
   }

   @Override
   public String getAtrributeName() {
      return widgetDef.getAtrributeName();
   }

   @Override
   public String getDefaultValue() {
      return widgetDef.getDefaultValue();
   }

   @Override
   public boolean is(WidgetOption widgetOption) {
      return widgetDef.is(widgetOption);
   }

   @Override
   public List<IAtsWidgetConstraint> getConstraints() {
      return widgetDef.getConstraints();
   }

   @Override
   public void set(WidgetOption widgetOption) {
      widgetDef.set(widgetOption);
   }

   @Override
   public String getXWidgetName() {
      return widgetDef.getXWidgetName();
   }

   @Override
   public void setXWidgetName(String xWidgetName) {
      widgetDef.setXWidgetName(xWidgetName);
   }

   @Override
   public int getHeight() {
      return widgetDef.getHeight();
   }

   @Override
   public void setHeight(int height) {
      widgetDef.setHeight(height);
   }

   @Override
   public Object getObject() {
      return null;
   }

   @Override
   public WidgetOptionHandler getWidgetOptionHandler() {
      return optionHandler;
   }

   @Override
   public IXWidgetOptionResolver getOptionResolver() {
      return ATSXWidgetOptionResolver.getInstance();
   }

   @Override
   public WidgetOptionHandler getOptions() {
      return widgetDef.getOptions();
   }

   @Override
   public boolean isHeightSet() {
      return widgetDef.getHeight() != 9999;
   }

}
