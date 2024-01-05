package net.studioblueplanet.strava;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OAuthTokenRequest
{
	private static final String STRAVA_TOKEN_URL = "https://www.strava.com/oauth/token";

	public Pair<String, String> get(String clientId, String clientSecret, String code)
	{
		OkHttpClient client = new OkHttpClient();
		MediaType mediaType = MediaType.parse("text/plain");
		RequestBody body = RequestBody.create(mediaType, "");

		HttpUrl url = HttpUrl.parse(STRAVA_TOKEN_URL)
				.newBuilder()
				.addQueryParameter("client_id", clientId)
				.addQueryParameter("client_secret", clientSecret)
				.addQueryParameter("code", code)
				.addQueryParameter("grant_type", "authorization_code")
				.build();

		Request request = new Request.Builder()
				.url(url)
				.method("POST", body)
				.build();

		try (Response response = client.newCall(request).execute())
		{
			int statusCode = response.code();
			if (statusCode == 200)
			{
				// Successful response, handle accordingly
				String responseBody = response.body().string();
				System.out.println("Response: " + responseBody);
				JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
				String accessToken = jsonResponse.get("access_token").getAsString();
				System.out.println("Access Token: " + accessToken);
				String refreshToken = jsonResponse.get("refresh_token").getAsString();
				System.out.println("Refresh Token: " + refreshToken);
				return Pair.of(accessToken, refreshToken);
			}
			else
			{
				// Handle errors based on status code
				System.err.println("Error: HTTP " + statusCode);
				return null;
			}
		}
		catch (IOException e)
		{
			// Handle exceptions
			e.printStackTrace();
		}
		return null;
	}

	public Pair<String, String> refresh(String clientId, String clientSecret, String refreshToken)
	{
		OkHttpClient client = new OkHttpClient();
		MediaType mediaType = MediaType.parse("text/plain");
		RequestBody body = RequestBody.create(mediaType, "");

		HttpUrl url = HttpUrl.parse(STRAVA_TOKEN_URL)
				.newBuilder()
				.addQueryParameter("client_id", clientId)
				.addQueryParameter("client_secret", clientSecret)
				.addQueryParameter("grant_type", "refresh_token")
				.addQueryParameter("refresh_token", refreshToken)
				.build();

		Request request = new Request.Builder()
				.url(url)
				.method("POST", body)
				.build();

		try (Response response = client.newCall(request).execute())
		{
			int statusCode = response.code();
			if (statusCode == 200)
			{
				// Successful response, handle accordingly
				String responseBody = response.body().string();
				System.out.println("Response: " + responseBody);
				JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
				String accessToken = jsonResponse.get("access_token").getAsString();
				System.out.println("Access Token: " + accessToken);
				String newRefreshToken = jsonResponse.get("refresh_token").getAsString();
				System.out.println("Refresh Token: " + newRefreshToken);
				return Pair.of(accessToken, newRefreshToken);
			}
			else
			{
				// Handle errors based on status code
				System.err.println("Error: HTTP " + statusCode);
				return null;
			}
		}
		catch (IOException e)
		{
			// Handle exceptions
			e.printStackTrace();
		}
		return null;
	}
}
