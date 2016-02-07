package org.rscdaemon.server;

import java.util.TreeMap;

import org.apache.mina.common.IoSession;
import org.rscdaemon.server.event.DelayedEvent;
import org.rscdaemon.server.event.SaveEvent;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Shop;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.net.PacketQueue;
import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.packethandler.PacketHandlerDef;
import org.rscdaemon.server.util.Logger;
import org.rscdaemon.server.util.PersistenceManager;
import org.rscdaemon.server.entityhandling.defs.extras.AdvertDef;
import org.rscdaemon.server.entityhandling.EntityHandler;
import java.util.*;

/**
 * The central motor of the game. This class is responsible for the
 * primary operation of the entire game.
 */
public final class GameEngine extends Thread {
	/**
	 * World instance
	 */
	private static final World world = World.getWorld();
	/**
	 * The packet queue to be processed
	 */
	private PacketQueue<RSCPacket> packetQueue;
	/**
	 * Whether the engine's thread is running
	 */
	int curAdvert;

	private long lastAdvert;

	private static boolean running = true;
	/**
	 * The mapping of packet IDs to their handler
	 */
	private TreeMap<Integer, PacketHandler> packetHandlers = new TreeMap<Integer, PacketHandler>();
	/**
	 * Responsible for updating all connected clients
	 */
	private ClientUpdater clientUpdater = new ClientUpdater();
	/**
	 * Handles delayed events rather than events to be ran every iteration
	 */
	private DelayedEventHandler eventHandler = new DelayedEventHandler();
	/**
	 * When the update loop was last ran, required for throttle
	 */
	private long lastSentClientUpdate = 0;

	/**
	 * Constructs a new game engine with an empty packet queue.
	 */
	public GameEngine() {
		curAdvert = 0;
		lastAdvert = 0;
		packetQueue = new PacketQueue<RSCPacket>();
		
		loadPacketHandlers();
		for(Shop shop : world.getShops()) {
			shop.initRestock();
		}
		lastAdvert = System.currentTimeMillis();
	}
	
	public static void getDayOfWeek() {
		Calendar cal = Calendar.getInstance();
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if(dayOfWeek == 1) { // Sunday
			System.out.println("Today is Sunday. The wilderness is F2P");
			world.wildernessType = false; // F2P
		} else
		if(dayOfWeek == 2) { // Monday
			System.out.println("Today is Monday. The wilderness is P2P");
			world.wildernessType = true; // P2P
		} else
		if(dayOfWeek == 3) { // Tuesday
			System.out.println("Today is Tuesday. The wilderness is P2P");
			world.wildernessType = true; // P2P
		} else
		if(dayOfWeek == 4) { // Wednesday
			System.out.println("Today is Wednesday. The wilderness is P2P");
			world.wildernessType = true; // P2P
		} else
		if(dayOfWeek == 5) { // Thursday
			System.out.println("Today is Thursday. The wilderness is P2P");
			world.wildernessType = true; // P2P
		} else
		if(dayOfWeek == 6) { // Friday
			System.out.println("Today is Friday. The wilderness is P2P");
			world.wildernessType = true; // P2P
		} else
		if(dayOfWeek == 7) { // Saturday
			System.out.println("Today is Saturday. The wilderness is F2P");
			world.wildernessType = false; // F2P
		}
	}
		
	/**
	 * The thread execution process.
	 */
	public void run() {
		Logger.print("GameEngine now running", 3);
		int curAdvert = -1;
		Logger.print(GameVars.serverName + " is now Online!", 3);
		GameVars.serverRunning = true;
		running = true;
		
		eventHandler.add(new DelayedEvent(null, GameVars.saveAll * 60000) {
			public void run() {
				for(Player p : world.getPlayers()) {
					SaveEvent.saveAll();
				}
			}
		});
		while (running) {
			long curTime = System.currentTimeMillis();
			if(curTime - lastAdvert >= 60000L)
			{
			lastAdvert = curTime;
			if(++curAdvert >= EntityHandler.getAdverts().length || curAdvert < 0)
			{
			curAdvert = 0;
			}
			AdvertDef advertDef = EntityHandler.getAdverts()[curAdvert];
			String advert = advertDef.getMessage();
			Player p;
			for(Iterator i$ = world.getPlayers().iterator(); i$.hasNext(); p.getActionSender().sendMessage((new StringBuilder()).append("@cya@[Server Message] @whi@").append(processAdvert(advert, p)).toString()))
			{
			p = (Player)i$.next();
			}

			}
			try { Thread.sleep(50); } catch(InterruptedException ie) {}
			processLoginServer();
			processIncomingPackets();
			processEvents();
			processClients();

			if(!(world.getWildernessType()));
				for(Player p : world.getPlayers()) {
				preventP2P();
			}
		}
		if(!running)
		world.getServer().unbind();
		GUI.resetVars();
	}
	private static String processAdvert(String advert, Player p)
{
advert = advert.replaceAll("%name", p.getUsername());
advert = advert.replaceAll("%online", String.valueOf(world.getPlayers().size()));
return advert;
}

	public void preventP2P() {
		for(Player p : world.getPlayers())
			if((!(world.getWildernessType())) && (p.getLocation().wildernessLevel() > 0)) {
				for(InvItem currentItem : p.getInventory().getItems()) {
					for(int index : world.getP2PItems()) {
						if(((currentItem.getID() == index) && (currentItem.isWielded()))) {
							currentItem.setWield(false);
							p.updateWornItems(currentItem.getWieldableDef().getWieldPos(), p.getPlayerAppearance().getSprite(currentItem.getWieldableDef().getWieldPos()));
							p.getActionSender().sendSound("click");
							p.getActionSender().sendEquipmentStats();
							p.getActionSender().sendInventory();
							p.getActionSender().sendMessage("You can only use @gre@F2P @whi@items in the wilderness.");
					}
				}
			}
			if(p.getCurStat(0) > p.getMaxStat(0)) {
				p.setCurStat(0, p.getMaxStat(0));
			}
			if(p.getCurStat(1) > p.getMaxStat(1)) {
				p.setCurStat(1, p.getMaxStat(1));
			}
			if(p.getCurStat(2) > p.getMaxStat(2) * 1.1D + 3.0D) {
				p.setCurStat(2, p.getMaxStat(2) + DataConversions.roundUp(p.getMaxStat(2) / 100.0D * 10.0D) + 2);
			}
        		if(p.getCurStat(3) > p.getMaxStat(3)) {
          			p.setCurStat(3, p.getMaxStat(3));
        		}
        		if(p.getCurStat(4) > p.getMaxStat(4)) {
          			p.setCurStat(4, p.getMaxStat(4));
        		}
			p.getActionSender().sendStats();
		}
	}

	public void emptyWorld() {
		for(Player p : world.getPlayers()) {
			p.save();
			p.getActionSender().sendLogout();
		}
		//world.getServer().getLoginConnector().getActionSender().saveProfiles();
	}
	
	public static void kill() {
		Logger.print("Terminating GameEngine", 1);
		GameVars.serverRunning = false;
		GUI.resetVars();
		GUI.repaintVars();	
		running = false;
		
	}
	
	public void processLoginServer() {
		//LoginConnector connector = world.getServer().getLoginConnector();
		//if(connector != null) {
		//	connector.processIncomingPackets();
		//	connector.sendQueuedPackets();
		//}
	}
	
	/**
	 * Processes incoming packets.
	 */
	private void processIncomingPackets() {
		for(RSCPacket p : packetQueue.getPackets()) {
			IoSession session = p.getSession();
			Player player = (Player)session.getAttachment();
			player.ping();
			PacketHandler handler = packetHandlers.get(p.getID());
			if (handler != null) {
				try {
					handler.handlePacket(p, session);
				}
				catch(Exception e) {
					Logger.error("Exception with p[" + p.getID() + "] from " + player.getUsername() + " [" + player.getCurrentIP() + "]: " + e.getMessage());
					player.getActionSender().sendLogout();
					player.destroy(false);
				}
			}
			else {
				Logger.error("Unhandled packet from " + player.getCurrentIP() + ": " + p.getID());
			}
		}
	}
	
	private void processEvents() {
		eventHandler.doEvents();
	}
	
	private void processClients() {
		clientUpdater.sendQueuedPackets();
		
		long now = System.currentTimeMillis();
		if(now - lastSentClientUpdate >= 600) {
			lastSentClientUpdate = now;
			clientUpdater.updateClients();
		}
	}

	/**
	 * Returns the current packet queue.
	 *
	 * @return A <code>PacketQueue</code>
	 */
	public PacketQueue<RSCPacket> getPacketQueue() {
		return packetQueue;
	}

	/**
	 * Loads the packet handling classes from the persistence
	 * manager.
	 */
	protected void loadPacketHandlers() {
		PacketHandlerDef[] handlerDefs = (PacketHandlerDef[])PersistenceManager.load("PacketHandlers.xml");
		int count = 0;
		for(PacketHandlerDef handlerDef : handlerDefs) {
			try {
				String className = handlerDef.getClassName();
				Class<?> c = Class.forName(className);
				if (c != null) {
					count++;
					
					PacketHandler handler = (PacketHandler)c.newInstance();
					for(int packetID : handlerDef.getAssociatedPackets()) {
						
						packetHandlers.put(packetID, handler);
					}
					
				}
			}
			catch (Exception e) {
				Logger.error(e);
			}
		}
		Logger.print(count + " Packet Handlers Loaded.", 3);
	}

}
