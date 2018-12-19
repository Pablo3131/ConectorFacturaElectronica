/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lightspeed;

import Entidades.DetalleFactura;
import Entidades.DetalleImpuesto;
import Entidades.Factura;
import Entidades.Filter;
import Entidades.FilterList;
import Entidades.Retentions;
import Entidades.Taxes;
import Lightspeed.DTO.CustomerInfo;
import Lightspeed.DTO.InvoiceLineItem;
import Lightspeed.DTO.InvoiceLineItemTax;
import Lightspeed.DTO.InvoiceReply;
import Lightspeed.DTO.paymentMethod;
import interfaces.IERPConector;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import servidor1.ConfigurationController;

/**
 *
 * @author Usuario
 */
public class ControladorERPConector implements IERPConector {
    
    private static final String USER_AGENT = "Mozilla/5.0";
    String api_url = "";
    String api_dataname = "";
    String api_key = "";
    String api_token = "";
    private ERPConector conector;
    
    public ERPConectorConfiguration ERPconfiguration = null;
    
    public ControladorERPConector() {
        conector = new ERPConector();
        
//        ERPconfiguration = ConfigurationController.getInstance().ERPConector;
//        
//        api_url = ERPconfiguration.apiInformation.get("URL");
//        api_dataname = ERPconfiguration.apiInformation.get("DATANAME");
//        api_key = ERPconfiguration.apiInformation.get("KEY");
//        api_token = ERPconfiguration.apiInformation.get("TOKEN");
    }
    
    public Factura getInvoiceInformation(int invoiceId) {
        System.out.println("Enter: getInvoiceInformation");
        Factura result = new Factura();
        
        InvoiceReply invoiceInfo = conector.getInvoice(invoiceId);
        buildInvoiceHeader(result, invoiceInfo);    
        buildInvoicePaymentMethods(result, invoiceInfo);
        
        buildInvoiceLines(result, invoiceInfo); //TODO: Manage tax included with Real data
        
//        BigDecimal retentionsTotalAmount = new BigDecimal("0.0");
//        Factura result = new Factura();
//        FilterList orderPaymentsFilters = new FilterList();
//        FilterList orderLinesFilters = new FilterList();
//        
//        orderPaymentsFilters.addFilter(new Filter("order_id", "=", idOrder));
//        orderPaymentsFilters.addFilter(new Filter("voided", "=", "0")); //TODO: Remove this???
//        orderLinesFilters.addFilter(new Filter("order_id", "=", idOrder));
//        orderLinesFilters.addFilter(new Filter("void", "=", "0")); //TODO: Remove this???
//        
//        List<String> invoiceInfo = postData("&table=orders&column=order_id&value=" + idOrder);
//        List<String> invoiceLines = postData("&table=order_contents" + orderLinesFilters.builfFilterList());
//        List<Map<String, String>> invoiceLinesMap = buildMapListFromQueryResult(invoiceLines);
//        List<String> invoicePayments = postData("&table=order_payments" + orderPaymentsFilters.builfFilterList());
//        
//        if (invoiceInfo.size() == 0) {
//            return null;
//        }
//        
//        buildInvoiceHeader(result, invoiceInfo);
//        
//        if (result.getStatus().equals("open") || 
//            result.getStatus().equals("reopened")) { //Sometimes Lavu take longer to update info - try again
//            performDelay(3);
//            invoiceInfo = postData("&table=orders&column=order_id&value=" + idOrder);
//            buildInvoiceHeader(result, invoiceInfo);
//        }
//        
//        if (result.exemptionName.equals("Selina")) {
//           result.setStatus("open");
//           return result;
//        }
//        
//        if (result.getTotalComprante().equals(new BigDecimal("0.0"))) { 
//            return null; //TODO: Ignore when there is nothing in the invoice??
//        }
//        
//        buildInvoicePaymentMethods(result, invoicePayments); //Define Payment methods before process invoice lines
//        buildInvoiceLines(result, invoiceLinesMap);
//           
//        result.setIdOrden(idOrder);
//        
//        // Add Retentions to Invoice Lines List and Add amount to Total
//        for (DetalleFactura retention : result.getRetentions()) {
//            retention.setLinea(result.getDetalleFactura().size() + 1);
//            result.setDetalleFactura(retention);
//            
//            retentionsTotalAmount = retentionsTotalAmount.add(retention.getMonto());
//        }
//        
//        // Add retentions to Invoice Total Venta
//        result.setTotalVenta(result.getTotalVenta().add(retentionsTotalAmount));
//        result.setTotalVentaNeta(result.getTotalVenta().subtract(result.getTotalDescuentos())); //TODO: manage discounts properly
//        
//        //Calcular Exento Amount
//        for(DetalleFactura line : result.getDetalleFactura()) {
//            if (line.getdImpuesto().size() == 0) {
//                result.setTotalVentaExento(result.getTotalVentaExento().add(line.getMontoTotalLinea()));
//            }
//        }

        System.out.println("Leave: getInvoiceInformation");
        
        return result;
    }
       
    private void buildInvoiceHeader(Factura invoice, InvoiceReply invoiceInfo) {
        System.out.println("Enter: buildInvoiceHeader");
        
        invoice.setTotalVenta(convertStringToBigDecimal(invoiceInfo.totals.subtotal));
        invoice.setTotalVentaNeta(invoice.getTotalVenta()); //TODO: This could change if there is discount
        invoice.setTotalComprante(convertStringToBigDecimal(invoiceInfo.totals.total));
        invoice.setTotalDescuentos(BigDecimal.ZERO); //TODO: Manage discount
        invoice.setTotalImpuesto(convertStringToBigDecimal(invoiceInfo.totals.tax));
        invoice.setStatus(invoiceInfo.status);
        invoice.setIdOrden(invoiceInfo.invoice_id);
        setCustomerInfo(invoice, invoiceInfo.invoice_customer); //TODO: Finish with this function
                
        System.out.println("Leave: buildInvoiceHeader");
    }
    
    private void buildInvoicePaymentMethods(Factura invoice, InvoiceReply invoiceInfo) {
        System.out.println("Enter: buildInvoiceHeader");
        
        for(int index = 0; index < invoiceInfo.payments.size(); index++) {
            paymentMethod payment = conector.getPaymentMethod(invoiceInfo.id, invoiceInfo.payments.get(index).id);
            
            switch(payment.type) { //TODO: Get Payment types from configuration
//                case "Cash": //TODO: Manage cash
//                    invoice.setPaymentMethod("01");
//                    break;
                case "Credit Card":
                    invoice.setPaymentMethod("02"); //TODO: Get Payment types from configuration
                    break;
                default:
                    invoice.setPaymentMethod("99");
                    break;
            }
        }

        System.out.println("Leave: buildInvoiceHeader");
    }
    
    private void buildInvoiceLines(Factura invoice, InvoiceReply invoiceInfo) {
        System.out.println("Enter: buildInvoiceLines");
        BigDecimal exeptAmount = BigDecimal.ZERO;
        
        for(int index = 0; index < invoiceInfo.lineitems.size(); index++) {
            DetalleFactura newLine = new DetalleFactura();
            BigDecimal taxesAmount = BigDecimal.ZERO;
            InvoiceLineItem lineInfo = conector.getInvoiceLine(invoiceInfo.id, invoiceInfo.lineitems.get(index).id);
            
            newLine.setDescripcion(lineInfo.lineitem_product.description);
            
            newLine.setCantidad(convertStringToInt(lineInfo.quantity));
           // 
           
           List<DetalleImpuesto> taxesList = new ArrayList<DetalleImpuesto>();
           for(InvoiceLineItemTax tax : lineInfo.taxes) {
               if (!tax.exempt) {
                   DetalleImpuesto newTax = new DetalleImpuesto();
                   newTax.setMonto(convertStringToBigDecimal(tax.total)); //TODO: Manage more different of taxes
                   taxesList.add(newTax);
                   taxesAmount = taxesAmount.add(newTax.getMonto());
               }
           }
           
           newLine.setdImpuesto(taxesList);
           
           newLine.setMontoTotalLinea(convertStringToBigDecimal(lineInfo.sells.total).add(taxesAmount));
           newLine.setSubTotal(convertStringToBigDecimal(lineInfo.sells.total));
           newLine.setMonto(newLine.getSubTotal()); //TODO: This could be different if there is discount
           newLine.setPrecioUnitario(newLine.getMonto().divide(BigDecimal.valueOf(newLine.getCantidad()), RoundingMode.HALF_UP));
           
           if (newLine.getdImpuesto().size() == 0) {
               exeptAmount = exeptAmount.add(newLine.getSubTotal());
           }
                   
           invoice.setDetalleFactura(newLine);
            
        }
        
        invoice.setTotalVentaExento(exeptAmount);
        
//        for(Map<String, String> lineMap : invoiceLinesMap) {
//            Boolean calculateTaxes = false;
//            DetalleFactura line = new DetalleFactura();
//            
//            line.setDescripcion(lineMap.get("item"));
//            line.setCantidad(convertStringToInt(lineMap.get("quantity")));
//            line.setMontoTotalLinea(convertStringToBigDecimal(lineMap.get("tax_subtotal1")));
//            line.setSubTotal(convertStringToBigDecimal(lineMap.get("tax_subtotal1")));
//            
//            if (lineMap.get("discount_id").equals("")) {
//                line.setMonto(line.getSubTotal());
//                
//                if (lineMap.get("tax_subtotal1").equals(lineMap.get("total_with_tax"))) { //TODO: Compare BigDecimal???          
//                    calculateTaxes = true;
//                }
//                else {
//                    line.setMontoDescuento(new BigDecimal("0.0"));
//                    line.setNaturalezaDescuento("");
//                }        
//            }
//            else {
//                BigDecimal discountRate = convertStringToBigDecimal(lineMap.get("discount_value"));
//                BigDecimal discountPercentage = (new BigDecimal("1.00").subtract(discountRate)).multiply(new BigDecimal("100.00"));
//                line.setMonto((line.getSubTotal().multiply(new BigDecimal("100.00"))).divide(discountPercentage, RoundingMode.HALF_UP));
//                line.setMontoDescuento(line.getMonto().subtract(line.getSubTotal()));
//                line.setNaturalezaDescuento(getDiscountDescription(lineMap.get("discount_id")));
//            }
//            
//            line.setPrecioUnitario(line.getMonto().divide(BigDecimal.valueOf(line.getCantidad()), RoundingMode.HALF_UP));
//            
//            setInvoiceLineTaxes(invoice, line, lineMap, calculateTaxes);
//            
//            invoice.setDetalleFactura(line);
//            invoice.setTotalVenta(invoice.getTotalVenta().add(line.getMonto()));
//            invoice.setTotalDescuentos(invoice.getTotalDescuentos().add(line.getMontoDescuento()));
//            line.setLinea(invoice.getDetalleFactura().size());
//        }
        
        System.out.println("Leave: buildInvoiceLines");
    }
        
    
    
    public void setCustomerInfo(Factura invoice, CustomerInfo custInfo) {
        System.out.println("Enter setCustomerInfo");
        
        if (custInfo.name.equals("")) {
            invoice.setEsFactura(0);
        }
        else {
            invoice.setNombreCliente(custInfo.name);
            invoice.setTipoIdReceptor("01"); //TODO: Detect receptor type
            invoice.setEsFactura(1);
        }
        
        System.out.println("Leave setCustomerInfo");
        
//        customerInfo = customerInfo.replaceAll("&quot", "");
//        customerInfo = customerInfo.replaceAll("print_info", "");
//        customerInfo = customerInfo.replaceAll(";", "");
//        
//        String[] customerInfoSplit = customerInfo.split("\\|");
//        
//        System.out.println("SPLIT: ");
//        for (int index = 0; index < customerInfoSplit.length; index++) {         
//            if (customerInfoSplit[index].contains("{:[")) {
//                String infoFields = customerInfoSplit[index].substring(3, customerInfoSplit[index].length());
//                infoFields = infoFields.substring(0, infoFields.length() - 2);
//                String[] infoFieldsSplit = infoFields.split("\\},\\{");
//                
//                System.out.println("infoFields: ");
//                for(int fieldsIndex = 0; fieldsIndex < infoFieldsSplit.length; fieldsIndex++) {
//                   if (infoFieldsSplit[fieldsIndex].contains("First_Name") || 
//                      (infoFieldsSplit[fieldsIndex].contains("Last_Name") && !isCustomerIDInLastName(infoFieldsSplit[fieldsIndex]))) { //TODO: This is very especific to LAVU                   
//                       if (infoFieldsSplit[fieldsIndex].split(",").length > 3) {
//                           String customerName = infoFieldsSplit[fieldsIndex].split(",")[3];
//                           setCustomerName(invoice, customerName);
//                       }                                        
//                   }
//                   else if (infoFieldsSplit[fieldsIndex].contains(ERPconfiguration.customerInformation.get("IDField"))) {
//                       if (infoFieldsSplit[fieldsIndex].split(",").length > 3) {                           
//                           String customerId = infoFieldsSplit[fieldsIndex].split(",")[3];
//                           setCustomerID(invoice, customerId);                
//                       }       
//                   }
//                   else if (infoFieldsSplit[fieldsIndex].contains("Email")) {
//                       if (infoFieldsSplit[fieldsIndex].split(",").length > 3) {
//                           String customerEmail = infoFieldsSplit[fieldsIndex].split(",")[3];
//                           setCustomerEmail(invoice, customerEmail);
//                       }
//                   }
//                }
//                
//                break;
//            }
//        }
//        
//        System.out.println("OrderID: " + invoice.getIdOrden());
//        System.out.println("Name: " + invoice.getNombreCliente());
//        System.out.println("ID: " + invoice.getIdReceptor());
//        System.out.println("Email: " + invoice.getCorreoElectronicoCliente());
//        System.out.println("ID Tipo: " + invoice.getTipoIdReceptor());
//        System.out.println("EsFactura: " + invoice.getEsFactura());
//        
//        System.out.println();
//        
//        if (ERPconfiguration.context.equals("test")) { // Use fake email on test context
//            invoice.setCorreoElectronicoCliente("luchoorg.arce@gmail.com");
//        }
    }
    
    private boolean isCustomerIDInLastName(String customerInfo) {
        boolean result = false;
        
        if (ERPconfiguration.customerInformation.get("IDField").equals("Last_Name")) {
            result = true;
        }
        
        return result;
    }
            
    private void setCustomerEmail(Factura invoice, String customerInfo) { //TODO: technical Debt - put this logic in a single method
        String customerEmail = "";
        
        if (customerInfo.split(":").length > 1) {
            customerEmail = customerInfo.split(":")[1];
            invoice.setCorreoElectronicoCliente(customerEmail.replaceAll(" ", ""));
        } 
    }
    
    private void setCustomerName(Factura invoice, String customerInfo){
        String customerName = "";
        
        if (customerInfo.split(":").length > 1) {
            customerName = !invoice.getNombreCliente().equals("") ? 
                                invoice.getNombreCliente() + " " + customerInfo.split(":")[1] : 
                                customerInfo.split(":")[1];
            invoice.setNombreCliente(customerName);
            invoice.setEsFactura(1);
        }       
    }
    
    private void setCustomerID(Factura invoice, String customerInfo) {
        String customerID = "";
        
        if (customerInfo.split(":").length <= 1) {
            return;
        }
        
        customerInfo = customerInfo.split(":")[1].toLowerCase();
        
        if (customerInfo.contains(ERPconfiguration.customerInformation.get("LegalNumber"))) {
            invoice.setTipoIdReceptor("02");
            invoice.setEsFactura(1);
        }
        else if (customerInfo.contains(ERPconfiguration.customerInformation.get("IDNumber"))) {
            invoice.setTipoIdReceptor("01");
            invoice.setEsFactura(1);
        }
        else {
            invoice.setTipoIdReceptor("");
            invoice.setEsFactura(0);
        }
        
        for (int index = 0; index < customerInfo.length(); index++) {
            if (Character.isDigit(customerInfo.charAt(index))) {
                customerID = customerID + customerInfo.charAt(index);
            }
        }
        
        invoice.setIdReceptor(customerID);
    }
    
    private String getClientName(String clientInfo) {
        String[] clientInfoParts = clientInfo.split("\\|");
        String result = "";
        
        for (int index = 0; index < clientInfoParts.length; index++) {
            if (clientInfoParts[index].equals("Customer")) { // Customer is part of Lavu information
               result = clientInfoParts[index + 2];
            }
        }
        
        return result;
    }
        
    public String getData(String rowData, String fieldName) {
        String result = rowData.replace("<" + fieldName + ">", "");
        
        result = result.replace("</" + fieldName + ">", "");
              
        
        
        return result;
    }
    
    private BigDecimal getDataBigDecimal(String rowData, String fieldName) {
        String result = rowData.replace("<" + fieldName + ">", "");
        DecimalFormat decim = new DecimalFormat("0.00");
        Double resultDouble;
        
        result = result.replace("</" + fieldName + ">", "");
        
        if (result.indexOf(".") == 0) { // First character is Point(.)
            result = "0" + result; 
        }
        
        if (result.equals("")) {
            result = "0.0";
        }
        
        resultDouble = Double.parseDouble(result); 
        resultDouble = Double.parseDouble(decim.format(resultDouble));
               
        return new BigDecimal(Double.toString(resultDouble));
    }
    
    private BigDecimal convertStringToBigDecimal(String value) {
        DecimalFormat decim = new DecimalFormat("0.00");
        Double resultDouble;
        
        if (value.indexOf(".") == 0) { // First character is Point(.)
            value = "0" + value; 
        }
        
        if (value.equals("")) {
            value = "0.0";
        }
        
        resultDouble = Double.parseDouble(value); 
        resultDouble = Double.parseDouble(decim.format(resultDouble));
               
        return new BigDecimal(Double.toString(resultDouble));
    }
    
    private void performDelay(int seconds) {
        try        
            {
                TimeUnit.SECONDS.sleep(seconds);
            } 
            catch(InterruptedException ex) 
            {
                Thread.currentThread().interrupt();
            }
    }
    
    private int convertStringToInt(String StrInt) {
        int result = 0; 
        
        if ((StrInt == null) || (StrInt == "")) {
            return result;
        }
        
        int pointIndex = StrInt.indexOf(".");
        
        result = pointIndex < 0 ? Integer.parseInt(StrInt) : 
                                  Integer.parseInt(StrInt.substring(0, pointIndex));
        
        return result;
    }
    
//    private String getDiscountDescription(String discountId) {
//        String result = "";
//        
//        List<String> discountTypes = postData("&table=discount_types&column=id&value=" + discountId);
//        for(Map<String, String> discount : buildMapListFromQueryResult(discountTypes)) {
//            result = discount.get("title");
//            break;
//        }
//                
//        return result;
//    }
    
    private String getFieldName(String invoiceLineStr) {
        String result = "";
        
        for(int index = 0; index < invoiceLineStr.length(); index++) {
            char currentChar = invoiceLineStr.charAt(index);           
            if (currentChar == '<') {
                continue;
            }
            
            if (currentChar == '>') {
                break;
            }
            
            result = result + currentChar;              
        }

        return result;
    }
        
    private void setInvoiceLineTaxes(Factura invoice, DetalleFactura line, Map<String, String> lineMap, Boolean calculateTaxes) {
        List<DetalleImpuesto> taxesList = new ArrayList<DetalleImpuesto>();
        
        for(int taxIndex = 1; taxIndex <= 5; taxIndex++) {
            String taxName = lineMap.get("tax_name" + taxIndex);
            
            if (!taxName.equals("")) {
                String taxCode = getTaxCode(taxName);
                
                if (!taxCode.equals("")) { // IVI/IVA and others
                    DetalleImpuesto dTax = new DetalleImpuesto();
                    dTax.setTarifa(convertStringToBigDecimal(lineMap.get("tax_rate" + taxIndex)).multiply(new BigDecimal("100.0")));
                    dTax.setCodigo(taxCode);
                                                         
                    if (calculateTaxes) { // TODO: Technical Debt - What happen when the line has more than 1 tax???
                        BigDecimal taxRatePlusOne = isInRetentions(lineMap.get("tax_name" + (taxIndex + 1))) ? // TODO: Technical Debt - What happen when Servicio is before IVA/IVI??? - take in consideration all the retentions!!!
                                                        convertStringToBigDecimal(lineMap.get("tax_rate" + taxIndex)).add(convertStringToBigDecimal(lineMap.get("tax_rate" + (taxIndex + 1)))).add(BigDecimal.ONE) : 
                                                        convertStringToBigDecimal(lineMap.get("tax_rate" + taxIndex)).add(BigDecimal.ONE);
                        line.setSubTotal(line.getMontoTotalLinea().divide(taxRatePlusOne, RoundingMode.HALF_UP));     
                        dTax.setMonto(line.getSubTotal().multiply(convertStringToBigDecimal(lineMap.get("tax_rate" + taxIndex))));
                        line.setMontoTotalLinea(line.getSubTotal().add(dTax.getMonto())); // TODO: Technical Debt - What happen when the line has more than 1 tax???
                    } else {
                        dTax.setMonto(convertStringToBigDecimal(lineMap.get("tax" + taxIndex)));
                        line.setMontoTotalLinea(line.getMontoTotalLinea().add(dTax.getMonto()));
                    }
                    
                    taxesList.add(dTax);   
                    invoice.setTotalImpuesto(invoice.getTotalImpuesto().add(dTax.getMonto()));
                }
                else if (isInRetentions(taxName)) { // 10% Servic                   
                    BigDecimal retentionRate = convertStringToBigDecimal(lineMap.get("tax_rate" + taxIndex));
                    String retentionRateValue = retentionRate.multiply(new BigDecimal("100.0")).toPlainString();
                    BigDecimal retentionAmount = convertStringToBigDecimal(lineMap.get("tax" + taxIndex));
                    DetalleFactura newRetentionLine = new DetalleFactura();
                    
                    if (calculateTaxes) { // TODO: Technical Debt - What happen when the line has more than 1 retention???                       
                        retentionAmount = line.getSubTotal().multiply(convertStringToBigDecimal(lineMap.get("tax_rate" + taxIndex))); //.divide(retentionRatePlusOne, RoundingMode.HALF_UP);
                    }
                    
                    newRetentionLine.setDescripcion(taxName + " " + retentionRateValue + "%");
                    newRetentionLine.setCantidad(1);
                    newRetentionLine.setPrecioUnitario(retentionAmount);
                    newRetentionLine.setMonto(retentionAmount);
                    newRetentionLine.setMontoTotalLinea(retentionAmount);
                    newRetentionLine.setSubTotal(retentionAmount);
                    
                    invoice.setRetentions(newRetentionLine);                  
                }
            }
            else {
                break;
            }
        }
        
        if (calculateTaxes) {
            line.setMontoDescuento(line.getMonto().subtract(line.getSubTotal()));
            line.setNaturalezaDescuento(invoice.getPaymentType());
        }
        
        line.setdImpuesto(taxesList);      
    }
    
    private String getTaxCode(String taxTitle) {
        String result = "";
        
        for(Taxes tax : ERPconfiguration.linetaxes) {
            if (tax.title.contains(taxTitle)) {
                result = tax.code;
                break;
            } 
        }
        
        return result;
    }
    
    private Boolean isInRetentions(String retentionTitle) {
        Boolean result = false;
        
        if (retentionTitle == null) {
            return result;
        }
        
        for(Retentions retention : ERPconfiguration.lineRetentions) {
            if (retention.title.contains(retentionTitle)) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    public Boolean isAPIAvailable() {
        Boolean result = true;
        List<String> users = postData("&table=users");
        
        if (users.size() == 0) {
            result = false;
        }
        
        return result;
    }
    
     public List<String> postData(String postData) {
         return postData(postData, false);
     }
    
    //TODO: Use maps or different estructure, instead of List<Sring>
    public List<String> postData(String postData, Boolean printResult) //TODO: Manage when API is not available
    {
        System.out.println("ControladorERPConector postData");
        BufferedReader reply;
        List<String> result = new ArrayList<String>();
        OutputStream os;
        
        try
        {
            URL obj = new URL(api_url);
            HttpURLConnection request = (HttpURLConnection) obj.openConnection();
            
            request.setRequestMethod("POST");
            request.setRequestProperty("User-Agent", USER_AGENT);
            
            request.setDoOutput(true);
            os = request.getOutputStream();
            
            String postvars = "dataname=" + api_dataname + "&key=" + api_key + "&token=" + api_token;
            postvars += postData;
            os.write(postvars.getBytes());
            os.flush();
            os.close(); //TODO: Do we close on Finally block
            
            int responseCode = request.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                reply = new BufferedReader(new InputStreamReader(request.getInputStream()));
                String replyLine;

                while ((replyLine = reply.readLine()) != null) {
                    if (printResult) {
                        System.out.println(replyLine);
                    }
                    
                    result.add(replyLine);
                }
                
                reply.close();
            } else {
                System.out.println("POST request not worked");
            }
        }
        catch(Exception ex){
            Logger.getLogger(ControladorERPConector.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            return result;
        }
    }
}
