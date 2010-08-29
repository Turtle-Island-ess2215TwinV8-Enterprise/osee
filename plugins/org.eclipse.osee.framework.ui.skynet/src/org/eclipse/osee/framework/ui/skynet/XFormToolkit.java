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
package org.eclipse.osee.framework.ui.skynet;

import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Ryan D. Brooks
 */
public class XFormToolkit extends FormToolkit {

   public XFormToolkit(Display display) {
      super(display);
   }

   public XFormToolkit(FormColors colors) {
      super(colors);
   }

   public Combo createCombo(Composite parent, int style) {
      Combo combo = new Combo(parent, style | SWT.FLAT);
      adapt(combo, true, true);
      return combo;
   }

   public Composite createClientContainer(Section section, int span) {
      Composite container = createContainer(section, span);
      section.setClient(container);
      return container;
   }

   public Composite createContainer(Composite parent, int span) {
      Composite container = createComposite(parent);
      GridLayout layout = new GridLayout(span, false);
      layout.marginWidth = layout.marginHeight = 2;
      container.setLayout(layout);
      paintBordersFor(container);
      return container;
   }

   public void addHelpLinkToSection(final Section section, final String helpPath) {
      addHelpLinkToSection(this, section, helpPath);
   }

   public static void addHelpLinkToSection(final FormToolkit toolkit, final Section section, final String helpPath) {
      Control control = section.getTextClient();
      Composite parent = section;
      if (control != null) {
         parent = (Composite) control; // assumes that if this link is being added with existing controls they are contained in a composite
      }
      ImageHyperlink helpLink = new ImageHyperlink(parent, SWT.NULL);
      toolkit.adapt(helpLink, true, true);
      helpLink.setImage(ImageManager.getImage(FrameworkImage.HELP));
      helpLink.setBackground(section.getTitleBarGradientBackground());
      helpLink.addHyperlinkListener(new HyperlinkAdapter() {
         @Override
         public void linkActivated(HyperlinkEvent e) {
            PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpPath);
         }
      });
      if (control == null) {
         section.setTextClient(helpLink);
      }
   }
}
