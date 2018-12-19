/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FacturaElectronica.GuruSoft;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author Hugo
 */
public class ControladorBase {   
    
    public String ConvertirFecha(XMLGregorianCalendar fecha){
        return String.format("%s-%02d-%02d %02d:%02d:%02d:%s", 
                fecha.getYear(), fecha.getMonth(), fecha.getDay(), fecha.getHour(), fecha.getMinute(), fecha.getSecond(), fecha.getMillisecond());
    }
    
    public void EnviarCorreo(String remitente, String destinatarios, String asunto, String cuerpo) {
        // Esto es lo que va delante de @gmail.com en tu cuenta de correo. Es el remitente también.
        //String remitente = "raspberrypicr2018@gmail.com";  //Para la dirección nomcuenta@gmail.com

        Properties props = System.getProperties();
        props.put("mail.smtp.host", "smtp.gmail.com");  //El servidor SMTP de Google
        props.put("mail.smtp.user", remitente);
        props.put("mail.smtp.clave", "kanakit123");    //La clave de la cuenta
        props.put("mail.smtp.auth", "true");    //Usar autenticación mediante usuario y clave
        props.put("mail.smtp.starttls.enable", "true"); //Para conectar de manera segura al servidor SMTP
        props.put("mail.smtp.port", "587"); //El puerto SMTP seguro de Google

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(remitente));
            String[] listaCorreos = destinatarios.split(";");
            for(int i = 0; i < listaCorreos.length; i++){
              message.addRecipient(Message.RecipientType.TO, new InternetAddress(listaCorreos[i]));  
            }
            //message.addRecipient(Message.RecipientType.TO, new InternetAddress("vichms06@hotmail.com"));   //Se podrían añadir varios de la misma manera
            
            message.setSubject(asunto);
            message.setText(cuerpo);
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", remitente, "kanakit123");
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (MessagingException me) {
            me.printStackTrace();   //Si se produce un error
        }
    }
}
