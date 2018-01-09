package com.openbravo.pos.smj;

import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.JRootApp;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
/**
 *
 * @author pedrorozo
 */
public abstract class MQEndPoint {
    protected Channel channel;
    protected Connection connection;
    protected String endPointName;
    static String url = JRootApp.jmsUrl ;
    static String mquser = JRootApp.jmsUserLogin;
    static String mqpasswd = JRootApp.jmsPassword;
    static String mqInPort = JRootApp.jmsInPort;
    private static Logger logger = Logger.getLogger(MQEndPoint.class.getName());	
    
    public MQEndPoint(String endpointName) {
      try {  
         this.endPointName = endpointName;
         ConnectionFactory factory = new ConnectionFactory();  // get connection factory

         factory.setHost(url);                         
         int port = 5672;
         try {
             port = Integer.parseInt(mqInPort);
         }catch (Exception e) {
            logger.severe("Please configure a valid IP port for your MQbroker: jms.InPort resource, by default we will use 5672 ");     		 
         }
         factory.setPort(port);                         
         factory.setUsername(mquser);
         factory.setPassword(mqpasswd);
         connection = factory.newConnection();      // get connection   
         channel = connection.createChannel();         // get channel 
         //declaring a queue for this channel. If queue does not exist,
         //it will be created on the server.
         channel.queueDeclare(endpointName, true, false, false, null);
         logger.log(Level.INFO,"\n\n ****** SmartPOS Connected to MQ broker url: "+url+"-"+mqInPort+" ************* \n\n");
      } catch (IOException e) {
            JOptionPane.showMessageDialog(null,AppLocal.getIntString("mq.serverDownError"),AppLocal.getIntString("message.paymenterror"),JOptionPane.ERROR_MESSAGE);
            logger.log(Level.INFO,"Message Server is not available ->"+url+"-"+mqInPort+"<- " + e.getMessage()); 
            e.printStackTrace();
      }
      
    }
    
    /**
     * Close channel and connection. Not necessary as it happens implicitly any way. 
     * @throws IOException
     */
     public void close() throws IOException{
         this.channel.close();
         this.connection.close();
     }
      
}
