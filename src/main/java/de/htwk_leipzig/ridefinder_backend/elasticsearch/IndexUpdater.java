package de.htwk_leipzig.ridefinder_backend.elasticsearch;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;

import de.htwk_leipzig.ridefinder_backend.model.Ride;

/**
 * ermoeglicht Schreibzugriffe auf den ElasticSearch-Index
 *
 * @author Christian
 *
 */
public class IndexUpdater {

	/**
	 * fuegt den ElasticSearch-Index eine Mitfahrgelegenheit hinzu
	 *
	 * @param ride
	 * @param client
	 */
	public static void write(final Ride ride, final Client client) {
		final Map<String, Object> json = new HashMap<String, Object>();
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

		@SuppressWarnings("unused")
		final IndexResponse response = client.prepareIndex("rides", "ride", ride.getLink()).setSource(json).get();
	}
}
