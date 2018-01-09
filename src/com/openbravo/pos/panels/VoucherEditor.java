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

import com.ewallet.client.RestClient;
import com.ewallet.entity.Voucher;
import java.awt.Component;
import java.util.UUID;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
//import com.openbravo.pos.dao.VoucherDAO;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.smj.MQClient;
;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author adrianromero
 */
public class VoucherEditor extends javax.swing.JPanel implements EditorRecord {

    private ComboBoxValModel m_ReasonModel;
    private String m_sId;
    private String m_sPaymentId;
    private Date datenew;
    private String m_sNotes;
    private AppView m_App;
    private DataLogicSystem dlsystem;
    //private static Logger logger = Logger.getLogger(VoucherEditor.class.getName());

    /** Creates new form JPanelPayments */
    public VoucherEditor(AppView oApp, DirtyManager dirty) {

        m_App = oApp;
        dlsystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        initComponents();

        m_ReasonModel = new ComboBoxValModel();
        //m_ReasonModel.add(new PaymentReasonPositive("cashin", AppLocal.getIntString("transpayment.cashin")));//disable cash(in) bikin ngaco accounting ke ERP nya
        m_ReasonModel.add(new PaymentReasonNegative("cashout", AppLocal.getIntString("transpayment.cashout")));



        writeValueEOF();
    }

    public void writeValueEOF() {
        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        setReasonTotal(null, null);
        //smj 
        m_sNotes = null;
        //jNotes.setEnabled(false);
    }

    public void writeValueInsert() {
    }

    public void writeValueDelete(Object value) {
    }

    public void writeValueEdit(Object value) {
    }

    public Object createValue() throws BasicException {
        //SMJ
        Object[] payment = new Object[7];


        return payment;
    }

    /**
     * envio de mensajes XML al ERP para que registre entradas y salidas de caja
     * send xml message ERP to record cash inflows and outflows                       SmartPOS
     */
    private void sendCustomerPayment() {
    }

    public Component getComponent() {
        return this;
    }

    public void refresh() {
    }

    private void setReasonTotal(Object reasonfield, Object totalfield) {
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
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtQty = new javax.swing.JTextField();
        txtNominal = new javax.swing.JTextField();
        dpStart = new org.jdesktop.swingx.JXDatePicker();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        dpEnd = new org.jdesktop.swingx.JXDatePicker();
        cbType = new javax.swing.JComboBox();
        btnGenerate = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();

        setLayout(new java.awt.BorderLayout());

        jLabel5.setText(AppLocal.getIntString("label.paymentreason")); // NOI18N

        jLabel3.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N

        jLabel4.setText(AppLocal.getIntString("button.note")); // NOI18N

        jLabel1.setText("Type");

        txtNominal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNominalActionPerformed(evt);
            }
        });

        jLabel2.setText("From");

        jLabel6.setText("To");

        cbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Belanja", "Diskon" }));
        cbType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTypeActionPerformed(evt);
            }
        });

        btnGenerate.setText("Generate Voucher");
        btnGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNominal, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dpStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dpEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(btnGenerate))))
                .addContainerGap(95, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtQty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtNominal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2)
                    .addComponent(dpStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(dpEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnGenerate)
                .addContainerGap(126, Short.MAX_VALUE))
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(m_jKeys, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void btnGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerateActionPerformed
        try{
            int qty = Integer.parseInt(txtQty.getText());
            int size = String.valueOf(qty).length();
            List<Voucher> vouchers= new  ArrayList<Voucher>();
            for (int i = 1; i <= qty; i++) {
                String a = String.valueOf(i);
                while (a.length() < size) {
                    a = "0" + a;
                }
                //System.out.println(numbGen(13 - size) + a);
                Voucher v= new Voucher();
                v.setId(numbGen(13 - size) + a);
                v.setNominal(Double.parseDouble(txtNominal.getText()));
                v.setStartDate(dpStart.getDate());
                v.setEndDate(dpEnd.getDate());
                v.setType(cbType.getSelectedItem().toString());
                v.setStatus(1);
                //VoucherDAO.getInstance().insert(v);
                RestClient.createVoucher(v);
                vouchers.add(v);
                clear();
            }

            JOptionPane.showMessageDialog(null, "Voucher  created successfully");
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

    }//GEN-LAST:event_btnGenerateActionPerformed

    private void txtNominalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNominalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNominalActionPerformed

    private void cbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbTypeActionPerformed
    public long numbGen(int length) {
        while (true) {
            long numb = (long) (Math.random() * 100000000 * 1000000); // had to use this as int's are to small for a 13 digit number.
            if (String.valueOf(numb).length() == length) {
                return numb;
            }
        }
    }
    
    public void clear(){
        txtQty.setText("");
        txtNominal.setText("");        
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGenerate;
    private javax.swing.JComboBox cbType;
    private org.jdesktop.swingx.JXDatePicker dpEnd;
    private org.jdesktop.swingx.JXDatePicker dpStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JTextField txtNominal;
    private javax.swing.JTextField txtQty;
    // End of variables declaration//GEN-END:variables
}
