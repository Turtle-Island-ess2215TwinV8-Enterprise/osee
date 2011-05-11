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

package org.eclipse.osee.ats.review;

import java.util.Date;
import org.eclipse.osee.ats.AtsImage;
import org.eclipse.osee.ats.AtsOpenOption;
import org.eclipse.osee.ats.artifact.PeerToPeerReviewArtifact;
import org.eclipse.osee.ats.artifact.ReviewManager;
import org.eclipse.osee.ats.internal.AtsPlugin;
import org.eclipse.osee.ats.util.AtsUtil;
import org.eclipse.osee.ats.util.widgets.dialog.ActionableItemListDialog;
import org.eclipse.osee.framework.core.enums.Active;
import org.eclipse.osee.framework.logging.OseeLevel;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateComposite.TableLoadOption;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItem;
import org.eclipse.osee.framework.ui.plugin.xnavigate.XNavigateItemAction;
import org.eclipse.osee.framework.ui.skynet.widgets.dialog.EntryDialog;

/**
 * @author Donald G. Dunne
 */
public class NewPeerToPeerReviewItem extends XNavigateItemAction {

   public NewPeerToPeerReviewItem(XNavigateItem parent) {
      super(parent, "New Stand-alone Peer To Peer Review", AtsImage.PEER_REVIEW);
   }

   @Override
   public void run(TableLoadOption... tableLoadOptions) {
      final ActionableItemListDialog ld = new ActionableItemListDialog(Active.Both);
      ld.setMessage("Select Actionable Items to Review\n\nNOTE: To create a review against " + "an Action and Team Workflow\nopen the object in ATS and select the " + "review to create from the editor.");
      int result = ld.open();
      if (result == 0) {
         final EntryDialog ed = new EntryDialog("Peer Review Title", "Enter Peer Review Title");
         if (ed.open() == 0) {
            try {
               SkynetTransaction transaction = new SkynetTransaction(AtsUtil.getAtsBranch(), "New Peer To Peer Review");
               PeerToPeerReviewArtifact peerArt =
                  ReviewManager.createNewPeerToPeerReview(null, ed.getEntry(), null, new Date(), UserManager.getUser(),
                     transaction);
               peerArt.getActionableItemsDam().setActionableItems(ld.getSelected());
               peerArt.persist(transaction);
               AtsUtil.openATSAction(peerArt, AtsOpenOption.OpenAll);
               transaction.execute();
            } catch (Exception ex) {
               OseeLog.log(AtsPlugin.class, OseeLevel.SEVERE_POPUP, ex);
            }
         }
      }
   }
}