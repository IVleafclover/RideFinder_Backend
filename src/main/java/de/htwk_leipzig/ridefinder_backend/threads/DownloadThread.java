package de.htwk_leipzig.ridefinder_backend.threads;

import java.util.Date;

import de.htwk_leipzig.ridefinder_backend.downloader.DownloaderInterface;

/**
 * Thread, welcher Download ausfuehrt
 *
 * @author Christian
 *
 */
public class DownloadThread extends Thread {

	/**
	 * Datumsformat fuer das DateInputField
	 */
	private final static java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy");;

	/**
	 * die unterstuetzten Orte
	 */
	private final static String[] destinations = { "Leipzig", "Dresden" };

	/**
	 * Downloader, welcher ausgefuehrt werden soll
	 */
	private DownloaderInterface downloader;

	/**
	 * Konstruktor
	 *
	 * @param downloader
	 */
	public DownloadThread(final DownloaderInterface downloader) {
		super();
		this.setDownloader(downloader);
	}

	@Override
	public void run() {
		final String date = dateFormat.format(new Date());

		for (final String from : destinations) {
			for (final String to : destinations) {
				if (!from.equals(to)) {
					downloader.downloadRides(from, to, date);
				}
			}
		}
	}

	/**
	 * @return Downloader
	 */
	public DownloaderInterface getDownloader() {
		return downloader;
	}

	/**
	 * @param downloader
	 */
	public void setDownloader(final DownloaderInterface downloader) {
		this.downloader = downloader;
	}

}
