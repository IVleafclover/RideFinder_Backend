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

public class FahrgemeinschaftDownloader {

	private static String DOMAIN = "https://www.fahrgemeinschaft.de";

	public static void downloadRides(String from, String to, String date) {

		try {
			// Browser starten
			final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);

			// Startseite laden
			HtmlPage homePage;

			homePage = webClient.getPage(DOMAIN);

			// Form-Elemente laden
			final HtmlTextInput fromInput = (HtmlTextInput) homePage
					.getElementById("edtOrigin");
			final HtmlTextInput toInput = (HtmlTextInput) homePage
					.getElementById("edtDestination");
			final HtmlTextInput dateInput = homePage
					.getElementByName("selDate");
			final HtmlSubmitInput submitButton = homePage
					.getElementByName("btnSend");

			// Form-Elemente befuellen
			fromInput.setValueAttribute(from);
			fromInput.fireEvent(Event.TYPE_BLUR);
			toInput.setValueAttribute(to);
			toInput.fireEvent(Event.TYPE_BLUR);
			dateInput.setValueAttribute(date);
			// dateInput.fireEvent(Event.TYPE_FOCUS_OUT);

			// Form abschicken und Ergebnisseite als Resultat erhalten
			final HtmlPage resultPage = submitButton.click();

			List<Ride> rides = parseResults(resultPage, from, to, date);

			webClient.close();

			ElasticSearchClient client = new ElasticSearchClient();
			client.connect();

			for (Ride ride : rides) {
				IndexUpdater.write(ride, client.getClient());
			}

			client.close();
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<Ride> parseResults(HtmlPage resultPage, String from,
			String to, String date) {
		List<Ride> rides = new ArrayList<Ride>();

		List<DomNode> times = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' time ')]");
		List<DomNode> prices = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' costs ')]");
		List<DomNode> seats = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' ion-person-stalker ') and contains(concat(' ', @class, ' '), ' icon ')]/span");
		List<DomNode> links = (List<DomNode>) resultPage
				.getByXPath("//a[contains(concat(' ', @class, ' '), ' clearfixafter ')]");

		for (int i = 0; i < times.size(); i++) {
			String time = times.get(i).getTextContent().replace(" Uhr", "");

			int seat = Integer.parseInt(seats.get(i).getTextContent());

			String priceString = prices.get(i).getTextContent()
					.replaceAll("[^0-9,]+", "").replace(",", ".");
			Float price = null;
			if (!priceString.isEmpty()) {
				price = Float.parseFloat(priceString);
			}

			String link = DOMAIN
					+ ((HtmlAnchor) links.get(i)).getAttribute("href");

			Ride ride = new Ride(from, to, time, price, seat, date, link,
					"fahrgemeinschaft");

			rides.add(ride);

			System.out.println(ride);
		}

		return rides;
	}
}
