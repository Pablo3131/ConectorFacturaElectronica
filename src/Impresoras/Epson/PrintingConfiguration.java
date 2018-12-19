/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Impresoras.Epson;

import java.util.Map;
import servidor1.ControladorDB;

/**
 *
 * @author Luis Arce
 */
public class PrintingConfiguration {
    public String printerIP = ""; // TODO: Sibu Default
    public int printerPort = -1; // Default printer Port
    public String logoPath = ""; // TODO: Sibu Default
    public int logoY = 250;
    public int logoX = 250;
    public String printerName = "";
    public int width = 226;
    public int fontSize = 9;
    
    public PrintingConfiguration(Map<String, String> configuration) {          
            printerIP = configuration.get("printerIP");
            printerPort =  Integer.parseInt(configuration.get("printerPort"));
            logoPath = configuration.get("logoPath");
            logoY = configuration.get("logoY") == null ? 100 : 
                        Integer.parseInt(configuration.get("logoY"));
            logoX = configuration.get("logoX") == null ? 100 : 
                        Integer.parseInt(configuration.get("logoX"));
            width = configuration.get("width") == null ? 226 :
                        Integer.parseInt(configuration.get("width"));
            fontSize = configuration.get("fontSize") == null ? 9 : 
                        Integer.parseInt(configuration.get("fontSize"));
            printerName = configuration.get("printerName") != null ? configuration.get("printerName") : "";
    }   
}
