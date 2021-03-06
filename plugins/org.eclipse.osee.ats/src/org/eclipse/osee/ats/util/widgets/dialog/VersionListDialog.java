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
package org.eclipse.osee.ats.util.widgets.dialog;

import java.util.Collection;
import java.util.LinkedList;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.osee.ats.api.version.IAtsVersion;
import org.eclipse.osee.framework.ui.skynet.widgets.XCheckBox;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Donald G. Dunne
 */
public class VersionListDialog extends org.eclipse.ui.dialogs.ListDialog {

   XCheckBox showReleased = new XCheckBox("Show Released Versions");
   VersionContentProvider versionContentProvider;
   private final Collection<IAtsVersion> verArts;

   public VersionListDialog(String title, String message, Collection<IAtsVersion> verArts) {
      super(Displays.getActiveShell());
      this.verArts = verArts;
      this.setTitle(title);
      this.setMessage(message);
      versionContentProvider = new VersionContentProvider(false);
      this.setContentProvider(versionContentProvider);
      setLabelProvider(new VersionLabelProvider());
      setInput(verArts);
   }

   @Override
   protected Control createDialogArea(Composite container) {
      Control control = super.createDialogArea(container);
      getTableViewer().setSorter(new AtsObjectNameReverseSorter());

      Composite comp = new Composite(control.getParent(), SWT.NONE);
      comp.setLayout(new GridLayout(2, false));
      comp.setLayoutData(new GridData(GridData.FILL_BOTH));

      showReleased.createWidgets(comp, 2);
      showReleased.set(false);
      showReleased.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            versionContentProvider.setShowReleased(!versionContentProvider.getShowReleased());
            setInput(verArts);
            getTableViewer().refresh();
         };
      });

      return control;
   }

   public class VersionContentProvider extends ArrayContentProvider {

      boolean showReleased = false;

      public VersionContentProvider(boolean showReleased) {
         this.showReleased = showReleased;
      }

      public boolean getShowReleased() {
         return showReleased;
      }

      @SuppressWarnings({"rawtypes", "unchecked"})
      @Override
      public Object[] getElements(Object inputElement) {
         if (inputElement instanceof Collection) {
            Collection list = (Collection) inputElement;
            Collection<Object> verArts = new LinkedList<Object>();
            for (Object obj : list.toArray(new IAtsVersion[list.size()])) {
               IAtsVersion verArt = (IAtsVersion) obj;
               if (showReleased || (!showReleased && !verArt.isReleased())) {
                  verArts.add(verArt);
               }
            }
            return verArts.toArray(new IAtsVersion[verArts.size()]);
         }
         return super.getElements(inputElement);
      }

      public void setShowReleased(boolean showReleased) {
         this.showReleased = showReleased;
      }
   }
}
