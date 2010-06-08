package org.eclipse.osee.framework.skynet.core.event2;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.osee.framework.core.data.DefaultBasicGuidArtifact;
import org.eclipse.osee.framework.skynet.core.event.msgs.NetworkSender;

public class TransactionEvent extends FrameworkEvent {

   private TransactionEventType eventType;
   private List<TransactionChange> transactions;
   private NetworkSender networkSender;

   /**
    * Gets the value of the artifacts property.
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the artifacts property.
    * <p>
    * For example, to add a new item, do as follows:
    * 
    * <pre>
    * getArtifacts().add(newItem);
    * </pre>
    * <p>
    * Objects of the following type(s) are allowed in the list {@link DefaultBasicGuidArtifact }
    */
   public List<TransactionChange> getTransactions() {
      if (transactions == null) {
         transactions = new ArrayList<TransactionChange>();
      }
      return this.transactions;
   }

   /**
    * Gets the value of the networkSender property.
    * 
    * @return possible object is {@link NetworkSender }
    */
   public NetworkSender getNetworkSender() {
      return networkSender;
   }

   /**
    * Sets the value of the networkSender property.
    * 
    * @param value allowed object is {@link NetworkSender }
    */
   public void setNetworkSender(NetworkSender value) {
      this.networkSender = value;
   }

   public TransactionEventType getEventType() {
      return eventType;
   }

   public void setEventType(TransactionEventType eventType) {
      this.eventType = eventType;
   }

}