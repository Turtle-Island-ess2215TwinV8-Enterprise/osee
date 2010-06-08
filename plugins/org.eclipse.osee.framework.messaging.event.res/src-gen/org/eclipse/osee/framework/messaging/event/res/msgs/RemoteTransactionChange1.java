//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.05.28 at 02:08:30 PM MST 
//


package org.eclipse.osee.framework.messaging.event.res.msgs;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.eclipse.osee.framework.messaging.event.res.RemoteEvent;


/**
 * <p>Java class for RemoteTransactionChange1 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RemoteTransactionChange1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="branchGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transactionId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="artifacts" type="{}RemoteBasicGuidArtifact1" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RemoteTransactionChange1", propOrder = {
    "branchGuid",
    "transactionId",
    "artifacts"
})
public class RemoteTransactionChange1
    extends RemoteEvent
{

    @XmlElement(required = true)
    protected String branchGuid;
    protected int transactionId;
    @XmlElement(required = true)
    protected List<RemoteBasicGuidArtifact1> artifacts;

    /**
     * Gets the value of the branchGuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBranchGuid() {
        return branchGuid;
    }

    /**
     * Sets the value of the branchGuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBranchGuid(String value) {
        this.branchGuid = value;
    }

    /**
     * Gets the value of the transactionId property.
     * 
     */
    public int getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the value of the transactionId property.
     * 
     */
    public void setTransactionId(int value) {
        this.transactionId = value;
    }

    /**
     * Gets the value of the artifacts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the artifacts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArtifacts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoteBasicGuidArtifact1 }
     * 
     * 
     */
    public List<RemoteBasicGuidArtifact1> getArtifacts() {
        if (artifacts == null) {
            artifacts = new ArrayList<RemoteBasicGuidArtifact1>();
        }
        return this.artifacts;
    }

}