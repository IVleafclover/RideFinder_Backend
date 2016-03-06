package de.htwk_leipzig.ridefinder_backend.elasticsearch;

import java.net.UnknownHostException;
import java.util.List;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

/**
 * ermoeglicht das Loeschen von Mitfahrgelegenheiten
 *
 * @author Christian
 *
 */
public class Delete {

	/**
	 * loescht die Suchergebnisse
	 *
	 * @param client
	 * @param hits
	 */
	@SuppressWarnings("unused")
	public static void delete(final Client client, final List<SearchHit> hits) {
		for (final SearchHit hit : hits) {
			final DeleteResponse response = client.prepareDelete("rides", "ride", hit.getId()).execute().actionGet();
		}
	}

	/**
	 * loescht Mitfahrgelegenheiten an bestimmten Datum
	 *
	 * @param client
	 * @param date
	 * @throws UnknownHostException
	 */
	public static void deleteByDate(final Client client, final String date) throws UnknownHostException {
		delete(client, Search.searchByDate(client, date));
	}

	/**
	 * erstellt einen Client, verbindet diesen und loescht dann, anschliessend
	 * wird der Client wieder geschlossen
	 *
	 * @param date
	 * @throws UnknownHostException
	 */
	public static void deleteWithOutClient(final String date) throws UnknownHostException {
		final ElasticSearchClient client = new ElasticSearchClient();
		client.connect();

		deleteByDate(client.getClient(), date);

		client.close();
	}
}
