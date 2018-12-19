/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FacturaElectronica.GuruSoft;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 *
 * @author Hugo
 */
public class EnviarNotaCreditoHandler implements SOAPHandler<SOAPMessageContext>{

    @Override
    public Set<QName> getHeaders() {
        return Collections.EMPTY_SET;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext messageContext) {
        try {
            SOAPMessage msg = messageContext.getMessage();

            boolean bolMsgSalida = (Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            // Obtenemos el contenedor del mensaje SOAP
            SOAPPart sp = msg.getSOAPPart();
            // A partir del contendor, obtenemos el nodo "Envelope"
            SOAPEnvelope env = sp.getEnvelope();
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            
            if (bolMsgSalida) {
                try {
                    //get the soap message and envelope
                    SOAPMessage soapMsg = messageContext.getMessage();
                    soapMsg.getSOAPPart().getEnvelope().setPrefix("s");
                    soapMsg.getSOAPBody().setPrefix("s");
                    soapMsg.getSOAPHeader().setPrefix("s");

                    soapMsg.getSOAPPart().getEnvelope().removeAttribute("xmlns:S");
                    soapMsg.getSOAPPart().getEnvelope().removeAttribute("xmlns:SOAP-ENV");
                    
                    Iterator itr = soapMsg.getSOAPBody().getChildElements();
                    
                    
                    SOAPElement soapElementoCabecera1 = soapFactory.createElement("EnviarNotaCredito").addAttribute(new QName("xmlns:i"), "http://www.w3.org/2001/XMLSchema-instance");
                    
                    while(itr.hasNext()) {
                       Object element = itr.next();
                       SOAPElement elementoSoap = (SOAPElement)element;
                       
                       Iterator itr2 = elementoSoap.getChildElements();
                       while(itr2.hasNext()) {
                        Object element2 = itr2.next();
                        SOAPElement elementoSoap2 = (SOAPElement)element2;    
                        soapElementoCabecera1.addChildElement(elementoSoap2);
                       }                    
                       
                        env.getBody().detachNode();
                        SOAPBody soapBody = env.addBody();
                        soapBody.addChildElement(soapElementoCabecera1);
                        //System.out.print(element + " ");
                    }
                    //System.out.println();                
                } catch (SOAPException e) {
                    e.printStackTrace();
                }
            }
            else{
                SOAPMessage soapMsg = messageContext.getMessage();
                Iterator itr = soapMsg.getSOAPBody().getChildElements();
                Object element = itr.next();
                SOAPElement elementoSoap = (SOAPElement)element;

                Iterator itr3 = elementoSoap.getChildElements();
                SOAPElement elementoSoap1 = (SOAPElement)itr3.next();

                SOAPElement elementoSoap2 = soapFactory.createElement("EnviarNotaCreditoResponse", "", "http://WSCreditoElectronico/");
                //elementoSoap.addAttribute(null, null)  
                
                elementoSoap2.addChildElement(elementoSoap1);
                env.getBody().detachNode();
                SOAPBody soapBody = env.addBody();                
                soapBody.addChildElement(elementoSoap2);                                               
            }
            msg.saveChanges();
            
        } catch (SOAPException ex) {
            Logger.getLogger(EnviarFacturaHandler.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
        
    }
    
}
