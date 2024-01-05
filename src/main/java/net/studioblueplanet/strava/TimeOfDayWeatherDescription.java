package net.studioblueplanet.strava;

import java.io.IOException;
import java.util.prefs.Preferences;

import com.google.gson.Gson;

import net.studioblueplanet.tomtomwatch.TomTomWatch;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TimeOfDayWeatherDescription
{
	private static final Preferences prefs = Preferences.userRoot().node(TomTomWatch.class.getName());

	public static String get(double latitude, double longitude, long timestamp) throws IOException
	{
		String apiKey = prefs.get(SettingsDialog.PrefField.OPEN_WEATHER_API_KEY.toString(), null);
		if (apiKey == null)
		{
			return null;
		}

		timestamp = timestamp / 1000;
		String weatherDescriptionString = "No weather found for : " + latitude + ", " + longitude + ", " + timestamp;

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder()
				.url("https://api.openweathermap.org/data/3.0/onecall/timemachine?lat=" + latitude + "&lon=" + longitude + "&dt=" + timestamp + "&appid="
						+ apiKey)
				.build();

		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();
		Gson gson = new Gson();
		WeatherData weatherData = gson.fromJson(responseBody, WeatherData.class);

		if (weatherData.getData().size() > 0)
		{
			DataEntry dataEntry = weatherData.getData().get(0);

			String weatherDescription = "";
			if (dataEntry.getWeather().size() > 0)
			{
				WeatherInfo weatherInfo = dataEntry.getWeather().get(0);
				weatherDescription = weatherInfo.getDescription();
			}
			double windSpeedKmh = dataEntry.getWindSpeedKmh();
			String windCardinal = dataEntry.getWindCardinal();
			double tempC = dataEntry.getTempC();
			double feelsLikeC = dataEntry.getFeelsLikeC();
			double humidity = dataEntry.getHumidity();

			// Create a formatted string
			weatherDescriptionString = String.format(
					"Weather: %s\n" +
							"Temperature: %.1f°C\n" +
							"Feels Like: %.1f°C\n" +
							"Humidity: %.1f%%\n" +
							"Wind: %.1f km/h %s",
					weatherDescription,
					tempC,
					feelsLikeC,
					humidity,
					windSpeedKmh,
					windCardinal);
		}
		else
		{
			System.out.println("No weather data available.");
		}

		return weatherDescriptionString;
	}
}
