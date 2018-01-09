package com.openbravo.pos.smj;

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.JRootApp;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class MQClient {
    private static Logger log = Logger.getLogger(MQClient.class.getName());
    protected static Channel channel;
    protected static  Connection connection;
    protected static ConnectionFactory factory;
    static String endPointName;
//    static String url = "localhost";
	static BasicProperties props;
	static String url = JRootApp.jmsUrl ;
	static String mquser = JRootApp.jmsUserLogin;
	static String mqpasswd = JRootApp.jmsPassword;
        static String mqOutPort = JRootApp.jmsOutPort;
        
	static {
	factory = new ConnectionFactory();  // get connection factory
        factory.setHost(url);                         
         int port = 5672;
         try {
             port = Integer.parseInt(mqOutPort);
         }catch (Exception e) {
            log.severe("Please configure a valid IP port for your MQbroker: jms.OutPort resource, by default we will use 5672 ");     		 
         }
        factory.setPort(port);  
        factory.setUsername(mquser);
        factory.setPassword(mqpasswd);
        try {
        connection = factory.newConnection();      // get connection   
        channel = connection.createChannel();         // get channel
    	
        } catch (IOException e)
        {
       	 log.severe("MQ Conecction issue:"+e.getMessage());
          mqError(e.getMessage());
        }
	}
    
    
    public static void sendMessage(String messageText){
    	System.out.println("*************** ERP Conectandose a activemq:" + JRootApp.jmsUrl + ":"+ JRootApp.jmsOutPort +
                            "- como:>"+JRootApp.jmsUserLogin+"<"+"- clave:"+JRootApp.jmsPassword+" - Queue->"+JRootApp.jmsOutgoingQueue  );
        String orgId = JRootApp.jmsOutgoingQueue;
        String value = "";
        String messageType = "POS-Client-MSG";
        try { 
        //declaring a queue for this channel. If queue does not exist,
        //it will be created on the server.
     	if (orgId != null && !orgId.equalsIgnoreCase("0"))
     	{
     	log.severe("Org ID - topic to send "+orgId);     		
        // properties for this message
        props = new BasicProperties
                    .Builder().deliveryMode(2)                    // delivery mode = 2 -> persistent
                    .contentEncoding("application/xml")
                    .correlationId(value)  
                    .appId(messageType)
                    .build();
                                        
     	channel.queueDeclare(orgId, true, false, false, null);   // second parameter = durable = true
    	channel.basicPublish("",orgId, props ,messageText.getBytes("UTF-8"));
    	log.severe("Msg Sent to:"+orgId+"->"+messageText);
     	}
        } catch (IOException e)
        {
       	 log.severe("MQ Connection issue:"+e.getMessage());
         mqError(e.getMessage());

        }

        
    }
    
    public static void sendMessageBlob(byte[] barray,String valor, String orgId){
       if  (orgId != null && !orgId.equalsIgnoreCase("0"))
     	{
     	log.severe("Org ID - topic to send "+orgId);     		

        try { 
        //declaring a queue for this channel. If queue does not exist,
        //it will be created on the server.
            // properties for this message
            props = new BasicProperties
                        .Builder().deliveryMode(2)                    // delivery mode = 2 -> persistent
                        .contentEncoding("application/octet-stream")
                        .correlationId(valor)  
                        .appId("binary")
                        .build();
        	
        	
        channel.queueDeclare(orgId, true, false, false, null);
     	channel.basicPublish("",orgId, props ,barray);
    	
        } catch (IOException e)
        {
       	 log.severe("MQ Connection issue:"+e.getMessage());
         mqError(e.getMessage());
        }
        }
        
    }
    	
    public static void mqError(String message) {
                    final String error = message +
                    " Please contact your ERP administrator "    ;
                // code required to access swing components from a different thread 
                SwingUtilities.invokeLater(new Runnable() 
                 {
                   public void run() 
                   {
                    JOptionPane.showMessageDialog(null , error, AppLocal.getIntString("message.paymenterror"),JOptionPane.ERROR_MESSAGE) ;    
                   }
                 }
                );

    }
    
    
    
}