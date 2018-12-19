package servidor1;

import java.util.concurrent.TimeUnit;
import Entidades.DatosEmpresa;
import Entidades.DetalleFactura;
import Entidades.Estado;
import Entidades.Factura;
import Entidades.Filter;
import Entidades.FilterList;
import FacturaElectronica.GuruSoft.ControladorFacturaElectronica;
import FacturaElectronica.GuruSoft.ControladorNotaCredito;
import Impresoras.Epson.ControladorImpresion;
import Impresoras.Epson.FilesController;
import Lightspeed.ControladorERPConector;
import Lightspeed.ControladorParser;
import WSTiqueteElectronico.ArrayOfClsDetalleServicio;
import WSTiqueteElectronico.ArrayOfOtros;
import WSTiqueteElectronico.ClsDetalleServicio;
import WSTiqueteElectronico.ClsEmisor;
import WSTiqueteElectronico.ClsOtros;
import WSTiqueteElectronico.ClsReceptor;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
/**
 * @author vmora
 */
public class Servidor {  
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {          

//******************************************************//
//******************************************************//
//******************************************************//
//****************PRODUCTION****************************//
//******************************************************//
//******************************************************//
////******************************************************//
      
        ControladorDB cBD = new ControladorDB();
        DatosEmpresa datosEmpresa = cBD.ObtenerDatosEmpresa();
        if(!datosEmpresa.getMensajeError().equals("NA"))
            System.out.println("Datos de la empresa consultados correctamente.");
                        
        //Hilo de ejecucion para enviar facturas electrónicas a guru.
        ControladorFacturaElectronica cFacturaElectronica = new ControladorFacturaElectronica(datosEmpresa);
        cFacturaElectronica.start();

        //Se abre el puerto para impresiones entrantes:
        ControladorParser lightSpeedParser = new ControladorParser();
        ControladorSocket socket = new ControladorSocket(lightSpeedParser);
        socket.start();

//        //Hilo de ejecucion para enviar notas de credito a guru.
//        ControladorNotaCredito cNotaCredito = new ControladorNotaCredito(datosEmpresa);
//        cNotaCredito.start();         
        
        
//******************************************************//
//******************************************************//
//******************************************************//
//****************TESTING*******************************//
//******************************************************//
//******************************************************//
//******************************************************//
//        ControladorDB cBD = new ControladorDB();
//        List<Factura> pendingInvoices = cBD.BuscarFacturas(Estado.CREADA);
//
//        FilesController fController = new FilesController();
//        
//        for(Factura invoice: pendingInvoices) {
//            System.out.println();
//            System.out.println();
//            List<String> result = fController.buildLinesToPrintFromInvoice(invoice);
//            
//            for(String line: result) {
//                System.out.println(line);
//            }
//            
//            System.out.println();
//            System.out.println();
//        }

//ControladorImpresion impresion = new ControladorImpresion();
////FilesController file = new FilesController();
//
//impresion.sendFileToPrinter("12_Invoice.pdf");
//
////ControladorImpresion impresion = new ControladorImpresion();
////List<String> toPrint = new ArrayList<String>();
////toPrint.add("Line1");
////toPrint.add("Line2");
////toPrint.add("Line3");
////toPrint.add("Line4");
////toPrint.add("Line5");
////impresion.printToKitchen(toPrint);


//        ControladorERPConector lavuConector = new ControladorERPConector();
//        List<String> taxProfiles = lavuConector.postData("&table=tax_profiles");
//        taxProfiles = lavuConector.postData("&table=tax_rates");
        
////////
//        ControladorERPConector lavuConector = new ControladorERPConector();
//        ControladorParser lavuParser = new ControladorParser();       
////////////////////
////        List<String> currentInvoice = lavuConector.postData("&table=order_payments&column=order_id&value=1011-452", true); //<//&column=closed&value_min=2018-12-09 06:01:26:847&value_max=2018-12-09 12:01:26:847", false);
//        //Factura invoice = new Factura();1003-13243
//        
////        for(String invoiceStr : currentInvoice) {
////            if (invoiceStr.contains("order_id")) {
////                System.out.println(invoiceStr);
//                Factura invoice = lavuConector.getInvoiceInformation("1011-537");//lavuConector.getData(invoiceStr, "order_id"));
//                lavuParser.checkInvoiceStatus(invoice);
                
//            try        
//            {
//                TimeUnit.SECONDS.sleep(20);
//            } 
//            catch(InterruptedException ex) 
//            {
//                Thread.currentThread().interrupt();
//            }
//            }
//        }

            
        
//          
//          currentInvoice = lavuConector.postData("&table=tax_rates", true);
//            System.out.println(invoiceLine);
//
//             Factura invoice = lavuConector.getInvoiceInformation("1003-13019");
//             lavuParser.checkInvoiceStatus(invoice);
//             try        
//            {
//                TimeUnit.SECONDS.sleep(1);
//            } 
//            catch(InterruptedException ex) 
//            {
//                Thread.currentThread().interrupt();
//            }
//             }
//         } //        List<String> currentInvoice = lavuConector.postData("&table=discount_types", true);
//////////////        
////////////         List<String> currentInvoice = lavuConector.postData("&table=orders&column=closed&value_min=2018-12-03 00:06:00&value_max=2018-12-03 23:59:00");
////////////         for (String invoiceLine : currentInvoice) {
////////////             if (invoiceLine.contains("order_id")) {
/////////
         
//         Collections.sort(IDs);
//         
//         System.out.println("");
//         System.out.println("IDs Sorted: ");
//         for(Integer ID : IDs) {
//             System.out.println(ID);
//         }
         
//        //for(int initialInvoice = 10595; initialInvoice <= 10765; initialInvoice++) {
//            List<String> currentInvoice = lavuConector.postData("&table=orders&column=order_id&value=8-10735");
//            Factura invoice = new Factura();
//            for(int index = 0; index < currentInvoice.size(); index++) {
//                if (currentInvoice.get(index).contains("order_id")) {
//                    invoice = lavuConector.getInvoiceInformation(lavuConector.getData(currentInvoice.get(index), "order_id"));
//                }
//                if (currentInvoice.get(index).contains("original_id")) {
//                    lavuConector.setCustomerInfo(invoice, lavuConector.getData(currentInvoice.get(index), "original_id"));
//                }
//            }
//            //invoice.setIdReceptor("113760395");
//            lavuParser.checkInvoiceStatus(invoice);
//            
//            try        
//            {
//                TimeUnit.SECONDS.sleep(25);
//            } 
//            catch(InterruptedException ex) 
//            {
//                Thread.currentThread().interrupt();
//            }
        //}
                

    }
}
