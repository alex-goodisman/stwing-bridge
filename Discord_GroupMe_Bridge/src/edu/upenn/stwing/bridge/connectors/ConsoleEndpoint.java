package edu.upenn.stwing.bridge.connectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import edu.upenn.stwing.bridge.api.BridgeEndpoint;
import edu.upenn.stwing.bridge.api.Message;

/**
 * This endpoint implements the same input/output spec
 * as the real endpoints, but uses STDIN/STDOUT on the machine
 * where the bridge is running as its UI.
 * @author agoodisman
 *
 */
public class ConsoleEndpoint implements BridgeEndpoint
{
	/** The listeners to notify when a message is typed into STDIN */
	private List<Consumer<Message>> listeners;
		
	/**
	 * Constructs a new ConsoleEndpoint.
	 * Spawns a new Thread to await command-line input.
	 */
	public ConsoleEndpoint()
	{
		//initialize
		listeners = new ArrayList<>();
        System.out.println("Initializing Console Endpoint");
        
        //create the input management thread
        new Thread(() ->
        {
        	//setup
        	@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
        	//await input forever
        	while(true)
        	{
        		//get input
        		String line = scanner.nextLine();
        		//alert listeners
        		for(Consumer<Message> listener : listeners)
        		{
        			listener.accept(new Message("Server", line));
        		}
        	}
        }).start();
	}
	
	/**
	 * Displays a message to STDOUT
	 * @param message the message to display
	 */
	public void sendMessage(Message message)
	{
		System.out.println(message.toString());
	}
	
	/**
	 * Adds a listener to relay input from STDIN
	 * @param response a message listener to add
	 */
	public void onMessageReceived(Consumer<Message> response)
	{
		listeners.add(response);
	}
}
