/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.DataWrite;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.data.loader.SerializableWrite;
import com.openbravo.data.loader.SerializerRead;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 *
 * @author ade
 */
public class TicketProductNote implements SerializableWrite, SerializableRead, Serializable{
    
    private int id;
    private String ticketId;
    private String productId;
    private String note;  

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the ticketId
     */
    public String getTicketId() {
        return ticketId;
    }

    /**
     * @param ticketId the ticketId to set
     */
    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * @return the productId
     */
    public String getProductId() {
        return productId;
    }

    /**
     * @param productId the productId to set
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }
    
    public static SerializerRead getSerializerRead() {
        return new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
            TicketProductNote t = new TicketProductNote();
            t.id = dr.getInt(1);
            t.ticketId =dr.getString(2);
            t.productId=dr.getString(3);
            t.note=dr.getString(4);
            
            return t;
        }};
    }
    @Override
    public String toString(){
        return note;
    }

    public void writeValues(DataWrite dp) throws BasicException {
        dp.setInt(1, id);
        dp.setString(2, ticketId);
        dp.setString(3, productId);
        dp.setString(4, note);
        
    }

    public void readValues(DataRead dr) throws BasicException {
        id = dr.getInt(1);
        ticketId =dr.getString(2);
        productId=dr.getString(3);
        note =dr.getString(4);
    }
    
}
