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
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import de.htwk_leipzig.ridefinder_backend.elasticsearch.ElasticSearchClient;
import de.htwk_leipzig.ridefinder_backend.elasticsearch.IndexUpdater;
import de.htwk_leipzig.ridefinder_backend.model.Ride;

public class BesserMitfahrenDownloader {

	private static final String DOMAIN = "https://www.bessermitfahren.de";

	private static int actualPage = 1;

	public static void downloadRides(final String from, final String to, final String date) {

		try {
			// Browser starten
			final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);

			// auch nicht SSL Verbindungen akzeptieren
			webClient.getOptions().setUseInsecureSSL(true);

			// Startseite laden
			HtmlPage homePage;

			homePage = webClient.getPage(DOMAIN);

			// Form-Elemente laden
			final HtmlTextInput fromInput = (HtmlTextInput) homePage.getElementById("form_searchfrom");
			final HtmlTextInput toInput = (HtmlTextInput) homePage.getElementById("form_searchto");
			final HtmlTextInput dateInput = (HtmlTextInput) homePage.getElementById("form_searchdate");
			final HtmlAnchor submitButton = (HtmlAnchor) homePage.getElementById("searchsubmit");

			// Form-Elemente befuellen
			fromInput.setValueAttribute(from);
			toInput.setValueAttribute(to);
			dateInput.setValueAttribute(date);

			// Form abschicken und Ergebnisseite als Resultat erhalten
			final HtmlPage resultPage = submitButton.click();

			final List<Ride> rides = parseResults(resultPage, from, to);

			webClient.close();

			final ElasticSearchClient client = new ElasticSearchClient();
			client.connect();

			for (final Ride ride : rides) {
				IndexUpdater.write(ride, client.getClient());
			}

			client.close();
		} catch (final FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<Ride> parseResults(final HtmlPage resultPage, final String from, final String to) {
		final List<Ride> rides = new ArrayList<Ride>();

		final List<DomNode> times = (List<DomNode>) resultPage
				.getByXPath("//a/*[contains(concat(' ', @class, ' '), ' time ')]");
		final List<DomNode> prices = (List<DomNode>) resultPage
				.getByXPath("//a/*[contains(concat(' ', @class, ' '), ' price ')]");
		final List<DomNode> seats = (List<DomNode>) resultPage
				.getByXPath("//a/*[contains(concat(' ', @class, ' '), ' people ')]");
		final List<DomNode> dates = (List<DomNode>) resultPage
				.getByXPath("//a/*[contains(concat(' ', @class, ' '), ' date ')]");
		final List<DomNode> links = (List<DomNode>) resultPage
				.getByXPath("//li[contains(concat(' ', @class, ' '), ' clear ')]/a");

		final List<DomNode> pages = (List<DomNode>) resultPage.getByXPath(".//*[@id='pager']/a");

		// lese Ergebnisse und wandele in Fahrten um
		for (int i = 0; i < times.size(); i++) {
			final String time = times.get(i).getTextContent().replace(" Uhr", "");

			final String priceString = prices.get(i).getTextContent().replaceAll("[^0-9,]+", "").replace(",", ".");
			Float price = null;
			if (!priceString.isEmpty()) {
				price = Float.parseFloat(priceString);
			}

			final int seat = Integer.parseInt(seats.get(i).getTextContent());

			final String dateString = dates.get(i).getTextContent();
			String date = dateString.substring(dateString.length() - 8, dateString.length());
			date = date.substring(0, 6) + "20" + date.substring(6, date.length());

			final String link = DOMAIN + ((HtmlAnchor) links.get(i)).getAttribute("href");

			final Ride ride = new Ride(from, to, time, price, seat, date, link, "bessermitfahren");

			rides.add(ride);

			System.out.println(ride);
		}

		// wenn es weitere Ergebnisseiten gibt, rufe und lese diese aus
		if (pages.size() > actualPage) {
			try {
				final HtmlAnchor nextPageLink = (HtmlAnchor) pages.get(actualPage);
				actualPage++;
				rides.addAll(parseResults((HtmlPage) nextPageLink.click(), from, to));
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return rides;

	}
}
