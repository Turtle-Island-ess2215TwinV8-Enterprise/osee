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
package org.eclipse.osee.ats.task;

import java.util.Collection;
import org.eclipse.nebula.widgets.xviewer.customize.CustomizeData;
import org.eclipse.osee.ats.world.search.WorldSearchItem.SearchType;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;

/**
 * @author Donald G. Dunne
 */
public class TaskEditorSimpleProvider extends TaskEditorProvider {

   private final String name;
   private final Collection<? extends Artifact> artifacts;

   public TaskEditorSimpleProvider(String name, Collection<? extends Artifact> artifacts) {
      this(name, artifacts, null, TableLoadOption.None);
   }

   public TaskEditorSimpleProvider(String name, Collection<? extends Artifact> artifacts, CustomizeData customizeData, TableLoadOption... tableLoadOption) {
      super(customizeData, tableLoadOption);
      this.name = name;
      this.artifacts = artifacts;
   }

   @Override
   public String getTaskEditorLabel(SearchType searchType) {
      return Strings.truncate(name, TaskEditor.TITLE_MAX_LENGTH, true);
   }

   @Override
   public Collection<? extends Artifact> getTaskEditorTaskArtifacts() {
      return artifacts;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public ITaskEditorProvider copyProvider() {
      return new TaskEditorSimpleProvider(name, artifacts, customizeData, tableLoadOptions);
   }

}
