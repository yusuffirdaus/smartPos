/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EwalletPanel.java
 *
 * Created on Jul 29, 2015, 11:48:27 AM
 */
package com.ewallet.gui;

import com.ewallet.client.RestClient;
import com.ewallet.model.WristBand;
import com.ewallet.utilities.Util;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Ade Saprudin
 */
public class EwalletPanel extends JPanel {

    /** Creates new form EwalletPanel */
    public EwalletPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtRfId = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtBalance = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cbType = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        txtValue = new javax.swing.JTextField();
        btnProses = new javax.swing.JButton();

        txtRfId.setName("txtRfId"); // NOI18N
        txtRfId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRfIdActionPerformed(evt);
            }
        });

        jLabel1.setText("RFID");
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel4.setText("Value");
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel3.setText("Action");
        jLabel3.setName("jLabel3"); // NOI18N

        txtBalance.setName("txtBalance"); // NOI18N
        txtBalance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBalanceActionPerformed(evt);
            }
        });

        jLabel2.setText("Balance");
        jLabel2.setName("jLabel2"); // NOI18N

        cbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "topup", "payment", "refund" }));
        cbType.setName("cbType"); // NOI18N

        jButton1.setText("Check");
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        txtValue.setName("txtValue"); // NOI18N
        txtValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValueActionPerformed(evt);
            }
        });

        btnProses.setText("Proses");
        btnProses.setName("btnProses"); // NOI18N
        btnProses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProsesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnProses)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtValue, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                            .addComponent(cbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtBalance)
                                .addComponent(txtRfId, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addContainerGap(90, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtRfId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtBalance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(txtValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addComponent(btnProses)
                .addContainerGap(71, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void txtRfIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRfIdActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_txtRfIdActionPerformed

private void txtBalanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBalanceActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_txtBalanceActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    String result = RestClient.checkSaldo(txtRfId.getText());
    WristBand re= Util.gson.fromJson(result, WristBand.class);
    txtBalance.setText(String.valueOf(re.getBalance()));
}//GEN-LAST:event_jButton1ActionPerformed

private void txtValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtValueActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_txtValueActionPerformed

private void btnProsesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProsesActionPerformed

    String result = RestClient.requestEwallet(txtRfId.getText(), Double.parseDouble(txtValue.getText()), cbType.getSelectedItem().toString());
    JOptionPane.showMessageDialog(null, result);
    //Message re= Util.gson.fromJson(result, Message.class);
    //re.getMessage()
    
}//GEN-LAST:event_btnProsesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnProses;
    private javax.swing.JComboBox cbType;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField txtBalance;
    private javax.swing.JTextField txtRfId;
    private javax.swing.JTextField txtValue;
    // End of variables declaration//GEN-END:variables
}
