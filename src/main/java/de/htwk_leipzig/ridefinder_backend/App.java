package de.htwk_leipzig.ridefinder_backend;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

import de.htwk_leipzig.ridefinder_backend.downloader.BesserMitfahrenDownloader;
import de.htwk_leipzig.ridefinder_backend.downloader.FahrgemeinschaftDownloader;

/**
 * Einstiegspunkt des Backends
 *
 * @author Christian
 *
 */
public class App {
	private static final String FROM = "Leipzig";
	private static final String TO = "Dresden";
	private static final String DATE = "24.02.2016";

	/**
	 * Einstiegspunkt
	 *
	 * @param args
	 * @throws FailingHttpStatusCodeException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(final String[] args)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException {

		// Log fuer HTMLUnit deaktivieren
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

		FahrgemeinschaftDownloader.downloadRides(FROM, TO, DATE);

		// BlaBlaCarDownloader.downloadRides("Leipzig", "Dresden",
		// "20.12.2015");

		BesserMitfahrenDownloader.downloadRides(FROM, TO, DATE);
	}
}
