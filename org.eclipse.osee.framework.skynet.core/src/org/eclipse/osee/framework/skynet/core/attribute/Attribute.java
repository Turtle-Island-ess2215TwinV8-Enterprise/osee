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
package org.eclipse.osee.framework.skynet.core.attribute;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.PersistenceMemo;
import org.eclipse.osee.framework.jdk.core.util.PersistenceObject;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.AttributeMemo;
import org.eclipse.osee.framework.skynet.core.artifact.CacheArtifactModifiedEvent;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactModifiedEvent.ModType;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;

/**
 * @author Ryan D. Brooks
 */
public abstract class Attribute<T> implements PersistenceObject {
   private static final int MAX_VARCHAR_LENGTH = 4000;
   private static final SkynetEventManager eventManager = SkynetEventManager.getInstance();
   private final DynamicAttributeDescriptor attributeType;
   private DynamicAttributeManager manager;
   private AttributeMemo memo;
   private boolean deletable;
   private boolean deleted;
   protected boolean dirty;

   private String rawStringValue;
   private byte[] rawContent;

   protected Attribute(DynamicAttributeDescriptor attributeType) {
      this.attributeType = attributeType;
      this.deletable = true;
      this.dirty = false;
      this.memo = null;
   }

   public void setDirty() {
      checkDeleted();
      this.dirty = true;
      if (getManager() != null && getManager().getParentArtifact() != null) {
         getManager().getParentArtifact().setInTransaction(false);
         eventManager.kick(new CacheArtifactModifiedEvent(getManager().getParentArtifact(), ModType.Changed, this));
      }
   }

   /**
    * @return the attributeType
    */
   public DynamicAttributeDescriptor getAttributeType() {
      return attributeType;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      Object value = getValue();
      return value == null ? "" : value.toString();
   }

   public String getNameValueDescription() {
      return attributeType.getName() + ": " + toString();
   }

   /**
    * @return Returns the deletable.
    */
   public boolean isDeletable() {
      checkDeleted();
      return deletable;
   }

   /**
    * @param deletable The deletable to set.
    */
   public void setDeletable(boolean deletable) {
      checkDeleted();
      this.deletable = deletable;
   }

   /**
    * @param parent The parent to set.
    */
   protected void setParent(DynamicAttributeManager parent) {
      checkDeleted();
      this.manager = parent;
   }

   /**
    * @return Returns the parent.
    */
   public DynamicAttributeManager getManager() {
      checkDeleted();
      return manager;
   }

   /**
    * @return Returns the dirty.
    */
   public boolean isDirty() {
      return dirty;
   }

   /**
    * Set this attribute as not dirty. Should only be called my the persistence manager once it has persisted this
    * attribute.
    */
   public void setNotDirty() {
      checkDeleted();
      this.dirty = false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see osee.plugin.core.util.PersistenceObject#getPersistenceMemo()
    */
   public AttributeMemo getPersistenceMemo() {
      checkDeleted();
      return memo;
   }

   /*
    * (non-Javadoc)
    * 
    * @see osee.plugin.core.util.PersistenceObject#setPersistenceMemo(osee.plugin.core.util.PersistenceMemo)
    */
   public void setPersistenceMemo(PersistenceMemo memo) {
      checkDeleted();
      if (memo instanceof AttributeMemo)
         this.memo = (AttributeMemo) memo;
      else
         throw new IllegalArgumentException("Invalid memo type");
   }

   public void delete() {
      checkDeleted();
      if (deletable) {
         manager.removeAttribute(this);
      }
   }

   public boolean isDeleted() {
      return deleted;
   }

   protected void checkDeleted() {
      if (deleted) throw new IllegalStateException("This artifact has been deleted");
   }

   /**
    * Provides Attribute subclasses access to the raw large object value from the datastore
    * 
    * @return exact bytes from datastore
    */
   public ByteArrayInputStream getRawContentStream() {
      if (getRawContent() == null) {
         return null;
      }
      return new ByteArrayInputStream(getRawContent());
   }

   /**
    * Copies this attribute onto the specified artifact. Can not handle attribute types with multiple instances
    */
   public void copyTo(Artifact artifact) throws SQLException {
      Attribute<?> attributeCopy;
      if (artifact.isInAttributeInitialization()) {
         attributeCopy = artifact.getAttributeManager(attributeType).getNewAttribute();
      } else {
         attributeCopy = artifact.getAttributeManager(attributeType).getAttributeForSet();
      }
      attributeCopy.setRawContent(getRawContent());
      attributeCopy.setRawStringValue(rawStringValue);
   }

   @Deprecated
   public String getStringData() {
      return getRawStringValue();
   }

   @Deprecated
   public void setStringData(String rawStringVaule) {
      setRawStringValue(rawStringVaule);
   }

   public abstract void setValue(T value);

   public abstract T getValue();

   public abstract void setValueFromInputStream(InputStream value) throws IOException;

   /**
    * callers to this should ensure they have string attributes and then do the replacements themselves
    */
   @Deprecated
   public void replaceAll(Pattern pattern, String replacement) {
      setStringData(pattern.matcher(getStringData()).replaceAll(replacement));
   }

   /**
    * Purge attribute from the database.
    */
   public void purge() throws SQLException {
      manager.purge(this);
      deleted = true;
   }

   /**
    * Provides Attribute subclasses access to the raw String value from the datastore
    * 
    * @return exact String from datastore
    */
   public String getRawStringValue() {
      return rawStringValue != null ? rawStringValue : "";
   }

   /**
    * Provides Attribute subclasses access to the raw large object value from the datastore
    * 
    * @return exact bytes from datastore
    */
   protected byte[] getRawContent() {
      return rawContent;
   }

   /**
    * @param rawStringValue the rawStringVaule to set
    */
   protected void setRawStringValue(String rawStringValue) {
      // the == is used to handle equality when both are null
      if (this.rawStringValue == rawStringValue) {
         return;
      }
      if (this.rawStringValue != null && this.rawStringValue.equals(rawStringValue)) {
         return;
      }
      if (rawStringValue.length() > MAX_VARCHAR_LENGTH) {
         try {
            setRawContent(rawStringValue.getBytes("UTF-8"));
         } catch (UnsupportedEncodingException ex) {
            SkynetActivator.getLogger().log(Level.SEVERE, ex.toString(), ex);
         }
      } else {
         this.rawStringValue = rawStringValue;
         setDirty();
      }
   }

   /**
    * @param rawContent the rawContent to set
    */
   protected void setRawContent(byte[] rawContent) {
      if (Arrays.equals(getRawContent(), rawContent)) {
         return;
      }

      this.rawContent = rawContent;
      setDirty();
   }

   /**
    * This should never be called from the application software.
    */
   protected void injectFromDb(InputStream rawContent, String rawStringVaule) {
      try {
         this.rawContent = rawContent == null ? null : Lib.inputStreamToBytes(rawContent);
         this.rawStringValue = rawStringVaule;
      } catch (IOException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.toString(), ex);
      }
   }
}