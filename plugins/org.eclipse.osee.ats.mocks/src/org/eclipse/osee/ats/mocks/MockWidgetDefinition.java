package org.eclipse.osee.ats.mocks;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetConstraint;
import org.eclipse.osee.ats.api.workdef.IAtsWidgetDefinition;
import org.eclipse.osee.framework.core.enums.WidgetOption;
import org.eclipse.osee.framework.core.util.WidgetOptionHandler;

/**
 * @author Donald G. Dunne
 */
public class MockWidgetDefinition implements IAtsWidgetDefinition {

   private String attributeName;
   private String toolTip;
   private String description;
   private int height;
   private String xWidgetName;
   private String defaultValue;
   private final WidgetOptionHandler options = new WidgetOptionHandler();
   private final List<IAtsWidgetConstraint> constraints = new ArrayList<IAtsWidgetConstraint>();
   private String name;

   public MockWidgetDefinition(String name) {
      this.name = name;
   }

   @Override
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   @Override
   public String getAtrributeName() {
      return attributeName;
   }

   public void setAttributeName(String storeName) {
      this.attributeName = storeName;
   }

   @Override
   public String getToolTip() {
      return toolTip;
   }

   public void setToolTip(String toolTip) {
      this.toolTip = toolTip;
   }

   @Override
   public boolean is(WidgetOption widgetOption) {
      return options.contains(widgetOption);
   }

   @Override
   public void set(WidgetOption widgetOption) {
      options.add(widgetOption);
   }

   @Override
   public String getXWidgetName() {
      return xWidgetName;
   }

   @Override
   public void setXWidgetName(String xWidgetName) {
      this.xWidgetName = xWidgetName;
   }

   @Override
   public String getDefaultValue() {
      return defaultValue;
   }

   public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
   }

   @Override
   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   @Override
   public int getHeight() {
      return height;
   }

   @Override
   public void setHeight(int height) {
      this.height = height;
   }

   @Override
   public String toString() {
      return String.format("[%s][%s]", getName(), getAtrributeName());
   }

   @Override
   public List<IAtsWidgetConstraint> getConstraints() {
      return constraints;
   }

   @Override
   public String getUsename() {
      return name;
   }

   @Override
   public WidgetOptionHandler getOptions() {
      return options;
   }

}
