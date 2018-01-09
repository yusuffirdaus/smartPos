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
package com.openbravo.pos.ticket;

import com.ewallet.utilities.Util;
import java.util.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializableRead;
import com.openbravo.format.Formats;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.LocalRes;
import com.openbravo.pos.customers.CustomerInfoExt;

import com.openbravo.pos.payment.PaymentInfoMagcard;
import com.openbravo.pos.sales.DataLogicReceipts;

import com.openbravo.pos.sales.restaurant.Place;
import com.openbravo.pos.util.StringUtils;
import com.openbravo.pos.util.VariableG;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author adrianromero
 */
public class TicketInfo implements SerializableRead, Externalizable {

    private static final long serialVersionUID = 2765650092387265178L;

    public static final int RECEIPT_NORMAL = 0;
    public static final int RECEIPT_REFUND = 1;
    public static final int RECEIPT_PAYMENT = 2;

    private static DateFormat m_dateformat = new SimpleDateFormat("hh:mm");

    private String m_sId;
    private int tickettype;
    private int m_iTicketId;
    private java.util.Date m_dDate;
    private Properties attributes;
    private UserInfo m_User;
    private CustomerInfoExt m_Customer;
    //private VariableG varG;
    private String m_sActiveCash;
    private List<TicketLineInfo> m_aLines;
    private List<PaymentInfo> payments;
    private List<TicketTaxInfo> taxes;
    private String m_sResponse;
    private int refundticketId =0;
    private InsuranceInfo iInfo = null;           
    private String copay =  "";
    private String coveredInsurance = "";
    
    private Customer customer;
    private Place place;
    private DataLogicReceipts dlReceipts = null;
    private Date customer_date;
    

    /** Creates new TicketModel */
    public TicketInfo() {
        m_sId = String.valueOf(Calendar.getInstance().getTimeInMillis());
               // UUID.randomUUID().toString();
        tickettype = RECEIPT_NORMAL;
        m_iTicketId = 0; // incrementamos
        m_dDate = new Date();
        attributes = new Properties();
        m_User = null;
        m_Customer = null;
        m_sActiveCash = null;
        m_aLines = new ArrayList<TicketLineInfo>(); // vacio de lineas

        payments = new ArrayList<PaymentInfo>();
        taxes = null;
        m_sResponse = null;
        
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // esto es solo para serializar tickets que no estan en la bolsa de tickets pendientes
        out.writeObject(m_sId);
        out.writeInt(tickettype);
        out.writeInt(m_iTicketId);
        out.writeObject(m_Customer);
        //@win
        //out.writeObject(m_User);
        out.writeObject(m_dDate);
        out.writeObject(attributes);
        out.writeObject(m_aLines);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // esto es solo para serializar tickets que no estan en la bolsa de tickets pendientes
        m_sId = (String) in.readObject();
        tickettype = in.readInt();
        m_iTicketId = in.readInt();
        m_Customer = (CustomerInfoExt) in.readObject();
//@win
        //m_User = (UserInfo) in.readObject();
        //--
        m_dDate = (Date) in.readObject();
        attributes = (Properties) in.readObject();
        m_aLines = (List<TicketLineInfo>) in.readObject();
        m_User = null;
        m_sActiveCash = null;

        payments = new ArrayList<PaymentInfo>();
        taxes = null;
    }

    public void readValues(DataRead dr) throws BasicException {
        m_sId = dr.getString(1);
        tickettype = dr.getInt(2).intValue();
        m_iTicketId = dr.getInt(3).intValue();
        m_dDate = dr.getTimestamp(4);
        m_sActiveCash = dr.getString(5);
        try {
            byte[] img = dr.getBytes(6);
            if (img != null) {
                attributes.loadFromXML(new ByteArrayInputStream(img));
            }
        } catch (IOException e) {
        }
        m_User = new UserInfo(dr.getString(7), dr.getString(8));
        m_Customer = new CustomerInfoExt(dr.getString(9));
        m_aLines = new ArrayList<TicketLineInfo>();

        payments = new ArrayList<PaymentInfo>();
        taxes = null;
    }

    public TicketInfo copyTicket() {
        TicketInfo t = new TicketInfo();

        t.tickettype = tickettype;
        t.m_iTicketId = m_iTicketId;
        t.m_dDate = m_dDate;
        t.m_sActiveCash = m_sActiveCash;
        t.attributes = (Properties) attributes.clone();
        t.m_User = m_User;
        t.m_Customer = m_Customer;
        t.m_aLines = new ArrayList<TicketLineInfo>();
        for (TicketLineInfo l : m_aLines) {
            t.m_aLines.add(l.copyTicketLine());
        }
        t.refreshLines();

        t.payments = new LinkedList<PaymentInfo>();
        for (PaymentInfo p : payments) {
            t.payments.add(p.copyPayment());
        }

        // taxes are not copied, must be calculated again.

        return t;
    }

    public String getId() {
        return m_sId;
    }
    //@ade
    public void setId(String m_sId) {
        this.m_sId= m_sId;
    }

    public int getTicketType() {
        return tickettype;
    }

    public void setTicketType(int tickettype) {
        this.tickettype = tickettype;
    }

    public int getTicketId() {
        return m_iTicketId;
    }

    public void setTicketId(int iTicketId) {
        m_iTicketId = iTicketId;
    // refreshLines();
    }

    public String getName(Object info) {

        StringBuffer name = new StringBuffer();

        if (getCustomerId() != null) {
            name.append(m_Customer.toString());
            name.append(" - ");
        }else{
            //@win
            name.append("One Time Customer");
            name.append(" - ");
        }

        if (info == null) {
            if (m_iTicketId == 0) {
                name.append("(" + m_dateformat.format(m_dDate) + " " + Long.toString(m_dDate.getTime() % 1000) + ")");
            } else {
                name.append(Integer.toString(m_iTicketId));
            }
        } else {
            name.append(info.toString());
        }
        
        return name.toString();
    }

    public String getName() {
        return getName(null);
    }

    public java.util.Date getDate() {
        return m_dDate;
    }

    public void setDate(java.util.Date dDate) {
        m_dDate = dDate;
    }

    public UserInfo getUser() {
        return m_User;
    }

    public void setUser(UserInfo value) {
        m_User = value;
    }
    
    public String getPlace(){
                if(getProperty("customer")!=null){
                    Customer c= Util.gson2.fromJson(getProperty("customer"), Customer.class);
                    return c.getPlace();
                    }else{
                        return "na";
                    }
    }
    
    public CustomerInfoExt getCustomer()  { 
        return m_Customer;
        
    }
      
    public void setCustomer(CustomerInfoExt value) {
        m_Customer = value;
    }

      
    public String getCustomerId() {
        if (m_Customer == null) {
           return null;
        } else {
            return m_Customer.getId();
        }
    }
    
    public String getTransactionID(){
        return (getPayments().size()>0)
            ? ( getPayments().get(getPayments().size()-1) ).getTransactionID()
            : StringUtils.getCardNumber(); //random transaction ID
    }
    
    public String getReturnMessage(){
        return ( (getPayments().get(getPayments().size()-1)) instanceof PaymentInfoMagcard )
            ? ((PaymentInfoMagcard)(getPayments().get(getPayments().size()-1))).getReturnMessage()
            : LocalRes.getIntString("button.ok");
    }

    public void setActiveCash(String value) {
        m_sActiveCash = value;
    }

    public String getActiveCash() {
        return m_sActiveCash;
    }

    public String getProperty(String key) {
        return attributes.getProperty(key);
    }
    
        
    public String getProperty(String key, String defaultvalue) {
        return attributes.getProperty(key, defaultvalue);
    }
 
    public void setProperty(String key, String value) {
        attributes.setProperty(key, value);
    }

    public Properties getProperties() {
        return attributes;
    }

    public String getWaiter(){
     return attributes.getProperty("username")==null ? m_User.getName() : attributes.getProperty("username");
    }//@win
    
    public String getFloor(){
        return attributes.getProperty("floor")==null ? "na" : attributes.getProperty("floor");
    }
      
    
    public int getMale(){//@win
        if(getProperty("customer")!=null){
        Customer c= Util.gson2.fromJson(getProperty("customer"), Customer.class);
        return c.getMale();
        }else{
            return 0;
        }
    }
      
    public int getFemale(){//@win
        if(getProperty("customer")!=null){
        Customer c= Util.gson2.fromJson(getProperty("customer"), Customer.class);
        return c.getFemale();
        }else{
            return 0;
        }
    }
    
     public Date getCustomer_date() { //@win
        return customer_date;
    }

    public void setCustomer_date(Date customer_date) { //@win
        this.customer_date = customer_date;
    }
    
    public TicketLineInfo getLine(int index) {
        return m_aLines.get(index);
    }

    public void addLine(TicketLineInfo oLine) {

        oLine.setTicket(m_sId, m_aLines.size());
        m_aLines.add(oLine);
    }

    public void insertLine(int index, TicketLineInfo oLine) {
        m_aLines.add(index, oLine);
        refreshLines();
    }

    public void setLine(int index, TicketLineInfo oLine) {
        oLine.setTicket(m_sId, index);
        m_aLines.set(index, oLine);
    }

    public void removeLine(int index) {
        m_aLines.remove(index);
        refreshLines();
    }

    private void refreshLines() {
        for (int i = 0; i < m_aLines.size(); i++) {
            getLine(i).setTicket(m_sId, i);
        }
    }

    public int getLinesCount() {
        return m_aLines.size();
    }
    
    public double getArticlesCount() {
        double dArticles = 0.0;
        TicketLineInfo oLine;

        for (Iterator<TicketLineInfo> i = m_aLines.iterator(); i.hasNext();) {
            oLine = i.next();
            if(oLine.getProductID() != null)
                dArticles += oLine.getMultiply();
        }

        return dArticles;
    }

    public double getSubTotal() {
        double sum = 0.0;
        for (TicketLineInfo line : m_aLines) {
            if(line.getProductName().indexOf("Propina") < 0)
            sum += line.getSubValue();
        }
        
           DecimalFormat df = new DecimalFormat("#.##");
           DecimalFormatSymbols ds = new DecimalFormatSymbols();
           ds.setDecimalSeparator('.');
           ds.setGroupingSeparator(' ');
           df.setDecimalFormatSymbols(ds);
           df.setRoundingMode(RoundingMode.HALF_UP);   // define that we don´t need rounding 
        
       //    System.out.println (" ****  subtotal antes de df:"+ sum);
       // System.out.println (" ****  subtotal despues de df:"+ df.format(sum));
           
        return new Double(df.format(sum));
        //return sum;
    }
    
    public double getSubTotal2() {
        double sum = 0.0;
        for (TicketLineInfo line : m_aLines) {
            if(line.getProductName().indexOf("Propina") < 0)
            sum += line.getSubValue2();
        }
        
           DecimalFormat df = new DecimalFormat("#.##");
           DecimalFormatSymbols ds = new DecimalFormatSymbols();
           ds.setDecimalSeparator('.');
           ds.setGroupingSeparator(' ');
           df.setDecimalFormatSymbols(ds);
           df.setRoundingMode(RoundingMode.HALF_UP);   // define that we don´t need rounding 
        
       //    System.out.println (" ****  subtotal antes de df:"+ sum);
       // System.out.println (" ****  subtotal despues de df:"+ df.format(sum));
           
        return new Double(df.format(sum));
        //return sum;
    }
    
    public double getDiscounts(){
        double sum = 0.0;
        for (TicketLineInfo line : m_aLines) {
            if(line.getProductName().indexOf("Desc.") >= 0)
                sum += line.getSubValueWithDiscount();//@win
        }
        return sum;
    }
    
    public double getDiscounts2(){
        double sum = 0.0;
        for (TicketLineInfo line : m_aLines) {
            if(line.getProductName().indexOf("Desc.") >= 0)
                sum += line.getSubValueWithDiscount2();//@win
        }
        return sum;
    }
    
    /**
     * suma todas las propinas
     * @return total propina
     */
    public double getTip(){
        double sum = 0.0;
        for (TicketLineInfo line : m_aLines) {
            if(line.getProductName().indexOf("Propina") >= 0)
                sum += line.getSubValue();
        }
        return sum;
    }

    public double getTax() {

        double sum = 0.0;
        if(!isCustomerTaxExempt()){
            if (hasTaxesCalculated()) {
                for (TicketTaxInfo tax : taxes) {
                    sum += tax.getTax(); // Taxes are already rounded...
                }
            } else {
                for (TicketLineInfo line : m_aLines) {
                    if(line.getProductName().indexOf("Propina") < 0)
                        sum += line.getTax();
                }
            }
        }
        return sum;
    }
    
     public double getTax2() {
        double sum = 0.0;
        if(!isCustomerTaxExempt()){
            if (hasTaxesCalculated()) {
                for (TicketTaxInfo tax : taxes) {
                    sum += tax.getTax(); // Taxes are already rounded...
                }
            } else {
                for (TicketLineInfo line : m_aLines) {
                    if(line.getProductName().indexOf("Propina") < 0)
                        sum += line.getTax2();
                }
            }
        }
        return sum;
    }
    
    public boolean isCustomerTaxExempt(){
        if(m_Customer == null)
            return false; 
        else{
            return m_Customer.isTaxExempt();
        }
    }
    
    public double getTotal() {
        //return getSubTotal2() + getTax() + getService();
        //return getSubTotal() + getTax();// + getTip(); //@win
        return getTotal2();
    }
    
     //@win
    public double getServiceCharge(){
        return (getSubTotal2()*0.05);
    }
    
    //@win
    public double getTotal2() {
       // return getSubTotal() + getTax() + getService();
        return getSubTotal2()+getServiceCharge();// + getTip(); //@win
    }
   
    
    /**
     * Se agregaron nuevos campos, se modifico el conteo de articulos, y se mejoro el redeondero de decimales para calculo de totales y calculo de impuestos.
     * Added new fields, edit the count of items, and improved the redeondero of decimal  for calculation of totals and tax calculation
     * @return BigDecimal
     */
    public BigDecimal getTotalBigDecimal(){
        BigDecimal priceBD = new BigDecimal(getTotal());
        if(getProperty("discount-rate") != null && !getProperty("discount-rate").equalsIgnoreCase("")){
//            BigDecimal priceDiscountBD = new BigDecimal(getProperty("discount-value"));
//            priceBD = priceBD.add(priceDiscountBD);
            
            BigDecimal discountRateBD = new BigDecimal(getProperty("discount-rate"));
            BigDecimal priceDiscountBD = priceBD.multiply(discountRateBD);
            priceBD = priceBD.add(priceDiscountBD);
        }
        priceBD = priceBD.setScale(2,RoundingMode.HALF_UP);
        return priceBD;   
    }

    public BigDecimal getTotalBigDecimal2(){
        BigDecimal priceBD2 = new BigDecimal(getTotal2());
        if(getProperty("discount-rate") != null && !getProperty("discount-rate").equalsIgnoreCase("")){
//            BigDecimal priceDiscountBD = new BigDecimal(getProperty("discount-value"));
//            priceBD = priceBD.add(priceDiscountBD);
            
            BigDecimal discountRateBD2 = new BigDecimal(getProperty("discount-rate"));
            BigDecimal priceDiscountBD2 = priceBD2.multiply(discountRateBD2);
            priceBD2 = priceBD2.add(priceDiscountBD2);
        }
        priceBD2 = priceBD2.setScale(2,RoundingMode.HALF_UP);
        return priceBD2;   
    }
    
    public double getTotalPaid() {

        double sum = 0.0;
        for (PaymentInfo p : payments) {
            if (!"debtpaid".equals(p.getName())) {
                sum += p.getTotal();
            }
        }
        return sum;
    }
    
    public boolean isThereATip(){
        boolean isThereATip = false;

        for (TicketLineInfo line : m_aLines) {
            if(line.getProductName().indexOf("Propina") >= 0){
                isThereATip = true;
                break;
            } 
           
        }
        
        return isThereATip;
    }

    public List<TicketLineInfo> getLines() {
        return m_aLines;
    }

    public void setLines(List<TicketLineInfo> l) {
        m_aLines = l;
    }

    public List<PaymentInfo> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentInfo> l) {
        payments = l;
    }

    public void resetPayments() {
        payments = new ArrayList<PaymentInfo>();
    }

    public List<TicketTaxInfo> getTaxes() {
        return taxes;
    }

    public boolean hasTaxesCalculated() {
        return taxes != null;
    }

    public void setTaxes(List<TicketTaxInfo> l) {
        taxes = l;
    }

    public void resetTaxes() {
        taxes = null;
    }

    public TicketTaxInfo getTaxLine(TaxInfo tax) {

        for (TicketTaxInfo taxline : taxes) {
            if (tax.getId().equals(taxline.getTaxInfo().getId())) {
                return taxline;
            }
        }

        return new TicketTaxInfo(tax);
    }

    public TicketTaxInfo[] getTaxLines() {

        Map<String, TicketTaxInfo> m = new HashMap<String, TicketTaxInfo>();

        TicketLineInfo oLine;
        for (Iterator<TicketLineInfo> i = m_aLines.iterator(); i.hasNext();) {
            oLine = i.next();

            TicketTaxInfo t = m.get(oLine.getTaxInfo().getId());
            if (t == null) {
                t = new TicketTaxInfo(oLine.getTaxInfo());
                m.put(t.getTaxInfo().getId(), t);
            }
            t.add(oLine.getSubValue());
        }

        // return dSuma;       
        Collection<TicketTaxInfo> avalues = m.values();
        return avalues.toArray(new TicketTaxInfo[avalues.size()]);
    }

    public String printId() {
        if (m_iTicketId > 0) {
            // valid ticket id
            return Formats.INT.formatValue(new Integer(m_iTicketId));
        } else {
            return "";
        }
    }

    public String printDate() {
        return Formats.TIMESTAMP.formatValue(m_dDate);
    }

    public String printUser() {
        return m_User == null ? "" : m_User.getName();
    }
    
    public String printFloor(){
        return attributes.getProperty("floor")== null ? "" :attributes.getProperty("floor");
    }//@win
    
    public String printWaiter(){
        return attributes.getProperty("username")== null ? "" :attributes.getProperty("username");
    }//@win

    public String printCustomer() {
        return m_Customer == null ? "" : m_Customer.getName();
    }

    public String printArticlesCount() {
        return Formats.DOUBLE.formatValue(new Double(getArticlesCount()));
    }

    public String printSubTotal() {
        return Formats.CURRENCY.formatValue(new Double(getSubTotal()));
    }

    public String printSubTotal2() {
        return Formats.CURRENCY.formatValue(new Double(getSubTotal2()));
    }//@Win
    
    public String printTax() {
        return Formats.CURRENCY.formatValue(new Double(getTax()));
    }
    
    public String printTax2() {
        return Formats.CURRENCY.formatValue(new Double(getTax2()));
    }

    //@win
    public String printServiceCharge(){
        return Formats.CURRENCY.formatValue(new Double(getServiceCharge()));
    }
    
    public String printTotal() {
        return Formats.CURRENCY.formatValue(new Double(getTotal()));
    }
    
    public String printTotal2() {
        return Formats.CURRENCY.formatValue(new Double(getTotal2()));
    }
    
    public String printTip(){
        return Formats.CURRENCY.formatValue(new Double(getTip()));
    }
    
    public String printDiscounts(){
        return Formats.CURRENCY.formatValue(new Double(getDiscounts2()));
    }

    public String printTotalPaid() {
        return Formats.CURRENCY.formatValue(new Double(getTotalPaid()));
    }
    
    /********************************************************/
    public boolean isSendAllArticles(){
        for(TicketLineInfo ticketLineInfo : m_aLines){
            if(!ticketLineInfo.getProperty("sendstatus").equals("Yes"))
                return false;
        }
        return true;
    }
    
    public void recalculateTip(){
        List<TicketLineInfo> infos = new ArrayList<TicketLineInfo>();
        double rate =0.0;
        double subTotal = 0.0;
        if(this.getRefundticketId() != 0)
            return;
        
        for(TicketLineInfo ti : m_aLines){
            if(ti.getProductName().indexOf("Propina") >=0){
                if(!ti.getProperty("tip").equals("0"))
                    infos.add(ti);
            }
        }
        
        subTotal = getSubTotal();
        
        for(TicketLineInfo tli: infos){
            try {  
                rate = (Double)Formats.DOUBLE.parseValue(tli.getProperty("tip"));
                rate = rate /100;
            } catch (BasicException ex) {
                Logger.getLogger(TicketInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            tli.setPrice(subTotal * rate); 
        }
    }
    
    
    public double getService() {
        double sum = 0.0;
        for (TicketLineInfo line : m_aLines) {
            if(line.getProductName().indexOf("Propina") >= 0)
            sum += line.getSubValue();
        }
        return sum;
    }
    
    public void recalculateDiscount(){
        List<TicketLineInfo> infos = new ArrayList<TicketLineInfo>();
        double rate =0.0;
        int index =0;
        TicketLineInfo tliAux = null;
        
        if(this.getRefundticketId() != 0)
            return;
        
        for(TicketLineInfo ti : m_aLines){
            if(ti.getProductName().indexOf("Desc.") >=0){
                infos.add(ti);
            }
        }
           
        for(TicketLineInfo tli: infos){
            try {  
                rate = (Double)Formats.DOUBLE.parseValue(tli.getProperty("discount"));
                rate = rate /100;
            } catch (BasicException ex) {
                Logger.getLogger(TicketInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            index = m_aLines.indexOf(tli);
            
            if(index>0){
                tliAux = m_aLines.get(index-1);
                //tli.setPrice(tliAux.getPrice() * tliAux.getMultiply() * rate); //@win
                tli.setPrice(tliAux.getPrice2() * tliAux.getMultiply() * rate);
            }
            
        }
    }

    public int getRefundticketId() {
        return refundticketId;
    }

    public void setRefundticketId(int refundticketId) {
        this.refundticketId = refundticketId;
    }
    
    public void setGlobalTicket(float rateFlo, String tcId,String notes){
        double rate = (double)rateFlo;
        Map<TicketLineInfo, TicketLineInfo> map = new HashMap<TicketLineInfo, TicketLineInfo>();

        if(getTaxLines().length >0 ){
            for(TicketLineInfo line : m_aLines){
                line.setProperty("discount-rate-global",Double.toString(rate));
                String discountRateStr = line.getProperty("discount-rate");
                
                if(line.getProductID() == null || line.getProductID().trim().equals(""))
                    continue;
                
                if(discountRateStr != null && !discountRateStr.equals(""))
                    continue;
                
                //double totalP = -line.getPrice() * rate *line.getMultiply(); //@win
                double totalP = -line.getPrice2() * rate *line.getMultiply();
                line.setProperty("discount-value",Double.toString(totalP));
                line.setProperty("discount-rate",Double.toString(rate));
                
                TicketLineInfo tlineInfo = new TicketLineInfo(
                        "Desc. global" + Formats.PERCENT.formatValue(rate), // + " de " + line.getProductName(),   
                        tcId,          
                        1.0, 
                        totalP,
                        getTaxLines()[0].getTaxInfo());
            
                tlineInfo.setProperty("discount-rate",Double.toString(rate));
                tlineInfo.setProperty("sendstatus","Yes");
                tlineInfo.setProperty("notes",notes);
                          
                map.put(line, tlineInfo);
            }
            
            for(Map.Entry<TicketLineInfo, TicketLineInfo> ent: map.entrySet() ){
                int index = m_aLines.indexOf(ent.getKey());
                this.insertLine(index +1, ent.getValue());
            }
            
            this.recalculateTip();
        }
    }
    
    public void removeGlobalDiscount(){
        List<TicketLineInfo> lineInfos = new ArrayList<TicketLineInfo>();
        for(TicketLineInfo line : m_aLines){
            
            int index = m_aLines.indexOf(line);
            if(line.getProductName().indexOf("Desc. global") >= 0){
                if(index > 0){
                    TicketLineInfo lineAux = this.getLine(index -1);
                    lineAux.setProperty("discount-value","");
                    lineAux.setProperty("discount-rate","");
                }
                lineInfos.add(line);
           }
        }
        
        for(TicketLineInfo line : lineInfos){
            this.removeLine(m_aLines.indexOf(line));
        }
    }

    /**
     * activa forma de pago free
     * free payform activate
     * @return boolean
     */
    public boolean isPaymentFree() {
        for(PaymentInfo p: payments){
            if(p.getName().trim().equalsIgnoreCase("free")){
                return true;
            }
        }
        return false;
    }
    
    /**
     * mensaje de anulacion
     * void message
     * @return 
     */
    public boolean isTicketVoid() {
        if((payments == null ||payments.size() ==0) && (taxes == null || taxes.size() ==0) && (m_aLines == null || m_aLines.size()==0))
            return true;
        else
            return false;
                   
    }

    
    public InsuranceInfo getiInfo() {
        return iInfo;
    }

    public void setiInfo(InsuranceInfo iInfo) {
        this.iInfo = iInfo;
    }

    public String getCopay() {
        return copay;
    }

    public void setCopay(String copay) {
        this.copay = copay;
    }

    public String getCoveredInsurance() {
        return coveredInsurance;
    }

    public void setCoveredInsurance(String coveredInsurance) {
        this.coveredInsurance = coveredInsurance;
    }
        
    
    
    /********************************************************/
    
                       
}
