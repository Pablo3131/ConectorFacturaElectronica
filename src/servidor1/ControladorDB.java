/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor1;

import Entidades.DatosEmpresa;
import Entidades.Factura;
import Entidades.DetalleFactura;
import Entidades.DetalleImpuesto;
import Entidades.Estado;
import Entidades.NotasCredito;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.Pragma;

/**
 *
 * @author Usuario
 */
public class ControladorDB {
    
    private String url = null;
    private ConfigurationController config = ConfigurationController.getInstance();
    
    public ControladorDB() {
        
    }
    
    public void setURL(String connectionStr) {
        if ((connectionStr != null) && (!connectionStr.equals(""))) {
                url = connectionStr;
        }
    }
    
    //Se abre la conexión:
    public Connection Connect() {
        url = config.DB.url;
        
        Connection connect = null;
//        System.out.println();
//        System.out.println("Enter: ControladorDB.Connect");
//        System.out.println("DB URL: " + url);
        
        try {
            SQLiteConfig sqLiteConfig = new SQLiteConfig();
            Properties properties = sqLiteConfig.toProperties();
            properties.setProperty(Pragma.DATE_STRING_FORMAT.pragmaName, "yyyy-MM-dd HH:mm:ss:SSS");            
            connect = DriverManager.getConnection("jdbc:sqlite:" + url);
            if (connect != null) {
                //System.out.println("Conectado");
            }
        } catch (SQLException ex) {
            System.err.println("No se ha podido conectar a la base de datos\n" + ex.getMessage());
        }

        return connect;
    }
    
    public String getFacturaNextSequence() {
        String result = "";
        
        Connection conn = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        
        try
        {
            String sql = "select seq From sqlite_sequence Where name = 'Facturas'";
            
            conn = this.Connect();
            Statement stmt = conn.createStatement();            
            ResultSet rs = stmt.executeQuery(sql);                        
            List<Factura> invoices = new ArrayList<Factura>();
            List<Integer> Ids = new ArrayList<Integer>();

            while (rs.next()) {
                String seqStr = rs.getString("seq");
                
                if (seqStr != null) {
                    Integer seq = Integer.parseInt(seqStr) + 1;
                    result = seq.toString();
                }
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        }   
        finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return result;
    }
    
    public List<String> getLineasPorImprimir(String invoiceSeq) {
        List<String> result = new ArrayList<String>();
        
        Connection conn = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        
        try
        {
            String sql = "select TextoLinea From LineasPorImprimir Where SecuenciaFactura = " + invoiceSeq;
            
            conn = this.Connect();
            Statement stmt = conn.createStatement();            
            ResultSet rs = stmt.executeQuery(sql);                        
            List<Factura> invoices = new ArrayList<Factura>();
            List<Integer> Ids = new ArrayList<Integer>();
            int maxId = -1;
            
            while (rs.next()) {
                result.add(rs.getString("TextoLinea"));
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        }   
        finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return result;
    }
        
    public boolean isInCreditMemos(String OrderId, String referenceNumber) {
        boolean result = false;        
        Connection conn = null;
        
        try
        {
            String sql = "select IdOrdenFactura From NotasCredito Where IdOrdenFactura = '" + OrderId + "' and NumeroReferencia = '" + referenceNumber + "'";
            
            conn = this.Connect();
            Statement stmt = conn.createStatement();            
            ResultSet rs = stmt.executeQuery(sql);                        

            while (rs.next()) {
                result = true;
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return result;
    }
    
    public Factura getLastInvoiceByOrderID(String OrderId) {    
        // FUTURE RISK: DELAY TO GET EDOC/HACIENDA INFORMATION        
        Connection conn = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Factura result = null;
        
        try
        {
            String sql = "select Secuencia, IdOrden, NumeroConsecutivo, ClaveComprobante, FechaAutorizacion From Facturas Where IdOrden = '" + OrderId + "'";
            
            conn = this.Connect();
            Statement stmt = conn.createStatement();            
            ResultSet rs = stmt.executeQuery(sql);                        
            List<Factura> invoices = new ArrayList<Factura>();
            List<Integer> Ids = new ArrayList<Integer>();
            int maxId = -1;
            
            while (rs.next()) {
                Factura invoice = new Factura();
                
                Ids.add(rs.getInt("Secuencia"));
                
                invoice.setSecuencia(rs.getInt("Secuencia"));
                invoice.setIdOrden(rs.getString("IdOrden"));
                invoice.setNumeroConsecutivo(rs.getString("NumeroConsecutivo"));
                invoice.setClaveComprobante(rs.getString("ClaveComprobante"));
                invoice.setFechaAutorizacion(rs.getString("FechaAutorizacion") != null ? formatter.parse(rs.getString("FechaAutorizacion")) :
                                                                                         null);
                invoices.add(invoice);
            }
            
            if(Ids.size() > 0) {
                 maxId = Collections.max(Ids);
            }
                       
            for(Factura inv : invoices) {
                if (inv.getSecuencia() == maxId) {
                    result = inv;
                    break;
                }
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } 
        catch (ParseException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        }      
        finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return result;
    }
    
    public void insertCreditMemo(String OrderId, String NumberReference, String DateReference) {
        Connection conn = Connect();
        
        try {    
            String sqlInsert = "Insert Into NotasCredito(IdOrdenFactura, NumeroReferencia, FechaEmisionReferencia, Estado) Values (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sqlInsert);
            pstmt.setString(1, OrderId);
            pstmt.setString(2, NumberReference);
            pstmt.setString(3, DateReference);
            pstmt.setInt(4, 0); // Initial Status
            pstmt.executeUpdate(); 
            
        } catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
   }
    
    public DatosEmpresa ObtenerDatosEmpresa() {
        DatosEmpresa datosEmpresa = new DatosEmpresa();
        String sql = "Select Matriz, PuntoVenta, RazonSocial, TipoIdentificacion, NumIdentificacion, NombreComercial, Provincia, Canton, Distrito, Direccion, CorreoElectronico, Password, CantidadMaximaReintentos, RemitenteErrores, DestinatariosErrores, UrlTiquete, UrlNotaCredito, UrlFactura, Barrio From DatosEmpresa";
        //String sql = "Select * From DatosEmpresa"; 
        Connection conn = null;
        try {
            conn = this.Connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                datosEmpresa.setMatriz(rs.getInt("Matriz"));
                datosEmpresa.setPuntoVenta(rs.getInt("PuntoVenta"));
                datosEmpresa.setRazonSocial(rs.getString("RazonSocial"));
                datosEmpresa.setTipoIdentificacion(rs.getString("TipoIdentificacion"));
                datosEmpresa.setNumIdentificacion(rs.getString("NumIdentificacion"));
                datosEmpresa.setNombreComercial(rs.getString("NombreComercial"));
                datosEmpresa.setProvincia(rs.getInt("Provincia"));
                datosEmpresa.setCanton(rs.getInt("Canton"));
                datosEmpresa.setDistrito(rs.getInt("Distrito"));
                datosEmpresa.setDireccion(rs.getString("Direccion"));
                datosEmpresa.setCorreoElectronico(rs.getString("CorreoElectronico"));
                datosEmpresa.setPassword(rs.getString("Password"));
                datosEmpresa.setCantidadMaximaReintentos(rs.getInt("CantidadMaximaReintentos"));
                datosEmpresa.setRemitenteErrores(rs.getString("RemitenteErrores"));
                datosEmpresa.setDestinatariosErrores(rs.getString("DestinatariosErrores"));
                datosEmpresa.setUrlTiquete(rs.getString("UrlTiquete"));
                datosEmpresa.setUrlNotaCredito(rs.getString("UrlNotaCredito"));
                datosEmpresa.setUrlFactura(rs.getString("UrlFactura"));
                datosEmpresa.setBarrio(rs.getInt("Barrio"));
                datosEmpresa.setMensajeError("NA");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return datosEmpresa;
    }
    
    //0:Creada, cuando se ingresa un nuevo registro desde el parser.
    //1:Autorizada en Hacienda.
    //2:No autorizada.
    //3:En proceso, en estos casos se debe volver a preguntar por el estado.
    //4:Impresa.
    //5:Impresa y Pendiente por error de coneccion a la RED
    public List<Factura> BuscarFacturas(Estado estado) {

        List<Factura> lista = new ArrayList<Factura>();
        List<String> lineas = new ArrayList<String>();
        boolean primerRegistro = true;
        int consecutivo = 0;
        Factura factura = null;
        DetalleFactura dFactura = null;
        List<DetalleImpuesto> dImpuestos = null;

        //Factura factura = new Factura();
        //String sql = "Select * From Facturas Where Estado = " + estado; 
        String sql = "select ";
        sql = sql + "t1.Secuencia, t1.IdOrden, t1.Estado, t1.TotalVenta, t1.TotalImpuesto, t1.TotalComprobante, ";
        sql = sql + "t1.TotalDescuento, t1.TotalVentaNeta, t1.TotalVentaExento, t1.ClaveComprobante, ";
        sql = sql + "t1.CodCondicionVenta, t1.CodMedioPago1, t1.CodMedioPago2, t1.CodMedioPago3, t1.CodMedioPago4, ";
        sql = sql + "t2.Linea, t2.Descripcion, t2.Cantidad, t2.UnidadMedida, t2.PrecioUnitario, ";
        sql = sql + "t2.Monto, t2.MontoDescuento, t2.NaturalezaDescuento, t2.SubTotal, ";
        sql = sql + "t2.MontoTotalLinea, ";
        sql = sql + "t1.NombreCliente, t1.CorreoElectronicoCliente, t1.Reintentos, t1.IndexHaciedaInformation, t1.IdReceptor, t1.EsFactura, t1.TipoIdReceptor ";
        sql = sql + "from Facturas t1 ";
        sql = sql + "inner join DetalleFactura t2 On t1.Secuencia = t2.SecuenciaFactura ";
        sql = sql + "Where t1.Estado = " + estado.toInt() + " ";
        sql = sql + "Order By t1.Secuencia, t2.Linea asc";
        Connection conn = null;

        try {

            conn = this.Connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                dFactura = new DetalleFactura();
                dFactura.setCantidad(rs.getInt("Cantidad"));
                dFactura.setDescripcion(rs.getString("Descripcion"));
                dFactura.setLinea(rs.getInt("Linea"));
                dFactura.setMonto(rs.getBigDecimal("Monto"));
                dFactura.setMontoDescuento(rs.getBigDecimal("MontoDescuento"));
                dFactura.setMontoTotalLinea(rs.getBigDecimal("MontoTotalLinea"));
                dFactura.setNaturalezaDescuento(rs.getString("NaturalezaDescuento"));
                dFactura.setPrecioUnitario(rs.getBigDecimal("PrecioUnitario"));
                dFactura.setSubTotal(rs.getBigDecimal("SubTotal"));
                //dFactura.setTotalImpuesto(rs.getBigDecimal("TotalImpuesto"));
                dFactura.setUnidadMedida(rs.getString("UnidadMedida"));

                if (consecutivo != rs.getInt("Secuencia")) {
                    
                    if (!primerRegistro) {
                        lineas = SearchLinesToPrint(factura.getSecuencia(), conn);
                        factura.setInvoiceLinesToPrint(lineas);
                        lista.add(factura);                      
                    }else{
                        primerRegistro = false;
                    }

                    consecutivo = rs.getInt("Secuencia");
                    factura = new Factura();
                    factura.setSecuencia(consecutivo);
                    factura.setNombreCliente(rs.getString("NombreCliente"));
                    factura.setIdReceptor(rs.getString("IdReceptor"));
                    factura.setTipoIdReceptor(rs.getString("TipoIdReceptor"));
                    factura.setEsFactura(rs.getInt("EsFactura"));
                    factura.setCorreoElectronicoCliente(rs.getString("CorreoElectronicoCliente"));
                    factura.setCodigMedioPago1(rs.getString("CodMedioPago1"));
                    factura.setCodigMedioPago2(rs.getString("CodMedioPago2"));
                    factura.setCodigMedioPago3(rs.getString("CodMedioPago3"));
                    factura.setCodigMedioPago4(rs.getString("CodMedioPago4"));
                    factura.setCondicionVenta(rs.getString("CodCondicionVenta"));
                    factura.setIdOrden(rs.getString("IdOrden"));
                    factura.setTotalComprante(rs.getBigDecimal("TotalComprobante"));
                    factura.setTotalImpuesto(rs.getBigDecimal("TotalImpuesto"));
                    factura.setTotalVenta(rs.getBigDecimal("TotalVenta"));
                    factura.setTotalDescuentos(rs.getBigDecimal("TotalDescuento"));
                    factura.setTotalVentaNeta(rs.getBigDecimal("TotalVentaNeta"));
                    factura.setTotalVentaExento(rs.getBigDecimal("TotalVentaExento"));
                    factura.setClaveComprobante(rs.getString("ClaveComprobante") != null ? rs.getString("ClaveComprobante"): "");
                    
                    factura.setReintentos(rs.getInt("Reintentos"));
                    factura.setIndexHaciendaInformation(rs.getInt("IndexHaciedaInformation"));
                    factura.setDetalleFactura(dFactura);
                } else {
                    factura.setDetalleFactura(dFactura);
                }
                
                dImpuestos = SearchTaxDetails(consecutivo, dFactura.getLinea(), conn);
                dFactura.setdImpuesto(dImpuestos);
               
//                if (((estado == Estado.CREADA) || (estado == Estado.IMPRESA_ERROR_CONECCION))) //TODO: manage problems with Internet connection
//                }
            }
            if(factura != null){
                lineas = SearchLinesToPrint(factura.getSecuencia(), conn);
                factura.setInvoiceLinesToPrint(lineas);                
                lista.add(factura);
            }                      
        } catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return lista;
    }
    
    public List<DetalleImpuesto> SearchTaxDetails(int secuencia, int line, Connection conn){
        List<DetalleImpuesto> result = new ArrayList<DetalleImpuesto>();
        try
        {
            String sql = "select Codigo, Tarifa, Monto From DetalleImpuesto Where SecuenciaFactura = " + secuencia + " and LineaDetalle = " + line;
            
            Statement stmt = conn.createStatement();            
            ResultSet rs = stmt.executeQuery(sql);                        

            while (rs.next()) {
                DetalleImpuesto dImpuesto = new DetalleImpuesto();
                dImpuesto.setCodigo(rs.getString("Codigo"));
                dImpuesto.setTarifa(rs.getBigDecimal("Tarifa"));
                dImpuesto.setMonto(rs.getBigDecimal("Monto"));
                result.add(dImpuesto);
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        }   
        
        return result;
    }    
    
    public List<String> SearchLinesToPrint(int secuencia, Connection conn){
        List<String> lineas = new ArrayList<String>();
        try
        {
            String sql = "select TextoLinea From LineasPorImprimir Where SecuenciaFactura = " + secuencia + " Order By NumeroLinea asc";
            
            Statement stmt = conn.createStatement();            
            ResultSet rs = stmt.executeQuery(sql);                        

            while (rs.next()) {   
                lineas.add(rs.getString("TextoLinea"));
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        }   
        
        return lineas;
    }
    
    public void AgregarResultado(
            int Secuencia,
            Estado estado, 
            String fechaAutorizacion, 
            String numeroConsecutivo, 
            String claveComprobante
    ) {
        Connection conn = null;
        String sql = "Update Facturas Set " 
                + "Estado = ?, "
                + "FechaAutorizacion = ?, "
                + "NumeroConsecutivo = ?, "
                + "ClaveComprobante = ? "
                + "Where Secuencia = ?";

        try {

            conn = this.Connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, estado.toInt());
            pstmt.setString(2, fechaAutorizacion);
            pstmt.setString(3, numeroConsecutivo);
            pstmt.setString(4, claveComprobante);
            pstmt.setInt(5, Secuencia);
            
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void AgregarResultadoNotaCredito(
            int Secuencia,
            Estado estado, 
            String fechaAutorizacion, 
            String numeroConsecutivo, 
            String claveComprobante
    ) {
        Connection conn = null;
        String sql = "Update NotasCredito Set " 
                + "Estado = ?, "
                + "FechaAutorizacion = ?, "
                + "NumeroConsecutivo = ?, "
                + "ClaveComprobante = ? "
                + "Where Secuencia = ?";

        try {

            conn = this.Connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, estado.toInt());
            pstmt.setString(2, fechaAutorizacion);
            pstmt.setString(3, numeroConsecutivo);
            pstmt.setString(4, claveComprobante);
            pstmt.setInt(5, Secuencia);
            
            pstmt.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }    
    
    public void GuardarBitacoraRechazos(int secuencia, String trama, String mensajeRespuesta){
        Connection conn = null;
        String sql = "INSERT INTO BitacoraEnviosRechazados(Secuencia,Trama,Respuesta) VALUES(?,?,?)";
        try{
            conn = this.Connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, secuencia);
            pstmt.setString(2, trama);
            pstmt.setString(3, mensajeRespuesta);
            pstmt.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }    
    }
      
    public void CambiarEstado(String tabla, int secuencia, Estado estado, int reintentos, String clave, String consecutivo){
        Connection conn = null;
        String sql = "Update " + tabla + " Set " 
                + "Estado = ?, "
                + "Reintentos = ?, "
                + "ClaveComprobante = ?, "
                + "NumeroConsecutivo = ? "
                + "Where Secuencia = ?";
        
        try
        {
            conn = this.Connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, estado.toInt());
            pstmt.setInt(2, reintentos);
            pstmt.setString(3, clave == null ? "" : clave);
            pstmt.setString(4, consecutivo == null ? "" : consecutivo);
            pstmt.setInt(5, secuencia);
            
            pstmt.executeUpdate();            
            
        } catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }  
    }
    
    public void InsertInvoice(Factura factura){
        Connection conn = null;
        String sql1 = "Insert Into Facturas (Estado,";
        String sql2 = "Values (0,";
        //String sql3 = "select max(Secuencia) Maximo from Facturas;";
        String sql3 = "select last_insert_rowid() Consecutivo;";
        
        try{            
            conn = this.Connect();
            
            if (!factura.getClaveComprobante().equals("")) {
                sql1 += "ClaveComprobante,";
                sql2 += "'" + factura.getClaveComprobante() + "',";
            }
            
            sql1 += "IdOrden,";
            sql2 += "'" + factura.getIdOrden() + "',";
            
            //Se arma la trama encabezado de la base de datos:            
            sql1 += "IdOrden,";
            sql2 += "'" + factura.getIdOrden() + "',";
            
            //Se arma la trama encabezado de la base de datos:            
            sql1 += "NombreCliente,";
            sql2 += "'" + factura.getNombreCliente() + "',";
            
            sql1 += "CorreoElectronicoCliente,";
            sql2 += "'" + factura.getCorreoElectronicoCliente() + "',";
            
            //Parametro para indicar si es una factura electronica:
            sql1 += "EsFactura,";
            sql2 += factura.getEsFactura() + ",";    //0: es tiquete, 1; es factura.    
            
            if(factura.getEsFactura() == 1){
                sql1 += "IdReceptor,";
                sql2 += "'" + factura.getIdReceptor() + "',";        
                sql1 += "TipoIdReceptor,";
                sql2 += "'" + factura.getTipoIdReceptor() + "',";                   
            }
            
            sql1 += "TotalVenta,";
            sql2 += factura.getTotalVenta() + ",";
            
            if(factura.getTotalDescuentos().compareTo(BigDecimal.ZERO) > 0){
                sql1 += "TotalDescuento,";
                sql2 += factura.getTotalDescuentos() + ",";       
            }
            
            sql1 += "TotalVentaNeta,";
            sql2 += factura.getTotalVentaNeta() + ",";
            
            if(factura.getTotalImpuesto().compareTo(BigDecimal.ZERO) > 0){
                sql1 += "TotalImpuesto,";
                sql2 += factura.getTotalImpuesto() + ",";       
            }
            
            sql1 += "TotalVentaExento,";
            sql2 += factura.getTotalVentaExento() + ",";
                                       
            sql1 += "TotalComprobante,";
            sql2 += factura.getTotalComprante() + ",";
            
            sql1 += "CodCondicionVenta,";
            sql2 += "'" + factura.getCondicionVenta() + "',";
            
            if(!factura.getCodigMedioPago1().equals("")){
                sql1 += "CodMedioPago1,";
                sql2 += "'" + factura.getCodigMedioPago1() + "',";       
            }               
            
            if(!factura.getCodigMedioPago2().equals("")){
                sql1 += "CodMedioPago2,";
                sql2 += "'" + factura.getCodigMedioPago2() + "',";      
            }
            
            if(!factura.getCodigMedioPago3().equals("")){
                sql1 += "CodMedioPago3,";
                sql2 += "'" + factura.getCodigMedioPago3() + "',";      
            }

            if(!factura.getCodigMedioPago4().equals("")){
                sql1 += "CodMedioPago4,";
                sql2 += "'" + factura.getCodigMedioPago4() + "',";      
            }
            
            sql1 += "IndexHaciedaInformation,";
            sql2 += factura.getIndexHaciendaInformation() + ",";
            
            sql1 += "Reintentos)";
            sql2 += "0)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql1 + sql2);
            pstmt.executeUpdate();
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql3);

            int consecutivo = 0;
            
            while (rs.next()) {
                consecutivo = rs.getInt("Consecutivo");
                System.out.println(consecutivo);
            }
            
            //Se arma la trama detalle de la base de datos:            
            for(int i = 0; i < factura.getDetalleFactura().size(); i++){
                sql1 = "Insert Into DetalleFactura (SecuenciaFactura, Linea, Descripcion, Cantidad, UnidadMedida, PrecioUnitario, Monto, SubTotal, MontoTotalLinea";
                sql2 = "Values (?, ?, ?, ?, ?, ?, ?, ?, ?";       
                
                if(factura.getDetalleFactura().get(i).getMontoDescuento().compareTo(BigDecimal.ZERO) > 0){
                    sql1 += ", MontoDescuento, NaturalezaDescuento)";
                    sql2 += ", " + factura.getDetalleFactura().get(i).getMontoDescuento() + ", '" + factura.getDetalleFactura().get(i).getNaturalezaDescuento() + "');";
                }else{
                    sql1 += ")";
                    sql2 += ");";
                }
                
                pstmt = conn.prepareStatement(sql1 + sql2);
                
                pstmt.setInt(1, consecutivo);
                pstmt.setInt(2, factura.getDetalleFactura().get(i).getLinea());
                pstmt.setString(3, factura.getDetalleFactura().get(i).getDescripcion());
                pstmt.setInt(4, factura.getDetalleFactura().get(i).getCantidad());
                pstmt.setString(5, factura.getDetalleFactura().get(i).getUnidadMedida());
                pstmt.setBigDecimal(6, factura.getDetalleFactura().get(i).getPrecioUnitario());
                pstmt.setBigDecimal(7, factura.getDetalleFactura().get(i).getMonto());
                pstmt.setBigDecimal(8, factura.getDetalleFactura().get(i).getSubTotal());
                pstmt.setBigDecimal(9, factura.getDetalleFactura().get(i).getMontoTotalLinea());                                              
                pstmt.executeUpdate(); 
                
                for (DetalleImpuesto dImpuesto : factura.getDetalleFactura().get(i).getdImpuesto()) {
                    sql1 = "Insert Into DetalleImpuesto (SecuenciaFactura, LineaDetalle, Codigo, Tarifa, Monto) Values (?, ?, ?, ?, ?);";

                    pstmt = conn.prepareStatement(sql1);
                    pstmt.setInt(1, consecutivo);
                    pstmt.setInt(2, factura.getDetalleFactura().get(i).getLinea());
                    pstmt.setString(3, dImpuesto.getCodigo());
                    pstmt.setBigDecimal(4, dImpuesto.getTarifa());
                    pstmt.setBigDecimal(5, dImpuesto.getMonto());
                    pstmt.executeUpdate();
                }                
                
            }                                
            
            //Se inserta la línea por imprimir.
            int count = 1;
            for(int i = 0; i < factura.getInvoiceLinesToPrint().size(); i++){                
                sql1 = "Insert Into LineasPorImprimir(SecuenciaFactura, TextoLinea, NumeroLinea) Values (?, ?, ?)";
                pstmt = conn.prepareStatement(sql1);
                pstmt.setInt(1, consecutivo);
                pstmt.setString(2, factura.getInvoiceLinesToPrint().get(i));
                pstmt.setInt(3, count);
                pstmt.executeUpdate();  
                count++;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //0:Creada, cuando se ingresa un nuevo registro desde el parser.
    //1:Autorizada en Hacienda.
    //2:No autorizada.
    //3:En proceso, en estos casos se debe volver a preguntar por el estado.
    //4:Impresa.
    public List<NotasCredito> BuscarNotasCredito(Estado estado) {

        List<NotasCredito> lista = new ArrayList<NotasCredito>();
        List<String> lineas = new ArrayList<String>();
        boolean primerRegistro = true;
        int consecutivo = 0;
        NotasCredito notaCredito = null;
        DetalleFactura dFactura = null;
        List<DetalleImpuesto> dImpuestos = null;

        //Factura factura = new Factura();
        //String sql = "Select * From Facturas Where Estado = " + estado; 
        String sql = "select ";
        sql = sql + "t1.Secuencia, tf.IdOrden, t1.Estado, tf.TotalVenta, tf.TotalImpuesto, tf.TotalComprobante, ";
        sql = sql + "tf.TotalDescuento, tf.TotalVentaNeta, ";
        sql = sql + "tf.CodCondicionVenta, tf.CodMedioPago1, tf.CodMedioPago2, tf.CodMedioPago3, tf.CodMedioPago4, ";
        sql = sql + "t2.Linea, t2.Descripcion, t2.Cantidad, t2.UnidadMedida, t2.PrecioUnitario, ";
        sql = sql + "t2.Monto, t2.MontoDescuento, t2.NaturalezaDescuento, t2.SubTotal, ";
        sql = sql + "t2.MontoTotalLinea, ";
        sql = sql + "tf.NombreCliente, tf.CorreoElectronicoCliente, t1.Reintentos, t1.IndexHaciedaInformation, ";
        sql = sql + "tf.NumeroConsecutivo, tf.FechaAutorizacion ";
        sql = sql + "from NotasCredito t1 ";
        sql = sql + "inner join Facturas tf On t1.NumeroReferencia = tf.NumeroConsecutivo ";
        sql = sql + "inner join DetalleFactura t2 On tf.Secuencia = t2.SecuenciaFactura ";
        sql = sql + "Where t1.Estado = " + estado.toInt() + " ";
        sql = sql + "Order By t1.Secuencia, t2.Linea asc";
        Connection conn = null;

        try {

            conn = this.Connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                dFactura = new DetalleFactura();
                dFactura.setCantidad(rs.getInt("Cantidad"));
                dFactura.setDescripcion(rs.getString("Descripcion"));
                dFactura.setLinea(rs.getInt("Linea"));
                dFactura.setMonto(rs.getBigDecimal("Monto"));
                dFactura.setMontoDescuento(rs.getBigDecimal("MontoDescuento"));
                dFactura.setMontoTotalLinea(rs.getBigDecimal("MontoTotalLinea"));
                dFactura.setNaturalezaDescuento(rs.getString("NaturalezaDescuento"));
                dFactura.setPrecioUnitario(rs.getBigDecimal("PrecioUnitario"));
                dFactura.setSubTotal(rs.getBigDecimal("SubTotal"));
                //dFactura.setTotalImpuesto(rs.getBigDecimal("TotalImpuesto"));
                dFactura.setUnidadMedida(rs.getString("UnidadMedida"));

                if (consecutivo != rs.getInt("Secuencia")) {
                    
                    if (!primerRegistro) {
                        lineas = SearchLinesToPrint(notaCredito.getSecuencia(), conn);
                        notaCredito.setInvoiceLinesToPrint(lineas);
                        lista.add(notaCredito);                      
                    }else{
                        primerRegistro = false;
                    }

                    consecutivo = rs.getInt("Secuencia");
                    notaCredito = new NotasCredito();
                    notaCredito.setSecuencia(consecutivo);
                    notaCredito.setNombreCliente(rs.getString("NombreCliente"));
                    notaCredito.setCorreoElectronicoCliente(rs.getString("CorreoElectronicoCliente"));
                    notaCredito.setCodigMedioPago1(rs.getString("CodMedioPago1"));
                    notaCredito.setCodigMedioPago2(rs.getString("CodMedioPago2"));
                    notaCredito.setCodigMedioPago3(rs.getString("CodMedioPago3"));
                    notaCredito.setCodigMedioPago4(rs.getString("CodMedioPago4"));
                    notaCredito.setCondicionVenta(rs.getString("CodCondicionVenta"));
                    notaCredito.setIdOrden(rs.getString("IdOrden"));
                    notaCredito.setTotalComprante(rs.getBigDecimal("TotalComprobante"));
                    notaCredito.setTotalImpuesto(rs.getBigDecimal("TotalImpuesto"));
                    notaCredito.setTotalVenta(rs.getBigDecimal("TotalVenta"));
                    notaCredito.setTotalDescuentos(rs.getBigDecimal("TotalDescuento"));
                    notaCredito.setTotalVentaNeta(rs.getBigDecimal("TotalVentaNeta"));
                    notaCredito.setReintentos(rs.getInt("Reintentos"));
                    notaCredito.setIndexHaciendaInformation(rs.getInt("IndexHaciedaInformation"));
                    notaCredito.setReferenciaFactura(rs.getString("NumeroConsecutivo"));
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                    try {
                        notaCredito.setFechaReferencia(formatter.parse(rs.getString("FechaAutorizacion")));
                    } catch (ParseException ex) {
                        Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    notaCredito.setDetalleFactura(dFactura);
                } else {
                    notaCredito.setDetalleFactura(dFactura);
                }
                
                dImpuestos = SearchTaxDetails(consecutivo, dFactura.getLinea(), conn);
                dFactura.setdImpuesto(dImpuestos);
            }
            if(notaCredito != null){
                lineas = SearchLinesToPrint(notaCredito.getSecuencia(), conn);
                notaCredito.setInvoiceLinesToPrint(lineas);                
                lista.add(notaCredito);
            }                      
        } catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return lista;
    }
    
    public void InsertarBitacoraSistema(int factura, String bitacora){
        Connection conn = null;
        String sql = "INSERT INTO BitacoraSistema(FacturaReferencia,DescripcionEvento,FechaHora) VALUES(?,?,datetime('now', 'localtime'))";
        try{
            conn = this.Connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, factura);
            pstmt.setString(2, bitacora);
            pstmt.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ControladorDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }     
    }
}
