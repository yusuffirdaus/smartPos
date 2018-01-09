/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.customers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializerRead;

/**
 * class for handling synchronized insurance plan from the ERP
 * @author Freddy Rodriguez - SmartJsp S.A.S.
 */
public class InsurancePlanInfo {
    private String smj_insuranceplan_id;
    private String name, c_bpartner_id;
    private String copay_percentage;
    private String copay_value;

    public InsurancePlanInfo(String smj_insuranceplan_id, String name, String c_bpartner_id, String copay_percentage, String copay_value) {
        this.smj_insuranceplan_id = smj_insuranceplan_id;
        this.name = name;
        this.c_bpartner_id = c_bpartner_id;
        this.copay_percentage = copay_percentage;
        this.copay_value = copay_value;
    }

    public String getC_bpartner_id() {
        return c_bpartner_id;
    }

    public void setC_bpartner_id(String c_bpartner_id) {
        this.c_bpartner_id = c_bpartner_id;
    }

    public String getCopay_percentage() {
        return copay_percentage;
    }

    public void setCopay_percentage(String copay_percentage) {
        this.copay_percentage = copay_percentage;
    }

    public String getCopay_value() {
        return copay_value;
    }

    public void setCopay_value(String copay_value) {
        this.copay_value = copay_value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSmj_insuranceplan_id() {
        return smj_insuranceplan_id;
    }

    public void setSmj_insuranceplan_id(String smj_insuranceplan_id) {
        this.smj_insuranceplan_id = smj_insuranceplan_id;
    }
    
    public static SerializerRead getSerializerRead() {
        return new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
            return new InsurancePlanInfo(dr.getString(1), dr.getString(2), dr.getString(3), dr.getString(4), dr.getString(5));
        }};
    }
}//InsurancePlanInfo
