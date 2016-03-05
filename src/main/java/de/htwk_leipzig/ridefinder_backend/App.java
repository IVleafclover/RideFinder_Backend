package de.htwk_leipzig.ridefinder_backend;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

import de.htwk_leipzig.ridefinder_backend.downloader.BlaBlaCarDownloader;
import de.htwk_leipzig.ridefinder_backend.threads.DownloadThread;

/**
 * Einstiegspunkt des Backends
 *
 * @author Christian
 *
 */
public class App {

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

		// new DownloadThread(FahrgemeinschaftDownloader.getInstance()).start();
		new DownloadThread(BlaBlaCarDownloader.getInstance()).start();
		// new DownloadThread(BesserMitfahrenDownloader.getInstance()).start();
	}
}
