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
package org.eclipse.osee.framework.skynet.core.relation;

import java.sql.SQLException;
import java.util.logging.Level;
import org.eclipse.osee.framework.skynet.core.SkynetActivator;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.ArtifactCache;
import org.eclipse.osee.framework.skynet.core.artifact.Branch;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.event.SkynetEventManager;
import org.eclipse.osee.framework.skynet.core.relation.RelationModifiedEvent.ModType;
import org.eclipse.osee.framework.skynet.core.util.ArtifactDoesNotExist;

/**
 * @author Jeff C. Phillips
 */
public class RelationLink {
   private int relationId;
   private int gammaId;
   private Artifact artA;
   private Artifact artB;
   private boolean deleted;
   private int aOrder;
   private int bOrder;
   private String rationale;
   private RelationType relationType;
   private boolean dirty;
   private int aArtifactId;
   private int bArtifactId;
   private Branch aBranch;
   private Branch bBranch;

   public RelationLink(int aArtifactId, int bArtifactId, Branch aBranch, Branch bBranch, RelationType relationType, int relationId, int gammaId, String rationale, int aOrder, int bOrder) {
      this.relationType = relationType;
      this.relationId = relationId;
      this.gammaId = gammaId;
      this.rationale = rationale == null ? "" : rationale;
      this.aOrder = aOrder;
      this.bOrder = bOrder;
      this.deleted = false;
      this.dirty = false;
      this.aArtifactId = aArtifactId;
      this.bArtifactId = bArtifactId;
      this.aBranch = aBranch;
      this.bBranch = bBranch;
   }

   public RelationLink(Artifact aArtifact, Artifact bArtifact, RelationType relationType, String rationale) {
      this(aArtifact.getArtId(), bArtifact.getArtId(), aArtifact.getBranch(), bArtifact.getBranch(), relationType, 0,
            0, rationale, 0, 0);
   }

   @Deprecated
   public Artifact getOtherSideAritfactIfAvailable(Artifact artifact) {
      if (artifact == artA) {
         return artB;
      }
      return artA;
   }

   public boolean isOnSideA(Artifact artifact) {
      if (aArtifactId == artifact.getArtId()) {
         return true;
      }
      if (bArtifactId == artifact.getArtId()) {
         return true;
      }
      throw new IllegalArgumentException("The artifact " + artifact + " is on neither side of " + this);
   }

   /**
    * @return the aArtifactId
    */
   public int getAArtifactId() {
      return aArtifactId;
   }

   /**
    * @return the bArtifactId
    */
   public int getBArtifactId() {
      return bArtifactId;
   }

   public int getArtifactId(boolean sideA) {
      return sideA ? aArtifactId : bArtifactId;
   }

   /**
    * @return the aBranch
    */
   public Branch getABranch() {
      return aBranch;
   }

   /**
    * @return the bBranch
    */
   public Branch getBBranch() {
      return bBranch;
   }

   /**
    * @return Returns the deleted.
    */
   public boolean isDeleted() {
      return deleted;
   }

   /**
    * @return Returns the dirty.
    */
   public boolean isDirty() {
      return dirty;
   }

   @Deprecated
   public void persist() throws SQLException {
      if (dirty) {
         RelationPersistenceManager.makePersistent(this);
      }
   }

   public void delete() throws SQLException {
      deleted = true;
      // There must be at least one link manager loaded to access delete
      if (!artA.isLinksLoaded() && !artB.isLinksLoaded()) throw new IllegalStateException(
            "Invalid state where neither link manager is loaded");
      // Only one of these needs to be called in order to delete a link
      if (artA.isLinksLoaded()) artA.getLinkManager().deleteLink(this);
      if (artB.isLinksLoaded()) artB.getLinkManager().deleteLink(this);

      SkynetEventManager.getInstance().kick(
            new CacheRelationModifiedEvent(this, getRelationType().getTypeName(), getASideName(),
                  ModType.Deleted.name(), this, getABranch()));

   }

   public Artifact getArtifact(boolean sideA) throws ArtifactDoesNotExist, SQLException {
      Artifact relatedArtifact = ArtifactCache.get(getArtifactId(sideA), getBranch(sideA));
      if (relatedArtifact == null) {
         return ArtifactQuery.getArtifactFromId(getArtifactId(sideA), getBranch(sideA));
      }
      return null; // by design this return should never happen
   }

   public Artifact getArtifactA() {
      if (artA == null) {
         try {
            artA = ArtifactQuery.getArtifactFromId(aArtifactId, artB.getBranch());
         } catch (Exception ex) {
         }
      }
      return artA;
   }

   public Artifact getArtifactB() {
      if (artB == null) {
         try {
            artB = ArtifactQuery.getArtifactFromId(bArtifactId, artA.getBranch());
         } catch (Exception ex) {
         }
      }
      return artB;
   }

   /**
    * @return Returns the order.
    */
   public int getAOrder() {
      return aOrder;
   }

   /**
    * @param order The order to set.
    */
   public void setAOrder(int order) {
      try {
         this.aOrder = order;
         dirty = true;

         if (artA.isLinksLoaded()) artA.getLinkManager().fixOrderingOf(this, true);
         if (artB.isLinksLoaded()) artB.getLinkManager().fixOrderingOf(this, false);
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
   }

   /**
    * @return Returns the order.
    */
   public int getBOrder() {
      return bOrder;
   }

   public int getOrder(boolean sideA) {
      return sideA ? aOrder : bOrder;
   }

   /**
    * @param order The order to set.
    */
   public void setBOrder(int order) {
      try {
         this.bOrder = order;
         dirty = true;

         if (isInDb()) {
            if (artA.isLinksLoaded()) artA.getLinkManager().fixOrderingOf(this, true);
            if (artB.isLinksLoaded()) artB.getLinkManager().fixOrderingOf(this, false);
         }
      } catch (SQLException ex) {
         SkynetActivator.getLogger().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
   }

   public void swapAOrder(RelationLink link) {
      swapOrder(link, true);
   }

   public void swapBOrder(RelationLink link) {
      swapOrder(link, false);
   }

   private void swapOrder(RelationLink link, boolean sideA) {
      if (link == null) throw new IllegalArgumentException("link can not be null.");

      // Swapping a link with itself has no effect.
      if (link == this) return;

      if (sideA) {
         int tmp = aOrder;
         if (link.getAOrder() == aOrder)
            setAOrder(link.getAOrder() + 1);
         else
            setAOrder(link.getAOrder());
         link.setAOrder(tmp);
      } else {
         int tmp = bOrder;
         setBOrder(link.getBOrder());
         link.setBOrder(tmp);
      }
   }

   /**
    * @return Returns the rationale.
    */
   public String getRationale() {
      return rationale;
   }

   /**
    * @param rationale The rationale to set.
    */
   public void setRationale(String rationale, boolean notify) {
      if (rationale == null) throw new IllegalArgumentException("Rationale can not be null");

      if (this.rationale.equals(rationale)) return;

      this.rationale = rationale;

      dirty = true;

      if (notify) {
         SkynetEventManager.getInstance().kick(
               new CacheRelationModifiedEvent(this, getRelationType().getTypeName(), getASideName(),
                     ModType.RationaleMod.name(), this, getABranch()));
      }
   }

   public RelationType getRelationType() {
      return relationType;
   }

   public String getSideNameFor(Artifact artifact) {
      return processArtifactSideName(artifact, false);
   }

   public String getSideNameForOtherArtifact(Artifact artifact) {
      return processArtifactSideName(artifact, true);
   }

   private String processArtifactSideName(Artifact artifact, boolean otherArtifact) {
      String sideName = "";

      if (artifact == artA) {

         if (otherArtifact)
            sideName = relationType.getSideBName();
         else
            sideName = relationType.getSideAName();
      } else if (artifact == artB) {

         if (otherArtifact)
            sideName = relationType.getSideAName();
         else
            sideName = relationType.getSideBName();
      } else
         throw new IllegalArgumentException("Link does not contain the artifact.");

      return sideName;
   }

   public String getSidePhrasingFor(Artifact artifact) {
      return processArtifactSidePhrasing(artifact, false);
   }

   public String getSidePhrasingForOtherArtifact(Artifact artifact) {
      return processArtifactSidePhrasing(artifact, true);
   }

   private String processArtifactSidePhrasing(Artifact artifact, boolean otherArtifact) {
      String sideName = "";

      if (artifact == artA) {

         if (otherArtifact)
            sideName = relationType.getBToAPhrasing();
         else
            sideName = relationType.getAToBPhrasing();
      } else if (artifact == artB) {

         if (otherArtifact)
            sideName = relationType.getAToBPhrasing();
         else
            sideName = relationType.getBToAPhrasing();
      } else
         throw new IllegalArgumentException("Link does not contain the artifact.");

      return sideName;
   }

   public String getASideName() {
      return relationType.getSideAName();
   }

   public String getBSideName() {
      return relationType.getSideBName();
   }

   public String toString() {
      return String.format("%s: %s(%s)<-->%s(%s)", relationType.getTypeName(), artA.getDescriptiveName(),
            Float.toString(aOrder), artB.getDescriptiveName(), Float.toString(bOrder));
   }

   public boolean isExplorable() {
      return true;
   }

   public void setNotDirty() {
      dirty = false;
   }

   public void setDirty() {
      dirty = true;
   }

   public boolean isVersionControlled() {
      return true;
   }

   public Branch getBranch(boolean sideA) {
      return sideA ? aBranch : bBranch;
   }

   public void setDirty(boolean isDirty) {
      dirty = isDirty;
   }

   public void setPersistenceIds(int relationId, int gammaId) {
      this.relationId = relationId;
      this.gammaId = gammaId;
   }

   public int getRelationId() {
      return relationId;
   }

   public int getGammaId() {
      return gammaId;
   }

   public boolean isInDb() {
      return gammaId > 0;
   }

   /**
    * @param gammaId2
    */
   public void setGammaId(int gammaId) {
      this.gammaId = gammaId;
   }

   /**
    * @return
    */
   public Branch getBranch() {
      return getBranch(true);
   }
}
