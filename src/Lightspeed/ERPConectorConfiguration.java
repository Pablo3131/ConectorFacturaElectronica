/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lightspeed;

import Entidades.DetalleImpuesto;
import Entidades.Retentions;
import Entidades.Taxes;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.css.StyleOrigin.USER_AGENT;
import servidor1.ControladorDB;

/**
 *
 * @author luis.arce
 */
public class ERPConectorConfiguration {
    private int taxesRatesTableRowSize = 0;
    public List<Taxes> linetaxes = new ArrayList<Taxes>();
    public List<Retentions> lineRetentions = new ArrayList<Retentions>();
    private String taxesProfile = "";
    private static final String USER_AGENT = "Mozilla/5.0";
    public Map<String, String> apiInformation = new HashMap<String, String>();
    public Map<String, String> customerInformation = new HashMap<String, String>();
    private String api_url = "";
    private String api_dataname = "";
    private String api_key = "";
    private String api_token = "";
    public String context = "test"; //Always default test context - production is the other option
    
    public ERPConectorConfiguration(Map<String, String> configuration) {       
        setTaxesFromConfiguration(configuration);
        
        setRetentionsFromCOnfiguration(configuration);
        
        taxesRatesTableRowSize =  Integer.parseInt(configuration.get("taxesRatesTableRowSize"));
        
        getAPIInformation(configuration);
        
        getConfigurationFromERP();
        
        getCustomerCustomInformation(configuration);
        
        context = configuration.get("context");
    }
    
    private void setTaxesFromConfiguration(Map<String, String> configuration) {
        String[] taxesTitles = configuration.get("taxesTitle").split(",");
        String[] taxesCodes = configuration.get("taxesCode").split(",");
        taxesProfile = configuration.get("taxesProfile"); //"CR Taxes";
        
        for (int index = 0; index < taxesTitles.length; index++) {
            Taxes tax = new Taxes();
            tax.code = taxesCodes[index].substring(1); //Skip first char - there is a SQL Lite limitation
            tax.title = taxesTitles[index];      
            linetaxes.add(tax);
        }
    }
    
    private void setRetentionsFromCOnfiguration(Map<String, String> configuration) {
        String[] retentionsTitle = configuration.get("retentionsTitle").split(",");
        
        for (String retentionTitle : retentionsTitle) {
            Retentions ret = new Retentions();
            ret.title = retentionTitle;
            lineRetentions.add(ret);
        }
    }
    
    private void getCustomerCustomInformation(Map<String, String> configuration) { //TODO: Get this from DB or Config file - EVERYTHING TO LOWER CASE, and without comas or other similar
        customerInformation.put("IDField", configuration.get("IDField"));
        customerInformation.put("LegalNumber", configuration.get("LegalNumber"));
        customerInformation.put("IDNumber", configuration.get("IDNumber"));
    }
    
    private void getAPIInformation(Map<String, String> configuration) { //TODO: Get this from DB or Config file
//        apiInformation.put("URL", "https://admin.poslavu.com/cp/reqserv/"); // SIBU
//        apiInformation.put("DATANAME", "sibu_heredia");
//        apiInformation.put("KEY", "3llUH1S83wUv9KqL9Y4v");
//        apiInformation.put("TOKEN", "rCqvCphilMy3erxWm5lM");
//        
//        apiInformation.put("URL", "https://admin.poslavu.com/cp/reqserv/"); // DARIO
//        apiInformation.put("DATANAME", "ag_tech");
//        apiInformation.put("KEY", "ATlLPspZg3XqMnBzfGBM");
//        apiInformation.put("TOKEN", "AIGKcr5ChOZmQlo0o2Mh");
        
//        apiInformation.put("URL", "https://admin.poslavu.com/cp/reqserv/"); // SELINA SJ
//        apiInformation.put("DATANAME", "selina_san_jos");
//        apiInformation.put("KEY", "FppEv4euwQE4vMxGDMiY");
//        apiInformation.put("TOKEN", "8U8sLrPRKEHwVhILlaKG");

        apiInformation.put("URL", configuration.get("URL")); // SELINA SJ
        apiInformation.put("DATANAME", configuration.get("DATANAME"));
        apiInformation.put("KEY", configuration.get("KEY"));
        apiInformation.put("TOKEN", configuration.get("TOKEN"));
        
        api_url = apiInformation.get("URL");
        api_dataname = apiInformation.get("DATANAME");
        api_key = apiInformation.get("KEY");
        api_token = apiInformation.get("TOKEN");     
    }
    
    private void getConfigurationFromERP() {
        Taxes CurrentTax = null;
        Retentions CurrentRetention = null;
        BigDecimal currentRate = null;
        String taxesProfileID = getTaxesProfileID();
        
        List<String> taxes = postData("&table=tax_rates&column=profile_id&value=" + taxesProfileID);
        int taxesRowsNum = taxes.size() / taxesRatesTableRowSize;
        
        // Configure Taxes and Retentions rates
        for (int index = 0; index < taxesRowsNum; index++) {
            int currentTaxIndex = index * taxesRatesTableRowSize;
            List<String> taxeColumnValues = taxes.subList(currentTaxIndex, currentTaxIndex + taxesRatesTableRowSize);
            
            for(String columnValue : taxeColumnValues) {
                if (columnValue.contains("<title>")) {
                    String title = getData(columnValue, "title");
                    CurrentTax = geTaxbyTitle(title);

                    if (CurrentTax == null) {
                        CurrentRetention = geRetentionbyTitle(title);
                    }                       
                }
                else if (columnValue.contains("<calc>")){
                    currentRate = getDataBigDecimal(columnValue, "calc");
                }
            }
            
            if (currentRate != null) {
                if (CurrentTax != null) {
                    CurrentTax.rate = currentRate;
                }
                else if(CurrentRetention != null) {
                    CurrentRetention.rate = currentRate;
                }
                
                CurrentTax = null;
                CurrentRetention = null;
                currentRate = null;
            }            
        }
        
        
    }
    
    private String getData(String rowData, String fieldName) { //TODO: Put this in a common class
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
    
    private String getTaxesProfileID() {
        String result = "";
        
        List<String> taxesProfiles = postData("&table=tax_profiles&column=title&value=" + taxesProfile);
        
        for(String columnValue : taxesProfiles) {
            if (columnValue.contains("<id>")) {
                result = getData(columnValue, "id");
                break;
            }
        }
        
        return result;
    }
    
    private Retentions geRetentionbyTitle(String title) {
        Retentions result = null;
        
        for(Retentions retention : lineRetentions) {
            if (retention.title.equals(title)) {
                result = retention;
                break;
            }
        }
        
        return result;
    }
    
    private Taxes geTaxbyTitle(String title) {
        Taxes result = null;
        
        for(Taxes tax : linetaxes) {
            if (tax.title.equals(title)) {
                result = tax;
                break;
            }
        }
        
        return result;
    }
    
    private List<String> postData(String postData) // TODO: Put this code in a single class - avoid duplicate
    {
        
        System.out.println("ERPConectorConfiguration postData");
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
                    //System.out.println(replyLine);
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
