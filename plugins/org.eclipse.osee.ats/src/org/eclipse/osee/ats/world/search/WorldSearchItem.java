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
package org.eclipse.osee.ats.world.search;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.osee.ats.internal.Activator;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.swt.ImageManager;
import org.eclipse.osee.framework.ui.swt.KeyedImage;
import org.eclipse.swt.graphics.Image;

/**
 * @author Donald G. Dunne
 */
public abstract class WorldSearchItem {

   private final String name;
   protected static final Set<Artifact> EMPTY_SET = new HashSet<Artifact>();
   protected boolean cancelled = false;
   private LoadView loadView;
   private KeyedImage oseeImage;
   public static enum LoadView {
      TaskEditor,
      WorldEditor,
      None
   }
   public static enum SearchType {
      Search,
      ReSearch
   };

   public WorldSearchItem(String name) {
      this(name, LoadView.WorldEditor);
   }

   public WorldSearchItem(String name, LoadView loadView) {
      this(name, loadView, null);
   }

   public WorldSearchItem(String name, LoadView loadView, boolean cancelled, KeyedImage oseeImage) {
      super();
      this.name = name;
      this.loadView = loadView;
      this.cancelled = cancelled;
      this.oseeImage = oseeImage;
   }

   public WorldSearchItem(String name, LoadView loadView, KeyedImage oseeImage) {
      this(name, loadView, false, oseeImage);
   }

   public WorldSearchItem(WorldSearchItem worldSearchItem) {
      this(worldSearchItem, null);
   }

   public WorldSearchItem(WorldSearchItem worldSearchItem, KeyedImage oseeImage) {
      this(worldSearchItem.name, worldSearchItem.loadView, worldSearchItem.cancelled, oseeImage);
   }

   public abstract WorldSearchItem copy() throws OseeArgumentException, OseeCoreException;

   @SuppressWarnings("unused")
   public String getName() throws OseeCoreException {
      return name;
   }

   /**
    * Method called to display the current search in the view. Override to provide more information about selected
    * values (eg MyWorld)
    */
   public String getSelectedName(SearchType searchType) throws OseeCoreException {
      return getName();
   }

   public boolean isCancelled() {
      return cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   public LoadView getLoadView() {
      return loadView;
   }

   public void setLoadView(LoadView loadView) {
      this.loadView = loadView;
   }

   @Override
   public String toString() {
      try {
         return getName();
      } catch (OseeCoreException ex) {
         OseeLog.log(Activator.class, Level.SEVERE, ex);
         return ex.getLocalizedMessage();
      }
   }

   public Image getImage() {
      return oseeImage == null ? null : ImageManager.getImage(oseeImage);
   }

   public KeyedImage getOseeImage() {
      return oseeImage;
   }

   public void setOseeImage(KeyedImage oseeImage) {
      this.oseeImage = oseeImage;
   }

}
