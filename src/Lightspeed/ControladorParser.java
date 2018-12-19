/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lightspeed;

import Entidades.DatosEmpresa;
import interfaces.IParser;
import java.util.HashSet;
import java.util.Set;
import Entidades.DetalleFactura;
import Entidades.DetalleImpuesto;
import Entidades.Factura;
import FacturaElectronica.GuruSoft.KeyGenerationController;
import Impresoras.Epson.ControladorImpresion;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import servidor1.ConfigurationController;
import servidor1.ControladorDB;
/**
 *
 * @author luis.arce
 */
public class ControladorParser implements IParser {
    // Move all these to a Properties/Configuration class
    private ParserConfiguration pConfiguration = ConfigurationController.getInstance().parser;
    private ControladorDB cDB = new ControladorDB();
    private ControladorERPConector lightSpeedConector = new ControladorERPConector();
    private ControladorImpresion cPrinter = new  ControladorImpresion();
    private KeyGenerationController keyGeneration = new KeyGenerationController();
    private DatosEmpresa datosEmpresa = cDB.ObtenerDatosEmpresa();
         
    @Override 
    public void procesarFactura(String rawFactura, List<String> lineasFactura){
        System.out.println("Enter procesarFactura");
        int invoiceId = getInvoiceId(lineasFactura);
        Factura newInvoice;
               
        if (invoiceId < 0) {
            System.out.println("Cannot get OrderID from Invoice Lines");
            return;
        }
                                  
        newInvoice = lightSpeedConector.getInvoiceInformation(invoiceId); //Use API
        
        //cDB.InsertInvoice(newInvoice);
        if (newInvoice == null) {
            System.out.println("getInvoiceInformation returned NULL value");
            return;
        }
  
        checkInvoiceStatus(newInvoice);
        
        System.out.println(invoiceId + "|");
        System.out.println("The invoice was processed  ORDERID: " + invoiceId);
    }
            
    private void printInvoice(Factura invoiceToPrint, Factura closedInvoice) {
        try {
            if (closedInvoice != null) {
                if ((closedInvoice.getClaveComprobante() != null) && (closedInvoice.getNumeroConsecutivo() != null)) {
                    invoiceToPrint.addHaciendaInfo(closedInvoice.getClaveComprobante(), closedInvoice.getNumeroConsecutivo());
                
                    invoiceToPrint.setClaveComprobante(closedInvoice.getClaveComprobante());
                    invoiceToPrint.setNumeroConsecutivo(closedInvoice.getNumeroConsecutivo());
                }             
            }
                        
            //Enviar a imprimir la factura:
            cPrinter.printInvoice(invoiceToPrint);
        } catch (IOException ex) {
            cDB.InsertarBitacoraSistema(invoiceToPrint.getSecuencia(), ex.toString());
        } catch (InterruptedException ex) {
            cDB.InsertarBitacoraSistema(invoiceToPrint.getSecuencia(), ex.toString());
        }
    }
    
    public void checkInvoiceStatus(Factura invoice) { //TODO: Get status from configuration
        if (invoice == null) {
            return;
        }
        
        switch(invoice.getStatus()) {
            case "Owing": //Open
                printInvoice(invoice, null);
                break;
            case "Paid": //Closed
                Factura closedInvoice = cDB.getLastInvoiceByOrderID(invoice.getIdOrden());
                if (closedInvoice == null) {
                    generateHaciendaInformation(invoice);
                    cDB.InsertInvoice(invoice);
                }
                
                printInvoice(invoice, closedInvoice);
                
                break;
//            case "reopened":
//                printInvoice(invoice, null);
//                break;
//            case "reclosed": //TODO: Manage Key Generation with CreditMemos
//                Factura referenceInvoice = cDB.getLastInvoiceByOrderID(invoice.getIdOrden());
//                if (referenceInvoice != null) {
//                    if (cDB.isInCreditMemos(referenceInvoice.getIdOrden(), referenceInvoice.getNumeroConsecutivo())) {
//                        printInvoice(invoice, referenceInvoice);
//                    }
//                    else {
//                        createCreditMemo(referenceInvoice, invoice);
//                    }
//                }
//                else {
//                    generateHaciendaInformation(invoice);
//                    cDB.InsertInvoice(invoice);
//                    printInvoice(invoice, null);
//                }
//                    
//                break;
            default:
                break;
        }
    }
    
    private void generateHaciendaInformation(Factura invoice) {
         Date currentDay = new Date();
                    
        String consecutiveNumber = keyGeneration.CreaNumeroConsecutivo(String.valueOf(datosEmpresa.getMatriz()), 
                                                                       String.valueOf(datosEmpresa.getPuntoVenta()), 
                                                                       invoice.getEsFactura() == 1 ?  "01": "04", 
                                                                       cDB.getFacturaNextSequence());

        String securityCode = keyGeneration.CreaCodigoSeguridad(invoice.getEsFactura() == 1 ?  "01": "04", 
                                                                "1", String.valueOf(datosEmpresa.getPuntoVenta()), 
                                                                currentDay, cDB.getFacturaNextSequence());

        String claveComprobante = keyGeneration.CreaClave("506", String.valueOf(currentDay.getDate()), 
                                                                 String.valueOf(currentDay.getMonth()+1), 
                                                                "18", datosEmpresa.getNumIdentificacion(), 
                                                                consecutiveNumber, "1", 
                                                                securityCode);
        System.out.println();
        System.out.println();
        System.out.println("Generated Consecutive: " + consecutiveNumber);
        System.out.println("Generated claveComprobante: " + claveComprobante);
        System.out.println();
        System.out.println();

        invoice.addHaciendaInfo(claveComprobante, consecutiveNumber);
        invoice.setClaveComprobante(claveComprobante);
        invoice.setNumeroConsecutivo(consecutiveNumber);
    }
    
    private void createCreditMemo(Factura referenceInvoice, Factura invoice) {     
        cDB.insertCreditMemo(referenceInvoice.getIdOrden(), referenceInvoice.getNumeroConsecutivo(), 
                             referenceInvoice.getFechaAutorizacion().toString());
        
        cDB.InsertInvoice(invoice);
    }
    
    private int getInvoiceId(List<String> InvoiceLines) {
        int orderId = -1;
        
        System.out.println("Enter getInvoiceId");
        
        for(int index = 0; index < InvoiceLines.size(); index++) {
            if (InvoiceLines.get(index).contains("Title")) {
                String lineLowerCase = InvoiceLines.get(index).toLowerCase();
                int idIndex = lineLowerCase.indexOf("i-"); //TODO: Use configuration
                
                if (idIndex >= 0) {
                    orderId = getID(idIndex + 2, lineLowerCase); //Pass number start index
                    break;
                }
            }
        }
        
        System.out.println("Leave getInvoiceId");
               
        return orderId;
    }
    
    private int getID(int index, String line) {
        int result = -1;
        String resultStr = "";
        
        for(int i = index; i < line.length(); i++) {
            if (Character.isDigit(line.charAt(i))) {
                resultStr = resultStr + line.charAt(i);              
            }
            else {
                break;
            }
        }
        
        if (!resultStr.equals("")) {
            result = Integer.parseInt(resultStr);
        }
                              
        return result;
    }
}
