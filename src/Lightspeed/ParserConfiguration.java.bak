/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lavu;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ParserConfiguration {
    private Map<String, String> paymentMethods = new HashMap<String, String>();
    private Map<String, String> parserKeyWords = new HashMap<String, String>(); //TODO: Implemente the part with all the others strings
    private BigDecimal taxPercentage = new BigDecimal("0.13");
    
    private String laguague = "English"; // Spanish is second option - get it from config file
    
    public ParserConfiguration(String lang) {
        laguague = lang;
        buildPayMethods();
        buildParserKeyWords();
    }
    
    private void buildParserKeyWords() {
        parserKeyWords.put("DISCOUNTLINE", "off");
        parserKeyWords.put("COUPON", "Coupon");
        parserKeyWords.put("TAXESLINE", "IVA");
        parserKeyWords.put("TOTALLINE", "Total");
        parserKeyWords.put("SUBTOTALLINE", "Subtotal");
        
        //TODO: Get it from a file
        if (laguague.equals("English")) { 
            parserKeyWords.put("ORDERIDLINE", "Order");
        }
        else if (laguague.equals("Spanish")) {
            parserKeyWords.put("ORDERIDLINE", "Orden");
        }    
    }
    
    private void buildPayMethods() {
        if (laguague.equals("English")) {
            paymentMethods.put("efectivo", "01"); //TODO: Put English version
            paymentMethods.put("tarjeta", "02");
        }
        else if (laguague.equals("Spanish")) {
            paymentMethods.put("efectivo", "01");
            paymentMethods.put("tarjeta", "02");
        }    
    }
    
    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }
    
    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }
    
    public Map<String, String> getPaymentMethods() {
        if (paymentMethods.isEmpty()) {
            buildPayMethods();
        }
        
        return paymentMethods;
    }
    
    public Map<String, String> getParserKeyWords() {
        if (parserKeyWords.isEmpty()) {
            buildParserKeyWords();
        }
        
        return parserKeyWords;
    }
    
    public void setLanguage(String lang) {
        laguague = lang;
    }
}
