/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entidades;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Universal
 */
public class FilterList { //TODO: Implement list
    private List<Filter> filters = new ArrayList<Filter>();
    
    public void addFilter(Filter filter) {
        filters.add(filter);
    }
    
    public String builfFilterList() {
        String result = "&filters=[";
        
        for(int index = 0; index < filters.size(); index++) {
            String divider = index == (filters.size() - 1) ? "]" : ",";
            
            result = result + filters.get(index).buildFilter() + divider;
        }
        
        return result;
    }
}
