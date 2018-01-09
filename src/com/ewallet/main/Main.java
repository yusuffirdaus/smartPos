/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ewallet.main;

import com.ewallet.utilities.Property;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 *
 * @author Ade Saprudin
 */
public class Main {
    
    public static Logger  logger = Logger.getLogger(Main.class);
    public final static String CLIENT_ID = Property.getProperty("smartpost.clientid");
    public final static String SECRET = Property.getProperty("smartpost.secret");
//    public final static String EWALLET_URL = Property.getProperty("smartpost.ewallet.url");
     public final static String CRM_URL = Property.getProperty("smartpost.crm.url");
    
    public static void main(String[] args){
        Random r= new Random();
        int qty=100;
        int size=String.valueOf(qty).length();
        for(int i=1;i<=qty;i++){
            String  a= String.valueOf(i);            
            while(a.length()<size){
               a="0"+a; 
            }
            System.out.println(numbGen(13-size)+a);
        }
    }
    
    public static long numbGen(int length) {
    while (true) {
        long numb = (long)(Math.random() * 100000000 * 1000000); // had to use this as int's are to small for a 13 digit number.
        if (String.valueOf(numb).length() == length)
            return numb;
    }
}
}
