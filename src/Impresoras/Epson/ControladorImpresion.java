/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Impresoras.Epson;

import Entidades.Factura;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.PrintServiceLookup;
import servidor1.ConfigurationController;

/**
 *
 * @author vmora
 */
public class ControladorImpresion { //TODO: Use interfaces
     
    private FilesController fControler = new FilesController();
    private PrintingConfiguration config = ConfigurationController.getInstance().printing;
    
    public void sendLinesToPrinter(List<String> lines) {
        try {        
            Socket sock = new Socket(config.printerIP, config.printerPort); 
            PrintWriter oStream = new PrintWriter(sock.getOutputStream());
            
            for(String line : lines) {
                oStream.println(line);
            }
            
            //oStream.println(new char[]{29, 86, 49});
            //oStream.println(new char[]{29, 86, 66, 0}); //Corte desperdicio grande
            //oStream.println(new char[]{27, 109}); //Corte desperdicio pequeNo
            
            //oStream.print(new char[]{0x1D, 0x56, 0x41, 0x10}); // Cut the Paper
            
            oStream.close(); 
            sock.close();            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }  
    
    public void cashdrawerOpen() {
        try {        
            Socket sock = new Socket(config.printerIP, config.printerPort); 
            PrintWriter oStream = new PrintWriter(sock.getOutputStream()); 
            oStream.write(27);
            oStream.write(112);
            oStream.write(0);
            oStream.write(150);
            oStream.write(150);
            oStream.write(0);
            oStream.close(); 
            sock.close(); 
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }  
    
    public void sendFileToPrinter(String fileName) {
        
        try{
            PrintService pss[] = PrintServiceLookup.lookupPrintServices(null, null);
            if (pss.length == 0) {
                throw new RuntimeException("No printer services available.");
            }

            for(int i = 0; i < pss.length; i++){
                System.out.println("Printing to " + pss[i]);
            }
 //prueba
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
            PrintService ps = getPrinterServiceByName(pss, config.printerName); //TODO: Defensive code in case of there is no Printer pss[1]; //TODO: Use printer name instead of array
            System.out.println("Printing to " + ps);
            DocPrintJob job = ps.createPrintJob();
            FileInputStream fin = new FileInputStream("/opt/PrintServer/files/" + fileName);
            Doc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.PDF, null);
            job.print(doc, pras);
            fin.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }      
    }
    
    private PrintService getPrinterServiceByName(PrintService pss[], String name) {
        PrintService result = null;
        
        for(PrintService printer : pss) {
            if (printer.getName().equals(name)) {
                result = printer;
                break;
            }
        }
        
        return result;
    }
    
    //ORIGINAL CODE
    public void printInvoice(Factura invoice) throws UnknownHostException, IOException, InterruptedException {
            System.out.println("Enter printInvoice");
            String fileName = "";
            
            if (invoice.getInvoiceLinesToPrint().size() == 0) {
                invoice.setInvoiceLinesToPrint(fControler.buildLinesToPrintFromInvoice(invoice));
            }
            
            if (!config.printerName.equals("")) {
                if (invoice.getSecuencia() < 0) {
                    fileName = fControler.buildFile("temporal", invoice.getInvoiceLinesToPrint());
                }
                else {
                    fileName = fControler.buildFile(String.valueOf(invoice.getSecuencia()), invoice.getInvoiceLinesToPrint());
                }

                sendFileToPrinter(fileName);
            }
            else {
                sendLinesToPrinter(invoice.getInvoiceLinesToPrint());
            }       
    }
}
