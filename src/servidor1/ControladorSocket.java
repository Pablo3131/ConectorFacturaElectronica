/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor1;

import Lightspeed.ParserConfiguration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import interfaces.IParser;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vmora
 */
public class ControladorSocket implements Runnable{
    
    private final static int PORT = 9100; //TODO: Obtener este valor de un archivo de configuracion
    private final static int lineasMax = 1000;
    private Thread t;
    private IParser parser;
    private ParserConfiguration config = ConfigurationController.getInstance().parser;
   
    public ControladorSocket(IParser newParser) {
        parser = newParser;
    }
    
    private void AbrirSocket(){
        List<String> invoiceLines = new ArrayList<String>();
        String request = "";
        String lineaActual = "";
        
        try {                       
            ServerSocket serverSocket = new ServerSocket(PORT);
                
            Socket clientSocket;
            
            while(true){
                System.out.println("");
                System.out.println("");
                System.out.println("Servidor> Servidor iniciado");    
                System.out.println("Servidor> Esperando nuevos tiquetes...");
            
                clientSocket = serverSocket.accept();
                BufferedReader socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));              
                                              
                while ((lineaActual = socketReader.readLine()) != null) {
                    String lineToAdd = lineaActual;                                    
                    //System.out.println(lineToAdd);
                    invoiceLines.add(lineToAdd);
                    request += lineaActual;
                }
                
                parser.procesarFactura(request, invoiceLines);

                invoiceLines.clear();
                request = "";
                lineaActual = "";
                clientSocket.close();
            }    
        } catch (IOException ex) {
            System.err.println(ex.getMessage()); //TODO: Manejar bien los errores para que nunca se caiga realmente
        }        
    }

    @Override
    public void run() {
        AbrirSocket();
    }
    
    public void start (){
        System.out.println("Starting ControladorSocket");
        if (t == null)
        {
            t = new Thread (this);
            t.start ();
        }
    }
}
