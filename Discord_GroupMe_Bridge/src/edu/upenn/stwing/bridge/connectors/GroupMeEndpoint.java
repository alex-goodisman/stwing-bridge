package edu.upenn.stwing.bridge.connectors;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.upenn.stwing.bridge.STWingConstants;
import edu.upenn.stwing.bridge.api.BridgeEndpoint;
import edu.upenn.stwing.bridge.api.BridgeServer;
import edu.upenn.stwing.bridge.api.Message;

/**
 * Manages sending and receiving messages to the STWing GroupMe group
 * @author agoodisman
 *
 */
public class GroupMeEndpoint implements BridgeEndpoint
{
	/** The unique identifying token for this bot */
	private String botID;
	/** The user ID for this bot in this group, used to detect its own messages */
	private String botUserID;
	/** The URL path element for this servlet */
	private String servletName;
	/** The entry point for all GroupMe bots */
	private static final String GROUPME_API_URL = "https://api.groupme.com/v3/bots/post";
	
	/** The list of message listeners to be notified when a message is received */
	private List<Consumer<Message>> listeners;
		
	 /**
	  * Constructs a new GroupMe Endpoint and adds a GroupMe monitoring servlet to the
	  * singleton Bridge server
	  */
	public GroupMeEndpoint(String botID, String botUserID, String servletName)
	{
		this.botID = botID;
		this.botUserID = botUserID;
		this.servletName = servletName;
		
		//initialize
		listeners = new ArrayList<>();
		
		//add to the server under the /GroupMe path
		BridgeServer.addServlet(this.new GroupMeMonitorServlet(), "/"+this.servletName+"/*");
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
			jsonObject.put("bot_id", botID);
			jsonObject.put("text", message.toString());
			if(message.getImage() != null)
			{
				String processed = processImageForGroupMe(message.getImage());
				if(processed != null)
				{
					JSONObject image = new JSONObject();
					image.put("type", "image");
					image.put("url", processed);
					JSONArray arr = new JSONArray();
					arr.put(image);
					jsonObject.put("attachments", arr);
				}
			}
			
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
	
	private static String processImageForGroupMe(String imageURL)
	{
		try
		{
			
			HttpsURLConnection connection = (HttpsURLConnection) (new URL(imageURL).openConnection());
	        connection.setRequestMethod("GET");
	                connection.setRequestProperty(
	                        "User-Agent",
	                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
	        
	        String contentType = connection.getContentType();
	        InputStream is = connection.getInputStream();
	        
	        URL serviceURL = new URL("https://image.groupme.com/pictures");
	        HttpsURLConnection service = (HttpsURLConnection)serviceURL.openConnection();
	        service.setRequestMethod("POST");
	        service.setRequestProperty("X-Access-Token",STWingConstants.GROUPME_ACCESS_TOKEN);
	        service.setRequestProperty("Content-Type",contentType);
	        service.setDoOutput(true);
	        OutputStream os = service.getOutputStream();
	        

	        byte[] buffer = new byte[1024];
	        int len;
	        while ((len = is.read(buffer)) != -1) {
	            os.write(buffer, 0, len);
	        }
	        
	        
	        is.close();
	        os.close();
	        

			StringBuffer jb = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(service.getInputStream()));
			
			String line = null;
			
			while ((line = reader.readLine()) != null)
			{
				jb.append(line);
			}

			reader.close();
			
			JSONObject jsonObject =  new JSONObject(jb.toString());
			JSONObject payload = jsonObject.getJSONObject("payload");
			return payload.getString("url");
		}
		catch(IOException ex)
		{
			return null;
		}
		
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
				if(!(jsonObject.getString("sender_type").equals("bot") && jsonObject.getString("sender_id").equals(botUserID)))
				{
					JSONArray attachments = jsonObject.getJSONArray("attachments");
					for(int i = 0, l = attachments.length(); i < l;i++)
					{
						JSONObject att = attachments.getJSONObject(i);
						if(att.getString("type").equals("image"))
						{
							message = new Message(name,text,att.getString("url"));
							break;
						}
					}
					if(message == null)
						message = new Message(name,text);
				}
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
