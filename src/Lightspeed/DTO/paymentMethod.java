/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lightspeed.DTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author Luis Arce
 */
@XmlType(propOrder = {
    "type",
    "payment_method"
})
@XmlRootElement( name = "payment" )
public class paymentMethod {
    @XmlElement(name = "type")
    public String type;
    @XmlElement(name = "payment_method")
    public String payment_method;
    
    //Attrubutes
    @XmlAttribute( name = "uri", required = true )
    public String uri;
    @XmlAttribute( name = "id", required = true )
    public int id; 
}
