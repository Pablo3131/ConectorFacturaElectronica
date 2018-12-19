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
    "quantity",
    "sell_price", 
    "sells", 
    "lineitem_product", 
    "taxes"
})
@XmlRootElement( name = "lineitem" )
public class InvoiceLineItem {
    @XmlElement(name = "quantity")
    public String quantity;
    @XmlElement(name = "sell_price")
    public String sell_price;
    @XmlElement( name = "sells", type=InvoiceLineItemSell.class )
    public InvoiceLineItemSell sells;
    @XmlElement( name = "lineitem_product", type=InvoiceLineItemProduct.class )
    public InvoiceLineItemProduct lineitem_product;
    
    //Attrubutes
    @XmlAttribute( name = "uri", required = true )
    public String uri;  
    @XmlAttribute( name = "id", required = true )
    public int id;
    
    @XmlElementWrapper(name="taxes")
    @XmlElement(name="tax")
    public ArrayList<InvoiceLineItemTax> taxes;
}


