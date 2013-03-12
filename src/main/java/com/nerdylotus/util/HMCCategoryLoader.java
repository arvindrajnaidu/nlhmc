package com.nerdylotus.util;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.json.JSONObject;

import com.nerdylotus.core.NLFactory;
import com.nerdylotus.core.NLPersistance;
import com.nerdylotus.game.NLCategoryLoader;

public class HMCCategoryLoader implements NLCategoryLoader{
	
	public static void main(String[] args) {		
		(new HMCCategoryLoader()).loadCategories();
	}
	public void loadCategories() {
		try{
			InputStream in = HMCCategoryLoader.class.getClassLoader().getResourceAsStream("categories.txt");			
			BufferedReader input =  new BufferedReader(new InputStreamReader(in));
	        String line = null; 
	        NLPersistance nlp = NLFactory.getPersistance();
	        while (( line = input.readLine()) != null){	 
	        	JSONObject cnf = new JSONObject();
	        	cnf.put("category", line);
	        	cnf.put("minplayers", 1);
	        	cnf.put("maxplayers", 5);
		        nlp.addValueToKey("HM", cnf.toString());
	        }			
			in.close();
			input.close();
        }catch(Exception e){
        	e.printStackTrace();
        }		
	}

}
