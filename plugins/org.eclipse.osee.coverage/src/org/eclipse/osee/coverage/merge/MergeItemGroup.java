/*
 * Created on Nov 3, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.coverage.merge;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.coverage.model.ICoverage;
import org.eclipse.osee.coverage.util.CoverageUtil;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.ui.plugin.util.Result;
import org.eclipse.osee.framework.ui.swt.KeyedImage;

/**
 * @author Donald G. Dunne
 */
public class MergeItemGroup extends MergeItemBase {

   private final Collection<IMergeItem> mergeItems;
   private final ICoverage parentCoverage;

   public MergeItemGroup(MergeType mergeType, ICoverage parentCoverage, Collection<IMergeItem> mergeItems, boolean isCheckable) {
      super(mergeType, isCheckable);
      this.parentCoverage = parentCoverage;
      this.mergeItems = mergeItems;
   }

   @Override
   public String toString() {
      return getMergeType().toString() + " - " + mergeItems.toString();
   }

   @Override
   public String getAssignees() throws OseeCoreException {
      return "";
   }

   @Override
   public Collection<? extends ICoverage> getChildren() {
      return mergeItems;
   }

   @Override
   public Collection<? extends ICoverage> getChildren(boolean recurse) {
      Set<ICoverage> children = new HashSet<ICoverage>();
      for (IMergeItem mergeItem : mergeItems) {
         if (mergeItem instanceof MergeItem && ((MergeItem) mergeItem).getImportItem() != null) {
            children.addAll(((MergeItem) mergeItem).getImportItem().getChildren(recurse));
         }
      }
      return children;
   }

   @Override
   public int getCoveragePercent() {
      Collection<? extends ICoverage> children = getChildren(true);
      Set<ICoverage> covered = new HashSet<ICoverage>();
      for (ICoverage coverage : children) {
         if (coverage.isCovered()) covered.add(coverage);
      }
      return CoverageUtil.getPercent(covered.size(), children.size(), true).getFirst();
   }

   @Override
   public String getCoveragePercentStr() {
      Collection<? extends ICoverage> children = getChildren(true);
      Set<ICoverage> covered = new HashSet<ICoverage>();
      for (ICoverage coverage : children) {
         if (coverage.isCovered()) covered.add(coverage);
      }
      return CoverageUtil.getPercent(covered.size(), children.size(), true).getSecond();
   }

   @Override
   public String getGuid() {
      return "";
   }

   @Override
   public String getLocation() {
      return "";
   }

   @Override
   public String getName() {
      return getMergeType().toString() + " - " + CoverageUtil.getFullPath(parentCoverage) + "[" + parentCoverage.getName() + "]";
   }

   @Override
   public String getNamespace() {
      return parentCoverage.getNamespace();
   }

   @Override
   public String getNotes() {
      return "";
   }

   @Override
   public KeyedImage getOseeImage() {
      return mergeItems.iterator().next().getOseeImage();
   }

   @Override
   public ICoverage getParent() {
      return parentCoverage;
   }

   @Override
   public String getFileContents() throws OseeCoreException {
      String str = parentCoverage.getFileContents();
      if (Strings.isValid(str)) {
         return str;
      }
      if (parentCoverage.getParent() != null) {
         return parentCoverage.getParent().getFileContents();
      }
      return "";
   }

   @Override
   public boolean isAssignable() {
      return false;
   }

   @Override
   public boolean isCovered() {
      return false;
   }

   @Override
   public Result isEditable() {
      return parentCoverage.isEditable();
   }

   @Override
   public boolean isFolder() {
      return false;
   }

   public Collection<IMergeItem> getMergeItems() {
      return mergeItems;
   }

   @Override
   public String getOrderNumber() {
      return "";
   }
}
