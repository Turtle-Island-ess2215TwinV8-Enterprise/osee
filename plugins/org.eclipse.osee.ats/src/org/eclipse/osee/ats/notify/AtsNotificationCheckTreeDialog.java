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
package org.eclipse.osee.ats.notify;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osee.ats.core.client.notify.AtsNotificationManager;
import org.eclipse.osee.ats.core.client.notify.IAtsNotification;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.swt.Displays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

/**
 * @author Donald G. Dunne
 */
public class AtsNotificationCheckTreeDialog extends CheckedTreeSelectionDialog {
   private Button sendNotificationsButton;
   private boolean sendNotifications;

   public AtsNotificationCheckTreeDialog() {
      super(Displays.getActiveShell(), labelProvider, treeContentProvider);
      setTitle("Select ATS Notifications");
      setMessage("Select Desired ATS Notifications");
      try {
         setInput(AtsNotificationManager.getAtsNotificationItems());
      } catch (Exception ex) {
         OseeLog.log(Activator.class, OseeLevel.SEVERE_POPUP, ex);
      }
   }

   public Collection<IAtsNotification> getSelectedAtsNotifications() {
      ArrayList<IAtsNotification> notifications = new ArrayList<IAtsNotification>();
      for (Object obj : getResult()) {
         notifications.add((IAtsNotification) obj);
      }
      return notifications;
   }

   @Override
   protected Control createDialogArea(Composite container) {
      Control c = super.createDialogArea(container);
      getTreeViewer().setSorter(new ViewerSorter() {
         @SuppressWarnings("unchecked")
         @Override
         public int compare(Viewer viewer, Object e1, Object e2) {
            return getComparator().compare(((IAtsNotification) e1).getNotificationName(),
               ((IAtsNotification) e2).getNotificationName());
         }
      });

      sendNotificationsButton = new Button(container, SWT.CHECK);
      sendNotificationsButton.setText("Send Notifications (Otherwise, only report will display)");
      sendNotificationsButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            sendNotifications = sendNotificationsButton.getSelection();
         }
      });
      return c;
   }

   public void setArtifacts(Collection<? extends Artifact> artifacts) {
      setInput(artifacts);
   }

   static ILabelProvider labelProvider = new ILabelProvider() {

      @Override
      public Image getImage(Object element) {
         return null;
      }

      @Override
      public String getText(Object element) {
         if (element instanceof IAtsNotification) {
            return ((IAtsNotification) element).getNotificationName();
         }
         return "Unknown";
      }

      @Override
      public void addListener(ILabelProviderListener listener) {
         // do nothing
      }

      @Override
      public void dispose() {
         // do nothing
      }

      @Override
      public boolean isLabelProperty(Object element, String property) {
         return false;
      }

      @Override
      public void removeListener(ILabelProviderListener listener) {
         // do nothing
      }

   };
   static ITreeContentProvider treeContentProvider = new ITreeContentProvider() {
      @Override
      @SuppressWarnings("rawtypes")
      public Object[] getElements(Object inputElement) {
         if (inputElement instanceof Collection) {
            return ((Collection) inputElement).toArray();
         }
         return Collections.EMPTY_ARRAY;
      };

      @Override
      @SuppressWarnings("rawtypes")
      public Object[] getChildren(Object parentElement) {
         if (parentElement instanceof Collection) {
            return ((Collection) parentElement).toArray();
         }
         return Collections.EMPTY_ARRAY;
      };

      @Override
      public boolean hasChildren(Object element) {
         return getChildren(element).length > 0;
      }

      @Override
      public Object getParent(Object element) {
         return null;
      }

      @Override
      public void dispose() {
         // do nothing
      }

      @Override
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         // do nothing
      };
   };

   public boolean isSendNotifications() {
      return sendNotifications;
   }
}
