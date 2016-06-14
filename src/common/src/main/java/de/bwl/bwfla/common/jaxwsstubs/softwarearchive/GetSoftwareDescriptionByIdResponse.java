
package de.bwl.bwfla.common.jaxwsstubs.softwarearchive;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import de.bwl.bwfla.common.datatypes.SoftwareDescription;


/**
 * <p>Java class for getSoftwareDescriptionByIdResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getSoftwareDescriptionByIdResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://bwfla.bwl.de/common/datatypes}softwareDescription" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getSoftwareDescriptionByIdResponse", propOrder = {
    "_return"
})
public class GetSoftwareDescriptionByIdResponse {

    @XmlElement(name = "return")
    protected SoftwareDescription _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link SoftwareDescription }
     *     
     */
    public SoftwareDescription getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link SoftwareDescription }
     *     
     */
    public void setReturn(SoftwareDescription value) {
        this._return = value;
    }

}
