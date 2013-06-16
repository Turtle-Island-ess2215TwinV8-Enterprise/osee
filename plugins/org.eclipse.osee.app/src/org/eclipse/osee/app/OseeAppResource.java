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

import static org.eclipse.osee.framework.core.enums.BranchArchivedState.UNARCHIVED;
import static org.eclipse.osee.framework.core.enums.BranchState.CREATED;
import static org.eclipse.osee.framework.core.enums.BranchState.MODIFIED;
import static org.eclipse.osee.framework.core.enums.BranchType.BASELINE;
import static org.eclipse.osee.framework.core.enums.BranchType.WORKING;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.eclipse.osee.framework.core.enums.BranchType;
import org.eclipse.osee.framework.core.enums.CoreArtifactTypes;
import org.eclipse.osee.framework.core.enums.CoreAttributeTypes;
import org.eclipse.osee.framework.core.enums.CoreBranches;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.core.model.Branch;
import org.eclipse.osee.framework.core.model.cache.BranchFilter;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.eclipse.osee.orcs.OrcsApi;
import org.eclipse.osee.orcs.data.ArtifactReadable;
import org.eclipse.osee.orcs.search.QueryBuilder;
import org.eclipse.osee.orcs.search.QueryFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.QuirksMode;
import org.jsoup.nodes.Element;

/**
 * https://wikis.oracle.com/display/Jersey/Overview+of+JAX-RS+1.0+Features <br />
 * http://www.w3.org/2007/07/xhtml-basic-ref.html <br />
 * http://validator.w3.org/check <br />
 * http://sixrevisions.com/web-standards/20-html-best-practices-you-should-follow/ <br />
 * http://docs.oracle.com/javaee/6/tutorial/doc/gknav.html/ <br />
 * http://www.w3schools.com/tags/ref_colorpicker.asp <br />
 * http://download.oracle.com/javaee/6/api/javax/ws/rs/ApplicationPath.html.
 * 
 * @author Ryan D. Brooks
 */
public final class OseeAppResource {
   private final String appName;
   private final OrcsApi orcsApi;

   public OseeAppResource(String appName, OrcsApi orcsApi) {
      this.appName = appName;
      this.orcsApi = orcsApi;
   }

   @GET
   @Produces(MediaType.TEXT_HTML)
   public String getAppUserInterface() throws Exception {
      HtmlPageCreator html = new HtmlPageCreator();
      addRules(html);
      return html.realizePageAsString("ui/oseeApp.html");
   }

   private void getApp(HtmlPageCreator html) throws OseeCoreException, IOException {
      QueryFactory queryFactory = orcsApi.getQueryFactory(null);
      QueryBuilder query =
         queryFactory.fromBranch(CoreBranches.COMMON).andIsOfType(CoreArtifactTypes.OseeApp).andNameEquals(appName);

      ArtifactReadable app = query.getResults().getOneOrNull();
      if (app == null) {
         html.readSubstitutions("ui/" + appName + ".html");
      } else {
         InputStream stream = app.getSoleAttributeValue(CoreAttributeTypes.HTMLContent);
         //TODO: expose html.readSubstitutions for inputstreams
      }
   }

   private void addRules(HtmlPageCreator html) throws Exception {
      getApp(html);
      CharSequence widgets = html.getSubstitution("widgets");
      Document doc = Jsoup.parse(widgets.toString());
      doc.quirksMode(QuirksMode.noQuirks);

      CompositeRule compositeRule = new CompositeRule("dataLists");
      for (Element inputElement : doc.getElementsByTag("input")) {
         String listId = inputElement.attr("list");
         if (listId.equals("baselineBranches") || listId.equals("workingAndBaslineBranches")) {
            if (!compositeRule.ruleExists(listId)) {
               compositeRule.addRule(createBranchDatalistRule(listId));
            }
         }
      }
      html.addSubstitution(compositeRule);
   }

   private DataListRule createBranchDatalistRule(String listId) throws OseeCoreException {
      BranchType[] branchTypes =
         listId.equals("baselineBranches") ? new BranchType[] {BASELINE} : new BranchType[] {BASELINE, WORKING};
      BranchFilter branchFilter = new BranchFilter(UNARCHIVED, branchTypes);
      branchFilter.setBranchStates(CREATED, MODIFIED);

      List<Branch> branches = orcsApi.getBranchCache().getBranches(branchFilter);
      String[][] options = new String[branches.size()][2];
      int i = 0;

      for (Branch branch : branches) {
         options[i][0] = branch.getName();
         options[i++][1] = branch.getGuid();
      }
      return new DataListRule(options, listId);
   }

   @Path("source")
   @GET
   @Produces(MediaType.TEXT_HTML)
   public String getSource() throws Exception {
      HtmlPageCreator html = new HtmlPageCreator();
      html.addStringSubstitution("title", appName);
      return html.realizePageAsString("ui/oseeAppSource.html");
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("status")
   public Response createResult() {
      StreamingOutput streamingOutput = new StreamingOutput() {
         @Override
         public void write(OutputStream output) throws WebApplicationException {
            try {
               // TODO: use return object annotated for serialization instead of JSON library 
               long startTime = System.currentTimeMillis();
               for (int percentComplete = 0; percentComplete <= 100; percentComplete += 10) {
                  output.write(createJsonStatus(percentComplete, startTime).getBytes("UTF-8"));
                  output.write('\n');
                  output.flush();
                  Thread.sleep(100);
               }
            } catch (Exception ex) {
               throw new WebApplicationException(ex);
            }
         }
      };

      URI uri = URI.create("unitOfWorkId");
      return Response.created(uri).entity(streamingOutput).build();
   }

   private String createJsonStatus(int percentComplete, long startTime) throws JSONException {
      JSONObject jsonObj = new JSONObject();
      //if status has changed

      String status;
      String state;
      if (percentComplete >= 100) {
         status = "Completed in " + Lib.getElapseString(startTime) + " seconds";
         state = "done";
      } else {
         status = percentComplete + " %";
         state = "running";
      }
      jsonObj.put("status", status);
      jsonObj.put("state", state);

      // if new message has been logged by app
      jsonObj.put("message", "Wow look at the time: " + System.currentTimeMillis());
      return jsonObj.toString();
   }
}