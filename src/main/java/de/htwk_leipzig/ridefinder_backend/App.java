package de.htwk_leipzig.ridefinder_backend;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

import de.htwk_leipzig.ridefinder_backend.downloader.BesserMitfahrenDownloader;
import de.htwk_leipzig.ridefinder_backend.downloader.FahrgemeinschaftDownloader;

/**
 * Hello world!
 *
 */
public class App {
	private static final String FROM = "Leipzig";
	private static final String TO = "Dresden";
	private static final String DATE = "24.02.2016";

	public static void main(final String[] args)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException {

		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

		FahrgemeinschaftDownloader.downloadRides(FROM, TO, DATE);

		// BlaBlaCarDownloader.downloadRides("Leipzig", "Dresden",
		// "20.12.2015");

		BesserMitfahrenDownloader.downloadRides(FROM, TO, DATE);
	}
}
