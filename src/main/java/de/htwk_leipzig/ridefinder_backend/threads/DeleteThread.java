package de.htwk_leipzig.ridefinder_backend.threads;

import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import de.htwk_leipzig.ridefinder_backend.elasticsearch.Delete;

/**
 * loescht alle gestrigen Mitfahrgelegenheiten
 *
 * @author Christian
 */
public class DeleteThread extends Thread {

	/**
	 * Datumsformat fuer das DateInputField
	 */
	private final static java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("dd.MM.yyyy");

	/**
	 * wie lange der Thread zwischen den Abarbeitungen pausieren soll
	 */
	private static int hoursToSleep = 24;

	/**
	 * Konstruktor
	 */
	public DeleteThread() {
		super();
	}

	@Override
	public void run() {
		while (true) {
			deleteRidesForYesterday();
			System.out.println("Mitfahrgelegenheiten von gestern gelöscht");
			// 24 Stunden schlafen
			try {
				TimeUnit.HOURS.sleep(hoursToSleep);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * loescht alle Mitfahrgelegenheiten vom gestrigen Tag, damit der Indez
	 * nicht stetig waechst
	 */
	private void deleteRidesForYesterday() {
		final Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DATE, -1);
		try {
			Delete.deleteWithOutClient(dateFormat.format(yesterday.getTime()));
		} catch (final UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
