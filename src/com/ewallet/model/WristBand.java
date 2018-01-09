/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ewallet.model;

/**
 *
 * @author Ade Saprudin
 */
public class WristBand {
    private String rfid;
    private double balance;

    /**
     * @return the rfid
     */
    public String getRfid() {
        return rfid;
    }

    /**
     * @param rfid the rfid to set
     */
    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    /**
     * @return the balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * @param balance the balance to set
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    
}
