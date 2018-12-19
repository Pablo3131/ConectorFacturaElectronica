/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

/**
 *
 * @author Universal
 */
public class Filter {
    private String field = null;
    private String operator = null;
    private String value1 = null; 
    private String value2 = null;
    
    public Filter(String Field, String Operator, String Value1) {
        field = Field;
        operator = Operator;
        value1 = Value1;
    }
    
    public Filter(String Field, String Operator, String Value1, String Value2) {
        field = Field;
        operator = Operator;
        value1 = Value1;
        value2 = Value2;
    }
    
    public String buildFilter() {
        String fieldStr = "\"field\" : \"" + field + "\", ";
        String operatorStr = "\"operator\" : \"" + operator + "\", ";
        String value1Str = "\"value1\" : \"" + value1 + "\"";
        String value2Str = value2 != null ? ", \"value2\" : \"" + value2 + "\"" : "";
        
         return "{ " + fieldStr + operatorStr + value1Str + value2Str + " }";
    }
}
