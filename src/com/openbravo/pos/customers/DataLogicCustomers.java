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

package com.openbravo.pos.customers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataParams;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.QBFBuilder;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceExecTransaction;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerWriteParams;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author adrianromero
 */
public class DataLogicCustomers extends BeanFactoryDataSingle {
    
    protected Session s;
    private TableDefinition tcustomers;
    private static Datas[] customerdatas = new Datas[] {Datas.STRING, Datas.TIMESTAMP, Datas.TIMESTAMP, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.INT, Datas.BOOLEAN, Datas.STRING};
    
    public void init(Session s){
        
                this.s = s;
        tcustomers = new TableDefinition(s
            , "CUSTOMERS"
            , new String[] { "ID", "TAXID", "SEARCHKEY", "NAME", "NOTES", "VISIBLE", "CARD", "MAXDEBT", "CURDATE", "CURDEBT"
                           , "FIRSTNAME", "LASTNAME", "EMAIL", "PHONE", "PHONE2", "FAX"
                           , "ADDRESS", "ADDRESS2", "POSTAL", "CITY", "REGION", "COUNTRY"
                           , "TAXCATEGORY", "tax_exempt", "smj_nationalidnumber", "smj_insurancecompany_id", "smj_insuranceplan_id" }
            , new String[] { "ID", AppLocal.getIntString("label.taxid"), AppLocal.getIntString("label.searchkey"), AppLocal.getIntString("label.name"), AppLocal.getIntString("label.notes"), "VISIBLE", "CARD", AppLocal.getIntString("label.maxdebt"), AppLocal.getIntString("label.curdate"), AppLocal.getIntString("label.curdebt")
                           , AppLocal.getIntString("label.firstname"), AppLocal.getIntString("label.lastname"), AppLocal.getIntString("label.email"), AppLocal.getIntString("label.phone"), AppLocal.getIntString("label.phone2"), AppLocal.getIntString("label.fax")
                           , AppLocal.getIntString("label.address"), AppLocal.getIntString("label.address2"), AppLocal.getIntString("label.postal"), AppLocal.getIntString("label.city"), AppLocal.getIntString("label.region"), AppLocal.getIntString("label.country")
                           , "TAXCATEGORY","tax_exempt", AppLocal.getIntString("label.nationalNumber"), AppLocal.getIntString("label.insuranceCompany")
                           , AppLocal.getIntString("label.insurancePlan")}
            , new Datas[] { Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.BOOLEAN, Datas.STRING, Datas.DOUBLE, Datas.TIMESTAMP, Datas.DOUBLE
                          , Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING
                          , Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING
                          , Datas.STRING, Datas.BOOLEAN, Datas.STRING, Datas.STRING, Datas.STRING }
            , new Formats[] { Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.BOOLEAN, Formats.STRING, Formats.CURRENCY, Formats.TIMESTAMP, Formats.CURRENCY
                            , Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING
                            , Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING
                            , Formats.STRING, Formats.BOOLEAN, Formats.STRING, Formats.STRING , Formats.STRING  }
            , new int[] {0}
        );     
        
    }
    
    // CustomerList list
    public SentenceList getCustomerList() {
        return new StaticSentence(s
            , new QBFBuilder("SELECT ID, TAXID, SEARCHKEY, NAME FROM CUSTOMERS WHERE VISIBLE = " + s.DB.TRUE() + " AND ?(QBF_FILTER) ORDER BY NAME", new String[] {"TAXID", "SEARCHKEY", "NAME"})
            , new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING})
            , new SerializerRead() {
                    public Object readValues(DataRead dr) throws BasicException {
                        CustomerInfo c = new CustomerInfo(dr.getString(1));
                        c.setTaxid(dr.getString(2));
                        c.setSearchkey(dr.getString(3));
                        c.setName(dr.getString(4));
                        return c;
                    }                
                });
    }
       
    public int updateCustomerExt(final CustomerInfoExt customer) throws BasicException {
     
        return new PreparedSentence(s
                , "UPDATE CUSTOMERS SET NOTES = ? WHERE ID = ?"
                , SerializerWriteParams.INSTANCE      
                ).exec(new DataParams() { public void writeValues() throws BasicException {
                        setString(1, customer.getNotes());
                        setString(2, customer.getId());
                }});        
    }
    
    public final SentenceList getReservationsList() {
        return new PreparedSentence(s
            , "SELECT R.ID, R.CREATED, R.DATENEW, C.CUSTOMER, CUSTOMERS.TAXID, CUSTOMERS.SEARCHKEY, COALESCE(CUSTOMERS.NAME, R.TITLE),  R.CHAIRS, R.ISDONE, R.DESCRIPTION " +
              "FROM RESERVATIONS R LEFT OUTER JOIN RESERVATION_CUSTOMERS C ON R.ID = C.ID LEFT OUTER JOIN CUSTOMERS ON C.CUSTOMER = CUSTOMERS.ID " +
              "WHERE R.DATENEW >= ? AND R.DATENEW < ?"
            , new SerializerWriteBasic(new Datas[] {Datas.TIMESTAMP, Datas.TIMESTAMP})
            , new SerializerReadBasic(customerdatas));             
    }
    
    public final SentenceExec getReservationsUpdate() {
        return new SentenceExecTransaction(s) {
            public int execInTransaction(Object params) throws BasicException {  
    
                new PreparedSentence(s
                    , "DELETE FROM RESERVATION_CUSTOMERS WHERE ID = ?"
                    , new SerializerWriteBasicExt(customerdatas, new int[]{0})).exec(params);
                if (((Object[]) params)[3] != null) {
                    new PreparedSentence(s
                        , "INSERT INTO RESERVATION_CUSTOMERS (ID, CUSTOMER) VALUES (?, ?)"
                        , new SerializerWriteBasicExt(customerdatas, new int[]{0, 3})).exec(params);                
                }
                return new PreparedSentence(s
                    , "UPDATE RESERVATIONS SET ID = ?, CREATED = ?, DATENEW = ?, TITLE = ?, CHAIRS = ?, ISDONE = ?, DESCRIPTION = ? WHERE ID = ?"
                    , new SerializerWriteBasicExt(customerdatas, new int[]{0, 1, 2, 6, 7, 8, 9, 0})).exec(params);
            }
        };
    }
    
    public final SentenceExec getReservationsDelete() {
        return new SentenceExecTransaction(s) {
            public int execInTransaction(Object params) throws BasicException {  
    
                new PreparedSentence(s
                    , "DELETE FROM RESERVATION_CUSTOMERS WHERE ID = ?"
                    , new SerializerWriteBasicExt(customerdatas, new int[]{0})).exec(params);
                return new PreparedSentence(s
                    , "DELETE FROM RESERVATIONS WHERE ID = ?"
                    , new SerializerWriteBasicExt(customerdatas, new int[]{0})).exec(params);
            }
        };
    }
    
    public final SentenceExec getReservationsInsert() {
        return new SentenceExecTransaction(s) {
            public int execInTransaction(Object params) throws BasicException {  
    
                int i = new PreparedSentence(s
                    , "INSERT INTO RESERVATIONS (ID, CREATED, DATENEW, TITLE, CHAIRS, ISDONE, DESCRIPTION) VALUES (?, ?, ?, ?, ?, ?, ?)"
                    , new SerializerWriteBasicExt(customerdatas, new int[]{0, 1, 2, 6, 7, 8, 9})).exec(params);

                if (((Object[]) params)[3] != null) {
                    new PreparedSentence(s
                        , "INSERT INTO RESERVATION_CUSTOMERS (ID, CUSTOMER) VALUES (?, ?)"
                        , new SerializerWriteBasicExt(customerdatas, new int[]{0, 3})).exec(params);                
                }
                return i;
            }
        };
    }
    
    public final TableDefinition getTableCustomers() {
        return tcustomers;
    }  
    
    /**
     * regresa lista de paises - return country list
     * @return  List<CountryInfo>
     */
    public final List<CountryInfo> getCountrys()   {
        try{
            return new PreparedSentence(s
                , "SELECT id,currency_id, country_code, description, name, region_name "+
                  "FROM country where '1' = ? ORDER BY name "
                , SerializerWriteString.INSTANCE
                , CountryInfo.getSerializerRead()).list("1");
        }catch(BasicException be){
            be.printStackTrace();
            return new ArrayList<CountryInfo>() {};
        }
    }
    

    /**
     * regresa la informacion de pais por id - return country information by Id
     * @param id
     * @return CountryInfo
     * @throws BasicException 
     */
    public final CountryInfo getCountry(String id) throws BasicException {
        return (CountryInfo) new PreparedSentence(s
            , "SELECT id,currency_id, country_code, description, name, region_name " +
              "FROM country WHERE id = ?"
            , SerializerWriteString.INSTANCE
            , CountryInfo.getSerializerRead()).find(id);
    }
    
    /**
     * regresa lista de Regiones - return Region List
     * @param countryId
     * @return List<RegionInfo>
     */
    public final List<RegionInfo> getRegions(String countryId){
        try{
            return new PreparedSentence(s
                , "SELECT id, country_id, description, name "+
                  "FROM  region WHERE country_id = ? ORDER BY name "
                , SerializerWriteString.INSTANCE
                , RegionInfo.getSerializerRead()).list(countryId);
        }catch(BasicException be){
            return new ArrayList<RegionInfo>() {};
        }
    }
    
    
    /**
     * regresa la informacion de region por id - return region information by Id
     * @param id
     * @return RegionInfo
     * @throws BasicException 
     */
    public final RegionInfo getRegion(String id) throws BasicException {
        return (RegionInfo) new PreparedSentence(s
            , "SELECT id, country_id, description, name " +
              "FROM  region WHERE id = ?"
            , SerializerWriteString.INSTANCE
            , RegionInfo.getSerializerRead()).find(id);
    }
    
    /**
     * regresa lista de ciudades - return city List
     * @param regionId
     * @return List<CityInfo>
     */
    public final List<CityInfo> getCities(String regionId){
        try{
            return new PreparedSentence(s
                , "SELECT id, country_id, region_id, name, postal "+
                  "FROM  city WHERE region_id = ? ORDER BY name "
                , SerializerWriteString.INSTANCE
                , CityInfo.getSerializerRead()).list(regionId);
        }catch(BasicException be){
            return new ArrayList<CityInfo>() {};
        }
    }
    
    
    /**
     * regresa la informacion de ciudad por id - return city information by Id
     * @param id
     * @return CountryInfo
     * @throws BasicException 
     */
    public final CountryInfo getCity(String id) throws BasicException {
        return (CountryInfo) new PreparedSentence(s
            , "SELECT id, country_id, region_id, name, postal " +
              "FROM  city WHERE id = ?"
            , SerializerWriteString.INSTANCE
            , CityInfo.getSerializerRead()).find(id);
    }
    
    /**
     * regresa el idi del cliente por TaxId - Returns Customer Id by TaxId
     * @param taxId
     * @return String
     * @throws BasicException 
     */
    public final String getCustomerIdWithTaxId(final String taxId) throws BasicException {
        return (String) new PreparedSentence(s
            , "SELECT id  " +
              "  FROM customers  where taxid = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
                return dr.getString(1);
            }}).find(taxId);
    }
    
        /**
     * regresa lista de empresas aseguradoras - return Insurance Company list
     * @return  List<InsuranceCompanyInfo>
     */
    public final List<InsuranceCompanyInfo> getInsureCompanys()   {
        try{
            return new PreparedSentence(s
                , "SELECT id, smj_insurancecompany_id, name, smj_amountdue_debt, smj_nationalidnumber, smj_insurancecompanytype_id  "+
                  "FROM smj_insurancecompany where '1' = ? ORDER BY name "
                , SerializerWriteString.INSTANCE
                , InsuranceCompanyInfo.getSerializerRead()).list("1");
        }catch(BasicException be){
            be.printStackTrace();
            return new ArrayList<InsuranceCompanyInfo>() {};
        }
    }
    
    /**
     * regresa la informacion de empresa aseguradora por id - return Insurance Company information by Id
     * @param id
     * @return InsuranceCompanyInfo
     * @throws BasicException 
     */
    public final InsuranceCompanyInfo getInsuranceCompany(String id) throws BasicException {
        return (InsuranceCompanyInfo) new PreparedSentence(s
            , "SELECT id, smj_insurancecompany_id, name, smj_amountdue_debt, smj_nationalidnumber, smj_insurancecompanytype_id  " +
              "FROM smj_insurancecompany WHERE id = ?"
            , SerializerWriteString.INSTANCE
            , InsuranceCompanyInfo.getSerializerRead()).find(id);
    }
    
    /**
     * regresa lista de planes de seguros - return Insurance Plan
     * @param insureCompanyId
     * @return List<InsurancePlanInfo>
     */
    public final List<InsurancePlanInfo> getInsurancePlans(String insureCompanyId){
        try{
            return new PreparedSentence(s
                , "SELECT smj_insuranceplan_id, name, c_bpartner_id, copay_percentage, copay_value "+
                  "FROM  smj_insuranceplan WHERE c_bpartner_id = ? ORDER BY name "
                , SerializerWriteString.INSTANCE
                , InsurancePlanInfo.getSerializerRead()).list(insureCompanyId);
        }catch(BasicException be){
            return new ArrayList<InsurancePlanInfo>() {};
        }
    }
    
    
    /**
     * regresa la informacion de plan de seguros por empresa aseguradora - 
     * return Insurance Plan information by Insurance Company
     * @param id
     * @return InsuranceCompanyInfo
     * @throws BasicException 
     */
    public final InsurancePlanInfo getInsurancePlan(String id) throws BasicException {
        return (InsurancePlanInfo) new PreparedSentence(s
            , "SELECT smj_insuranceplan_id, name, c_bpartner_id, copay_percentage, copay_value " +
              "FROM  smj_insuranceplan WHERE smj_insuranceplan_id = ?"
            , SerializerWriteString.INSTANCE
            , InsurancePlanInfo.getSerializerRead()).find(id);
    }
    
}
