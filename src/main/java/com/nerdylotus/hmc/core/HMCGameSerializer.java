package com.nerdylotus.hmc.core;

import net.sf.ezmorph.bean.BeanMorpher;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JSONUtils;

import com.nerdylotus.core.NLGameSerializer;
import com.nerdylotus.core.NLGameUpdate;
import com.nerdylotus.game.NLGame;
import com.nerdylotus.hmc.game.HMCGame;
import com.nerdylotus.hmc.game.HMCPlayer;

public class HMCGameSerializer extends NLGameSerializer {
	JsonConfig jsonConfig = new JsonConfig();
	public HMCGameSerializer() {
		super();
		// Filter current word here
		jsonConfig.setExcludes( new String[]{ "cards", "cardtimegap", "nextGameIn" } );
	}
	
	@Override
	public NLGame stringToGame(String strGame) {
		BeanMorpher beanMorpher = new BeanMorpher( HMCPlayer.class, JSONUtils.getMorpherRegistry() );
		JSONObject jsonObject = JSONObject.fromObject(strGame);		
		HMCGame game = (HMCGame) JSONObject.toBean(jsonObject, HMCGame.class);
		for(String playername: game.getPlayers().keySet()){
			HMCPlayer plyr = (HMCPlayer)beanMorpher.morph(game.getPlayers().get(playername));
			game.getPlayers().put(playername, plyr);
		}
		return game;
	}

	@Override
	public String gameToString(NLGame game) {
		// TODO Auto-generated method stub
		return JSONObject.fromObject(game).toString();
	}
	public static void main(String[] args){
		HMCGame g = new HMCGame();
		g.getPlayers().put("a", new HMCPlayer("a"));
		g.getPlayers().put("b", new HMCPlayer("b"));
		g.reset();
		g.setup();
		NLGame g1 = new HMCGameSerializer().stringToGame(JSONObject.fromObject(g).toString());
		System.out.println("End");
		
	}

	@Override
	public String updateToString(NLGameUpdate update) {
		// TODO Auto-generated method stub
		return JSONObject.fromObject(update, jsonConfig).toString();
	}

}
