package com.nerdylotus.hmc.core;

import java.util.Date;

import net.sf.json.JSONObject;

import com.nerdylotus.core.NLDelayedAction;
import com.nerdylotus.core.NLFactory;
import com.nerdylotus.core.NLGameMessageProcesser;
import com.nerdylotus.core.NLGameUpdate;
import com.nerdylotus.core.redis.NLRedisPersistanceImpl;
import com.nerdylotus.game.NLGameStatus;
import com.nerdylotus.hmc.game.HMCGame;
import com.nerdylotus.hmc.game.HMCPlayer;

public class HMCMessageProcessor extends NLGameMessageProcesser {
	public HMCMessageProcessor(String gameType) {
		super(gameType);
		this.serializer = new HMCGameSerializer();
	} 
	
	@Override
	public NLGameUpdate performAction(String gameid, String gamescope, String gameconfJSON, String gameJSON, String username, String action) {
		
		HMCGame game = null;
		NLGameUpdate update = null;
		System.out.println("Action: " + action);
		System.out.println("Game: " + gameJSON);
		System.out.println("Username: " + username);
		if(action.startsWith("setup?")){
			JSONObject gameconf = JSONObject.fromObject(gameconfJSON);		
			game = new HMCGame();
			game.setGameid(gameid);
			game.setScope(gamescope);
			game.setCategory(gameconf.getString("wordcategory"));
			game.setBuyin(gameconf.getDouble("buyin"));
			game.setMinPlayers(gameconf.getInt("minplayers"));
			game.setMaxPlayers(gameconf.getInt("maxplayers"));
			String[] arr = action.replace("setup?playerlist=", "").split(",");
			for(String user: arr){
				HMCPlayer player = new HMCPlayer(user);
				Double points = -1.0;
				if(!player.isGuest()){
					
					points = NLFactory.getPersistance().getBalanceForMemberInScope(game.getScope() + "-points", user);
					if(points == null){
						// Read default points off a props file
						points = 10000.0;	
						NLFactory.getPersistance().setBalanceForMemberInScope(game.getScope() + "-points", user, points);
					}					
				}
				player.setBalance(points);
				game.getPlayers().put(user, player);
			}			
			game.setup();
			// Setup would have changed the balances according to the fees. Update to redis
			for(String plyrname: game.getPlayers().keySet()){
				HMCPlayer plyr = (HMCPlayer)game.getPlayers().get(plyrname);
				NLFactory.getPersistance().setBalanceForMemberInScope(game.getScope() + "-points", plyrname, plyr.getBalance());
			}						
			update = new NLGameUpdate("dealt", game);
			if(game.isTimeouts()){
				new NLDelayedAction(gameid, "HM", "kick?username=" + game.getTurn(), game.getTurnTimeout()).start();
			}
		}else{
			if(gameJSON == null) return null;
			game = (HMCGame)serializer.stringToGame(gameJSON);
			if(action.startsWith("play?") && game.getTurn().equals(username)){
				String alphabet = action.replace("play?guessAlpha=", "").toUpperCase();
				if(!game.getAlphabets().contains(alphabet)){
					HMCPlayer tempPlayer = (HMCPlayer)game.getPlayers().get(username);
					game.getAlphabets().add(alphabet);
					System.out.println("Alphabets NOW ---> " + game.getAlphabets());
					if(game.checkWord()){
						System.out.println("Alphabets After checking word ---> " + game.getAlphabets());
						// Hit so retain turn
						if (game.getGuessWord().equals(game.getCurrentWord())) {
							// Game over
							game.setStatus(NLGameStatus.COMPLETE);
							game.setWinner(game.getTurn());
							Double winnings = game.getPot();
							game.setMessage("Winner takes $ " + winnings + ".");
							if(!tempPlayer.isGuest()){
								Double newBalance = NLFactory.getPersistance().incrementBalanceOfMemberInScopeBy(game.getScope() + "-points" , 
										game.getTurn(), winnings);
								tempPlayer.setBalance(newBalance);
							}
							new NLDelayedAction(gameid, "HM", "reset?username=SERVER", game.getNextGameIn()).start();
						}else{
							// Simply reset turn timer back
							game.setTurnChangedAt(new Date().getTime());
							if(game.isTimeouts()){
								new NLDelayedAction(gameid, "HM", "kick?username=" + game.getTurn(), game.getTurnTimeout()).start();
							}
						}
					}else{
//						if(!tempPlayer.isGuest()){
//							Double newBalance = NLFactory.getPersistance().incrementBy(game.getScope() + "-points" , 
//									game.getTurn(), -1 * game.penalty(alphabet));
//							tempPlayer.setBalance(newBalance);						
//							game.setPot(game.getPot() + game.penalty(alphabet));
//						}
						game.rotateTurn();					
					}					
				}
				update = new NLGameUpdate("played", game);			
			}else if(action.startsWith("join?")){								
				if(game.getStatus() != NLGameStatus.DELETED &&
						 game.getPlayers().size() < game.getMaxPlayers()){
					//System.out.println("Join existing game");
					HMCPlayer player = new HMCPlayer(username);
					Double points = -1.0;
					if(!player.isGuest()){
						points = NLFactory.getPersistance().getBalanceForMemberInScope(game.getScope() + "-points", username);
						if(points == null){
							// Read default points off a props file
							points = 10000.0;	
							NLFactory.getPersistance().setBalanceForMemberInScope(game.getScope() + "-points", username, points);
						}					
					}										
					player.setBalance(points);
					game.getPlayers().put(username, player);
					update = new NLGameUpdate("joinedactivegame", game);
				}
			}else if(action.startsWith("kick?")){				
				String kickedUsername = action.replace("kick?username=", "");
				long currtime = new Date().getTime();
				if(game.getStatus() == NLGameStatus.INPROGRESS &&
						game.getTurn().equals(kickedUsername) && 
						game.getTurnChangedAt() >= game.getStartedAt() &&
						currtime - game.getTurnChangedAt()  >= game.getTurnTimeout() * 1000){
					//System.out.println("Valid kick");
					game.rotateTurn();
					game.getPlayers().remove(kickedUsername);
					game.clean();
					if(game.getStatus() == NLGameStatus.DELETED){
						game.setMessage("Game was shutdown because of a user exiting.");
						update = new NLGameUpdate("shutdown", game);
					}else{
						game.setMessage("{" + kickedUsername + "} has exited.");
						update = new NLGameUpdate("kicked", game);
					}															
				}else{
//					System.out.println("This kicks is not valid");					
//					System.out.println("In Prog check: " + (game.getStatus() == NLGameStatus.INPROGRESS));
//					System.out.println("Turn check: " + (game.getTurn().equals(kickedUsername)));
//					System.out.println("Turn and start time check: " + (game.getTurnChangedAt() >= game.getStartedAt()));
//					System.out.println("Turn changed check: " + (currtime - game.getTurnChangedAt()  >= game.getTurnTimeout() * 1000));
//					System.out.println("Difference: " + String.valueOf(currtime - game.getTurnChangedAt()));
				}
			}else if(action.startsWith("reset?")){
				if(game.getStatus() == NLGameStatus.COMPLETE){
//					System.out.println("Resetting game");
					game.reset();					
					update = new NLGameUpdate("dealt", game);
					if(game.isTimeouts()){
						new NLDelayedAction(gameid, "HM", "kick?username=" + game.getTurn(), game.getTurnTimeout()).start();
					}					
				}
			}else if(action.startsWith("exit?")){
				String exitUsername = action.replace("exit?username=", "");				
//				System.out.println("Exiting");
				if(game.getStatus() == NLGameStatus.INPROGRESS &&
						game.getTurn().equals(exitUsername)){
					game.rotateTurn();
				}
				game.getPlayers().remove(exitUsername);	
				game.clean();
				if(game.getStatus() == NLGameStatus.DELETED){
					game.setMessage("Game was shutdown because of a user exiting.");
					update = new NLGameUpdate("shutdown", game);
				}else{
					update = new NLGameUpdate("exited", game);	
				}													
			}			
		}		
		return update;
	}
}
