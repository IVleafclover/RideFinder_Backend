package de.htwk_leipzig.ridefinder_backend.downloader;

/**
 * Interface fuer die Downloader
 *
 * @author Christian
 *
 */
public interface DownloaderInterface {

	/**
	 * greift auf Seite zu, laedt Mitfahrgelegenheiten von dieser herunter und
	 * sendet diese an ElasticSearch weiter
	 *
	 * @param from
	 * @param to
	 * @param date
	 */
	public void downloadRides(final String from, final String to, final String date);

}
