package com.nerdylotus.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.nerdylotus.core.NLPersistance;
import com.nerdylotus.core.NLRedisPersistanceImpl;

public class HMCWordLoader {

	/**
	 * @param args
	 * File, HOLLYWOOD
	 * File, ADJECTIVES
	 */
	public static void main(String[] args) {
		try{
			InputStream in = new FileInputStream(new File(args[0]));
			BufferedReader input =  new BufferedReader(new InputStreamReader(in));
	        String line = null; 
	        NLPersistance nlp = NLRedisPersistanceImpl.getInstance();
	        while (( line = input.readLine()) != null){	 
	        	nlp.addValueToKey(args[1], line);
	        }
        }catch(Exception e){
        	e.printStackTrace();
        }
	}

}
