package de.htwk_leipzig.ridefinder_backend.downloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import de.htwk_leipzig.ridefinder_backend.elasticsearch.ElasticSearchClient;
import de.htwk_leipzig.ridefinder_backend.elasticsearch.IndexUpdater;
import de.htwk_leipzig.ridefinder_backend.model.Ride;

/**
 * Downloader fuer Bla Bla Car
 *
 * @author Christian
 *
 */
public class BlaBlaCarDownloader implements DownloaderInterface {

	/**
	 * Singleton Instanz
	 */
	private static BlaBlaCarDownloader instance;

	/**
	 * Domain zu Fahrgemeinschaft
	 */
	private final String DOMAIN = "https://www.blablacar.de";

	/**
	 * Uhrzeit der letzten Mitfahrgelegenheit, wird benoetigt um festzustellen,
	 * ob es sich um den naechsten Tag handelt
	 */
	private String lastTime;

	/**
	 * zum Auslesen und Konvertieren der aktuellen Zeit in einen String
	 */
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

	/**
	 * zum Auslesen und Konvertieren des aktuellen Datums in einen String
	 */
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

	/**
	 * wie viele Tage heruntergeladen werden sollen
	 */
	private final int numberOfDaysToDownload = 1;

	/**
	 * der Nummer des aktuellen Tags beim Auslesen
	 */
	private int actualDay;

	/**
	 * wie lange auf JavaScript gewartet wird
	 */
	private final int timeToWaitForJavaScript = 10000;

	/**
	 * gibt die Singleton Instanz wieder
	 *
	 * @return Singleton
	 */
	public static BlaBlaCarDownloader getInstance() {
		if (BlaBlaCarDownloader.instance == null) {
			BlaBlaCarDownloader.instance = new BlaBlaCarDownloader();
		}
		return instance;
	}

	/**
	 * greift auf Seite zu, laedt Mitfahrgelegenheiten von dieser herunter und
	 * sendet diese an ElasticSearch weiter
	 *
	 * @param from
	 * @param to
	 * @param date
	 */
	public void downloadRides(final String from, final String to, final String date) {

		try {
			if (isSearchingForToday(date)) {
				// aktuelle Uhrzeit fuer Auslesen der Suchergebnisse setzen
				final Calendar cal = Calendar.getInstance();
				lastTime = timeFormat.format(cal.getTime());
			} else {
				// wenn nicht fuer heute egsucht, ist letzte Uhrzeit
				// 00:00
				lastTime = "00:00";
			}

			// den aktuellen Tag auf den ersten Tag setzen
			actualDay = 0;

			// Browser starten
			final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);

			// Startseite laden
			HtmlPage homePage;

			homePage = webClient.getPage(DOMAIN);

			// Form-Elemente laden
			final HtmlTextInput fromInput = (HtmlTextInput) homePage.getElementById("search_from_name");
			final HtmlTextInput toInput = (HtmlTextInput) homePage.getElementById("search_to_name");
			final HtmlTextInput dateInput = (HtmlTextInput) homePage.getElementById("search_to_date");
			final HtmlButton submitButton = (HtmlButton) homePage
					.getByXPath("//*[contains(concat(' ', @class, ' '), ' btn-2action ')]").get(0);

			// Form-Elemente befuellen
			fromInput.setValueAttribute(from);
			toInput.setValueAttribute(to);
			dateInput.setValueAttribute(date);

			// Form abschicken und Ergebnisseite als Resultat erhalten
			final HtmlPage resultPage = submitButton.click();

			final List<Ride> rides = parseResults(resultPage, from, to, date);

			webClient.close();

			final ElasticSearchClient client = new ElasticSearchClient();
			client.connect();

			for (final Ride ride : rides) {
				IndexUpdater.write(ride, client.getClient());
			}

			client.close();
		} catch (final FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * wird fuer heute gesucht
	 *
	 * @param date
	 * @return wird fuer heute gesucht
	 */
	private boolean isSearchingForToday(final String date) {
		final Calendar cal = Calendar.getInstance();
		final String today = dateFormat.format(cal.getTime());
		return today.equals(date);
	}

	/**
	 * liest Suchergebnisse anhand aus
	 *
	 * @param resultPage
	 * @param from
	 * @param to
	 * @param date
	 * @return Liste von ausgelesenen Mitfahrgelegenheiten
	 */
	@SuppressWarnings("unchecked")
	private List<Ride> parseResults(final HtmlPage resultPage, final String from, final String to, final String date) {
		// warten bis JavaScript geladen wird
		try {
			Thread.sleep(timeToWaitForJavaScript);
		} catch (final InterruptedException e1) {
			e1.printStackTrace();
		}

		final List<Ride> rides = new ArrayList<Ride>();

		final List<DomNode> times = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' time ')]");
		final List<DomNode> prices = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' price ')]/strong/span");
		final List<DomNode> seats = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' availability ')]/strong");
		final List<DomNode> links = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' trip-search-oneresult ')]");

		final List<DomNode> nextPage = (List<DomNode>) resultPage.getByXPath(
				"//*[contains(concat(' ', @class, ' '), ' next ') and not(contains(concat(' ', @class, ' '), ' disabled '))]/a");

		// lese Ergebnisse und wandele in Fahrten um
		for (int i = 0; i < times.size(); i++) {
			String time = times.get(i).getTextContent();
			if (time.split("-").length > 1) {
				time = time.split("-")[1];
				time = time.substring(1);
				time = time.replace(" uhr", "");
				time = time.replace(" Uhr", "");
			} else {
				time = time.split("~")[1];
			}

			if (isNewDate(time)) {
				actualDay++;
				if (isNumberOfDaysReach()) {
					return rides;
				}
			}
			instance.lastTime = time;

			final float price = Float.parseFloat(prices.get(i).getTextContent().replaceAll("[^0-9]+", ""));

			final String seatString = seats.get(i).getTextContent();
			int seat = 0;
			if (seatString.length() < 2) {
				seat = Integer.parseInt(seatString);
			}

			final String link = DOMAIN + ((HtmlAnchor) links.get(i)).getAttribute("href");

			final Calendar dateCalendar = Calendar.getInstance();
			try {
				dateCalendar.setTime(dateFormat.parse(date));
			} catch (final ParseException e) {
				e.printStackTrace();
			}
			dateCalendar.add(Calendar.DATE, actualDay);
			final String dateForRide = dateFormat.format(dateCalendar.getTime());

			final Ride ride = new Ride(from, to, time, price, seat, dateForRide, link, "blablacar");

			rides.add(ride);

			System.out.println(ride);
		}

		// wenn es weitere Ergebnisseiten gibt, rufe und lese diese aus
		if (!nextPage.isEmpty()) {
			try {
				final HtmlAnchor nextPageLink = (HtmlAnchor) nextPage.get(0);
				rides.addAll(parseResults((HtmlPage) nextPageLink.click(), from, to, date));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		return rides;
	}

	/**
	 * prueft ob die Uhrzeit der neuen Mitfahrgelegenheit kleiner (vorher) als
	 * die der letzten ist, dadurch wird festgestellt, ob es sich um ein neues
	 * Datum handelt
	 *
	 * @param time
	 * @return ist neue Zeit vorher
	 */
	private boolean isNewDate(final String time) {
		return time.compareTo(instance.lastTime) < 0;
	}

	/**
	 * @return ist die gewuenschte Anzahl an Tagen ausgelesen
	 */
	private boolean isNumberOfDaysReach() {
		return actualDay >= numberOfDaysToDownload;
	}
}
