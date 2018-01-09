/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.dao;

import com.openbravo.pos.ticket.TicketProductNote;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author ade
 */
public class TicketProductNoteDAO extends BaseJdbcDAO {
    
    static TicketProductNoteDAO tpd;
    
    public static TicketProductNoteDAO getInstance(){
        if(tpd==null){
            tpd= new TicketProductNoteDAO();
            return tpd;
        }else{
            return tpd;
        }
        
    }
    
    public List<TicketProductNote> findAllByTicket(String ticketId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<TicketProductNote> vos = null;
        String sqlStr = "SELECT * FROM TICKET_PRODUCT_NOTE t WHERE t.TICKET_ID=?";

        try {
            //get connection
            con = getConnection();
            //prepare statement
            ps = con.prepareStatement(sqlStr);
            ps.setString(1, ticketId);
            //execute
            rs = ps.executeQuery();
            //transform to VO
            vos = transformSet(rs);
            System.out.println("findAllByTicket succeessed");
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

        return vos;
    }
    
    public TicketProductNote findByTicket(String ticketId, String productId) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        TicketProductNote t = null;
        String sqlStr = "SELECT * FROM TICKET_PRODUCT_NOTE t WHERE t.TICKET_ID=? AND t.PRODUCT_ID=?";
        System.out.println("Query : SELECT * FROM TICKET_PRODUCT_NOTE t WHERE t.TICKET_ID='"+ticketId+"' AND t.PRODUCT_ID='"+productId+"'");

        try {
            //get connection
            con = getConnection();
            //prepare statement
            ps = con.prepareStatement(sqlStr);
            ps.setString(1, ticketId);
            ps.setString(2, productId);
            //execute
            rs = ps.executeQuery();
            if(rs.next()){
                t =map2VO(rs);
            }
            //transform to VO
            
            System.out.println("findAllByTicket succeessed");
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

        return t;
    }

    public void deleteByTicket(String ticketId) {
        //String result = "";
        Connection con = null;
        PreparedStatement ps = null;
        String sqlStr = "DELETE FROM TICKET_PRODUCT_NOTE t WHERE t.TICKET_ID=?";

        try {
            //get connection
            con = getConnection();
            //prepare statement
            ps = con.prepareStatement(sqlStr);
            ps.setString(1, ticketId);
            //execute
            int a = ps.executeUpdate();
            //transform to VO

            System.out.println("deleteByTicket successed");
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

        //return result;
    }
    
    public void insert(TicketProductNote t) {
        String result = "";
        Connection con = null;
        PreparedStatement ps = null;
        String sqlStr = "INSERT INTO TICKET_PRODUCT_NOTE(TICKET_ID, PRODUCT_ID, NOTE) VALUES(?,?,?)";

        try {
            //get connection
            con = getConnection();
            //prepare statement
            ps = con.prepareStatement(sqlStr);
            ps.setString(1,t.getTicketId());
            ps.setString(2, t.getProductId());
            ps.setString(3, t.getNote());
            //execute
            int a = ps.executeUpdate();
            //transform to VO

            System.out.println("insert ticket successed");
        } catch (Exception ex) {
            result = ex.getMessage();
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

        //return result;
    }

    @Override
    protected TicketProductNote map2VO(ResultSet rs) throws SQLException {
        TicketProductNote t = new TicketProductNote();
        t.setId(rs.getInt("id"));
        t.setTicketId(rs.getString("ticket_id"));
        t.setProductId(rs.getString("product_id"));
        t.setNote(rs.getString("note"));
        return t;
    }
}
