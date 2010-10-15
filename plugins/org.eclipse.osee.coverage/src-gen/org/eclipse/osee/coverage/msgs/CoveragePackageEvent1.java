//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.09.20 at 02:51:29 PM MST 
//


package org.eclipse.osee.coverage.msgs;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CoveragePackageEvent1 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CoveragePackageEvent1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="package" type="{}CoverageChange1"/>
 *         &lt;element name="coverages" type="{}CoverageChange1" maxOccurs="unbounded"/>
 *         &lt;element name="sessionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoveragePackageEvent1", propOrder = {
    "_package",
    "coverages",
    "sessionId"
})
public class CoveragePackageEvent1 {

    @XmlElement(name = "package", required = true)
    protected CoverageChange1 _package;
    @XmlElement(required = true)
    protected List<CoverageChange1> coverages;
    @XmlElement(required = true)
    protected String sessionId;

    /**
     * Gets the value of the package property.
     * 
     *     possible object is
     *     {@link CoverageChange1 }
     *     
     */
    public CoverageChange1 getPackage() {
        return _package;
    }

    /**
     * Sets the value of the package property.
     * 
     *     allowed object is
     *     {@link CoverageChange1 }
     *     
     */
    public void setPackage(CoverageChange1 value) {
        this._package = value;
    }

    /**
     * Gets the value of the coverages property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the coverages property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCoverages().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CoverageChange1 }
     * 
     * 
     */
    public List<CoverageChange1> getCoverages() {
        if (coverages == null) {
            coverages = new ArrayList<CoverageChange1>();
        }
        return this.coverages;
    }

    /**
     * Gets the value of the sessionId property.
     * 
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the value of the sessionId property.
     * 
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

}
