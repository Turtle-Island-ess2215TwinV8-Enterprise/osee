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
package org.eclipse.osee.framework.jdk.core.util;

import java.io.File;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * @author Michael A. Winston
 */
public class AEmail extends MimeMessage {
   protected static final String emailType = "mail.smtp.host";
   protected static final String HTMLHead = "<html><body>\n";
   protected static final String HTMLEnd = "</body></html>\n";

   public static final String plainText = "text/plain";
   public static final String HTMLText = "text/html";

   private String body = null;
   private String bodyType = null;
   private Multipart mainMessage;

   /**
    * Default constructor
    */
   public AEmail() {
      super(getSession());
      mainMessage = new MimeMultipart();
   }

   /**
    * Constructs an AEmail with the given arguments
    * 
    * @param recipients - a list of valid addresses to send the message TO
    * @param from - the sender of the message
    * @param replyTo - a valid address of who the message should reply to
    * @param subject - the subject of the message
    */
   public AEmail(String[] recipients, String from, String replyTo, String subject) {
      this(recipients, from, replyTo, subject, null);
   }

   /**
    * Constructs an AEmail with the given arguments
    * 
    * @param recipients - a list of valid addresses to send the message TO
    * @param from - the sender of the message
    * @param replyTo - a valid address of who the message should reply to
    * @param subject - the subject of the message
    * @param textBody - the plain text of the body
    */
   public AEmail(String[] recipients, String from, String replyTo, String subject, String textBody) {
      this();
      try {
         setRecipients(recipients);
         setFrom(from);
         setSubject(subject);
         setReplyTo(replyTo);

         if (textBody != null) setBody(textBody);

      } catch (MessagingException e) {
         e.printStackTrace();
      }
   }

   /**
    * Constructs an AEmail with the given arguments
    * 
    * @param fromToReplyEmail - recipient email, from email and replyTo email address
    * @param subject - the subject of the message
    * @param textBody - the plain text of the body
    */
   public AEmail(String fromToReplyEmail, String subject, String textBody) {
      this(new String[] {fromToReplyEmail}, fromToReplyEmail, fromToReplyEmail, subject, textBody);
   }

   /**
    * Adds a single address to the recipient list
    * 
    * @param addresses - a valid address to send the message TO
    * @throws MessagingException
    */
   public void addRecipients(String addresses) throws MessagingException {
      addRecipients(Message.RecipientType.TO, addresses);
   }

   /**
    * Adds a list of addresses to the recipient list
    * 
    * @param addresses - a list of valid addresses to send the message TO
    * @throws MessagingException
    */
   public void addRecipients(String[] addresses) throws MessagingException {
      addRecipients(Message.RecipientType.TO, addresses);
   }

   /**
    * Adds a list of addresses to the corresponding recipient list
    * 
    * @param type - specifies which field the address should be put in
    * @param addresses - a list of valid addresses to send the message
    * @throws MessagingException
    */
   public void addRecipients(Message.RecipientType type, String[] addresses) throws MessagingException {
      if (addresses != null) {

         InternetAddress newAddresses[] = new InternetAddress[addresses.length];

         for (int i = 0; i < addresses.length; i++) {
            newAddresses[i] = new InternetAddress(addresses[i]);
         }

         addRecipients(type, newAddresses);
      }
   }

   /**
    * Sets the recipient TO field
    * 
    * @param addresses - a valid address to send the message TO
    * @throws MessagingException
    */
   public void setRecipients(String addresses) throws MessagingException {
      setRecipients(Message.RecipientType.TO, addresses);
   }

   /**
    * Sets a list of addresses to the recipient list
    * 
    * @param addresses - a list of valid addresses to send the message TO
    * @throws MessagingException
    */
   public void setRecipients(String[] addresses) throws MessagingException {
      setRecipients(Message.RecipientType.TO, addresses);
   }

   /**
    * Sets a list of addresses to the corresponding recipient list
    * 
    * @param type - specifies which field the address should be put in
    * @param addresses - a list of valid addresses to send the message
    * @throws MessagingException
    */
   public void setRecipients(Message.RecipientType type, String[] addresses) throws MessagingException {
      if (addresses != null) {

         InternetAddress newAddresses[] = new InternetAddress[addresses.length];

         for (int i = 0; i < addresses.length; i++) {
            newAddresses[i] = new InternetAddress(addresses[i]);
         }

         setRecipients(type, newAddresses);
      }
   }

   /**
    * Sets the from address
    * 
    * @param address - the user name the message is from
    * @throws AddressException
    * @throws MessagingException
    */
   // Set all the From Values
   public void setFrom(String address) throws AddressException, MessagingException {
      setFrom(new InternetAddress(address));
   }

   /**
    * Sets the address to reply to (if different than the from addresss)
    * 
    * @param address - a valid address to reply to
    * @throws MessagingException
    */
   public void setReplyTo(String address) throws MessagingException {
      InternetAddress replyAddresses[] = new InternetAddress[1];
      replyAddresses[0] = new InternetAddress(address);
      setReplyTo(replyAddresses);
   }

   /**
    * Gets the current Body Type of the message. NULL if one is not selected yet.
    * 
    * @return A String representation of the current Body Type
    */
   // Set the Body
   public String getBodyType() {
      return bodyType;
   }

   /**
    * Sets the text in the body of the message.
    * 
    * @param text - the text to for the body of the message
    */
   public void setBody(String text) {
      body = text;
      bodyType = plainText;
   }

   /**
    * Adds text to the body if the Body Type is "plain". If the body doesn't exist yet, then calls setBody.
    * 
    * @param text - the text to add to the body
    */
   public void addBody(String text) {
      if (bodyType == null)
         setBody(text);
      else if (bodyType.equals(plainText)) body += text;
   }

   /**
    * Sets the text in the body of the HTML message. This will already add the &lthtml&gt&ltbody&gt and
    * &lt/body&gt&lt/html&gt tags.
    * 
    * @param htmlText - the text for the body of the HTML message
    */
   public void setHTMLBody(String htmlText) {
      bodyType = HTMLText;
      body = HTMLHead + htmlText;
   }

   /**
    * Adds text to the HTML body if the Body Type is "html". If the body doesn't exist yet, then calls setHTMLBody.
    * 
    * @param htmlText - the text to add to the HTML body
    */
   public void addHTMLBody(String htmlText) {
      if (bodyType == null)
         setHTMLBody(htmlText);
      else if (bodyType.equals(HTMLText)) body += htmlText;

   }

   /**
    * Sends the message.
    */
   public void send() {
      SendThread sendThread = new SendThread(this);
      sendThread.start();
   }

   private class SendThread extends Thread {

      private final AEmail email;

      public SendThread(AEmail email) {
         this.email = email;
      }

      @Override
      public void run() {
         super.run();
         email.sendLocalThread();
      }
   }

   public void sendLocalThread() {
      MimeBodyPart messageBodyPart = new MimeBodyPart();

      if (bodyType == null) {
         bodyType = plainText;
         body = "";
      } else if (bodyType.equals(HTMLText)) body += HTMLEnd;

      try {
         messageBodyPart.setContent(body, bodyType);
         mainMessage.addBodyPart(messageBodyPart, 0);
         setContent(mainMessage);
         Transport.send(this);
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }

   private static String getMailServer() {
      IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();

      if (extensionRegistry == null) {
         throw new IllegalStateException("The extension registry is unavailable");
      }

      String extensionPointId = "org.eclipse.osee.framework.jdk.core.DefaultMailServer";
      IExtensionPoint point = extensionRegistry.getExtensionPoint(extensionPointId);
      if (point == null) {
         throw new IllegalArgumentException("The extension point " + extensionPointId + " does not exist");
      }

      for (IExtension extension : point.getExtensions()) {
         IConfigurationElement[] elements = extension.getConfigurationElements();

         for (IConfigurationElement element : elements) {
            return element.getAttribute("serverAddress");
         }
      }
      throw new IllegalStateException(
            "No mail server defined.  Use the extension point " + extensionPointId + " to define one.");
   }

   /**
    * Gets the current session
    * 
    * @return the Current SMTP Session
    */
   private static Session getSession() {
      Properties props = System.getProperties();
      props.put(emailType, getMailServer());

      return Session.getDefaultInstance(props, null);
   }

   /**
    * Adds an attachment to an email
    * 
    * @param source
    * @param attachmentName
    * @throws MessagingException
    */
   public void addAttachment(DataSource source, String attachmentName) throws MessagingException {
      MimeBodyPart messageBodyPart = new MimeBodyPart();
      messageBodyPart.setDataHandler(new DataHandler(source));
      messageBodyPart.setFileName(attachmentName);
      mainMessage.addBodyPart(messageBodyPart);
   }

   public void addAttachment(File file) throws MessagingException {
      addAttachment(new FileDataSource(file), file.getName());
   }

   public void addAttachment(String contents, String attachmentName) throws MessagingException {
      addAttachment(new StringDataSource(contents, attachmentName), attachmentName);
   }
}
