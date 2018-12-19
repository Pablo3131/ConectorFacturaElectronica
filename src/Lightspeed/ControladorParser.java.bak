/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lavu;

import interfaces.IParser;
import java.util.HashSet;
import java.util.Set;
import Entidades.DetalleFactura;
import Entidades.DetalleImpuesto;
import Entidades.Factura;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import servidor1.ControladorDB;
/**
 *
 * @author luis.arce
 */
public class ControladorParser implements IParser {
    // Move all these to a Properties/Configuration class
    private ParserConfiguration pConfiguration = new ParserConfiguration("Spanish"); //TODO: Demo proposes
    private ControladorDB cDB = new ControladorDB();
    private final static String FACTURADIVISIONES = "------";
    private final static int HEADERSECTION = 0;
    private final static int DETAILSSECTION = 1;
    private final static int TOTALSSECTION = 2;
    private final static int COMPANYINFOSECTION = 3;
      
    @Override 
    public void procesarFactura(String rawFactura, List<String> lineasFactura){
        
        
        String orderId = getOrderId(lineasFactura);        
        try {
            System.out.println("Tiquete recibido de Lavu con orden#: " + orderId);
            Thread.sleep(5000);            
            /* TODO: First Try is get information from the API */
        } catch (InterruptedException ex) {
            Logger.getLogger(ControladorParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        // Get information from the invoice
        Factura newInvoice = getFacturaInformation(lineasFactura, orderId);
        
        cDB.InsertInvoice(newInvoice);             
        
        //System.out.println(orderId + "|");
        //System.out.println("Factura Procesada correctamente");
    }
    
    private Factura getFacturaInformation(List<String> lineasFactura, String orderID){
        Factura nuevaFactura = new Factura(); //TODO: Set Default values of BigDecimals to Cero
        nuevaFactura.setInvoiceLinesToPrint(new ArrayList(lineasFactura));
        int currentSecction = HEADERSECTION;
        int lineasConteo = 0;
        
        nuevaFactura.setIdOrden(orderID);
        
        for(int index = 0; index < lineasFactura.size(); index++) {
            String currentLine = lineasFactura.get(index);
            
            if (isLineValid(currentLine)) {
                if (isInvoiceDivision(currentLine)) {
                    currentSecction += 1;
                    continue;
                }
                
                if (currentSecction == TOTALSSECTION) {
                    processTotalSection(nuevaFactura, currentLine);
                    
                    
                }
                else if(currentSecction == DETAILSSECTION) {
                    processInvoiceDetailSection(nuevaFactura, currentLine);
                    nuevaFactura.setIndexHaciendaInformation(index - 2); // Put Hacienda Key and Secuencia
                    nuevaFactura.addHaciendaInfo("", "");
                }
            }
        }
        
        return nuevaFactura;
    }
    
    private void processInvoiceDetailSection(Factura invoice, String line) {
        int lineCount = invoice.getDetalleFactura().size();
        
        String discountLine = pConfiguration.getParserKeyWords().get("DISCOUNTLINE");
            String coupon = pConfiguration.getParserKeyWords().get("COUPON");
        
        if (isFirstCharNumber(line) && (!line.contains(discountLine))) { // This indicate the init of a new invoice detail
            lineCount += 1;
            DetalleFactura dFactura = construirDetalleFactura(line, lineCount);                       
            dFactura.setLinea(lineCount);
            invoice.setDetalleFactura(dFactura);
        }
        else {
            DetalleFactura dFActura = invoice.getDetalleFactura().get(lineCount - 1);      
            BigDecimal additionalAmount = new BigDecimal(getInvoiceDetailAdditionalAmount(line));
            
            if((line.contains(discountLine)|| (line.contains(coupon)))) {
                dFActura.setMontoDescuento(additionalAmount);
                dFActura.setNaturalezaDescuento(getLineaContenido(line, 3));
                dFActura.setSubTotal(dFActura.getMonto().subtract(additionalAmount));
                DetalleImpuesto dImpuesto = dFActura.getdImpuesto().get(0); // TODO: Only 1 tax for the lines
                dImpuesto.setMonto(dFActura.getSubTotal().multiply(pConfiguration.getTaxPercentage()));
                dFActura.setMontoTotalLinea(dFActura.getSubTotal().add(dImpuesto.getMonto()));
            }
            else {
                dFActura.setMonto(dFActura.getMonto().add(additionalAmount));
                dFActura.setSubTotal(dFActura.getMonto());          
                dFActura.setPrecioUnitario(dFActura.getMonto().divide(BigDecimal.valueOf(dFActura.getCantidad())));
                DetalleImpuesto dImpuesto = dFActura.getdImpuesto().get(0); // TODO: Only 1 tax for the lines
                dImpuesto.setMonto(dFActura.getSubTotal().multiply(pConfiguration.getTaxPercentage()));
                dFActura.setMontoTotalLinea(dFActura.getSubTotal().add(dImpuesto.getMonto()));
                
            }                       
        }
    }
    
    private void processTotalSection(Factura invoice, String line) {
        String paymentMethod = "";
        Map<String, String> pKeyWords = pConfiguration.getParserKeyWords();
        
        if (line.contains(pKeyWords.get("SUBTOTALLINE"))) {
            BigDecimal subTotal = new BigDecimal(getNumberBackward(line));
            invoice.setTotalVenta(subTotal); 
            invoice.setTotalVentaNeta(subTotal);
        }
        //TODO: CHECK IF 10% IS REQUIRE
        else if (line.contains(pKeyWords.get("TOTALLINE")) && 
                !line.contains(pKeyWords.get("SUBTOTALLINE"))) {
            //BigDecimal total = new BigDecimal(getNumberBackward(line));
            //invoice.setTotalComprante(total); //TODO: CHECK IF 10% IS REQUIRE
            invoice.setTotalComprante(invoice.getTotalVentaNeta().add(invoice.getTotalImpuesto()));
        }
        else if(line.contains(pKeyWords.get("DISCOUNTLINE"))) {
            BigDecimal discountTotal = new BigDecimal(getNumberBackward(line));
            invoice.setTotalDescuentos(discountTotal);
            invoice.setTotalVentaNeta(invoice.getTotalVenta().subtract(invoice.getTotalDescuentos()));
        }
        else if(line.contains(pKeyWords.get("TAXESLINE"))) {
            BigDecimal taxes = new BigDecimal(getNumberBackward(line));
            invoice.setTotalImpuesto(taxes);
            BigDecimal total = invoice.getTotalVentaNeta().add(taxes); //TODO: CHECK IF 10% IS REQUIRE
            invoice.setTotalComprante(total); //TODO: CHECK IF 10% IS REQUIRE
        }
        else if(!(paymentMethod = getPaymentMethod(line)).equals("")){
            invoice.setPaymentMethod(paymentMethod);
        }
    }
     
    private String getPaymentMethod(String line) {
        String result = "";
        
        Map<String, String> paymentMethods = pConfiguration.getPaymentMethods();
        for (Map.Entry<String, String> entry : paymentMethods.entrySet())
        {
            if (line.contains(entry.getKey())) {
                result = entry.getValue();
                break;
            }
        }
        
        return result;
    }
    
    private String getInvoiceDetailAdditionalAmount(String line) {
        String result = getNumberBackward(line);
        
        if (result.equals("")) {
            result = "0.0";
        }
              
        return result;
    }
    
    private String getNumberBackward (String line) {
        String[] chars = line.split("");
        String total = "";
        
        for(int index = chars.length - 1; index >= 0; index--) {
            if((Character.isDigit(chars[index].charAt(0))) ||
               (chars[index].equals(".")) ||
               (chars[index].equals(","))) {
                total = chars[index] + total;
            }
            else {
                break;
            }
        }
        
        return total;
    }
    
    private String getNumber(String number) {
        String[] chars = number.split("");
        String result = "";
        
        for(int index = 0; index < chars.length; index++) {
            // Check if Character is part of a Number
            if((Character.isDigit(chars[index].charAt(0))) ||
               (chars[index].equals(".")) ||
               (chars[index].equals(","))) {
                result += chars[index];
            }
            else {
                break;
            }
        }
        
        return result;
    }
    
    private void buildInvoiceDefault(Factura invoice) {
        invoice.setTotalVenta(new BigDecimal("0.0")); ;
        invoice.setTotalComprante(new BigDecimal("0.0"));
        invoice.setTotalImpuesto(new BigDecimal("0.0"));
        invoice.setTotalVentaNeta(new BigDecimal("0.0"));            
        invoice.setTotalDescuentos(new BigDecimal("0.0"));
    }
    
    
    private DetalleFactura construirDetalleFactura(String line, int lineCount) {
        String[] lineChars = line.split("");
        DetalleFactura newFLine = new DetalleFactura();
        int lineQuantity = -1;
        BigDecimal lineAmount = new BigDecimal("-1.0");
        String lineDescription = "";
        List<DetalleImpuesto> dImpuesto = new ArrayList<DetalleImpuesto>();
        
        newFLine.setLinea(lineCount);
        
        for(int index = 0; index < lineChars.length; index++) {
            if(Character.isDigit(lineChars[index].charAt(0))) {
                if (lineQuantity == -1) { // Check if Quantity has not set yeld
                    String numberString = getNumber(line.substring(index, line.length()));
                    lineQuantity = Integer.parseInt(numberString);
                }
                else {
                    String numberString = getNumber(line.substring(index, line.length()));
                    lineAmount = new BigDecimal(numberString);
                    break;
                }
            }
            else if ((Character.isAlphabetic(lineChars[index].charAt(0))) && lineDescription.equals("")) {
                lineDescription = getLineaContenido(line.substring(index, line.length()), 3);
            }
        }
        
        newFLine.setCantidad(lineQuantity);
        newFLine.setMonto(lineAmount);
        newFLine.setSubTotal(lineAmount);
        newFLine.setDescripcion(lineDescription);
        newFLine.setPrecioUnitario(newFLine.getMonto().divide(BigDecimal.valueOf(newFLine.getCantidad())));
        newFLine.setUnidadMedida("Unid");
        dImpuesto.add(new DetalleImpuesto());
        dImpuesto.get(0).setTarifa(pConfiguration.getTaxPercentage().multiply(new BigDecimal("100.0"))); //TODO: Put in a config file
        dImpuesto.get(0).setMonto(newFLine.getSubTotal().multiply(pConfiguration.getTaxPercentage()));
        newFLine.setMontoTotalLinea(newFLine.getSubTotal().add(dImpuesto.get(0).getMonto()));
        newFLine.setdImpuesto(dImpuesto);
        
        return newFLine;
    }
    
    private String getOrderId(List<String> InvoiceLines) {
        String orderId = "";
        String orderIDKeyWord = pConfiguration.getParserKeyWords().get("ORDERIDLINE");
        
        for(int index = 0; index < InvoiceLines.size(); index++) {
            String currentLine = InvoiceLines.get(index);
            
            if (isLineValid(InvoiceLines.get(index))) {
                if (currentLine.contains(orderIDKeyWord)){
                    String lineTrim = currentLine.trim();
                    for(int lineTrimIndex = currentLine.indexOf(orderIDKeyWord); lineTrim.length() > lineTrimIndex; lineTrimIndex++) {
                        if (Character.isDigit(lineTrim.charAt(lineTrimIndex))) {
                            orderId = getOrderIdDeLinea(currentLine, lineTrimIndex);
                            break;
                        }
                    }
                    
                    break;
                }
            }
            
        }
               
        return orderId;
    }
    
    private String getOrderIdDeLinea(String linea, int indexOrderId) {
        String orderId = "";
        String[] lineaChars = linea.split("");
        
        for(int index = indexOrderId; index < lineaChars.length; index++) {
            if(!lineaChars[index].equals(" ")) {
                orderId += lineaChars[index];
            }
            else {
                break;
            }
        }
        
        return orderId;
    }
    
    private String getLineaContenido(String linea, int emptySpaceBeforeBreak) {
        String result = "";
        String resultBuffer = "";
        boolean firstCharFound = false;
        int emptySpacesCount = 0; 
        
        String[] lineaChars = linea.split("");
        
        for(int index = 0; index < lineaChars.length; index++) {
            firstCharFound = !lineaChars[index].equals(" ") || firstCharFound;
            emptySpacesCount = firstCharFound && lineaChars[index].equals(" ") ? emptySpacesCount + 1 : 0;
            
            if (firstCharFound) {
                if(emptySpacesCount > emptySpaceBeforeBreak) {
                    result = resultBuffer.substring(0, resultBuffer.length() - emptySpaceBeforeBreak);
                    break;
                }
                else {
                    resultBuffer += lineaChars[index];
                }                
            }
        }
        
        return result;
    }
    
    private boolean isLineValid(String linea) {
        return linea != null && linea != "";
    }
    
    private boolean isInvoiceDivision(String linea) {
        return linea.indexOf(FACTURADIVISIONES) >= 0;
    }
    
    private boolean isFirstCharNumber(String linea) {
        boolean result = false;
        String[] lineaChars = linea.split("");
        
        for(int index = 0; index < lineaChars.length; index++) {
            if(!lineaChars[index].equals(" ")) {
                result = Character.isDigit(lineaChars[index].charAt(0));
                break;
            }
        }
        
        return result;
    }    
}
