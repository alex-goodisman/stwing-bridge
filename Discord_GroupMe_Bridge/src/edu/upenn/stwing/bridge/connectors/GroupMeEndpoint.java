package edu.upenn.stwing.bridge.connectors;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import edu.upenn.stwing.bridge.BridgeServer;
import edu.upenn.stwing.bridge.api.BridgeEndpoint;
import edu.upenn.stwing.bridge.api.Message;

/**
 * Manages sending and receiving messages to the STWing GroupMe group
 * @author agoodisman
 *
 */
public class GroupMeEndpoint implements BridgeEndpoint
{
	/** The unique identifying token for this bot */
	private static final String BOT_ID = "37b4147a5eeb53138376644ec7";
	/** The user ID for this bot in this group, used to detect its own messages */
	private static final String BOT_USER_ID = "660903";
	/** The entry point for all GroupMe bots */
	private static final String GROUPME_API_URL = "https://api.groupme.com/v3/bots/post";
	
	/** The list of message listeners to be notified when a message is received */
	private List<Consumer<Message>> listeners;
		
	 /**
	  * Constructs a new GroupMe Endpoint and adds a GroupMe monitoring servlet to the
	  * singleton Bridge server
	  */
	public GroupMeEndpoint()
	{
		//initialize
		listeners = new ArrayList<>();
		
		//add to the server under the /GroupMe path
		BridgeServer.addServlet(this.new GroupMeMonitorServlet(), "/GroupMe/*");
        System.out.println("Initializing GroupMe Endpoint");
	}
	
	/**
	 * Sends a message to the GroupMe group
	 * @param message the message to send
	 */
	@Override
	public void sendMessage(Message message)
	{
		try
		{
			//create the JSON representation of the message
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("bot_id", BOT_ID);
			jsonObject.put("text", message.toString());
			
			//connect to GroupMe API
			URL obj = new URL(GROUPME_API_URL);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			
			System.out.println("Sending message to GroupMe");
			
			//write the JSON object
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(jsonObject.toString());
			wr.flush();
			wr.close();
			
			//read the response
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			while (in.readLine() != null) 
			{
			}
			in.close();
			System.out.println("Message Sent");

		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Registers a message listener to be notified when the GroupMe servlet is POSTed to
	 * @param response the listener to add
	 */
	@Override
	public void onMessageReceived(Consumer<Message> response)
	{
		listeners.add(response);
	}
	
	/**
	 * The GroupMe servlet that the bot can POST to in order to
	 * notify the bridge that a message has been received.
	 * Inner class is associated with a particular instance of GroupMeEndpoint
	 * @author agoodisman
	 *
	 */
	@SuppressWarnings("serial")
	private class GroupMeMonitorServlet extends HttpServlet
	{
		/**
		 * Responds to a POST request
		 * @param request the Http POST request that was received
		 * @param response the response object to write output to
		 */
		@Override
		public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
		{
			//read the request data into a string
			System.out.println("Receiving POST request");
			StringBuffer jb = new StringBuffer();
			BufferedReader reader = null;
			
			reader = request.getReader();
			String line = null;
			
			while ((line = reader.readLine()) != null)
			{
				jb.append(line);
			}

			reader.close();
			
			Message message = null;
			
			//parse string to JSON
			try
			{
				JSONObject jsonObject =  new JSONObject(jb.toString());
				String name = jsonObject.getString("name");
				String text = jsonObject.getString("text");
				//screen out messages that this bot itself sent, so that it doesn't mirror its own messages
				if(!(jsonObject.getString("sender_type").equals("bot") && jsonObject.getString("sender_id").equals(BOT_USER_ID)))
					message = new Message(name,text);
				else
					System.out.println("Dismissing a message from this bot to itself");
			}
			catch(JSONException ex)
			{
				//if it's missing any of the right components, ignore it
				System.out.println("Request not parseable as a GroupMe Message:");
				ex.printStackTrace();
			}
			
			//if the message was parsed correctly
			if(message != null)
			{
				//alert the listeners
				System.out.println("Receiving GroupMe message");
				for(Consumer<Message> listener : listeners)
				{
					listener.accept(message);
				}
			}
			

		}
	}
}