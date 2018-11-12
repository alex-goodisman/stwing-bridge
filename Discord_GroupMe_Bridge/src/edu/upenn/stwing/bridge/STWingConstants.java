package edu.upenn.stwing.bridge;
/**
 * Connection IDs and Tokens for all the endpoints.
 * @author agoodisman
 *
 */
public class STWingConstants 
{
	/**
	 * Prevent Instantiation
	 */
	private STWingConstants()
	{
	}
	
	/** Identifier for all the TranscriberBot instances */
	public static String DISCORD_BOT_TOKEN = "NDY4MjM2NTM2MTUyOTgxNTA0.Di2OzQ.tFRuLPjeKyYDMrHfYpcFu1h3akk";
	
	/** The STWing Discord's Mirror Channel ID */
	public static long DISCORD_CHANNEL_ID = 468611919025012746L;
	
	/** GroupMe Access Token (for accessing Image API) - burner account */
	public static String GROUPME_ACCESS_TOKEN = "XQTJjbUGbj1Jh69bT14KxmWqkR2CCMtnjab4JmNQ";
		
	/** GroupMe Bot ID for the old GroupMe */
	public static String GROUPME_OLD_BOT_ID = "37b4147a5eeb53138376644ec7";
	
	/** Internal name for the old GroupMe bot*/
	public static String GROUPME_OLD_USER_ID = "660903";
	
	/** GroupMe Bot ID for the new GroupMe */
	public static String GROUPME_NEW_BOT_ID = "8c996e55e8a05f22cd9e3f47ec";

	/** Internal name for the new GroupMe bot*/
	public static String GROUPME_NEW_USER_ID = "733521";
	
	/** Servlet Subpath 1*/
	public static String GROUPME_SERVLET_1 = "GroupMe";
	
	/** Servlet Subpath 2*/
	public static String GROUPME_SERVLET_2 = "GroupMeNew";
	
}
