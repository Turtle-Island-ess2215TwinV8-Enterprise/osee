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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.artifact.TeamDefinitionArtifact;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.LegacyPCRActions;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.util.Collections;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.IATSArtifact;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.ui.plugin.util.AWorkbench;
import org.eclipse.osee.framework.ui.skynet.ArtifactImageManager;
import org.eclipse.osee.framework.ui.skynet.FrameworkImage;
import org.eclipse.osee.framework.ui.skynet.artifact.editor.ArtifactEditor;
import org.eclipse.osee.framework.ui.skynet.branch.BranchSelectionDialog;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryCheckDialog;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;
import org.eclipse.swt.widgets.Display;

/**
 * @author Donald G. Dunne
 */
public class MultipleHridSearchItem extends WorldUISearchItem {
   private String enteredIds = "";
   private boolean includeArtIds = false;
   private Branch branch;

   public MultipleHridSearchItem(String name) {
      super(name, AtsImage.OPEN_BY_ID);
   }

   public MultipleHridSearchItem() {
      this("Search by ID(s)");
   }

   public MultipleHridSearchItem(MultipleHridSearchItem multipleHridSearchItem) {
      super(multipleHridSearchItem, AtsImage.OPEN_BY_ID);
      this.enteredIds = multipleHridSearchItem.enteredIds;
   }

   @Override
   public String getSelectedName(SearchType searchType) throws OseeCoreException {
      return String.format(getName() + " - %s", enteredIds);
   }

   @Override
   public Collection<Artifact> performSearch(SearchType searchType) throws OseeCoreException {
      if (!Strings.isValid(enteredIds)) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, "Must Enter Valid Id");
         return EMPTY_SET;
      }
      List<String> ids = new ArrayList<String>();
      for (String str : enteredIds.split(",")) {
         str = str.replaceAll("^\\s+", "");
         str = str.replaceAll("\\s+$", "");
         if (!str.equals("")) {
            ids.add(str);
         }
         // allow for lower case hrids
         if (str.length() == 5) {
            if (!ids.contains(str.toUpperCase())) {
               ids.add(str.toUpperCase());
            }
         }
      }

      if (isCancelled()) return EMPTY_SET;
      if (ids.size() == 0) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, "Must Enter Valid Id");
         return EMPTY_SET;
      }

      Set<Artifact> resultAtsArts = new HashSet<Artifact>();
      Set<Artifact> resultNonAtsArts = new HashSet<Artifact>();
      Set<Artifact> artifacts = new HashSet<Artifact>();

      resultAtsArts.addAll(LegacyPCRActions.getTeamsTeamWorkflowArtifacts(ids,
            (Collection<TeamDefinitionArtifact>) null));

      // This does artId search
      if (includeArtIds && branch != null) {
         for (Artifact art : ArtifactQuery.getArtifactListFromIds(Lib.stringToIntegerList(enteredIds), branch)) {
            artifacts.add(art);
         }
      }
      // This does hrid/guid search
      for (Artifact art : ArtifactQuery.getArtifactListFromIds(ids, AtsUtil.getAtsBranch())) {
         artifacts.add(art);
      }

      for (Artifact art : artifacts) {
         if (art instanceof IATSArtifact) {
            resultAtsArts.add(art);
         } else {
            resultNonAtsArts.add(art);
         }
      }

      if (isCancelled()) return EMPTY_SET;

      if (resultAtsArts.size() == 0 && resultNonAtsArts.size() == 0) {
         OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP,
               "Invalid HRID/Guid/Legacy PCR Id(s): " + Collections.toString(ids, ", "));
      }
      if (resultNonAtsArts.size() > 0) {
         ArtifactEditor.editArtifacts(resultNonAtsArts);
      }
      return resultAtsArts;
   }

   @Override
   public String getName() throws OseeCoreException {
      return super.getName() + (includeArtIds ? " (include ArtIds)" : "");
   }

   @Override
   public void performUI(SearchType searchType) throws OseeCoreException {
      super.performUI(searchType);
      EntryDialog ed = null;
      if (AtsUtil.isAtsAdmin()) {
         ed = new EntryCheckDialog(getName(), "Enter Legacy ID, Guid or HRID (comma separated)", "Include ArtIds");
      } else {
         ed =
               new EntryDialog(Display.getCurrent().getActiveShell(), getName(), null,
                     "Enter Legacy ID, Guid or HRID (comma separated)", MessageDialog.QUESTION, new String[] {"OK",
                           "Cancel"}, 0);
      }
      int response = ed.open();
      if (response == 0) {
         enteredIds = ed.getEntry();
         if (ed instanceof EntryCheckDialog) {
            includeArtIds = ((EntryCheckDialog) ed).isChecked();
            if (includeArtIds) {
               branch = BranchSelectionDialog.getBranchFromUser();
            }
         }
         if (enteredIds.equals("oseerocks") || enteredIds.equals("osee rocks")) {
            AWorkbench.popup("Confirmation", "Confirmed!  Osee Rocks!");
            cancelled = true;
            return;
         }
         if (enteredIds.equals("purple icons")) {
            AWorkbench.popup("Confirmation", "Yeehaw, Purple Icons Rule!!");
            ArtifactImageManager.setOverrideImageEnum(FrameworkImage.PURPLE);
            cancelled = true;
            return;
         }
         return;
      } else
         enteredIds = null;
      cancelled = true;
   }

   /**
    * @return the enteredIds
    */
   public String getEnteredIds() {
      return enteredIds;
   }

   @Override
   public WorldUISearchItem copy() {
      return new MultipleHridSearchItem(this);
   }

}
