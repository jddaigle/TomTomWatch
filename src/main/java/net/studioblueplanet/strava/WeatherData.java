package net.studioblueplanet.strava;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class WeatherData
{
	private double lat;
	private double lon;
	private String timezone;
	@SerializedName("timezone_offset")
	private int timezoneOffset;
	private List<DataEntry> data;

	public double getLat()
	{
		return lat;
	}

	public void setLat(double lat)
	{
		this.lat = lat;
	}

	public double getLon()
	{
		return lon;
	}

	public void setLon(double lon)
	{
		this.lon = lon;
	}

	public String getTimezone()
	{
		return timezone;
	}

	public void setTimezone(String timezone)
	{
		this.timezone = timezone;
	}

	public int getTimezoneOffset()
	{
		return timezoneOffset;
	}

	public void setTimezoneOffset(int timezoneOffset)
	{
		this.timezoneOffset = timezoneOffset;
	}

	public List<DataEntry> getData()
	{
		return data;
	}

	public void setData(List<DataEntry> data)
	{
		this.data = data;
	}

}
