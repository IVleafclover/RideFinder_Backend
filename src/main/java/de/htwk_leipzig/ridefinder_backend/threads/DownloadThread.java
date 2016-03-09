package de.htwk_leipzig.ridefinder_backend.threads;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
	 * ob im Downloader fuer jeden Tag ein eigener Seitenaufruf notwendig ist
	 */
	private boolean requestForEachDayNecessary;

	/**
	 * wie viele Tage heruntergeladen werden sollen
	 */
	private final static int numberOfDaysToDownload = 7;

	/**
	 * Sekunden, wie lange Thread warten soll, um nicht geblacklisted zu werden
	 */
	private static final int secondsToSleep = 60;

	/**
	 * Konstruktor
	 *
	 * @param downloader
	 * @param requestForEachDayNecessary
	 */
	public DownloadThread(final DownloaderInterface downloader, final boolean requestForEachDayNecessary) {
		super();
		this.setDownloader(downloader);
		this.setRequestForEachDayNecessary(requestForEachDayNecessary);
	}

	@Override
	public void run() {
		if (!isRequestForEachDayNecessary()) {
			// Downloader liefert mit einer Anfrage Ergebnisse zu mehreren
			// Datums
			final String date = dateFormat.format(new Date());

			for (final String from : destinations) {
				for (final String to : destinations) {
					if (!from.equals(to)) {
						downloader.downloadRides(from, to, date);
						// Thread schlafen schicken, um nicht geblacklisted zu
						// werden
						try {
							TimeUnit.SECONDS.sleep(secondsToSleep);
						} catch (final InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} else {
			// im Downloader muss jedes Daum mit einer eigenen Anfrage
			// ausgelesen werden
			final Calendar today = Calendar.getInstance();

			for (int i = 0; i < numberOfDaysToDownload; i++) {
				final String date = dateFormat.format(today.getTime());

				for (final String from : destinations) {
					for (final String to : destinations) {
						if (!from.equals(to)) {
							downloader.downloadRides(from, to, date);
							// Thread schlafen schicken, um nicht geblacklisted
							// zu werden
							try {
								TimeUnit.SECONDS.sleep(secondsToSleep);
							} catch (final InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
				today.add(Calendar.DATE, 1);
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

	/**
	 * @return fuer jeden Tag eigener Seitenaufruf notwendig
	 */
	public boolean isRequestForEachDayNecessary() {
		return requestForEachDayNecessary;
	}

	/**
	 * @param requestForEachDayNecessary
	 */
	public void setRequestForEachDayNecessary(final boolean requestForEachDayNecessary) {
		this.requestForEachDayNecessary = requestForEachDayNecessary;
	}

}
