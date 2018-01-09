/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ewallet.utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author Ade Saprudin
 */
public class Util {
    
    public static Gson gson = new GsonBuilder().setDateFormat(
            "yyyy-MM-dd").create();
    public static Gson gson2 = new GsonBuilder().setDateFormat(
			"yyyy-MM-dd HH:mm:ss").create();
    
}
