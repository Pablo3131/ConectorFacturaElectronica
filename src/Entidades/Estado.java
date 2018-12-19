/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

/**
 *
 * @author Hugo
 */
public enum Estado {
    CREADA(0), AUTORIZADA(1), NO_AUTORIZADA(2), EN_PROCESO(3), IMPRESA(4), ERROR(5), IMPRESA_ERROR_CONECCION(6);
    
    private final int code;
    
    private Estado(int code) {
        this.code = code;
    }

    public int toInt() {
        return code;
    }   
}
