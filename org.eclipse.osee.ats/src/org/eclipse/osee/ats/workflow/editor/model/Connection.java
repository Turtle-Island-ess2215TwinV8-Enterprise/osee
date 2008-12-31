/*******************************************************************************
 * Copyright (c) 2004, 2005 Donald G. Dunne and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Donald G. Dunne - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.workflow.editor.model;

import org.eclipse.draw2d.Graphics;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * A connection between two distinct shapes.
 * 
 * @author Donald G. Dunne
 */
public class Connection extends ModelElement {

   /** Property ID to use when the line style of this connection is modified. */
   public static final String TYPE_PROP = "Type";
   private static final IPropertyDescriptor[] descriptors = new IPropertyDescriptor[1];

   /** True, if the connection is attached to its endpoints. */
   private boolean isConnected;
   private int lineStyle = Graphics.LINE_SOLID;
   /** Connection's source endpoint. */
   private Shape source;
   /** Connection's target endpoint. */
   private Shape target;

   static {
      descriptors[0] = new TextPropertyDescriptor(TYPE_PROP, TYPE_PROP);
   }

   /**
    * Create a (solid) connection between two distinct shapes.
    * 
    * @param source a source endpoint for this connection (non null)
    * @param target a target endpoint for this connection (non null)
    * @throws IllegalArgumentException if any of the parameters are null or source == target
    * @see #setLineStyle(int)
    */
   public Connection(Shape source, Shape target) {
      reconnect(source, target);
   }

   public Color getForegroundColor() {
      return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
   }

   @Override
   public String toString() {
      return "Transition: " + getPropertyValue(TYPE_PROP);
   }

   /**
    * Disconnect this connection from the shapes it is attached to.
    */
   public void disconnect() {
      if (isConnected) {
         source.removeConnection(this);
         target.removeConnection(this);
         isConnected = false;
      }
   }

   /**
    * Returns the line drawing style of this connection.
    * 
    * @return an int value (Graphics.LINE_DASH or Graphics.LINE_SOLID)
    */
   public int getLineStyle() {
      return lineStyle;
   }

   /* (non-Javadoc)
    * @see org.eclipse.osee.ats.workflow.editor.model.ModelElement#validForSave()
    */
   @Override
   public Result validForSave() {
      System.err.println("Add Connection validations.");
      return Result.TrueResult;
   }

   /**
    * Returns the descriptor for the lineStyle property
    * 
    * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
    */
   @Override
   public IPropertyDescriptor[] getPropertyDescriptors() {
      return descriptors;
   }

   /**
    * Returns the lineStyle as String for the Property Sheet
    * 
    * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java.lang.Object)
    */
   @Override
   public Object getPropertyValue(Object id) {
      if (id.equals(TYPE_PROP)) {
         return getLabel();
      }
      return super.getPropertyValue(id);
   }

   /**
    * Returns the source endpoint of this connection.
    * 
    * @return a non-null Shape instance
    */
   public Shape getSource() {
      return source;
   }

   /**
    * Returns the target endpoint of this connection.
    * 
    * @return a non-null Shape instance
    */
   public Shape getTarget() {
      return target;
   }

   /**
    * Reconnect this connection. The connection will reconnect with the shapes it was previously attached to.
    */
   public void reconnect() {
      if (!isConnected) {
         source.addConnection(this);
         target.addConnection(this);
         isConnected = true;
      }
   }

   /**
    * Reconnect to a different source and/or target shape. The connection will disconnect from its current attachments
    * and reconnect to the new source and target.
    * 
    * @param newSource a new source endpoint for this connection (non null)
    * @param newTarget a new target endpoint for this connection (non null)
    * @throws IllegalArgumentException if any of the paramers are null or newSource == newTarget
    */
   public void reconnect(Shape newSource, Shape newTarget) {
      if (newSource == null || newTarget == null || newSource == newTarget) {
         throw new IllegalArgumentException();
      }
      disconnect();
      this.source = newSource;
      this.target = newTarget;
      reconnect();
   }

   /**
    * Set the line drawing style of this connection.
    * 
    * @param lineStyle one of following values: Graphics.LINE_DASH or Graphics.LINE_SOLID
    * @see Graphics#LINE_DASH
    * @see Graphics#LINE_SOLID
    * @throws IllegalArgumentException if lineStyle does not have one of the above values
    */
   public void setLineStyle(int lineStyle) {
      if (lineStyle != Graphics.LINE_DASH && lineStyle != Graphics.LINE_SOLID) {
         throw new IllegalArgumentException();
      }
      this.lineStyle = lineStyle;
   }

   /**
    * @return the label
    */
   public String getLabel() {
      return "Transition";
   }

   /**
    * @return the lineWidth
    */
   public int getLineWidth() {
      return 2;
   }

}