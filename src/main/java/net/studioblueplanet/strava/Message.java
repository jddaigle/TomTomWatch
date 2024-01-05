package net.studioblueplanet.strava;

public class Message
{
	private Action action;
	private String message;

	private Message(Action action, String message)
	{
		this.action = action;
		this.message = message;
	}

	public Action getAction()
	{
		return action;
	}

	public void setAction(Action action)
	{
		this.action = action;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public enum Action
	{
		APPEND,
		SET
	}

	public static Message set(String message)
	{
		return new Message(Action.SET, message);
	}

	public static Message append(String message)
	{
		return new Message(Action.APPEND, message);
	}
}
