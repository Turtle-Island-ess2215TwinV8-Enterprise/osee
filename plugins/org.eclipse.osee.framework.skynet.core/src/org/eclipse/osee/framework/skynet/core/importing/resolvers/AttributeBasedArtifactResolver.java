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
package org.eclipse.osee.framework.skynet.core.importing.resolvers;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.ArtifactType;
import org.eclipse.osee.framework.core.model.AttributeType;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.importing.RoughArtifact;
import org.eclipse.osee.framework.skynet.core.importing.RoughAttributeSet;
import org.eclipse.osee.framework.skynet.core.internal.Activator;

/**
 * @author Robert A. Fisher
 */
public class AttributeBasedArtifactResolver extends NewArtifactImportResolver {
   private final Collection<AttributeType> nonChangingAttributes;
   private final boolean createNewIfNotExist;

   public AttributeBasedArtifactResolver(ArtifactType primaryArtifactType, ArtifactType secondaryArtifactType, Collection<AttributeType> nonChangingAttributes, boolean createNewIfNotExist, boolean deleteUnmatchedArtifacts) {
      super(primaryArtifactType, secondaryArtifactType);
      this.nonChangingAttributes = nonChangingAttributes;
      this.createNewIfNotExist = createNewIfNotExist;
   }

   private boolean attributeValuesMatch(RoughArtifact roughArtifact, Artifact artifact) throws OseeCoreException {
      RoughAttributeSet roughAttributeSet = roughArtifact.getAttributes();
      HashCollection<String, String> roughAttributeMap = roughAttributeSet.getAllEntries();

      for (AttributeType attributeType : nonChangingAttributes) {
         Collection<String> attributeValues = artifact.getAttributesToStringList(attributeType.getName());
         Collection<String> roughAttributes = roughAttributeMap.getValues(attributeType.getName());

         if (roughAttributes == null) {
            roughAttributes = Collections.emptyList();
         }

         if (attributeValues.size() == roughAttributes.size()) {
            for (String attributeValue : attributeValues) {
               Iterator<String> iter = roughAttributes.iterator();

               String normalizedAttributeValue = normalizeAttributeValue(attributeValue);
               while (iter.hasNext()) {
                  String otherAttribute = iter.next();

                  if (normalizedAttributeValue.equals(normalizeAttributeValue(otherAttribute))) {
                     return true;
                  }
               }
            }
         }
      }
      return false;
   }

   private String normalizeAttributeValue(String value) {
      return value.trim().replaceAll("\\.$", "").toLowerCase();
   }

   @Override
   public Artifact resolve(RoughArtifact roughArtifact, Branch branch, Artifact realParent, Artifact root) throws OseeCoreException {
      Artifact realArtifact = null;
      RoughArtifact roughParent = roughArtifact.getRoughParent();

      if (roughParent != null) {
         List<Artifact> descendants = root.getDescendants();
         Collection<Artifact> candidates = new LinkedList<Artifact>();

         for (Artifact artifact : descendants) {
            if (attributeValuesMatch(roughArtifact, artifact)) {
               candidates.add(artifact);
            }
         }

         if (candidates.size() == 1) {
            realArtifact = candidates.iterator().next();
            translateAttributes(roughArtifact, realArtifact);
         } else {
            String output =
                  String.format("Found %s candidates during reuse import for \"%s\"", candidates.size(),
                        roughArtifact.getName());
            OseeLog.log(Activator.class, Level.INFO, output);
            if (createNewIfNotExist) {
               realArtifact = super.resolve(roughArtifact, branch, null, root);
            }
         }
      }

      return realArtifact;
   }
}