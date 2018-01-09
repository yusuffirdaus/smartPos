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

package com.openbravo.pos.forms;

import java.util.Locale;
import javax.swing.UIManager;
import com.openbravo.format.Formats;
import com.openbravo.pos.instance.InstanceQuery;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.smj.EmailSender;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.LookAndFeel;
import org.apache.log4j.PropertyConfigurator;
//import org.jvnet.substance.SubstanceLookAndFeel;
//import org.jvnet.substance.api.SubstanceSkin;
//import org.jvnet.substance.skin.SkinInfo;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
//import org.pushingpixels.substance.api.skin.SkinInfo;

/**
 *
 * @author adrianromero
 */
public class StartPOS {

    private static Logger logger = Logger.getLogger("com.openbravo.pos.forms.StartPOS");
    
    private DataLogicSystem m_dlSystem;
    private TicketParser m_TTP2;
    
    /** Creates a new instance of StartPOS */
    private StartPOS() {
    }
    
    
    public static boolean registerApp() {
                       
        // vemos si existe alguna instancia        
        InstanceQuery i = null;
        try {
            i = new InstanceQuery();
            i.getAppMessage().restoreWindow();
            return false;
        } catch (Exception e) {
            return true;
        }  
    }
    
    public static void main (final String args[]) {      
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                
                if (!registerApp()) {
                    System.exit(1);
                }
                
                AppConfig config = new AppConfig(args);
                config.load();
                               
                // set Locale.
                String slang = config.getProperty("user.language");
                String scountry = config.getProperty("user.country");
                String svariant = config.getProperty("user.variant");
                if (slang != null && !slang.equals("") && scountry != null && svariant != null) {                                        
                    Locale.setDefault(new Locale(slang, scountry, svariant));
                }
                
                // Set the format patterns
                Formats.setIntegerPattern(config.getProperty("format.integer"));
                Formats.setDoublePattern(config.getProperty("format.double"));
                Formats.setCurrencyPattern(config.getProperty("format.currency"));
                Formats.setPercentPattern(config.getProperty("format.percent"));
                Formats.setDatePattern(config.getProperty("format.date"));
                Formats.setTimePattern(config.getProperty("format.time"));
                Formats.setDateTimePattern(config.getProperty("format.datetime"));               
                
                // Set the look and feel.
                try {             
                    
                    Object laf = Class.forName(config.getProperty("swing.defaultlaf")).newInstance();
                    
                    if (laf instanceof LookAndFeel){
                        UIManager.setLookAndFeel((LookAndFeel) laf);
                    } else if (laf instanceof SubstanceSkin) {                      
                        SubstanceLookAndFeel.setSkin((SubstanceSkin) laf);                   
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Cannot set look and feel", e);
                }
                
                String screenmode = config.getProperty("machine.screenmode");
                if ("fullscreen".equals(screenmode)) {
                    JRootKiosk rootkiosk = new JRootKiosk();
                    rootkiosk.initFrame(config);
                } else {
                    JRootFrame rootframe = new JRootFrame(); 
                    rootframe.initFrame(config);
                }
            }
        });
     
    }
   /*
  private void sendEmail(){
            
        Properties eProperties = m_dlSystem.getResourceAsPropertiesFromString("email.properties");
        EmailSender emailSender = new EmailSender(eProperties);

        String toString = eProperties.getProperty("to-list");
        List<String> list  = Arrays.asList( toString.split(",") );

        StringBuffer text = new StringBuffer(AppLocal.getIntString("email.closeCash2"));
        text.append("\n\n++++++++++++++++++++++++++\n");
        text.append(m_TTP2.getTicketText());
        text.append("\n++++++++++++++++++++++++++\n");

        emailSender.send(list, eProperties.getProperty("from"), "Laporan Close Cash", text.toString());//"Cierre de caja", text.toString());
        
    }
  */
 }
