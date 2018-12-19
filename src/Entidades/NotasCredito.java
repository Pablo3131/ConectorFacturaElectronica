/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

import java.util.Date;
import java.util.List;

/**
 *
 * @author Hugo
 */
public class NotasCredito extends Factura{
    
    private String referenciaFactura;
    private Date fechaReferencia;

    /**
     * @return the referenciaFactura
     */
    public String getReferenciaFactura() {
        return referenciaFactura;
    }

    /**
     * @param referenciaFactura the referenciaFactura to set
     */
    public void setReferenciaFactura(String referenciaFactura) {
        this.referenciaFactura = referenciaFactura;
    }

    /**
     * @return the fechaReferencia
     */
    public Date getFechaReferencia() {
        return fechaReferencia;
    }

    /**
     * @param fechaReferencia the fechaReferencia to set
     */
    public void setFechaReferencia(Date fechaReferencia) {
        this.fechaReferencia = fechaReferencia;
    }
    
}
