/*******************************************************************************
 * Copyright (c) 2011 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.display.presenter.mocks;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.framework.jdk.core.type.MatchLocation;
import org.eclipse.osee.orcs.data.ReadableArtifact;
import org.eclipse.osee.orcs.data.ReadableAttribute;
import org.eclipse.osee.orcs.search.Match;

/**
 * @author John Misinco
 */
public class MockMatch implements Match<ReadableArtifact, ReadableAttribute<?>> {

   private final ReadableArtifact artifact;
   private final List<ReadableAttribute<?>> attributes = new LinkedList<ReadableAttribute<?>>();

   public MockMatch(ReadableArtifact artifact) {
      this.artifact = artifact;
   }

   public MockMatch(ReadableArtifact artifact, ReadableAttribute<?> attribute) {
      this(artifact);
      attributes.add(attribute);
   }

   public MockMatch(ReadableArtifact artifact, List<ReadableAttribute<?>> attributes) {
      this(artifact);
      this.attributes.addAll(attributes);
   }

   @Override
   public boolean hasLocationData() {
      return false;
   }

   @Override
   public ReadableArtifact getItem() {
      return artifact;
   }

   @Override
   public List<ReadableAttribute<?>> getElements() {
      return attributes;
   }

   @Override
   public List<MatchLocation> getLocation(ReadableAttribute<?> element) {
      List<MatchLocation> toReturn = new LinkedList<MatchLocation>();
      toReturn.add(new MatchLocation());
      return toReturn;
   }
}