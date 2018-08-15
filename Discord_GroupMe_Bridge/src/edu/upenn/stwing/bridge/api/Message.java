package edu.upenn.stwing.bridge.api;

/**
 * Encapsulates the data being sent in a single message.
 * This protocol is used internally to communicate between endpoints
 * @author agoodisman
 *
 */
public class Message
{
	//more properties can be added here as endpoints support richer messages, such as images
	/** The text content of the message */
	private String text;
	/** The name of whoever posted the message */
	private String sender;
	
	/** 
	 * Constructs a message from component data
	 * @param sender The author of the message
	 * @param text The text of the message itself
	 */
	public Message(String sender, String text)
	{
		this.text = text;
		this.sender = sender;
	}
	
	/**
	 * Returns the message as it should appear when output by endpoints under default conditions
	 * @return the message in string form
	 */
	@Override
	public String toString()
	{
		return sender + ": " + text;
	}
}
