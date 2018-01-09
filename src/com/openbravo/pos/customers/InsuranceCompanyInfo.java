/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.customers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializerRead;

/**
 * class for handling synchronized insurance company from the ERP
 * @author Freddy Rodriguez - SmartJsp S.A.S.
 */
public class InsuranceCompanyInfo {
    private String id;
    private String smj_insurancecompany_id;
    private String name;
    private String smj_amountdue_debt;
    private String smj_nationalidnumber;
    private String smj_insurancecompanytype_id;

    public InsuranceCompanyInfo(String id, String smj_insurancecompany_id, String name, String smj_amountdue_debt, String smj_nationalidnumber, String smj_insurancecompanytype_id) {
        this.id = id;
        this.smj_insurancecompany_id = smj_insurancecompany_id;
        this.name = name;
        this.smj_amountdue_debt = smj_amountdue_debt;
        this.smj_nationalidnumber = smj_nationalidnumber;
        this.smj_insurancecompanytype_id = smj_insurancecompanytype_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSmj_amountdue_debt() {
        return smj_amountdue_debt;
    }

    public void setSmj_amountdue_debt(String smj_amountdue_debt) {
        this.smj_amountdue_debt = smj_amountdue_debt;
    }

    public String getSmj_insurancecompany_id() {
        return smj_insurancecompany_id;
    }

    public void setSmj_insurancecompany_id(String smj_insurancecompany_id) {
        this.smj_insurancecompany_id = smj_insurancecompany_id;
    }

    public String getSmj_insurancecompanytype_id() {
        return smj_insurancecompanytype_id;
    }

    public void setSmj_insurancecompanytype_id(String smj_insurancecompanytype_id) {
        this.smj_insurancecompanytype_id = smj_insurancecompanytype_id;
    }

    public String getSmj_nationalidnumber() {
        return smj_nationalidnumber;
    }

    public void setSmj_nationalidnumber(String smj_nationalidnumber) {
        this.smj_nationalidnumber = smj_nationalidnumber;
    }
    
    public static SerializerRead getSerializerRead() {
        return new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
            return new InsuranceCompanyInfo(dr.getString(1), dr.getString(2), dr.getString(3), dr.getString(4), dr.getString(5),dr.getString(6));
        }};
    }
}//InsuranceCompanyInfo
