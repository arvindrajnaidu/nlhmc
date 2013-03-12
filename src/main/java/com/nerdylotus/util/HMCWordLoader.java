package com.nerdylotus.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.nerdylotus.core.NLFactory;
import com.nerdylotus.core.NLPersistance;

public class HMCWordLoader {

	/**
	 * @param args
	 * File, HOLLYWOOD
	 * File, ADJECTIVES
	 */
	public static void main(String[] args) {
		try{
			InputStream in = HMCWordLoader.class.getClassLoader().getResourceAsStream("mvnames.txt");
			BufferedReader input =  new BufferedReader(new InputStreamReader(in));
	        String line = null; 
	        NLPersistance nlp = NLFactory.getPersistance();
	        while (( line = input.readLine()) != null){
	        	nlp.addValueToKey("HOLLYWOOD", line);
	        }
        }catch(Exception e){
        	e.printStackTrace();
        }
	}

}
