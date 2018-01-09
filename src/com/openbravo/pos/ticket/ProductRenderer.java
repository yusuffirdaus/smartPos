//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.ticket;

import javax.swing.*;
import java.awt.*;

import com.openbravo.pos.util.ThumbNailBuilder;


import java.text.DecimalFormat;
import org.apache.commons.lang.StringUtils;

/**
 * Controls the rendering of the jtable for the products finder
 * @author adrianromero
 * modified by Pedro Rozo
 */
public class ProductRenderer extends DefaultListCellRenderer {
                
    ThumbNailBuilder tnbprod;
    final DecimalFormat df = new DecimalFormat("#.#");
    /** Creates a new instance of ProductRenderer */
    public ProductRenderer() {   
        tnbprod = new ThumbNailBuilder(64, 32, "com/openbravo/images/package.png");
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
        
        
        
       // PosDAO dao = new PosDAO(JRootApp.getM_props());
        
        ProductInfoExt prod = (ProductInfoExt) value;
        if (prod != null) {
            String uniTexto = prod.getProperty("unidadDefaultTexto");
            if (uniTexto.equalsIgnoreCase("null") || uniTexto == null) {
                uniTexto = " ";
            }
                
            String linea =  getStringFijo(prod.getName(),35) + " "+
                            getStringFijoDerecha(df.format(new Double(prod.getPriceSell()) ),7) +" "+
                            getStringFijo(uniTexto ,4) +" "+
                            getStringFijoDerecha(prod.getProperty("existencias"),5) + " "+
                            getStringFijo(prod.getProperty("unidad") ,4) +" "+
                            getStringFijo(prod.getProperty("ubicacion"),8);
            setText("<HTML><PRE>"+linea+"</PRE>");
           // Image img = tnbprod.getThumbNail(prod.getImage());
           // setIcon(img == null ? null :new ImageIcon(img));
        }
        return this;
    }  
    
    public String getStringFijo (String s, int t){
        return StringUtils.left(StringUtils.rightPad(s,t," "),t);
    }
    
    public String getStringFijoDerecha (String s, int t){
        return StringUtils.right(StringUtils.leftPad(s.trim(),t," "),t);
    }
    
}
