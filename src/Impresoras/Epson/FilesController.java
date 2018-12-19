/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Impresoras.Epson;

import Entidades.DetalleFactura;
import Entidades.Factura;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import servidor1.ConfigurationController;
import servidor1.ControladorDB;

/**
 *
 * @author luis.arce
 */
public class FilesController {
    private ControladorDB cDB = new ControladorDB();
    
    private PrintingConfiguration config = ConfigurationController.getInstance().printing;
    
    public String buildFile(String invoiceSeq, List<String> lines) { //TODO: Get configurations/path from configuration file
        Font paragraphFont = FontFactory.getFont(FontFactory.COURIER, config.fontSize, Font.BOLD); //TODO: Add Fonts to configuration DB
        String fileName = invoiceSeq + "_Invoice.pdf"; //TODO: Generate this file name
        List<String> linesToPrint =  new ArrayList<String>();
        
        if (lines != null) {
            linesToPrint = lines;
        }
        else {
            linesToPrint = cDB.getLineasPorImprimir(invoiceSeq);
        }
        
        //Ejemplo de creación de pdf:
        try
        {                    
            
            // I define a width of 200pt
            //float width = 226; sibu
            float width = config.width;
            // I define the height as 10000pt (which is much more than I'll ever need)
            float max = 10000;
            
            //int cRow = 10;
            
            // I create a column without a `writer` (strange, but it works)
            ColumnText ct = new ColumnText(null);
            ct.setSimpleColumn(new Rectangle(width, max));
            Image foto1 = Image.getInstance(config.logoPath);
            foto1.scaleToFit(config.logoX, config.logoY);
            foto1.setAlignment(Chunk.ALIGN_MIDDLE);  
            ct.addElement(foto1);
            for (int i = 0; i < linesToPrint.size(); i++) {
                if(linesToPrint.get(i).contains("----------------")) {
                    LineSeparator dottedline = new LineSeparator();
                    dottedline.setOffset(-3); //TODO: This should get from configuration file
                    ct.addElement(dottedline);
                }
                else {
                    ct.addElement(new Paragraph(linesToPrint.get(i), paragraphFont));
                }
            }
            // I add content in simulation mode
            ct.go(true);
            // Now I ask the column for its Y position
            float y = ct.getYLine();    
            //Esto primero fue una simulación para saber el tamaño.
            
            Rectangle pagesize = new Rectangle(width, 50 + (max - y));
            //Rectangle pagesize = new Rectangle(164.41f, 14400);
            //Se crea el documento
            Document documento = new Document(pagesize, 5, 5, 5, 5);            
            // Se crea el OutputStream para el fichero donde queremos dejar el pdf.
            FileOutputStream ficheroPdf = new FileOutputStream("/opt/PrintServer/files/" + fileName);
            //FileOutputStream ficheroPdf = new FileOutputStream("C:\\Imagen\\fichero.pdf");
            // Se asocia el documento al OutputStream y se indica que el espaciado entre
            // lineas sera de 20. Esta llamada debe hacerse antes de abrir el documento
            PdfWriter.getInstance(documento,ficheroPdf).setInitialLeading(5);

            // Se abre el documento.
            documento.open();   
                    
            Image foto = Image.getInstance(config.logoPath);
            //Image foto = Image.getInstance("/opt/PruebaImpresion/logosibu.png");
            foto.scaleToFit(config.logoX, config.logoY);
            foto.setAlignment(Chunk.ALIGN_MIDDLE);
            documento.add(foto);
            
            for (int i = 0; i < linesToPrint.size(); i++) {
                if(linesToPrint.get(i).contains("----------------")) {
                    LineSeparator dottedline = new LineSeparator();
                    dottedline.setOffset(-3); //TODO: This should get from configuration file
                    documento.add(dottedline);
                }
                else {
                    documento.add(new Paragraph(linesToPrint.get(i), paragraphFont));
                }
//                if(linesToPrint.get(i).equals("Linea")){
//                    LineSeparator dottedline = new LineSeparator();
//                    dottedline.setOffset(-3);
//                    //dottedline.setGap(2f);
//                    documento.add(dottedline);
//                }else{
                    //documento.add(new Paragraph(lineas[i], paragraphFont));
//                }
            }
            
            documento.close();
        }
        catch ( Exception e )
        {
                e.printStackTrace();
        }    
               
        return fileName;
    }
    
    public List<String> buildLinesToPrintFromInvoice(Factura invoice) {
        List<String> result = new ArrayList<String>();
        int lineLenght = 40; //TODO: Get this values from Configuration
        int marginLeft = 4;
        int amountSpaces = 10;
        int descriptionSpaces = 26; //36 Sol Nicoyano
        
        result.addAll(buildInvoiceHeaderStart(invoice, marginLeft, lineLenght));
        result.addAll(buildInvoiceLinesStr(invoice.getDetalleFactura(), marginLeft, descriptionSpaces, amountSpaces, lineLenght)); //TODO: Get this values from Configuration
        result.addAll(buildTotalsLinesStr(invoice, marginLeft, lineLenght));
        result.add(getEmptySpaces(lineLenght));
        result.addAll(buildStoreATMInformation(marginLeft, lineLenght));
        //TODO: Add Hacienda Autorization line
        
        return result;
    }
    
    private List<String> buildInvoiceHeaderStart(Factura invoice, int marginLeft, int lineTotalSpaces) {
        List<String> result = new ArrayList();
        List<String> linesStr = new ArrayList<String>();
        Date todayDate = new Date();
        linesStr.add("Tienda y Zapateria Sol Nicoyano"); //TODO: Get this information from configuration
        linesStr.add("Costado Norte del Mercado Municipal");
        linesStr.add("Nicoya Guanacaste");
        linesStr.add("Carlos Kokway Sanchez W.");
        linesStr.add("Ced: 1-1178-0291-24");
        linesStr.add("");
        linesStr.add(invoice.getIdOrden());
        linesStr.add("");
        linesStr.add(todayDate.toLocaleString());
        linesStr.add("");
        
        for(String lineStr : linesStr) {
            result.add(getEmptySpaces(marginLeft) + 
                       getJustifyCenterSpaces(lineStr, lineTotalSpaces) +
                       lineStr + 
                       getJustifyCenterSpaces(lineStr, lineTotalSpaces));
        }
        
        result.add("Clave: " + invoice.getClaveComprobante());
        result.add("Consecutivo: " + invoice.getNumeroConsecutivo());
        result.add(getEmptySpaces(lineTotalSpaces));
        
        return result;
    }
    
    private List<String> buildTotalsLinesStr(Factura invoice, int marginLeft, int lineTotalSpaces) {
        List<String> result = new ArrayList<String>();
        String marginLStr = getEmptySpaces(marginLeft);
        
        int subTotalEmptySpaces = calculateTotalEmptySpaces(lineTotalSpaces, 
                                                            marginLeft, 
                                                            "Subtotal", 
                                                            invoice.getTotalVentaNeta());
        result.add(marginLStr + 
                   "Subtotal" + 
                   getEmptySpaces(subTotalEmptySpaces) + 
                   invoice.getTotalVentaNeta().toPlainString());
        
        
        int IVEmptySpaces = calculateTotalEmptySpaces(lineTotalSpaces,
                                                      marginLeft,     
                                                      "IV",
                                                      invoice.getTotalImpuesto());   
        result.add(marginLStr + 
                   "IV" + 
                   getEmptySpaces(IVEmptySpaces) + 
                   invoice.getTotalImpuesto().toPlainString());
        
        
        int TotalEmptySpaces = calculateTotalEmptySpaces(lineTotalSpaces,
                                                      marginLeft,     
                                                      "Total",
                                                      invoice.getTotalComprante());
        result.add(marginLStr + 
                   "Total" + 
                   getEmptySpaces(TotalEmptySpaces) + 
                   invoice.getTotalComprante().toPlainString());
        
        
        int PagoEmptySpaces = calculateTotalEmptySpaces(lineTotalSpaces,
                                                      marginLeft,     
                                                      "Pago",
                                                      invoice.getTotalComprante());   
        result.add(marginLStr + 
                   "Pago" + 
                   getEmptySpaces(PagoEmptySpaces) + 
                   invoice.getTotalComprante().toPlainString());
        
        int SaldoEmptySpaces = calculateTotalEmptySpaces(lineTotalSpaces,
                                                      marginLeft,     
                                                      "Saldo",
                                                      BigDecimal.ZERO);  
        result.add(marginLStr + 
                   "Saldo" + 
                   getEmptySpaces(SaldoEmptySpaces) + 
                   BigDecimal.ZERO.toPlainString());
       
        
        return result;
    }
    
    private List<String> buildInvoiceLinesStr(List<DetalleFactura> lines, int lineQSpaces,
                                              int lineDescrSpaces, int lineAmountSpaces, int lineTotalSpaces) {
        List<String> result = new ArrayList<String>();
        
        for(DetalleFactura line : lines) {
            String lineStr = "";
            String extraLineStr = "";
            
            int quantityEmptySpaces = lineQSpaces - String.valueOf(line.getCantidad()).length();
            lineStr = lineStr + line.getCantidad() + getEmptySpaces(quantityEmptySpaces);
            
            int descriptionEmptySpaces = lineDescrSpaces - line.getDescripcion().length();
            if (descriptionEmptySpaces < 0) {
                String firtsLineStr = line.getDescripcion().substring(0, lineDescrSpaces);
                String secondLineStr = line.getDescripcion().substring(lineDescrSpaces, line.getDescripcion().length());
                lineStr = lineStr + firtsLineStr;
                extraLineStr = getEmptySpaces(lineQSpaces) + 
                               secondLineStr +
                               getEmptySpaces(lineDescrSpaces - secondLineStr.length()) + 
                               getEmptySpaces(lineAmountSpaces);
            }
            else {
                lineStr = lineStr + line.getDescripcion() + getEmptySpaces(descriptionEmptySpaces);
            }
            
            String lineAmountStr = line.getMontoTotalLinea().toPlainString();                    
            lineStr = lineStr + getEmptySpaces(lineAmountSpaces - lineAmountStr.length()) + lineAmountStr;
            
            result.add(lineStr);
            
            if (!extraLineStr.equals("")) {
                result.add(extraLineStr);
            }        
        }
        
        return result;
    }
    
    private List<String> buildStoreATMInformation(int marginLeft, int lineTotalSpaces) 
    {
        List<String> result = new ArrayList<String>();
        List<String> linesStr = new ArrayList<String>();
        linesStr.add("Station: Raspberry"); //TODO: Get this information from configuration
        linesStr.add("Tienda Sol Nicoyano");
        linesStr.add("");
        linesStr.add("Precios con I.V.I.");
        linesStr.add("2685-6022");
        linesStr.add("solnicoyano@yahoo.com");
        
        for(String lineStr : linesStr) {
            result.add(getEmptySpaces(marginLeft) + 
                       getJustifyCenterSpaces(lineStr, lineTotalSpaces) +
                       lineStr + 
                       getJustifyCenterSpaces(lineStr, lineTotalSpaces));
        }
          
        return result;
    }
    
    private int calculateTotalEmptySpaces(int lineTotalSpaces, int marginLeft, String totalStr, BigDecimal total) {
        return lineTotalSpaces - marginLeft - totalStr.length() - total.toPlainString().length();
    }
 
    //private int calculateATMEmptySpaces(String totalStr, ){
    private String getJustifyCenterSpaces(String str, int lineTotalSpaces){
        return getEmptySpaces((lineTotalSpaces - str.length())/2);
    }
    
    private String getEmptySpaces(int count) {
        String result = "";

        for(int counter = 0; counter < count; counter++) {
            result = result + " ";
        }

        return result;
    }
}
