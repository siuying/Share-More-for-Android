package hk.ignition.commons;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


public class Instapaper {
	private String username;
	private String password;
	private HttpClient httpclient;
	
	private static final String URL_AUTH = "https://www.instapaper.com/api/authenticate";
	private static final String URL_ADD = "https://www.instapaper.com/api/add";
	
	public Instapaper(String username, String password) {
		this.username = username;
		this.password = password;
		this.httpclient = new DefaultHttpClient();
	}

	/**
	 * Attempt to authencate the user
	 * 
	 * @return true if successfully authenticate user, false if username/password invalid OR service error
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws InstapaperException
	 */
	public boolean authenticate() throws ClientProtocolException, IOException, InstapaperException {
		String reqUrl = URL_AUTH + "?username=" + URLEncoder.encode(username) + "&" +
			"password=" + URLEncoder.encode(password);

		HttpGet httpGet = new HttpGet(reqUrl);
		HttpResponse response = httpclient.execute(httpGet);
		return (response.getStatusLine().getStatusCode() == 200);
	}
	
	/**
	 * Add url to instapaper
	 * 
	 * @param urlToAdd url to be added to instapaper
	 * @return
	 * @throws IOException Connection error
	 * @throws ClientProtocolException protocol error
	 * @throws InstapaperException Instapaper error
	 */
	public void add(String urlToAdd) throws ClientProtocolException, IOException, InstapaperException {
		String reqUrl = URL_ADD + "?auto-title=1&" +
			"username=" + URLEncoder.encode(username) + "&" +
			"password=" + URLEncoder.encode(password) + "&" +
			"url=" + URLEncoder.encode(urlToAdd); 
		HttpGet httpGet = new HttpGet(reqUrl);
		HttpResponse response = httpclient.execute(httpGet);
		InstapaperException.throwExceptionFromStatus(response.getStatusLine().getStatusCode());
	}
}
