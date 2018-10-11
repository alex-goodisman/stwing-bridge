package edu.upenn.stwing.bridge.api;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Manages a web-facing server for endpoints to use with Servlets.
 * Endpoints with static access to this class can add servlets to URL mappings.
 * @author agoodisman
 *
 */
public final class BridgeServer 
{
	/**
	 * Prevent Instantiation
	 */
	private BridgeServer()
	{
	}
	
	/** The port to open the server on */
	private static int serverPort;
	/** The server instance */
	private static Server server = null;
	/** The Jetty server handler to register Servlets */
	private static ServletHandler handler;
	
	/**
	 * Sets the port that the server will be started on, when it starts.
	 * @param port the port to use
	 */
	public static void setPort(int port)
	{
		serverPort = port;
	}
	
	/**
	 * Creates and sets up the server instance.
	 * This called internally once the first servlet is registered.
	 * This does NOT start the server.
	 */
	private static void init()
	{
		System.out.println("Creating the common Server instance");
		server = new Server(serverPort);
        handler = new ServletHandler();
        server.setHandler(handler);
	}
	
	/**
	 * Registers a Servlet to the singleton server
	 * @param servlet the Servlet to add
	 * @param path The path the servlet should be mapped to (Usually should be of the form /Name/* where Name is the type of endpoint)
	 */
	public static void addServlet(Servlet servlet, String path)
	{
		//detect the first instance
		if(server == null)
			init();
		
		System.out.println("Creating a Servlet for path " + path);
		
        ServletHolder holder = new ServletHolder(servlet);
        handler.addServletWithMapping(holder, path);
	}
	
	/**
	 * If the server has been initialized by registering a servlet, starts the server.
	 * Otherwise, does nothing.
	 */
	public static void start()
	{
		if(server != null)
		{
			try
			{
				server.start();
				System.out.println("Server started");
			}
			catch(Exception ex)
			{
				System.out.println("Server failed to start:");
				ex.printStackTrace();
			}
		}
		else
			System.out.println("Server initialization was skipped");
	
	}
}
