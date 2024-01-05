package net.studioblueplanet.strava;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class OAuthFlow
{
	private String authorizationCode;
	private final CountDownLatch codeReceivedLatch = new CountDownLatch(1);

	private static final String AUTH_BASE_URL = "http://www.strava.com/oauth/authorize";
	private static final String REDIRECT_URI = "http://localhost/exchange_token";
	private static final String SCOPE = "activity:write,read";
	private static final int SERVER_PORT = 80;
	private static HttpServer server;

	private final String clientId;

	public OAuthFlow(String clientId)
	{
		this.clientId = clientId;
	}

	public void start()
	{
		try
		{
			String authUrl = buildAuthorizationUrl(clientId);
			openAuthorizationUrl(authUrl);
			startHttpServer(SERVER_PORT);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private String buildAuthorizationUrl(String clientId)
	{
		return AUTH_BASE_URL + "?client_id=" + clientId +
				"&response_type=code&redirect_uri=" + REDIRECT_URI +
				"&approval_prompt=force&scope=" + SCOPE;
	}

	private void openAuthorizationUrl(String url) throws IOException, URISyntaxException
	{
		Desktop desktop = Desktop.getDesktop();
		desktop.browse(new URI(url));
	}

	private void startHttpServer(int port) throws IOException
	{
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/exchange_token", new OAuthCallbackHandler());
		server.start();

		System.out.println("Server is running on port " + port + ". Waiting for the callback...");
	}

	public String waitForAuthorizationCode() throws InterruptedException
	{
		codeReceivedLatch.await();
		server.stop(10);
		return authorizationCode;
	}

	class OAuthCallbackHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange exchange) throws IOException
		{
			// Get the request URI
			URI requestURI = exchange.getRequestURI();

			// Parse the query parameters from the URI
			Map<String, String> queryParams = new HashMap<>();
			String query = requestURI.getQuery();
			if (query != null)
			{
				String[] pairs = query.split("&");
				for (String pair : pairs)
				{
					String[] keyValue = pair.split("=");
					if (keyValue.length == 2)
					{
						queryParams.put(keyValue[0], keyValue[1]);
					}
				}
			}

			// Extract the authorization code
			String code = queryParams.get("code");

			// Set the authorization code in the instance variable
			authorizationCode = code;

			// Signal that the code has been received
			codeReceivedLatch.countDown();

			// Create an HTML response page
			String response = "<html><body><h1>Authorization Code Received</h1>"
					+ "<p>The authorization code is: " + code + "</p>"
					+ "<p>You can now close this window.</p></body></html>";

			// Set the response headers
			exchange.sendResponseHeaders(200, response.length());

			// Get the response output stream and write the HTML response
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
