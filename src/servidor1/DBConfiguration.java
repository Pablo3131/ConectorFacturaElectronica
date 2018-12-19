/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor1;

import java.util.Map;

/**
 *
 * @author Luis Arce
 */
public class DBConfiguration {
    //String url = "/opt/PrintServer/db/Conectorlavu.db"; //PRODUCTION
    //String url = "/opt/PrintServer/db/Conectorlavu_Test.db"; //REAL PATH TESTING
    //String url = "C:\\Users\\luis.arce\\Desktop\\Lavu\\ConectorFacturaElectronica\\Conectorlavu.db"; //PRODUCTION LOCAL
    public String url = ""; //"C:\\Users\\Universal\\Desktop\\Lavu\\ConectorFacturaElectronica\\Conectorlavu_Test.db"; // Test DataBase is default

    public DBConfiguration(Map<String, String> configuration) {       
        url = configuration.get("url");
    }
}
