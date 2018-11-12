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
	/** URL of image attached to message, null if no image */
	private String image;
	
	/** 
	 * Constructs a message from component data
	 * @param sender The author of the message
	 * @param text The text of the message itself
	 */
	public Message(String sender, String text)
	{
		this(sender,text,null);
	}
	
	/**
	 * Constructs a message from component data with an attached image
	 * @param sender The author of the message
	 * @param text The text content of the message
	 * @param image the attached image URL
	 */
	public Message(String sender, String text, String image)
	{
		this.text = text;
		this.sender = sender;
		this.image = image;
	}
	
	public String getImage()
	{
		return image;
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
