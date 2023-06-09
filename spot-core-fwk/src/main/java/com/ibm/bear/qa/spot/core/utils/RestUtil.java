/*********************************************************************
* Copyright (c) 2012, 2021 IBM Corporation and others.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
**********************************************************************/
package com.ibm.bear.qa.spot.core.utils;

import static com.ibm.bear.qa.spot.core.scenario.ScenarioUtils.debugPrint;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.net.ssl.*;


public class RestUtil {

	/**
	 * Execute HTTP DELETE
	 * @param url
	 * @param login
	 * @param password
	 * @return response
	 * @throws Exception
	 */
	public static String doDelete(final String url, final String login, final String password) throws Exception {
		HttpURLConnection connection = getConnection(url, login, password, null);
		connection.setRequestMethod("DELETE");
		connection.setRequestProperty("Accept", "*/*");
		int responseCode = connection.getResponseCode();
		String result = readResponse(connection);
		if (responseCode != HttpURLConnection.HTTP_OK) {
			connection.disconnect();
			throw new Exception("Get returned response code " + responseCode + " with message " + result);
		}
		connection.disconnect();
		return result;
	}

	/**
	 * Execute HTTP GET
	 * @param url
	 * @param login
	 * @param password
	 * @return response
	 * @throws Exception
	 */
	public static String doGet(final String url, final String login, final String password) throws Exception {
		HttpURLConnection connection = getConnection(url, login, password, null);
		connection.setRequestMethod("GET");
		connection.setRequestProperty("accept", "*/*");
		int responseCode = connection.getResponseCode();
		String result = readResponse(connection);
		if (responseCode == HttpURLConnection.HTTP_OK) {
			connection.disconnect();
			return result;
		}
		connection.disconnect();
		throw new Exception("Get returned response code " + responseCode + " with message " + result);
	}


	/**
	 * Execute HTTP GET and store result to file
	 * @param url
	 * @param login
	 * @param password
	 * @param outputPath
	 * @throws Exception
	 */
	public static void doGetToFile(final String url, final String login, final String password, final String outputPath) throws Exception {
		HttpURLConnection connection = getConnection(url, login, password, null);
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "*/*");
		int responseCode = connection.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			try (InputStream inputStream = connection.getInputStream();
				FileOutputStream outputStream = new FileOutputStream(outputPath)) {
				int bytesRead = -1;
				byte[] buffer = new byte[4096];
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
			}
			connection.disconnect();
		} else {
			connection.disconnect();
			throw new Exception("GET returned response code " + responseCode);
		}
	}

	/**
	 * Execute HTTP POST
	 * @param url
	 * @param login
	 * @param password
	 * @param apiKeyHeader
	 * @param payload
	 * @return response
	 * @throws Exception
	 */
	public static String doPost(final String url, final String login, final String password, final String apiKeyHeader, final String payload, final String contentType) throws Exception {
		HttpURLConnection connection = getConnection(url, login, password, apiKeyHeader);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", contentType);
		connection.setRequestProperty("Accept", "*/*");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setReadTimeout(2*60000);
		connection.setConnectTimeout(2*60000);

		OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
		wr.write(payload);
		wr.flush();

		int responseCode = connection.getResponseCode();
		String result = readResponse(connection);
		debugPrint("Execution result " + result);
		if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
			connection.disconnect();
			return result;
		}
		connection.disconnect();
		throw new Exception("POST returned response code " + responseCode + " with message " + result);
	}

	/**
	 * Execute HTTP POST of a file.
	 *
	 * @param url
	 * @param login
	 * @param password
	 * @param filePath
	 * @throws Exception
	 */
	public static String doPostFile(final String url, final String login, final String password, final String filePath) throws Exception {
		HttpURLConnection connection = getConnection(url, login, password, null);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Accept", "*/*");
		connection.setRequestProperty("Content-Type", "application/octet-stream");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setReadTimeout(2*60000);
		connection.setConnectTimeout(2*60000);

		try (FileInputStream fileInputStream = new FileInputStream(new File(filePath));
			DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
			int bufferSize = 4096;
			byte[] buffer = new byte[bufferSize];
			int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
		}

		int responseCode = connection.getResponseCode();
		String result = readResponse(connection);
		if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
			connection.disconnect();
			return result;
		}
		connection.disconnect();
		throw new Exception("POST returned response code " + responseCode + " with message " + result);
	}

	private static HttpURLConnection getConnection(final String url, final String login, final String password, final String apiKeyHeader) throws IOException {
		HttpURLConnection connection = null;
		SSLSocketFactory sf = null;
		String authorization = null;
		if (url.startsWith("http://")) {
			String userpass = login + ":" + password;
			authorization = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			connection = (HttpURLConnection) new URL(url).openConnection();
		} else {
			// Override hostname verification for vhost
			HttpsURLConnection.setDefaultHostnameVerifier(
					new javax.net.ssl.HostnameVerifier(){
						@Override
						public boolean verify(final String hostname,
								final javax.net.ssl.SSLSession sslSession) {
							debugPrint("HostnameVerifier hostname: " + hostname);
							/* Always allow SSL as we can have SSL certificate for any https protocol (e.g. ODM on ICP)
							if (hostname.contains(SSL_HOSTNAME_STRING_TO_IGNORE)) {
								return true;
							}
							return false;
							*/
							return true;
						}
					});
			// Set credentials
			if (apiKeyHeader == null) {
				String userpass = login + ":" + password;
				authorization = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			} else {
				authorization = "ZenApiKey " + apiKeyHeader;
			}
			connection = (HttpsURLConnection) new URL(url).openConnection();

			sf = getSSLSocketFactory();
			if (sf != null) {
				debugPrint("Setting SSL Socket Factory on connection");
				((HttpsURLConnection) connection).setSSLSocketFactory(sf);
				// Now you can access an https URL without having the certificate in the truststore
			} else {
				debugPrint("No  SSL Socket Factory needed for connection");
			}
		}
		connection.setRequestProperty("Authorization", authorization);

		return connection;
	}


	private static String readResponse(final HttpURLConnection connection) throws UnsupportedEncodingException, IOException {
		StringBuilder sb = new StringBuilder();
		int responseCode = connection.getResponseCode();
		try (InputStream is = (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) ? connection.getInputStream() : connection.getErrorStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		}
		return sb.toString();
	}

	private static SSLSocketFactory getSSLSocketFactory(){
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] {
		    new X509TrustManager() {
		        @Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		            return new X509Certificate[0];
		        }
		        @Override
				public void checkClientTrusted(
		            final java.security.cert.X509Certificate[] certs, final String authType) {
		            }
		        @Override
				public void checkServerTrusted(
		            final java.security.cert.X509Certificate[] certs, final String authType) {
		        }
		    }
		};

		try {
			//SSLContext sslContext =SSLContext.getDefault();
			//return sslContext.getSocketFactory();

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			return sc.getSocketFactory();
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
		}
		return null;
	}

}
