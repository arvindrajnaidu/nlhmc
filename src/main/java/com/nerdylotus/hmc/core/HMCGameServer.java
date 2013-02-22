package com.nerdylotus.hmc.core;


import com.nerdylotus.core.NLGameServer;
import com.nerdylotus.util.HMCCategoryLoader;

public class HMCGameServer extends NLGameServer {
	
	public HMCGameServer() {
		super("HM");
		this.msgProcesser = new HMCMessageProcessor();
		this.catLoader = new HMCCategoryLoader();
	}
}
