package edu.upenn.stwing.bridge;

import edu.upenn.stwing.bridge.api.BridgeEndpoint;
import edu.upenn.stwing.bridge.api.BridgeServer;
import edu.upenn.stwing.bridge.connectors.ConsoleEndpoint;
import edu.upenn.stwing.bridge.connectors.DiscordEndpoint;
import edu.upenn.stwing.bridge.connectors.GroupMeEndpoint;

import static edu.upenn.stwing.bridge.STWingConstants.*;

/**
 * Manages the connection between all the different endpoints.
 * Initializes the server instance.
 * @author agoodisman
 *
 */
public class DiscordGroupMeBridge 
{
	/** The endpoint for TranscriberBot in the Discord's groupme-mirror channel */
	private BridgeEndpoint discord;
	/** The endpoint for TranscriberBot in the STWing GroupMe */
	private BridgeEndpoint groupMe;
	/** The endpoint for TranscriberBot in the new STWing GroupMe */
	private BridgeEndpoint groupMeNew;
	/** The endpoint for the command line */
	private ConsoleEndpoint console;
	
	/**
	 * Constructs a bridge. Initializes the component endpoints
	 */
	public DiscordGroupMeBridge()
	{
		discord = new DiscordEndpoint(DISCORD_BOT_TOKEN, DISCORD_CHANNEL_ID);
		groupMe = new GroupMeEndpoint(GROUPME_OLD_BOT_ID, GROUPME_OLD_USER_ID, GROUPME_SERVLET_1);
		groupMeNew = new GroupMeEndpoint(GROUPME_NEW_BOT_ID, GROUPME_NEW_USER_ID, GROUPME_SERVLET_2);
		console = new ConsoleEndpoint();
	}
	
	/**
	 * Connects the endpoints to each other.
	 * Joins Discord and GroupMe to each other,
	 * and dispatches command-line input to both outputs
	 */
	private void startBridge()
	{	
		//mirror both groupmes on to discord
		groupMe.onMessageReceived(discord::sendMessage);
		groupMeNew.onMessageReceived(discord::sendMessage);
		
		//Don't send messages back to the old GroupMe
		//discord.onMessageReceived(groupMe::sendMessage);
		
		//Do send messages to the new GroupMe
		discord.onMessageReceived(groupMeNew::sendMessage);
		
		//mirror console onto all
		console.onMessageReceived(discord::sendMessage);
		console.onMessageReceived(groupMe::sendMessage);
		console.onMessageReceived(groupMeNew::sendMessage);
		
		System.out.println("Starting Bridge");
	}
	
	/**
	 * Run the Bridge. After initialization, waits forever on the main thread.
	 * @param args Command-Line arguments. args[0] is an optional port number for the web server(defaults to 8080)
	 */
	public static void main(String[] args)
	{
		//parse arguments
		int port = 8080;
		if(args.length != 0)
		{
			try
			{
				port = Integer.parseInt(args[0]);
				System.out.println("Using port " + port);
			}
			catch(NumberFormatException ex)
			{
				System.out.println("Malformed port \"" + args[0] + "\", using default");
			}
		}
		else
			System.out.println("Using default port");
		
		System.out.println("port=" + port);
		
		//initialization
		BridgeServer.setPort(port);
		
		DiscordGroupMeBridge bridge = new DiscordGroupMeBridge();
		bridge.startBridge();
		
		BridgeServer.start();
		
		//wait forever
		while(true)
		{
			System.out.println("Waiting for input...");
			try
			{
				Thread.sleep(10000);
			}
			catch(InterruptedException ex)
			{
				System.out.println("Interrupted at: " + System.currentTimeMillis());
			}
		}
	}
}
