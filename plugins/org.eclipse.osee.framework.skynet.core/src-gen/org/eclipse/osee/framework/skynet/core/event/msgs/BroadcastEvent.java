//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.05.13 at 02:25:18 PM MST 
//


package org.eclipse.osee.framework.skynet.core.event.msgs;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.osee.framework.skynet.core.event2.FrameworkEvent;


/**
 * <p>Java class for BroadcastEvent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BroadcastEvent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="eventTypeGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="userIds" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="networkSender" type="{}NetworkSender"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BroadcastEvent", propOrder = {
    "eventTypeGuid",
    "userIds",
    "message",
    "networkSender"
})
public class BroadcastEvent
    extends FrameworkEvent
{

    @XmlElement(required = true)
    protected String eventTypeGuid;
    @XmlElement(required = true)
    protected List<String> userIds;
    @XmlElement(required = true)
    protected String message;
    @XmlElement(required = true)
    protected NetworkSender networkSender;

    /**
     * Gets the value of the eventTypeGuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEventTypeGuid() {
        return eventTypeGuid;
    }

    /**
     * Sets the value of the eventTypeGuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEventTypeGuid(String value) {
        this.eventTypeGuid = value;
    }

    /**
     * Gets the value of the userIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getUserIds() {
        if (userIds == null) {
            userIds = new ArrayList<String>();
        }
        return this.userIds;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Gets the value of the networkSender property.
     * 
     * @return
     *     possible object is
     *     {@link NetworkSender }
     *     
     */
    public NetworkSender getNetworkSender() {
        return networkSender;
    }

    /**
     * Sets the value of the networkSender property.
     * 
     * @param value
     *     allowed object is
     *     {@link NetworkSender }
     *     
     */
    public void setNetworkSender(NetworkSender value) {
        this.networkSender = value;
    }

}