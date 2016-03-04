package de.htwk_leipzig.ridefinder_backend.elasticsearch;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

import de.htwk_leipzig.ridefinder_backend.model.Ride;

public class IndexUpdater {

	public static void write(Ride ride, Client client) {
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("from", ride.getFrom());
		json.put("to", ride.getTo());
		json.put("date", ride.getDate());
		json.put("time", ride.getTime());
		if (ride.getPrice() == null) {
			json.put("price", "null");
		} else {
			json.put("price", Float.toString(ride.getPrice()));
		}
		json.put("seat", Integer.toString(ride.getSeat()));
		json.put("provider", ride.getProvider());

		IndexResponse response = client
				.prepareIndex("rides", "ride", ride.getLink()).setSource(json)
				.get();
	}

	public static void update(Ride ride) {

	}

}
