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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.osee.framework.core.exception.OseeArgumentException;
import org.eclipse.osee.framework.jdk.core.util.Lib;
import org.osgi.framework.FrameworkUtil;

/**
 * http://www.w3.org/2007/07/xhtml-basic-ref.html <br />
 * http://validator.w3.org/check <br />
 * 
 * @author Ryan D. Brooks
 */
public final class HtmlPageCreator {
   private static final Pattern pathPattern = Pattern.compile("path\\s*=\\s*\"(.*?)\"");
   private static final Pattern xmlProcessingInstructionStartOrEnd = Pattern.compile("(\\?>)|(\\s*<\\?\\s*)");
   private final Class<?> clazz;
   private final StringBuilder htmlPage;
   private final HashMap<String, AppendableRule> substitutions = new HashMap<String, AppendableRule>();

   public HtmlPageCreator() {
      this(HtmlPageCreator.class);
   }

   public HtmlPageCreator(Class<?> clazz) {
      this(clazz, new StringBuilder(7000));
   }

   public HtmlPageCreator(Class<?> clazz, StringBuilder htmlPage) {
      this.clazz = clazz;
      this.htmlPage = htmlPage;
   }

   public void addSubstitution(AppendableRule rule) {
      substitutions.put(rule.getName(), rule);
   }

   public String getSubstitution(String ruleName) {
      return substitutions.get(ruleName).toString();
   }

   public void addStringSubstitution(String ruleName, String substitution) {
      addSubstitution(new StringRule(ruleName, substitution));
   }

   /**
    * Create valid xhtml exception page that includes the full stack trace and even return a reasonable error page if
    * that fails too.
    */
   public void toExceptionPage(Exception ex) {
      try {
         addStringSubstitution("title", ex.toString());
         addStringSubstitution("stacktrace", Lib.exceptionToString(ex));
         InputStream templateStream = getInputStreamFromFile("ui/exception.html");
         realizePage(templateStream);
      } catch (Exception ex1) {
         htmlPage.setLength(0);
         htmlPage.append("<pre>");
         htmlPage.append(StringEscapeUtils.escapeHtml(Lib.exceptionToString(ex)));
         htmlPage.append("</pre>");
      }
   }

   public void readSubstitutions(String path) throws IOException {
      InputStream keyValueStream = getInputStreamFromFile(path);
      Scanner scanner = new Scanner(keyValueStream, "UTF-8");
      scanner.useDelimiter(xmlProcessingInstructionStartOrEnd);
      while (scanner.hasNext()) {
         addStringSubstitution(scanner.next(), scanner.next());
      }
      scanner.close();
   }

   public String realizePageAsString(String path) throws Exception {
      realizePage(path);
      return toString();
   }

   private void realizePage(InputStream template) throws Exception {
      Scanner scanner = new Scanner(template, "UTF-8");
      scanner.useDelimiter(xmlProcessingInstructionStartOrEnd);

      boolean isProcessingInstruction = true;
      while (scanner.hasNext()) {
         processToken(substitutions, scanner.next(), isProcessingInstruction);
         isProcessingInstruction = !isProcessingInstruction;
      }
      scanner.close();
   }

   private InputStream getInputStreamFromFile(String path) throws IOException {
      return getInputStreamFromFile(clazz, path);
   }

   private InputStream getInputStreamFromFile(Class<?> clazz, String path) throws IOException {
      return FrameworkUtil.getBundle(clazz).getResource(path).openStream();
   }

   private void loadFileInto(String path) throws IOException {
      Lib.inputStreamToStringBuilder(getInputStreamFromFile(path), htmlPage);
   }

   private void realizePage(String path) throws Exception {
      realizePage(getInputStreamFromFile(path));
   }

   private void processToken(HashMap<String, AppendableRule> substitutions, String token, boolean isProcessingInstruction) throws IOException, OseeArgumentException {
      if (isProcessingInstruction) {
         Matcher pathMatcher = pathPattern.matcher(token);
         if (pathMatcher.find()) {
            handleInclude(pathMatcher.group(1));
         } else {
            AppendableRule rule = substitutions.get(token);
            if (rule == null) {
               throw new OseeArgumentException("no substitution was found for token %s", token);
            }
            rule.applyTo(htmlPage);
         }
      } else {
         htmlPage.append(token);
      }
   }

   private void handleInclude(String path) throws IOException {
      boolean css = path.endsWith(".css");
      htmlPage.append(css ? "\n/* " : "\n<!-- ");
      htmlPage.append(path);
      htmlPage.append(css ? " */\n" : " -->\n");
      loadFileInto(path);
      htmlPage.append('\n');
   }

   @Override
   public String toString() {
      return htmlPage.toString();
   }
}