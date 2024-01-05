package net.studioblueplanet.strava;

import java.util.Observable;

public class MessageObservable extends Observable
{
	public void appendStatus(String message)
	{
		setChanged();
		notifyObservers(Message.append(message));
	}

	public void setStatus(String message)
	{
		setChanged();
		notifyObservers(Message.set(message));
	}
}
