package de.htwk_leipzig.ridefinder_backend.downloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;

import de.htwk_leipzig.ridefinder_backend.elasticsearch.ElasticSearchClient;
import de.htwk_leipzig.ridefinder_backend.elasticsearch.IndexUpdater;
import de.htwk_leipzig.ridefinder_backend.model.Ride;

/**
 * Downloader fuer Fahrgemeinschaft
 *
 * @author Christian
 *
 */
public class FahrgemeinschaftDownloader implements DownloaderInterface {

	/**
	 * Singleton Instanz
	 */
	private static FahrgemeinschaftDownloader instance;

	/**
	 * Domain zu Fahrgemeinschaft
	 */
	private final String DOMAIN = "https://www.fahrgemeinschaft.de";

	/**
	 * gibt die Singleton Instanz wieder
	 *
	 * @return Singleton
	 */
	public static FahrgemeinschaftDownloader getInstance() {
		if (FahrgemeinschaftDownloader.instance == null) {
			FahrgemeinschaftDownloader.instance = new FahrgemeinschaftDownloader();
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
			// Browser starten
			final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);

			// Startseite laden
			HtmlPage homePage;

			homePage = webClient.getPage(DOMAIN);

			// Form-Elemente laden
			final HtmlTextInput fromInput = (HtmlTextInput) homePage.getElementById("edtOrigin");
			final HtmlTextInput toInput = (HtmlTextInput) homePage.getElementById("edtDestination");
			final HtmlTextInput dateInput = homePage.getElementByName("selDate");
			final HtmlSubmitInput submitButton = homePage.getElementByName("btnSend");

			// Form-Elemente befuellen
			fromInput.setValueAttribute(from);
			fromInput.fireEvent(Event.TYPE_BLUR);
			toInput.setValueAttribute(to);
			toInput.fireEvent(Event.TYPE_BLUR);
			dateInput.setValueAttribute(date);
			// dateInput.fireEvent(Event.TYPE_FOCUS_OUT);

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
	 * liest Suchergebnisse anhand aus
	 *
	 * @param resultPage
	 * @param from
	 * @param to
	 * @param date
	 * @return Liste von ausgelesenen Mitfahrgelegenhieten
	 */
	@SuppressWarnings("unchecked")
	private List<Ride> parseResults(final HtmlPage resultPage, final String from, final String to, final String date) {
		final List<Ride> rides = new ArrayList<Ride>();

		final List<DomNode> times = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' time ')]");
		final List<DomNode> prices = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' costs ')]");
		final List<DomNode> seats = (List<DomNode>) resultPage.getByXPath(
				"//*[contains(concat(' ', @class, ' '), ' ion-person-stalker ') and contains(concat(' ', @class, ' '), ' icon ')]/span");
		final List<DomNode> links = (List<DomNode>) resultPage
				.getByXPath("//a[contains(concat(' ', @class, ' '), ' clearfixafter ')]");

		for (int i = 0; i < times.size(); i++) {
			final String time = times.get(i).getTextContent().replace(" Uhr", "");

			final int seat = Integer.parseInt(seats.get(i).getTextContent());

			final String priceString = prices.get(i).getTextContent().replaceAll("[^0-9,]+", "").replace(",", ".");
			Float price = null;
			if (!priceString.isEmpty()) {
				price = Float.parseFloat(priceString);
			}

			final String link = DOMAIN + ((HtmlAnchor) links.get(i)).getAttribute("href");

			final Ride ride = new Ride(from, to, time, price, seat, date, link, "fahrgemeinschaft");

			rides.add(ride);

			System.out.println(ride);
		}

		return rides;
	}
}
