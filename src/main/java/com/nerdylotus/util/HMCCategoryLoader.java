package com.nerdylotus.util;


import java.io.InputStream;
import java.util.Properties;

import net.sf.json.JSONObject;

import com.nerdylotus.core.NLPersistance;
import com.nerdylotus.core.NLRedisPersistanceImpl;
import com.nerdylotus.game.NLCategoryLoader;
import com.nerdylotus.hmc.core.HMCGameConfig;

public class HMCCategoryLoader implements NLCategoryLoader{
	
	public static void main(String[] args) {		
		(new HMCCategoryLoader()).loadCategories();
	}
	public void loadCategories() {
		try{
			Properties prop = new Properties();
			InputStream in = HMCCategoryLoader.class.getClassLoader().getResourceAsStream("resources/categories.properties");
			prop.load(in);
			in.close();
	        NLPersistance nlp = NLRedisPersistanceImpl.getInstance();
	        JSONObject allcategories = new JSONObject();
	        for(Object key: prop.keySet()){	
	        	String tempKey = (String) key;
	        	String tempStr = (String)prop.get(key);
	        	String[] sarr = tempStr.split(",");
	        	HMCGameConfig cnf = new HMCGameConfig();
	        	cnf.setWordcategory(sarr[0]);
	        	cnf.setBuyin(Double.parseDouble(sarr[1]));
	        	cnf.setLookup(tempKey);
	        	cnf.setMaxplayers(Integer.parseInt(sarr[2]));
	        	cnf.setMinplayers(Integer.parseInt(sarr[3]));	
	        	JSONObject jcnf = JSONObject.fromObject(cnf);	
	        	nlp.setValueForKey(tempKey, jcnf.toString());
	        	allcategories.put(tempKey, jcnf);
	        }
	        nlp.setValueForKey("HM", allcategories.toString());
        }catch(Exception e){
        	e.printStackTrace();
        }		
	}

}
