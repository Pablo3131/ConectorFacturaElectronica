/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lightspeed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import servidor1.ControladorDB;

public class ParserConfiguration {
    public String openCashDrawerToken = "7y";
    public List<String> orderIDTokens = new ArrayList<String>();
    public int skipInitialChars = 0;
    public int moveLinesToRight = 0;
    //private Map<String, String> paymentMethods = new HashMap<String, String>();
    //private Map<String, String> parserKeyWords = new HashMap<String, String>(); //TODO: Implemente the part with all the others strings
    //private BigDecimal taxPercentage = new BigDecimal("0.13");
    //private String mode = "TEST"; //TEST - PRODUCTION
    
    //private String laguague = "English"; // Spanish is second option - get it from config file
    
    public ParserConfiguration(Map<String, String> configurations) {
        skipInitialChars =  configurations.get("skipInitialChars") == null ? 0 : 
                            Integer.parseInt(configurations.get("skipInitialChars"));
        moveLinesToRight = configurations.get("moveLinesToRight") == null ? 0 : 
                            Integer.parseInt(configurations.get("moveLinesToRight"));
        openCashDrawerToken = configurations.get("openCashDrawerToken");
        setOrderIDTokens(configurations);
    }
    
    private void setOrderIDTokens(Map<String, String> configurations) {
        String[] tokens = configurations.get("orderIDTokens").split(",");
        
        for(String token : tokens) {
            orderIDTokens.add(token);
        } 
    }
    
//    private void buildParserKeyWords() {
//        parserKeyWords.put("DISCOUNTLINE", "off");
//        parserKeyWords.put("COUPON", "Coupon");
//        parserKeyWords.put("TAXESLINE", "IVA");
//        parserKeyWords.put("TOTALLINE", "Total");
//        parserKeyWords.put("SUBTOTALLINE", "Subtotal");
//        parserKeyWords.put("OPENCASHDRAWER", "7y");
//        
//        //TODO: Get it from a file
//        if (laguague.equals("English")) { 
//            parserKeyWords.put("ORDERIDLINE", "Order");
//        }
//        else if (laguague.equals("Spanish")) {
//            parserKeyWords.put("ORDERIDLINE", "Factura");
//            //parserKeyWords.put("ORDERIDLINE", "Orden");
//        }  
//    }
//    
//    private void buildPayMethods() {
//        if (laguague.equals("English")) {
//            paymentMethods.put("Cash", "01");
//            paymentMethods.put("Card", "02"); //TODO; Check if parser works fine with this option
//        }
//        else if (laguague.equals("Spanish")) {
//            paymentMethods.put("efectivo", "01");
//            paymentMethods.put("tarjeta", "02");
//        }    
//    }
//    
//    public BigDecimal getTaxPercentage() {
//        return taxPercentage;
//    }
//    
//    public void setTaxPercentage(BigDecimal taxPercentage) {
//        this.taxPercentage = taxPercentage;
//    }
//    
//    public Map<String, String> getPaymentMethods() {
//        if (paymentMethods.isEmpty()) {
//            buildPayMethods();
//        }
//        
//        return paymentMethods;
//    }
//    
//    public Map<String, String> getParserKeyWords() {
//        if (parserKeyWords.isEmpty()) {
//            buildParserKeyWords();
//        }
//        
//        return parserKeyWords;
//    }
//    
//    public void setLanguage(String lang) {
//        laguague = lang;
//    }
//    
//    public String getMode() {
//        if (mode.isEmpty()) {
//            mode = "TEST";
//        }
//        
//        return mode;
//    }
//    
//    public void setMode(String newMode) {
//        mode = newMode;
//    }
}
