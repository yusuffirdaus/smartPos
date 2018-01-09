package com.openbravo.pos.smj;

import com.openbravo.data.loader.ImageUtils;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.DriverWrapper;
import com.openbravo.pos.util.AltEncrypter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase para manejo de logica de sincronizacion con el ERP
 * Process all transactions database synchronization process of the ERP to POS
 * @author pedrorozo - SmartJSP 
 *
 */
public class PosDAO {
	private Connection con;
    private Statement st;
    private ResultSet rs;
    private String mensaje;
    private String url;
    private String usuario;
    private String clave;
    private AppProperties props;
    private Logger logger;
    private DataLogicSystem dlSystem;
    private DataLogicSales dlSales = new DataLogicSales();   
    

    /*
     * Logger settings for error log
     */
    public void setLogger(Logger logger, DataLogicSystem dlSystem) {
        this.logger = logger;
        this.dlSystem = dlSystem;
    }
    
	
    /**
     * Constructor
     */
    public PosDAO(AppProperties props) {
    	try {
            this.props = props;
            Conexion();
	} catch (SQLException e) {
            logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n");
            logger.log(Level.SEVERE, null, e);
	} catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n");
            logger.log(Level.SEVERE, null, e);
	}
    }
    
    /**
     * Saves the errors presented in  syncronizacion on resource jms.error 
     * @param errorMsj
     * @param t 
     */
    private void setErrorInLog(String errorMsj,Throwable t){
        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
        logger.log(Level.SEVERE, errorMsj, t);
        String error = dlSystem.getResourceAsText("jms.error");
        error += "\n\n" +errorMsj;
        dlSystem.setResource("jms.message",0,"SYNC-ERROR".getBytes());
        dlSystem.setResource("jms.error",0,error.getBytes());
    }
	
    /**
     *  Processes a BPartner, if you update it, if you do not believe (veficia by ID)
     *  Tabla: BPARTNER  
     * @param id
     * @param taxId
     * @param nombre1
     * @param nombre2
     * @param valor
     * @param duns
     */
    void procesaTercero (String id,String taxId,String nombre1,String nombre2,String valor,String duns,String isActive,String category, String nombre, double creditLimit,double creditUsed, String taxExempt,String card,
                         String smj_isinsuranceCompany,String smj_amountDue_debt,String smj_nationalIDNumber,String smj_insuranceCompanyType_ID,String smj_insurancePlan_ID,String smj_insuranceCompany_ID)
    {
        
        try {
            
            
            
            PreparedStatement ps = con.prepareStatement("select id  from customers where id=?");
            ps.setString(1, id.trim());
            ResultSet res = ps.executeQuery();

            if (res.next()) {   // el tercero i existia con el id especificado
                String q = "update customers set searchkey=?,firstname=?, lastname=?,notes=?,name=?, visible=?, taxcategory=?, maxdebt=?, curdebt=?, tax_exempt =?, taxid=?,card=?, "+
                                          " smj_isinsuranceCompany=?,smj_amountDue_debt=?,smj_nationalIDNumber=?,smj_insuranceCompanyType_ID=?,smj_insurancePlan_ID=?,smj_insuranceCompany_ID=? "+  
                                          " where id=?";
                logger.log(Level.SEVERE,"\n+++ SQL >"+q+"<");
                ps = con.prepareStatement(q);
                ps.setString(1, id);
                ps.setString(2, nombre1);
                ps.setString(3, nombre2);
                ps.setString(4, duns);
                ps.setString(5, nombre);
                ps.setBoolean(6,Boolean.parseBoolean(isActive));
                ps.setString(7,category);
                ps.setDouble(8, creditLimit);
                ps.setDouble(9, creditUsed);
                ps.setBoolean(10,Boolean.parseBoolean(taxExempt));
                ps.setString(11, taxId.trim());
                ps.setString(12, card.trim());
                
                ps.setBoolean(13, Boolean.parseBoolean(smj_isinsuranceCompany.trim()));
                
                if (smj_amountDue_debt.equalsIgnoreCase(""))
                    {
                        smj_amountDue_debt = "0";
                    }
                logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n smj_amountDue_debt >"+smj_amountDue_debt+"<");
                ps.setDouble(14, Double.parseDouble(smj_amountDue_debt));
                ps.setString(15, smj_nationalIDNumber.trim());
                ps.setString(16, smj_insuranceCompanyType_ID.trim());
                ps.setString(17, smj_insurancePlan_ID.trim());
                ps.setString(18, smj_insuranceCompany_ID.trim());

                
                ps.setString(19, id);

                int i = ps.executeUpdate();
                 if (i != 0) {
                     logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                     logger.log(Level.SEVERE, "tercero updated");
                 } else {
                     logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                     logger.log(Level.SEVERE, "tercero not updated");
                 }
            } else {  // el tercero no existia con id, pero si existia con el taxid entonces procede a actualizarle
                ps = con.prepareStatement("select taxid  from customers where taxid=? " );
                ps.setString(1, taxId.trim());
                ResultSet res2 = ps.executeQuery();

                if(res2.next()){
                    ps = con.prepareStatement("update customers set searchkey=?,firstname=?, lastname=?,notes=?,name=?, visible=?, taxcategory=?, id=?, maxdebt=?, curdebt=?, tax_exempt=?,card=?,"+
                                            " smj_isinsuranceCompany=?,smj_amountDue_debt=?,smj_nationalIDNumber=?,smj_insuranceCompanyType_ID=?,smj_insurancePlan_ID=?,smj_insuranceCompany_ID=? "+              
                                        " where taxid=? ");
                    
                    ps.setString(1, id);
                    ps.setString(2, nombre1);
                    ps.setString(3, nombre2);
                    ps.setString(4, duns);
                    ps.setString(5, nombre );
                    ps.setBoolean(6,Boolean.parseBoolean(isActive));
                    ps.setString(7,category);
                    ps.setString(8, id);
                    ps.setDouble(9, creditLimit);
                    ps.setDouble(10, creditUsed);
                    ps.setBoolean(11,Boolean.parseBoolean(taxExempt));
                    ps.setString(12, card.trim());
                
                ps.setBoolean(13, Boolean.parseBoolean(smj_isinsuranceCompany.trim()));
                
                if (smj_amountDue_debt.equalsIgnoreCase(""))
                    {
                        smj_amountDue_debt = "0";
                    }
                logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n smj_amountDue_debt >"+smj_amountDue_debt+"<");
                ps.setDouble(14, Double.parseDouble(smj_amountDue_debt));
                    ps.setString(15, smj_nationalIDNumber.trim());
                    ps.setString(16, smj_insuranceCompanyType_ID.trim());
                    ps.setString(17, smj_insurancePlan_ID.trim());
                    ps.setString(18, smj_insuranceCompany_ID.trim());

                    ps.setString(19, taxId.trim());


                    int i = ps.executeUpdate();
                    if (i != 0) {
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                        logger.log(Level.SEVERE, "tercero updated by taxid");
                    } else {
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                        logger.log(Level.SEVERE, "tercero not updated by taxid");
                    }
                }else{  // tercero no existia por tax id - ni por id entonce se crea como nuevo 
                    ps = con.prepareStatement("insert into customers(id, firstname, lastname,searchkey, notes,taxid,name,taxcategory,curdebt, visible, maxdebt,tax_exempt,card," +
                                              " smj_isinsuranceCompany,smj_amountDue_debt,smj_nationalIDNumber,smj_insuranceCompanyType_ID,smj_insurancePlan_ID,smj_insuranceCompany_ID "+                                 
                                              "  ) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                    ps.setString(1, id);
                    ps.setString(2, nombre1);
                    ps.setString(3, nombre2);
                    ps.setString(4, id);
                    ps.setString(5, duns);
                    ps.setString(6, taxId);
                    ps.setString(7, nombre);
                    ps.setString(8, category);
                    ps.setDouble(9, creditUsed);    //credito = 0  - local en el pos
                    ps.setBoolean(10, Boolean.parseBoolean(isActive));
                    ps.setDouble(11, creditLimit);
                    ps.setBoolean(12, Boolean.parseBoolean(taxExempt));
                    ps.setString(13, card.trim());
                    // info de aseguradoras
                    
                    
                    ps.setBoolean(14, Boolean.parseBoolean(smj_isinsuranceCompany.trim()));
                
                    if (smj_amountDue_debt.equalsIgnoreCase(""))
                    {
                        smj_amountDue_debt = "0";
                    }
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n smj_amountDue_debt >"+smj_amountDue_debt+"<");
                    ps.setDouble(15, Double.parseDouble(smj_amountDue_debt));
                    
                    ps.setString(16, smj_nationalIDNumber.trim());
                    ps.setString(17, smj_insuranceCompanyType_ID.trim());
                    ps.setString(18, smj_insurancePlan_ID.trim());
                    ps.setString(19, smj_insuranceCompany_ID.trim());
                    
                    int i = ps.executeUpdate();
                    if (i != 0){
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE, "tercero not Inserted");
                    } 
                    else {
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE, "tercero not Inserted");
                    }
                }
                res2.close();
            }
            res.close();
        } catch (SQLException ex) {
            setErrorInLog("Business Partner Error", ex);
        }
    }
    
        /**
     *  Processes Insurance company data
     *  Tabla: BPARTNER  
     * @param id
     * @param taxId
     * @param nombre1
     * @param nombre2
     * @param valor
     * @param duns
     */
    void procesaAseguradora (String id,String taxId,String nombre1,String nombre2,String valor,String duns,String isActive,String category, String nombre, double creditLimit,double creditUsed, String taxExempt,String card,
                         String smj_isinsuranceCompany,String smj_amountDue_debt,String smj_nationalIDNumber,String smj_insuranceCompanyType_ID,String smj_insurancePlan_ID,String smj_insuranceCompany_ID)
    {
        
        try {
            PreparedStatement ps = con.prepareStatement("select smj_insuranceCompany_ID  from smj_insuranceCompany where smj_insuranceCompany_ID=?");
            ps.setString(1, taxId);
            ResultSet res = ps.executeQuery();

            if (res.next()) {   // el tercero i existia con el id especificado
                String q = "update smj_insuranceCompany set smj_amountDue_debt=?,smj_nationalIDNumber=?,smj_insuranceCompanyType_ID=?,name =?,smj_insuranceCompany_ID= ? "+  
                                          " where id=?";
                logger.log(Level.SEVERE,"\n+++ SQL >"+q+"<");
                ps = con.prepareStatement(q);
                
                if (smj_amountDue_debt.equalsIgnoreCase(""))
                    {
                        smj_amountDue_debt = "0";
                    }
                logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n smj_amountDue_debt >"+smj_amountDue_debt+"<");
                ps.setDouble(1, Double.parseDouble(smj_amountDue_debt));
                ps.setString(2, smj_nationalIDNumber.trim());
                ps.setString(3, smj_insuranceCompanyType_ID.trim());
                ps.setString(4, nombre.trim());
                ps.setString(5, taxId);
                ps.setString(6, id.trim());

                int i = ps.executeUpdate();
                 if (i != 0) {
                     logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                     logger.log(Level.SEVERE, "Insurance company updated");
                 } else {
                     logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                     logger.log(Level.SEVERE, "Insurance  company  not updated");
                 }
                 
            } else {  
                    ps = con.prepareStatement("insert into smj_insuranceCompany(smj_amountDue_debt,smj_nationalIDNumber,smj_insuranceCompanyType_ID,smj_insuranceCompany_ID,name,id "+                                 
                                              "  ) values(?,?,?,?,?,?)");
                    // info de aseguradoras
                    
                    if (smj_amountDue_debt.equalsIgnoreCase(""))
                    {
                        smj_amountDue_debt = "0";
                    }
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n smj_amountDue_debt >"+smj_amountDue_debt+"<");
                    ps.setDouble(1, creditUsed);
                    ps.setString(2, smj_nationalIDNumber.trim());
                    ps.setString(3, smj_insuranceCompanyType_ID.trim());
                    ps.setString(4, taxId.trim());
                    ps.setString(5, nombre.trim());
                    ps.setString(6, id.trim());
                    
                    int i = ps.executeUpdate();
                    if (i != 0){
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE, "Insurance company Inserted");
                    } 
                    else {
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE, "Insurance company not Inserted");
                    }
                }
            
            res.close();
        } catch (SQLException ex) {
            setErrorInLog("Insurance company Error", ex);
        }
    }

    
    
    
    
/**
 * Processing a product, if you update it, if you do not believe (veficia by ID)
     *  Tabla: PRODUCT
 * @param id
 * @param valor
 * @param nombre1
 * @param categoria
 * @param unidad
 * @param accesorio
 * @param ayuda
 * @param uomId
 */
    void procesaProducto (String id,String valor,String nombre1,String categoria,String unidad, String accesorio,String ayuda,String cocina,String uomId,String taxCategoryId, boolean  isActive, String upc,String imgUrl,String existencias, String ubicacion)
    {
        // hace un select para mirar si el tercero existe, si esta lo actualiza, si no lo crea
    	try {
            
        // obtiene datos de unidades    
            
    	PreparedStatement ps = con.prepareStatement("select id  from products where id=?");
        ps.setString(1, id);
        ResultSet res = ps.executeQuery();
        if (!categoria.trim().equalsIgnoreCase(dlSystem.getResourceAsText("id.basicProduct")))  //no es materia prima
        {
            if (!res.next()) {              // no existia el producto   (insert)
                ps = con.prepareStatement("insert into products(id, name, category,attributes,reference,code,pricebuy,pricesell,taxcat,unit) values(?,?,?,?,?,?,?,?,?,?)");
                ps.setString(1, id);
                ps.setString(2, nombre1);
                ps.setString(3, categoria);
                // adicionar segun la categoria el envio a bar, lo demas va para la cocina
                String atri = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">"+
                    "<properties>"+
                    "<entry key=\"printkb\">"+cocina.trim() +"</entry>"+ 
                    "<entry key=\"sendstatus\">No</entry>"+
                    "<entry key=\"accesorio\">"+ accesorio +"</entry>"+
                    "<entry key=\"unidadId\">"+ uomId+"</entry>"+    
                    "<entry key=\"unidad\">"+ getUnitText(uomId)+"</entry>"+
                    "<entry key=\"info\">"+ayuda+"</entry>"+
                    "<entry key=\"existencias\">"+existencias+"</entry>"+
                    "<entry key=\"ubicacion\">"+ubicacion+"</entry>"+
                    "<entry key=\"unidadDefault\">"+" "+"</entry>"+
                    "<entry key=\"unidadDefaultTexto\">"+" "+"</entry>"+                        
                    "</properties>";
                
                ByteArrayInputStream b = new ByteArrayInputStream (atri.getBytes("UTF-8"));
                ps.setBinaryStream(4, (InputStream) b, (int) atri.length());
                ps.setString(5, valor +  "  ");   //reference
                ps.setString(6, upc);  //code
                ps.setInt(7, 0);   //pricebuy
                ps.setInt(8, 0);   //pricesell
                ps.setString(9,taxCategoryId );   //taxcat
                ps.setString(10,uomId );   //uomId (unidad por defecto)
                //@win
                //ps.setBoolean(11, isActive); //isActive

                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                    logger.log(Level.SEVERE, "Prod Inserted"+nombre1+"|atributes|"+atri);
                     
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Prod not Inserted");
                }
    
                
             PreparedStatement psp = con.prepareStatement("select product  from stockcurrent where product=?");
            psp.setString(1, id);
            ResultSet resp = psp.executeQuery();
            if (!resp.next()) {   // no previous stock was reported
                   ps = con.prepareStatement("insert into stockcurrent(product, units,location) values(?,?,?)");
                ps.setString(1, id);
                ps.setInt(2, Integer.parseInt(existencias));
                ps.setString(3,ubicacion );
            }
            else {   /// there is already a stock entry for this product
                ps = con.prepareStatement("update stockcurrent set units = ?,location = ? where  product = ?");
                ps.setInt(1, Integer.parseInt(existencias));
                ps.setString(2,ubicacion );
                ps.setString(3, id);
                
            }
             //inserta existencias del producto en la tabla stockcurrent si ellas no existen antes ...

                i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"stock current Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"stock current not Inserted");
                }

    //inserta el id del producto en la tabla products_cat
                procesaProductsCat(id, isActive);
                procesaConversionUnidad(id, uomId ,uomId ,"1","1", id);

            } else {                             // si existia el producto  (update)
             ps = con.prepareStatement("update products set name=?,category=?, attributes=?,unit=?, taxcat=?, code =?, reference=?, visible=? where id=?");
             ps.setString(1, nombre1);
             ps.setString(2, categoria);
   
             
                HashMap<String,String> defUOMComplete =  getDefaultUOMComplete(id) ;
        
            String defUOMid = null;   String defUOMidText  = null;
            if (defUOMComplete != null) {
                defUOMid = defUOMComplete.get ("id");
                defUOMidText  = defUOMComplete.get ("name");
            }
       if (defUOMidText == null)
        {
            defUOMid = "100";
            defUOMidText  = getUnitText(defUOMid);
        } 
             
             // adicionar segun la categoria el envio a bar, lo demas va para la cocina
             String atri = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">"+
                    "<properties>"+
                    "<entry key=\"printkb\">"+cocina.trim()+"</entry>"+ 
                    "<entry key=\"sendstatus\">No</entry>"+
                    "<entry key=\"accesorio\">"+ accesorio +"</entry>"+
                    "<entry key=\"unidad\">"+ getUnitText(uomId)+"</entry>"+
                    "<entry key=\"unidadId\">"+ uomId+"</entry>"+   
                    "<entry key=\"info\">"+ayuda+"</entry>"+
                    "<entry key=\"existencias\">"+existencias+"</entry>"+
                    "<entry key=\"ubicacion\">"+ubicacion+"</entry>"+
                    "<entry key=\"unidadDefault\">"+ defUOMid +"</entry>"+
                    "<entry key=\"unidadDefaultTexto\">"+ defUOMidText +"</entry>"+
                     "</properties>";
             
             ByteArrayInputStream b = new ByteArrayInputStream (atri.getBytes());
             ps.setBinaryStream(3, (InputStream) b, (int) atri.length());
             ps.setString(4, uomId);
             ps.setString(5, taxCategoryId);
             ps.setString(6, upc);
             ps.setString(7, valor+"  ");
                       ps.setBoolean(8, isActive);
             ps.setString(9, id);

             int i = ps.executeUpdate();
              if (i != 0) {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Prod updated:"+nombre1+"|"+atri);
              } else {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Prod not updated");
              }
              procesaProductsCat(id, isActive);
            }
            
            // aqui actualiza las localizaiones del producto, cunado esta actualizando el producto
            
            PreparedStatement psp = con.prepareStatement("select product  from stockcurrent where product=?");
            psp.setString(1, id);
            ResultSet resp = psp.executeQuery();
            if (!resp.next()) {   // no previous stock was reported
                   ps = con.prepareStatement("insert into stockcurrent(product, units,location) values(?,?,?)");
                ps.setString(1, id);
                ps.setInt(2, Integer.parseInt(existencias));
                ps.setString(3,ubicacion );
            }
            else {   /// there is already a stock entry for this product
                ps = con.prepareStatement("update stockcurrent set units = ?,location = ? where  product = ?");
                ps.setInt(1, Integer.parseInt(existencias));
                ps.setString(2,ubicacion );
                ps.setString(3, id);
                
            }
             //inserta existencias del producto en la tabla stockcurrent si ellas no existen antes ...
              int  j = ps.executeUpdate();
                if (j != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"stock current updated");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"stock current not Inserted");
                }

    //inserta el id del producto en la tabla products_cat
                procesaProductsCat(id, isActive);
                procesaConversionUnidad(id, uomId ,uomId ,"1","1", id);
            
            
            
            
            
            
        }
        res.close();	
    	
    	} catch (Exception e) {
            setErrorInLog("Product error " +id + " " + nombre1, e);
	}
    	
    }
 
    
    /**
     * Updates the stocj and locator of this product within the attributes (properties section)
     * @param id
     * @param existencias
     * @param ubicacion 
     */
 
    void actualizaProductoStock (String id, String existencias, String ubicacion) {
        
    try {
        // trae datos del producto acua, si esta lo actualiza, si no lo crea
        //ProductInfoExt p =  new ProductInfoExt(); 
        Properties p = getProductAtributes(id);
       
        HashMap<String,String> defUOMComplete =  getDefaultUOMComplete(id) ;

        String defUOMid = null;   String defUOMidText  = null;
        if (defUOMComplete != null) {
        defUOMid = defUOMComplete.get ("id");
        defUOMidText  = defUOMComplete.get ("name");
        }
        if (defUOMidText == null)
        {
            defUOMid = "100";
            defUOMidText  = getUnitText(defUOMid);
        } 

        PreparedStatement ps = con.prepareStatement("update products set attributes=? where id=?");
             String atri = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">";
                atri +=    "<properties>";
                atri +=    "<entry key=\"printkb\">"+p.getProperty("printkb").trim()+"</entry>";
                atri +=    "<entry key=\"sendstatus\">"+p.getProperty("sendstatus")+"</entry>";
                atri +=    "<entry key=\"accesorio\">"+ p.getProperty("accesorio") +"</entry>";
                atri +=    "<entry key=\"unidad\">"+p.getProperty("unidad")+"</entry>";
                atri +=    "<entry key=\"unidadId\">"+p.getProperty("unidadId")+"</entry>";
                atri +=    "<entry key=\"info\">"+p.getProperty("info")+"</entry>";
                atri +=    "<entry key=\"existencias\">"+existencias+"</entry>";  //update stock
                atri +=    "<entry key=\"ubicacion\">"+ubicacion+"</entry>";    //updated locator
                atri +=    "<entry key=\"unidadDefault\">"+ defUOMid +"</entry>";
                atri +=    "<entry key=\"unidadDefaultTexto\">"+ defUOMidText +"</entry>";
                atri +=    "</properties>";
             
             //System.out.println("@@@@@@@@ Product id"+ id +" Atributos creados: \n "+ atri);
            
             ByteArrayInputStream b = new ByteArrayInputStream (atri.getBytes());
             ps.setBinaryStream(1, (InputStream) b, (int) atri.length());
             ps.setBytes(1, atri.getBytes());
             ps.setString(2, id);

             int i = ps.executeUpdate();
              if (i != 0) {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Prod updated:"+id+"|"+atri);
              } else {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Prod not updated");
              }
        
    	
    	} catch (Exception e) {
            e.printStackTrace();
            setErrorInLog("Product error " +id , e);
	}
    	
    }
    
    
    
    
    /**
     * process a images  product. stored in the database as a byte array
     * @param imgByteArray
     * @param id 
     */
    void procesaImgProducto(byte[] imgByteArray, String id){
        try {
            PreparedStatement ps = con.prepareStatement("update products set image=?  where id=?");
            ps.setBytes(1, imgByteArray);
            ps.setString(2, id);
            
            int i = ps.executeUpdate();
            if (i != 0) {
                logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                logger.log(Level.SEVERE,"Prod IMG updated");
            } else {
                logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                logger.log(Level.SEVERE,"Prod IMG not updated");
            }
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
   
    /**
     * processes and product catalogs on or off depending on the case
     * @param productId
     * @param isActive 
     */
    void procesaProductsCat(String productId,boolean isActive){
        PreparedStatement ps = null;
        ResultSet res = null;
        int i =0;
        try {
            ps = con.prepareStatement("select product  from products_cat where product=?");
            ps.setString(1, productId);
            res = ps.executeQuery();
            if(!res.next()){      
                if(isActive){
                    ps = con.prepareStatement("insert into products_cat(product, catorder) values(?,?)");
                    ps.setString(1, productId);
                    ps.setInt(2, 0);

                    i = ps.executeUpdate();
                    if (i != 0){
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Prod cat Inserted");
                    } 
                    else {
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Prod cat not Inserted");
                    }
                }
            }else if(!isActive){
                ps = con.prepareStatement("delete from products_cat where product=?");
                    ps.setString(1, productId);

                    i = ps.executeUpdate();
                    if (i != 0){
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Prod cat deleted");
                    } 
                    else {
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Prod cat not deleted");
                    }
            }
        } catch (SQLException ex) {
            setErrorInLog("Prod catalog error", ex);
        }
    }

    
    /**
     * processes locators on or off depending on the case
     * @param id, description
     * 
     */
    void procesaLocator(String id,String name,boolean isActive,String idErp){
        PreparedStatement ps = null;
        ResultSet res = null;
        int i =0;
        try {
            ps = con.prepareStatement("select *  from locations where id=?");
            ps.setString(1, id);
            res = ps.executeQuery();
            if(!res.next()){  // no existe en las ubicaciones actuales, entonces se crea si esta activo    
                if(isActive){
                    ps = con.prepareStatement("insert into locations(id, name,address) values(?,?,?)");
                    ps.setString(1, id);
                    ps.setString(2, name);
                    ps.setString(3, idErp);
                    i = ps.executeUpdate();
                    if (i != 0){
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Locator Inserted");
                    } 
                    else {
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Locator not Inserted");
                    }
                }
            }else      // existe en el POS
                if(!isActive){  // si se desactiva en el ERP, entonces se borra en el POS
                ps = con.prepareStatement("delete from locations where id=?");
                    ps.setString(1, id);

                    i = ps.executeUpdate();
                    if (i != 0){
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Locator deleted");
                    } 
                    else {
                        logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Locator not deleted");
                    }
                  } //is active
               else {    // esta activo y existia - lo actualiza entonces 
                  ps = con.prepareStatement("update locations set address= ? where id=?");
                  ps.setString(1, idErp);
                  ps.setString(2, id);
                  int j = ps.executeUpdate();
              if (j != 0) {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"locator updated");
              } else {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"locator not updated");
              }    
                
            }
        } catch (SQLException ex) {
            setErrorInLog("Locator error", ex);
        }
    }
    
    /**
     * Processed product category (the id and the name will be equal in the POS from the name of the ERP
     * @param nombre
     */
    void procesaCategoriasProd (String id,String nombre,boolean isActive)
    {
        // hace un select y mria si existe, si esta lo actualiza, si no lo crea
    	try {
            PreparedStatement ps = con.prepareStatement("select id  from categories where id=?");
            ps.setString(1, id);
            ResultSet res = ps.executeQuery();
            if (!(id.trim().equalsIgnoreCase(dlSystem.getResourceAsText("id.basicProduct"))))  //no es materia prima 
            {
            if (!res.next() ) {    // no existia el usuario y    (insert)
                    ps = con.prepareStatement("insert into categories(id, name,visible) values(?,?,?)");
                ps.setString(1, id);
                ps.setString(2, nombre);
                ps.setBoolean(3, isActive);

                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Cat Prod Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Cat Prod not Inserted");
                }
            }   
            else {                              // si existia el usuario  (update)
             ps = con.prepareStatement("update categories set name=?, visible=? where id=?");
             ps.setString(1, nombre);
             ps.setBoolean(2, isActive);
             ps.setString(3,id);

             int i = ps.executeUpdate();
              if (i != 0) {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Cat Prod updated");
              } else {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Cat Prod not updated");
              }
            }
        }
        res.close();	
    	
    	} catch (Exception e) {
           setErrorInLog("Product categories error ", e);
    	}
    }
    
    /**
     * Processes price changes
     * @param idProd
     * @param precio
     */
    void procesaPrecioProd (String idProd,String precio, String precioStd,String precioLimite)
    {
        // hace un select y mria si existe, si esta lo actualiza, si no lo crea
    	 
    	try {
    	PreparedStatement ps = con.prepareStatement("update products set pricesell=?, pricestd=?, pricelimit=? where id=?");
         ps.setDouble(1, Double.parseDouble(precio));
         ps.setDouble(2, Double.parseDouble(precioStd));
         ps.setDouble(3, Double.parseDouble(precioLimite));
         ps.setString(4,idProd);
         
         int i = ps.executeUpdate();
          if (i != 0) {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"Precio Prod updated");
          } else {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"Precio not updated");
          }
            
//            procesaPrecioUndConId(idProd,);
    	
    	} catch (Exception e) {
             setErrorInLog("Product prices error", e);
	}
    	
    }
    
    
    /**
     * Processes inventory changes
     * @param idProd
     * @param precio
     */
    void procesaStockProd (String idProd,String stock,String locator)
    {
        // hace un select y mria si existe, si esta lo actualiza, si no lo crea
    	PreparedStatement ps; 
    	try {
            PreparedStatement psp = con.prepareStatement("select product  from stockcurrent where product=?");
            psp.setString(1, idProd);
            ResultSet resp = psp.executeQuery();
            if (!resp.next()) {   // no previous stock was reported
                ps = con.prepareStatement("insert into stockcurrent(product, units,location) values(?,?,?)");
                ps.setString(1, idProd);
                ps.setDouble(2, Double.parseDouble(stock));
                ps.setString(3,locator );
            }
            else {   /// there is already a stock entry for this product
                ps = con.prepareStatement("update stockcurrent set units = ?,location = ? where  product = ?");
                ps.setDouble(1, Double.parseDouble(stock));
                ps.setString(2, locator);
                ps.setString(3, idProd);
                
            }

         
         int i = ps.executeUpdate();
          if (i != 0) {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"Stock Prod updated or inserted");
          } else {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"Stock not updated or inserted");
          }
    	
    	} catch (Exception e) {
             setErrorInLog("Product stock error", e);
	}
        try {
            // update the product attributes (stock & locator) 
            //get the info of the current product
          actualizaProductoStock(idProd, stock, locator);  
        } catch (Exception ex) {
            Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Process email changes 
     * @param email
     * @param precio
     */
    void procesaUsuario (String email,String id)
    {
        // hace un select y mria si existe, si esta lo actualiza, si no lo crea
    	 
    	try {
    		PreparedStatement ps = con.prepareStatement("update customers set email=? where id=?");
            ps.setString(1, email); 
            ps.setString(2, id);
         int i = ps.executeUpdate();
          if (i != 0) {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"email updated");
          } else {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"email not updated");
          }
    	
    	} catch (Exception e) {
             setErrorInLog("User error", e);
	}
    	
    }
    
    /**
     * Processing changes in the location table from BP - Locations
     * @param email
     * @param precio
     */
    void procesaBPLocation (String fax, String phone, String cellPhone,String locId, String id)
    {
        // hace un select y mria si existe, si esta lo actualiza, si no lo crea
    	 
    	try {
    		PreparedStatement ps = con.prepareStatement("update customers set fax=?, phone=?,phone2=?,address2=? where id=?");
                ps.setString(1, fax); 
                ps.setString(2, phone); 
                ps.setString(3, cellPhone);
                ps.setString(4, locId);
                ps.setString(5, id);
         int i = ps.executeUpdate();
          if (i != 0) {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"phone updated");
          } else {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"phone not updated");
          }
    	
    	} catch (Exception e) {
             setErrorInLog("BP Location error", e);
	}
    	
    }
    /**
     * Update existing locations with the text of the address, city and region
     * @param add1
     * @param add2
     * @param city
     * @param regionName
     * @param locId
     */
    void procesaLocation (String add1, String add2, String city, String regionName,String locId,String  countryId, String postalCode)
    {
        String            countryName = "";
        PreparedStatement ps = null;
        ResultSet         rs = null;
        
        // hace un select y mria si existe, si esta lo actualiza, si no lo crea
    	 
    	try {
            ps = con.prepareStatement("Select name from country where id = ?");
            ps.setString(1, countryId);
            rs = ps.executeQuery();
            if(rs.next())
                countryName = rs.getString("name");
            
            rs.close();
            ps.close();
            
            ps = con.prepareStatement("Select name from region where id = ?");
            ps.setString(1, regionName);
            rs = ps.executeQuery();
            if(rs.next())
                regionName = rs.getString("name");
            
            ps = con.prepareStatement("Select name from city where id = ?");
            ps.setString(1, city);
            rs = ps.executeQuery();
            if(rs.next())
                city = rs.getString("name");
            /*
            ps = con.prepareStatement("Select address2 from customers where id = ?");
            ps.setString(1, city);
            rs = ps.executeQuery();
            if(rs.next())         // revisa si existia una localizacion previamente para actualizarla si no have el update con la info nueva
            { */
             // usuario ya tenia localizaion entonces la actualiza            
            ps = con.prepareStatement("update customers set address=?,city=?,region=?,country=?,postal=? where address2=?");
            ps.setString(1, add1.trim());//+" "+add2.trim()); 
            ps.setString(2, city);
            ps.setString(3, regionName);
            ps.setString(4, countryName);
            ps.setString(5, postalCode);
            ps.setString(6, locId);
            int i = ps.executeUpdate();
            if (i != 0) {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE, "current location updated");
            } else {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"current location not updated");
               }
//            }
            /*
            else {
             // usuario no tenia la localizaion entonces se crea (update a customer)
            ps = con.prepareStatement("update customers set address=?,city=?,region=?,country=?,postal=? where address2=?");
            ps.setString(1, add1.trim());//+" "+add2.trim()); 
            ps.setString(2, city);
            ps.setString(3, regionName);
            ps.setString(4, countryName);
            ps.setString(5, postalCode);
            ps.setString(6, locId);
            int i = ps.executeUpdate();
            if (i != 0) {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE, "current location updated");
            } else {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"current location not updated");   
            }
             
             */
    	} catch (Exception e) {
            setErrorInLog("BP Location error 1", e);
        }finally{
            try {
                if(rs != null && !rs.isClosed()){
                    ps.close();
                }
            } catch (SQLException ex) {
                setErrorInLog("BP Location error 2", ex);
            }
            try {
                if(ps != null && !ps.isClosed()){
                    ps.close();
                }
            } catch (SQLException ex) {
                setErrorInLog("BP Location error 3", ex);
            }
        }
    	
    }
    
    /**
     * processing units derived from ERP
     * @param id
     * @param code
     * @param name
     * @param costingPrecision
     * @param type
     * @param stdPrecision 
     */
    void procesaUnidad(String id, String code, String name,String costingPrecision, String type, String stdPrecision){
        // hace un select y mirar si existe, si esta lo actualiza, si no lo crea
    	try {
            PreparedStatement ps = con.prepareStatement("select id  from units where id=?");
            ps.setString(1, id);
            ResultSet res = ps.executeQuery();
            
            if (!res.next() ) {      // no existia el usuario y    (insert)
                ps = con.prepareStatement("insert into units(id, code, name, costing_precision, type, std_precision) values(?,?,?,?,?,?)");
                ps.setString(1, id);
                ps.setString(2, code);
                ps.setString(3, name);
                ps.setDouble(4, Double.parseDouble(costingPrecision));
                ps.setString(5, type);
                ps.setDouble(6, Double.parseDouble(stdPrecision));

                int i = ps.executeUpdate();
                
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"unit Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"insert not Inserted");
                }

            }  else {                              // si existia el usuario  (update)
             ps = con.prepareStatement("update units set code =?, name=?, costing_precision =?, type=?, std_precision =?   where id=?");
             ps.setString(1, code);
             ps.setString(2, name);
             ps.setDouble(3, Double.parseDouble(costingPrecision));
             ps.setString(4, type);
             ps.setDouble(5, Double.parseDouble(stdPrecision));
             ps.setString(6,id);

             int i = ps.executeUpdate();
              if (i != 0) {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"unit updated");
              } else {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"unit not updated");
              }
            }
            res.close();	
    	
    	} catch (Exception e) {
            setErrorInLog("Unit error", e);
    	}
    }
    
    /**
     * Processing unit conversions product
     * @param id
     * @param unitId
     * @param unitToId
     * @param divideRate
     * @param multiplyRate
     * @param productId 
     */
    void procesaConversionUnidad(String id, String unitId, String unitToId,String divideRate, String multiplyRate,String productId){
        // hace un select y mirar si existe, si esta lo actualiza, si no lo crea
    	try {
            PreparedStatement ps = con.prepareStatement("select id  from additional_prices_for_products where id=?");
            ps.setString(1, id);
            ResultSet res = ps.executeQuery();
            
            if (!res.next() ) {      // no existia el usuario y    (insert)
                ps = con.prepareStatement("insert into additional_prices_for_products(id, unit_id, unit_to_id, divide_rate, multiply_rate,product_id) values(?,?,?,?,?,?)");
                ps.setString(1, id);
                ps.setString(2, unitId);
                ps.setString(3, unitToId);
                ps.setDouble(4, Double.parseDouble(divideRate));
                ps.setDouble(5, Double.parseDouble(multiplyRate));
                ps.setString(6, productId);

                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"unit Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"inset not Inserted");
                }

            }  else {                              // si existia el usuario  (update)
                 ps = con.prepareStatement("update additional_prices_for_products set unit_id =?, unit_to_id=?, divide_rate=?, multiply_rate=?, product_id =? where id=?");
                 ps.setString(1, unitId);
                 ps.setString(2, unitToId);
                 ps.setDouble(3, Double.parseDouble(divideRate));
                 ps.setDouble(4, Double.parseDouble(multiplyRate));
                 ps.setString(5, productId);
                 ps.setString(6,id);

                 int i = ps.executeUpdate();
                  if (i != 0) {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"additional_prices_for_products updated");
                  } else {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"additional_prices_for_products not updated");
                  }
            }
            
            res.close();	
    	
    	} catch (Exception e) {
            setErrorInLog("Unit conversion error", e);
    	}
    }
    
    /**
     * process countries
     * @param id
     * @param currencyID
     * @param countryCode
     * @param description
     * @param name
     * @param regionName 
     */
    void procesaPais(String id, String currencyID, String countryCode,String description, String name,String regionName){
        // hace un select y mirar si existe, si esta lo actualiza, si no lo crea
    	try {
            PreparedStatement ps = con.prepareStatement("select id  from country where id=?");
            ps.setString(1, id);
            ResultSet res = ps.executeQuery();
            
            if (!res.next() ) {      // no existia el usuario y    (insert)
                ps = con.prepareStatement("insert into country(id, currency_id, country_code, description, name,region_name) values(?,?,?,?,?,?)");
                ps.setString(1, id);
                ps.setString(2, currencyID);
                ps.setString(3, countryCode);
                ps.setString(4, description);
                ps.setString(5, name);
                ps.setString(6, regionName);

                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"country Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"country not Inserted");
                }

            }  else {                              // si existia el usuario  (update)
                 ps = con.prepareStatement("update country set currency_id =?, country_code=?, description=?, name=?, region_name =? where id=?");
                 ps.setString(1, currencyID);
                 ps.setString(2, countryCode);
                 ps.setString(3, description);
                 ps.setString(4, name);
                 ps.setString(5, regionName);
                 ps.setString(6,id);

                 int i = ps.executeUpdate();
                  if (i != 0) {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"country updated");
                  } else {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"country not updated");
                  }
            }
            res.close();	
    	
    	} catch (Exception e) {
            setErrorInLog("Country error", e);
    	}
    }
    
    /**
     * process regions for  information from bpartner
     * @param id
     * @param countryID
     * @param description
     * @param name 
     */
    void procesaRegion(String id, String countryID, String description, String name){
        // hace un select y mirar si existe, si esta lo actualiza, si no lo crea
    	try {
            PreparedStatement ps = con.prepareStatement("select id  from region where id=?");
            ps.setString(1, id);
            ResultSet res = ps.executeQuery();
            
            if (!res.next() ) {      // no existia el usuario y    (insert)
                ps = con.prepareStatement("insert into region(id, country_id, description, name) values(?,?,?,?)");
                ps.setString(1, id);
                ps.setString(2, countryID);
                ps.setString(3, description);
                ps.setString(4, name);

                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Region Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Region not Inserted");
                }

            }  else {                              // si existia el usuario  (update)
                 ps = con.prepareStatement("update region set country_id =?, description=?, name=? where id=?");
                 ps.setString(1, countryID);
                 ps.setString(2, description);
                 ps.setString(3, name);
                 ps.setString(4,id);

                 int i = ps.executeUpdate();
                  if (i != 0) {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Region updated");
                  } else {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Region not updated");
                  }
            }
            res.close();	
    	
    	} catch (Exception e) {
            setErrorInLog("Region error", e);
    	}
    }
    
    /**
     * process cities for partner
     * @param id
     * @param countryID
     * @param regionID
     * @param name
     * @param postal 
     */
    void procesaCiudad(String id, String countryID, String regionID, String name,String postal){
        // hace un select y mirar si existe, si esta lo actualiza, si no lo crea
    	try {
            PreparedStatement ps = con.prepareStatement("select id  from city where id=?");
            ps.setString(1, id);
            ResultSet res = ps.executeQuery();
            
            if (!res.next() ) {      // no existia el usuario y    (insert)
                ps = con.prepareStatement("insert into city(id, country_id, region_id,name, postal) values(?,?,?,?,?)");
                ps.setString(1, id);
                ps.setString(2, countryID);
                ps.setString(3, regionID);
                ps.setString(4, name);
                ps.setString(5, postal);

                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"city Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"city not Inserted");
                }

            }  else {                              // si existia el usuario  (update)
                 ps = con.prepareStatement("update city set country_id =?, region_id=?, name=?, postal=? where id=?");
                 ps.setString(1, countryID);
                 ps.setString(2, regionID);
                 ps.setString(3, name);
                 ps.setString(4, postal);
                 ps.setString(5,id);

                 int i = ps.executeUpdate();
                  if (i != 0) {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"city updated");
                  } else {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"city not updated");
                  }
            }
            res.close();	
    	
    	} catch (Exception e) {
            setErrorInLog("City error", e);
    	}
    }
    
    /**
     * processes the tax category
     * @param id
     * @param name 
     */
    void procesaCategoriasDeImpuestos(String id, String name){
        // hace un select y mirar si existe, si esta lo actualiza, si no lo crea
    	try {
            PreparedStatement ps = con.prepareStatement("select id  from taxcategories where id=?");
            ps.setString(1, id);
            ResultSet res = ps.executeQuery();
            
            if (!res.next() ) {      // no existia el usuario y    (insert)
                ps = con.prepareStatement("insert into taxcategories(id, name) values(?,?)");
                ps.setString(1, id);
                ps.setString(2, name);
                                
                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"taxcategories Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"taxcategories not Inserted");
                }

            }  else {                              // si existia el usuario  (update)
                 ps = con.prepareStatement("update taxcategories set name =? where id=?");
                 ps.setString(1, name);
                 ps.setString(2,id);

                 int i = ps.executeUpdate();
                  if (i != 0) {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"taxcategories updated");
                  } else {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"taxcategories not updated");
                  }
            }
            res.close();	
    	
    	} catch (Exception e) {
            setErrorInLog("Tax category error", e);
    	}
    }
    
    /**
     * processes the tax categories for clients
     * @param id
     * @param name 
     */
    void procesaCategoriasDeImpuestosPorClientes(String id, String name){
        // hace un select y mirar si existe, si esta lo actualiza, si no lo crea
    	try {
            PreparedStatement ps = con.prepareStatement("select id  from taxcustcategories where id=?");
            ps.setString(1, id);
            ResultSet res = ps.executeQuery();
            
            if (!res.next() ) {      // no existia el usuario y    (insert)
                ps = con.prepareStatement("insert into taxcustcategories(id, name) values(?,?)");
                ps.setString(1, id);
                ps.setString(2, name);
                                
                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"taxcustcategories Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"taxcustcategories not Inserted");
                }

            }  else {                              // si existia el usuario  (update)
                 ps = con.prepareStatement("update taxcustcategories set name =? where id=?");
                 ps.setString(1, name);
                 ps.setString(2,id);

                 int i = ps.executeUpdate();
                  if (i != 0) {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"taxcustcategories updated");
                  } else {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"taxcustcategories not updated");
                  }
            }
            res.close();	
    	
    	} catch (Exception e) {
            setErrorInLog("Tax category per client error", e);

    	}
    }
    
    /**
     * processes tax
     * @param id
     * @param name
     * @param validfrom
     * @param categoryID
     * @param rate 
     */
    void procesaImpuestos(String id, String name,Date validfrom,String categoryID,double rate ){
        // hace un select y mirar si existe, si esta lo actualiza, si no lo crea
    	try {
            rate = rate/100;
            PreparedStatement ps = con.prepareStatement("select id  from taxes where id=?");
            ps.setString(1, id);
            ResultSet res = ps.executeQuery();
            java.sql.Date validfromSQL = new java.sql.Date(validfrom.getTime());
            
            if (!res.next() ) {      // no existia el usuario y    (insert)
                ps = con.prepareStatement("insert into taxes(id, name, validfrom, category,rate) values(?,?,?,?,?)");
                ps.setString(1, id);
                ps.setString(2, name);
                
                
                ps.setDate(3, validfromSQL);
                
                ps.setString(4, categoryID);
                ps.setDouble(5, rate);
                                
                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"tax Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"tax not Inserted");
                }

            }  else {                              // si existia el usuario  (update)
                 ps = con.prepareStatement("update taxes set name =?, validfrom=?, category=?, rate=? where id=?");
                 ps.setString(1, name);
                 ps.setDate(2, validfromSQL);
                 ps.setString(3, categoryID);
                 ps.setDouble(4, rate);
                 ps.setString(5,id);

                 int i = ps.executeUpdate();
                  if (i != 0) {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"tax updated");
                  } else {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"tax not updated");
                  }
            }
            res.close();	
    	
    	} catch (Exception e) {
           setErrorInLog("Tax error ", e);
    	}
    }
    
    /**
     *processed product prices per unit
     * @param idProd
     * @param unidadNombre
     * @param precio
     * @param precioStd
     * @param precioLimite
     * @param isDefaultPrice 
     */
    void procesaPrecioUnd (String idProd,String unidadNombre,double precio, double precioStd,double precioLimite,String isDefaultPrice)
    {
    	 
    	try {
        // hace un select y mira si existe, si esta lo actualiza, si no lo crea
            /* 
            PreparedStatement ps = con.prepareStatement("select  product_id from additional_prices_for_products where product_id=? and unit_to_id = (select id from units where name = ?) ");
            ps.setString(1, idProd);
            ps.setString(2, unidadNombre);
            ResultSet res = ps.executeQuery();
            
            
            if (!res.next() ) {      // no existia el usuario y    (insert)
                ps = con.prepareStatement("insert into additional_prices_for_products(product_id,unit_to_id, pricelist,pricestd,pricelimit,is_default) values(?,?,?,?,?)");
                ps.setString(1, idProd);
                ps.setString(2, name);
                ps.setDouble(3, precio);
                ps.setDouble(4, precioStd);
                ps.setDouble(5, precioLimite);
                                
                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"tax Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"tax not Inserted");
                }

            }  else {   */
         PreparedStatement  ps = con.prepareStatement("update additional_prices_for_products set pricelist = ?, pricestd = ?, pricelimit = ?, is_default=? where product_id = ? and unit_to_id = (select id from units where name = ?)");
         ps.setDouble(1, precio);
         ps.setDouble(2, precioStd);
         ps.setDouble(3, precioLimite);
         if(isDefaultPrice.equalsIgnoreCase("Y"))
            ps.setBoolean(4, true);
         else
            ps.setBoolean(4, false);
         ps.setString(5,idProd);
         
         ps.setString(6, unidadNombre.trim());
         
         
         int i = ps.executeUpdate();
          if (i != 0) {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"Precio unidad updated");
          } else {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"Precio unidad not updated");
          }
    	
          if(isDefaultPrice.equalsIgnoreCase("Y")){
              procesaPrecioProd(idProd, Double.toString(precio), Double.toString(precioStd), Double.toString(precioLimite));
          }
           // }
    	} catch (Exception e) {
            setErrorInLog("Prices per unit error", e);
	}
    	
    }
    
    /**
     * process prices per unit of products bearing the ID
     * @param idProd
     * @param unidadId
     * @param precio
     * @param precioStd
     * @param precioLimite 
     */
    void procesaPrecioUndConId (String idProd,String unidadId,double precio, double precioStd,double precioLimite)
    {
        // hace un select y mria si existe, si esta lo actualiza, si no lo crea
    	 
    	try {
         PreparedStatement ps = con.prepareStatement("update additional_prices_for_products set pricelist = ?, pricestd = ?, pricelimit = ? where product_id = ? and unit_to_id = ?");
         ps.setDouble(1, precio);
         ps.setDouble(2, precioStd);
         ps.setDouble(3, precioLimite);
         ps.setString(4,idProd);
         ps.setString(5, unidadId);
         
         
         int i = ps.executeUpdate();
          if (i != 0) {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"Precio unidad updated");
          } else {
              logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
              logger.log(Level.SEVERE,"Precio unidad not updated");
          }
    	
    	} catch (Exception e) {
            setErrorInLog("Prices per unit error 2", e);
	}
    	
    }
    
    /**
     * processes names of credit cards for payments
     * @param nameCC 
     */
    void procesaCreditCard(String nameCC) {
        // hace un select y mria si existe, si esta lo actualiza, si no lo crea
    	 
    	String cardNames = dlSystem.getResourceAsText("card.names");
        String auxCardNames = cardNames.toUpperCase().trim();
        String nameCCAux = nameCC.toUpperCase().trim();
        
        int index = auxCardNames.indexOf(nameCCAux);
        if(index < 0){
            cardNames = cardNames+ ","+nameCC;
            dlSystem.setResource("card.names", 0, cardNames.getBytes());
        }
            
            
        
        
        
        
    }
    
    
    public void refreshDefaultUnits () {
        
        //  Loop to update the default units of all products
        PreparedStatement ps = null;
        String atr = null;
        ResultSet res = null;
        try {
            ps = con.prepareStatement("SELECT id FROM  products");
            res = ps.executeQuery();
            while (res.next()) {
                atr = res.getString("id");
                actualizaUnidadesProducto(atr);    
            }
        }catch(Exception e){
           logger.log(Level.SEVERE,"\n++Error trying to get name for unit    ++\n\n"+e.getMessage());
        }finally{
            try {
                if(rs != null && !rs.isClosed())
                    rs.close();
                if(ps != null && !ps.isClosed())
                    ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //SmartPOS
        
    }
    
    
    /**
     * Establishes connection to the database
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void Conexion() throws SQLException, ClassNotFoundException {
        try{
            /**
             * @Author Carlos Prieto
             * Modificacion de la forma de cargar las clases
             */
            ClassLoader cloader = new URLClassLoader(new URL[] {new File(props.getProperty("db.driverlib")).toURI().toURL()});
            DriverManager.registerDriver(new DriverWrapper((Driver) Class.forName(props.getProperty("db.driver"), true, cloader).newInstance()));
            
            url = props.getProperty("db.URL");
            usuario = props.getProperty("db.user");
            AltEncrypter cypher = new AltEncrypter("cypherkey" + usuario);
            clave = cypher.decrypt(props.getProperty("db.password").substring(6));

            con = DriverManager.getConnection(url,usuario,clave);

        }
        catch(Exception e){
            logger.log(Level.SEVERE,"\n++Error trying to get local DB connection    ++\n\n");
            logger.log(Level.SEVERE, null, e);

        }
    }
      
    public String mensaje(){
        return this.mensaje; 
    }
    
    public void cerrarBD()throws SQLException{
        cerrarConsulta();
        if(con!= null){
            con.close();
            con = null;
        }
    }
    /**
     *  ejecutada cuando el objeto dejar de existir
     */
    protected void finalize() throws java.lang.Throwable {
        cerrarBD();
        super.finalize();
    }
    
    public void cerrarConsulta() throws SQLException{
        if(rs!= null){
            rs.close();
            rs = null;
        }
        if(st != null){
            st.close();
            st = null;
        }
    }

    /**
     * removes the products and if not possible the inactive
     * @param id 
     */
    void procesaBorradoProducto(String id) {
        PreparedStatement ps = null;
        ResultSet res = null;
        try {
            ps = con.prepareStatement("select line  from ticketlines where product=?");
            ps.setString(1, id.trim());
            res = ps.executeQuery();

            if (!res.next()) {
                ps = con.prepareStatement("delete from products_cat where product = ?");
                ps.setString(1, id);
                ps.executeUpdate();
                
                ps = con.prepareStatement("delete from stockcurrent where product = ?");
                ps.setString(1, id);
                ps.executeUpdate();
                
                ps = con.prepareStatement("delete from stockdiary where product  = ?");
                ps.setString(1, id);
                ps.executeUpdate();
                
                ps = con.prepareStatement("delete from products where id = ?");
                ps.setString(1, id);
                ps.executeUpdate();
            }else{
                ps = con.prepareStatement("update products set visible = false where id = ?");
                ps.setString(1, id);
                ps.executeUpdate();
                
                ps = con.prepareStatement("delete from products_cat where product = ?");
                ps.setString(1, id);
                ps.executeUpdate();
            }
        }catch(Exception e){
            e.printStackTrace();
                logger.log(Level.SEVERE,"\n++Error trying to delete produtc    ++\n\n"+e.getMessage());
        }finally{
            try {
                if(rs != null && !rs.isClosed())
                    rs.close();
                if(ps != null && !ps.isClosed())
                    ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * eliminates product categories and if possible the inactive
     * @param id 
     */
    void procesaBorradoCategoriaProducto(String id) {
        PreparedStatement ps = null;
        ResultSet res = null;
        try {
            ps = con.prepareStatement("select id from products where category = ? and visible = true");
            ps.setString(1, id.trim());
            res = ps.executeQuery();

            if (!res.next()) {
                ps = con.prepareStatement("delete from products_cat where product in (select id from products where category = ? )");
                ps.setString(1, id);
                ps.executeUpdate();
                
                ps = con.prepareStatement("delete from categories where id = ?");
                ps.setString(1, id);
                ps.executeUpdate();
            }else{
//                ps = con.prepareStatement("delete from products_cat where product in (select id from products where category = ? )");
//                ps.setString(1, id);
//                ps.executeUpdate();
//                ps.close();

                ps = con.prepareStatement("update categories set visible = false where id = ?");
                ps.setString(1, id);
                ps.executeUpdate();
                
                
            }
        }catch(Exception e){
                logger.log(Level.SEVERE,"\n++Error trying to delete productt category    ++\n\n");
        }finally{
            try {
                if(rs != null && !rs.isClosed())
                    rs.close();
                if(ps != null && !ps.isClosed())
                    ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * eliminate customers if not possible inactive
     * @param id 
     */
    void procesaBorradoClientes(String id) {
        PreparedStatement ps = null;
        ResultSet res = null;
        try {
            ps = con.prepareStatement("select id from tickets where customer = ?");
            ps.setString(1, id.trim());
            res = ps.executeQuery();

            if (!res.next()) {
                ps = con.prepareStatement("delete from customers where id = ?");
                ps.setString(1, id);
                ps.executeUpdate();
            }else{
                ps = con.prepareStatement("update customers set visible = false where id = ?");
                ps.setString(1, id);
                ps.executeUpdate();
            }
        }catch(Exception e){
                        logger.log(Level.SEVERE,"\n++Error trying to delete customer    ++\n\n"+e.getMessage());
        }finally{
            try {
                if(rs != null && !rs.isClosed())
                    rs.close();
                if(ps != null && !ps.isClosed())
                    ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get details about stock and locator (ubicacion) of the product 
     * @param id 
     */
    HashMap <String,String> getDetailsStock(String id) {
        PreparedStatement ps = null;
        HashMap <String,String> stock = null;
        ResultSet res = null;
        try {
            ps = con.prepareStatement("select units, location from stockcurrent where product = ?");
            ps.setString(1, id.trim());
            res = ps.executeQuery();
            if (res.next()) {
                stock = new HashMap <String,String> ();
                stock.put("existencias",res.getString("units"));
                stock.put("ubicacion",res.getString("location"));
            }
        }catch(Exception e){
           logger.log(Level.SEVERE,"\n++Error trying to query stock    ++\n\n"+e.getMessage());
        }finally{
            try {
                if(rs != null && !rs.isClosed())
                    rs.close();
                if(ps != null && !ps.isClosed())
                    ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return stock;
    }
    
    /**
     * Get details about locator of (ubicacion) of the product 
     * @param id of ERP 
     */
    String getLocatorByIdErp(String idErp) {
        PreparedStatement ps = null;
        String ubicacion = "";
        ResultSet res = null;
        try {
            ps = con.prepareStatement("select id from locations where address = ?");
            ps.setString(1, idErp.trim());
            res = ps.executeQuery();
            if (res.next()) {
                ubicacion = res.getString("id");
            }
        }catch(Exception e){
           logger.log(Level.SEVERE,"\n++Error trying to query locations    ++\n\n"+e.getMessage());
        }finally{
            try {
                if(rs != null && !rs.isClosed())
                    rs.close();
                if(ps != null && !ps.isClosed())
                    ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ubicacion;
    }
    
    
    
    
        /**
     * Get attributes of the product 
     * @param id 
     */
    public Properties getProductAtributes(String id) {
        PreparedStatement ps = null;
        Properties atr = null;
        ResultSet res = null;
        try {
            ps = con.prepareStatement("SELECT attributes FROM PRODUCTS WHERE id = ?");
            ps.setString(1, id.trim());
            res = ps.executeQuery();
            if (res.next()) {
                atr = ImageUtils.readProperties(ImageUtils.getBytesInputStream(res.getBinaryStream("attributes"))) ;
            }
        }catch(Exception e){
           logger.log(Level.SEVERE,"\n++Error trying to get properties    ++\n\n"+e.getMessage());
        }finally{
            try {
                if(rs != null && !rs.isClosed())
                    rs.close();
                if(ps != null && !ps.isClosed())
                    ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return atr;
    }
   
    
            /**
     * Get a field of the product  
     * @param id 
     */
    public String getProductField(String field,String id) {
        PreparedStatement ps = null;
        String atr = null;
        ResultSet res = null;
        try {
            ps = con.prepareStatement("SELECT "+field.trim()+" FROM PRODUCTS WHERE id = ?");
            ps.setString(1, id.trim());
            res = ps.executeQuery();
            if (res.next()) {
                atr = res.getString(field.trim());
            }
        }catch(Exception e){
           logger.log(Level.SEVERE,"\n++Error trying to get field of product    ++\n\n"+e.getMessage());
        }finally{
            try {
                if(rs != null && !rs.isClosed())
                    rs.close();
                if(ps != null && !ps.isClosed())
                    ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return atr;
    }
    
  
    
     /**
     * Get a default unit for a product
     * @param id prodtc
     */
    public String getDefaultUOM(String id) {
        PreparedStatement ps = null;
        String atr = null;
        ResultSet res = null;
        try {
            ps = con.prepareStatement("SELECT id FROM  additional_prices_for_products  WHERE product_id = ? AND is_default = TRUE");
            ps.setString(1, id.trim());
            res = ps.executeQuery();
            if (res.next()) {
                atr = res.getString("name");
            }
        }catch(Exception e){
           logger.log(Level.SEVERE,"\n++Error trying to get name for unit    ++\n\n"+e.getMessage());
        }finally{
            try {
                if(rs != null && !rs.isClosed())
                    rs.close();
                if(ps != null && !ps.isClosed())
                    ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return atr;
    }
    
    
     /**
     * Get a description of a unit  
     * @param id 
     */
    public String getUnitText(String id) {
        PreparedStatement ps = null;
        String atr = null;
        ResultSet res = null;
        try {
            ps = con.prepareStatement("SELECT name FROM UNITS WHERE id = ?");
            ps.setString(1, id.trim());
            res = ps.executeQuery();
            if (res.next()) {
                atr = res.getString("name");
            }
        }catch(Exception e){
           logger.log(Level.SEVERE,"\n++Error trying to get name for unit    ++\n\n"+e.getMessage());
        }finally{
            try {
                if(rs != null && !rs.isClosed())
                    rs.close();
                if(ps != null && !ps.isClosed())
                    ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return atr;
    }
    
   
         /**
     * Get a default unit for a product
     * @param id prodtc
     */
    public HashMap getDefaultUOMComplete(String id) {
        PreparedStatement ps = null;
        HashMap<String,String> atr = null;
        ResultSet res = null;
         logger.log(Level.SEVERE,"\n @@@@@ Entro a getDefaultUOMComplete con prodid: "+ id+"  \n");
        try {
            ps = con.prepareStatement("SELECT u.name as name, u.id  as id  FROM additional_prices_for_products ap" +
                                         "  JOIN units u ON (u.id = ap.unit_to_id)  where ap.product_id = ? and ap.is_default = true");
            ps.setString(1, id.trim());
            res = ps.executeQuery();
            
            
            if (res.next()) {
                atr = new HashMap <String,String>();
                String idU = res.getString("id");
                String nameU =  res.getString("name");
                atr.put("name",nameU);
                atr.put("id",idU);
                logger.log(Level.SEVERE,"\n @@@@@Default Unit -> Product ID:"+ id +"-> codeU:"+idU +" textU->"+ nameU +"  \n");
            }
            
            
            
        }catch(Exception e){
           logger.log(Level.SEVERE,"\n++Error trying to get name for unit    ++\n\n"+e.getMessage());
        }finally{
            try {
                if(rs != null && !rs.isClosed())
                    rs.close();
                if(ps != null && !ps.isClosed())
                    ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(PosDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return atr;
    }
  
    
    
    /**
     * Updates the units of a product - default and nase
     * @param id prod
     
     */
 
    void actualizaUnidadesProducto(String id) {
        
    try {
        // trae datos del producto acua, si esta lo actualiza, si no lo crea
        //ProductInfoExt p =  new ProductInfoExt(); 
        Properties p = getProductAtributes(id);
        if (p != null) {
        logger.log(Level.SEVERE,"@@@@@@ Propiedades:"+p.toString());
        
        HashMap<String,String> defUOMComplete =  getDefaultUOMComplete(id) ;
        
        String defUOMid = null;   String defUOMidText  = null;
        if (defUOMComplete != null) {   // if the product doesnt have a valid unit - assigns the default (see resources)
        defUOMid = defUOMComplete.get ("id");
        defUOMidText  = defUOMComplete.get ("name");
        }
       if (defUOMidText == null)
        {
            defUOMid = "100";
            defUOMidText  = getUnitText(defUOMid);
        } 

        
        logger.log(Level.SEVERE,"((((((( Producto: "+id +" -Info unidad defecto:"+ defUOMid + " -"+ defUOMidText  );
        
         PreparedStatement ps = con.prepareStatement("update products set attributes=? where id=?");
             String atri = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">"+
                    "<properties>"+
                    "<entry key=\"printkb\">"+p.getProperty("printkb").trim()+"</entry>"+ 
                    "<entry key=\"sendstatus\">"+p.getProperty("sendstatus")+"</entry>"+
                    "<entry key=\"accesorio\">"+ p.getProperty("accesorio") +"</entry>"+
                    "<entry key=\"unidad\">"+p.getProperty("unidad")+"</entry>"+
                    "<entry key=\"unidadId\">"+p.getProperty("unidadId")+"</entry>"+
                    "<entry key=\"info\">"+p.getProperty("info")+"</entry>"+
                    "<entry key=\"existencias\">"+p.getProperty("existencias")+"</entry>"+     //update stock
                    "<entry key=\"ubicacion\">"+p.getProperty("ubicacion")+"</entry>"+        //updated locator
                    "<entry key=\"unidadDefault\">"+ defUOMid +"</entry>"+
                    "<entry key=\"unidadDefaultTexto\">"+ defUOMidText +"</entry>"+                        
                    "</properties>" ;
             
            logger.log(Level.SEVERE,"@@@@ Product id"+ id +" unidades de producto actualizadas: \n "+ atri);
            
             ByteArrayInputStream b = new ByteArrayInputStream (atri.getBytes());
             ps.setBinaryStream(1, (InputStream) b, (int) atri.length());
             ps.setBytes(1, atri.getBytes());
             ps.setString(2, id);

             int i = ps.executeUpdate();
              if (i != 0) {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Prod updated:"+id+"|"+atri);
              } else {
                  logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Prod not updated");
              }
        }  // if p != null       
        else {
            logger.log(Level.SEVERE," @@@@@@  Atributos del producto fueron nulos :"+id);  
        }
    	
    	} catch (Exception e) {
            e.printStackTrace();
            setErrorInLog("Product error " +id , e);
	}
    	
    }
    
    
  /**
     * 
     * @param smj_insurancePlan_ID
     * @param name
     * @param C_BPartner_ID
     * @param coPay_percentage
     * @param coPay_value 
     */
     
     
    void procesaPlanSeguros (String smj_insurancePlan_ID, String name,String C_BPartner_ID,Double coPay_percentage,Double coPay_value){
        // hace un select y mirar si existe, si esta lo actualiza, si no lo crea
    	try {
            PreparedStatement ps = con.prepareStatement("select smj_insurancePlan_ID  from smj_insurancePlan where smj_insurancePlan_ID=?");
            ps.setString(1, smj_insurancePlan_ID);
            ResultSet res = ps.executeQuery();
            
            if (!res.next() ) {      // no existia el registro y    (insert)
                ps = con.prepareStatement("insert into smj_insurancePlan(smj_insurancePlan_ID, name,C_BPartner_ID , coPay_percentage,coPay_value) values(?,?,?,?,?)");
                ps.setString(1, smj_insurancePlan_ID);
                ps.setString(2, name);
                ps.setString(3, C_BPartner_ID);
                ps.setDouble(4, coPay_percentage);
                ps.setDouble(5, coPay_value);
                                
                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Insurance plan Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Insurance plan not Inserted");
                }

            }  else {                              // si existia el registro  (update)
                 ps = con.prepareStatement("update smj_insurancePlan  set smj_insurancePlan_ID= ?,name =?, C_BPartner_ID=?, coPay_percentage=?, coPay_value=? where smj_insurancePlan_ID=?");
                ps.setString(1, smj_insurancePlan_ID);
                ps.setString(2, name);
                ps.setString(3, C_BPartner_ID);
                ps.setDouble(4, coPay_percentage);
                ps.setDouble(5, coPay_value);
                ps.setString(6, smj_insurancePlan_ID);
                
                 int i = ps.executeUpdate();
                  if (i != 0) {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Insurance plan updated");
                  } else {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Insurance plan not updated");
                  }
            }
            res.close();	
    	
    	} catch (Exception e) {
           setErrorInLog("Insurance plan  error ", e);
    	}
    }   
    
    
    
  /**
     * 
     * @param smj_insurancePlan_ID
     * @param name
     * @param C_BPartner_ID
     * @param coPay_percentage
     * @param coPay_value 
     */
     
     
    void procesaTipoAseguradora (String smj_insuranceCompanyType_ID, String name){
        // hace un select y mirar si existe, si esta lo actualiza, si no lo crea
    	try {
            PreparedStatement ps = con.prepareStatement("select smj_insuranceCompanyType_ID  from smj_insuranceCompanyType where smj_insuranceCompanyType_ID=?");
            ps.setString(1, smj_insuranceCompanyType_ID.trim());
            ResultSet res = ps.executeQuery();
            
            if (!res.next() ) {      // no existia el registro y    (insert)
                ps = con.prepareStatement("insert into smj_insuranceCompanyType(smj_insuranceCompanyType_ID, name) values(?,?)");
                ps.setString(1, smj_insuranceCompanyType_ID);
                ps.setString(2, name);
                int i = ps.executeUpdate();
                if (i != 0){
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Insurance company type Inserted");
                } 
                else {
                    logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                  logger.log(Level.SEVERE,"Insurance company typenot Inserted");
                }

            }  else {                              // si existia el registro  (update)
                 ps = con.prepareStatement("update smj_insuranceCompanyType  set smj_insuranceCompanyType_ID= ?,name =? where smj_insuranceCompanyType_ID=?");
                ps.setString(1, smj_insuranceCompanyType_ID);
                ps.setString(2, name);
                ps.setString(3, smj_insuranceCompanyType_ID);
                
                 int i = ps.executeUpdate();
                  if (i != 0) {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Insurance company type updated");
                  } else {
                      logger.log(Level.SEVERE,"\n+++++++++++++++++++++++++\n\n");
                      logger.log(Level.SEVERE,"Insurance company type not updated");
                  }
            }
            res.close();	
    	
    	} catch (Exception e) {
           setErrorInLog("Insurance company type error ", e);
    	}
    }   
    
    
    
    
    
}