
package de.bwl.bwfla.common.jaxwsstubs.softwarearchive;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.bwl.bwfla.common.datatypes.SoftwarePackage;


/**
 * <p>Java class for getSoftwarePackageByIdResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getSoftwarePackageByIdResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://bwfla.bwl.de/common/datatypes}softwarePackage" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getSoftwarePackageByIdResponse", propOrder = {
    "_return"
})
public class GetSoftwarePackageByIdResponse {

    @XmlElement(name = "return", namespace = "")
    protected SoftwarePackage _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link SoftwarePackage }
     *     
     */
    public SoftwarePackage getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link SoftwarePackage }
     *     
     */
    public void setReturn(SoftwarePackage value) {
        this._return = value;
    }

}
