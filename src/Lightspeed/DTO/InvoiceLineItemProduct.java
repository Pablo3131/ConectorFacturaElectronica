/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lightspeed.DTO;

import javax.xml.bind.annotation.XmlElement;

public class InvoiceLineItemProduct {
    @XmlElement(name = "description")
    public String description;  
}
