//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.09.20 at 02:51:29 PM MST 
//


package org.eclipse.osee.coverage.msgs;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.eclipse.osee.coverage.msgs package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CoveragePackageEvent1_QNAME = new QName("", "CoveragePackageEvent1");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.eclipse.osee.coverage.msgs
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CoveragePackageEvent1 }
     * 
     */
    public CoveragePackageEvent1 createCoveragePackageEvent1() {
        return new CoveragePackageEvent1();
    }

    /**
     * Create an instance of {@link CoverageChange1 }
     * 
     */
    public CoverageChange1 createCoverageChange1() {
        return new CoverageChange1();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CoveragePackageEvent1 }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "CoveragePackageEvent1")
    public JAXBElement<CoveragePackageEvent1> createCoveragePackageEvent1(CoveragePackageEvent1 value) {
        return new JAXBElement<CoveragePackageEvent1>(_CoveragePackageEvent1_QNAME, CoveragePackageEvent1 .class, null, value);
    }

}
