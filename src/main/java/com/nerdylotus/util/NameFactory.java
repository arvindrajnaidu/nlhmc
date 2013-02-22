package com.nerdylotus.util;

import com.nerdylotus.core.NLRedisPersistanceImpl;

public class NameFactory {
	
	public static String getRandomName(String category){		
		return NLRedisPersistanceImpl.getInstance().getRandomValueInKey(category);
	}
}
