/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lightspeed.DTO;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {
    "exempt",
    "total"
})
@XmlRootElement( name = "tax" )
public class InvoiceLineItemTax {
    @XmlElement(name = "exempt")
    public boolean exempt;
    @XmlElement(name = "total")
    public String total;
}
