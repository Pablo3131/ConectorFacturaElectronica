/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FacturaElectronica.GuruSoft;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Hugo
 */
public class KeyGenerationController {
    // 'CasaMatriz debe de se de tres caracteres m?ximo
    // 'Terminal debe ser máximo 5 cataracteres
    // 'Tipo comprobante dos caracteres
    // 'NumeroFactura diez caracteres, si se llega al numero máximo, comienza de 1 nuevamente
    public String CreaNumeroConsecutivo(String CasaMatriz, String TerminalPOS, String TipoComprobante, String NumeroFactura) {
        try {
            if ((CasaMatriz.trim().length() > 3)) {
                throw new Exception("Casa Matiz no debe de superar los 3 caracteres");
            }

            if ((TerminalPOS.trim().length() > 5)) {
                throw new Exception("Numero de terminal no debe de superar los 5 caracteres");
            }

            if ((TipoComprobante.trim().length() > 2)) {
                throw new Exception("Tipo Comprobante no debe de superar los 2 caracteres");
            }

            if ((NumeroFactura.trim().length() > 10)) {
                throw new Exception("Numero Factura no debe de superar los 10 caracteres");
            }

            String NumeroSecuencia = StringUtils.leftPad(CasaMatriz.trim(), 3, "0");
            //NumeroSecuencia = CasaMatriz.trim().PadLeft(3, '0');
            NumeroSecuencia += StringUtils.leftPad(TerminalPOS.trim(), 5, "0");
            //NumeroSecuencia = (NumeroSecuencia + TerminalPOS.trim().PadLeft(5, '0'));
            NumeroSecuencia += StringUtils.leftPad(TipoComprobante.trim(), 2, "0");
            //NumeroSecuencia = (NumeroSecuencia + TipoComprobante.trim().PadLeft(2, '0'));
            NumeroSecuencia += StringUtils.leftPad(NumeroFactura.trim(), 10, "0");
            //NumeroSecuencia = (NumeroSecuencia + NumeroFactura.trim().PadLeft(10, '0'));
            if ((NumeroSecuencia.trim().length() < 20)) {
                throw new Exception("Numero de secuencia inválido, debe tener 20 caracteres");
            }

            return NumeroSecuencia;
        } catch (Exception ex) {
            System.out.println();
            System.out.println("CreaNumeroConsecutivo");
            System.out.println(ex.getMessage());
            System.out.println();
            
            return "";
        }
    }

    // 'Los parametros TipoComprobante, Localidad y CodigoPuntoVenta pueden modificarse por otros valores, siempre manteniendo la longitud
    // 'Tipo Comprobante debe tener 2 caracteres máximo
    // 'Localidad debe tener 3 caracteres máximo
    // 'Punto de Venta debe de tener 5 caracteres máximo
    // 'Fecha es un campo datetime, debe ser la fecha de la factura
    // 'Número Factura debe tener máximo 10 caracteres y debe ser el mismo parámetro que se usa en la funcion GeneraNumeroSecuencia    
    public String CreaCodigoSeguridad(String TipoComprobante, String Localidad, String CodigoPuntoVenta, Date Fecha, String NumeroFactura) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
            if ((TipoComprobante.trim().length() > 2)) {
                throw new Exception("Tipo Comprobante debe tener 2 caracteres");
            }

            if ((Localidad.trim().length() > 3)) {
                throw new Exception("Localidad no debe de superar los 3 caracteres");
            }

            if ((CodigoPuntoVenta.trim().length() > 5)) {
                throw new Exception("Codigo Punto Venta no debe de superar los 5 caracteres");
            }

            if ((NumeroFactura.trim().length() > 10)) {
                throw new Exception("Numero Factura no de superar los 10 caracteres");
            }

            String concatenado = StringUtils.leftPad(TipoComprobante.trim(), 2, "0");
            concatenado += StringUtils.leftPad(Localidad.trim(), 3, "0");
            concatenado += StringUtils.leftPad(CodigoPuntoVenta.trim(), 5, "0");
            concatenado += df.format(Fecha);
            concatenado += StringUtils.leftPad(NumeroFactura.trim(), 10, "0");
            if ((concatenado.length() != 34)) {
                throw new Exception("El concatenado debe de ser de 34 caracteres para poder calcular el código de seguridad");
            }

            int calculo = 0;
            int contador = 3;
            for (int i = 0; i < concatenado.length(); i++) {
                if (contador == 1) {
                    contador = 9;
                }
                calculo += Integer.parseInt(String.valueOf(concatenado.charAt(i))) * contador;
                contador--;
            }

            int mDV = 0;
            int digitoMod = (calculo % 11);
            if (((digitoMod == 0)
                    || (digitoMod == 1))) {
                mDV = 0;
            } else {
                mDV = (11 - digitoMod);
            }

            return StringUtils.leftPad(TipoComprobante.trim(), 2, "0") + StringUtils.leftPad(String.valueOf(calculo), 5, "0") + String.valueOf(mDV);
        } catch (Exception ex) {
            System.out.println();
            System.out.println("CreaCodigoSeguridad");
            System.out.println(ex.getMessage());
            System.out.println();
            
            return "";
        }
    }

    // 'CodigoPais tres caracteres 
    // 'Dia y Mes dos caracteres
    // 'A?o dos caracteres
    // 'Numero idenficacion es el numero de cedula del emisor, 12 caracteres máximo
    // 'Numero consecutivo 20 caracteres, generados en la funcion CreaNumeroSecuencia
    // 'Situacion comprobante un caracter, 1.Normal 2.Contingencia 3.Sin Internet
    // 'Codigo Seguridad 8 caracteres generado con la funci?n CreaCodigoSeguridad    
    public String CreaClave(String CodigoPais, String Dia, String Mes, String Anno, String NumeroIdentifiaccion, String NumeracionConsecutiva, String SituacionComprobante, String CodigoSeguridad) {
        try {
            if (Anno.trim().length() == 4) {
                Anno = Anno.substring(2, 2);
            }

            if ((CodigoPais.trim().length() != 3)) {
                throw new Exception("Codigo país  debe tener 3 caracteres");
            }

            if ((Dia.trim().length() > 2)) {
                throw new Exception("Día no debe de superar los 2 caracteres");
            }

            if ((Mes.trim().length() > 2)) {
                throw new Exception("Mes no debe de superar los 2 caracteres");
            }

            if ((Anno.trim().length() > 2)) {
                throw new Exception("A?o no debe de superar los 2 caracteres");
            }

            if ((NumeroIdentifiaccion.trim().length() > 12)) {
                throw new Exception("Número Identifiacción de superar los 12 caracteres");
            }

            if ((NumeracionConsecutiva.trim().length() != 20)) {
                throw new Exception("Numero Consecutivo  debe tener 20 caracteres");
            }

            if ((SituacionComprobante.trim().length() > 1)) {
                throw new Exception("Situacion Comprobante debe tener un caracter");
            }

            if ((CodigoSeguridad.trim().length() > 8)) {
                throw new Exception("Código seguridad no debe de superar los 8 caracteres");
            }

            String Clave = CodigoPais;
            Clave = Clave + StringUtils.leftPad(Dia, 2, "0");
            Clave = Clave + StringUtils.leftPad(Mes, 2, '2');
            Clave = Clave + StringUtils.leftPad(Anno, 2, '0');
            Clave = Clave + StringUtils.leftPad(NumeroIdentifiaccion, 12, '0');
            Clave = Clave + NumeracionConsecutiva;
            Clave = Clave + SituacionComprobante;
            Clave = Clave + CodigoSeguridad;
            if ((Clave.length() != 50)) {
                throw new Exception("Clave inválida, debe de tener 50 caracteres");
            }

            return Clave;
        } catch (Exception ex) {
            System.out.println();
            System.out.println("CreaClave");
            System.out.println(ex.getMessage());
            System.out.println();
            
            return "";
        }
    }
}
