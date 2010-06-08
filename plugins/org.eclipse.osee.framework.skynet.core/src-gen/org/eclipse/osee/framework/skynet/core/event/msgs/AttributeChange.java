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
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.messaging.event.res.AttributeEventModificationType;
import org.eclipse.osee.framework.skynet.core.attribute.AttributeTypeManager;
import org.eclipse.osee.framework.skynet.core.event2.FrameworkEvent;

/**
 * <p>
 * Java class for AttributeChange complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttributeChange">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="attrTypeGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="modTypeGuid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="attributeId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="gammaId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="data" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttributeChange", propOrder = {"attrTypeGuid", "modTypeGuid", "attributeId", "gammaId", "data"})
public class AttributeChange extends FrameworkEvent {

   @XmlElement(required = true)
   protected String attrTypeGuid;
   @XmlElement(required = true)
   protected String modTypeGuid;
   protected int attributeId;
   protected int gammaId;
   @XmlElement(required = true)
   protected List<String> data;

   /**
    * Gets the value of the attrTypeGuid property.
    * 
    * @return possible object is {@link String }
    */
   public String getAttrTypeGuid() {
      return attrTypeGuid;
   }

   /**
    * Sets the value of the attrTypeGuid property.
    * 
    * @param value allowed object is {@link String }
    */
   public void setAttrTypeGuid(String value) {
      this.attrTypeGuid = value;
   }

   /**
    * Gets the value of the modTypeGuid property.
    * 
    * @return possible object is {@link String }
    */
   public String getModTypeGuid() {
      return modTypeGuid;
   }

   /**
    * Sets the value of the modTypeGuid property.
    * 
    * @param value allowed object is {@link String }
    */
   public void setModTypeGuid(String value) {
      this.modTypeGuid = value;
   }

   /**
    * Gets the value of the attributeId property.
    */
   public int getAttributeId() {
      return attributeId;
   }

   /**
    * Sets the value of the attributeId property.
    */
   public void setAttributeId(int value) {
      this.attributeId = value;
   }

   /**
    * Gets the value of the gammaId property.
    */
   public int getGammaId() {
      return gammaId;
   }

   /**
    * Sets the value of the gammaId property.
    */
   public void setGammaId(int value) {
      this.gammaId = value;
   }

   /**
    * Gets the value of the data property.
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the data property.
    * <p>
    * For example, to add a new item, do as follows:
    * 
    * <pre>
    * getData().add(newItem);
    * </pre>
    * <p>
    * Objects of the following type(s) are allowed in the list {@link String }
    */
   public List<String> getData() {
      if (data == null) {
         data = new ArrayList<String>();
      }
      return this.data;
   }

   public String toString() {
      try {
         return String.format("[AttrChg: %s - %s - %s]", AttributeEventModificationType.getType(modTypeGuid),
               AttributeTypeManager.getTypeByGuid(attrTypeGuid), data);
      } catch (OseeCoreException ex) {
         return "Exception: " + ex.getLocalizedMessage();
      }
   }
}