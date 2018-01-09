/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ewallet.utilities;

import com.ewallet.main.Main;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Ade
 */
public class Property {

    private final static Properties prop = new Properties();
    //private final static Properties propJdbc = new Properties();

    static {
        try {
            //System.getProperty("user.home")), APP_ID + ".properties"
            //FileInputStream in = new FileInputStream(System.getProperty("user.home")+"/config/config.properties");
            //String path="D:/DIAgent";
            String path=System.getProperty("user.home");
            FileInputStream in = new FileInputStream(path+"/crm.properties");
            //FileInputStream in = new FileInputStream("config/config.properties");
            prop.load(in);
            in.close();
            
            //FileInputStream in2 = new FileInputStream("config/persistence.properties");
            //propJdbc.load(in2);
            //in2.close();
            
        } catch (FileNotFoundException ex) {
            Main.logger.info("Error File Not Found: "+ex);
        } catch (IOException ex) {
            Main.logger.info("Error IOException: "+ex);
        }
    }

    public static String getProperty(String keys) {
        String value = prop.getProperty(keys);
        return value;
    }    
    
}
