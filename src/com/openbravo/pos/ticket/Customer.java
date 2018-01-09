/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.ticket;

import java.util.Date;

/**
 *
 * @author Ade Saprudin
 */
public class Customer {
    private int id;
    private Date customer_date;
    private int male;
    private int female;
    private String place;

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
     * @return the customer_date
     */
    public Date getCustomer_date() {
        return customer_date;
    }

    /**
     * @param customer_date the customer_date to set
     */
    public void setCustomer_date(Date customer_date) {
        this.customer_date = customer_date;
    }

    /**
     * @return the male
     */
    public int getMale() {
        return male;
    }

    /**
     * @param male the male to set
     */
    public void setMale(int male) {
        this.male = male;
    }

    /**
     * @return the female
     */
    public int getFemale() {
        return female;
    }

    /**
     * @param female the female to set
     */
    public void setFemale(int female) {
        this.female = female;
    }

    /**
     * @return the place
     */
    public String getPlace() {
        return place;
    }

    /**
     * @param place the place to set
     */
    public void setPlace(String place) {
        this.place = place;
    }
    
}
