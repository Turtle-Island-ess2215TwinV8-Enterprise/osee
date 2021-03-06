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
package org.eclipse.osee.framework.skynet.core.importing;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.osee.framework.core.data.IAttributeType;
import org.eclipse.osee.framework.jdk.core.type.CaseInsensitiveString;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.skynet.core.importing.RoughAttributeSet.RoughAttribute;

public final class RoughAttributeSet implements Iterable<Entry<CaseInsensitiveString, Collection<RoughAttribute>>> {

   private final HashCollection<CaseInsensitiveString, RoughAttribute> attributes =
      new HashCollection<CaseInsensitiveString, RoughAttribute>();

   public void clear() {
      attributes.clear();
   }

   public void addAttribute(String name, String... values) {
      for (String value : values) {
         attributes.put(new CaseInsensitiveString(name), new RoughAttribute(value));
      }
   }

   public void addAttribute(String name, URI... uris) {
      for (URI uri : uris) {
         attributes.put(new CaseInsensitiveString(name), new RoughAttribute(uri));
      }
   }

   public Set<String> getAttributeTypeNames() {
      Set<String> typeNames = new HashSet<String>();
      for (CharSequence attrTypeName : attributes.keySet()) {
         typeNames.add(attrTypeName.toString());
      }
      return typeNames;
   }

   public String getSoleAttributeValue(String typeName) {
      Collection<String> values = getAttributeValueList(typeName);
      return values != null && !values.isEmpty() ? values.iterator().next() : null;
   }

   public Collection<String> getAttributeValueList(IAttributeType attributeType) {
      return getAttributeValueList(attributeType.getName());
   }

   /**
    * @return Same as getAttributeValueList, returns defaultList if getAttributeValueList is null.
    */
   public Collection<String> getAttributeValueList(IAttributeType attributeType, Collection<String> defaultList) {
      Collection<String> list = getAttributeValueList(attributeType);
      return list != null ? list : defaultList;
   }

   public Collection<String> getAttributeValueList(String attributeTypeName) {
      Collection<RoughAttribute> roughAttributes = attributes.getValues(new CaseInsensitiveString(attributeTypeName));
      Collection<String> values = new ArrayList<String>();
      for (RoughAttribute attribute : roughAttributes) {
         if (!attribute.hasURI()) {
            values.add(attribute.getValue());
         }
      }
      return values;
   }

   public Collection<URI> getURIAttributes() {
      Collection<RoughAttribute> roughAttributes = attributes.getValues();
      Collection<URI> values = new ArrayList<URI>();
      for (RoughAttribute attribute : roughAttributes) {
         if (attribute.hasURI()) {
            values.add(attribute.getURI());
         }
      }
      return values;
   }

   @Override
   public Iterator<Entry<CaseInsensitiveString, Collection<RoughAttribute>>> iterator() {
      return attributes.entrySet().iterator();
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof RoughAttributeSet)) {
         return false;
      }
      RoughAttributeSet other = (RoughAttributeSet) obj;
      return this.attributes.equals(other.attributes);
   }

   @Override
   public int hashCode() {
      return attributes.hashCode();
   }

   @Override
   public String toString() {
      return attributes.toString();
   }

   public final static class RoughAttribute {
      private final String value;
      private final URI uri;

      public RoughAttribute(String value) {
         this.value = value;
         this.uri = null;
      }

      public RoughAttribute(URI uri) {
         this.value = null;
         this.uri = uri;
      }

      public boolean hasURI() {
         return uri != null;
      }

      public InputStream getContent() throws Exception {
         InputStream inputStream;
         if (hasURI()) {
            inputStream = new BufferedInputStream(getURI().toURL().openStream());
         } else {
            inputStream = new ByteArrayInputStream(getValue().getBytes("UTF-8"));
         }
         return inputStream;
      }

      public String getValue() {
         return value;
      }

      public URI getURI() {
         return uri;
      }

      @Override
      public String toString() {
         String toReturn;
         if (hasURI()) {
            toReturn = getURI().toASCIIString();
         } else {
            toReturn = getValue();
         }
         return toReturn;
      }
   }

}