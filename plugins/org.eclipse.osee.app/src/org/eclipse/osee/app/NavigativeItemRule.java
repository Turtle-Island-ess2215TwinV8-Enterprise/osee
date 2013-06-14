/*******************************************************************************
 * Copyright (c) 2013 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.app;

import java.io.IOException;

/**
 * @author Donald G. Dunne
 */
public final class NavigativeItemRule extends AppendableRule {

   public NavigativeItemRule(String ruleName) {
      super(ruleName);
   }

   @Override
   public void applyTo(Appendable appendable) throws IOException {
      addNavListAdminItems(appendable);
      addNavListUserItems(appendable);
      addNavListTeamOseeItems(appendable);
      addNavListTeamCeeItems(appendable);
   }

   private void addNavListUserItems(Appendable items) throws IOException {
      items.append("<li><strong>Favorites</strong></li><ul>\n");
      items.append("<li><a href=\"apps/SystemSafety\">System Safety Report</a></li>\n");
      items.append("</ul>");
   }

   private void addNavListTeamOseeItems(Appendable items) throws IOException {
      items.append("<li><strong>Team - OSEE</strong></li><ul>\n");
      items.append("<li><a href=\"https://sun817.msc.az.boeing.com:1158/em/console/logon/logon\">Oracle Enterprise Mgr - 817-lba9</a></li>\n");
      items.append("<li><a href=\"https://dev.eclipse.org/portal/myfoundation/portal/portal.php\">OSEE Committer Tools</a></li>\n");
      items.append("<li><a href=\"osee-build.msc.az.boeing.com:9050\">OSEE SDC Installs</a></li>\n");
      items.append("<li><a href=\"https://apache.msc.az.boeing.com/wiki/lba/index.php/Main_Page\">OSEE LBA Wiki</a></li>\n");
      items.append("<li><a href=\"http://wiki.eclipse.org/OSEE\">OSEE ORG Wiki</a></li>\n");
      items.append("<li><a href=\"https://apache.msc.az.boeing.com/wiki/lba/index.php/Main_Page\">OSEE LBA Gerrit</a></li>\n");
      items.append("<li><a href=\"https://git.eclipse.org/r/#/q/is:watched+status:open,n,z\">OSEE ORG Gerrit</a></li>\n");
      items.append("</ul>");
   }

   private void addNavListTeamCeeItems(Appendable items) throws IOException {
      items.append("<li><strong>Team - CEE</strong></li><ul>\n");
      items.append("<li><a href=\"https://apache.msc.az.boeing.com/wiki/lba/index.php/CEE_Cell_Phone_Coverage\">CEE Cell Phone Coverage</a></li>\n");
      items.append("<li><a href=\"\\\\sw\\mes\\MSA\\ipt-cee\">CEE ipt folder</a></li>\n");
      items.append("</ul>");
   }

   private void addNavListAdminItems(Appendable items) throws IOException {
      // TODO  if (isAdmin) {
      items.append("<li><strong style=\"color:red\">Admin</strong></li><ul>\n");
      items.append("<li><a href=\"https://osee-build.msc.az.boeing.com/hudson/\">Hudson</a></li>\n");
      items.append("<li><a href=\"http://osee.msc.az.boeing.com:8015/osee/manager\">Session Search</a></li>\n");
      items.append("</ul>");
      // TODO } // end isAdmin
   }

}