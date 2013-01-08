package uk.bl.monitrix;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import play.Logger;

import com.google.common.net.InternetDomainName;

/**
 * Wraps a single Heritrix log line.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class CrawlLogEntry {
	
	private static DateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	private String line;
	
	private List<String> fields = new ArrayList<String>();
	
	private String bufferedHost = null;
	
	public CrawlLogEntry(String line) {
		this.line = line;
		for (String field : line.split(" ")) {
			if (!field.isEmpty())
				fields.add(field.trim());
		}
	}
	
	/**
	 * Returns the alerts that were generated while parsing the log entry, if any.
	 * @return the list of alerts
	 */
	public List<Alert> getValidationAlerts() {
		// TODO implement
		return new ArrayList<Alert>();
	}
	
	/**
	 * Column #1 - ISO timestamp
	 * @return the crawl time
	 */
	public Date getTimestamp() {
		try {
			return ISO_FORMAT.parse(fields.get(0));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Column #2 - the HTTP status or Heritrix error code
	 * @return the HTTP status or error code
	 */
	public int getHTTPCode() {
		return Integer.parseInt(fields.get(1));
	}
	
	/**
	 * Column #3 - the download file size in bytes
	 * @return the filesize
	 */
	public int getDownloadSize() {
		if (fields.get(2).equals("-"))
			return 0;
		
		return Integer.parseInt(fields.get(2));
	}
	
	/**
	 * Column #4 - the URL of the crawled document
	 * @return the URL
	 */
	public String getURL() {
		return fields.get(3);
	}
	
	/**
	 * Extracts the hostname from the URL and returns it. The hostname is
	 * buffered in memory to avoid multiple parsing.
	 * @return the hostname
	 */
	public String getHost() {
		if (bufferedHost == null)
			bufferedHost = getHostFromURL(getURL());
		
		return bufferedHost;
	}
	
	// TODO column 5
	
	// TODO column 6
	
	/**
	 * Column #7 - content type
	 * @return the content type
	 */
	public String getContentType() {
		return fields.get(6);
	}
	
	/**
	 * Column #8 - crawler ID
	 * @return the crawler ID
	 */
	public String getCrawlerID() {
		return fields.get(7);
	}
	
	// TODO column 9
	
	/**
	 * Column #10 - file SHA1 hash
	 * @return the file hash
	 */
	public String getSHA1Hash() {
		return fields.get(9);
	}
	
	// TODO column 10
	
	// TODO column 11
	
	@Override
	public String toString() {
		return line;
	}
	
	/**
	 * Utility method to extract the domain name from a URL. Cf.
	 * 
	 * http://stackoverflow.com/questions/4819775/implementing-public-suffix-extraction-using-java
	 * 
	 * @param url the URL
	 * @return the domain name
	 */
	private static String getHostFromURL(String url) {
		// Not the nicest solution - but neither java.net.URL nor com.google.common.net.InternetDomainName
		// can handle Heritrix' custom 'dns:' protocol prefix.
		if (url.startsWith("dns:"))
			url = "http://" + url.substring(4);
		
		String host = null;
		try {
			host = new URL(url).getHost();
			InternetDomainName domainName = InternetDomainName.from(host);
			return domainName.topPrivateDomain().name();
		} catch (MalformedURLException e) {
			Logger.warn(e.getMessage());
			return url;
		} catch (IllegalArgumentException e) {
			// Will be thrown by InternetDomainName.from in case the host name looks weird (which is frequently the case...)
			Logger.warn(e.getMessage());
			return host; 
		}
	}

}