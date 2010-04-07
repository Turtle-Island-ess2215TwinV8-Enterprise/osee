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

package org.eclipse.osee.framework.skynet.core.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.osee.framework.core.enums.ModificationType;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.exception.OseeDataStoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;
import org.eclipse.osee.framework.jdk.core.util.xml.Xml;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.Attribute;
import org.eclipse.osee.framework.skynet.core.internal.Activator;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.skynet.core.types.IArtifact;

/**
 * Utility methods for common tasks performed on Artifact's.
 * 
 * @author Robert A. Fisher
 * @author Donald G. Dunne
 */
public final class Artifacts {

   private Artifacts() {
      // This constructor is private because there is no reason to instantiate this class
   }

   public static List<String> toGuids(Collection<? extends IArtifact> artifacts) {
      List<String> guids = new ArrayList<String>(artifacts.size());
      for (IArtifact artifact : artifacts) {
         guids.add(artifact.getGuid());
      }
      return guids;
   }
   
   public static ModificationType getArtifactModType(Artifact artifact) throws OseeCoreException{
	   ModificationType modificationType = artifact.getModType();
	   
	   if(modificationType == ModificationType.NEW || modificationType == ModificationType.INTRODUCED){
		   for(Attribute<?> attribute : artifact.getAttributes()){
			   if(attribute.getModificationType() != ModificationType.NEW && 
					   attribute.getModificationType() != ModificationType.INTRODUCED){
				   modificationType = attribute.getModificationType();
				   break;
			   }
		   }
	   }
	   return modificationType;
   }

   public static String commaArts(Collection<? extends Artifact> artifacts) {
      return toTextList(artifacts, ", ");
   }

   public static String semmicolonArts(Collection<? extends Artifact> artifacts) {
      return toTextList(artifacts, "; ");
   }

   public static String toString(String separator, Collection<? extends Artifact> artifacts) {
      return toTextList(artifacts, separator);
   }

   public static String toTextList(Collection<? extends Artifact> artifacts, String separator) {
      StringBuilder sb = new StringBuilder();
      for (Artifact art : artifacts) {
         sb.append(art.getName());
         sb.append(separator);
      }
      if (sb.length() > separator.length()) {
         return sb.substring(0, sb.length() - separator.length());
      }
      return "";
   }

   public static Collection<String> artNames(Collection<? extends Artifact> arts) {
      ArrayList<String> names = new ArrayList<String>();
      for (Artifact art : arts) {
         names.add(art.getName());
      }
      return names;
   }

   public static void persistInTransaction(final Collection<? extends Artifact> artifacts) throws OseeCoreException {
      persistInTransaction(artifacts.toArray(new Artifact[artifacts.size()]));
   }

   public static void persistInTransaction(Artifact... artifacts) throws OseeCoreException {
      SkynetTransaction transaction =
            new SkynetTransaction(artifacts[0].getBranch(), "Artifacts.persistInTransaction(Artifact... artifacts)");
      for (Artifact art : artifacts) {
         art.persist(transaction);
      }
      transaction.execute();
   }

   public static void persistInTransaction(String comment, Artifact... artifacts) throws OseeCoreException {
      SkynetTransaction transaction = new SkynetTransaction(artifacts[0].getBranch(), comment);
      for (Artifact art : artifacts) {
         art.persist(transaction);
      }
      transaction.execute();
   }

   /**
    * Recurses default hierarchy and collections children of parentArtifact that are of type class
    * 
    * @param <A>
    * @param parentArtifact
    * @param children
    * @param clazz
    * @param recurse
    * @throws OseeDataStoreException
    */
   @SuppressWarnings("unchecked")
   public static <A extends Artifact> void getChildrenOfType(Artifact parentArtifact, Collection<A> children, Class<A> clazz, boolean recurse) throws OseeCoreException {
      for (Artifact child : parentArtifact.getChildren()) {
         if (child.getClass().equals(clazz)) {
            children.add((A) child);
            if (recurse) {
               getChildrenOfType(child, children, clazz, recurse);
            }
         }
      }
   }

   /**
    * @return Set of type class that includes parentArtifact and children and will recurse children if true
    */
   @SuppressWarnings("unchecked")
   public static <A extends Artifact> Set<A> getChildrenAndThisOfTypeSet(Artifact parentArtifact, Class<A> clazz, boolean recurse) throws OseeCoreException {
      Set<A> thisAndChildren = new HashSet<A>();
      if (parentArtifact.getClass().equals(clazz)) {
         thisAndChildren.add((A) parentArtifact);
      }
      getChildrenOfTypeSet(parentArtifact, clazz, recurse);
      return thisAndChildren;
   }

   @SuppressWarnings("unchecked")
   public static <A extends Artifact> Set<A> getChildrenOfTypeSet(Artifact parentArtifact, Class<A> clazz, boolean recurse) throws OseeCoreException {
      Set<A> children = new HashSet<A>();
      for (Artifact child : parentArtifact.getChildren()) {
         if (child.getClass().equals(clazz)) {
            children.add((A) child);
            if (recurse) {
               getChildrenOfType(child, children, clazz, recurse);
            }
         }
      }
      return children;
   }

   public static String getDetailsFormText(Artifact artifact) {
      String template = "<p><b>%s:</b> %s</p>";
      StringBuilder sb = new StringBuilder();
      sb.append("<form>");

      if (artifact != null) {
         sb.append(String.format(template, "GUID", Xml.escape(artifact.getGuid())));
         sb.append(String.format(template, "HRID", Xml.escape(artifact.getHumanReadableId())));
         sb.append(String.format(template, "Branch", Xml.escape(artifact.getBranch().toString())));
         sb.append(String.format(template, "Branch Id", artifact.getBranch().getId()));
         sb.append(String.format(template, "Artifact Id", artifact.getArtId()));
         sb.append(String.format(template, "Artifact Type Name", Xml.escape(artifact.getArtifactTypeName())));
         sb.append(String.format(template, "Artifact Type Id", artifact.getArtTypeId()));
         sb.append(String.format(template, "Gamma Id", artifact.getGammaId()));
         sb.append(String.format(template, "Historical", artifact.isHistorical()));
         sb.append(String.format(template, "Deleted", artifact.isDeleted()));
         sb.append(String.format(template, "Revision",
               (artifact.isInDb() ? artifact.getTransactionNumber() : "Not In Db")));
         sb.append(String.format(template, "Read Only", artifact.isReadOnly()));
         sb.append(String.format(template, "Last Modified",
               (artifact.isInDb() ? artifact.getLastModified() : "Not In Db")));
         try {
            sb.append(String.format(template, "Last Modified By",
                  (artifact.isInDb() ? artifact.getLastModifiedBy() : "Not In Db")));
         } catch (Exception ex) {
            OseeLog.log(Activator.class, Level.SEVERE, ex);
            sb.append(String.format(template, "Last Modified By", "Exception " + ex.getLocalizedMessage()));
         }
      } else {
         sb.append(String.format(template, "Artifact", "null"));
      }
      sb.append("</form>");
      return sb.toString();
   }

   public static HashCollection<Branch, Artifact> getBranchArtifactMap(Collection<Artifact> artifacts) throws OseeCoreException {
      HashCollection<Branch, Artifact> branchMap = new HashCollection<Branch, Artifact>();
      for (Artifact artifact : artifacts) {
         branchMap.put(artifact.getBranch(), artifact);
      }
      return branchMap;
   }
}