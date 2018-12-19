/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lightspeed.DTO;

import javax.xml.bind.annotation.XmlElement;

public class InvoiceLineItemSell {
    @XmlElement(name = "sell")
    public String sell;  
    @XmlElement(name = "base")
    public String base;  
    @XmlElement(name = "total")
    public String total;
    @XmlElement(name = "sell_quantity_discount")
    public String sell_quantity_discount; 
    @XmlElement(name = "sell_tax_inclusive_quantity_discount")
    public String sell_tax_inclusive_quantity_discount; 
    @XmlElement(name = "sell_tax_inclusive")
    public String sell_tax_inclusive;  
    @XmlElement(name = "sell_tax_inclusive_total")
    public String sell_tax_inclusive_total; 
    @XmlElement(name = "sell_tax_inclusive_discounted")
    public String sell_tax_inclusive_discounted;
}
