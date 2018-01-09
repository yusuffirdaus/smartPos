/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.dao;


import com.openbravo.pos.ticket.Customer;
import com.openbravo.pos.ticket.TicketInfo;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
//import com.openbravo.pos.ticket.TicketInfo;

/**
 *
 * @author Ade Saprudin
 */
public class CustomerDAO extends BaseJdbcDAO{
    
     static CustomerDAO pd;
     
    
    public static CustomerDAO getInstance(){
        if(pd==null){
            pd=new CustomerDAO();
        }
        return pd;
    }
     //public void insert(Customer customer, String ticket) {
    public void insert(java.util.Date date, String id, String place, int male, int female) {

        Connection con = null;
        PreparedStatement ps = null;

        String sqlStr = "INSERT INTO customer_tbl(customer_date, male, female, place, ticket) VALUES (?, ?, ?, ?, ?)";

        try {
            //get connection
            con = getConnection();
            //prepare statement
            ps = con.prepareStatement(sqlStr);
//            ps.setTimestamp(1, new java.sql.Timestamp(customer.getCustomer_date().getTime()));
//            ps.setInt(2, customer.getMale());
//            ps.setInt(3, customer.getFemale());
//            ps.setString(4, customer.getPlace());
            ps.setTimestamp(1, new java.sql.Timestamp(date.getTime()));
            //ps.setTimestamp(1, date);
            ps.setInt(2, male);
            ps.setInt(3, female);
            ps.setString(4, place);
            ps.setString(5, id);
           
            
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    public void insert(Customer c, TicketInfo t) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

//    public void insert(java.util.Date date, String id, String place, int male, int female) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

}
