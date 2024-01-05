package net.studioblueplanet.strava;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author jddai
 *
 */
/**
 * @author jddai
 *
 */
public class DataEntry
{
	private long dt;
	private long sunrise;
	private long sunset;
	private double temp;
	@SerializedName("feels_like")
	private double feelsLike;
	private int pressure;
	private int humidity;
	@SerializedName("dew_point")
	private double dewPoint;
	private double uvi;
	private int clouds;
	private int visibility;
	@SerializedName("wind_speed")
	private double windSpeed;
	@SerializedName("wind_deg")
	private int windDeg;
	private List<WeatherInfo> weather;

	public long getDt()
	{
		return dt;
	}

	public void setDt(long dt)
	{
		this.dt = dt;
	}

	public long getSunrise()
	{
		return sunrise;
	}

	public void setSunrise(long sunrise)
	{
		this.sunrise = sunrise;
	}

	public long getSunset()
	{
		return sunset;
	}

	public void setSunset(long sunset)
	{
		this.sunset = sunset;
	}

	public double getTemp()
	{
		return temp;
	}

	public double getTempC()
	{
		return kelvinToCelsius(getTemp());
	}

	public double getTempF()
	{
		return kelvinToFahrenheit(getTemp());
	}

	public static double kelvinToCelsius(double kelvin)
	{
		return kelvin - 273.15;
	}

	public static double kelvinToFahrenheit(double kelvin)
	{
		return (kelvin - 273.15) * 9.0 / 5.0 + 32.0;
	}

	public void setTemp(double temp)
	{
		this.temp = temp;
	}

	public double getFeelsLike()
	{
		return feelsLike;
	}

	public double getFeelsLikeC()
	{
		return kelvinToCelsius(getTemp());
	}

	public double getFeelsLikeF()
	{
		return kelvinToFahrenheit(getTemp());
	}

	public void setFeelsLike(double feelsLike)
	{
		this.feelsLike = feelsLike;
	}

	public int getPressure()
	{
		return pressure;
	}

	public void setPressure(int pressure)
	{
		this.pressure = pressure;
	}

	public int getHumidity()
	{
		return humidity;
	}

	public void setHumidity(int humidity)
	{
		this.humidity = humidity;
	}

	public double getDewPoint()
	{
		return dewPoint;
	}

	public void setDewPoint(double dewPoint)
	{
		this.dewPoint = dewPoint;
	}

	public double getUvi()
	{
		return uvi;
	}

	public void setUvi(double uvi)
	{
		this.uvi = uvi;
	}

	public int getClouds()
	{
		return clouds;
	}

	public void setClouds(int clouds)
	{
		this.clouds = clouds;
	}

	public int getVisibility()
	{
		return visibility;
	}

	public void setVisibility(int visibility)
	{
		this.visibility = visibility;
	}

	public double getWindSpeed()
	{
		return windSpeed;
	}

	public double getWindSpeedKmh()
	{
		return windSpeed * 3.6;
	}

	public void setWindSpeed(double windSpeed)
	{
		this.windSpeed = windSpeed;
	}

	public int getWindDeg()
	{
		return windDeg;
	}

	public String getWindCardinal()
	{
		// Define cardinal directions and their degree ranges
		String[] cardinals = {
				"N",
				"NNE",
				"NE",
				"ENE",
				"E",
				"ESE",
				"SE",
				"SSE",
				"S",
				"SSW",
				"SW",
				"WSW",
				"W",
				"WNW",
				"NW",
				"NNW",
				"N"
		};

		// Ensure degrees are within 0 to 360 range
		double degrees = (getWindDeg() % 360 + 360) % 360;

		// Calculate the index into the cardinals array
		int index = (int) Math.floor((degrees + 11.25) / 22.5);

		// Return the cardinal direction
		return cardinals[index];
	}

	public void setWindDeg(int windDeg)
	{
		this.windDeg = windDeg;
	}

	public List<WeatherInfo> getWeather()
	{
		return weather;
	}

	public void setWeather(List<WeatherInfo> weather)
	{
		this.weather = weather;
	}

}
