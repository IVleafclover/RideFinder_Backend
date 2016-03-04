package de.htwk_leipzig.ridefinder_backend.downloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class BlaBlaCarDownloader {

	public static void downloadRides(final String from, final String to, final String date) {

		try {
			// Browser starten
			final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);

			// Startseite laden
			HtmlPage homePage;

			homePage = webClient.getPage("https://www.blablacar.de/");

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

			parseResults(resultPage, from, to, date);

			webClient.close();
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

	private static void parseResults(final HtmlPage resultPage, final String from, final String to, final String date) {
		// warten bis JavaScript geladen wird
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		final List<DomNode> times = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' time ')]");
		final List<DomNode> prices = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' price ')]/strong/span");
		final List<DomNode> seats = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' availability ')]/strong");

		final List<DomNode> nextPage = (List<DomNode>) resultPage
				.getByXPath("//*[contains(concat(' ', @class, ' '), ' next ')]/a");

		// lese Ergebnisse und wandele in Fahrten um
		for (int i = 0; i < times.size(); i++) {
			String time = times.get(i).getTextContent();
			time = time.split("-")[1];
			time = time.substring(1);

			final float price = Float.parseFloat(prices.get(i).getTextContent().replaceAll("[^0-9]+", ""));

			final String seatString = seats.get(i).getTextContent();
			int seat = 0;
			if (seatString.length() < 2) {
				seat = Integer.parseInt(seatString);
			}

			// TODO fix bla bla car downloader
			// Ride ride = new Ride(from, to, time, price, seat, date);
			// System.out.println(ride);

		}

		// wenn es weitere Ergebnisseiten gibt, rufe und lese diese aus
		if (!nextPage.isEmpty()) {
			try {
				final HtmlAnchor nextPageLink = (HtmlAnchor) nextPage.get(0);
				parseResults((HtmlPage) nextPageLink.click(), from, to, date);
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
