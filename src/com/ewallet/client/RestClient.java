/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ewallet.client;


import com.ewallet.entity.Voucher;
import com.ewallet.main.Main;
import com.ewallet.model.PointReward;
import com.ewallet.utilities.Util;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 *
 * @author ade
 */
public class RestClient {

    public static void main(String[] args) {

       //requestEwallet("111", 1000000,"topup");
       //requestEwallet("111", 250000,"payment");
       //requestEwallet("111", 1000000,"refund");
        checkSaldo("222");
    }

    public static String requestEwallet(String rfId, double value,String type) {
        String result = null;
        Main.logger.info("Try Connect to server ..");
      
        try {
            Client client = Client.create();
            WebResource webResource = client.resource(Main.CRM_URL + "/EwalletService/"+type );           
            String json = "{\"rfId\":\""+rfId+"\", "
                    + "\"type\" : \""+type+"\","
                    + "\"value\" : " + value + ","
                    + "\"clientId\" : " + Main.CLIENT_ID + ","
                    + "\"secret\": \"" + Main.SECRET + "\"}";
            
            System.out.println("Hasil " + json);
            Main.logger.info("Send Request " + json);
            ClientResponse response = webResource.type("application/json").post(ClientResponse.class, json);
            
            result = response.getEntity(String.class);         
            Main.logger.info("Response " + result);
            System.out.println("Response \n" + result);
        } catch (Exception ex) {
            result = "{\"code\":-1,\"message\":\""+ex.getMessage()+"\"}";
            //ex.printStackTrace();
            Main.logger.error(ex.getMessage(), ex);
        }
        return result;
    }
    
    public static String checkSaldo(String rfId) {
        String result = null;
        Main.logger.info("Try Connect to server ..");
      
        try {
            Client client = Client.create();
            WebResource webResource = client.resource(Main.CRM_URL + "/EwalletService/getWristBand/"+rfId );           
            
            
            //System.out.println("Hasil " + json);
            Main.logger.info("Send Request " + rfId);
            ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
            
            result = response.getEntity(String.class);         
            Main.logger.info("Response " + result);
            System.out.println("Response \n" + result);
        } catch (Exception ex) {
            result = "{\"code\":-1,\"message\":\""+ex.getMessage()+"\"}";
            //ex.printStackTrace();
            Main.logger.error(ex.getMessage(), ex);
        }
        return result;
    }
    
    public static String getVoucher(String id) {
        String result = null;
        Main.logger.info("Try Connect to server ..");
      
        try {
            Client client = Client.create();
            WebResource webResource = client.resource(Main.CRM_URL + "/VoucherService/getVoucher/"+id );           
            System.out.println("Req "+Main.CRM_URL + "/VoucherService/getVoucher/"+id);
            
            //System.out.println("Hasil " + json);
            Main.logger.info("Send Request " + id);
            ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
            
            result = response.getEntity(String.class);         
            Main.logger.info("Response " + result);
            System.out.println("Response \n" + result);
        } catch (Exception ex) {
            result = "{\"code\":-1,\"message\":\""+ex.getMessage()+"\"}";
            //ex.printStackTrace();
            Main.logger.error(ex.getMessage(), ex);
        }
        return result;
    }
    
    public static String createVoucher(Voucher voucher) {
        String result = null;
        Main.logger.info("Try Connect to server ..");
      
        try {
            Client client = Client.create();
            WebResource webResource = client.resource(Main.CRM_URL + "/VoucherService/createVoucher" );
            System.out.println("URL :  "+Main.CRM_URL + "/VoucherService/createVoucher"+" "+Util.gson.toJson(voucher) );
            
            
            //System.out.println("Hasil " + json);
            //Main.logger.info("Send Request " + rfId);
            ClientResponse response = webResource.type("application/json").post(ClientResponse.class,Util.gson.toJson(voucher));
            
            result = response.getEntity(String.class);         
            Main.logger.info("Response " + result);
            System.out.println("Response \n" + result);
        } catch (Exception ex) {
            result = "{\"code\":-1,\"message\":\""+ex.getMessage()+"\"}";
            //ex.printStackTrace();
            Main.logger.error(ex.getMessage(), ex);
        }
        return result;
    }
    
     public static String updateVoucher(Voucher voucher) {
        String result = null;
        Main.logger.info("Try Connect to server ..");
      
        try {
            Client client = Client.create();
            WebResource webResource = client.resource(Main.CRM_URL + "/VoucherService/updateVoucher" );           
            
            
            //System.out.println("Hasil " + json);
            //Main.logger.info("Send Request " + rfId);
            ClientResponse response = webResource.type("application/json").post(ClientResponse.class,Util.gson.toJson(voucher));
            
            result = response.getEntity(String.class);         
            Main.logger.info("Response " + result);
            System.out.println("Response \n" + result);
        } catch (Exception ex) {
            result = "{\"code\":-1,\"message\":\""+ex.getMessage()+"\"}";
            //ex.printStackTrace();
            Main.logger.error(ex.getMessage(), ex);
        }
        return result;
    }
   
          public static String getPointReward(String id) {
        String result = null;
        Main.logger.info("Try Connect to server ..");
      
        try {
            Client client = Client.create();
            WebResource webResource = client.resource(Main.CRM_URL + "/PointReward/getAllPointRewardByCustomerId/"+id );           
            
            
            //System.out.println("Hasil " + json);
            Main.logger.info("Send Request " + id);
            ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
            result = response.getEntity(String.class);                                                                                                                                                                                                                                                                                                                                                                                                                       
            System.out.println("Response \n" + result);
        } catch (Exception ex) {    
            result = "{\"code\":-1,\"message\":\""+ex.getMessage()+"\"}";
            //ex.printStackTrace();
            Main.logger.error(ex.getMessage(), ex);
        }
        return result;
    }
          
    public static String updatePointReward(PointReward pointReward) {
        String result = null;
        Main.logger.info("Try Connect to server ..");
      
        try {
            Client client = Client.create();
            WebResource webResource = client.resource(Main.CRM_URL + "/PointReward/updatePointReward" );           
            
            
            //System.out.println("Hasil " + json);
            //Main.logger.info("Send Request " + rfId);
            ClientResponse response = webResource.type("application/json").post(ClientResponse.class,Util.gson.toJson(pointReward));
            
            result = response.getEntity(String.class);         
            Main.logger.info("Response " + result);
            System.out.println("Response \n" + result);
        } catch (Exception ex) {
            result = "{\"code\":-1,\"message\":\""+ex.getMessage()+"\"}";
            //ex.printStackTrace();
            Main.logger.error(ex.getMessage(), ex);
        }
        return result;
    }

}
