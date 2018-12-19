/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

import java.math.BigDecimal;

/**
 *
 * @author Hugo
 */
public class DetalleImpuesto {
    private String codigo = "01";
    private BigDecimal tarifa = new BigDecimal("0.13");
    private BigDecimal monto = new BigDecimal("0.0");
    private BigDecimal exchangeRate = BigDecimal.ONE;
    
    public void setExchangeRate(BigDecimal exchangeR) {
        this.exchangeRate = exchangeR;   
    }
    
    public BigDecimal getExchangeRate() {
        return this.exchangeRate;
    }
    
    /**
     * @return the codigo
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * @param codigo the codigo to set
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    /**
     * @return the tarifa
     */
    public BigDecimal getTarifa() {
        return tarifa;
    }

    /**
     * @param tarifa the tarifa to set
     */
    public void setTarifa(BigDecimal tarifa) {
        this.tarifa = tarifa;
    }

    /**
     * @return the monto
     */
    public BigDecimal getMonto() {
        if (exchangeRate.equals(BigDecimal.ONE)) {
          return this.monto;
        }
        else {
          return this.monto != null ? this.monto.multiply(exchangeRate) :
                                      this.monto;
        }
    }

    /**
     * @param monto the monto to set
     */
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}
