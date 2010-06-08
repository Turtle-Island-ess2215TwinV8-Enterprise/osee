package org.eclipse.osee.framework.messaging.event.res;

import org.eclipse.osee.framework.messaging.MessageID;
import org.eclipse.osee.framework.messaging.event.res.msgs.RemoteBranchEvent1;
import org.eclipse.osee.framework.messaging.event.res.msgs.RemoteBroadcastEvent1;
import org.eclipse.osee.framework.messaging.event.res.msgs.RemotePersistEvent1;
import org.eclipse.osee.framework.messaging.event.res.msgs.RemoteTransactionEvent1;

public enum ResMessages implements MessageID {
   RemoteBranchEvent1(true, "Aylfa1wlKXIbX2gOrVgA", "topic:org.eclipse.osee.coverage.msgs.RemoteBranchEvent1", RemoteBranchEvent1.class, false),
   RemoteBroadcastEvent1(true, "Aylfa1y3ZBSIGbVU3JgA", "topic:org.eclipse.osee.coverage.msgs.RemoteBroadcastEvent1", RemoteBroadcastEvent1.class, false),
   RemotePersistEvent1(true, "AISIbRj0KGBv62x2pMAA", "topic:org.eclipse.osee.coverage.msgs.RemotePersistEvent1", RemotePersistEvent1.class, false),
   RemoteTransactionEvent1(true, "AAn_QHkqUhz3vJKwp8QA", "topic:org.eclipse.osee.coverage.msgs.RemoteTransactionEvent1", RemoteTransactionEvent1.class, false);

   private String name;
   private Class<?> clazz;
   boolean isReplyRequired;
   private String guid;
   private String destination;
   private boolean isTopic;

   ResMessages(boolean isTopic, String guid, String name, Class<?> clazz, boolean isReplyRequired) {
      this.guid = guid;
      this.name = name;
      this.clazz = clazz;
      this.isReplyRequired = isReplyRequired;
      this.isTopic = isTopic;
      if (isTopic) {
         destination = "topic:" + guid;
      } else {
         destination = guid;
      }
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public Class<?> getSerializationClass() {
      return clazz;
   }

   @Override
   public boolean isReplyRequired() {
      return isReplyRequired;
   }

   @Override
   public String getGuid() {
      return guid;
   }

   @Override
   public String getMessageDestination() {
      return destination;
   }

   public boolean isTopic() {
      return isTopic;
   }
}