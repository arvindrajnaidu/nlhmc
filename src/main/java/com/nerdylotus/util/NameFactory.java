package com.nerdylotus.util;

import com.nerdylotus.core.NLFactory;

public class NameFactory {
	
	public static String getRandomName(String category){		
		return NLFactory.getPersistance().getRandomString(category);
	}
}
