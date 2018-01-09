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

package com.openbravo.pos.panels;

import java.util.*;
import javax.swing.table.AbstractTableModel;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.*;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.util.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 *
 * @author adrianromero
 */
public class PaymentsModel {

    private String m_sHost;
    private int m_iSeq;
    private Date m_dDateStart;
    private Date m_dDateEnd;       
            
    private Integer m_iPayments;
    private Double m_dPaymentsTotal;
    private java.util.List<PaymentsLine> m_lpayments;
    
    // smj
    private Integer m_iProductSalesRows;
    private Double m_dProductSalesTotalUnits;
    private Double m_dProductSalesTotal;
    private Double m_dProductAllSalesTotal;
    private Double m_dProductFoodCost;
    private Double m_dProductBeverageCost;
    private Double m_dProductOthersCost;
    
    private Integer m_firstTicketId;
    private Integer m_lastTicketId;
    private Integer m_TicketCount;
    private Integer m_iServices;
    private Double m_dServices;
    //smj
    
    private java.util.List<ProductSalesLine> m_lproductsales;
    
    private java.util.List<TopSalesLine> m_ltopsales;
    
    
    private final static String[] PAYMENTHEADERS = {"Label.Payment", "label.totalcash"};
    
    private Integer m_iSales;
    private Double m_dSalesBase;
    private Double m_dSalesTaxes;
    private java.util.List<SalesLine> m_lsales;
    private Integer m_irefund;
    private Double m_iDiscount;
    
    private final static String[] SALEHEADERS = {"label.taxcash", "label.totalcash"};
    private Double m_dTotalTopProducts;
    
    
    

    private PaymentsModel() {
    }    
    
    public static PaymentsModel emptyInstance() {
        
        PaymentsModel p = new PaymentsModel();
        
        p.m_iPayments = new Integer(0);
        p.m_dPaymentsTotal = new Double(0.0);
        p.m_lpayments = new ArrayList<PaymentsLine>();
        
        p.m_iSales = null;
        p.m_dSalesBase = null;
        p.m_dSalesTaxes = null;
        p.m_lsales = new ArrayList<SalesLine>();
        
        // smj
         p.m_lpayments = new ArrayList<PaymentsLine>();
 
        p.m_iProductSalesRows = new Integer(0);
        p.m_dProductSalesTotalUnits = new Double(0.0);
        p.m_dProductSalesTotal = new Double(0.0);
        p.m_dProductAllSalesTotal = new Double(0.0);//@win
        p.m_dTotalTopProducts = new Double(0.0); //@win
        
        p.m_lproductsales = new ArrayList<ProductSalesLine>();
        p.m_ltopsales = new ArrayList<TopSalesLine>();//@win
        
        p.m_dProductFoodCost = new Double(0.0);
        p.m_dProductBeverageCost = new Double(0.0);
        p.m_dProductOthersCost = new Double(0.0);//@win
        
        p.m_iSales = null;
        p.m_irefund = null;
        //  smj
        
        
        return p;
    }
    
    /**
     * modificacion para guardar las vueltas en los pagos y para guardar informacion de las tarjetas de credito
     * change for save payment return and credit card information
     * @param app
     * @return PaymentsModel
     * @throws BasicException 
     */
    public static PaymentsModel loadInstance(AppView app) throws BasicException {
        
        PaymentsModel p = new PaymentsModel();
        
        // Propiedades globales
        p.m_sHost = app.getProperties().getHost();
        p.m_iSeq = app.getActiveCashSequence();
        p.m_dDateStart = app.getActiveCashDateStart();
        p.m_dDateEnd = null;
        
        
        // Pagos
        Object[] valtickets = (Object []) new StaticSentence(app.getSession()
            , "SELECT COUNT(*), SUM(PAYMENTS.TOTAL) " +
              "FROM PAYMENTS, RECEIPTS " +
              "WHERE PAYMENTS.RECEIPT = RECEIPTS.ID AND RECEIPTS.MONEY = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
            .find(app.getActiveCashIndex());

            
        if (valtickets == null) {
            p.m_iPayments = new Integer(0);
            p.m_dPaymentsTotal = new Double(0.0);
        } else {
            p.m_iPayments = (Integer) valtickets[0];
            p.m_dPaymentsTotal = (Double) valtickets[1];
        }  
        
        /***********************************************************/
//        // tickets
        List ticketsInfo =  new StaticSentence(app.getSession()
            , "SELECT tickets.ticketid " +
              "FROM receipts, tickets " +
              "WHERE   tickets.id = receipts.id AND receipts.money = ? "+
              "order by tickets.ticketid asc "
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.INT}))
            .list(app.getActiveCashIndex());
////
        if (ticketsInfo == null || ticketsInfo.size() ==0) {
            p.m_firstTicketId = new Integer(0);
            p.m_lastTicketId = new Integer(0);
            p.m_TicketCount = new Integer(0);
        } else {
            p.m_firstTicketId = (Integer) ((Object [])ticketsInfo.get(0))[0];
            p.m_lastTicketId = (Integer) ((Object [])ticketsInfo.get(ticketsInfo.size() - 1))[0];
            p.m_TicketCount = ticketsInfo.size();
        }
//        
        /***********************************************************/
        
        
        
        //@win
        List l = new StaticSentence(app.getSession()            
            , "SELECT PAYMENTS.PAYMENT, SUM(PAYMENTS.TOTAL),CARDTYPE, NOTES " +
              "FROM PAYMENTS, RECEIPTS " +
              "WHERE PAYMENTS.RECEIPT = RECEIPTS.ID AND RECEIPTS.MONEY = ? " +
              "GROUP BY PAYMENTS.PAYMENT, CARDTYPE, NOTES "+
              "ORDER BY PAYMENTS.PAYMENT"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(PaymentsModel.PaymentsLine.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
            .list(app.getActiveCashIndex()); 
        
        if (l == null) {
            p.m_lpayments = new ArrayList();
        } else {
            p.m_lpayments = l;
        }        
        
        
        /******************************************************************/
        
        // Services
        Object[] recService = (Object []) new StaticSentence(app.getSession(),
            "SELECT COUNT(DISTINCT RECEIPTS.ID), SUM(TICKETLINES.UNITS * TICKETLINES.PRICE) " +
            "FROM RECEIPTS, TICKETLINES WHERE RECEIPTS.ID = TICKETLINES.TICKET AND TICKETLINES.PRODUCT is null AND TICKETLINES.PRICE > 0 AND RECEIPTS.MONEY = ?",
            SerializerWriteString.INSTANCE,
            new SerializerReadBasic(new Datas[] {Datas.INT,Datas.DOUBLE}))
            .find(app.getActiveCashIndex());
        if (recService == null) {
            p.m_iServices = null;
            p.m_dServices = 0.0;
        } else {
            p.m_iServices = (Integer) recService[0];
            p.m_dServices = (Double) recService[1];
            if(p.m_dServices == null)
                p.m_dServices =0.0;
        } 
        
        
        /******************************************************************/
        
        // Sales
        Object[] recsales = (Object []) new StaticSentence(app.getSession(),
            "SELECT COUNT(DISTINCT RECEIPTS.ID), SUM(TICKETLINES.UNITS * (TICKETLINES.PRICE/1.1)) " +
            "FROM RECEIPTS, TICKETLINES WHERE RECEIPTS.ID = TICKETLINES.TICKET AND RECEIPTS.MONEY = ?",
            SerializerWriteString.INSTANCE,
            new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
            .find(app.getActiveCashIndex());
        if (recsales == null || recsales.length == 0) {
            p.m_iSales = null;
            p.m_dSalesBase = null;
        } else {
            p.m_iSales = (Integer) recsales[0];
            if(recsales[1] != null)
                p.m_dSalesBase = (Double) recsales[1] -  p.m_dServices;
            else
                p.m_dSalesBase = 0.0;
        }             
        
        //refound
        Object[] recRefound = (Object []) new StaticSentence(app.getSession(),
            " SELECT COUNT(DISTINCT RECEIPTS.ID) " +
            " FROM RECEIPTS, TICKETS WHERE TICKETS.ID = RECEIPTS.ID AND TICKETS.TICKETTYPE = 1 AND RECEIPTS.MONEY = ?",
            SerializerWriteString.INSTANCE,
            new SerializerReadBasic(new Datas[] {Datas.INT}))
            .find(app.getActiveCashIndex());
        if (recRefound == null || recRefound.length == 0) {
            p.m_irefund = null;
        } else {
            p.m_irefund = (Integer) recRefound[0];
        }
        
        //Discount
        Object[] recDiscount = (Object []) new StaticSentence(app.getSession(),
            "SELECT COUNT(DISTINCT RECEIPTS.ID), SUM(TICKETLINES.UNITS * TICKETLINES.PRICE) " +
            "FROM RECEIPTS, TICKETLINES WHERE RECEIPTS.ID = TICKETLINES.TICKET AND TICKETLINES.PRODUCT is null AND TICKETLINES.PRICE < 0 AND RECEIPTS.MONEY = ? ",
            SerializerWriteString.INSTANCE,
            new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE}))
            .find(app.getActiveCashIndex());
        if (recDiscount == null || recDiscount.length == 0) {
            p.m_iDiscount = null;
        } else {
            p.m_iDiscount = (Double) recDiscount[1];
        }
        
        
        
        // Taxes
        Object[] rectaxes = (Object []) new StaticSentence(app.getSession(),
            "SELECT SUM(TAXLINES.AMOUNT) " +
            "FROM RECEIPTS, TAXLINES WHERE RECEIPTS.ID = TAXLINES.RECEIPT AND RECEIPTS.MONEY = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.DOUBLE}))
            .find(app.getActiveCashIndex());            
        if (rectaxes == null || rectaxes.length ==0) {
            p.m_dSalesTaxes = null;
        } else {
            p.m_dSalesTaxes = (Double) rectaxes[0];
        } 
                
        List<SalesLine> asales = new StaticSentence(app.getSession(),
                "SELECT TAXCATEGORIES.NAME, SUM(TAXLINES.AMOUNT) " +
                "FROM RECEIPTS, TAXLINES, TAXES, TAXCATEGORIES WHERE RECEIPTS.ID = TAXLINES.RECEIPT AND TAXLINES.TAXID = TAXES.ID AND TAXES.CATEGORY = TAXCATEGORIES.ID " +
                "AND RECEIPTS.MONEY = ?" +
                "GROUP BY TAXCATEGORIES.NAME"
                , SerializerWriteString.INSTANCE
                , new SerializerReadClass(PaymentsModel.SalesLine.class))
                .list(app.getActiveCashIndex());
        if (asales == null || asales.size() == 0) {
            p.m_lsales = new ArrayList<SalesLine>();
        } else {
            p.m_lsales = asales;
        }
        // smj
       // Product Sales
        Object[] valproductsales = (Object []) new StaticSentence(app.getSession()
             /*, "SELECT COUNT(*), SUM(TICKETLINES.UNITS), SUM((TICKETLINES.PRICE + TICKETLINES.PRICE * TAXES.RATE ) * TICKETLINES.UNITS) " +
              "FROM TICKETLINES, TICKETS, RECEIPTS, TAXES " +
              "WHERE TICKETLINES.TICKET = TICKETS.ID AND TICKETS.ID = RECEIPTS.ID AND TICKETLINES.TAXID = TAXES.ID AND TICKETLINES.PRODUCT IS NOT NULL AND RECEIPTS.MONEY = ? " +
              "GROUP BY RECEIPTS.MONEY" */
            , "SELECT COUNT(*), SUM(TICKETLINES.UNITS), SUM((TICKETLINES.PRICE) * TICKETLINES.UNITS) " +
              "FROM TICKETLINES, TICKETS, RECEIPTS, TAXES " +
              "WHERE TICKETLINES.TICKET = TICKETS.ID AND TICKETS.ID = RECEIPTS.ID AND TICKETLINES.TAXID = TAXES.ID AND TICKETLINES.PRODUCT IS NOT NULL AND RECEIPTS.MONEY = ? " +
              "GROUP BY RECEIPTS.MONEY"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.INT, Datas.DOUBLE, Datas.DOUBLE}))
            .find(app.getActiveCashIndex());
 
        if (valproductsales == null || valproductsales.length ==0) {
            p.m_iProductSalesRows = new Integer(0);
            p.m_dProductSalesTotalUnits = new Double(0.0);
            p.m_dProductSalesTotal = new Double(0.0);
            p.m_dProductAllSalesTotal = new Double(0.0);
        } else {
            p.m_iProductSalesRows = (Integer) valproductsales[0];
            p.m_dProductSalesTotalUnits = (Double) valproductsales[1];
            p.m_dProductSalesTotal= (Double) valproductsales[2];
            p.m_dProductAllSalesTotal= (Double) valproductsales[2];
        }
        
        List products = new StaticSentence(app.getSession()
//            , "SELECT PRODUCTS.NAME, SUM(TICKETLINES.UNITS), TICKETLINES.PRICE, TAXES.RATE " +
//              "FROM TICKETLINES, TICKETS, RECEIPTS, PRODUCTS, TAXES " +
//              "WHERE TICKETLINES.PRODUCT = PRODUCTS.ID AND TICKETLINES.TICKET = TICKETS.ID AND TICKETS.ID = RECEIPTS.ID AND TICKETLINES.TAXID = TAXES.ID AND RECEIPTS.MONEY = ? " 
////              " AND TICKETLINES.ATTRIBUTES NOT LIKE '%discount-rate%'"+
//               + "GROUP BY PRODUCTS.NAME, TICKETLINES.PRICE, TAXES.RATE"
                
            , "SELECT PRODUCTS.NAME, TICKETLINES.UNITS, TICKETLINES.PRICE, TAXES.RATE,  TICKETLINES.ATTRIBUTES " +
              "FROM TICKETLINES, TICKETS, RECEIPTS, PRODUCTS, TAXES " +
              "WHERE TICKETLINES.PRODUCT = PRODUCTS.ID AND TICKETLINES.TICKET = TICKETS.ID AND TICKETS.ID = RECEIPTS.ID AND TICKETLINES.TAXID = TAXES.ID AND RECEIPTS.MONEY = ? " 
               +"GROUP BY PRODUCTS.NAME, TICKETLINES.UNITS, TICKETLINES.PRICE, TAXES.RATE,  TICKETLINES.ATTRIBUTES " //@win
               +"ORDER BY sum(TICKETLINES.UNITS*TICKETLINES.PRICE) DESC,PRODUCTS.NAME, TICKETLINES.PRICE, TAXES.RATE, TICKETLINES.ATTRIBUTES  "
                + "LIMIT 10"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(PaymentsModel.ProductSalesLine.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
            .list(app.getActiveCashIndex());
 
        if (products == null || products.size() ==0) {
            p.m_lproductsales = new ArrayList();
        } else {
            Iterator<PaymentsModel.ProductSalesLine> iter = products.iterator();
            String name = "";
            double price = 0;
            double rate = 0;
            double discountRate = -99;
            double discountAux = 0;
            String discountAuxStr = "";
            PaymentsModel.ProductSalesLine lineAux = null;
            List products2 = new ArrayList();
            double totalPrice =0;
            
            while(iter.hasNext()){
                PaymentsModel.ProductSalesLine line = iter.next();
                discountAuxStr =line.getProperty("discount-rate");
                if(discountAuxStr != null && !discountAuxStr.equals(""))
                    discountAux = Double.parseDouble(discountAuxStr);
                else
                    discountAux = 0;
                
                if(!name.equalsIgnoreCase(line.m_ProductName) 
                        || price != line.m_ProductPrice 
                        || rate != line.m_TaxRate 
                        || discountRate != discountAux ){
                    name = line.m_ProductName;
                    price = line.m_ProductPrice ;
                    rate = line.m_TaxRate;
                    discountRate = discountAux;
                    lineAux = new ProductSalesLine();
                    lineAux.m_ProductName = name;
                    lineAux.m_ProductPrice = price * (1 - discountRate);
                    lineAux.m_TaxRate = rate;
                    lineAux.m_ProductUnits = line.m_ProductUnits;
                    products2.add(lineAux);
                }else{
                    lineAux.m_ProductUnits += line.m_ProductUnits;
                }
                lineAux.m_ProductPriceTax = lineAux.m_ProductPrice;// + lineAux.m_ProductPrice*lineAux.m_TaxRate; @win
                totalPrice += lineAux.m_ProductPriceTax * line.m_ProductUnits;
            }
            
            p.m_dProductSalesTotal = totalPrice;
            p.m_lproductsales = products2;
        }
        
        List topproducts = new StaticSentence(app.getSession()
                ,"SELECT PRODUCTS.NAME, SUM(TICKETLINES.UNITS) AS units, TICKETLINES.PRICE, SUM(TICKETLINES.UNITS*TICKETLINES.PRICE) as total " +
"FROM TICKETLINES, TICKETS, RECEIPTS, PRODUCTS " +
"WHERE TICKETLINES.PRODUCT = PRODUCTS.ID " +
"AND TICKETLINES.TICKET = TICKETS.ID " +
"AND TICKETS.ID = RECEIPTS.ID " +
"AND RECEIPTS.MONEY = ? " +
"GROUP BY PRODUCTS.NAME, TICKETLINES.PRICE " +
"ORDER BY sum(TICKETLINES.UNITS*TICKETLINES.PRICE) DESC,PRODUCTS.NAME " +
"LIMIT 10 "
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(PaymentsModel.TopSalesLine.class)) //new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.DOUBLE}))
            .list(app.getActiveCashIndex());
        if (topproducts == null) {
            p.m_ltopsales = new ArrayList();
        } else {
            Iterator<PaymentsModel.TopSalesLine> iter = topproducts.iterator();
            String name = "";
            double price = 0;
            double totaltopprice = 0;
            PaymentsModel.TopSalesLine lineAux = null;
            List topproducts2 = new ArrayList();
            
            while(iter.hasNext()){
            PaymentsModel.TopSalesLine line = iter.next();
            
            if(!name.equalsIgnoreCase(line.m_TopProductName) 
                        || price != line.m_TopProductPrice )
                        //|| rate != line.m_TaxRate 
                        //|| discountRate != discountAux ){
                    {name = line.m_TopProductName;
                    price = line.m_TopProductPrice ;
                    //rate = line.m_TaxRate;
                    //discountRate = discountAux;
                    lineAux = new TopSalesLine();
                    lineAux.m_TopProductName = name;
                    lineAux.m_TopProductPrice = price;//= price * (1 - discountRate);
                    //lineAux.m_TaxRate = rate;
                    lineAux.m_TopProductUnits = line.m_TopProductUnits;
                    topproducts2.add(lineAux);
                }else{
                    lineAux.m_TopProductUnits += line.m_TopProductUnits;
                }
                //lineAux.m_ProductPriceTax = lineAux.m_TopProductPrice;// + lineAux.m_ProductPrice*lineAux.m_TaxRate; @win
                totaltopprice += lineAux.m_TopProductPrice * lineAux.m_TopProductUnits;
            }
            p.m_dTotalTopProducts = totaltopprice;
            p.m_ltopsales = topproducts2;
        }
 /*      
        Object[] totaltopproducts = (Object []) new StaticSentence(app.getSession()
                ,"SELECT SUM(TICKETLINES.UNITS*TICKETLINES.PRICE) as total " +
"FROM TICKETLINES, TICKETS, RECEIPTS, PRODUCTS " +
"WHERE TICKETLINES.PRODUCT = PRODUCTS.ID AND " +
"TICKETLINES.TICKET = TICKETS.ID  " +
"AND TICKETS.ID = RECEIPTS.ID " +
"AND RECEIPTS.MONEY = ? " +
"LIMIT 10 "
         , SerializerWriteString.INSTANCE
         , new SerializerReadBasic(new Datas[] {Datas.DOUBLE}))
                .find(app.getActiveCashIndex());
        
        if(totaltopproducts ==null || totaltopproducts.length == 0) {
            p.m_dTotalTopProducts = new Double(0.0);
        }else{
            p.m_dTotalTopProducts = (Double) totaltopproducts[0];
        }
*/        
        Object[] valproductcost = (Object []) new StaticSentence(app.getSession()
             , "SELECT SUM((e.PRICELIMIT) * A.UNITS) as foodcost " +
", SUM((f.PRICELIMIT) * A.UNITS) as beveragecost " +
", SUM((g.PRICELIMIT) * A.UNITS) as otherscost " +
"FROM TICKETLINES A " +
"left join TICKETS B ON A.TICKET = B.ID " +
"left join RECEIPTS C ON B.ID = C.ID " +
"left join TAXES D ON A.TAXID = D.ID " +
"left join VW_PRODUCTS_BARKIT E ON A.PRODUCT = E.ID " +
"left join VW_PRODUCTS_BARBARATTIMUR F ON A.PRODUCT = F.ID " +
"left join VW_PRODUCTS_BARSTO G ON A.PRODUCT = G.ID " +
"WHERE A.PRODUCT IS NOT NULL " +
"AND c.MONEY = ? " +
"GROUP BY c.MONEY"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.DOUBLE, Datas.DOUBLE, Datas.DOUBLE}))
            .find(app.getActiveCashIndex());
 
        if (valproductcost == null || valproductcost.length ==0) {
            p.m_dProductFoodCost = new Double(0.0);
            p.m_dProductBeverageCost = new Double(0.0);
            p.m_dProductOthersCost = new Double(0.0);
        } else {
            p.m_dProductFoodCost = (Double) valproductcost[0];
            p.m_dProductBeverageCost = (Double) valproductcost[1];
            p.m_dProductOthersCost= (Double) valproductcost[2];
        }
        
        //smj
 
        return p;
    }
    
    

    
        public double getProductSalesRows() {
        return m_iProductSalesRows.intValue();
    }
 
    public String printProductSalesRows() {
        return Formats.INT.formatValue(m_iProductSalesRows);
    }

    public double getProductFoodCost(){
        return m_dProductFoodCost.doubleValue();
    }
    
    //@win
    public String printProductFoodCost(){
        return Formats.CURRENCY.formatValue(m_dProductFoodCost);
    }
    
    public double getProductBeverageCost(){
        return m_dProductBeverageCost.doubleValue();
    }
    
    public String printProductBeverageCost(){
        return Formats.CURRENCY.formatValue(m_dProductBeverageCost);
    }
    
    public double getProductOthersCost(){
        return m_dProductOthersCost.doubleValue();
    }
    
    public String printProductOthersCost(){
        return Formats.CURRENCY.formatValue(m_dProductOthersCost);
    }
    
    public double getProductSalesTotalUnits() {
        return m_dProductSalesTotalUnits.doubleValue();
    }
 
    public String printProductSalesTotalUnits() {
        return Formats.DOUBLE.formatValue(m_dProductSalesTotalUnits);
    }
 
    public double getProductSalesTotal() {
        return m_dProductSalesTotal.doubleValue();
    }
 
    public String printProductSalesTotal() {
        return Formats.CURRENCY.formatValue(m_dProductSalesTotal);
    }
    
    public double getProductAllSalesTotal() {
        return m_dProductAllSalesTotal.doubleValue();
    }
    
    public String printProductAllSalesTotal() {
        return Formats.CURRENCY.formatValue(m_dProductAllSalesTotal);
    }
    
     public double getAllTopProductTotal() {
        return m_dTotalTopProducts.doubleValue();
    }
    
    public String printAllTopProductTotal() {
        return Formats.CURRENCY.formatValue(m_dTotalTopProducts);
    }
    
    public String printPercentAllTopTotal() {
        //double m_PercentAllTop= getAllTopProductTotal()/getProductAllSalesTotal();
        return Formats.PERCENT.formatValue(m_dTotalTopProducts/m_dProductAllSalesTotal);
    }
    
    public String printAllCost(){        
        return Formats.CURRENCY.formatValue(
                (m_dProductBeverageCost != null && m_dProductFoodCost == null && m_dProductOthersCost == null ) ?  m_dProductBeverageCost :
                  (m_dProductBeverageCost == null && m_dProductFoodCost != null && m_dProductOthersCost == null ) ? m_dProductFoodCost :
                  (m_dProductBeverageCost == null && m_dProductFoodCost == null && m_dProductOthersCost != null ) ? m_dProductOthersCost :
                (m_dProductBeverageCost != null && m_dProductFoodCost != null && m_dProductOthersCost == null ) ? m_dProductBeverageCost + m_dProductFoodCost :
                (m_dProductBeverageCost == null && m_dProductFoodCost != null && m_dProductOthersCost != null ) ? m_dProductFoodCost + m_dProductOthersCost :
                m_dProductBeverageCost + m_dProductFoodCost + m_dProductOthersCost
                );
    }
 
    public List<ProductSalesLine> getProductSalesLines() {
        return m_lproductsales;
    }
    
    public List<TopSalesLine> getTopSalesLines() {
        return m_ltopsales;
    }
    
    //@win
    public String printPercentAllCost(){
      return Formats.PERCENT.formatValue(
      (m_dProductBeverageCost != null && m_dProductFoodCost == null && m_dProductOthersCost == null ) ?  m_dProductBeverageCost/m_dProductAllSalesTotal :
                  (m_dProductBeverageCost == null && m_dProductFoodCost != null && m_dProductOthersCost == null ) ? m_dProductFoodCost/m_dProductAllSalesTotal :
                  (m_dProductBeverageCost == null && m_dProductFoodCost == null && m_dProductOthersCost != null ) ? m_dProductOthersCost/m_dProductAllSalesTotal :
                (m_dProductBeverageCost != null && m_dProductFoodCost != null && m_dProductOthersCost == null ) ? (m_dProductBeverageCost + m_dProductFoodCost)/m_dProductAllSalesTotal :
                (m_dProductBeverageCost == null && m_dProductFoodCost != null && m_dProductOthersCost != null ) ? (m_dProductFoodCost + m_dProductOthersCost)/m_dProductAllSalesTotal :
                (m_dProductBeverageCost + m_dProductFoodCost + m_dProductOthersCost)/m_dProductAllSalesTotal
              
              );
    }
   
    public String printPercentFoodCost(){
      return Formats.PERCENT.formatValue((m_dProductFoodCost == null) ? null : m_dProductFoodCost/m_dProductAllSalesTotal);
    }
    
    public String printPercentBevCost(){
      return Formats.PERCENT.formatValue((m_dProductBeverageCost == null) ? null : m_dProductBeverageCost/m_dProductAllSalesTotal);
    }
    
    public String printPercentOthersCost(){
      return Formats.PERCENT.formatValue(( m_dProductOthersCost == null)? null : m_dProductOthersCost/m_dProductAllSalesTotal);
    }
    
    /*********************************************/
    public int getFirstTicketId(){
        return m_firstTicketId;
    }
    
    public int getLastTicketId(){
        return m_lastTicketId;
    }
    
    public Integer getTicketCount(){
        return m_TicketCount;
    }
    /*******************************************/
    public int getPayments() {
        return m_iPayments.intValue();
    }
    public double getTotal() {
        return m_dPaymentsTotal.doubleValue();
    }
    public String getHost() {
        return m_sHost;
    }
    public int getSequence() {
        return m_iSeq;
    }
    public Date getDateStart() {
        return m_dDateStart;
    }
    public void setDateEnd(Date dValue) {
        m_dDateEnd = dValue;
    }
    public Date getDateEnd() {
        return m_dDateEnd;
    }
    
    public String printHost() {
        return StringUtils.encodeXML(m_sHost);
    }
    public String printSequence() {
        return Formats.INT.formatValue(m_iSeq);
    }
    public String printDateStart() {
        return Formats.TIMESTAMP.formatValue(m_dDateStart);
    }
    public String printDateEnd() {
        return Formats.TIMESTAMP.formatValue(m_dDateEnd);
    }  
    
    public String printPayments() {
        return Formats.INT.formatValue(m_iPayments);
    }

    public String printPaymentsTotal() {
        return Formats.CURRENCY.formatValue(m_dPaymentsTotal);
    }     
    
    public List<PaymentsLine> getPaymentLines() {
        return m_lpayments;
    }
    
    public int getSales() {
        return m_iSales == null ? 0 : m_iSales.intValue();
    }    
    public String printSales() {
        return Formats.INT.formatValue(m_iSales-getRefound());
    }
    public String printSalesBase() {
        return Formats.CURRENCY.formatValue(m_dSalesBase);
    }     
    public String printSalesTaxes() {
        return Formats.CURRENCY.formatValue(m_dSalesTaxes);
    }     
    public String printSalesTotal() {            
        return Formats.CURRENCY.formatValue((m_dSalesBase == null || m_dSalesTaxes == null)
                ? null
                : m_dSalesBase + m_dSalesTaxes + m_dServices);
    }     
    public List<SalesLine> getSaleLines() {
        return m_lsales;
    }
    
    public int getRefound() {
        return m_irefund == null ? 0 : m_irefund.intValue();
    }    
    
    public String printRefound(){
        return Formats.INT.formatValue(getRefound());
    }
    
    
    /***********************************/
    public String printServices() {
        return Formats.CURRENCY.formatValue(m_dServices);
    } 
    
    public String printServicesQuantity() {
        return ""+m_iServices;
    } 
    
    public String printDiscount() {
        return Formats.CURRENCY.formatValue(m_iDiscount);
    } 
    /***********************************/
    
    public AbstractTableModel getPaymentsModel() {
        return new AbstractTableModel() {
            public String getColumnName(int column) {
                return AppLocal.getIntString(PAYMENTHEADERS[column]);
            }
            public int getRowCount() {
                return m_lpayments.size();
            }
            public int getColumnCount() {
                return PAYMENTHEADERS.length;
            }
            public Object getValueAt(int row, int column) {
                PaymentsLine l = m_lpayments.get(row);
                switch (column) {
                case 0: return l.getType();
                case 1: return l.getValue();
                default: return null;
                }
            }  
        };
    }
    
    public static class SalesLine implements SerializableRead {
        
        private String m_SalesTaxName;
        private Double m_SalesTaxes;
        
        public void readValues(DataRead dr) throws BasicException {
            m_SalesTaxName = dr.getString(1);
            m_SalesTaxes = dr.getDouble(2);
        }
        public String printTaxName() {
            return m_SalesTaxName;
        }      
        public String printTaxes() {
            return Formats.CURRENCY.formatValue(m_SalesTaxes);
        }
        public String getTaxName() {
            return m_SalesTaxName;
        }
        public Double getTaxes() {
            return m_SalesTaxes;
        }        
    }

    public AbstractTableModel getSalesModel() {
        return new AbstractTableModel() {
            public String getColumnName(int column) {
                return AppLocal.getIntString(SALEHEADERS[column]);
            }
            public int getRowCount() {
                return m_lsales.size();
            }
            public int getColumnCount() {
                return SALEHEADERS.length;
            }
            public Object getValueAt(int row, int column) {
                SalesLine l = m_lsales.get(row);
                switch (column) {
                case 0: return l.getTaxName();
                case 1: return l.getTaxes();
                default: return null;
                }
            }  
        };
    }
    
    public static class PaymentsLine implements SerializableRead {
        
        private String m_PaymentType;
        private Double m_PaymentValue;
        private String m_PaymentCardType;
        private String m_PaymentNotes;//@win
        
        public void readValues(DataRead dr) throws BasicException {
            m_PaymentType = dr.getString(1);
            m_PaymentValue = dr.getDouble(2);
            m_PaymentCardType = dr.getString(3);
            m_PaymentNotes = dr.getString(4);//@win
        }
        
        public String printType() {
            if(m_PaymentType.contains("magcard"))
                return AppLocal.getIntString("transpayment." + m_PaymentType) + " " + m_PaymentCardType;
            else
                return AppLocal.getIntString("transpayment." + m_PaymentType);
        }
        public String getType() {
            if(m_PaymentType.contains("magcard"))
                return m_PaymentType + " " + m_PaymentCardType;
            else
                return m_PaymentType;
        }
        public String printValue() {
            return Formats.CURRENCY.formatValue(m_PaymentValue);
        }
        public String printNotes(){
           return m_PaymentNotes == null ? "" : m_PaymentNotes;
        }//@win
        
        public Double getValue() {
            return m_PaymentValue;
        }        
    }

    
    public static class ProductSalesLine implements SerializableRead {
 
        private String m_ProductName;
        private Double m_ProductUnits;
        private Double m_ProductPrice;
        private Double m_TaxRate;
        private Double m_ProductPriceTax;
        private Properties m_attributes = new Properties();
        private BigDecimal m_discountRate;
 
        public void readValues(DataRead dr) throws BasicException {
            m_ProductName = dr.getString(1);
            m_ProductUnits = dr.getDouble(2);
            m_ProductPrice = dr.getDouble(3);
            m_TaxRate = dr.getDouble(4);
            try {
                byte[] img = dr.getBytes(5);
                if (img != null) {
                    m_attributes.loadFromXML(new ByteArrayInputStream(img));
                }
            } catch (IOException e) {
            }
            m_ProductPriceTax = m_ProductPrice + m_ProductPrice*m_TaxRate;
        }
 
        public String printProductName() {
            return StringUtils.encodeXML(m_ProductName);
        }
 
        public String printProductUnits() {
            return Formats.DOUBLE.formatValue(m_ProductUnits);
        }
 
        public Double getProductUnits() {
            return m_ProductUnits;
        }
 
        public String printProductPrice() {
            return Formats.CURRENCY.formatValue(m_ProductPrice);
        }
 
        public Double getProductPrice() {
            return m_ProductPrice;
        }
 
        public String printTaxRate() {
            return Formats.PERCENT.formatValue(m_TaxRate);
        }
 
        public Double getTaxRate() {
            return m_TaxRate;
        }
 
        public String printProductPriceTax() {
            return Formats.CURRENCY.formatValue(m_ProductPriceTax);
        }
        
        public String printProductSubValue() {
            return Formats.CURRENCY.formatValue(m_ProductPriceTax*m_ProductUnits);
        }
        
        public String getProperty(String key) {
        return m_attributes.getProperty(key);
        }
    }
    
    public static class TopSalesLine implements SerializableRead {
        private String m_TopProductName;
        private Double m_TopProductUnits;
        private Double m_TopProductPrice;
        private Double m_TopProductTotal;
        
         public void readValues(DataRead dr) throws BasicException {
            m_TopProductName = dr.getString(1);
            m_TopProductUnits = dr.getDouble(2);
            m_TopProductPrice = dr.getDouble(3);
            m_TopProductTotal = dr.getDouble (4);
         }
        
         public String printTopProductName() {
            return StringUtils.encodeXML(m_TopProductName);
        }
 
        public String printTopProductUnits() {
            return Formats.DOUBLE.formatValue(m_TopProductUnits);
        }
 
        public Double getTopProductUnits() {
            return m_TopProductUnits;
        }
 
        public String printTopProductPrice() {
            return Formats.CURRENCY.formatValue(m_TopProductPrice);
        }
 
        public Double getTopProductPrice() {
            return m_TopProductPrice;
        }
        
        public String printTopProductTotal() {
            return Formats.CURRENCY.formatValue(m_TopProductPrice*m_TopProductUnits);
        }
 
        public Double getTopProductTotal() {
            return m_TopProductTotal;
        }
        
    }

   
}    