/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import interfaces.IInvoice;
import java.util.logging.Level;
import java.util.logging.Logger;
import servidor1.Servidor;

/**
 *
 * @author Usuario
 */
public class Factura implements IInvoice {

    private int secuencia = -1;
    private String idOrden;
    private BigDecimal totalVenta = new BigDecimal("0.0"); 
    private BigDecimal totalVentaExento = new BigDecimal("0.0"); 
    private BigDecimal totalDescuentos = new BigDecimal("0.0");;
    private BigDecimal totalVentaNeta = new BigDecimal("0.0");;    
    private BigDecimal totalImpuesto = new BigDecimal("0.0");;
    private BigDecimal totalComprante = new BigDecimal("0.0");;
    private List<DetalleFactura> detalleFactura = null;
    private List<DetalleFactura> retentionList = null;
    private String condicionVenta = "01";
    private String codigMedioPago1 = "";
    private String codigMedioPago2 = "";
    private String codigMedioPago3 = "";
    private String codigMedioPago4 = "";
    private Date fechaAutorizacion;
    private String numeroConsecutivo = "";
    private String claveComprobante = "";
    private String nombreCliente = "";
    private String correoElectronicoCliente = "";
    private int reintentos = 0;
    private List<String> invoiceLinesToPrint;
    private int indexHaciendaInformation = 0;
    private int esInformacionConfiable = 1; //TODO: Set this value in DB
    private String status = "";
    private int esFactura = 0;
    private String IdReceptor;    
    private String tipoIdReceptor;  //01 Cédula Física, 02 Cédula Jurídica, 03 DIMEX, 04 NITE
    private String paymentType = "";
    public String exemptionName = ""; //TODO: put this private
    private BigDecimal exchangeRate = BigDecimal.ONE;
    
    public Factura(){
        this.detalleFactura = new ArrayList<DetalleFactura>();
        this.retentionList = new ArrayList<DetalleFactura>();
        this.invoiceLinesToPrint = new ArrayList<String>();
    }
    
    public Factura(int secuencia, String idOrden, BigDecimal totalVenta, BigDecimal totalImpuesto, BigDecimal totalComprante, List<DetalleFactura> detalleFactura, String condicionVenta, String codigMedioPago1) {
        this.secuencia = secuencia;
        this.idOrden = idOrden;
        this.totalVenta = totalVenta;
        this.totalImpuesto = totalImpuesto;
        this.totalComprante = totalComprante;
        this.detalleFactura = detalleFactura;
        this.condicionVenta = condicionVenta;
        this.codigMedioPago1 = codigMedioPago1;
    }
    
    public void setExchangeRate(BigDecimal exchangeR) {
        this.exchangeRate = exchangeR;
        
        for(DetalleFactura line : this.detalleFactura) {
            line.setExchangeRate(exchangeR);
        }  
    }
    
    public BigDecimal getExchangeRate() {
        return this.exchangeRate;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String pType) {
        paymentType = pType;
    }
    
    public BigDecimal getTotalVentaExento() {
        if (exchangeRate.equals(BigDecimal.ONE)) {
          return this.totalVentaExento;
        }
        else {
          return this.totalVentaExento != null ? this.totalVentaExento.multiply(exchangeRate) :
                                                 this.totalVentaExento;
        }
    }
    
    public void setTotalVentaExento(BigDecimal totalExento) {
        this.totalVentaExento = totalExento;
    }
    
    public List<DetalleFactura> getRetentions() {
        if (this.retentionList == null) {
            this.retentionList = new ArrayList<DetalleFactura>();
        }
        
        return retentionList;
    }
    
    public void setRetentions(DetalleFactura retention) {
        if (this.retentionList == null) {
            this.retentionList = new ArrayList<DetalleFactura>();
        }
        
        DetalleFactura retentionAdded = getRetentionAdded(retention.getDescripcion()); 
        
        if (retentionAdded == null) {
            this.retentionList.add(retention);
        }
        else {
            retentionAdded.setPrecioUnitario(retentionAdded.getPrecioUnitario().add(retention.getPrecioUnitario()));
            retentionAdded.setMonto(retentionAdded.getMonto().add(retention.getMonto()));
            retentionAdded.setMontoTotalLinea(retentionAdded.getMontoTotalLinea().add(retention.getMontoTotalLinea()));
            retentionAdded.setSubTotal(retentionAdded.getSubTotal().add(retention.getSubTotal()));
        }
    }
    
    private DetalleFactura getRetentionAdded(String description) {
        DetalleFactura result = null;
        
        for(DetalleFactura retention : this.retentionList) {
            if (retention.getDescripcion().equals(description)) {
                result = retention; 
                break;
            }
        }
        
        return result;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String newStatus) {
        status = newStatus;
    }
    
    public void addHaciendaInfo(String invoiceKey, String invoiceConsecutive) {
        try{
            System.out.println();
            System.out.println("Entry: addHaciendaInfo");
            System.out.println("Clave: " + invoiceKey);
            System.out.println("Consecutivo: " + invoiceConsecutive);
            
//        if ((invoiceLinesToPrint.get(indexHaciendaInformation).contains("Tiquete")) || 
//            (invoiceLinesToPrint.get(indexHaciendaInformation).contains("Clave"))) {
//            invoiceLinesToPrint.remove(indexHaciendaInformation);
//            invoiceLinesToPrint.remove(indexHaciendaInformation);
//        }
//
//        invoiceLinesToPrint.add(indexHaciendaInformation, "Clave#: " + invoiceKey);
//        invoiceLinesToPrint.add(indexHaciendaInformation, "Tiquete#: " + invoiceConsecutive); 
          for (int index = 0; index < invoiceLinesToPrint.size(); index++) {
              //System.out.println("index: " + index);
              if (invoiceLinesToPrint.get(index).contains("----------------")) {
                  //System.out.println("----- Index:" + index);
                  String invoiceKeyString = "Clave#: " + invoiceKey;
                  String invoiceConsecutiveString = "Consecutivo#: " + invoiceConsecutive; //
                  
                  if (invoiceKeyString.length() > 37) { //37 Max lenght of line
                      invoiceLinesToPrint.add(index, invoiceKeyString.substring(37, invoiceKeyString.length()));
                      invoiceLinesToPrint.add(index, invoiceKeyString.substring(0, 37));
                  }
                  else {
                      invoiceLinesToPrint.add(index, invoiceKeyString);
                  }
                  
                  if (invoiceConsecutiveString.length() > 37) { //37 Max lenght of line
                      invoiceLinesToPrint.add(index, invoiceConsecutiveString.substring(37, invoiceConsecutiveString.length()));
                      invoiceLinesToPrint.add(index, invoiceConsecutiveString.substring(0, 37));
                  } else {
                      invoiceLinesToPrint.add(index, invoiceConsecutiveString);
                  }
                  

                  //invoiceLinesToPrint.add(index, "Clave#: " + invoiceKey);
                  //invoiceLinesToPrint.add(index, "Consecutivo#: " + invoiceConsecutive);
                  break;
              }
          }


        }
        catch(Exception ex){
            Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	public Boolean getEsInformacionConfiable() {
        return this.esInformacionConfiable == 1;
    }	

	public void setEsInformacionConfiable(Boolean esInformacionConfiable) {
        if (esInformacionConfiable) {
            this.esInformacionConfiable = 1;
        }
        else {
            this.esInformacionConfiable = 0;
        }
    }
    
    public int getIndexHaciendaInformation() {
        return this.indexHaciendaInformation;
    }
    
    public void setIndexHaciendaInformation(int index) {
        if(this.indexHaciendaInformation == 0) {
            this.indexHaciendaInformation = index;
        }    
    }
    
    public List<String> getInvoiceLinesToPrint() {
        return this.invoiceLinesToPrint;
    }
    
    public void setInvoiceLinesToPrint(List<String> invoiceLinesToPrint) {
        this.invoiceLinesToPrint = invoiceLinesToPrint;
    }
    
    public int getSecuencia() {
        return secuencia;
    }

    public List<DetalleFactura> getDetalleFactura() {
        return detalleFactura;
    }

    public void setDetalleFactura(DetalleFactura detalleFactura) {      
        this.detalleFactura.add(detalleFactura);
        
        if (detalleFactura.getLinea() == 1) {
            detalleFactura.setLinea(this.detalleFactura.size());
        }
    }
    
    public void setSecuencia(int secuencia) {
        this.secuencia = secuencia;
    }

    public String getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(String idOrden) {
        this.idOrden = idOrden;
    }

    public BigDecimal getTotalVenta() {
        if (exchangeRate.equals(BigDecimal.ONE)) {
          return this.totalVenta;
        }
        else {
          return this.totalVenta != null ? this.totalVenta.multiply(exchangeRate) :
                                           this.totalVenta;
        }
    }

    public void setTotalVenta(BigDecimal totalVenta) {
        this.totalVenta = totalVenta;
    }
    
    public BigDecimal getTotalVentaNeta() {
        if (exchangeRate.equals(BigDecimal.ONE)) {
          return this.totalVentaNeta;
        }
        else {
          return this.totalVentaNeta != null ? this.totalVentaNeta.multiply(exchangeRate) :
                                               this.totalVentaNeta;
        }
    }

    public void setTotalVentaNeta(BigDecimal totalVentaNeta) {
        this.totalVentaNeta = totalVentaNeta;
    }
    
    public BigDecimal getTotalDescuentos() {
        if (exchangeRate.equals(BigDecimal.ONE)) {
          return this.totalDescuentos;
        }
        else {
          return this.totalDescuentos != null ? this.totalDescuentos.multiply(exchangeRate) :
                                                this.totalDescuentos;
        }
    }

    public void setTotalDescuentos(BigDecimal totalDescuentos) {
        this.totalDescuentos = totalDescuentos;
    }  

    public BigDecimal getTotalImpuesto() {
        if (exchangeRate.equals(BigDecimal.ONE)) {
          return this.totalImpuesto;
        }
        else {
          return this.totalImpuesto != null ? this.totalImpuesto.multiply(exchangeRate) :
                                              this.totalImpuesto;
        }
    }

    public void setTotalImpuesto(BigDecimal totalImpuesto) {
        this.totalImpuesto = totalImpuesto;
    }

    public BigDecimal getTotalComprante() {
        if (exchangeRate.equals(BigDecimal.ONE)) {
          return this.totalComprante;
        }
        else {
          return this.totalComprante != null ? this.totalComprante.multiply(exchangeRate) :
                                               this.totalComprante;
        }
    }

    public void setTotalComprante(BigDecimal totalComprante) {
        this.totalComprante = totalComprante;
    }

    public String getCondicionVenta() {
        return condicionVenta;
    }

    public void setCondicionVenta(String condicionVenta) {
        this.condicionVenta = condicionVenta;
    }

    public String getCodigMedioPago1() {
        return codigMedioPago1;
    }

    public void setCodigMedioPago1(String codigMedioPago1) {
        this.codigMedioPago1 = codigMedioPago1;
    }

    public String getCodigMedioPago2() {
        return codigMedioPago2;
    }

    public void setCodigMedioPago2(String codigMedioPago2) {
        this.codigMedioPago2 = codigMedioPago2;
    }

    public String getCodigMedioPago3() {
        return codigMedioPago3;
    }

    public void setCodigMedioPago3(String codigMedioPago3) {
        this.codigMedioPago3 = codigMedioPago3;
    }

    public String getCodigMedioPago4() {
        return codigMedioPago4;
    }

    public void setCodigMedioPago4(String codigMedioPago4) {
        this.codigMedioPago4 = codigMedioPago4;
    }

    public Date getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public void setFechaAutorizacion(Date fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    public String getNumeroConsecutivo() {
        return numeroConsecutivo;
    }

    public void setNumeroConsecutivo(String numeroConsecutivo) {
        this.numeroConsecutivo = numeroConsecutivo;
    }

    public String getClaveComprobante() {
        return claveComprobante;
    }

    public void setClaveComprobante(String claveComprobante) {
        this.claveComprobante = claveComprobante;
    }

    /**
     * @return the nombreCliente
     */
    public String getNombreCliente() {
        return nombreCliente;
    }

    /**
     * @param nombreCliente the nombreCliente to set
     */
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    /**
     * @return the correoElectronicoCliente
     */
    public String getCorreoElectronicoCliente() {
        return correoElectronicoCliente;
    }

    /**
     * @param correoElectronicoCliente the correoElectronicoCliente to set
     */
    public void setCorreoElectronicoCliente(String correoElectronicoCliente) {
        this.correoElectronicoCliente = correoElectronicoCliente;
    }

    /**
     * @return the reintentos
     */
    public int getReintentos() {
        return reintentos;
    }

    /**
     * @param reintentos the reintentos to set
     */
    public void setReintentos(int reintentos) {
        this.reintentos = reintentos;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        if (codigMedioPago1.equals("")) {
            setCodigMedioPago1(paymentMethod);
        }
        else if (codigMedioPago2.equals("")) {
            setCodigMedioPago2(paymentMethod);
        }
        else if (codigMedioPago3.equals("")) {
            setCodigMedioPago3(paymentMethod);
        }
        else {
            setCodigMedioPago4(paymentMethod);
        }
    }
    
    /**
     * @return the esFactura
     */
    public int getEsFactura() {
        return esFactura;
    }

    /**
     * @param esFactura the esFactura to set
     */
    public void setEsFactura(int esFactura) {
        this.esFactura = esFactura;
    }

    /**
     * @return the IdReceptor
     */
    public String getIdReceptor() {
        return IdReceptor;
    }

    /**
     * @param IdReceptor the IdReceptor to set
     */
    public void setIdReceptor(String IdReceptor) {
        this.IdReceptor = IdReceptor;
    }    

    /**
     * @return the tipoIdReceptor
     */
    public String getTipoIdReceptor() {
        return tipoIdReceptor;
    }

    /**
     * @param tipoIdReceptor the tipoIdReceptor to set
     */
    public void setTipoIdReceptor(String tipoIdReceptor) {
        this.tipoIdReceptor = tipoIdReceptor;
    }
}
