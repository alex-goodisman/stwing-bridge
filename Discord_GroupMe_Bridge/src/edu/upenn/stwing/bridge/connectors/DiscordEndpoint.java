package edu.upenn.stwing.bridge.connectors;

import java.util.function.Consumer;

import edu.upenn.stwing.bridge.api.BridgeEndpoint;
import edu.upenn.stwing.bridge.api.Message;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

/**
 * Manages sending and receiving messages to the STWing groupme-mirror Discord channel
 * @author agoodisman
 *
 */
public class DiscordEndpoint implements BridgeEndpoint
{
	/** The unique identifying token for this bot */
	private static final String BOT_TOKEN = "NDY4MjM2NTM2MTUyOTgxNTA0.Di2OzQ.tFRuLPjeKyYDMrHfYpcFu1h3akk";
	/** The ID of the channel to post to */
	private static final long CHANNEL_ID = 468611919025012746L;
	
	/** Instance object for the discord client, used for posting messages */
	private IDiscordClient client;
	
	/**
	 * Constructs a DiscordEndpoint and connects to the STWing channel.
	 */
	public DiscordEndpoint()
	{
		//initialize the client object
		ClientBuilder clientBuilder = new ClientBuilder();
        clientBuilder.withToken(BOT_TOKEN);
        client = clientBuilder.build();
        
        //login
        client.login();
        System.out.println("Initializing Discord Endpoint");
        
        //wait until the client confirms that is actually logged in
        while(!client.isLoggedIn())
        {
        	System.out.println("Logging in...");
        	try 
        	{
				Thread.sleep(500);
			}
        	catch (InterruptedException e)
        	{
        		System.out.println("Interrupted at: " + System.currentTimeMillis());
			}
        }
        System.out.println("Logged In");
	}
	
	/**
	 * Sends a message to the Discord channel
	 * @param message the message to send
	 */
	@Override
	public void sendMessage(Message message)
	{
		System.out.println("Sending message to Discord");
		//request that the message be sent as soon as the buffer is empty
		RequestBuffer.request(() -> 
		{
			//actually send the message
			client.getChannelByID(CHANNEL_ID).sendMessage(message.toString());
			System.out.println("Message Sent");
		});
	}

	/**
	 * Adds a listener to the Discord client's event dispatcher
	 * @param response the message response to add
	 */
	@Override
	public void onMessageReceived(Consumer<Message> response)
	{
		//create a discord Listener object that responds to Discord Events
		IListener<MessageReceivedEvent> listener = ev -> 
		{
			if(ev.getChannel().getLongID() == CHANNEL_ID)
			{
				System.out.println("Receiving message from Discord");
				response.accept(new Message(ev.getMessage().getAuthor().getDisplayName(ev.getGuild()), ev.getMessage().getContent()));
			}
		};
		//register the listener
		client.getDispatcher().registerListener(listener);
		
	}
}
