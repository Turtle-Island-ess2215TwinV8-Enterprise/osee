/*******************************************************************************
 * Copyright (c) 2010 Boeing.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boeing - initial API and implementation
 *******************************************************************************/
package org.eclipse.osee.ats.notify;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.osee.ats.core.client.workflow.AbstractWorkflowArtifact;
import org.eclipse.osee.framework.core.util.Result;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.skynet.core.artifact.Artifact;

/**
 * @author Donald G. Dunne
 */
public class EmailActionsData {

   private String subject;
   private String body;
   private final Set<Artifact> workflows = new HashSet<Artifact>(5);

   public String getSubject() {
      return subject;
   }

   public void setSubject(String subject) {
      this.subject = subject;
   }

   public String getBody() {
      return body;
   }

   public void setBody(String body) {
      this.body = body;
   }

   public Set<Artifact> getGroups() {
      return workflows;
   }

   public Result isValid() {
      if (!Strings.isValid(getSubject())) {
         return new Result("Must enter subject");
      }
      if (!Strings.isValid(getBody())) {
         return new Result("Must enter body");
      }
      if (workflows.isEmpty()) {
         return new Result("No workflows dropped");
      }
      for (Artifact workflow : workflows) {
         if (!(workflow instanceof AbstractWorkflowArtifact)) {
            return new Result("Only valid for Workflow Artifacts, not [%s]", workflow.getArtifactTypeName());
         }
      }
      return Result.TrueResult;
   }

   public String getHtmlResult() {
      StringBuilder html = new StringBuilder();

      html.append("<pre>");
      html.append(body);
      html.append("</pre>");

      return html.toString();
   }

   public Set<Artifact> getWorkflows() {
      return workflows;
   }
}
