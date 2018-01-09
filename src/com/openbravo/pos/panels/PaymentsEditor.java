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

import java.awt.Component;
import java.util.UUID;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.main.Main;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.smj.MQClient;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * @author adrianromero
 */
public class PaymentsEditor extends javax.swing.JPanel implements EditorRecord {
    
    private ComboBoxValModel m_ReasonModel;
    private String key1 = "cashout";
    
    private String m_sId;
    private String m_sPaymentId;
    private Date datenew;
    private String m_sNotes;
    private AppView m_App;
    private DataLogicSystem dlsystem;
    private static Logger logger = Logger.getLogger(PaymentsEditor.class.getName());
    
    private TicketParser m_TTP;
    //private String pilih;
    
    /** Creates new form JPanelPayments */
    public PaymentsEditor(AppView oApp, DirtyManager dirty) {
        
        m_App = oApp;
        dlsystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        
        initComponents();
    
        m_ReasonModel = new ComboBoxValModel();
        m_ReasonModel.add(new PaymentReasonNegative("cashout", AppLocal.getIntString("transpayment.cashout")));
        //m_ReasonModel.add(new PaymentReasonPositive("cashin", AppLocal.getIntString("transpayment.cashin")));//disable cash(in) bikin ngaco accounting ke ERP nya
        //m_ReasonModel.setSelectedFirst();
        
        
        m_jreason.setModel(m_ReasonModel);
               
        jTotal.addEditorKeys(m_jKeys);
        m_jreason.addActionListener(dirty);
        jTotal.addPropertyChangeListener("Text", dirty);
        m_TTP = new TicketParser(m_App.getDeviceTicket(), dlsystem);//@win

        writeValueEOF();
    }
    
    public void writeValueEOF() {
        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        setReasonTotal(null, null);
        m_jreason.setEnabled(false);
        jTotal.setEnabled(false);
        //smj 
        m_sNotes = null;
        jNotes.setEnabled(false);
    }  
    
    public void writeValueInsert() {
        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        //setReasonTotal("cashin", null);
        setReasonTotal("cashout", null);
        m_jreason.setEnabled(true);
        jTotal.setEnabled(true);   
        jTotal.activate();
        //smj 
        m_sNotes = null;
        jNotes.setEnabled(true);
        jNotes.setText(m_sNotes);
    }
    
    public void writeValueDelete(Object value) {
        Object[] payment = (Object[]) value;
        m_sId = (String) payment[0];
        datenew = (Date) payment[2];
        m_sPaymentId = (String) payment[3];
        setReasonTotal(payment[4], payment[5]);
        m_jreason.setEnabled(false);
        jTotal.setEnabled(false);
        //smj  
        m_sNotes = (String) payment[6];
        jNotes.setEnabled(false);
    }
    
    public void writeValueEdit(Object value) {
        Object[] payment = (Object[]) value;
        m_sId = (String) payment[0];
        datenew = (Date) payment[2];
        m_sPaymentId = (String) payment[3];
        setReasonTotal(payment[4], payment[5]);
        m_jreason.setEnabled(false);
        jTotal.setEnabled(false);
        jTotal.activate();
        
        //SMJ
        m_sNotes = (String) payment[6];
        jNotes.setEnabled(false);
        
    }
    
    public Object createValue() throws BasicException {
        //SMJ
        Object[] payment = new Object[7];
        payment[0] = m_sId == null ? UUID.randomUUID().toString() : m_sId;
        payment[1] = m_App.getActiveCashIndex();
        payment[2] = datenew == null ? new Date() : datenew;
        payment[3] = m_sPaymentId == null ? UUID.randomUUID().toString() : m_sPaymentId;
        payment[4] = m_ReasonModel.getSelectedKey();
        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();
        Double dtotal = jTotal.getDoubleValue();
        payment[5] = reason == null ? dtotal : reason.addSignum(dtotal);
        if(dtotal != null)
            sendCustomerPayment();
            printPayments("Printer.CashOut");
        //SMJ
        String snotes = "";
        m_sNotes = jNotes.getText();
        payment[6] = m_sNotes == null ? snotes : m_sNotes;
                
        return payment;
    }
    
    /**
     * envio de mensajes XML al ERP para que registre entradas y salidas de caja
     * send xml message ERP to record cash inflows and outflows                       SmartPOS
     */
    private void sendCustomerPayment(){
                
                String xml ="";
                xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
                xml += "<entityDetail>";
                xml += "	<type>cash-in-out</type>";
                xml += "	<detail>";
                xml += "	<waiter-login>" +m_App.getAppUserView().getUser().getName() +"</waiter-login>";
                xml += "	<customerID>" +dlsystem.getResourceAsText("customer.default.id") +"</customerID>";
                xml += "	<payment-value>" + jTotal.getDoubleValue()+"</payment-value>";
                xml += "	<organization>" + dlsystem.getResourceAsText("jms.inqueue")+"</organization>";
                xml += "	<in-out>" + m_ReasonModel.getSelectedKey()+"</in-out>";
                xml += "	</detail>";
                xml += "</entityDetail>";
                MQClient.sendMessage(xml);
                Main.logger.info("$$$$$$$$$$ sendCustomerPayment : \n" +xml);
    }
    
    public Component getComponent() {
        return this;
    }
    
    public void refresh() {
    }  
    
    private void setReasonTotal(Object reasonfield, Object totalfield) {
        
        m_ReasonModel.setSelectedKey(reasonfield);
             
        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();     
        
        if (reason == null) {
            jTotal.setDoubleValue((Double) totalfield);
        } else {
            jTotal.setDoubleValue(reason.positivize((Double) totalfield));
        }  
    }
    
     private void printPayments(String cashOut) {//@win
        String sresource = dlsystem.getResourceAsXML(cashOut);    
        //System.out.println("$$$$$$$$$$$$$$$$$$ CashOUT : " +sresource);
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
        }else{
            try{
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("outcashier", m_App.getAppUserView().getUser().getName());
                script.put("outdate", new Date());
                script.put("outtotal",jTotal.getDoubleValue());
                script.put("outnotes",jNotes.getText());
                System.out.println("$$$$$$$$$$$$$$ script-eval CashOut : " +script.eval(sresource));
                m_TTP.printTicket(script.eval(sresource).toString());
                Main.logger.info("$$$$$$$$$$  Print CashOut :\n " + script.eval(sresource).toString() );
                
            } 
            catch (ScriptException e) {
                e.printStackTrace();
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            } catch (TicketPrinterException e) {
                e.printStackTrace();
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
                
            } catch (Exception e){e.printStackTrace();}
            
            
        }
    }
     
    private static abstract class PaymentReason implements IKeyed {
        private String m_sKey;
        private String m_sText;
        
        public PaymentReason(String key, String text) {
            m_sKey = key;
            m_sText = text;
        }
        public Object getKey() {
            return m_sKey;
        }
        public abstract Double positivize(Double d);
        public abstract Double addSignum(Double d);
        
        @Override
        public String toString() {
            return m_sText;
        }
    }
    private static class PaymentReasonPositive extends PaymentReason {
        public PaymentReasonPositive(String key, String text) {
            super(key, text);
        }
        public Double positivize(Double d) {
            return d;
        }
        public Double addSignum(Double d) {
            if (d == null) {
                return null;
            } else if (d.doubleValue() < 0.0) {
                return new Double(-d.doubleValue());
            } else {
                return d;
            }
        }
    }
    private static class PaymentReasonNegative extends PaymentReason {
        public PaymentReasonNegative(String key, String text) {
            super(key, text);
        }
        public Double positivize(Double d) {
            return d == null ? null : new Double(-d.doubleValue());
        }
        public Double addSignum(Double d) {
            if (d == null) {
                return null;
            } else if (d.doubleValue() > 0.0) {
                return new Double(-d.doubleValue());
            } else {
                return d;
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        m_jreason = new javax.swing.JComboBox();
        jTotal = new com.openbravo.editor.JEditorCurrency();
        jNotes = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();

        setLayout(new java.awt.BorderLayout());

        m_jreason.setFocusable(false);
        m_jreason.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jreasonActionPerformed(evt);
            }
        });

        jTotal.setOpaque(false);

        jNotes.setToolTipText("Observaciones");

        jLabel1.setText("Payment Type");

        jLabel2.setText("Nominal");

        jLabel6.setText("Notes");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel6))
                .addGap(72, 72, 72)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jreason, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(91, 91, 91))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jreason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(11, 11, 11)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel6))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(197, Short.MAX_VALUE))
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(m_jKeys, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jreasonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jreasonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jreasonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField jNotes;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private com.openbravo.editor.JEditorCurrency jTotal;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JComboBox m_jreason;
    // End of variables declaration//GEN-END:variables
    
}
