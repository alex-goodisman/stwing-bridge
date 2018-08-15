package edu.upenn.stwing.bridge.api;

import java.util.function.Consumer;
/**
 * API for all types of endpoints.
 * Any endpoint must be able to send messages and dispatch messages that it receives.
 * @author agoodisman
 *
 */
public interface BridgeEndpoint 
{
	/**
	 * Sends a message to be displayed on this endpoint.
	 * @param message The message to send
	 */
	public void sendMessage(Message message);
	
	/**
	 * Registers a message-listener to this endpoint, to be notified when this endpoint sees new messages
	 * @param response the listener to register
	 */
	public void onMessageReceived(Consumer<Message> response);
}
