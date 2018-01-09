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

package com.openbravo.pos.sales;
     
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Date;

import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.printer.*;

import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.panels.JProductFinder;
import com.openbravo.pos.scale.ScaleException;
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListKeyed;
import com.openbravo.data.loader.LocalRes;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinder;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.main.Main;
import com.openbravo.pos.payment.JPaymentSelectReceipt;
import com.openbravo.pos.payment.JPaymentSelectRefund;
import com.openbravo.pos.smj.EmailSender;
import com.openbravo.pos.smj.MQClient;
import com.openbravo.pos.ticket.InsuranceInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.util.JRPrinterAWT300;
import com.openbravo.pos.util.ReportUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.print.PrintService;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import java.util.List;
import com.openbravo.pos.ticket.UnitInfo;
import com.openbravo.pos.util.VariableG;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;

/**
 * Events for sales panel
 * 
 * @author adrianromero
 * @modified by Pedro Rozo - SmartJSP
 */
public abstract class JPanelTicket extends JPanel implements JPanelView, BeanFactoryApp, TicketsEditor {
   
    // Variable numerica
    private final static int NUMBERZERO = 0;
    private final static int NUMBERVALID = 1;
    
    private final static int NUMBER_INPUTZERO = 0;
    private final static int NUMBER_INPUTZERODEC = 1;
    private final static int NUMBER_INPUTINT = 2;
    private final static int NUMBER_INPUTDEC = 3; 
    private final static int NUMBER_PORZERO = 4; 
    private final static int NUMBER_PORZERODEC = 5; 
    private final static int NUMBER_PORINT = 6; 
    private final static int NUMBER_PORDEC = 7; 
    public static String  currentCustomer = null;
    protected JTicketLines m_ticketlines;
        
    // private Template m_tempLine;
    private TicketParser m_TTP;//= new TicketParser(null, null);
    
    protected TicketInfo m_oTicket; 
    protected Object m_oTicketExt; 
    
    // Estas tres variables forman el estado...
    private int m_iNumberStatus;
    private int m_iNumberStatusInput;
    private int m_iNumberStatusPor;
    private StringBuffer m_sBarcode;
    // insurance info container - SmartPOS
    private InsuranceInfo iInfo = null;           
    private double copay =  0;
    private double coveredInsurance = 0;
    private String codInsurance = "";
    private String codPlan = "";
    

    private JTicketsBag m_ticketsbag;
    private JRootApp m_appview;//@win
    
    private SentenceList senttax;
    private ListKeyed taxcollection;
    // private ComboBoxValModel m_TaxModel;
    
    private SentenceList senttaxcategories;
    private ListKeyed taxcategoriescollection;
    private ComboBoxValModel taxcategoriesmodel;
    
    private TaxesLogic taxeslogic;
    
//    private ScriptObject scriptobjinst;
    protected JPanelButtons m_jbtnconfig;
    
    protected AppView m_App;
    protected DataLogicSystem dlSystem;
    protected DataLogicSales dlSales;
    protected DataLogicCustomers dlCustomers;
    protected DataLogicReceipts dlReceipts;
    
    private JPaymentSelect paymentdialogreceipt;
    private JPaymentSelect paymentdialogrefund;
    private JRefundLines  jRefundLines = null;
    private SentenceList sentTaxCategoriesZeroRate;
    private String printerId;
    private String printerLocation;
    private static Logger logger = Logger.getLogger(JPanelTicket.class.getName());
    private boolean canSendEmail = false;
    DecimalFormat df = new DecimalFormat("#.##");
    DecimalFormatSymbols ds = new DecimalFormatSymbols();
    
    
    
    
    
    /** Creates new form JTicketView */
    public JPanelTicket() {
        initComponents ();
        initToolTips();
        //init(m_App);
    }

    public AppView getM_App() {
        return m_App;
    }
   
        /**
     * Find all the total of requested items of this product in the current ticket
     * using the base unit (example oz)
     * @param prodId
     * @return 
     */    
    private double getCantidadTotalProductoTicket(String prodId, String currentUnitId)  {
        double tot    = 0;
        String baseUnit = "";
         List lista =  m_oTicket.getLines();
         
            for (int i=0;  i < lista.size();i++)
            {
                TicketLineInfo linea  = (TicketLineInfo) lista.get(i);
                if (linea.getProductID().equalsIgnoreCase(prodId.trim()))
                {
                  
                 String unitIdRequested =  linea.getProperty("unidadDefault");   // obtiene la unidad de la linea actual - pro ejemplo onzas     
                 baseUnit =  linea.getProperty("unidadId");
                 double cantidad =  linea.getMultiply();
                 
                 Double divideRate =  dlSales.getConversionFactorsUOMProduct(prodId, baseUnit, unitIdRequested); //get divideRate
                 tot +=   divideRate.doubleValue() * cantidad;
                }  
                

            }
            // aqui toma el total que estaba en la unidad base y lo deuvelve en la unidad de la linea actual
            // para ser visualizado por el usuario final
            if ( lista.size() > 0 )
            {
            Double divideRateTotal =  dlSales.getConversionFactorsUOMProduct(prodId, baseUnit, currentUnitId); //get divideRate
            tot = tot / divideRateTotal.doubleValue() ;
            }
            else
            {
             tot = 0;
            }
            
        return tot;
    }
    
    
    
    @Override
    public void init(AppView app) throws BeanFactoryException {
        System.out.println("INISIALISIIIIIIIIIIIIIIIIIIiiii");
        ds.setDecimalSeparator('.');
        ds.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(ds);
        df.setRoundingMode(RoundingMode.HALF_UP);   // define that we don´t need rounding 
        
        m_App = app;
        dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlReceipts = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        dlCustomers = (DataLogicCustomers) m_App.getBean("com.openbravo.pos.customers.DataLogicCustomers");
                    
        
        // borramos el boton de bascula si no hay bascula conectada
        if (!m_App.getDeviceScale().existsScale()) {
            m_jbtnScale.setVisible(false);
        }
        
        m_ticketsbag = getJTicketsBag();
        m_jPanelBag.add(m_ticketsbag.getBagComponent(), BorderLayout.LINE_START);
        add(m_ticketsbag.getNullComponent(), "null");

        m_ticketlines = new JTicketLines(dlSystem.getResourceAsXML("Ticket.Line"),dlSales);
        m_jPanelCentral.add(m_ticketlines, java.awt.BorderLayout.CENTER);
        
        m_TTP = new TicketParser(m_App.getDeviceTicket(), dlSystem);
               System.out.println(" Deviced " + m_App.getDeviceTicket());
        // Los botones configurables...
        m_jbtnconfig = new JPanelButtons("Ticket.Buttons", this);
        m_jButtonsExt.add(m_jbtnconfig);           
       
        // El panel de los productos o de las lineas...        
        catcontainer.add(getSouthComponent(), BorderLayout.CENTER);
        
        // El modelo de impuestos
        senttax = dlSales.getTaxList();
        senttaxcategories = dlSales.getTaxCategoriesList();
        sentTaxCategoriesZeroRate = dlSales.getTaxCategoriesListWithTaxRateZero();
        
        taxcategoriesmodel = new ComboBoxValModel();    
              
        // ponemos a cero el estado
        stateToZero();  
        
        // inicializamos
        m_oTicket = null;
        m_oTicketExt = null;      
    }
    
    public Object getBean() {
        return this;
    }
    
    public JComponent getComponent() {
        return this;
    }

    public void activate() throws BasicException {

        paymentdialogreceipt = JPaymentSelectReceipt.getDialog(this);
        paymentdialogreceipt.init(m_App);
        paymentdialogrefund = JPaymentSelectRefund.getDialog(this); 
        paymentdialogrefund.init(m_App);
        
        // impuestos incluidos seleccionado ?
        m_jaddtax.setSelected("true".equals(m_jbtnconfig.getProperty("taxesincluded")));

        // Inicializamos el combo de los impuestos.
        java.util.List<TaxInfo> taxlist = senttax.list();
        taxcollection = new ListKeyed<TaxInfo>(taxlist);
        java.util.List<TaxCategoryInfo> taxcategorieslist = senttaxcategories.list();
        taxcategoriescollection = new ListKeyed<TaxCategoryInfo>(taxcategorieslist);
        
        taxcategoriesmodel = new ComboBoxValModel(taxcategorieslist);
        m_jTax.setModel(taxcategoriesmodel);

        String taxesid = m_jbtnconfig.getProperty("taxcategoryid");
        if (taxesid == null) {
            if (m_jTax.getItemCount() > 0) {
                m_jTax.setSelectedIndex(0);
            }
        } else {
            taxcategoriesmodel.setSelectedKey(taxesid);
        }              
                
        taxeslogic = new TaxesLogic(taxlist);
        
        // Show taxes options
        if (m_App.getAppUserView().getUser().hasPermission("sales.ChangeTaxOptions")) {
            m_jTax.setVisible(true);
            m_jaddtax.setVisible(true);
        } else {
            m_jTax.setVisible(false);
            m_jaddtax.setVisible(false);
        }
        
        // Authorization for buttons
        btnSplit.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Total"));
        m_jDelete.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
        m_jNumberKeys.setMinusEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
        m_jNumberKeys.setEqualsEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Total"));
        m_jbtnconfig.setPermissions(m_App.getAppUserView().getUser()); 
        
               
        m_ticketsbag.activate();        
    }
    
    public boolean deactivate() {

        return m_ticketsbag.deactivate();
    }
    
    protected abstract JTicketsBag getJTicketsBag();
    protected abstract Component getSouthComponent();
    protected abstract void resetSouthComponent();
     
    public void setActiveTicket(TicketInfo oTicket, Object oTicketExt) {
        VariableG.ticketInfo =oTicket;
        m_oTicket = oTicket;
        m_oTicketExt = oTicketExt;
        
        if (m_oTicket != null) {            
            // Asign preeliminary properties to the receipt
            m_oTicket.setUser(m_App.getAppUserView().getUser().getUserInfo());
            m_oTicket.setActiveCash(m_App.getActiveCashIndex());
            m_oTicket.setDate(new Date()); // Set the edition date.
        }
        
        executeEvent(m_oTicket, m_oTicketExt, "ticket.show");
        
        refreshTicket();               
    }
    
    public TicketInfo getActiveTicket() {
        return m_oTicket;
    }
    
    private void refreshTicket() {
        
        CardLayout cl = (CardLayout)(getLayout());
        
        if (m_oTicket == null) {        
            m_jTicketId.setText(null); 
            m_jWaiter.setText(null);//@win
            m_ticketlines.clearTicketLines();
           
            m_jSubtotalEuros.setText("0");
            m_jTaxesEuros.setText("0");
            m_jTotalEuros.setText("0"); 
        
            stateToZero();
            
            // Muestro el panel de nulos.
            cl.show(this, "null");  
            resetSouthComponent();

        } else {
            Main.logger.info("**********Cash "+m_oTicket.getId()+" "+m_oTicketExt+" "+m_oTicket.getCustomerId());
            Main.logger.info("**********Cash "+m_oTicket.getProperty("cash"));
            Main.logger.info("**********Cash "+m_oTicket.getProperty("floor"));
            if(m_oTicket.getProperty("cash") !=null){
                VariableG.GIVEN=m_oTicket.getProperty("cash");
            }else{
                VariableG.GIVEN="";
            }
            
            if(m_oTicket.getProperty("username") !=null){
                VariableG.WAITER=m_oTicket.getProperty("username");
            }else{
                VariableG.WAITER="";
            }
            Main.logger.info("**********username "+m_oTicket.getProperty("username"));
            if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
                //Make disable Search and Edit Buttons
                m_jEditLine.setVisible(false);
                m_jList.setVisible(false);
            }
            
            // Refresh ticket taxes
            for (TicketLineInfo line : m_oTicket.getLines()) {
                line.setTaxInfo(taxeslogic.getTaxInfo(line.getProductTaxCategoryID(), m_oTicket.getDate(), m_oTicket.getCustomer()));
            }  
        
            // The ticket name
            m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));
            m_jWaiter.setText(VariableG.WAITER);//@win

            // Limpiamos todas las filas y anadimos las del ticket actual
            m_ticketlines.clearTicketLines();

            for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
                try{
                    m_ticketlines.addTicketLine(m_oTicket.getLine(i),m_oTicketExt);
                }catch (Exception e){
                    continue;
                }
            }
            printPartialTotals();
            stateToZero();
            
            // Muestro el panel de tickets.
            cl.show(this, "ticket");
            resetSouthComponent();
            
            // activo el tecleador...
            m_jKeyFactory.setText(null);       
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    m_jKeyFactory.requestFocus();
                }
            });
        }
    }
       
    private void printPartialTotals(){
               
        if (m_oTicket.getLinesCount() == 0) {
            m_jSubtotalEuros.setText("0");
            m_jTaxesEuros.setText("0");
            m_jTotalEuros.setText("0");
        } else {
            m_jSubtotalEuros.setText(m_oTicket.printSubTotal2());//@win
            m_jTaxesEuros.setText(m_oTicket.printTax2());//@win
            m_jTotalEuros.setText(m_oTicket.printTotal2());//@win
        }
    }
    
    private void paintTicketLine(int index, TicketLineInfo oLine){
        
        if (executeEventAndRefresh("ticket.setline", new ScriptArg("index", index), new ScriptArg("line", oLine)) == null) {

            m_oTicket.setLine(index, oLine);
            m_ticketlines.setTicketLine(index, oLine);
            m_ticketlines.setSelectedIndex(index);

            visorTicketLine(oLine); // Y al visor tambien...
            printPartialTotals();   
            stateToZero();  

            // event receipt
            executeEventAndRefresh("ticket.change");
        }
   }

    private void addTicketLine(ProductInfoExt oProduct, double dMul, double dPrice) {   
        if(!oProduct.getName().equals("")){
            TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(),  m_oTicket.getDate(), m_oTicket.getCustomer());
               // SmartPOS         
            // valida que al adicionar una nueva linea de producto si halla stock suficiente
            double totProd = getCantidadTotalProductoTicket(oProduct.getID(),oProduct.getProperty("unidadDefault"));
            // Main.logger.info("\n 5 cantidad preliminar de producto:"+totProd); 
            totProd += 1; 
            
            //@win
            //Double available = checkStock(oProduct.getID(), totProd  ,oProduct.getProperty("unidadDefault")  );                                
            // Main.logger.info("\n 5 disponibles reportadas de producto:"+available);
                   //newline.setMultiply(available);
            
           
            
            //@win : hilangkan pengecekan stock
            if (//(totProd  <= available)  && 
                    isAValidPrinter(oProduct.getProperty("printkb")) ) 
            {
            addTicketLine(new TicketLineInfo(oProduct, dMul, dPrice, tax, (java.util.Properties) (oProduct.getProperties().clone())));
            }
            
        }
    }
    
    /**
     * Validate if the current product printers is included in the list of valida printers: resource: Printer.mapping
     * @param printerProduct
     * @return 
     */
    public boolean isAValidPrinter(String printerProduct) {
        boolean retorno = true;
 //          Main.logger.info("\n Impresora de producto:"+ printerProduct); 
            String printerMapping = dlSystem.getResourceAsText("Printer.Mapping");
            java.util.Properties proper2 = new Properties();
            printerMapping = printerMapping.toUpperCase();
            try {
                InputStream  inputStream = new ByteArrayInputStream(printerMapping.getBytes("UTF-8"));
                proper2.load(inputStream);  
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } 
            printerProduct = printerProduct.trim().toUpperCase();
            String [] impre = printerProduct.split(",");   //separa los nombres de impresoras por , para impresoras multiples
            String novalida = "";
            for (int i = 0; i < impre.length; i++ )             // valida las n impresoras contra la confiracion  todas deben existir
            {
                if (proper2.getProperty(impre[i] ) == null) 
                {
                retorno = false;
                novalida = impre[i];
                break;
                }
            }
            
            if (!retorno)
            {
                javax.swing.JOptionPane.showMessageDialog(null, LocalRes.getIntString("message.invalidPrinter")+":"+novalida);
            }
            return retorno;
    }
    
    
    
    protected void addTicketLine(TicketLineInfo oLine) {   
        
        if (executeEventAndRefresh("ticket.addline", new ScriptArg("line", oLine)) == null) {
        
            if (oLine.isProductCom()) {
                // Comentario entonces donde se pueda
                int i = m_ticketlines.getSelectedIndex();

                // me salto el primer producto normal...
                if (i >= 0 && !m_oTicket.getLine(i).isProductCom()) {
                    i++;
                }

                // me salto todos los productos auxiliares...
                while (i >= 0 && i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                    i++;
                }

                if (i >= 0) {
                    m_oTicket.insertLine(i, oLine);
                    m_ticketlines.insertTicketLine(i, oLine); // Pintamos la linea en la vista...                 
                } else {
                    Toolkit.getDefaultToolkit().beep();                                   
                }
            } else {    
                // Producto normal, entonces al finalnewline.getMultiply() 
                m_oTicket.addLine(oLine);            
                m_ticketlines.addTicketLine(oLine); // Pintamos la linea en la vista... 
            }

            visorTicketLine(oLine);
            printPartialTotals();   
            stateToZero();  

            // event receipt
            executeEventAndRefresh("ticket.change");
        }
    }    
    
    private void removeTicketLine(int i){
        
        if (executeEventAndRefresh("ticket.removeline", new ScriptArg("index", i)) == null) {
        
            if (m_oTicket.getLine(i).isProductCom()) {
                // Es un producto auxiliar, lo borro y santas pascuas.
                m_oTicket.removeLine(i);
                m_ticketlines.removeTicketLine(i);   
            } else {
                // Es un producto normal, lo borro.
                m_oTicket.removeLine(i);
                m_ticketlines.removeTicketLine(i); 
                // Y todos lo auxiliaries que hubiera debajo.
                while(i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                    m_oTicket.removeLine(i);
                    m_ticketlines.removeTicketLine(i); 
                }
            }

            visorTicketLine(null); // borro el visor 
            printPartialTotals(); // pinto los totales parciales...                           
            stateToZero(); // Pongo a cero    

            // event receipt
            executeEventAndRefresh("ticket.change");
        }
    }
    
    private ProductInfoExt getInputProduct() {
        ProductInfoExt oProduct = new ProductInfoExt(); // Es un ticket
        oProduct.setReference(null);
        oProduct.setCode(null);
        oProduct.setName("");
        oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
        
        oProduct.setPriceSell(includeTaxes(oProduct.getTaxCategoryID(), getInputValue()));
        
        return oProduct;
    }
    
    private double includeTaxes(String tcid, double dValue) {
        if (m_jaddtax.isSelected()) {
            TaxInfo tax = taxeslogic.getTaxInfo(tcid,  m_oTicket.getDate(), m_oTicket.getCustomer());
            double dTaxRate = tax == null ? 0.0 : tax.getRate();           
            return dValue / (1.0 + dTaxRate);      
        } else {
            return dValue;
        }
    }
    
    private double getInputValue() {
        try {
            return Double.parseDouble(m_jPrice.getText());
        } catch (NumberFormatException e){
            return 0.0;
        }
    }

    private double getPorValue() {
        try {
            return Double.parseDouble(m_jPor.getText().substring(1));                
        } catch (NumberFormatException e){
            return 1.0;
        } catch (StringIndexOutOfBoundsException e){
            return 1.0;
        }
    }
    
    private void stateToZero(){
        m_jPor.setText("");
        m_jPrice.setText("");
        m_sBarcode = new StringBuffer();

        m_iNumberStatus = NUMBER_INPUTZERO;
        m_iNumberStatusInput = NUMBERZERO;
        m_iNumberStatusPor = NUMBERZERO;
    }
   /**
     * AÃƒÆ’Ã‚Â±ade producto por codigo de barras al ticket actual
     * @param sCode 
     */ 
    private void incProductByCode(String sCode) {
    // precondicion: sCode != null
         
         
        try {
            ProductInfoExt oProduct = dlSales.getProductInfoByCode(sCode);
            
            if (oProduct == null) {                  
                Toolkit.getDefaultToolkit().beep();                   
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noproduct")).show(this);           
                stateToZero();
            } else {

                incProduct(oProduct);
            }
        } catch (BasicException eData) {
            stateToZero();           
            new MessageInf(eData).show(this);           
        }
    }
    

    
    
    
    private void incProductByCodePrice(String sCode, double dPriceSell) {
    // precondicion: sCode != null
        
        try {
            ProductInfoExt oProduct = dlSales.getProductInfoByCode(sCode);
            
            if (oProduct == null) {                  
                Toolkit.getDefaultToolkit().beep();                   
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noproduct")).show(this);           
                stateToZero();
            } else {
                // Se anade directamente una unidad con el precio y todo
                if (m_jaddtax.isSelected()) {
                    // debemos quitarle los impuestos ya que el precio es con iva incluido...
                    TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(),  m_oTicket.getDate(), m_oTicket.getCustomer());
                    addTicketLine(oProduct, 1.0, dPriceSell / (1.0 + tax.getRate()));
                } else {
                    addTicketLine(oProduct, 1.0, dPriceSell);
                }                
            }
        } catch (BasicException eData) {
            stateToZero();
            new MessageInf(eData).show(this);               
        }
    }
    
    private void incProduct(ProductInfoExt prod) {
        
        if (prod.isScale() && m_App.getDeviceScale().existsScale()) {
            try {
                Double value = m_App.getDeviceScale().readWeight();
                if (value != null) {
                    incProduct(value.doubleValue(), prod);
                }
            } catch (ScaleException e) {
                Toolkit.getDefaultToolkit().beep();                
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);           
                stateToZero(); 
            }
        } else {
            // No es un producto que se pese o no hay balanza
            incProduct(1.0, prod);
        }
    }
    
    private void incProduct(double dPor, ProductInfoExt prod) {
        // precondicion: prod != null
        addTicketLine(prod, dPor, prod.getPriceSell());       
    }
       
    protected void buttonTransition(ProductInfoExt prod) {
    // precondicion: prod != null
        
         if (m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
            incProduct(prod);
        } else if (m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
            incProduct(getInputValue(), prod);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }       
    }
    /**
     * Procesa eventos del teclado en panel de ventas 
     * como codigo de barras  - SmartPOS
     * @param cTrans 
     */
    private void stateTransition(char cTrans) {

        if (cTrans == '\n') {
            // Codigo de barras introducido
            if (m_sBarcode.length() > 0) {           
                String sCode = m_sBarcode.toString();
                if (sCode.startsWith("c") || sCode.startsWith("C") ) {
                    // barcode of a customers card
                    try {
                        CustomerInfoExt newcustomer = dlSales.findCustomerExt(sCode);
                        if (newcustomer == null) {
                            Toolkit.getDefaultToolkit().beep();                   
                            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.nocustomer")).show(this);           
                        } else {
                            m_oTicket.setCustomer(newcustomer);
                            m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));
                            m_jWaiter.setText(VariableG.WAITER);//@win
                        }
                    } catch (BasicException e) {
                        Toolkit.getDefaultToolkit().beep();                   
                        new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.nocustomer"), e).show(this);           
                    }
                    stateToZero();
                } else if (sCode.length() == 13 && sCode.startsWith("250")) {
                    // barcode of the other machine
                    ProductInfoExt oProduct = new ProductInfoExt(); // Es un ticket
                    oProduct.setReference(null); // para que no se grabe
                    oProduct.setCode(sCode);
                    oProduct.setName("Ticket " + sCode.substring(3, 7));
                    oProduct.setPriceSell(Double.parseDouble(sCode.substring(7, 12)) / 100);   
                    oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
                    // Se anade directamente una unidad con el precio y todo
                    addTicketLine(oProduct, 1.0, includeTaxes(oProduct.getTaxCategoryID(), oProduct.getPriceSell()));
                } else if (sCode.length() == 13 && sCode.startsWith("210")) {
                    // barcode of a weigth product
                    incProductByCodePrice(sCode.substring(0, 7), Double.parseDouble(sCode.substring(7, 12)) / 100);
                } else {
                    // barcode for a normal product
                    
                    incProductByCode(sCode);
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        } else {
            // otro caracter
            // Esto es para el codigo de barras...
            m_sBarcode.append(cTrans);

            // Esto es para el los productos normales...
            if (cTrans == '\u007f') { 
                stateToZero();

            } else if ((cTrans == '0') 
                    && (m_iNumberStatus == NUMBER_INPUTZERO)) {
                m_jPrice.setText("0");            
            } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                    && (m_iNumberStatus == NUMBER_INPUTZERO)) { 
                // Un numero entero
                m_jPrice.setText(Character.toString(cTrans));
                m_iNumberStatus = NUMBER_INPUTINT;    
                m_iNumberStatusInput = NUMBERVALID;
            } else if ((cTrans == '0' || cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                       && (m_iNumberStatus == NUMBER_INPUTINT)) { 
                // Un numero entero
                m_jPrice.setText(m_jPrice.getText() + cTrans);

            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTZERO) {
                m_jPrice.setText("0.");
                m_iNumberStatus = NUMBER_INPUTZERODEC;            
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTINT) {
                m_jPrice.setText(m_jPrice.getText() + ".");
                m_iNumberStatus = NUMBER_INPUTDEC;

            } else if ((cTrans == '0')
                       && (m_iNumberStatus == NUMBER_INPUTZERODEC || m_iNumberStatus == NUMBER_INPUTDEC)) { 
                // Un numero decimal
                m_jPrice.setText(m_jPrice.getText() + cTrans);
            } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                       && (m_iNumberStatus == NUMBER_INPUTZERODEC || m_iNumberStatus == NUMBER_INPUTDEC)) { 
                // Un numero decimal
                m_jPrice.setText(m_jPrice.getText() + cTrans);
                m_iNumberStatus = NUMBER_INPUTDEC;
                m_iNumberStatusInput = NUMBERVALID;

            } else if (cTrans == '*' 
                    && (m_iNumberStatus == NUMBER_INPUTINT || m_iNumberStatus == NUMBER_INPUTDEC)) {
                m_jPor.setText("x");
                m_iNumberStatus = NUMBER_PORZERO;            
            } else if (cTrans == '*' 
                    && (m_iNumberStatus == NUMBER_INPUTZERO || m_iNumberStatus == NUMBER_INPUTZERODEC)) {
                m_jPrice.setText("0");
                m_jPor.setText("x");
                m_iNumberStatus = NUMBER_PORZERO;       

            } else if ((cTrans == '0') 
                    && (m_iNumberStatus == NUMBER_PORZERO)) {
                m_jPor.setText("x0");            
            } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                    && (m_iNumberStatus == NUMBER_PORZERO)) { 
                // Un numero entero
                m_jPor.setText("x" + Character.toString(cTrans));
                m_iNumberStatus = NUMBER_PORINT;            
                m_iNumberStatusPor = NUMBERVALID;
            } else if ((cTrans == '0' || cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                       && (m_iNumberStatus == NUMBER_PORINT)) { 
                // Un numero entero
                m_jPor.setText(m_jPor.getText() + cTrans);

            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORZERO) {
                m_jPor.setText("x0.");
                m_iNumberStatus = NUMBER_PORZERODEC;            
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORINT) {
                m_jPor.setText(m_jPor.getText() + ".");
                m_iNumberStatus = NUMBER_PORDEC;

            } else if ((cTrans == '0')
                       && (m_iNumberStatus == NUMBER_PORZERODEC || m_iNumberStatus == NUMBER_PORDEC)) { 
                // Un numero decimal
                m_jPor.setText(m_jPor.getText() + cTrans);
            } else if ((cTrans == '1' || cTrans == '2' || cTrans == '3' || cTrans == '4' || cTrans == '5' || cTrans == '6' || cTrans == '7' || cTrans == '8' || cTrans == '9')
                       && (m_iNumberStatus == NUMBER_PORZERODEC || m_iNumberStatus == NUMBER_PORDEC)) { 
                // Un numero decimal
                m_jPor.setText(m_jPor.getText() + cTrans);
                m_iNumberStatus = NUMBER_PORDEC;            
                m_iNumberStatusPor = NUMBERVALID;  
            
            } else if (cTrans == '\u00a7' 
                    && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
                // Scale button pressed and a number typed as a price
                if (m_App.getDeviceScale().existsScale() && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                    try {
                        Double value = m_App.getDeviceScale().readWeight();
                        if (value != null) {
                            //
                            ProductInfoExt product = getInputProduct();
                            addTicketLine(product, value.doubleValue(), product.getPriceSell());
                        }
                    } catch (ScaleException e) {
                        Toolkit.getDefaultToolkit().beep();
                        new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);           
                        stateToZero(); 
                    }
                } else {
                    // No existe la balanza;
                    Toolkit.getDefaultToolkit().beep();
                }
            } else if (cTrans == '\u00a7' 
                    && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
                // Scale button pressed and no number typed.
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0){
                    Toolkit.getDefaultToolkit().beep();
                } else if (m_App.getDeviceScale().existsScale()) {
                    try {
                        Double value = m_App.getDeviceScale().readWeight();
                        if (value != null) {
                            TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                            newline.setMultiply(value.doubleValue());
                            newline.setPrice(Math.abs(newline.getPrice()));
                            paintTicketLine(i, newline);
                        }
                    } catch (ScaleException e) {
                        // Error de pesada.
                        Toolkit.getDefaultToolkit().beep();
                        new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);           
                        stateToZero(); 
                    }
                } else {
                    // No existe la balanza;
                    Toolkit.getDefaultToolkit().beep();
                }      
                
            // Add one product more to the selected line
            } else if (cTrans == '+' 
                    && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
                
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0){
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    //If it's a refund + button means one unit less
                    if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND){
                        newline.setMultiply(newline.getMultiply() - 1.0);
                        paintTicketLine(i, newline);                   
                    }
                    else {
                        // add one unit to the selected line
                        
                   double totProd = getCantidadTotalProductoTicket(newline.getProductID(),newline.getProperty("unidadDefault") );
                   totProd += 1; 
                   
                // SmartPOS         
                // busca existencias del producto en la localizacion(bodega) por defecto 0  // newline.getMultiply() de la linea actual
                   //Double available = checkStock(newline.getProductID(), totProd  ,newline.getProperty("unidadDefault")  );                                
                   //newline.setMultiply(available);
                   // diferencia de total de lineas - linea actual para sugerir el valor adecuado
                   
                   //@win : hilangkan pengecekan stock
                   //if ((totProd)  == available ) {
                       // newline.setMultiply(available - resta );
                         newline.setMultiply(newline.getMultiply() + 1 );
                   //} 
                        //newline.setMultiply(newline.getMultiply() + 1.0);
                   paintTicketLine(i, newline); 
                    }
                   
                    

                }

            // Delete one product of the selected line
            } else if (cTrans == '-' 
                    && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0){
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    //If it's a refund - button means one unit more
                    if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND){
                        newline.setMultiply(newline.getMultiply() + 1.0);
                        if (newline.getMultiply() >= 0) {
                            removeTicketLine(i);
                        } else {
                            paintTicketLine(i, newline);
                        }
                    } else {
                        // substract one unit to the selected line
                        newline.setMultiply(newline.getMultiply() - 1.0);
                        if (newline.getMultiply() <= 0.0) {                   
                            removeTicketLine(i); // elimino la linea
                        } else {
                            paintTicketLine(i, newline);                   
                        }
                    }
                }

            // Set n products to the selected line
            } else if (cTrans == '+' 
                    && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERVALID) {
                
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0){
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    double dPor = getPorValue();
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i)); 
                    
                    if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
                        newline.setMultiply(-dPor);
                        newline.setPrice(Math.abs(newline.getPrice()));
                        paintTicketLine(i, newline);                
                    } else {
                        
                // SmartPOS         
                // totaliza lineas del mismo producto en el ticket
                   double totProd = getCantidadTotalProductoTicket(newline.getProductID(),newline.getProperty("unidadDefault"));
                   //Main.logger.info(" 2 cantidad preliminar de producto:"+totProd); 
                  
                  //@win 
                 //double available = checkStock(newline.getProductID(), totProd ,newline.getProperty("unidadDefault")  );                               
                  //newline.setMultiply(available - totProd );
                   //@win
                   newline.setMultiply(totProd );
                        
                       //-- newline.setMultiply(dPor);
                        newline.setPrice(Math.abs(newline.getPrice()));
                        paintTicketLine(i, newline);
                    }
                }

            // Set n negative products to the selected line
            } else if (cTrans == '-' 
                    && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERVALID
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0){
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    double dPor = getPorValue();
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_NORMAL) {
                        newline.setMultiply(dPor);
                        newline.setPrice(-Math.abs(newline.getPrice()));
                        paintTicketLine(i, newline);
                    }           
                }

            // entra cunado se van adicionar N cantidades de 1 producto
            } else if (cTrans == '+' 
                    && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0){
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    //If it's a refund + button means one unit less
                    if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND){
                        newline.setMultiply(newline.getMultiply() - 1.0);
                        paintTicketLine(i, newline);                   
                    }
                    else {
                         // SmartPOS         
                // busca existencias del producto en la localizacion(bodega) por defecto 0
                 Double available = new Double(0);   
                 double totProd = getCantidadTotalProductoTicket(newline.getProductID(),newline.getProperty("unidadDefault"));
                 totProd += getInputValue();
                 //Main.logger.info("3  cantidad preliminar de producto:"+totProd); 
                 
                 //available = checkStock(newline.getProductID(), totProd ,newline.getProperty("unidadDefault")); 
                 
                 //@win : hilangkan pengecekan stock
                 //if (totProd  == available ) {
                       // newline.setMultiply(available - resta );
                         newline.setMultiply(newline.getMultiply() + getInputValue() );
                  // }  
                 //else {
                 //        newline.setMultiply(available );
                 //}
                 paintTicketLine(i, newline); 
                    }
                }
                
            // Anadimos 1 producto con precio negativo
            } else if (cTrans == '-' 
                    && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, 1.0, -product.getPriceSell());

            // Anadimos n productos
            } else if (cTrans == '+' 
                    && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERVALID
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                
                
                ProductInfoExt product = getInputProduct();
                
                 // SmartPOS_TODO
                // busca existencias del producto en la localizacion(bodega) por defecto 0

              //  checkStock(product.getID(),  getPorValue() ,newline.getProperty("unitid") ) ;
                
                    addTicketLine(product, getPorValue(), product.getPriceSell());
                

            // Anadimos n productos con precio negativo ?
            } else if (cTrans == '-' 
                    && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERVALID
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, getPorValue(), -product.getPriceSell());

            // Totals() Igual;
            } else if (cTrans == ' ' || cTrans == '=') {
                if(m_oTicket.getTicketId() == 0){
                    if (m_oTicket.getLinesCount() > 0) {

                        if (closeTicket(m_oTicket, m_oTicketExt)) {
                            // Ends edition of current receipt
                            
                            m_ticketsbag.deleteTicket();  
                        } else {
                            // repaint current ticket
                            refreshTicket();
                        }
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        }
    }
    
    /**
     * Valida si el pedido es mayor que las existencias actuales. comparandolas en la unidad base del producto
     * retorna el stock disponible para pedido
     * @param productId
     * @param cantidad
     * @return 
     */
    public Double checkStock(String productId,Double cantidad,String unitIdRequested)
    {
        Double retorno = new Double(0);
                // SmartPOS         
                // busca existencias del producto en la localizacion(bodega) por defecto 0
                 Double stockP = new Double(0);
                 try {
                    stockP = dlSales.findProductStock(productId, null);
                    ProductInfoExt prod = dlSales.getProductInfo(productId);
                  
                 // verifica negativos
                    if (stockP < 0)
                         stockP = new Double(0);
                    
                 // aqui obtiene la unidad base del producto y la unidad base del pedido
                 // convierte de la unidad del pedido a la base, para revisar si ahy inventario suficiente para despachar
                 String baseUnit =  prod.getUnit();   // obtiene la unidad base del producto - pro ejemplo onzas     
                 Double divideRate =  dlSales.getConversionFactorsUOMProduct(productId, baseUnit, unitIdRequested); //get divideRate
                 Double cantidadEnBase =  divideRate.doubleValue() * cantidad;
                 UnitInfo   uInfo =  dlSales.getUnitInfo(unitIdRequested);
                    
                 if (  Double.compare(cantidadEnBase,stockP ) > 0)      // reivsa si hay stock para el pedido (minimo 1)
                 {
                    Main.logger.info ("Cantidad: "+cantidad );
                    Main.logger.info ("Unit name; "+uInfo.getName());
                    Main.logger.info (AppLocal.getIntString("message.nostockavailable"));
                    Main.logger.info (" "+ new Double(stockP / divideRate) .toString());
                    if (JOptionPane.showConfirmDialog(this, 
                            df.format(cantidad) +" "+uInfo.getName()  +" "+AppLocal.getIntString("message.nostockavailable")+": "+ df.format(new Double(stockP / divideRate)),
                            AppLocal.getIntString("message.wishpurchaseorder"),
                         JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)  
                    {
                      logger.log(Level.SEVERE," Si quiere crear orden de Compra por: "+ (cantidad -stockP)  );
                      createPurchaseOrderERP(productId, ( cantidadEnBase - stockP), prod.getTaxCategoryID(),baseUnit);
                    }   
                     //retorna solo la cantidad que se puede perdir (stock) en la unidad que se pedia originalemnte)
                   retorno = (stockP / divideRate);
                 }
                 else {    // cantidad es menor que existencias 
                     retorno = (cantidad);
                 }
                 } catch (BasicException ex) {
                       logger.log(Level.SEVERE, null, ex);
                 }
       return retorno;
    }
    /**
     * Creates a new XML messahe requesting the creation of a purchase order to the ERP
     * @param d   SmartPOS
     */
    private void createPurchaseOrderERP(String productId, double multiply,String taxId,String unitId) {
   
                String xml ="";
                xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
                xml += "<entityDetail>";
                xml += "	<type>purchase-order</type>";
                xml += "	<detail>";
                xml += "	<productId>" + productId+"</productId>";
                xml += "	<customerId>" + dlSystem.getResourceAsXML("customer.default.id")+"</customerId>";
                xml += "	<taxId>" + taxId +"</taxId>";
                xml += "	<unitId>" + unitId +"</unitId>";
                xml += "	<priceListId>" + dlSystem.getResourceAsXML("price.listId")+"</priceListId>";
                xml += "	<organization>" + JRootApp.jmsInQueue+"</organization>";
                xml += "	<multiply>" + multiply +"</multiply>";
                xml += "	</detail>";
                xml += "</entityDetail>";
                MQClient.sendMessage(xml);
        
    }
        
    /**
     * Creates a new XML messahe requesting the creation of a purchase order to the ERP
     * @param d   SmartPOS
     */
    private void createInsuranceOrderERP(String productId, double multiply,String taxId,String unitId) {
   
                String xml ="";
                xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
                xml += "<entityDetail>";
                xml += "	<type>insurance-order</type>";
                xml += "	<detail>";
                xml += "	<productId>" + productId+"</productId>";
                xml += "	<customerId>" + dlSystem.getResourceAsXML("customer.default.id")+"</customerId>";
                xml += "	<taxId>" + taxId +"</taxId>";
                xml += "	<unitId>" + unitId +"</unitId>";
                xml += "	<priceListId>" + dlSystem.getResourceAsXML("price.listId")+"</priceListId>";
                xml += "	<organization>" + JRootApp.jmsInQueue+"</organization>";
                xml += "	<multiply>" + multiply +"</multiply>";
                xml += "	</detail>";
                xml += "</entityDetail>";
                MQClient.sendMessage(xml);
        
    }
    
           
    private boolean closeTicket(TicketInfo ticket, Object ticketext) {
        boolean resultok = false;
        if (m_App.getAppUserView().getUser().hasPermission("sales.Total")) {
            try {
                // reset the payment info
                taxeslogic.calculateTaxes(ticket);
                // incluye la logica de revision de impuestos
               // check insurance coverage                
                // ticket =  checkInsuranceCoverage(ticket);
                if (ticket.getTotal()>=0.0){
                    ticket.resetPayments(); //Only reset if is sale
                }
                
                if (executeEvent(ticket, ticketext, "ticket.total") == null) {

                    // Muestro el total
                    printTicket("Printer.TicketTotal", ticket, ticketext);
                      // check insurance coverage                
                 ticket =  checkInsuranceCoverage(ticket);
                    
                    // Select the Payments information
                    JPaymentSelect paymentdialog = ticket.getTicketType() == TicketInfo.RECEIPT_NORMAL
                            ? paymentdialogreceipt
                            : paymentdialogrefund;
                    paymentdialog.setPrintSelected("true".equals(m_jbtnconfig.getProperty("printselected", "true")));

                    paymentdialog.setTransactionID(ticket.getTransactionID());

                    if (paymentdialog.showDialog(ticket.getTotal(), ticket.getCustomer())) {

                        // assign the payments selected and calculate taxes.         
                        ticket.setPayments(paymentdialog.getSelectedPayments());

                        // Asigno los valores definitivos del ticket...
                        ticket.setUser(m_App.getAppUserView().getUser().getUserInfo()); // El usuario que lo cobra
                        ticket.setActiveCash(m_App.getActiveCashIndex());
                        ticket.setDate(new Date()); // Le pongo la fecha de cobro

                        if (executeEvent(ticket, ticketext, "ticket.save") == null) {
                            // Save the receipt and assign a receipt number
                            try {
                                dlSales.saveTicket(ticket, m_App.getInventoryLocation());                       
                            } catch (BasicException eData) {
                            Main.logger.error(eData.getMessage(), eData);
                            Main.logger.info("Recreate ticket...");
                            try {
                                
                             String idd=String.valueOf(Calendar.getInstance().getTimeInMillis());//@ade
                             ticket.setId(idd);
                              for (TicketLineInfo t: ticket.getLines())
                              { 
                                   t.setTicket(idd);
                              }
                              
                                dlSales.saveTicket(ticket, m_App.getInventoryLocation());                       
                            } catch (BasicException eData2) {
                               Main.logger.error(eData2.getMessage(), eData2);
                                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"), eData2);
                                msg.show(this); 
                                return false;//@win
                            }
                            }

                            executeEvent(ticket, ticketext, "ticket.close", new ScriptArg("print", paymentdialog.isPrintSelected()));

                            // Print receipt.
                            printTicket(paymentdialog.isPrintSelected()
                                    ? "Printer.Ticket"
                                    : "Printer.Ticket2", ticket, ticketext);
                            resultok = true;
                        }
                    }
                }
            } catch (TaxesException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotcalculatetaxes"));
                msg.show(this);
                resultok = false;
            }
            
            // reset the payment info
            m_oTicket.resetTaxes();
            m_oTicket.resetPayments();
        }
        
        // cancelled the ticket.total script
        // or canceled the payment dialog
        // or canceled the ticket.close script
        return resultok;        
    }
     
    // SMJ5
    TicketInfo checkInsuranceCoverage(TicketInfo ticket) {
        if (ticket.getCustomerId() != null  &&  // solo revisa cubrimiento de seguros a cliente validos 
                ticket.getTotal() > 0)    //y si no son devoluciones (> 0)
                                        
        {
        JInsurancePanel insurancePanel = new JInsurancePanel();
        try {
        // a partir del id del cliente, aqui debe buscar el plan valores de copago del paciente
        iInfo =  dlSales.getInsuranceInfo(ticket.getCustomerId());
        ticket.setiInfo(iInfo );  // deja info de aseguradora en el ticket
        if (iInfo != null &&  iInfo.getCopay_percentage() < 100 )    // just check if the customer has a valid insurance coverage
        {
        insurancePanel.setjInsuranceCompany(iInfo.getI_name());
        insurancePanel.setjPlan(iInfo.getP_name());
        insurancePanel.setjSubtotal(df.format(ticket.getTotal()));
        insurancePanel.setjCopayPercentage( df.format(iInfo.getCopay_percentage()));
        insurancePanel.setjCopayValue( df.format(iInfo.getCopay_value()));
        // luego a partir de los valores de copago, calcula el cubrimiento 
        
        if (iInfo.getCopay_percentage() > 0)    // Copay percentage
        {
            copay = (ticket.getTotal()* iInfo.getCopay_percentage() / 100) ;       
        }
        else {   //Copay per value
            copay = iInfo.getCopay_value();
        }
        coveredInsurance = ticket.getTotal() - copay  ;

        
        if (coveredInsurance < 0)  coveredInsurance = 0;

        insurancePanel.setjCovered( df.format(coveredInsurance));
        insurancePanel.setjTotal( df.format(copay));
        ticket.setCoveredInsurance( df.format(coveredInsurance));
        ticket.setCopay( df.format(copay));
        
        // pruebas 
        String xml = "";
        xml += "<CoveredInsurance>" + ticket.getCoveredInsurance()+"</CoveredInsurance>";
        xml += "<Copay>" + ticket.getCopay()+"</Copay>";
        xml += "<iName>" + ticket.getiInfo().getI_name()+"</iName>";
        xml += "<pName>" + ticket.getiInfo().getP_name()+"</pName>";
        xml += "<idInsurance>" + ticket.getiInfo().getId()+"</idInsurance>";
        //Main.logger.info("***** Insurance Info: "+xml);
        Main.logger.info("***** Insurance Info: "+xml);
        
        // muestra el panel de liquidacion previa
        int accion = JOptionPane.showConfirmDialog(null,insurancePanel,AppLocal.getIntString("insurance.details"),JOptionPane.OK_CANCEL_OPTION);
        if( accion == 0){ //  valores de loquidacion  aceptados 
                    // agrega linea de descentos al ticket
        
        String titu = "Desc. "  + AppLocal.getIntString("insuranceCompany.name");
        TaxInfo tInfo = dlSales.getTaxInfoByName("Standard");
        Main.logger.info (" ****  CoveredInsurance antes de df:"+ coveredInsurance);
        Main.logger.info (" ****  CoveredInsurance despues de df:"+ df.format(coveredInsurance));

        TicketLineInfo tlineInfo = new TicketLineInfo( titu, tInfo.getTaxCategoryID(),1 ,new Double(df.format(coveredInsurance)) * -1, tInfo);
        
        tlineInfo.setPrice(new Double(df.format(coveredInsurance)) * -1 );
        tlineInfo.setMultiply(1);
        tlineInfo.setProperty("product.name",titu);
        tlineInfo.setProperty("product.taxcategoryid",tInfo.getTaxCategoryID());
        tlineInfo.setProperty("sendstatus","Yes");
        tlineInfo.setProperty("notes","Insurance Coverage");
        tlineInfo.setProperty("discount",df.format(coveredInsurance));
        tlineInfo.setProperty("discount-value",df.format(coveredInsurance));
        tlineInfo.setProperty("discount-rate",df.format(coveredInsurance));
        
        ticket.addLine(tlineInfo);

        }   // accion
        
        }   // iInfo
        } catch (BasicException ex) {
            Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
        }
        // retotaliza el ticket y lo envia al pago normal
        refreshTicket();
        }
        return ticket;
    }
    
    
    
    /**
     * realizo modificaciones para envio de email cuando se hacen devoluciones, se agrego nuevos parametros para la ejecucion de los scripts.
     * changes carried out to send email when they return, they added new parameters for the execution of scripts.
     * @param sresourcename
     * @param ticket
     * @param ticketext 
     */
    private void printTicket(String sresourcename, TicketInfo ticket, Object ticketext) {
    VariableG.place=ticketext;
    //m_TTP = new TicketParser(m_App.getDeviceTicket(), dlSystem);
        //System.out.println("##### Device : " +m_App.getDeviceTicket());
        String sresource = dlSystem.getResourceAsXML(sresourcename);
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(JPanelTicket.this);
        } else {
            try {
                if(sresourcename.equals("Printer.TicketPreview") && !ticket.isSendAllArticles()){
                    JOptionPane.showMessageDialog(null, LocalRes.getIntString("message.sendcommandsbeforeprint"));
                }
                
                if(sresourcename.equals("Printer.TicketPreview")){
                try{
                        taxeslogic.calculateTaxes(ticket);
                }catch(Exception e){}
                }
                //System.out.println("$$$$$$$$ resource : " + sresource);        
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("taxes", taxcollection);
                script.put("taxeslogic", taxeslogic);
                script.put("ticket", ticket);

                script.put("place", ticketext);
                script.put("infoRUC", dlSystem.getResourceAsText("info.RUC"));
                script.put("infoDV", dlSystem.getResourceAsText("info.DV"));
                script.put("infoAddress", dlSystem.getResourceAsText("info.address"));
                script.put("infoCity", dlSystem.getResourceAsText("info.city"));
                script.put("infoEnd1", dlSystem.getResourceAsText("info.end1"));
                script.put("infoEnd2", dlSystem.getResourceAsText("info.end2"));
                script.put("printerId",this.printerId);
                script.put("printerLocation",this.printerLocation);
                m_TTP.printTicket(script.eval(sresource).toString());
                
                if(canSendEmail){
                    sendEmail(AppLocal.getIntString("email.rmatitle"));
                    canSendEmail = false;
                }
               
                
                if(sresourcename.equals("Printer.Ticket") && ticket.isPaymentFree()){
                    sendEmail(AppLocal.getIntString("email.freepayment"));
                }
                
                if(sresourcename.equals("Printer.TicketPreview")){
                try{
                        ticket.resetTaxes();
                }catch(Exception e){}
                }
                
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(JPanelTicket.this);
            } catch (TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(JPanelTicket.this);
            }catch (Exception e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                e.printStackTrace();
                msg.show(JPanelTicket.this);
            }
        }
    }

    public void setCanSendEmail(boolean canSendEmail) {
        this.canSendEmail = canSendEmail;
    }
    
    
    
    private void sendEmail(String message){
        Properties eProperties = dlSystem.getResourceAsPropertiesFromString("email.properties");
        EmailSender emailSender = new EmailSender(eProperties);

        String toString = eProperties.getProperty("to-list");
        List<String> list  = Arrays.asList( toString.split(",") );  

        StringBuffer text = new StringBuffer(message);
        text.append("<br/><br/>++++++++++++++++++++++++++<br/>");
        text.append(m_TTP.getTicketText());
        text.append("<br/>++++++++++++++++++++++++++<br/>");
// SmartPOS-TODO
        emailSender.send(list, eProperties.getProperty("from"), AppLocal.getIntString("email.return"), text.toString());
    }
    
    
    
    private void printReport(String resourcefile, TicketInfo ticket, Object ticketext) {
        
        try {     
         
            JasperReport jr;
           
            InputStream in = getClass().getResourceAsStream(resourcefile + ".ser");
            if (in == null) {      
                // read and compile the report
                JasperDesign jd = JRXmlLoader.load(getClass().getResourceAsStream(resourcefile + ".jrxml"));            
                jr = JasperCompileManager.compileReport(jd);    
            } else {
                // read the compiled reporte
                ObjectInputStream oin = new ObjectInputStream(in);
                jr = (JasperReport) oin.readObject();
                oin.close();
            }
           
            // Construyo el mapa de los parametros.
            Map reportparams = new HashMap();
            // reportparams.put("ARG", params);
            try {
                reportparams.put("REPORT_RESOURCE_BUNDLE", ResourceBundle.getBundle(resourcefile + ".properties"));
            } catch (MissingResourceException e) {
            }
            reportparams.put("TAXESLOGIC", taxeslogic); 
            
            Map reportfields = new HashMap();
            reportfields.put("TICKET", ticket);
            reportfields.put("PLACE", ticketext);

            JasperPrint jp = JasperFillManager.fillReport(jr, reportparams, new JRMapArrayDataSource(new Object[] { reportfields } ));
            
            PrintService service = ReportUtils.getPrintService(m_App.getProperties().getProperty("machine.printername"));
            
            JRPrinterAWT300.printPages(jp, 0, jp.getPages().size() - 1, service);
            
        } catch (Exception e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadreport"), e);
            msg.show(this);
        }               
    }

    private void visorTicketLine(TicketLineInfo oLine){
        if (oLine == null) { 
             m_App.getDeviceTicket().getDeviceDisplay().clearVisor();
        } else {                 
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("ticketline", oLine);
                m_TTP.printTicket(script.eval(dlSystem.getResourceAsXML("Printer.TicketLine")).toString());
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintline"), e);
                msg.show(JPanelTicket.this);
            } catch (TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintline"), e);
                msg.show(JPanelTicket.this);
            }
        } 
    }    
    
    
    private Object evalScript(ScriptObject scr, String resource, ScriptArg... args) {
        
        // resource here is guaratied to be not null
         try {
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            return scr.evalScript(dlSystem.getResourceAsXML(resource), args);                
        } catch (ScriptException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"), e);
            msg.show(this);
            return msg;
        } 
    }
        
    public void evalScriptAndRefresh(String resource, ScriptArg... args) {

        if (resource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"));
            msg.show(this);            
        } else {
            ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            evalScript(scr, resource, args);   
            refreshTicket();
            setSelectedIndex(scr.getSelectedIndex());
        }
    }  
    
    public void printTicket(String resource) {
        printTicket(resource, m_oTicket, m_oTicketExt);
    }
    
    public void setPrinter(String printerId){
        this.printerId = printerId;
    }
    
    
    public void setPrinterLocation(String printerLocation){
        this.printerLocation = printerLocation;
    }
    
    private Object executeEventAndRefresh(String eventkey, ScriptArg ... args) {
        
        String resource = m_jbtnconfig.getEvent(eventkey);
        if (resource == null) {
            return null;
        } else {
            ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            Object result = evalScript(scr, resource, args);   
            refreshTicket();
            setSelectedIndex(scr.getSelectedIndex());
            return result;
        }
    }
   
    private Object executeEvent(TicketInfo ticket, Object ticketext, String eventkey, ScriptArg ... args) {
        
        String resource = m_jbtnconfig.getEvent(eventkey);
        if (resource == null) {
            return null;
        } else {
            ScriptObject scr = new ScriptObject(ticket, ticketext);
            return evalScript(scr, resource, args);
        }
    }
    
    public String getResourceAsXML(String sresourcename) {
        return dlSystem.getResourceAsXML(sresourcename);
    }

    public BufferedImage getResourceAsImage(String sresourcename) {
        return dlSystem.getResourceAsImage(sresourcename);
    }
    
    private void setSelectedIndex(int i) {
        
        if (i >= 0 && i < m_oTicket.getLinesCount()) {
            m_ticketlines.setSelectedIndex(i);
        } else if (m_oTicket.getLinesCount() > 0) {
            m_ticketlines.setSelectedIndex(m_oTicket.getLinesCount() - 1);
        }    
    }

    private void initToolTips() {
        btnCustomer.setToolTipText(AppLocal.getIntString("rest.label.customer"));
        btnSplit.setToolTipText(AppLocal.getIntString("tooltiptext.splitaccount"));
        
    }

     
    public static class ScriptArg {
        private String key;
        private Object value;
        
        public ScriptArg(String key, Object value) {
            this.key = key;
            this.value = value;
        }
        public String getKey() {
            return key;
        }
        public Object getValue() {
            return value;
        }
    }
    
    public class ScriptObject {
        
        private TicketInfo ticket;
        private Object ticketext;
        
        private int selectedindex;
        
        private ScriptObject(TicketInfo ticket, Object ticketext) {
            this.ticket = ticket;
            this.ticketext = ticketext;
        }
        
        public double getInputValue() {
            if (m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
                return JPanelTicket.this.getInputValue();
            } else {
                return 0.0;
            }
        }
        
        public int getSelectedIndex() {
            return selectedindex;
            
        }
        
        public void setSelectedIndex(int i) {
            selectedindex = i;
        }  
        
        public void printReport(String resourcefile) {
            JPanelTicket.this.printReport(resourcefile, ticket, ticketext);
        }
        
        public void printTicket(String sresourcename) {
            JPanelTicket.this.printTicket(sresourcename, ticket, ticketext);   
        }
        
        public void setPrinter(String s){
            JPanelTicket.this.setPrinter(s);
        }
        
        public void setPrinterLocation(String s){
            JPanelTicket.this.setPrinterLocation(s);
        }
        
        /**
         * realizo modificaciones para envio de email cuando se hacen devoluciones, se agrego nuevos parametros para la ejecucion de los scripts.
         * changes carried out to send email when they return, they added new parameters for the execution of scripts.
         * @param code
         * @param args
         * @return Object
         * @throws ScriptException 
         */
        public Object evalScript(String code, ScriptArg... args) throws ScriptException{
            
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
            script.put("ticket", ticket);
            script.put("place", ticketext);
            script.put("taxes", taxcollection);
            script.put("taxeslogic", taxeslogic);             
            script.put("user", m_App.getAppUserView().getUser());
            script.put("sales", this);
            script.put("reasonToCancel",dlSystem.getResourceAsText("reasonToCancel"));
            script.put("reasonNotAffectInventory",dlSystem.getResourceAsText("reasonNotAffectInventory"));
            script.put("userDB",m_App.getProperties().getProperty("db.user"));
            script.put("passwordDB",m_App.getProperties().getProperty("db.password"));
            script.put("urlDB",m_App.getProperties().getProperty("db.URL"));
            script.put("jmsUrl",dlSystem.getResourceAsText("jms.url"));
            script.put("jmsUrlOut",dlSystem.getResourceAsText("jms.url.out"));
            script.put("jmsUserLogin",dlSystem.getResourceAsText("jms.userLogin"));
            script.put("jmsPassword",dlSystem.getResourceAsText("jms.password"));
            script.put("jmsOutQueue",dlSystem.getResourceAsText("jms.outqueue"));
            script.put("jmsInQueue",dlSystem.getResourceAsText("jms.inqueue"));
            script.put("sendCommands",Boolean.parseBoolean(dlSystem.getResourceAsText("send.commands")));
            script.put("razon",dlSystem.getResourceAsText("label.razon"));
            script.put("affectsInventory",dlSystem.getResourceAsText("label.affectsInventory"));
            script.put("pcTerminal",m_App.getProperties().getProperty("machine.hostname"));
            script.put("customerDefaultId",dlSystem.getResourceAsText("customer.default.id"));
            script.put("jRefundLines",jRefundLines);
            script.put("priceListID",dlSystem.getResourceAsText("price.listId"));
            script.put("loggerFile",logger);
            script.put("panel",this);
            String paymentSyncId = dlSystem.getResourceAsText("paymentSyncId");
            java.util.Properties proper = new Properties();
            try {
                InputStream  inputStream = new ByteArrayInputStream(paymentSyncId.getBytes("UTF-8"));
                proper.load(inputStream);  
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                
            } 
            script.put("paymentSyncId",proper);
            
            TaxCategoryInfo tcInfo;
            try {
                Iterator<TaxCategoryInfo> iterator = sentTaxCategoriesZeroRate.list().iterator();
                if(iterator.hasNext()){
                    tcInfo = iterator.next();
                    script.put("TCId",tcInfo.getID());
                }
            } catch (BasicException ex) {
                logger.log(Level.SEVERE, null, ex);
                
            }
            
            String printerMapping = dlSystem.getResourceAsText("Printer.Mapping");
            java.util.Properties proper1 = new Properties();
            printerMapping = printerMapping.toUpperCase();
            try {
                InputStream  inputStream = new ByteArrayInputStream(printerMapping.getBytes("UTF-8"));
                proper1.load(inputStream);  
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
                
            } 
            script.put("printerMapping",proper1);
             logger.log(Level.SEVERE, null, "******* Script interpretado:"+code);
             
            // more arguments
            for(ScriptArg arg : args) {
                logger.log(Level.SEVERE, null, "******* arg key procesado:"+arg.getKey()+"-- "+arg.getValue());
                script.put(arg.getKey(), arg.getValue());
                
            }             

            return script.eval(code);
        }
        public void setCanTicketSendEmail(boolean canSendEmail){
            setCanSendEmail(canSendEmail);
        }
    }

    public JRefundLines getjRefundLines() {
        return jRefundLines;
    }

    public void setjRefundLines(JRefundLines jRefundLines) {
        this.jRefundLines = jRefundLines;
    }
    
    
/** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        m_jPanContainer = new javax.swing.JPanel();
        m_jOptions = new javax.swing.JPanel();
        m_jButtons = new javax.swing.JPanel();
        m_jTicketId = new javax.swing.JLabel();
        btnCustomer = new javax.swing.JButton();
        btnSplit = new javax.swing.JButton();
        m_jPanelScripts = new javax.swing.JPanel();
        m_jButtonsExt = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        m_jbtnScale = new javax.swing.JButton();
        m_jPanelBag = new javax.swing.JPanel();
        m_jPanTicket = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        m_jUp = new javax.swing.JButton();
        m_jDown = new javax.swing.JButton();
        m_jDelete = new javax.swing.JButton();
        m_jList = new javax.swing.JButton();
        m_jEditLine = new javax.swing.JButton();
        jEditAttributes = new javax.swing.JButton();
        m_jPanelCentral = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jPanTotals = new javax.swing.JPanel();
        m_jTotalEuros = new javax.swing.JLabel();
        m_jLblTotalEuros1 = new javax.swing.JLabel();
        m_jSubtotalEuros = new javax.swing.JLabel();
        m_jTaxesEuros = new javax.swing.JLabel();
        m_jLblTotalEuros2 = new javax.swing.JLabel();
        m_jLblTotalEuros3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        m_jWaiter = new javax.swing.JLabel();
        m_jContEntries = new javax.swing.JPanel();
        m_jPanEntries = new javax.swing.JPanel();
        m_jNumberKeys = new com.openbravo.beans.JNumberKeys();
        jPanel9 = new javax.swing.JPanel();
        m_jPrice = new javax.swing.JLabel();
        m_jPor = new javax.swing.JLabel();
        m_jEnter = new javax.swing.JButton();
        m_jTax = new javax.swing.JComboBox();
        m_jaddtax = new javax.swing.JToggleButton();
        m_jKeyFactory = new javax.swing.JTextField();
        catcontainer = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 204, 153));
        setLayout(new java.awt.CardLayout());

        m_jPanContainer.setLayout(new java.awt.BorderLayout());

        m_jOptions.setLayout(new java.awt.BorderLayout());

        m_jTicketId.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jTicketId.setOpaque(true);
        m_jTicketId.setPreferredSize(new java.awt.Dimension(160, 25));
        m_jTicketId.setRequestFocusEnabled(false);
        m_jTicketId.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                m_jTicketIdMouseClicked(evt);
            }
        });
        m_jButtons.add(m_jTicketId);

        btnCustomer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/kuser.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        btnCustomer.setToolTipText(bundle.getString("Menu.Customers")); // NOI18N
        btnCustomer.setFocusPainted(false);
        btnCustomer.setFocusable(false);
        btnCustomer.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnCustomer.setRequestFocusEnabled(false);
        btnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomerActionPerformed(evt);
            }
        });
        m_jButtons.add(btnCustomer);

        btnSplit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/editcut.png"))); // NOI18N
        btnSplit.setToolTipText(bundle.getString("caption.split")); // NOI18N
        btnSplit.setFocusPainted(false);
        btnSplit.setFocusable(false);
        btnSplit.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnSplit.setRequestFocusEnabled(false);
        btnSplit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSplitActionPerformed(evt);
            }
        });
        m_jButtons.add(btnSplit);

        m_jOptions.add(m_jButtons, java.awt.BorderLayout.LINE_START);

        m_jPanelScripts.setLayout(new java.awt.BorderLayout());

        m_jButtonsExt.setLayout(new javax.swing.BoxLayout(m_jButtonsExt, javax.swing.BoxLayout.LINE_AXIS));

        m_jbtnScale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ark216.png"))); // NOI18N
        m_jbtnScale.setText(AppLocal.getIntString("button.scale")); // NOI18N
        m_jbtnScale.setFocusPainted(false);
        m_jbtnScale.setFocusable(false);
        m_jbtnScale.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnScale.setRequestFocusEnabled(false);
        m_jbtnScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnScaleActionPerformed(evt);
            }
        });
        jPanel1.add(m_jbtnScale);

        m_jButtonsExt.add(jPanel1);

        m_jPanelScripts.add(m_jButtonsExt, java.awt.BorderLayout.LINE_END);

        m_jOptions.add(m_jPanelScripts, java.awt.BorderLayout.LINE_END);

        m_jPanelBag.setLayout(new java.awt.BorderLayout());
        m_jOptions.add(m_jPanelBag, java.awt.BorderLayout.CENTER);

        m_jPanContainer.add(m_jOptions, java.awt.BorderLayout.NORTH);

        m_jPanTicket.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jPanTicket.setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanel2.setLayout(new java.awt.GridLayout(0, 1, 5, 5));

        m_jUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/1uparrow22.png"))); // NOI18N
        m_jUp.setToolTipText(bundle.getString("tooltip.up")); // NOI18N
        m_jUp.setFocusPainted(false);
        m_jUp.setFocusable(false);
        m_jUp.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jUp.setRequestFocusEnabled(false);
        m_jUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jUpActionPerformed(evt);
            }
        });
        jPanel2.add(m_jUp);

        m_jDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/1downarrow22.png"))); // NOI18N
        m_jDown.setToolTipText(bundle.getString("tooltip.down")); // NOI18N
        m_jDown.setFocusPainted(false);
        m_jDown.setFocusable(false);
        m_jDown.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jDown.setRequestFocusEnabled(false);
        m_jDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDownActionPerformed(evt);
            }
        });
        jPanel2.add(m_jDown);

        m_jDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/locationbar_erase.png"))); // NOI18N
        m_jDelete.setToolTipText(bundle.getString("tooltip.removeLine")); // NOI18N
        m_jDelete.setFocusPainted(false);
        m_jDelete.setFocusable(false);
        m_jDelete.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jDelete.setRequestFocusEnabled(false);
        m_jDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDeleteActionPerformed(evt);
            }
        });
        jPanel2.add(m_jDelete);

        m_jList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/search22.png"))); // NOI18N
        m_jList.setMnemonic(KeyEvent.VK_F);
        m_jList.setToolTipText(bundle.getString("tooltip.findProducts")); // NOI18N
        m_jList.setFocusPainted(false);
        m_jList.setFocusable(false);
        m_jList.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jList.setRequestFocusEnabled(false);
        m_jList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jListActionPerformed(evt);
            }
        });
        jPanel2.add(m_jList);

        m_jEditLine.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/color_line.png"))); // NOI18N
        m_jEditLine.setToolTipText(bundle.getString("tooltip.EditLine")); // NOI18N
        m_jEditLine.setEnabled(false);
        m_jEditLine.setFocusPainted(false);
        m_jEditLine.setFocusable(false);
        m_jEditLine.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jEditLine.setRequestFocusEnabled(false);
        m_jEditLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEditLineActionPerformed(evt);
            }
        });
        jPanel2.add(m_jEditLine);

        jEditAttributes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/colorize.png"))); // NOI18N
        jEditAttributes.setEnabled(false);
        jEditAttributes.setFocusPainted(false);
        jEditAttributes.setFocusable(false);
        jEditAttributes.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jEditAttributes.setRequestFocusEnabled(false);
        jEditAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEditAttributesActionPerformed(evt);
            }
        });
        jPanel2.add(jEditAttributes);

        jPanel5.add(jPanel2, java.awt.BorderLayout.NORTH);

        m_jPanTicket.add(jPanel5, java.awt.BorderLayout.LINE_END);

        m_jPanelCentral.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.BorderLayout());

        m_jPanTotals.setLayout(new java.awt.GridBagLayout());

        m_jTotalEuros.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        m_jTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        m_jTotalEuros.setText("0");
        m_jTotalEuros.setOpaque(true);
        m_jTotalEuros.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jTotalEuros.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jTotalEuros, gridBagConstraints);

        m_jLblTotalEuros1.setText(AppLocal.getIntString("label.totalcash")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jLblTotalEuros1, gridBagConstraints);

        m_jSubtotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        m_jSubtotalEuros.setText("0");
        m_jSubtotalEuros.setOpaque(true);
        m_jSubtotalEuros.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jSubtotalEuros.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jSubtotalEuros, gridBagConstraints);

        m_jTaxesEuros.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        m_jTaxesEuros.setText("0");
        m_jTaxesEuros.setOpaque(true);
        m_jTaxesEuros.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jTaxesEuros.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        m_jPanTotals.add(m_jTaxesEuros, gridBagConstraints);

        m_jLblTotalEuros2.setText(AppLocal.getIntString("label.taxcash")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        m_jPanTotals.add(m_jLblTotalEuros2, gridBagConstraints);

        m_jLblTotalEuros3.setText(AppLocal.getIntString("label.subtotalcash")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jLblTotalEuros3, gridBagConstraints);

        jPanel4.add(m_jPanTotals, java.awt.BorderLayout.LINE_END);

        jLabel1.setText("Waiter : ");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel4.add(jLabel1, java.awt.BorderLayout.LINE_START);
        jLabel1.getAccessibleContext().setAccessibleDescription("");

        m_jWaiter.setBackground(java.awt.Color.white);
        m_jWaiter.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel4.add(m_jWaiter, java.awt.BorderLayout.CENTER);

        m_jPanelCentral.add(jPanel4, java.awt.BorderLayout.SOUTH);

        m_jPanTicket.add(m_jPanelCentral, java.awt.BorderLayout.CENTER);

        m_jPanContainer.add(m_jPanTicket, java.awt.BorderLayout.CENTER);

        m_jContEntries.setLayout(new java.awt.BorderLayout());

        m_jPanEntries.setLayout(new javax.swing.BoxLayout(m_jPanEntries, javax.swing.BoxLayout.Y_AXIS));

        m_jNumberKeys.addJNumberEventListener(new com.openbravo.beans.JNumberEventListener() {
            public void keyPerformed(com.openbravo.beans.JNumberEvent evt) {
                m_jNumberKeysKeyPerformed(evt);
            }
        });
        m_jPanEntries.add(m_jNumberKeys);

        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel9.setLayout(new java.awt.GridBagLayout());

        m_jPrice.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jPrice.setOpaque(true);
        m_jPrice.setPreferredSize(new java.awt.Dimension(100, 22));
        m_jPrice.setRequestFocusEnabled(false);
        m_jPrice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                m_jPriceKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel9.add(m_jPrice, gridBagConstraints);

        m_jPor.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jPor.setOpaque(true);
        m_jPor.setPreferredSize(new java.awt.Dimension(22, 22));
        m_jPor.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(m_jPor, gridBagConstraints);

        m_jEnter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/barcode.png"))); // NOI18N
        m_jEnter.setFocusPainted(false);
        m_jEnter.setFocusable(false);
        m_jEnter.setRequestFocusEnabled(false);
        m_jEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(m_jEnter, gridBagConstraints);

        m_jTax.setFocusable(false);
        m_jTax.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel9.add(m_jTax, gridBagConstraints);

        m_jaddtax.setText("+");
        m_jaddtax.setFocusPainted(false);
        m_jaddtax.setFocusable(false);
        m_jaddtax.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel9.add(m_jaddtax, gridBagConstraints);

        m_jPanEntries.add(jPanel9);

        m_jKeyFactory.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setForeground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setBorder(null);
        m_jKeyFactory.setCaretColor(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setPreferredSize(new java.awt.Dimension(1, 1));
        m_jKeyFactory.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                m_jKeyFactoryKeyTyped(evt);
            }
        });
        m_jPanEntries.add(m_jKeyFactory);

        m_jContEntries.add(m_jPanEntries, java.awt.BorderLayout.NORTH);

        m_jPanContainer.add(m_jContEntries, java.awt.BorderLayout.LINE_END);

        catcontainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        catcontainer.setLayout(new java.awt.BorderLayout());
        m_jPanContainer.add(catcontainer, java.awt.BorderLayout.SOUTH);

        add(m_jPanContainer, "ticket");
    }// </editor-fold>//GEN-END:initComponents

    private void m_jbtnScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnScaleActionPerformed

        stateTransition('\u00a7');
        
    }//GEN-LAST:event_m_jbtnScaleActionPerformed

    private void m_jEditLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEditLineActionPerformed
       
        // edita la linea de producto
        if(m_oTicket.getTicketId() == 0){
            int i = m_ticketlines.getSelectedIndex();
            if (i < 0){
                Toolkit.getDefaultToolkit().beep(); // no line selected
            } else {
                try {
                   TicketLineInfo newline = m_oTicket.getLine(i);
                   //TicketLineInfo newlineBack = m_oTicket.getLine(i);
                
                   //MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, newline.printPrice(), "Precio del articulo");
                   //msg.show(this);
                   if(newline.getProductName().indexOf("Desc.") < 0 && newline.getProductName().indexOf("Propina") < 0){
                       newline = JProductLineEdit.showMessage(this, m_App, newline,dlSales,dlSystem);
                     if (newline != null) {
                         paintTicketLine(i, newline);
          // SmartPOS         
          // busca existencias del producto en la localizacion(bodega) por defecto 0
                    double totProd = getCantidadTotalProductoTicket(newline.getProductID(), newline.getProperty("unidadDefault"));
                    // Main.logger.info(" 4 cantidad preliminar de producto:"+totProd); 
                           
                    //double available = checkStock(newline.getProductID().toString(), totProd, newline.getProperty("unidadDefault") ) ;
                   // line has been modified
                     
                    // @win :hilangkan pengecekan stock
                    //if (totProd == available ){ 
                    //}        
                    //else {
                         //  Main.logger.info("4 cantidad sugerida  2:"+ available   );
                         // newline.setMultiply(available );
                    //}
                            paintTicketLine(i, newline);
                  }
                    }
                } catch (BasicException e) {
                    new MessageInf(e).show(this);
                }
            }
        }

    }//GEN-LAST:event_m_jEditLineActionPerformed

    private void m_jEnterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEnterActionPerformed

        stateTransition('\n');

    }//GEN-LAST:event_m_jEnterActionPerformed

    private void m_jNumberKeysKeyPerformed(com.openbravo.beans.JNumberEvent evt) {//GEN-FIRST:event_m_jNumberKeysKeyPerformed

        stateTransition(evt.getKey());

    }//GEN-LAST:event_m_jNumberKeysKeyPerformed

    private void m_jKeyFactoryKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_jKeyFactoryKeyTyped

        m_jKeyFactory.setText(null);
        stateTransition(evt.getKeyChar());

    }//GEN-LAST:event_m_jKeyFactoryKeyTyped

    private void m_jDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDeleteActionPerformed

        int i = m_ticketlines.getSelectedIndex();
        if (i < 0){
            Toolkit.getDefaultToolkit().beep(); // No hay ninguna seleccionada
        } else {               
            removeTicketLine(i); // elimino la linea           
        }   
        
    }//GEN-LAST:event_m_jDeleteActionPerformed

    private void m_jUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jUpActionPerformed
        
        m_ticketlines.selectionUp();

    }//GEN-LAST:event_m_jUpActionPerformed

    private void m_jDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDownActionPerformed

        m_ticketlines.selectionDown();

    }//GEN-LAST:event_m_jDownActionPerformed

    private void m_jListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jListActionPerformed

        ProductInfoExt prod = JProductFinder.showMessage(JPanelTicket.this, dlSales);    
        if (prod != null) {
            buttonTransition(prod);
        }
        
    }//GEN-LAST:event_m_jListActionPerformed

    private void btnCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomerActionPerformed
        if(m_oTicket.getTicketId() == 0){
            JCustomerFinder finder = JCustomerFinder.getCustomerFinder(this, dlCustomers,m_App);
            finder.search(m_oTicket.getCustomer());
            finder.setVisible(true);

            try {
                m_oTicket.setCustomer(finder.getSelectedCustomer() == null ? null
                        : dlSales.loadCustomerExt(finder.getSelectedCustomer().getId()));
            } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"), e);
                msg.show(this);            
            }

            refreshTicket();
        }
        
}//GEN-LAST:event_btnCustomerActionPerformed

    private void btnSplitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSplitActionPerformed
        if(m_oTicket.getTicketId() == 0){
            if (m_oTicket.getLinesCount() > 0) {
                ReceiptSplit splitdialog = ReceiptSplit.getDialog(this, dlSystem.getResourceAsXML("Ticket.Line"), dlSales, dlCustomers, taxeslogic);

                TicketInfo ticket1 = m_oTicket.copyTicket();
                TicketInfo ticket2 = new TicketInfo();
                ticket2.setCustomer(m_oTicket.getCustomer());

                if (splitdialog.showDialog(ticket1, ticket2, m_oTicketExt)) {
                    if (closeTicket(ticket2, m_oTicketExt)) { // already checked  that number of lines > 0                            
                        setActiveTicket(ticket1, m_oTicketExt);// set result ticket
                    }
                }
            }
        }
        
}//GEN-LAST:event_btnSplitActionPerformed

    private void jEditAttributesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEditAttributesActionPerformed

        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
            Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            try {
                TicketLineInfo line = m_oTicket.getLine(i);
                JProductAttEdit attedit = JProductAttEdit.getAttributesEditor(this, m_App.getSession());
                attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
                attedit.setVisible(true);
                if (attedit.isOK()) {
                    // The user pressed OK
                    line.setProductAttSetInstId(attedit.getAttributeSetInst());
                    line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
                    paintTicketLine(i, line);
                }
            } catch (BasicException ex) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindattributes"), ex);
                msg.show(this);
            }
        }
        
}//GEN-LAST:event_jEditAttributesActionPerformed

    private void m_jPriceKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_jPriceKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jPriceKeyTyped

    private void m_jTicketIdMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_jTicketIdMouseClicked
        if (JRootApp.currentCustomer  != null)
        {
        //new MessageInf(MessageInf.SGN_NOTICE,"Cliente actual"+JRootApp.currentCustomer ).show(this);        
            try {
                m_oTicket.setCustomer(dlSales.loadCustomerExtByTaxId(JRootApp.currentCustomer ));
            } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"), e);
                msg.show(this);            
            }
            refreshTicket();
        }
    }//GEN-LAST:event_m_jTicketIdMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnSplit;
    private javax.swing.JPanel catcontainer;
    private javax.swing.JButton jEditAttributes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel m_jButtons;
    private javax.swing.JPanel m_jButtonsExt;
    private javax.swing.JPanel m_jContEntries;
    private javax.swing.JButton m_jDelete;
    private javax.swing.JButton m_jDown;
    private javax.swing.JButton m_jEditLine;
    private javax.swing.JButton m_jEnter;
    private javax.swing.JTextField m_jKeyFactory;
    private javax.swing.JLabel m_jLblTotalEuros1;
    private javax.swing.JLabel m_jLblTotalEuros2;
    private javax.swing.JLabel m_jLblTotalEuros3;
    private javax.swing.JButton m_jList;
    private com.openbravo.beans.JNumberKeys m_jNumberKeys;
    private javax.swing.JPanel m_jOptions;
    private javax.swing.JPanel m_jPanContainer;
    private javax.swing.JPanel m_jPanEntries;
    private javax.swing.JPanel m_jPanTicket;
    private javax.swing.JPanel m_jPanTotals;
    private javax.swing.JPanel m_jPanelBag;
    private javax.swing.JPanel m_jPanelCentral;
    private javax.swing.JPanel m_jPanelScripts;
    private javax.swing.JLabel m_jPor;
    private javax.swing.JLabel m_jPrice;
    private javax.swing.JLabel m_jSubtotalEuros;
    private javax.swing.JComboBox m_jTax;
    private javax.swing.JLabel m_jTaxesEuros;
    private javax.swing.JLabel m_jTicketId;
    private javax.swing.JLabel m_jTotalEuros;
    private javax.swing.JButton m_jUp;
    private javax.swing.JLabel m_jWaiter;
    private javax.swing.JToggleButton m_jaddtax;
    private javax.swing.JButton m_jbtnScale;
    // End of variables declaration//GEN-END:variables

}
