/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor1;

import Entidades.Factura;
import Impresoras.Epson.PrintingConfiguration;
import Lightspeed.ERPConectorConfiguration;
import Lightspeed.ParserConfiguration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
 * @author Luis Arce
 */
public class ConfigurationController {
    private static ConfigurationController instance = null;
    public PrintingConfiguration printing = null;
    public ParserConfiguration parser = null;
    public ERPConectorConfiguration ERPConector = null;
    public DBConfiguration DB = null;
    private String configDBURL = "C:\\Users\\Universal\\Desktop\\lightspeed\\DBTemplate\\Configuration.db";
    
    private ConfigurationController() {
        Map<String, String> printingConfig = getConfigurationByType("PrintingConfig");
        printing = new PrintingConfiguration(printingConfig);
        
//        Map<String, String> parserConfig = getConfigurationByType("ParserConfig");
//        parser = new ParserConfiguration(parserConfig);
//        
//        Map<String, String> ERPConfig = getConfigurationByType("ERPConectorConfig");
//        ERPConector = new ERPConectorConfiguration(ERPConfig);
        
        Map<String, String> DBConfig = getConfigurationByType("DBConfig");
        DB = new DBConfiguration(DBConfig);
    }
    
    public static ConfigurationController getInstance() {
        if (instance == null) {
            instance = new ConfigurationController();
        }
        
        return instance;
    }
    
    private Connection Connect(String url) {
        Connection connect = null;

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
    
    public Map<String, String> getConfigurationByType(String configType) {       
        Map<String, String> result = new HashMap<String, String>();
        Connection conn = Connect(configDBURL);
        
        try
        {
            String sql = "select ConfigName, ConfigValue From Configuration Where ConfigType = '" + configType + "'"; 
            
            Statement stmt = conn.createStatement();            
            ResultSet rs = stmt.executeQuery(sql);                        
            List<Factura> invoices = new ArrayList<Factura>();
            
            while (rs.next()) {
                result.put(rs.getString("ConfigName"), rs.getString("ConfigValue"));
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
    
}
