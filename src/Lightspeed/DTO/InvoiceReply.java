/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lightspeed.DTO;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author Luis Arce
 */
@XmlType(propOrder = {
    "document_id",
    "invoice_id",
    "status", 
    "invoice_customer",
    "totals", 
    "lineitems", 
    "payments"
})
@XmlRootElement( name = "invoice" )
public class InvoiceReply {
    @XmlElement(name = "document_id")
    public int document_id;
    @XmlElement(name = "invoice_id")
    public String invoice_id;
    @XmlElement(name = "status")
    public String status;
    @XmlElement( name = "invoice_customer", type=CustomerInfo.class)
    public CustomerInfo invoice_customer;    
    @XmlElement( name = "totals", type=InvoiceTotals.class )
    public InvoiceTotals totals;
    
    //Attrubutes
    @XmlAttribute( name = "uri", required = true )
    public String uri;  
    @XmlAttribute( name = "id", required = true )
    public int id;
    
    @XmlElementWrapper(name="lineitems")
    @XmlElement(name="lineitem")
    public ArrayList<InvoiceLineItem> lineitems;
    
    @XmlElementWrapper(name="payments")
    @XmlElement(name="payment")
    public ArrayList<paymentMethod> payments;
}
