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
import Impresoras.Epson.ControladorImpresion;
import WSFacturaElectronica.ArrayOfClsDetalleServicio;
import WSFacturaElectronica.ArrayOfOtros;
import WSFacturaElectronica.ClsDetalleServicio;
import WSFacturaElectronica.ClsEmisor;
import WSFacturaElectronica.ClsFactura;
import WSFacturaElectronica.ClsOtros;
import WSFacturaElectronica.ClsReceptor;
import WSTiqueteElectronico.ClsDetalleImpuesto;
import com.google.gson.Gson;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import servidor1.ControladorDB;
import servidor1.LogMessageHandler;
import servidor1.Servidor;

/**
 *
 * @author Usuario
 */
public class ControladorFacturaElectronica extends ControladorBase implements Runnable {

    private Thread t;
    private DatosEmpresa datosEmpresa;
    private static String urlTiquete;
    private static String urlFactura;
    private ControladorDB cDB = new ControladorDB();
    private ControladorImpresion cImpresion = new ControladorImpresion();

    public ControladorFacturaElectronica(DatosEmpresa datosEmpresa) {
        this.datosEmpresa = datosEmpresa;
        this.urlTiquete = datosEmpresa.getUrlTiquete();
        this.urlFactura = datosEmpresa.getUrlFactura();
        //setUrl(url);
        //System.out.println("Creating ControladorFacturaElectronica");
    }

    @Override
    public void run() {
        while (true) {
            try {
                EnviarFacturas();
                //Thread.sleep(1000);
            } catch (Exception ex) {
                Logger.getLogger(ControladorFacturaElectronica.class.getName()).log(Level.SEVERE, null, ex);
            }
            //System.out.println("Hello from a thread ControladorFacturaElectronica!"); 
        }
    }

    //Se revisa si hay facturas electrónicas pendientes de enviar a Guru:
    private void EnviarFacturas() {

        boolean disponible = true;
        Estado estadoFactura;
        int reintento = 0;
        Estado estado = Estado.CREADA;
        String json = null;
        Gson gson = new Gson();
        List<Factura> listaFacturas = cDB.BuscarFacturas(Estado.CREADA);

        Holder<String> mensaje = new Holder<String>();
        Holder<WSTiqueteElectronico.RespuestaEDOC> enviarFacturaResult = new Holder<WSTiqueteElectronico.RespuestaEDOC>();
        Holder<WSFacturaElectronica.RespuestaEDOC> enviarFacturaResult2 = new Holder<WSFacturaElectronica.RespuestaEDOC>();
        mensaje.value = "1";
        
        //Datos de salida:
        String estadoResultado = "0";
        XMLGregorianCalendar fechaAutorizacion = null;
        String consecutivo = "";
        String clave = "";
        String mensajeRespuesta = "";

        for (Factura factura : listaFacturas) {           
            disponible = true;
            reintento = factura.getReintentos();
            //json = gson.toJson(factura);
            //System.out.println("Factura " + factura.getSecuencia() + ": " + json);

            if (factura.getEsFactura() == 1) {
                ClsFactura facturaElectronica = ConvertirAFacturaGuru(factura);
                System.out.println("Enviando factura a hacienda con el Orden#: " + factura.getIdOrden());
                try {
                    enviarFactura(datosEmpresa.getPassword(), "2", facturaElectronica, mensaje, enviarFacturaResult2);
                    estadoResultado = enviarFacturaResult2.value.getEstado();
                    fechaAutorizacion = enviarFacturaResult2.value.getFechaAutorizacion();
                    consecutivo = enviarFacturaResult2.value.getNumeroConsecutivo();
                    clave = enviarFacturaResult2.value.getClaveComprobante();    
                    mensajeRespuesta = enviarFacturaResult2.value.getMensajeRespuesta();
                } catch (Exception ex) {
                    disponible = false;
                    reintento++;
                    if (reintento >= datosEmpresa.getCantidadMaximaReintentos()) {
                        estado = Estado.ERROR;
                        
                        //Print Invoice with out Key and Consecutive
                        factura.addHaciendaInfo(clave.equals("") ? "Facturacion en tramite": clave, 
                                                consecutivo.equals("") ? "Facturacion en tramite" : consecutivo);
                        
//                        try {
//                        //Enviar a imprimir la factura:
//                            //cImpresion.printInvoice(factura);
//                        } catch (IOException exe) {
//                            cDB.InsertarBitacoraSistema(factura.getSecuencia(), exe.toString());
//                        } catch (InterruptedException exe) {
//                            cDB.InsertarBitacoraSistema(factura.getSecuencia(), exe.toString());
//                        }
                        
                        EnviarCorreo(datosEmpresa.getRemitenteErrores(), datosEmpresa.getDestinatariosErrores(), "Cantidad Máxima Reintentos Print Server", "Empresa: " + datosEmpresa.getNombreComercial() + System.lineSeparator() + "Error: " + ex.getMessage() + System.lineSeparator() + gson.toJson(factura));
                    }
                    cDB.CambiarEstado("Facturas", factura.getSecuencia(), estado, reintento, clave, consecutivo);
                    cDB.GuardarBitacoraRechazos(factura.getSecuencia(), gson.toJson(factura), ex.getMessage());
                }
            } else {
                WSTiqueteElectronico.ClsTiquete tiquete = ConvertirATiqueteGuru(factura);                
                System.out.println("Enviando tiquete a hacienda con el Orden#: " + factura.getIdOrden());

//            try {
//                System.out.println("Enviando tiquete a hacienda con el Orden#: " + factura.getIdOrden());
//                Thread.sleep(5000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(ControladorFacturaElectronica.class.getName()).log(Level.SEVERE, null, ex);
//            }         
                try {
                    enviarTiquete(datosEmpresa.getPassword(), "2", tiquete, mensaje, enviarFacturaResult);
                    estadoResultado = enviarFacturaResult.value.getEstado();
                    fechaAutorizacion = enviarFacturaResult.value.getFechaAutorizacion();
                    consecutivo = enviarFacturaResult.value.getNumeroConsecutivo();
                    clave = enviarFacturaResult.value.getClaveComprobante();    
                    mensajeRespuesta = enviarFacturaResult.value.getMensajeRespuesta();
                } catch (Exception ex) {
                    disponible = false;
                    reintento++;
                    if (reintento >= datosEmpresa.getCantidadMaximaReintentos()) {
                        estado = Estado.ERROR;
                        
                        //Print Invoice with out Key and Consecutive
                        factura.addHaciendaInfo(clave.equals("") ? "Facturacion en tramite": clave, 
                                                consecutivo.equals("") ? "Facturacion en tramite" : consecutivo);
//                        try {
//                        //Enviar a imprimir la factura:
//                            //cImpresion.printInvoice(factura);
//                        } catch (IOException exe) {
//                            cDB.InsertarBitacoraSistema(factura.getSecuencia(), exe.toString());
//                        } catch (InterruptedException exe) {
//                            cDB.InsertarBitacoraSistema(factura.getSecuencia(), exe.toString());
//                        }
                        
                        EnviarCorreo(datosEmpresa.getRemitenteErrores(), datosEmpresa.getDestinatariosErrores(), "Cantidad Máxima Reintentos Print Server", "Empresa: " + datosEmpresa.getNombreComercial() + System.lineSeparator() + "Error: " + ex.getMessage() + System.lineSeparator() + gson.toJson(factura));
                    }
                    cDB.CambiarEstado("Facturas", factura.getSecuencia(), estado, reintento, clave, consecutivo);
                    cDB.GuardarBitacoraRechazos(factura.getSecuencia(), gson.toJson(factura), ex.getMessage());       
                }
            }

            if (disponible) {
                //System.out.println("Mensaje: " + mensaje.value);
                if (estadoResultado.equals("1") || //En proceso.
                        estadoResultado.equals("2") || //Autorizada.
                        estadoResultado.equals("5")) {         //Recibido, en proceso por hacienda.

                    if (estadoResultado.equals("2")) {
                        estadoFactura = Estado.AUTORIZADA;
                    } else {
                        estadoFactura = Estado.EN_PROCESO;
                    }

                    System.out.println("Respuesta recibida de hacienda.");
                    System.out.println("Tiquete electronico numero: " + consecutivo);
                    System.out.println("Clave electronica numero: " + clave);

    //                try {
                    //                    System.out.println("Respuesta recibida de hacienda.");
                    //                    System.out.println("Tiquete electronico numero: " + enviarFacturaResult.value.getNumeroConsecutivo());
                    //                    System.out.println("Clave electronica numero: " + enviarFacturaResult.value.getClaveComprobante());                    
                    //                    Thread.sleep(5000);
                    //                } catch (InterruptedException ex) {
                    //                    Logger.getLogger(ControladorFacturaElectronica.class.getName()).log(Level.SEVERE, null, ex);
                    //                }
                    //Se actualizan los datos en la tabla de facturas:
                    cDB.AgregarResultado(
                            factura.getSecuencia(),
                            estadoFactura,
                            ConvertirFecha(fechaAutorizacion),//enviarFacturaResult.value.getFechaAutorizacion().toString(),
                            consecutivo,
                            clave
                    );

                    factura.addHaciendaInfo(clave, consecutivo); //TODO: take this back

//                    try {
//                        //Enviar a imprimir la factura:
//                        cImpresion.printInvoice(factura);
//                    } catch (IOException ex) {
//                        cDB.InsertarBitacoraSistema(factura.getSecuencia(), ex.toString());
//                    } catch (InterruptedException ex) {
//                        cDB.InsertarBitacoraSistema(factura.getSecuencia(), ex.toString());
//                    }
                    
                    EnviarCorreo(datosEmpresa.getRemitenteErrores(), datosEmpresa.getDestinatariosErrores(), "Tiquete enviado correctamente", "Empresa: " + datosEmpresa.getNombreComercial() + System.lineSeparator() + gson.toJson(factura));
                } else {
                    reintento++;
                    if (reintento >= datosEmpresa.getCantidadMaximaReintentos()) {
                        estado = Estado.NO_AUTORIZADA;
                        
                        //Print Invoice with out Key and Consecutive
                        factura.addHaciendaInfo(clave.equals("") ? "Facturacion en tramite": clave, 
                                                consecutivo.equals("") ? "Facturacion en tramite" : consecutivo);
//                        try {
//                        //Enviar a imprimir la factura:
//                            cImpresion.printInvoice(factura);
//                        } catch (IOException ex) {
//                            cDB.InsertarBitacoraSistema(factura.getSecuencia(), ex.toString());
//                        } catch (InterruptedException ex) {
//                            cDB.InsertarBitacoraSistema(factura.getSecuencia(), ex.toString());
//                        }
                        
                        EnviarCorreo(datosEmpresa.getRemitenteErrores(), datosEmpresa.getDestinatariosErrores(), "Cantidad Máxima Reintentos Print Server", "Empresa: " + datosEmpresa.getNombreComercial() + System.lineSeparator() + "Error: " + mensajeRespuesta + System.lineSeparator() + gson.toJson(factura));
                    }
                                                           
                    cDB.CambiarEstado("Facturas", factura.getSecuencia(), estado, reintento, clave, consecutivo);;
                    cDB.GuardarBitacoraRechazos(factura.getSecuencia(), gson.toJson(factura), mensajeRespuesta);
                }
            }
        }
    }

    public void start() {
        //System.out.println("Starting ControladorFacturaElectronica");
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    public static void enviarFactura(java.lang.String clave, java.lang.String entorno, WSFacturaElectronica.ClsFactura factura, javax.xml.ws.Holder<java.lang.String> mensaje, javax.xml.ws.Holder<WSFacturaElectronica.RespuestaEDOC> enviarFacturaResult) throws Exception {

        try {
            WSFacturaElectronica.WSEDOCFACTURAS service = new WSFacturaElectronica.WSEDOCFACTURAS(new URL(urlFactura));
            //WSFacturaElectronica.WSEDOCFACTURAS service = new WSFacturaElectronica.WSEDOCFACTURAS();
            WSFacturaElectronica.IWSEDOCFACTURAS port = service.getBasicHttpBindingIWSEDOCFACTURAS();

            BindingProvider bindingProvider = (BindingProvider) port;
            Binding binding = bindingProvider.getBinding();
            List<Handler> handlerChain = binding.getHandlerChain();
            handlerChain.add(new LogMessageHandler());
            binding.setHandlerChain(handlerChain);

            port.enviarFactura(clave, entorno, factura, mensaje, enviarFacturaResult);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw ex;            
        }
    }

    private WSFacturaElectronica.ClsFactura ConvertirAFacturaGuru(Factura factura) {
        WSFacturaElectronica.ClsFactura clsFactura = new WSFacturaElectronica.ClsFactura();
        try {
            BigDecimal exchangeRate = BigDecimal.ONE;
            BigDecimal exchange = BigDecimal.ONE;
            String coinCode = "CRC";
            
            //Set Dolar invoice - only for Trisquel (Sibu)
            if (datosEmpresa.getRazonSocial().equals("Trisquel S.A") &&
                isTrisquelEspecialClient(factura.getNombreCliente())) {
                exchangeRate = new BigDecimal("0.00167"); //Exchange to 600
                exchange = new BigDecimal("600.0");
                coinCode = "USD";
            }
            
            factura.setExchangeRate(exchangeRate);
            
            //Datos de la empresa emisora de la factura.
            WSFacturaElectronica.ClsEmisor emisor = new WSFacturaElectronica.ClsEmisor();
            emisor.setRazonSocial(datosEmpresa.getRazonSocial());
            emisor.setTipoIdentificacion(datosEmpresa.getTipoIdentificacion());
            emisor.setNumIdentificacion(datosEmpresa.getNumIdentificacion());
            emisor.setNombreComercial(datosEmpresa.getNombreComercial());
            emisor.setProvincia(datosEmpresa.getProvincia());
            emisor.setCanton(datosEmpresa.getCanton());
            emisor.setDistrito(datosEmpresa.getDistrito());
            emisor.setBarrio(datosEmpresa.getBarrio());
            emisor.setDireccion(datosEmpresa.getDireccion());
            emisor.setCorreoElectronico(datosEmpresa.getCorreoElectronico());
            clsFactura.setEmisor(emisor);
            clsFactura.setCodigoMoneda(coinCode); //TODO: This should be in the configuration file

            clsFactura.setMatrizEstab(datosEmpresa.getMatriz());
            clsFactura.setPuntoVenta(datosEmpresa.getPuntoVenta());
            clsFactura.setSecuencial(factura.getSecuencia());
            
            Timestamp elTimeStamp = new Timestamp(System.currentTimeMillis());
            GregorianCalendar unGregorianCalendar = new GregorianCalendar();
            unGregorianCalendar.setTime(elTimeStamp);
            try {
                XMLGregorianCalendar fecha = DatatypeFactory.newInstance().newXMLGregorianCalendar(unGregorianCalendar);
                clsFactura.setFechaEmision(fecha);
            } catch (DatatypeConfigurationException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }

            WSFacturaElectronica.ClsReceptor receptor = new WSFacturaElectronica.ClsReceptor();
            receptor.setRazonSocial(factura.getNombreCliente());
            receptor.setNumIdentificacion(factura.getIdReceptor());
            receptor.setTipoIdentificacion(factura.getTipoIdReceptor());
            if (factura.getCorreoElectronicoCliente() != null && !factura.getCorreoElectronicoCliente().isEmpty()) {
                receptor.setCorreoElectronico(factura.getCorreoElectronicoCliente());
            }
            //receptor.setProvincia(1);
            //receptor.setCanton(1);
            //receptor.setDistrito(1);
            //receptor.setBarrio(1);
            //receptor.setDireccion("Prueba de dirección");
            clsFactura.setReceptor(receptor);

            clsFactura.setTipoCambio(exchange);
            //clsFactura.setTotalServGravados(BigDecimal.ZERO);
            //clsFactura.setTotalServExentos(BigDecimal.ZERO);
            //clsFactura.setTotalMercanciasGravadas(BigDecimal.ZERO);
            //clsFactura.setTotalMercanciasExentas(BigDecimal.ZERO);
            //clsFactura.setTotalGravado(BigDecimal.ZERO);
            //clsFactura.setTotalExento(BigDecimal.ZERO);
            clsFactura.setSinInternet(false);
            clsFactura.setAdjunto(null);

            WSFacturaElectronica.ArrayOfClsDetalleServicio detalleServicioArray = new WSFacturaElectronica.ArrayOfClsDetalleServicio();
            ClsDetalleServicio detalleServicio;
            for (DetalleFactura detalleFactura : factura.getDetalleFactura()) {
                detalleServicio = new ClsDetalleServicio();
                detalleServicio.setCantidad(BigDecimal.valueOf(detalleFactura.getCantidad()));
                detalleServicio.setUnidadMedida(detalleFactura.getUnidadMedida());
                detalleServicio.setDetalleDescripcion(detalleFactura.getDescripcion());
                detalleServicio.setPrecioUnitario(detalleFactura.getPrecioUnitario());  //Unidades de Medida basadas en el estándar RTC 443:2010.
                detalleServicio.setMontoTotal(detalleFactura.getMonto());
                detalleServicio.setSubTotal(detalleFactura.getSubTotal());
                detalleServicio.setMontoTotalLinea(detalleFactura.getMontoTotalLinea());
                detalleServicio.setMontoDescuento(detalleFactura.getMontoDescuento());
                
                WSFacturaElectronica.ArrayOfClsDetalleImpuesto detalleImpuestoList = new WSFacturaElectronica.ArrayOfClsDetalleImpuesto();
                for (int i = 0; i < detalleFactura.getdImpuesto().size(); i++) {
                    WSFacturaElectronica.ClsDetalleImpuesto cDetalleImpuesto = new WSFacturaElectronica.ClsDetalleImpuesto();

                    cDetalleImpuesto.setCodigo(detalleFactura.getdImpuesto().get(i).getCodigo());
                    cDetalleImpuesto.setTarifa(detalleFactura.getdImpuesto().get(i).getTarifa());
                    cDetalleImpuesto.setMonto(detalleFactura.getdImpuesto().get(i).getMonto());

                    detalleImpuestoList.getClsDetalleImpuesto().add(cDetalleImpuesto);
                }
                detalleServicio.setImpuestos(detalleImpuestoList);                
                
                detalleServicioArray.getClsDetalleServicio().add(detalleServicio);
            }
            clsFactura.setDetalleServicio(detalleServicioArray);

            clsFactura.setInformacionReferencia(null);
            WSFacturaElectronica.ClsOtros otros = new WSFacturaElectronica.ClsOtros();
            WSFacturaElectronica.ArrayOfOtros array = new WSFacturaElectronica.ArrayOfOtros();
            otros.setOtrosTexto(array);
            clsFactura.setOtros(otros);

            clsFactura.setCodCondicionVenta(factura.getCondicionVenta());
            clsFactura.setCodMedioPago1(factura.getCodigMedioPago1());
            clsFactura.setCodMedioPago2(factura.getCodigMedioPago2());
            clsFactura.setCodMedioPago3(factura.getCodigMedioPago3());
            clsFactura.setCodMedioPago4(factura.getCodigMedioPago4());
            clsFactura.setTotalVenta(factura.getTotalVenta());
            clsFactura.setTotalMercanciasGravadas(factura.getTotalVenta().subtract(factura.getTotalVentaExento()));
            clsFactura.setTotalMercanciasExentas(factura.getTotalVentaExento()); //TODO: We should decide if the product is a service or not
            clsFactura.setTotalGravado(factura.getTotalVenta().subtract(factura.getTotalVentaExento()));          
            clsFactura.setTotalExento(factura.getTotalVentaExento());
            clsFactura.setTotalDescuentos(factura.getTotalDescuentos());    //-
            clsFactura.setTotalVentaNeta(factura.getTotalVentaNeta());      //=   
            clsFactura.setTotalImpuesto(factura.getTotalImpuesto());        //-
            clsFactura.setTotalComprobante(factura.getTotalComprante());    //=
            clsFactura.setSecuencialERP(String.valueOf(factura.getSecuencia()));
            
            if (!factura.getClaveComprobante().equals("")) {
                clsFactura.setClaveComprobante(factura.getClaveComprobante());
            }
            
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return clsFactura;
    }
    
    private boolean isTrisquelEspecialClient(String clientName) {
        String clientNameLCase = clientName.toLowerCase();
        
        if (clientNameLCase.contains("servicio de viajeros suiza sa") ||
            clientNameLCase.contains("tauck worldwide")) {
            return true;
        }
        
        return false;
    } 

    private static void enviarTiquete(java.lang.String clave, java.lang.String entorno, WSTiqueteElectronico.ClsTiquete tiquete, javax.xml.ws.Holder<java.lang.String> mensaje, javax.xml.ws.Holder<WSTiqueteElectronico.RespuestaEDOC> enviarTiqueteResult) throws Exception {

        try {
            //WSTiqueteElectronico.WSEDOCTIQUETE service = new generated.WSEDOCTIQUETE();           
            WSTiqueteElectronico.WSEDOCTIQUETE service = new WSTiqueteElectronico.WSEDOCTIQUETE(new URL(urlTiquete));
            WSTiqueteElectronico.IWSEDOCTIQUETE port = service.getBasicHttpBindingIWSEDOCTIQUETE();

            BindingProvider bindingProvider = (BindingProvider) port;
            Binding binding = bindingProvider.getBinding();
            List<Handler> handlerChain = binding.getHandlerChain();
            handlerChain.add(new LogMessageHandler());
            binding.setHandlerChain(handlerChain);

            port.enviarTiquete(clave, entorno, tiquete, mensaje, enviarTiqueteResult);
        } catch (Exception ex) {
            throw ex;
            //System.out.println(ex.getMessage());
        }
    }

    private WSTiqueteElectronico.ClsTiquete ConvertirATiqueteGuru(Factura factura) {
        WSTiqueteElectronico.ClsTiquete clsTiquete = new WSTiqueteElectronico.ClsTiquete();
        try {
            //Datos de la empresa emisora de la factura.
            WSTiqueteElectronico.ClsEmisor emisor = new WSTiqueteElectronico.ClsEmisor();
            emisor.setRazonSocial(datosEmpresa.getRazonSocial());
            emisor.setTipoIdentificacion(datosEmpresa.getTipoIdentificacion());
            emisor.setNumIdentificacion(datosEmpresa.getNumIdentificacion());
            emisor.setNombreComercial(datosEmpresa.getNombreComercial());
            emisor.setProvincia(datosEmpresa.getProvincia());
            emisor.setCanton(datosEmpresa.getCanton());
            emisor.setDistrito(datosEmpresa.getDistrito());
            emisor.setBarrio(datosEmpresa.getBarrio());
            emisor.setDireccion(datosEmpresa.getDireccion());
            emisor.setCorreoElectronico(datosEmpresa.getCorreoElectronico());
            clsTiquete.setEmisor(emisor);
            clsTiquete.setCodigoMoneda("CRC");

            clsTiquete.setMatrizEstab(datosEmpresa.getMatriz());
            clsTiquete.setPuntoVenta(datosEmpresa.getPuntoVenta());
            clsTiquete.setSecuencial(factura.getSecuencia());

            Timestamp elTimeStamp = new Timestamp(System.currentTimeMillis());
            GregorianCalendar unGregorianCalendar = new GregorianCalendar();
            unGregorianCalendar.setTime(elTimeStamp);
            try {
                XMLGregorianCalendar fecha = DatatypeFactory.newInstance().newXMLGregorianCalendar(unGregorianCalendar);
                clsTiquete.setFechaEmision(fecha);
            } catch (DatatypeConfigurationException ex) {
                Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            }

            clsTiquete.setTipoCambio(BigDecimal.ONE);
            //clsTiquete.setTotalServGravados(BigDecimal.ZERO);
            //clsTiquete.setTotalServExentos(BigDecimal.ZERO);
            //clsTiquete.setTotalMercanciasGravadas(BigDecimal.ZERO);
            //clsTiquete.setTotalMercanciasExentas(BigDecimal.ZERO);
            //clsTiquete.setTotalGravado(BigDecimal.ZERO);
            //clsTiquete.setTotalExento(BigDecimal.ZERO);
            clsTiquete.setSinInternet(false);
            clsTiquete.setAdjunto(null);

            WSTiqueteElectronico.ArrayOfClsDetalleServicio detalleServicioArray = new WSTiqueteElectronico.ArrayOfClsDetalleServicio();
            WSTiqueteElectronico.ClsDetalleServicio detalleServicio;
            for (DetalleFactura detalleFactura : factura.getDetalleFactura()) {
                detalleServicio = new WSTiqueteElectronico.ClsDetalleServicio();
                detalleServicio.setCantidad(BigDecimal.valueOf(detalleFactura.getCantidad()));
                detalleServicio.setUnidadMedida(detalleFactura.getUnidadMedida());
                detalleServicio.setDetalleDescripcion(detalleFactura.getDescripcion());
                detalleServicio.setPrecioUnitario(detalleFactura.getPrecioUnitario());  //Unidades de Medida basadas en el estándar RTC 443:2010.
                detalleServicio.setMontoTotal(detalleFactura.getMonto());
                detalleServicio.setSubTotal(detalleFactura.getSubTotal());
                detalleServicio.setMontoTotalLinea(detalleFactura.getMontoTotalLinea());
                detalleServicio.setMontoDescuento(detalleFactura.getMontoDescuento());
                detalleServicio.setNaturalezaDescuento(detalleFactura.getNaturalezaDescuento());

                WSTiqueteElectronico.ArrayOfClsDetalleImpuesto detalleImpuestoList = new WSTiqueteElectronico.ArrayOfClsDetalleImpuesto();
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
            clsTiquete.setDetalleServicio(detalleServicioArray);

            clsTiquete.setInformacionReferencia(null);
            WSTiqueteElectronico.ClsOtros otros = new WSTiqueteElectronico.ClsOtros();
            WSTiqueteElectronico.ArrayOfOtros array = new WSTiqueteElectronico.ArrayOfOtros();
            otros.setOtrosTexto(array);
            clsTiquete.setOtros(otros);

            clsTiquete.setCodCondicionVenta(factura.getCondicionVenta());
            clsTiquete.setCodMedioPago1(factura.getCodigMedioPago1());
            clsTiquete.setCodMedioPago2(factura.getCodigMedioPago2());
            clsTiquete.setCodMedioPago3(factura.getCodigMedioPago3());
            clsTiquete.setCodMedioPago4(factura.getCodigMedioPago4());
            clsTiquete.setTotalMercanciasGravadas(factura.getTotalVenta().subtract(factura.getTotalVentaExento()));
            clsTiquete.setTotalMercanciasExentas(factura.getTotalVentaExento());
            clsTiquete.setTotalGravado(factura.getTotalVenta().subtract(factura.getTotalVentaExento()));
            clsTiquete.setTotalExento(factura.getTotalVentaExento());
            clsTiquete.setTotalVenta(factura.getTotalVenta());
            clsTiquete.setTotalDescuentos(factura.getTotalDescuentos());    //-
            clsTiquete.setTotalVentaNeta(factura.getTotalVentaNeta());      //=   
            clsTiquete.setTotalImpuesto(factura.getTotalImpuesto());        //-
            clsTiquete.setTotalComprobante(factura.getTotalComprante());    //=
            clsTiquete.setSecuencialERP(String.valueOf(factura.getSecuencia()));
            
            if (!factura.getClaveComprobante().equals("")) {
                clsTiquete.setClaveComprobante(factura.getClaveComprobante());
            }
            
        } catch (Exception ex) {

        }

        return clsTiquete;
    }
}
