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
 * @author Universal
 */
public class InvoiceTotals {
    @XmlElement(name = "subtotal")
    public String subtotal;
    
    @XmlElement(name = "tax")
    public String tax;
    
    @XmlElement(name = "total")
    public String total;
    
    @XmlElement(name = "paid")
    public String paid;
    
}
