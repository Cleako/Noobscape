package org.rscdaemon.server;

import org.rscdaemon.server.event.DelayedEvent;
import org.rscdaemon.server.event.SingleEvent;
import org.rscdaemon.server.util.*;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.clansystem.ClanHandler;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.net.RSCConnectionHandler;
import org.rscdaemon.server.net.*;

import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoAcceptorConfig;
import org.apache.mina.transport.socket.nio.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.util.Calendar;
import java.text.SimpleDateFormat;

import java.net.InetSocketAddress;

import java.util.Properties;
import java.util.ArrayList;

import javax.swing.UIManager;

/**
 * The entry point for RSC server.
 */
public class Server {
	/**
	 * World instance
	 */
	private static final World world = World.getWorld();
	/**
	 * The game engine
	 */
	private GameEngine engine;
	/**
	 * The SocketAcceptor
	 */
	private IoAcceptor acceptor;
	/**
	 * Update event - if the server is shutting down
	 */
	private DelayedEvent updateEvent, pvpEvent, duelingEvent, dropParty, wildSwitch;
	/**
	 * The login server connection
	 */
	/**
	 * Is the server running still?
	 */
	private boolean running;
	/**
	 * A list of all the banned IP addresses
	 */
	private static ArrayList<String> bannedIP;
	
	public boolean running() {
		return running;
	}

	/**
	 * Clan System @author Yong Min
	 */
	private ClanHandler clanHandler;

	public ClanHandler getClanHandler() {
		return clanHandler;
	}
	
	/**
	 * Shutdown the server in 60 seconds
	 */
	public boolean shutdownForUpdate() {
		if(updateEvent != null) {
			return false;
		}
		updateEvent = new SingleEvent(null, 65000) {
    			public void action() {
    				kill();
    			}
    		};
		world.getDelayedEventHandler().add(updateEvent);
		return true;
	}

	/**
	 * Login Logger @author Yong Min
	 */
	public static final String DATE_FORMAT_NOW = "dd-MM-yyyy";
	public static final String TIME_FORMAT_NOW = "HH:mm:ss";

	Calendar calendar = Calendar.getInstance();
	SimpleDateFormat date = new SimpleDateFormat(DATE_FORMAT_NOW);
	SimpleDateFormat time = new SimpleDateFormat(TIME_FORMAT_NOW);

	public void addLogin() {
		for(Player p : world.getPlayers()) {
			try {
				BufferedWriter login = new BufferedWriter(new FileWriter("logs/" + "logins.txt", true));
				login.write(p.getUsername() + " logged in from " + p.getCurrentIP() + " on the " + date.format(calendar.getTime()) + " at " + time.format(calendar.getTime()));
				login.newLine();
				login.flush();
				login.close();
				//System.out.println("The login for " + p.getUsername() + " has been successfully logged.");
				}
				catch (Exception ex) {
					System.out.println(ex);
			}
		}
	}

	/**
	 * Drop Party
	 */
	public boolean startDropParty() {
		if(dropParty != null) {
			return false;
		}
		dropParty = new SingleEvent(null, 63500) {
			public void action() {
				for(Player p : world.getPlayers()) {
					p.getActionSender().sendMessage("The drop party has now started! The @or1@::partyhall @whi@command is now: @red@Disabled");
					dropParty = null;
					World.dropparty = 0;
				}
			}
		};
		world.getDelayedEventHandler().add(dropParty);
		return true;
	}

	/**
	 * Wilderness Switch
	 */
	public boolean startWildernessSwitch() {
		if(wildSwitch != null) {
			return false;
		}
		wildSwitch = new SingleEvent(null, 66200) {
			public void action() {
				if(world.getWildernessType())
					world.setWildernessType(false);
				else {
					world.setWildernessType(true);
				}
				for(Player p : world.getPlayers()) {
					String wildernessType;
					if(world.getWildernessType())
						wildernessType = "@gre@P2P";
					else {
						wildernessType = "@gre@F2P";
					}
					p.getActionSender().sendMessage("The wilderness has been changed to: " + wildernessType);
					if(p.getLocation().wildernessLevel() > 0) {
						for(InvItem wieldedItem : p.getInventory().getItems()) {
							for(int items : world.getP2PItems()) {
								if((wieldedItem.getID() == items) && (wieldedItem.isWielded())) {
									wieldedItem.setWield(false);
									p.updateWornItems(wieldedItem.getWieldableDef().getWieldPos(), p.getPlayerAppearance().getSprite(wieldedItem.getWieldableDef().getWieldPos()));
									p.getActionSender().sendSound("click");
									p.getActionSender().sendEquipmentStats();
									p.getActionSender().sendInventory();
									world.setWildernessSwitching(false);
								}
							}
						}
						if(p.getCurStat(0) > p.getMaxStat(0)) {
							p.setCurStat(0, p.getMaxStat(0));
						}
						if(p.getCurStat(1) > p.getMaxStat(1)) {
							p.setCurStat(1, p.getMaxStat(1));
						}
						if(p.getCurStat(2) > p.getMaxStat(2)) {
							p.setCurStat(2, p.getMaxStat(2) + DataConversions.roundUp(p.getMaxStat(2) / 100.0D * 10.0D) + 2);
						}
						if(p.getCurStat(3) > p.getMaxStat(3)) {
							p.setCurStat(3, p.getMaxStat(3));
						}
						if(p.getCurStat(4) > p.getMaxStat(4)) {
							p.setCurStat(4, p.getMaxStat(4));
						}
						p.getActionSender().sendStats();
						wildSwitch = null;
					}
				};
			}
		};
		world.getDelayedEventHandler().add(wildSwitch);
		return true;
	}

	/** 
	* PvP Arena
	**/
	public boolean pvpTimerStart(int time) {
		if(pvpEvent != null) {
			return false;
		}
		pvpEvent = new SingleEvent(null, time * 1000) {
    			public void action() {
					for(Player p : world.getPlayers()) {
						p.getActionSender().sendMessage("The PvP tournament has started!");
						if(world.getPvpEntry(p) && p.getLocation().inWaitingRoom()){
							p.teleport(228, 130, false);
						}
					}
					duelingEvent();
    			}
    		};
		world.getDelayedEventHandler().add(pvpEvent);
		return true;
	}

	public boolean duelingEvent() {
		if(duelingEvent != null) {
			return false;
		}
		stopPvp();
		duelingEvent = new SingleEvent(null, 666666666) {
    			public void action() {
					System.out.println("Shouldn't have reached here...Duel arena hackers.");
    			}
    		};
		world.getDelayedEventHandler().add(duelingEvent);
		return true;
	}

	public void stopPvp() {
		if(pvpEvent != null){
			pvpEvent.stop();
			pvpEvent=null;
		}
	}

	public boolean pvpIsRunning() {
		if(duelingEvent != null){
			return duelingEvent.isRunning();
		}
		else{return false;}
	}

	public boolean waitingIsRunning() {
		if(pvpEvent != null){
			return pvpEvent.isRunning();
		}
		else{return false;}
	}

	public void stopDuel() {
		if(duelingEvent != null){
			duelingEvent.stop();
			duelingEvent=null;
		}
		for(Player p : world.getPlayers()) {
			p.getActionSender().sendMessage("The winner of the PvP tournament was: @red@"+world.getWinner().getUsername());
			p.getActionSender().sendMessage("He won @gre@"+world.getJackPot()+"GP");
		}
	}
	
	/**
	 * MS till the server shuts down
	 */
	public int timeTillShutdown() {
		if(updateEvent == null) {
			return -1;
		}
		return updateEvent.timeTillNextRun();
	}

	public int timeTillDropParty() {
		if(dropParty == null) {
			return -1;
		}
		return dropParty.timeTillNextRun();
	}

	public int timeTillPvp() {
		if(pvpEvent == null) {
			return -1;
		}
		return pvpEvent.timeTillNextRun();
	}

	public int timeTillWildSwitch() {
		if(wildSwitch == null) {
			return -1;
		}
		return wildSwitch.timeTillNextRun();
	}

	public int timeTillDuel() {
		if(duelingEvent == null) {
			return -1;
		}
		return duelingEvent.timeTillNextRun();
	}
	
	public void resetOnline() {
		try {
		File files = new File("players/");
		int count = 0;
		for(File f : files.listFiles()) {
			
			if(f.getName().endsWith(".cfg")) {
				count++;
				Properties pr = new Properties();

				FileInputStream fis = new FileInputStream(f);
				pr.load(fis);
				fis.close();
				pr.setProperty("loggedin",  "false");
				FileOutputStream fos = new FileOutputStream(f);
				pr.store(fos, "Character Data.");
				fos.close();
			}
			
		}
		Logger.print(count + " Accounts exist.", 3);
		} catch (Exception e) {
			Logger.print(e.toString(), 1);
		}
	}

	/**
	 * Gets the banned IP addresses
	 */
	public static final ArrayList<String> getBannedIP() {
		return bannedIP;
	}

	/**
	 * IP Ban List
	 */
	public final void loadBannedIP() {
		try {
			bannedIP = new ArrayList<String>();
			FileInputStream ipbans = new FileInputStream("ipbans/" + "ipbans.txt");
			DataInputStream in = new DataInputStream(ipbans);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String ipLine;
			System.out.println("Loading Banned IP Addressess");
			System.out.println("");
			while ((ipLine = reader.readLine()) != null) {
				System.out.println("Loaded: " + ipLine);
				bannedIP.add(ipLine);
			}
			in.close();
			System.out.println("");
			System.out.println("Banned IP Addresses Loaded Successfully");
		} catch (Exception e) {
			System.err.println("Error reading the ipbans.txt file.");
		}
	}

	/**
	 * Creates a new server instance, which in turn creates a new
	 * engine and prepares the server socket to accept connections.
	 */
	public Server() {
		resetOnline();
		loadBannedIP();
		GameEngine.getDayOfWeek();
		running = true;
		world.setServer(this);
		try {
			engine = new GameEngine();
			engine.start();
			clanHandler = new ClanHandler();
			acceptor = new SocketAcceptor();
			IoAcceptorConfig config = new SocketAcceptorConfig();
			config.setDisconnectOnUnbind(true);
			((SocketSessionConfig)config.getSessionConfig()).setReuseAddress(true);
			acceptor.bind(new InetSocketAddress("localhost", GameVars.portNumber), new RSCConnectionHandler(engine), config);
		}
		catch (Exception e) {
			Logger.error(e);
		}
	}
	
	/**
	 * Returns the game engine for this server
	 */
	public GameEngine getEngine() {
		return engine;
	}
	
	public boolean isInitialized() {
		return engine != null;
	}
	
	/**
	 * Kills the game engine and irc engine
	 */
	public void kill() {
		GUI.resetVars();
		Logger.print("CleanRSC Shutting Down...", 3);
		running = false;
		engine.emptyWorld();
	}
	
	/**
	 * Unbinds the socket acceptor
	 */
	public void unbind() {
		try {
			acceptor.unbindAll();
			GUI.cout("Socket Closed", 3);
		}
		catch(Exception e) { }
	}

	public static void main(String[] args) throws IOException {
		try {
			
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			//UIManager.setLookAndFeel("com.easynth.lookandfeel.EaSynthLookAndFeel");
		} catch (Exception e) {
				
			}
			GUI.args = args;
		new GUI();
		
	}
}
