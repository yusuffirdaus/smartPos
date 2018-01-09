/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializerRead;

/**
 * clase para manejo de unidades de productos
 * class for management product units
 * @author Carlos Prieto - SmartJSP S.A.S.
 */
public class InsuranceInfo {
    private String id;
    private double copay_value;
    private double copay_percentage;
    private String p_name;
    private String smj_insurancecompany_id;
    private String i_name;

    public InsuranceInfo() {
    }

    public InsuranceInfo(String id, double copay_value, double copay_percentage , String p_name ,String  smj_insurancecompany_id , String i_name ) {
        this.id = id;
        this.copay_value = copay_value;
        this.copay_percentage = copay_percentage;
        this.p_name = p_name;
        this.smj_insurancecompany_id = smj_insurancecompany_id;
        this.i_name= i_name;
    }

    public double getCopay_percentage() {
        return copay_percentage;
    }

    public void setCopay_percentage(double copay_percentage) {
        this.copay_percentage = copay_percentage;
    }

    public double getCopay_value() {
        return copay_value;
    }

    public void setCopay_value(double copay_value) {
        this.copay_value = copay_value;
    }

    public String getI_name() {
        return i_name;
    }

    public void setI_name(String i_name) {
        this.i_name = i_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getP_name() {
        return p_name;
    }

    public void setP_name(String p_name) {
        this.p_name = p_name;
    }

    public String getSmj_insurancecompany_id() {
        return smj_insurancecompany_id;
    }

    public void setSmj_insurancecompany_id(String smj_insurancecompany_id) {
        this.smj_insurancecompany_id = smj_insurancecompany_id;
    }

    
    
    public static SerializerRead getSerializerRead() {
        return new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
            return new InsuranceInfo(dr.getString(1), dr.getDouble(2), dr.getDouble(3), dr.getString(4), dr.getString(5) ,dr.getString(6));
        }};
    }
}
