package com.orangomango.tetris;

import java.util.*;

import dev.webfx.platform.json.Json;
import dev.webfx.platform.json.JsonObject;
import dev.webfx.platform.json.ReadOnlyJsonObject;
import dev.webfx.platform.fetch.Fetch;
import dev.webfx.platform.fetch.Response;

public class Leaderboard{
	private String location;
	private JsonObject json;

	public Leaderboard(String loc){
		this.location = loc;
	}

	public void load(Runnable onSuccess){
		Fetch.fetch(this.location+"?mode=load").compose(Response::text).onSuccess(text -> {
			JsonObject json = Json.parseObjectSilently(text.split("\n")[5].split(" </body>")[0]);
			this.json = json;
			if (onSuccess != null) onSuccess.run();
		});
	}

	public void addEntry(String user, int value, Runnable onSuccess){
		Fetch.fetch(this.location+"?mode=save&user="+user+"&value="+value).onSuccess(response -> load(onSuccess));
	}

	public List<Map.Entry<String, Integer>> getEntries(){
		if (this.json == null){
			return null;
		}
		Map<String, Integer> lead = new HashMap<>();
		for (int i = 0; i < this.json.getArray("data").size(); i++){
			ReadOnlyJsonObject j = this.json.getArray("data").getObject(i);
			lead.put(j.getString("user"), j.getInteger("value"));
		}
		List<Map.Entry<String, Integer>> output = new ArrayList<>(lead.entrySet());
		output.sort((e1, e2) -> -Integer.compare(e1.getValue(), e2.getValue()));
		return output;
	}
}