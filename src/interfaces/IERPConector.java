/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import Entidades.Factura;

/**
 *
 * @author luis.arce
 */
public interface IERPConector {
    Factura getInvoiceInformation(int idOrden);
    Boolean isAPIAvailable();
}
