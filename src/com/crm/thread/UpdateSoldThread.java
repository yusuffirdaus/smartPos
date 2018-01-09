/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.crm.thread;

import com.ewallet.main.Main;
import com.openbravo.pos.dao.ProductDAO;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ade Saprudin
 */
public class UpdateSoldThread implements Runnable{
    
    List<TicketLineInfo> tls;

    public UpdateSoldThread(List<TicketLineInfo> tls){
        this.tls=tls;
    }
    
    @Override
    public void run() {
        try{
                 Main.logger.info("Thread Update Sold start ");     
                 for (TicketLineInfo ti : tls) {
                     System.out.println("Update SOLD "+ti.getProductID()+" "+ti.getMultiply());
                    ProductDAO.getInstance().updateSoldProduct(ti.getProductID(), ti.printMultiply());
                 }
                 Main.logger.info("Thread Update Sold start ");     
        }catch(Exception e){
        
        }
    }
    
}
