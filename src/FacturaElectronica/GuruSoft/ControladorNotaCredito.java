/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FacturaElectronica.GuruSoft;

import Entidades.DatosEmpresa;
import Entidades.DetalleFactura;
import Entidades.Estado;
import Entidades.Factura;
import Entidades.NotasCredito;
import Impresoras.Epson.ControladorImpresion;
import WSCreditoElectronico.ClsDetalleImpuesto;
import WSCreditoElectronico.ClsInformacionReferencia;
import com.google.gson.Gson;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.Handler;
import servidor1.ControladorDB;
import servidor1.LogMessageHandler;

/**
 *
 * @author Hugo
 */
public class ControladorNotaCredito implements Runnable {

    private Thread t;
    private DatosEmpresa datosEmpresa;
    private static String urlNotaCredito;
    private ControladorDB cDB = new ControladorDB();
    private ControladorImpresion cImpresion = new ControladorImpresion();
    
    public ControladorNotaCredito(DatosEmpresa datosEmpresa) {
        this.datosEmpresa = datosEmpresa;
        this.urlNotaCredito = datosEmpresa.getUrlNotaCredito();
        //System.out.println("Creating ControladorFacturaElectronica");
    }

    public void start() {
        //System.out.println("Starting ControladorFacturaElectronica");
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                EnviarNotasCredito();
                //Thread.sleep(1000);
            } catch (Exception ex) {
                Logger.getLogger(ControladorNotaCredito.class.getName()).log(Level.SEVERE, null, ex);
            }
            //System.out.println("Hello from a thread ControladorFacturaElectronica!"); 
        }
    }

    private void EnviarNotasCredito() {
        boolean disponible = true;
        Estado estadoFactura;
        int reintento = 0;
        Estado estado = Estado.CREADA;
        String json = null;
        Gson gson = new Gson();
        List<NotasCredito> listaFacturas = cDB.BuscarNotasCredito(Estado.CREADA);

        Holder<String> mensaje = new Holder<String>();
        Holder<WSCreditoElectronico.RespuestaEDOC> enviarFacturaResult = new Holder<WSCreditoElectronico.RespuestaEDOC>();
        mensaje.value = "1";

        for (NotasCredito notaCredito : listaFacturas) {
            disponible = true;
            reintento = notaCredito.getReintentos();
            json = gson.toJson(notaCredito);
            //System.out.println("Factura " + factura.getSecuencia() + ": " + json);

            WSCreditoElectronico.ClsNotaCredito documento = ConvertirANotaCreditoGuru(notaCredito);
            System.out.println("Enviando nota de credito a hacienda anulando comprobante#: " + notaCredito.getReferenciaFactura());

//            try {
//                System.out.println("Enviando nota de credito a hacienda anulando comprobante#: " + notaCredito.getReferenciaFactura());
//                //Thread.sleep(1000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(ControladorFacturaElectronica.class.getName()).log(Level.SEVERE, null, ex);
//            } 
            try
            {
                enviarNotaCredito(datosEmpresa.getPassword(), "2", documento, mensaje, enviarFacturaResult);
            }
            catch(Exception ex){
                disponible = false;
                reintento++;                
                if(reintento == datosEmpresa.getCantidadMaximaReintentos()){
                    estado = Estado.ERROR;
                }
                cDB.CambiarEstado("NotasCredito", notaCredito.getSecuencia(), estado, reintento, "", "");
                cDB.GuardarBitacoraRechazos(notaCredito.getSecuencia(), json, ex.getMessage());                
            }          
            
            if(disponible){
                //System.out.println("Mensaje: " + mensaje.value);
                if (enviarFacturaResult.value.getEstado().equals("1") || //En proceso.
                        enviarFacturaResult.value.getEstado().equals("2") || //Autorizada.
                        enviarFacturaResult.value.getEstado().equals("5")) {         //Recibido, en proceso por hacienda.

                    if (enviarFacturaResult.value.getEstado().equals("2")) {
                        estadoFactura = Estado.AUTORIZADA;
                    } else {
                        estadoFactura = Estado.EN_PROCESO;
                    }

                    System.out.println("Respuesta recibida de hacienda.");
                    System.out.println("Tiquete electronico numero: " + enviarFacturaResult.value.getNumeroConsecutivo());
                    System.out.println("Clave electronica numero: " + enviarFacturaResult.value.getClaveComprobante());

    //                try {
    //                    System.out.println("Respuesta recibida de hacienda.");
    //                    System.out.println("Tiquete electronico numero: " + enviarFacturaResult.value.getNumeroConsecutivo());
    //                    System.out.println("Clave electronica numero: " + enviarFacturaResult.value.getClaveComprobante());                    
    //                    //Thread.sleep(1000);
    //                } catch (InterruptedException ex) {
    //                    Logger.getLogger(ControladorFacturaElectronica.class.getName()).log(Level.SEVERE, null, ex);
    //                }
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
                    String strDate = dateFormat.format(enviarFacturaResult.value.getFechaAutorizacion().toGregorianCalendar().getTime());
                    //Se actualizan los datos en la tabla de facturas:
                    cDB.AgregarResultadoNotaCredito(
                            notaCredito.getSecuencia(),
                            estadoFactura,
                            strDate,
                            enviarFacturaResult.value.getNumeroConsecutivo(),
                            enviarFacturaResult.value.getClaveComprobante()
                    );

                    notaCredito.addHaciendaInfo(enviarFacturaResult.value.getClaveComprobante(), enviarFacturaResult.value.getNumeroConsecutivo());
                    //Enviar a imprimir la factura:
                    printInvoice(notaCredito); //TODO: should we add print in other places???
                } else {
                    reintento++;
                    if (reintento == datosEmpresa.getCantidadMaximaReintentos()) {
                        estado = Estado.NO_AUTORIZADA;
                    }
                    cDB.CambiarEstado("NotasCredito", notaCredito.getSecuencia(), estado, reintento, "", "");
                    cDB.GuardarBitacoraRechazos(notaCredito.getSecuencia(), json, enviarFacturaResult.value.getMensajeRespuesta());
                }
            }
        }
    }
    
    private void printInvoice(Factura invoiceToPrint) {
        try {                     
            //Enviar a imprimir la factura:
            cImpresion.printInvoice(invoiceToPrint);
        } catch (IOException ex) {
            cDB.InsertarBitacoraSistema(invoiceToPrint.getSecuencia(), ex.toString());
        } catch (InterruptedException ex) {
            cDB.InsertarBitacoraSistema(invoiceToPrint.getSecuencia(), ex.toString());
        }
    }

    private static void enviarNotaCredito(java.lang.String clave, java.lang.String entorno, WSCreditoElectronico.ClsNotaCredito notaCredito, javax.xml.ws.Holder<java.lang.String> mensaje, javax.xml.ws.Holder<WSCreditoElectronico.RespuestaEDOC> enviarNotaCreditoResult) throws Exception {
        
        try
        {
            WSCreditoElectronico.WSEDOCNOTASCREDITO service = new WSCreditoElectronico.WSEDOCNOTASCREDITO(new URL(urlNotaCredito));
            WSCreditoElectronico.IWSEDOCNOTASCREDITO port = service.getBasicHttpBindingIWSEDOCNOTASCREDITO();

            BindingProvider bindingProvider = (BindingProvider) port;
            Binding binding = bindingProvider.getBinding();
            List<Handler> handlerChain = binding.getHandlerChain();
            handlerChain.add(new LogMessageHandler());
            binding.setHandlerChain(handlerChain);

            port.enviarNotaCredito(clave, entorno, notaCredito, mensaje, enviarNotaCreditoResult);
        }
        catch(Exception ex){
            throw ex;
            //System.out.println(ex.getMessage());
        }        
    }

    private WSCreditoElectronico.ClsNotaCredito ConvertirANotaCreditoGuru(NotasCredito factura) {
        WSCreditoElectronico.ClsNotaCredito clsNotaCredito = new WSCreditoElectronico.ClsNotaCredito();
        try {
            //Datos de la empresa emisora de la factura.
            WSCreditoElectronico.ClsEmisor emisor = new WSCreditoElectronico.ClsEmisor();
            emisor.setRazonSocial(datosEmpresa.getRazonSocial());
            emisor.setTipoIdentificacion(datosEmpresa.getTipoIdentificacion());
            emisor.setNumIdentificacion(datosEmpresa.getNumIdentificacion());
            emisor.setNombreComercial(datosEmpresa.getNombreComercial());
            emisor.setProvincia(datosEmpresa.getProvincia());
            emisor.setCanton(datosEmpresa.getCanton());
            emisor.setDistrito(datosEmpresa.getDistrito());
            emisor.setBarrio(1);
            emisor.setDireccion(datosEmpresa.getDireccion());
            emisor.setCorreoElectronico(datosEmpresa.getCorreoElectronico());
            clsNotaCredito.setEmisor(emisor);
            clsNotaCredito.setCodigoMoneda("CRC");

            clsNotaCredito.setMatrizEstab(datosEmpresa.getMatriz());
            clsNotaCredito.setPuntoVenta(datosEmpresa.getPuntoVenta());
            clsNotaCredito.setSecuencial(factura.getSecuencia());

            Timestamp elTimeStamp = new Timestamp(System.currentTimeMillis());
            GregorianCalendar unGregorianCalendar = new GregorianCalendar();
            unGregorianCalendar.setTime(elTimeStamp);
            try {
                XMLGregorianCalendar fecha = DatatypeFactory.newInstance().newXMLGregorianCalendar(unGregorianCalendar);
                clsNotaCredito.setFechaEmision(fecha);
            } catch (DatatypeConfigurationException ex) {
                Logger.getLogger(ControladorNotaCredito.class.getName()).log(Level.SEVERE, null, ex);
            }

            clsNotaCredito.setTipoCambio(BigDecimal.ONE);
            //clsTiquete.setTotalServGravados(BigDecimal.ZERO);
            //clsTiquete.setTotalServExentos(BigDecimal.ZERO);
            //clsTiquete.setTotalMercanciasGravadas(BigDecimal.ZERO);
            //clsTiquete.setTotalMercanciasExentas(BigDecimal.ZERO);
            //clsTiquete.setTotalGravado(BigDecimal.ZERO);
            //clsTiquete.setTotalExento(BigDecimal.ZERO);
            clsNotaCredito.setSinInternet(false);
            clsNotaCredito.setAdjunto(null);

            WSCreditoElectronico.ArrayOfClsDetalleServicio detalleServicioArray = new WSCreditoElectronico.ArrayOfClsDetalleServicio();
            WSCreditoElectronico.ClsDetalleServicio detalleServicio;
            for (DetalleFactura detalleFactura : factura.getDetalleFactura()) {
                detalleServicio = new WSCreditoElectronico.ClsDetalleServicio();
                detalleServicio.setCantidad(BigDecimal.valueOf(detalleFactura.getCantidad()));
                detalleServicio.setUnidadMedida(detalleFactura.getUnidadMedida());
                detalleServicio.setDetalleDescripcion(detalleFactura.getDescripcion());
                detalleServicio.setPrecioUnitario(detalleFactura.getPrecioUnitario());  //Unidades de Medida basadas en el estándar RTC 443:2010.
                detalleServicio.setMontoTotal(detalleFactura.getMonto());
                detalleServicio.setSubTotal(detalleFactura.getSubTotal());
                detalleServicio.setMontoTotalLinea(detalleFactura.getMontoTotalLinea());
                detalleServicio.setMontoDescuento(detalleFactura.getMontoDescuento());
                detalleServicio.setNaturalezaDescuento(detalleFactura.getNaturalezaDescuento());

                WSCreditoElectronico.ArrayOfClsDetalleImpuesto detalleImpuestoList = new WSCreditoElectronico.ArrayOfClsDetalleImpuesto();
                for (int i = 0; i < detalleFactura.getdImpuesto().size(); i++) {
                    ClsDetalleImpuesto cDetalleImpuesto = new ClsDetalleImpuesto();

                    cDetalleImpuesto.setCodigo(detalleFactura.getdImpuesto().get(i).getCodigo());
                    cDetalleImpuesto.setTarifa(detalleFactura.getdImpuesto().get(i).getTarifa());
                    cDetalleImpuesto.setMonto(detalleFactura.getdImpuesto().get(i).getMonto());

                    detalleImpuestoList.getClsDetalleImpuesto().add(cDetalleImpuesto);
                }
                detalleServicio.setImpuestos(detalleImpuestoList);

                detalleServicioArray.getClsDetalleServicio().add(detalleServicio);
            }
            clsNotaCredito.setDetalleServicio(detalleServicioArray);

            clsNotaCredito.setInformacionReferencia(null);
            WSCreditoElectronico.ClsOtros otros = new WSCreditoElectronico.ClsOtros();
            WSCreditoElectronico.ArrayOfOtros array = new WSCreditoElectronico.ArrayOfOtros();
            otros.setOtrosTexto(array);
            clsNotaCredito.setOtros(otros);

            clsNotaCredito.setCodCondicionVenta(factura.getCondicionVenta());
            clsNotaCredito.setCodMedioPago1(factura.getCodigMedioPago1());
            clsNotaCredito.setCodMedioPago2(factura.getCodigMedioPago2());
            clsNotaCredito.setCodMedioPago3(factura.getCodigMedioPago3());
            clsNotaCredito.setCodMedioPago4(factura.getCodigMedioPago4());
            clsNotaCredito.setTotalMercanciasGravadas(factura.getTotalVenta());
            clsNotaCredito.setTotalGravado(factura.getTotalVenta()); //En Lavu no hay produtos que no esten exentos.
            clsNotaCredito.setTotalVenta(factura.getTotalVenta());
            clsNotaCredito.setTotalDescuentos(factura.getTotalDescuentos());    //-
            clsNotaCredito.setTotalVentaNeta(factura.getTotalVentaNeta());      //=   
            clsNotaCredito.setTotalImpuesto(factura.getTotalImpuesto());        //-
            clsNotaCredito.setTotalComprobante(factura.getTotalComprante());    //=
            clsNotaCredito.setSecuencialERP(String.valueOf(factura.getSecuencia()));

            //Referencia de la nota de credito hacia factura:
            WSCreditoElectronico.ArrayOfClsInformacionReferencia detalleReferencia = new WSCreditoElectronico.ArrayOfClsInformacionReferencia();
            ClsInformacionReferencia iReferencia = new ClsInformacionReferencia();
            iReferencia.setCodigo("01");    //Anula factura de referencia.

            try {
                GregorianCalendar c = new GregorianCalendar();
                c.setTime(factura.getFechaReferencia());
                XMLGregorianCalendar fechaReferencia = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
                iReferencia.setFechaEmision(fechaReferencia);
            } catch (DatatypeConfigurationException ex) {
                Logger.getLogger(ControladorNotaCredito.class.getName()).log(Level.SEVERE, null, ex);
            }

            iReferencia.setNumero(factura.getReferenciaFactura());
            iReferencia.setTipoDoc("04");   //04 Tiquete electrónico
            iReferencia.setRazon("Anulacion total de tiquete electronico.");
            detalleReferencia.getClsInformacionReferencia().add(iReferencia);
            clsNotaCredito.setInformacionReferencia(detalleReferencia);
        } catch (Exception ex) {

        }

        return clsNotaCredito;
    }
}
