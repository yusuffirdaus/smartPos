package com.openbravo.pos.smj;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */    
     
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.forms.DataLogicSystem;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/**
 * clase para recepcion de mensaje XML provenientes del ERP
 * Processes all incoming messages from the ERP
 * @author Pedro Rozo - SmartJSP 
 */
public class ERPListener extends MQEndPoint implements Runnable, Consumer{

    String subject;
    
    private PosDAO dao ;
    private AppProperties props;
    DataLogicSystem m_dlSystem;
    private static Logger logger = Logger.getLogger(ERPListener.class.getName());
    private String pcName;
    
    /**
     * 
     * @param props
     * @param m_dlSystem
     * @param pcName 
     */
     public ERPListener(AppProperties props, DataLogicSystem m_dlSystem, String pcName) throws IOException{
        super(m_dlSystem.getResourceAsText("jms.inqueue"));		
        this.pcName = pcName;
        this.m_dlSystem =m_dlSystem;
        this.props = props;
        dao = new PosDAO(props);
        dao.setLogger(logger, m_dlSystem);
    }
            
       public void run() {
		try {
    			channel.basicConsume(endPointName, true,this);  
                           //start consuming messages. Auto acknowledge messages.
		} catch (IOException e) {
		 e.printStackTrace();
		}
	}

	/**
	 * Called when consumer is registered.
	 */
	public void handleConsumeOk(String consumerTag) {
		logger.log(Level.INFO," SmartPOS :Successful registration with MQBroker: "+consumerTag);		
	}

	public void handleCancel(String consumerTag) {
        	logger.log(Level.INFO," SmartPOS Cancel  with MQBroker: "+consumerTag);		

        }
	public void handleCancelOk(String consumerTag) {
        	logger.log(Level.INFO," SmartPOS CancelOK with MQBroker: "+consumerTag);		

        }
	public void handleRecoverOk(String consumerTag) {
        	logger.log(Level.INFO," SmartPOS RecoverOK with MQBroker: "+consumerTag);		

        }
	public void handleShutdownSignal(String consumerTag, ShutdownSignalException arg1) {
            	logger.log(Level.INFO," SmartPOS Shutdown with MQBroker: "+consumerTag+"-exception:"+arg1.getMessage());		

        }

	@Override
	public void handleDelivery(String consumerTag, Envelope env,
			BasicProperties props , byte[] body)
			throws IOException {
            if(!props.getContentEncoding().equalsIgnoreCase("application/octet-stream") ){
	       logger.log(Level.SEVERE,"SmartPOS - Message received MQ: "+ new String(body,"UTF-8"));
            }
            processMessage(body,props.getAppId(),props.getCorrelationId(),props.getContentEncoding(),props.getTimestamp());
            
	}

    /**
     * processes messages for synchronization of:
     * bParner
     * PRODUCT
     * PRODUCT_CATEGORY
     * PRODUCTPRICE
     * STORAGE
     * BPARTNER_LOCATION
     * LOCATION
     * USER
     * UOM
     * UOM_CONVERSION
     * COUNTRY
     * REGION
     * CITY
     * TAXCATEGORY
     * TAX
     * PRICELISTVERSION
     * CreditCard
     * DELETE-PRODUCT
     * DELETE-PRODUCT-CATEGORY
     * DELETE-BPARTNER
     * DELETE-PHOTO
     * SYNC-END
     * SYNC-END-WITH-ERRORS
     * SYNC-ERROR
     * @param message 
     */
    public void processMessage(byte[] message,String messageType,String value2,String contentType,Date timeStamp) {
        Calendar            lastUpdate = null;
        Calendar            timeStampMessage = null;
        String              xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        SimpleDateFormat    sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        boolean             wereMessageProccess = true;
        logger.log(Level.SEVERE,"Entro a processMessage ----1 -");
        try {
            timeStampMessage = Calendar.getInstance();
            timeStampMessage.setTime(timeStamp);
            try {
                lastUpdate = Calendar.getInstance();
                lastUpdate.setTime(sdf.parse(m_dlSystem.getResourceAsText("jms.lasUpdate")));
            } catch (ParseException ex) {
                Logger.getLogger(ERPListener.class.getName()).log(Level.SEVERE, null, ex);
                logger.log(Level.SEVERE, null, ex);
                lastUpdate = null;
            }
           // if(lastUpdate != null && (lastUpdate.after(timeStampMessage) || lastUpdate.equals(timeStampMessage))){
           //     return;
           // }  //SMJ desahilitado por innesario
            logger.log(Level.SEVERE, "*"+m_dlSystem.getResourceAsText("jms.lasUpdate")+"*");
            
            if(contentType.equalsIgnoreCase("application/octet-stream") ){
                processImg(message,value2);
                return;
            }
  //          if(textMessage.getStringProperty("PCName") != null &&  !textMessage.getStringProperty("PCName").equals("") && !textMessage.getStringProperty("PCName").equalsIgnoreCase(pcName))
    //            return;
            
            Document document = null;
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            org.xml.sax.InputSource inStream = new org.xml.sax.InputSource();
            String xml2 = new String(message,"UTF-8");

            // cleans the XML header 
            xml2 = xml2.trim();
            int ini = xml2.indexOf("<entityDetail>");
            xml2 = xml2.substring(ini);
            xml = xml + xml2;
            inStream.setCharacterStream(new java.io.StringReader(xml));
            document = builder.parse(inStream);
            String tabla =  document.getElementsByTagName("type").item(0).getTextContent();
            if (tabla.equalsIgnoreCase("BPARTNER"))         //Procesa Terceros
            {
                String valor =  document.getElementsByTagName("Value").item(0).getTextContent().trim();	
                String id =  document.getElementsByTagName("C_BPartner_ID").item(0).getTextContent().trim();	
                String nombre =  document.getElementsByTagName("Name").item(0).getTextContent().trim();	
                String nombre2 =  document.getElementsByTagName("Name2").item(0).getTextContent().trim();
                String card =  document.getElementsByTagName("ReferenceNo").item(0).getTextContent().trim();
                if(card.equalsIgnoreCase("./.") ||  card.trim().equalsIgnoreCase("null") ) {
                    card ="";
                }
                String nombre1 = "";
                if(value2 != null)
                    nombre1 = value2.trim();
                
                String taxId  =  document.getElementsByTagName("TaxID").item(0).getTextContent().trim();
                if (taxId.trim().equalsIgnoreCase("./.")){
                    taxId = "";
                }
                String duns  =  document.getElementsByTagName("DUNS").item(0).getTextContent().trim();
                String esCliente  =  document.getElementsByTagName("IsCustomer").item(0).getTextContent().trim();
                String isActive = document.getElementsByTagName("IsActive").item(0).getTextContent().trim();
                String customerTaxCategory = document.getElementsByTagName("C_TaxGroup_ID").item(0).getTextContent().trim();
                Double creditLimit = new Double(document.getElementsByTagName("SO_CreditLimit").item(0).getTextContent());
                Double creditUsed = new Double(document.getElementsByTagName("SO_CreditUsed").item(0).getTextContent());
                if(customerTaxCategory.equalsIgnoreCase("./.")){
                    customerTaxCategory =null;
                }
                String taxExempt= document.getElementsByTagName("IsTaxExempt").item(0).getTextContent().trim();
 
                // informacion asociada a aseguradoras
                
		String smj_isinsuranceCompany  = document.getElementsByTagName("smj_isinsuranceCompany").item(0).getTextContent().trim(); 
		String smj_amountDue_debt  = document.getElementsByTagName("smj_amountDue_debt").item(0).getTextContent().trim(); 
		String smj_nationalIDNumber  = document.getElementsByTagName("smj_nationalIDNumber").item(0).getTextContent().trim(); 
		String smj_insuranceCompanyType_ID  = document.getElementsByTagName("smj_insuranceCompanyType_ID").item(0).getTextContent().trim(); 
		String smj_insurancePlan_ID  = document.getElementsByTagName("smj_insurancePlan_ID").item(0).getTextContent().trim(); 
		String smj_insuranceCompany_ID  = document.getElementsByTagName("smj_insuranceCompany_ID").item(0).getTextContent().trim(); 

                
                
                if (smj_isinsuranceCompany.equalsIgnoreCase("true")) 
                {   // procesado solo si es cliente y su TaxID (cedula) no esta vacia
                    dao.procesaAseguradora(id,taxId,nombre1,nombre2,valor,duns,isActive,customerTaxCategory,nombre, creditLimit,creditUsed,taxExempt,card,
                            smj_isinsuranceCompany,smj_amountDue_debt,smj_nationalIDNumber,smj_insuranceCompanyType_ID,smj_insurancePlan_ID,smj_insuranceCompany_ID);
                }
                else if (esCliente.equalsIgnoreCase("true")) 
                {   // procesado solo si es cliente y su TaxID (cedula) no esta vacia
                    dao.procesaTercero(id,taxId,nombre1,nombre2,valor,duns,isActive,customerTaxCategory,nombre, creditLimit,creditUsed,taxExempt,card,
                            smj_isinsuranceCompany,smj_amountDue_debt,smj_nationalIDNumber,smj_insuranceCompanyType_ID,smj_insurancePlan_ID,smj_insuranceCompany_ID);
                }
            }
            else if (tabla.equalsIgnoreCase("PRODUCT")) {  // Procesa productos
                    String id =  document.getElementsByTagName("M_Product_ID").item(0).getTextContent().trim();	
                    String valor =  document.getElementsByTagName("Value").item(0).getTextContent().trim();
                    String nombre1 =  document.getElementsByTagName("Name").item(0).getTextContent().trim();
                    String categoria =  document.getElementsByTagName("M_Product_Category_ID").item(0).getTextContent().trim();
                    String unidad =  document.getElementsByTagName("SKU").item(0).getTextContent().trim();
                    String accesorio =  document.getElementsByTagName("Group1").item(0).getTextContent().trim();
                    String cocina =  document.getElementsByTagName("Group2").item(0).getTextContent().trim();
                    String ayuda =  document.getElementsByTagName("Help").item(0).getTextContent().trim();
                    String umoId = document.getElementsByTagName("C_UOM_ID").item(0).getTextContent().trim();
                    String taxCategoryID = document.getElementsByTagName("C_TaxCategory_ID").item(0).getTextContent().trim();
                    String upc = document.getElementsByTagName("UPC").item(0).getTextContent().trim();
                    // gte location id from ERP,
                    String idLocatorErp = document.getElementsByTagName("M_Locator_ID").item(0).getTextContent().trim();
                    String ubicacion = dao.getLocatorByIdErp(idLocatorErp);  //  get the locator code in the POS side from the ERP code
                    System.out.println(" *************  ubicacion nueva de producto: " +ubicacion+" desde el id ERP:" + idLocatorErp);
                    if(upc.equals("./.")){
                        upc = valor;
                    }
                    boolean isActive =Boolean.parseBoolean(document.getElementsByTagName("IsActive").item(0).getTextContent().trim());
                    String imgUrl = document.getElementsByTagName("ImageURL").item(0).getTextContent().trim();
                    HashMap <String,String> stock = dao.getDetailsStock(id);
                    
                    String existencias = "0"; 
                    if (stock != null)  //  stock info available for product 
                    {
                       existencias = stock.get("existencias");
                      // ubicacion = stock.get("ubicacion");
                    }
                    dao.procesaProducto(id,valor,nombre1,categoria,unidad,accesorio,
                            ayuda,cocina,umoId,taxCategoryID,isActive, upc, imgUrl,existencias,ubicacion);
            }	
            else if (tabla.equalsIgnoreCase("PRODUCT_CATEGORY")) {  // procesa categorias de productos
                    String id =  document.getElementsByTagName("M_Product_Category_ID").item(0).getTextContent().trim();
                    String nombre =  document.getElementsByTagName("Name").item(0).getTextContent().trim();	
                    boolean isActive =Boolean.parseBoolean(document.getElementsByTagName("IsActive").item(0).getTextContent().trim());
                    dao.procesaCategoriasProd(id,nombre,isActive);	
                    }	
            else if (tabla.equalsIgnoreCase("LOCATOR")) {  // procesa locator
                    String codigo =  document.getElementsByTagName("Value").item(0).getTextContent().trim();	
                    String idErp =  document.getElementsByTagName("M_Locator_ID").item(0).getTextContent().trim();	
                    boolean isActive =Boolean.parseBoolean(document.getElementsByTagName("IsActive").item(0).getTextContent().trim());
                    dao.procesaLocator(codigo,codigo,isActive,idErp);	
                    }	

            else if (tabla.equalsIgnoreCase("PRODUCTPRICE")) {  // procesa precios de producto
                    String prodId =  document.getElementsByTagName("M_Product_ID").item(0).getTextContent().trim();
                    String precioLista =  document.getElementsByTagName("PriceList").item(0).getTextContent().trim();
                    String precioEstandar = document.getElementsByTagName("PriceStd").item(0).getTextContent().trim();
                    String precioLimite = document.getElementsByTagName("PriceLimit").item(0).getTextContent().trim();
                    dao.procesaPrecioProd(prodId,precioLista,precioEstandar,precioLimite);	
                    }
            else if (tabla.equalsIgnoreCase("STORAGEONHAND")) {  // procesa precios de producto
                    String prodId =  document.getElementsByTagName("M_Product_ID").item(0).getTextContent().trim();
                    //String location =  document.getElementsByTagName("M_Locator_ID").item(0).getTextContent().trim();
                    System.out.print("Detalle de existencias para prod id:"+prodId   + "-"+value2);
                    String valores [] = value2.trim().split(";");
                    String existencias = valores[0];
                    String locator = valores[1];
                    System.out.println(" ******************   Recibi: " +existencias+" - Locator:"+locator);
                    
                    dao.procesaStockProd(prodId,existencias,locator);	
                    }

            else if (tabla.equalsIgnoreCase("BPARTNER_LOCATION"))         //Procesa localizaciones de terceros (relacion)
            {
                    String id =  document.getElementsByTagName("C_BPartner_ID").item(0).getTextContent().trim();
                    String phone =  document.getElementsByTagName("Phone").item(0).getTextContent().trim();
                    String cellPhone =  document.getElementsByTagName("Phone2").item(0).getTextContent().trim();
                    String locId =  document.getElementsByTagName("C_Location_ID").item(0).getTextContent().trim();
                    String fax = document.getElementsByTagName("Fax").item(0).getTextContent().trim();


                    dao.procesaBPLocation( fax,phone,cellPhone,locId,id);
            }
            else if (tabla.equalsIgnoreCase("LOCATION"))         //Procesa localizaciones detalle 
            {
                String locId =  document.getElementsByTagName("C_Location_ID").item(0).getTextContent().trim();
                String add1 =  document.getElementsByTagName("Address1").item(0).getTextContent().trim();
                String add2 =  document.getElementsByTagName("Address2").item(0).getTextContent().trim();
                String city =  document.getElementsByTagName("C_City_ID").item(0).getTextContent().trim();
                String regionName =  document.getElementsByTagName("C_Region_ID").item(0).getTextContent().trim();
                String countryId =document.getElementsByTagName("C_Country_ID").item(0).getTextContent().trim();
                String postalCode =document.getElementsByTagName("Postal").item(0).getTextContent().trim();
                dao.procesaLocation(add1,add2,city,regionName,locId,countryId,postalCode);
            }
            else if (tabla.equalsIgnoreCase("USER"))         //Procesa informacion de usuario (email)
            {
                String id =  document.getElementsByTagName("C_BPartner_ID").item(0).getTextContent().trim();
                String email =  document.getElementsByTagName("EMail").item(0).getTextContent().trim();
                dao.procesaUsuario(email,id);

            } 
            else if(tabla.equalsIgnoreCase("UOM"))//Procesa unidades detalle 
            {                  
                String id = document.getElementsByTagName("C_UOM_ID").item(0).getTextContent().trim();
                String code = document.getElementsByTagName("UOMSymbol").item(0).getTextContent().trim();
                String name = document.getElementsByTagName("Name").item(0).getTextContent().trim();
                String costingPrecision = document.getElementsByTagName("CostingPrecision").item(0).getTextContent().trim();
                String type = document.getElementsByTagName("UOMType").item(0).getTextContent().trim();
                String stdPrecision = document.getElementsByTagName("StdPrecision").item(0).getTextContent();
                dao.procesaUnidad(id,code,name,costingPrecision,type,stdPrecision);
            }
            else if(tabla.equalsIgnoreCase("UOM_CONVERSION")){ //Procesa conversiones de unidades detalle 
                String id = document.getElementsByTagName("C_UOM_Conversion_ID").item(0).getTextContent().trim();
                String unitId = document.getElementsByTagName("C_UOM_ID").item(0).getTextContent().trim();
                String unitToId = document.getElementsByTagName("C_UOM_To_ID").item(0).getTextContent().trim();
                String divideRate = document.getElementsByTagName("DivideRate").item(0).getTextContent().trim();
                String multiplyRate = document.getElementsByTagName("MultiplyRate").item(0).getTextContent().trim();
                String productId = document.getElementsByTagName("M_Product_ID").item(0).getTextContent().trim();
                dao.procesaConversionUnidad(id,unitId,unitToId,divideRate,multiplyRate,productId);
            }
            else if(tabla.equalsIgnoreCase("COUNTRY")){ //Procesa paises detalle 
                String id = document.getElementsByTagName("C_Country_ID").item(0).getTextContent().trim();
                String currencyID = document.getElementsByTagName("C_Currency_ID").item(0).getTextContent().trim();
                String countryCode = document.getElementsByTagName("CountryCode").item(0).getTextContent().trim();
                String description = document.getElementsByTagName("Description").item(0).getTextContent().trim();
                String name = document.getElementsByTagName("Name").item(0).getTextContent().trim();
                String regionName = document.getElementsByTagName("RegionName").item(0).getTextContent().trim();
                dao.procesaPais(id, currencyID, countryCode, description, name, regionName);
            }
            else if(tabla.equalsIgnoreCase("REGION")){ //Procesa regiones detalle 
                String id = document.getElementsByTagName("C_Region_ID").item(0).getTextContent().trim();
                String countryID = document.getElementsByTagName("C_Country_ID").item(0).getTextContent().trim();
                String description = document.getElementsByTagName("Description").item(0).getTextContent().trim();
                String name = document.getElementsByTagName("Name").item(0).getTextContent().trim();
                dao.procesaRegion(id, countryID, description, name);
            }
            else if(tabla.equalsIgnoreCase("CITY")){ //Procesa ciudades detalle 
                String id = document.getElementsByTagName("C_City_ID").item(0).getTextContent().trim();
                String countryID = document.getElementsByTagName("C_Country_ID").item(0).getTextContent().trim();
                String regionID = document.getElementsByTagName("C_Region_ID").item(0).getTextContent().trim();
                String name = document.getElementsByTagName("Name").item(0).getTextContent().trim();
                String postal = document.getElementsByTagName("Postal").item(0).getTextContent().trim();
                dao.procesaCiudad(id, countryID, regionID, name, postal);
            }
            else if(tabla.equalsIgnoreCase("TAXCATEGORY")){ //Procesa categorias de impuestos detalle 
                String id = document.getElementsByTagName("C_TaxCategory_ID").item(0).getTextContent().trim();
                String name = document.getElementsByTagName("Name").item(0).getTextContent().trim();
                dao.procesaCategoriasDeImpuestos(id, name);
                dao.procesaCategoriasDeImpuestosPorClientes(id, name);
            }
            else if(tabla.equalsIgnoreCase("TAX")){ //Procesa impuestos detalle 
                String id = document.getElementsByTagName("C_Tax_ID").item(0).getTextContent().trim();
                String name = document.getElementsByTagName("Name").item(0).getTextContent().trim();

                String validfromStr = document.getElementsByTagName("ValidFrom").item(0).getTextContent().trim();
                SimpleDateFormat formatoDelTexto = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date validfrom = formatoDelTexto.parse(validfromStr);

                String categoryID = document.getElementsByTagName("C_TaxCategory_ID").item(0).getTextContent().trim();
                double rate = Double.parseDouble(document.getElementsByTagName("Rate").item(0).getTextContent().trim());
                dao.procesaImpuestos(id, name, validfrom, categoryID, rate);
            }
            else if(tabla.equalsIgnoreCase("PRICELISTVERSION")){ //Procesa precios de productos por unidad detalle 
                String productid = document.getElementsByTagName("M_PRODUCT_ID").item(0).getTextContent().trim();
                String unitName = document.getElementsByTagName("VERSION").item(0).getTextContent().trim();
                double priceList = Double.parseDouble(document.getElementsByTagName("PRICELIST").item(0).getTextContent().trim());
                double priceSTD = Double.parseDouble(document.getElementsByTagName("PRICESTD").item(0).getTextContent().trim());
                double priceLimit = Double.parseDouble(document.getElementsByTagName("PRICELIMIT").item(0).getTextContent().trim());
                String isDefaultPos = document.getElementsByTagName("DEFAULTSALESPOS").item(0).getTextContent().trim();

                dao.procesaPrecioUnd(productid, unitName, priceList, priceSTD, priceLimit,isDefaultPos);
            }
            else if(tabla.equalsIgnoreCase("CreditCard")){ //Procesa nombres de tarjetas de credito detalle 
              //  String value = document.getElementsByTagName("Value").item(0).getTextContent().trim();
                String nameCC = document.getElementsByTagName("Name").item(0).getTextContent().trim();
                dao.procesaCreditCard(nameCC);
            }
            else if (tabla.equalsIgnoreCase("INSURANCECOMPANYTYPE")) {  // procesa tipos de aseguradoras
                String smj_insuranceCompanyType_ID =  document.getElementsByTagName("smj_insuranceCompanyType_ID").item(0).getTextContent().trim();	
                String nombre =  document.getElementsByTagName("name").item(0).getTextContent().trim();	
                dao.procesaTipoAseguradora(smj_insuranceCompanyType_ID,nombre);	
            }
            else if (tabla.equalsIgnoreCase("INSURANCEPLAN")) {  // procesa planes de seguros
                String smj_insurancePlan_ID =  document.getElementsByTagName("smj_insurancePlan_ID").item(0).getTextContent().trim();	
                String name =  document.getElementsByTagName("name").item(0).getTextContent().trim();	
                String C_BPartner_ID =  document.getElementsByTagName("C_BPartner_ID").item(0).getTextContent().trim();	
                Double coPay_percentage =  Double.parseDouble(document.getElementsByTagName("coPay_percentage").item(0).getTextContent().trim());	
                Double coPay_value =  Double.parseDouble(document.getElementsByTagName("coPay_value").item(0).getTextContent().trim());	
                dao.procesaPlanSeguros(smj_insurancePlan_ID,name,C_BPartner_ID,coPay_percentage,coPay_value);	
            }
            
            else if(tabla.equalsIgnoreCase("DELETE-PRODUCT")){ //Procesa borrado de productos detalle 
                String id = document.getElementsByTagName("PRODUCT-ID").item(0).getTextContent().trim();
                dao.procesaBorradoProducto(id);
            }
            else if(tabla.equalsIgnoreCase("DELETE-PRODUCT-CATEGORY")){//Procesa borrado de categorias de productos detalle 
                String id = document.getElementsByTagName("PRODUCT-CATEGORY-ID").item(0).getTextContent().trim();
                dao.procesaBorradoCategoriaProducto(id);
            }
            else if(tabla.equalsIgnoreCase("DELETE-BPARTNER")){//Procesa borrado de clientes detalle 
                String id = document.getElementsByTagName("BPARTNER-ID").item(0).getTextContent().trim();
                dao.procesaBorradoClientes(id);
            }
            else if(tabla.equalsIgnoreCase("DELETE-PHOTO")){//Procesa borrado de fotos detalle 
                String id = document.getElementsByTagName("PHOTO-ID").item(0).getTextContent().trim();
                dao.procesaImgProducto(null, id);
            }
            else if(tabla.equalsIgnoreCase("SYNC-END")){//Procesa mensaje de fin de sincronizacion detalle 
                // lo ultimo que proceso son los default units
                dao.refreshDefaultUnits();
                m_dlSystem.setResource("jms.message",0,"SYNC-END".getBytes());
            }
            else if(tabla.equalsIgnoreCase("SYNC-END-WITH-ERRORS")){
                m_dlSystem.setResource("jms.message",0,"SYNC-END-WITH-ERRORS".getBytes());
            }
            else if(tabla.equalsIgnoreCase("SYNC-ERROR")){//Procesa mensaje de error de sincronizacion detalle 
                final String error = m_dlSystem.getResourceAsText("jms.error") +
                    "\n\n" +document.getElementsByTagName("Value").item(0).getTextContent().trim() +
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

              //  JOptionPane.showConfirmDialog(f1 , AppLocal.getIntString("message.paymenterror"),AppLocal.getIntString("title.editor"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //,JOptionPane.ERROR_MESSAGE);
                //int res = JOptionPane.showConfirmDialog(null, AppLocal.getIntString("message.wannadelete"), AppLocal.getIntString("title.editor"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            }
            else  {
                logger.log(Level.SEVERE, "Message not recognized by SmartPOS->"+tabla);
                wereMessageProccess = false;
            }
            
            if(wereMessageProccess){
                m_dlSystem.setResource("jms.lasUpdate", 0, sdf.format(timeStampMessage.getTime()).getBytes());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"\n++++++++++++Exceptio+\n\n");
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * processed product images
     * @param objectMessage 
     */
    private void processImg(byte[] barray,String value) {
        try {
           // System.out.println(barray);
            dao.procesaImgProducto(barray, value);
        } catch (Exception ex) {
            Logger.getLogger(ERPListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
