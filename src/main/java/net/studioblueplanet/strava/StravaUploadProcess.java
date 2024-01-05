package net.studioblueplanet.strava;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.prefs.Preferences;

import org.apache.commons.lang3.tuple.Pair;

import hirondelle.date4j.DateTime;
import net.studioblueplanet.tomtomwatch.ActivityData;
import net.studioblueplanet.tomtomwatch.TomTomWatch;
import net.studioblueplanet.ttbin.Activity;
import net.studioblueplanet.ttbin.ActivityRecord;
import net.studioblueplanet.ttbin.GpxWriter;
import net.studioblueplanet.ttbin.TtbinFileDefinition;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StravaUploadProcess
{

	private static final String REFRESH_TOKEN = "token";

	public static void uploadToStrava(ActivityData data, MessageObservable messageObservable)
	{
		Thread uploadThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					start(data, messageObservable);
				}
			});

		// Start the thread
		uploadThread.start();

		// You can do other work here concurrently with the uploadToStrava method
		// ...

		// Wait for the upload thread to finish (optional)
		try
		{
			uploadThread.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private static void start(ActivityData data, MessageObservable messageObservable)
	{
		try
		{
			final Preferences prefs = Preferences.userRoot().node(TomTomWatch.class.getName());
			String savedSecret = prefs.get(SettingsDialog.PrefField.SECRET.toString(), null);
			String savedClientId = prefs.get(SettingsDialog.PrefField.CLIENT_ID.toString(), null);
			String backupFolder = prefs.get(SettingsDialog.PrefField.BACKUP_FOLDER.toString(), null);
			String refreshToken = prefs.get(REFRESH_TOKEN, null);
			OAuthTokenRequest oAuthTokenRequest = new OAuthTokenRequest();

			String accessToken = null;
			if (refreshToken != null)
			{
				Pair<String, String> tokens = oAuthTokenRequest.refresh(savedClientId, savedSecret, refreshToken);
				accessToken = tokens.getLeft();
				prefs.put(REFRESH_TOKEN, tokens.getRight());
			}

			if (accessToken == null)
			{
				OAuthFlow oAuthFlow = new OAuthFlow(savedClientId);
				oAuthFlow.start();
				String authorizationCode = oAuthFlow.waitForAuthorizationCode();
				Pair<String, String> tokens = oAuthTokenRequest.get(savedClientId, savedSecret, authorizationCode);
				accessToken = tokens.getLeft();
				prefs.put(REFRESH_TOKEN, tokens.getRight());
			}

			if (accessToken == null)
			{
				messageObservable
						.appendStatus(
								"Can't get the accessToken... Something is wrong with either the Client ID, Client Secret or the Authentication to Strava");
				return;
			}
			messageObservable.appendStatus("AccessToken received : " + accessToken);

			if (data != null)
			{
				Activity activity = data.activity;
				String sportType;
				String sportDescription;

				switch (activity.getActivityType())
				{
					case TtbinFileDefinition.ACTIVITY_CYCLING:
						sportType = "Ride";
						sportDescription = "Bike Ride";
						break;
					case TtbinFileDefinition.ACTIVITY_HIKING:
					case TtbinFileDefinition.ACTIVITY_RUNNING:
					case TtbinFileDefinition.ACTIVITY_TRAILRUNNING:
					default:
						sportType = "Run";
						sportDescription = "Run";
						break;
				}

				String weatherDescription = "";
				String timeOfTheDay = "";
				DateTime timestamp = null;
				if (activity.getNumberOfSegments() > 0)
				{
					List<ActivityRecord> activityRecordList = activity.getRecords(0);
					ActivityRecord activityRecord = activityRecordList.get(0);

					timestamp = activity.getFirstActiveRecordTime();

					weatherDescription = weatherDescription(activityRecord, timestamp);

					timeOfTheDay = timeOfTheDay(timestamp);
				}

				String fileName = prepareBackupFileName(backupFolder, timestamp);

				// GitBuildInfo build = GitBuildInfo.getInstance();
				String appName = "TomTomWatch "; // + build.getGitCommitDescription() + " (" + build.getBuildTime() + ")";

				GpxWriter writer = GpxWriter.getInstance();
				writer.writeTrackToFile(fileName, activity, appName);

				// Upload
				Response response = uploadGPX(accessToken, fileName, sportType, sportDescription, weatherDescription, timeOfTheDay);

				if (response.isSuccessful())
				{
					String message = "GPX Uploaded Successfully to Strava";
					messageObservable.appendStatus(message);
					System.out.println(message);
				}
				else
				{
					String message = "Error while uploading to Strava";
					messageObservable.appendStatus(message);
					System.out.println(message);
				}
			}
		}
		catch (InterruptedException | IOException e1)
		{
			messageObservable.appendStatus(e1.getMessage());
			System.out.println(e1);
		}
	}

	private static Response uploadGPX(String accessToken, String gpxFileName, String sportType, String sportDescription, String weatherDescription,
			String timeOfTheDay) throws IOException
	{
		byte[] fileBytes = Files.readAllBytes(Paths.get(gpxFileName));
		OkHttpClient client = new OkHttpClient().newBuilder()
				.build();

		if (weatherDescription == null)
		{
			weatherDescription = "";
		}

		// MediaType mediaType = MediaType.parse("text/plain");
		MediaType mediaType = MediaType.parse("application/octet-stream");
		RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("file", gpxFileName, RequestBody.create(mediaType, fileBytes))
				.addFormDataPart("data_type", "gpx")
				.addFormDataPart("name", timeOfTheDay + " " + sportDescription)
				.addFormDataPart("description", weatherDescription)
				.addFormDataPart("trainer", "0")
				.addFormDataPart("sport_type", sportType)
				.build();
		Request request = new Request.Builder()
				.url("https://www.strava.com/api/v3/uploads")
				.method("POST", body)
				.addHeader("Authorization", "Bearer " + accessToken)
				.build();
		Response response = client.newCall(request).execute();
		return response;
	}

	private static String prepareBackupFileName(String backupFolder, DateTime timestamp) throws IOException
	{
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String formattedDate = currentDate.format(timeFormatter);
		Path folder = Paths.get(backupFolder, formattedDate);

		String fileName = UUID.randomUUID().toString();
		if (timestamp != null)
		{
			Instant instant = Instant.ofEpochMilli(timestamp.getMilliseconds(TimeZone.getTimeZone("UTC")));
			ZoneId localZone = ZoneId.systemDefault();
			ZonedDateTime zonedDateTime = instant.atZone(localZone);
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
			fileName = zonedDateTime.format(dateTimeFormatter);
		}

		// Create the folder if it does not exist
		if (!Files.exists(folder))
		{
			Files.createDirectories(folder);
			System.out.println("Folder created successfully.");
		}
		else
		{
			System.out.println("Folder already exists.");
		}

		String folderFileName = Paths.get(folder.toString(), fileName + ".gpx").toString();
		return folderFileName;
	}

	private static String weatherDescription(ActivityRecord activityRecord, DateTime timestamp) throws IOException
	{
		String weatherDescription;
		double latitude = activityRecord.getLatitude();
		double longitude = activityRecord.getLongitude();

		TimeZone tz = TimeZone.getTimeZone("UTC");
		long timestampUTC = timestamp.getMilliseconds(tz);
		weatherDescription = TimeOfDayWeatherDescription.get(latitude, longitude, timestampUTC);
		return weatherDescription;
	}

	private static String timeOfTheDay(DateTime timestamp)
	{
		String timeOfTheDay;
		Instant instant = Instant.ofEpochMilli(timestamp.getMilliseconds(TimeZone.getTimeZone("UTC")));
		ZoneId localZone = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = instant.atZone(localZone);

		int hour = zonedDateTime.getHour();

		if (hour >= 0 && hour < 6)
		{
			timeOfTheDay = "Night";
		}
		else if (hour >= 6 && hour < 12)
		{
			timeOfTheDay = "Morning";
		}
		else if (hour >= 12 && hour < 17)
		{
			timeOfTheDay = "Afternoon";
		}
		else if (hour >= 17 && hour < 20)
		{
			timeOfTheDay = "Evening";
		}
		else
		{
			timeOfTheDay = "Night";
		}
		return timeOfTheDay;
	}
}
