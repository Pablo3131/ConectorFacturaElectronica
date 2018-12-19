/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Lightspeed;

import Lightspeed.DTO.CustomerInfo;
import Lightspeed.DTO.InvoiceLineItem;
import Lightspeed.DTO.InvoiceReply;
import Lightspeed.DTO.InvoiceTotals;
import Lightspeed.DTO.paymentMethod;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Luis Arce
 */
public class ERPConector {
     
    static java.net.CookieManager msCookieManager;
    
    public InvoiceReply getInvoice(int invoiceID) {
        System.out.println("Enter getInvoice");
        InvoiceReply invoice = null;
        
        String url = "https://iMac-de-Sol:9630/api/invoices/" + invoiceID + "/";
        HttpsURLConnection connRequest = setConnection(url, "GET");
        
        try {
            int responseCode = connRequest.getResponseCode();
            System.out.println("Response Code : " + responseCode);
            
            if (responseCode != 200) {
                throw new Exception("LightSpeed bad response Code: " + responseCode);
            }
            
            invoice = processLightSpeedReply(connRequest);            
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        finally {
            connRequest.disconnect();
            connRequest = null;
            return invoice;
        }
    }
    
    public paymentMethod getPaymentMethod(int invoiceID, int paymentID) {
        System.out.println("Enter getPaymentMethod");
        paymentMethod payment = null;
        
        String url = "https://iMac-de-Sol:9630/api/invoices/" + invoiceID + "/payments/" + paymentID + "/"; //TODO: Function to build URL
        HttpsURLConnection connRequest = setConnection(url, "GET");
        
        try {
            int responseCode = connRequest.getResponseCode();
            System.out.println("Response Code : " + responseCode);
            
            if (responseCode != 200) {
                throw new Exception("LightSpeed bad response Code: " + responseCode);
            }
            
            payment = processLightSpeedReplyPaymentMethod(connRequest);            
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        finally {
            connRequest.disconnect();
            connRequest = null;
            return payment;
        }
    }
    
    public InvoiceLineItem getInvoiceLine(int invoiceID, int invoiceLineID) {
        System.out.println("Enter getInvoiceLine");
        InvoiceLineItem invoiceLine = null;
        
        String url = "https://iMac-de-Sol:9630/api/invoices/" + invoiceID + "/lineitems/" + invoiceLineID + "/"; //TODO: Function to build URL
        HttpsURLConnection connRequest = setConnection(url, "GET");
        
        try {
            int responseCode = connRequest.getResponseCode();
            System.out.println("Response Code : " + responseCode);
            
            if (responseCode != 200) {
                throw new Exception("LightSpeed bad response Code: " + responseCode);
            }
            
            invoiceLine = processLightSpeedReplyLineItem(connRequest);            
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        finally {
            connRequest.disconnect();
            connRequest = null;
            return invoiceLine;
        }
    }
    
    private InvoiceLineItem processLightSpeedReplyLineItem(HttpsURLConnection connection) throws Exception {
        
        /*Testing*/
        String COOKIES_HEADER = "Set-Cookie";       
        /*Testing*/

        
        
        BufferedReader inputStream = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        
        
        /*Testing*/
        if (msCookieManager == null) {
            msCookieManager = new java.net.CookieManager();
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    msCookieManager.getCookieStore().add(null,HttpCookie.parse(cookie).get(0));
                }               
            }
        }
        
        /*Testing*/
        
        
        //Convert XML reply to Object
        JAXBContext jaxbContext = JAXBContext.newInstance( InvoiceLineItem.class );
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        InvoiceLineItem invoiceLine = (InvoiceLineItem)jaxbUnmarshaller.unmarshal(inputStream);
        inputStream.close();
        
        return invoiceLine;
    }
    
    
    private InvoiceReply processLightSpeedReply(HttpsURLConnection connection) throws Exception {
        BufferedReader inputStream = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        
        //Convert XML reply to Object
        JAXBContext jaxbContext = JAXBContext.newInstance( InvoiceReply.class );
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        InvoiceReply invoice = (InvoiceReply)jaxbUnmarshaller.unmarshal(inputStream);
        inputStream.close();
        
        return invoice;
    }
    
    private paymentMethod processLightSpeedReplyPaymentMethod(HttpsURLConnection connection) throws Exception {
        BufferedReader inputStream = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        
        //Convert XML reply to Object
        JAXBContext jaxbContext = JAXBContext.newInstance( paymentMethod.class );
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        paymentMethod payment = (paymentMethod)jaxbUnmarshaller.unmarshal(inputStream);
        inputStream.close();
        
        return payment;
    }
    
    private HttpsURLConnection setConnection(String url, String method) {
        String USER_AGENT = "com.lightspeed.onsite.demo/1.0"; //TODO: Get this information from Configuration
        HttpsURLConnection connection = null;     
        String X_PAPPID = "12345678-90ab-cdef-1234-567890abcdef";
        System.out.println("X_PAPPID: " + X_PAPPID);
        
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            } };
            
            // Install the all-trusting trust manager
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
                     
            URL obj = new URL(url);
            connection = (HttpsURLConnection) obj.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("X-PAPPID", X_PAPPID); //TODO: Get this information from Configuration
            
            String userPassword = "lightspeed:admin";
            String encodingAuth = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodingAuth); //TODO: Get this information from Configuration

            connection.setSSLSocketFactory(sc.getSocketFactory());

            connection.setHostnameVerifier(new HostnameVerifier()
            {      
                public boolean verify(String hostname, SSLSession session)
                {
                    return true;
                }
            });
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        finally {
            /* Testing */
            if (msCookieManager != null) {
                if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                    // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                    connection.setRequestProperty("Cookie",
                    String.join(";", cookiesToStrArray(msCookieManager.getCookieStore().getCookies())));    
                }
            }

            /* Testing */
            
            return connection;
        }
    }
    
    private String[] cookiesToStrArray(List<HttpCookie> cookies) {
        String[] result = new String[cookies.size()];
        
        for(int index = 0; index < cookies.size(); index++) {
            result[index] = cookies.get(index).toString();
        }
        
        return result;
    }
    
}


