package servidor1;

import interfaces.IERPConector;
import interfaces.IFacturaElectronica;
import interfaces.IImpresora;
import interfaces.IParser;


/**
 *
 * @author luis.arce
 */
public class Controlador {
    IERPConector erpConector;
    IFacturaElectronica facturaElectronica;
    IImpresora impresora;
    IParser parser;
    
    public Controlador(IERPConector eConector, IFacturaElectronica fElectronica, IImpresora impr, IParser par) {
        erpConector = eConector;
        facturaElectronica = fElectronica;
        impresora = impr;
        parser = par;
    }
    
    public void procesarFactura() {
    
    }
}
