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

package org.eclipse.osee.ats.util.widgets.port;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osee.ats.core.client.team.TeamWorkFlowArtifact;
import org.eclipse.osee.ats.editor.SMAEditor;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.skynet.widgets.GenericXWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.IArtifactWidget;
import org.eclipse.osee.framework.ui.swt.FontManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author Donald G. Dunne
 */
public class XPortTableWidget extends GenericXWidget implements IArtifactWidget {

   public static final String WIDGET_NAME = "XPortTableWidget";
   public static final String NAME = "Port Manager";

   private final PortTableDisplay display;
   private final PortController portController;

   public XPortTableWidget() {
      super(NAME);
      display = new PortTableDisplay(this);
      portController = new PortController(display);
   }

   @Override
   public TeamWorkFlowArtifact getArtifact() {
      return portController.getDestinationWorkflow();
   }

   @Override
   protected void createControls(Composite parent, int horizontalSpan) {
      display.createControls(parent, horizontalSpan);
      portController.bind();
   }

   @Override
   public Control getControl() {
      return display.getControl();
   }

   @Override
   public void dispose() {
      portController.unbind();
   }

   @Override
   public void refresh() {
      portController.update();
   }

   @Override
   public IStatus isValid() {
      return portController.isValid();
   }

   @Override
   public void setArtifact(Artifact artifact) throws OseeCoreException {
      portController.setDestinationWorkflow(artifact);
   }

   @Override
   public void revert() {
      // do nothing
   }

   @Override
   public void saveToArtifact() {
      // do nothing - auto-save on drop
   }

   @Override
   public Control getErrorMessageControl() {
      return labelWidget;
   }

   @Override
   public String toString() {
      return String.format("%s", getLabel());
   }

   @Override
   public Result isDirty() {
      return portController.isDirty();
   }

   @Override
   public boolean isEmpty() {
      return portController.hasSourceWorkflows();
   }

   @Override
   public Object getData() {
      return portController.getSourceWorkflows();
   }

   // some XWidget operations that are needed by the display contained class, we provide the facade
   protected PortController getPortController() {
      return portController;
   }

   protected void doLabelWidget(Composite comp) {
      labelWidget = new Label(comp, SWT.NONE);
      labelWidget.setText("Workflows");
      labelWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
      SMAEditor.setLabelFonts(labelWidget, FontManager.getDefaultLabelFont());
   }

   protected void paintBordersFor(Composite comp) {
      if (toolkit != null) {
         toolkit.paintBordersFor(comp);
      }
   }

   protected void adapt(Composite comp) {
      if (toolkit != null) {
         toolkit.adapt(comp);
      }
   }

   protected void reflow(boolean value) {
      getManagedForm().reflow(value);
   }

   protected Button createButton(Composite buttonBar, String string, int push) {
      Button b = null;
      if (toolkit != null) {
         b = toolkit.createButton(buttonBar, string, push);
      }
      return b;
   }

   protected void adapt(Control control, boolean trackFocus, boolean trackKeyboard) {
      if (toolkit != null) {
         toolkit.adapt(control, trackFocus, trackKeyboard);
      }
   }
}
