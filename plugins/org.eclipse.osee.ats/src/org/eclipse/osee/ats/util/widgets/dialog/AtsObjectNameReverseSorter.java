package org.eclipse.osee.ats.util.widgets.dialog;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osee.ats.core.model.IAtsObject;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * Default sorter for artifacts. Sorts on descriptive name
 */
public class AtsObjectNameReverseSorter extends ViewerSorter {

   /**
    * Default sorter for artifacts. Sorts on descriptive name
    */
   public AtsObjectNameReverseSorter() {
      super();
   }

   @Override
   @SuppressWarnings("unchecked")
   public int compare(Viewer viewer, Object o1, Object o2) {
      if (o1 instanceof IAtsObject && o2 instanceof IAtsObject) {
         return getComparator().compare(((IAtsObject) o2).getName(), ((IAtsObject) o1).getName());
      } else if (o1 instanceof Artifact && o2 instanceof Artifact) {
         return getComparator().compare(((Artifact) o2).getName(), ((Artifact) o1).getName());
      } else if (o1 instanceof String && o2 instanceof String) {
         return getComparator().compare(o2, o1);
      }
      return super.compare(viewer, o2, o1);
   }

}