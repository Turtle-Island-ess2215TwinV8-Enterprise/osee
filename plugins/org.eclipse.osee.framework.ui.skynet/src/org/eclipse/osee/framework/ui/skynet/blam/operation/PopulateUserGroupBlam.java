/*
 * Created on Dec 8, 2007
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */

package org.eclipse.osee.framework.ui.skynet.blam.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreRelationTypes;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;
import org.eclipse.osee.framework.skynet.core.artifact.BranchManager;
import org.eclipse.osee.framework.skynet.core.artifact.search.ArtifactQuery;
import org.eclipse.osee.framework.skynet.core.transaction.SkynetTransaction;
import org.eclipse.osee.framework.ui.skynet.blam.AbstractBlam;
import org.eclipse.osee.framework.ui.skynet.blam.VariableMap;
import org.eclipse.osee.framework.ui.skynet.widgets.XArtifactList;
import org.eclipse.osee.framework.ui.skynet.widgets.XModifiedListener;
import org.eclipse.osee.framework.ui.skynet.widgets.XWidget;
import org.eclipse.osee.framework.ui.skynet.widgets.workflow.DynamicXWidgetLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Ryan D. Brooks
 */
public class PopulateUserGroupBlam extends AbstractBlam {
   HashMap<String, User> emailToUser = new HashMap<String, User>();

   @Override
   public String getName() {
      return "Populate User Group";
   }

   @Override
   public void runOperation(VariableMap variableMap, IProgressMonitor monitor) throws OseeCoreException {
      String emailAddresses = variableMap.getString("Email Addresses");
      Collection<Artifact> groups = variableMap.getCollection(Artifact.class, "User Groups");

      emailToUser.clear();
      for (User user : UserManager.getUsers()) {
         emailToUser.put(user.getSoleAttributeValue(CoreAttributeTypes.EMAIL, ""), user);
      }

      List<User> users = new ArrayList<User>();
      int count = 0;
      for (String emailAddress : emailAddresses.split("[\n\r\t ,;]+")) {
         User user = emailToUser.get(emailAddress);
         count++;
         if (user == null) {
            println("User does not exist for: " + emailAddress);
         } else {
            users.add(user);
         }
      }
      println("addresses: " + count);

      SkynetTransaction transaction = new SkynetTransaction(BranchManager.getCommonBranch(), getName());
      for (Artifact group : groups) {
         for (User user : users) {
            group.addRelation(CoreRelationTypes.Users_User, user);
         }
         group.persist(transaction);
      }
      transaction.execute();
   }

   @Override
   public void widgetCreating(XWidget xWidget, FormToolkit toolkit, Artifact art, DynamicXWidgetLayout dynamicXWidgetLayout, XModifiedListener modListener, boolean isEditable) throws OseeCoreException {
      super.widgetCreating(xWidget, toolkit, art, dynamicXWidgetLayout, modListener, isEditable);
      if (xWidget.getLabel().equals("User Groups")) {
         XArtifactList listViewer = (XArtifactList) xWidget;
         listViewer.setInputArtifacts(ArtifactQuery.getArtifactListFromType(CoreArtifactTypes.UserGroup,
               BranchManager.getCommonBranch()));
      }
   }

   @Override
   public String getXWidgetsXml() {
      return "<xWidgets><XWidget xwidgetType=\"XArtifactList\" displayName=\"User Groups\" multiSelect=\"true\" /><XWidget xwidgetType=\"XCheckBox\" horizontalLabel=\"true\" labelAfter=\"true\" displayName=\"Body is html\" /><XWidget xwidgetType=\"XText\" displayName=\"Email Addresses\" fill=\"Vertically\" /></xWidgets>";
   }

   @Override
   public String getDescriptionUsage() {
      return "populate user group(s) based on list of emails";
   }

   @Override
   public Collection<String> getCategories() {
      return Arrays.asList("Util");
   }
}