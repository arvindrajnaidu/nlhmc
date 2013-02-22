package com.nerdylotus.hmc.game;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nerdylotus.core.NLDelayedAction;
import com.nerdylotus.game.NLGameStatus;
import com.nerdylotus.game.NLPlayer;
import com.nerdylotus.game.NLTurnBasedGame;
import com.nerdylotus.util.NameFactory;

public class HMCGame extends NLTurnBasedGame {
	
	Double pot = 0.0;
	String currentWord = "";		
	String guessWord = "";
	String dealer = "";
	String category = "";
	List<String> alphabets = new ArrayList<String>(); 
	
	public HMCGame() {
		super();
		timeouts = true;
		scope = "global";
		category = "HOLLYWOOD";
	}
	public Double getPot() {
		return pot;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public void setPot(Double pot) {
		this.pot = pot;
	}

	public String getCurrentWord() {
		return currentWord;
	}

	public void setCurrentWord(String currentWord) {
		this.currentWord = currentWord;
	}

	public String getGuessWord() {
		return guessWord;
	}

	public void setGuessWord(String guessWord) {
		this.guessWord = guessWord;
	}

	public String getDealer() {
		return dealer;
	}

	public void setDealer(String dealer) {
		this.dealer = dealer;
	}

	public List<String> getAlphabets() {
		return alphabets;
	}

	public void setAlphabets(List<String> alphabets) {
		this.alphabets = alphabets;
	}
	
	public Double bonus(){
		return 0.0;
//		System.out.println("Alphabets size: " + alphabets.size());
//		if(alphabets.size() == 0) return 0.0;
//		int hits = 0;
//		for(String alpha: alphabets){
//			if(currentWord.indexOf(alpha) > -1){
//				hits++;
//			}
//		}
////		System.out.println("hits: " + hits);
////		System.out.println((hits * 100.0/alphabets.size()) );
//		
//		double successRate = hits * 100.0/alphabets.size();
////		System.out.println(successRate);
//		// MAX BONUS 200 Below
//		BigDecimal bd = new BigDecimal(200 * (successRate/100.0)).setScale(0, RoundingMode.HALF_EVEN);
//		if(hits == alphabets.size()) return bd.doubleValue() + 100.0;
//		return bd.doubleValue();
	}

	@Override
	public void reset() {
		this.eventNo = -1;
		this.currentWord = NameFactory.getRandomName(category);	
		this.alphabets.clear();
		this.status = NLGameStatus.INPROGRESS;
		this.winner = "";		
		this.pot = 0.0;
		this.message = "New game. Lets play!";
		this.checkWord();
		boolean turnSet = false;	
		for(String username: this.players.keySet()){
			HMCPlayer plyr = (HMCPlayer)this.players.get(username);
			if(!turnSet && 
					!plyr.getUsername().equals(dealer) ){
				this.turn = plyr.getUsername();
				this.turnChangedAt = new Date().getTime();
				this.dealer = turn;
				turnSet = true;				
			}else if(this.players.size() == 1){
				this.turn = plyr.getUsername();
				this.turnChangedAt = new Date().getTime();
				this.dealer = turn;
				turnSet = true;								
			}
			plyr.setRound(0);
			
		}		
		this.startedAt = new Date().getTime();
		// Fees
		double fee = 0.0;
		double pot = 0.0;
		try{
			fee = this.getBuyin();
		}catch(Exception e){}
		for(String plyrname: this.getPlayers().keySet()){
			HMCPlayer plyr = (HMCPlayer)this.getPlayers().get(plyrname);
			plyr.setBalance(plyr.getBalance() - fee);		
			pot = pot + fee;
		}	
		this.setPot(pot);				
	}
	@Override
	public  void teardown(){
		this.currentWord = "";	
		this.alphabets.clear();
		this.status = NLGameStatus.DELETED;
		this.winner = "";		
		this.pot = 0.0;
		this.message = "";
		this.players.clear();		
	}
	@Override
	public void clean() {
		// Updates game based in the integrity of the data in it.
		// Check if enough exist to play the game
		if(players.size() < minPlayers){
			this.teardown();
		}
	}

	@Override
	public void rotateTurn() {
		// Maybe we can kill old threads to delayed actions here
		// Come whatever the turn did change
		turnChangedAt = new Date().getTime();
		if (players.size() > 1) {
			if(turn == null || turn.equals("")){
				turn = players.keySet().iterator().next();
			}else{
	        	NLPlayer nextPlayer = getNextPlayerFrom(turn);
	            // Is this the same player again? if yes all have left
	            if (nextPlayer == null) {
	                return;
	            }
	            turn = nextPlayer.getUsername();	            
			}
			// Turn has been set ... set timeout
			if(this.isTimeouts()){
				NLDelayedAction da = new NLDelayedAction(gameid, "HM", "kick?username=" + turn, turnTimeout);
				da.start();
			}
        }		
	}

	@Override
	public void turnExpired() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void killTurnWatch() {
		// TODO Auto-generated method stub
		
	}
	@Override	
	public void setup(){
		this.setNextGameIn(7);
		this.reset();						
		if(this.turn.equals("")){
			// Give the turn back to dealer
			this.turn = this.dealer;			
		}
	}	
	public boolean checkWord(){		
		   String tmpGuessWrd = this.guessWord;
		   StringBuilder sb = new StringBuilder(this.currentWord.length());
		   for (int i =0; i<this.currentWord.length(); i++){
			   if(this.currentWord.substring(i, i+1).equals(" ")){
				   sb.append(" ");
			   }else{
				   sb.append("_");
			   }	   	 		
		   }
		   for (int i=0; i<this.alphabets.size(); i++){
			   String alphabet = this.alphabets.get(i);
			   Pattern p = Pattern.compile(alphabet);
		       Matcher m = p.matcher(this.currentWord); // get a matcher object
		       while(m.find()) {
		           sb.replace(m.start(), m.end(), alphabet);
		       }
		       alphabet = null;
		   }
		   this.guessWord = sb.toString();
		   
		   // If true then hit on the newly added alphabet
	       return !tmpGuessWrd.equals(guessWord);
	}
	public Double penalty(String alphabet){
		// Returns how much the user should be penalized for this wrong guess
		if(alphabet.equals("E")){
			return 200.0;
		}else if(alphabet.equals("T") || alphabet.equals("A")|| alphabet.equals("O")|| alphabet.equals("I")){
			return 150.0;
		}else if(alphabet.equals("N") || alphabet.equals("S")|| alphabet.equals("H")|| alphabet.equals("R")){
			return 100.0;
		}else if(alphabet.equals("D") || alphabet.equals("I")|| alphabet.equals("C")|| alphabet.equals("U")
				|| alphabet.equals("M")|| alphabet.equals("W")|| alphabet.equals("H")|| alphabet.equals("G")){
			return 50.0;
		}else if(alphabet.equals("Y") || alphabet.equals("P")|| alphabet.equals("B")|| alphabet.equals("V")
				|| alphabet.equals("K")|| alphabet.equals("J")){
			return 20.0;
		}
		return 5.0;
	}

}
