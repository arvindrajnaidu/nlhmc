package com.nerdylotus.hmc.game;

import com.nerdylotus.game.NLPlayer;

public class HMCPlayer extends NLPlayer {
	int round;	
	Double balance;
	
	public HMCPlayer(){
		super();
	}
	public HMCPlayer(String username) {
		super(username);
		if(username.startsWith("guest-")){
			this.guest = true;
		}
	}
	public int getRound() {
		return round;
	}
	public void setRound(int round) {
		this.round = round;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}
	

}
