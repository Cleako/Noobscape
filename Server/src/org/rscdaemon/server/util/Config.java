package org.rscdaemon.server.util;

/**
 * A class to handle loading configuration from XML
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.rscdaemon.server.GUI;
import org.rscdaemon.server.GameVars;

public class Config {
	static {
		loadEnv();
	}
	/**
	 * Called to load config settings from the given file
	 *
	 * @param file the xml file to load settings from
	 * @throws IOException if an i/o error occurs
	 */
	public static void initConfig(String file) throws IOException {
		START_TIME = System.currentTimeMillis();

		Properties props = new Properties();
		FileInputStream fis = new FileInputStream(new File(file));
		props.load(fis);
		
		GameVars.clientVersion = Integer.valueOf(props.getProperty("ClientVersion"));
		GameVars.portNumber = Integer.valueOf(props.getProperty("PortNumber"));
		GameVars.rangedDelaySpeed = Integer.valueOf(props.getProperty("ArrowDelaySpeed"));
		GameVars.serverLocation = props.getProperty("ServerLocation");
		GameVars.useFatigue = Integer.valueOf(props.getProperty("UseFatigue")) == 1;
		GameVars.maxUsers = Integer.valueOf(props.getProperty("MaxPlayers"));
		GameVars.saveAll = Integer.valueOf(props.getProperty("SaveAll"));
		GameVars.expMultiplier = Double.valueOf(props.getProperty("ExpMultiplier"));
		GameVars.serverName = props.getProperty("ServerName");
		/*SERVER_VERSION = Integer.parseInt(props.getProperty("version"));
		SERVER_NAME = props.getProperty("name");
		SERVER_IP = props.getProperty("ip");
		SERVER_PORT = Integer.parseInt(props.getProperty("port"));
		SERVER_LOCATION = props.getProperty("location");
		
		MAX_PLAYERS = Integer.parseInt(props.getProperty("maxplayers"));
		
		LS_IP = props.getProperty("lsip");
		LS_PORT = Integer.parseInt(props.getProperty("lsport"));
		LS_PASSWORD = props.getProperty("lspass");
		SERVER_NUM = Integer.parseInt(props.getProperty("servernum"));*/
		CONF_DIR = "conf" + File.separator + "server";
	fis.close();
	GUI.repaintSettings();
	}

	/**
	 * Called to load RSCD_HOME and CONF_DIR
	 * Used to be situated in PersistenceManager
	 */
	private static void loadEnv() {
		String home = System.getenv("RSCD_HOME");
		if (home == null) { //the env var hasnt been set, fall back to .
			home = ".";
		}
		CONF_DIR = "conf" + File.separator + "server";
		
		RSCD_HOME = home;
	}
	public static int SERVER_NUM = 72;
	public static String CONF_DIR, RSCD_HOME;
	//public static int SERVER_PORT, SERVER_VERSION, MAX_PLAYERS, LS_PORT, SERVER_NUM;
	public static long START_TIME;
}
