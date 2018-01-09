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

package com.openbravo.pos.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 *
 * @author jaroslawwozniak
 */
public class ProductDAO extends BaseJdbcDAO {

    static ProductDAO pd;
    
    public static ProductDAO getInstance(){
        if(pd==null){
            pd=new ProductDAO();
        }
        return pd;
    }
    

     public void updateSoldProduct(String productId, String qty) {

        Connection con = null;
        PreparedStatement ps = null;

        String sqlStr = "UPDATE PRODUCTS SET SOLD_UNIT=SOLD_UNIT+"+qty+" WHERE ID=? AND VISIBLE";

        try {
            //get connection
            con = getConnection();
            //prepare statement
            ps = con.prepareStatement(sqlStr);
            ps.setString(1, productId);
            //execute
            ps.executeUpdate();
            

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                // close the resources 
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException sqlee) {
                sqlee.printStackTrace();
            }
        }

//        return vos;

    }

    @Override
    protected Object map2VO(ResultSet rs) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
