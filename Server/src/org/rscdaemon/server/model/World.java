package org.rscdaemon.server.model;

import org.rscdaemon.server.Server;
import org.rscdaemon.server.entityhandling.locs.*;
import org.rscdaemon.server.ClientUpdater;
import org.rscdaemon.server.DelayedEventHandler;
import org.rscdaemon.server.GUI;
import org.rscdaemon.server.util.EntityList;
import org.rscdaemon.server.event.DelayedEvent;
import org.rscdaemon.server.event.SingleEvent;
import org.rscdaemon.server.npchandler.NpcHandler;
import org.rscdaemon.server.npchandler.NpcHandlerDef;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.util.Logger;
import org.rscdaemon.server.util.PersistenceManager;
import org.rscdaemon.server.io.WorldLoader;
import org.rscdaemon.server.states.CombatState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public final class World {
	/**
	 * Event System @author Yong Min
	 */
	public int eventLevel = 0;
	public int eventStatus = 0;
	public int eventCoordX = 0;
	public int eventCoordY = 0;
	/**
	 * Wilderness Changer
	 */
	public boolean wildernessType = true;
	private boolean switchingWildernessType = false;

	int[] itemsP2P = new int[] { 593, 594, 699, 701, 700, 698, 597, 524, 795, 1278, 1288, 1218, 1217, 1216, 1215, 1214, 1213, 657, 656, 655, 654, 653, 652, 651, 650, 649, 648, 1000, 618, 617, 516, 615, 609, 1006, 522, 990, 744, 559, 560, 561, 562, 563, 564, 565, 614, 682, 683, 684, 684, 685, 836, 837, 838, 839, 840, 841, 842, 843, 844, 845, 846, 847, 848, 849, 850, 967, 968, 969, 970 };
	/**
	 * World instance
	 */
	private static World worldInstance;
	/**
	 * The maximum width of the map
	 */
	public static final int MAX_WIDTH = 944;
	/**
	 * The maximum height of the map (944 squares per level)
	 */
	public static final int MAX_HEIGHT = 3776;
	/**
	 * The tiles the map is made up of
	 */
	public ActiveTile[][] tiles = new ActiveTile[MAX_WIDTH][MAX_HEIGHT];
	/**
	 * Data about the tiles, are they walkable etc
	 */
	private TileValue[][] tileType = new TileValue[MAX_WIDTH][MAX_HEIGHT];
	/**
	 * Jackpot for PvP tournament
	 */
	private int jackpot = 0;
	/**
	 * A list of all players on the server
	 */
	private EntityList<Player> players = new EntityList<Player>(2000);
	/**
	 * A list of all npcs on the server
	 */
	private EntityList<Npc> npcs = new EntityList<Npc>(4000);
	/**
	 * The client updater instance
	 */
	private ClientUpdater clientUpdater;
	/**
	 * The delayedeventhandler instance
	 */
	private DelayedEventHandler delayedEventHandler;
	/**
	 * The server instance
	 */
	private Server server;
	/**
	 * A list of all shops on the server
	 */
	private List<Shop> shops = new ArrayList<Shop>();
	/**
	 * The mapping of npc IDs to their handler
	 */
	private TreeMap<Integer, NpcHandler> npcHandlers = new TreeMap<Integer, NpcHandler>();
	/**
	 * NPC Scripting
	 */
	public HashMap<Integer, String> npcScripts = new HashMap<Integer, String>();
	/**
	 * Drop Party
	 */
	public static int dropparty = 0; // 0 = timer isn't running - 1 = timer is running
	/**
	 * List for PvP tournament
	 */
   	 List list = new ArrayList();          // List implemented as growable array
   	 List npcList = new ArrayList();

	/**
	 * returns the only instance of this world, if there
	 * is not already one, makes it and loads everything
	 */
	public static synchronized World getWorld() {
		if (worldInstance == null) {
			worldInstance = new World();
			try {
				WorldLoader wl = new WorldLoader();
				wl.loadWorld(worldInstance);
				worldInstance.loadNpcHandlers();
				worldInstance.loadScripts();
			}
			catch(Exception e) {
				Logger.error(e);
			}	
		}
		return worldInstance;
	}

	public void loadScripts() {
		int npccount = 0;
		int error = 0;
		for(File files : new File("scripts/").listFiles()) {
			try {
				int id = Integer.parseInt(files.getName().substring(0, 3).trim());
				npcScripts.put(id, files.getAbsolutePath());
			} catch(Exception e) {
				error++;
				continue;
			} finally {
				npccount++;
			}
		}
		System.out.println(npccount + " NPC Scripts loaded! " + (error > 1 ? ((error - 1) + " Error scripts") : ""));
	}

	/**
	 * Wilderness Changer
	 */
	public void setWildernessType(boolean type) {
		wildernessType = type;
	}

	public boolean getWildernessType() {
		return wildernessType;
	}

	public boolean isWildernessSwitching() {
		return switchingWildernessType;
	}

	public void setWildernessSwitching(boolean switching) {
		switchingWildernessType = switching;
	}

	public int[] getP2PItems() {
		return itemsP2P;
	}

	public void setP2PItems(int[] itemsP2P) {
		itemsP2P = itemsP2P;
	}

	/**
	 * returns the associated npc handler
	 */
	public NpcHandler getNpcHandler(int npcID) {
		return npcHandlers.get(npcID);
	}

	public void sendToAll(String s) {
		World world = World.getWorld();
		for(Player p : world.getPlayers()) {
			p.getActionSender().sendMessage(s);
		}
	}
	
	public void deletePlayer(String player) {
		if(GUI.isOnline(player)) {
			this.banPlayer(player);
		}
		File f = new File("players/" + player + ".cfg");
		if(f.exists()) {
			f.delete();
		}
		GUI.populateWorldList();
	}
	
	public void mutePlayer(String player) {
		Player p = this.getPlayer(DataConversions.usernameToHash(player));
		if(p != null) {
			p.mute = 1;
		} else {
			GUI.writeValue(player, "mute", "1");
		}
	}

	public void demotePlayer(String player) {
		Player p = this.getPlayer(DataConversions.usernameToHash(player));
			if(p != null) {
				p.rank = 0;
			} else {
				GUI.writeValue(player, "rank", "0");
			}
	}

      public void promotePMod(String player) {
		Player p = this.getPlayer(DataConversions.usernameToHash(player));
			if(p != null) {
				p.rank = 2;
			} else {
				GUI.writeValue(player, "rank", "2");
			}
        }

        public void promoteMod(String player) {
		Player p = this.getPlayer(DataConversions.usernameToHash(player));
			if(p != null) {
				p.rank = 3;
			} else {
				GUI.writeValue(player, "rank", "3");
			}
        }

        public void promoteAdmin(String player) {
		Player p = this.getPlayer(DataConversions.usernameToHash(player));
			if(p != null) {
				p.rank = 4;
			} else {
				GUI.writeValue(player, "rank", "4");
			}
        }

        public void promoteEvent(String player) {
		Player p = this.getPlayer(DataConversions.usernameToHash(player));
			if(p != null) {
				p.rank = 7;
			} else {
				GUI.writeValue(player, "rank", "7");
			}
        }

        public void promoteDeveloper(String player) {
		Player p = this.getPlayer(DataConversions.usernameToHash(player));
			if(p != null) {
				p.rank = 8;
			} else {
				GUI.writeValue(player, "rank", "8");
			}
        }

	  public void kickPlayer(String p) {
		try {
		Player player = this.getPlayer(DataConversions.usernameToHash(p));
		String play = player.getUsername().replaceAll(" ","_");
		File f = new File("players/" + play.toLowerCase() + ".cfg");
		Properties pr = new Properties();

		FileInputStream fis = new FileInputStream(f);
		pr.load(fis);
		fis.close();

		FileOutputStream fos = new FileOutputStream(f);
		pr.setProperty("loggedin", "false");
		pr.store(fos, "Character Data.");
		fos.close();

		for(Player pla : this.getPlayers()) {
			if(pla.isFriendsWith(player.getUsername())) {
				pla.getActionSender().sendFriendUpdate(player.getUsernameHash(), 0);
			}
		}
		player.save();
		player.destroy(true);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void unbanPlayer(String player) {
		
		GUI.writeValue(player, "rank", "0");
	}

      public void unMutePlayer(String p) {
		Player player = this.getPlayer(DataConversions.usernameToHash(p));
		if(player != null) {
	 		player.mute = 0;
		} else {
			GUI.writeValue(p, "mute", "1");
		}
               
      }

	public void banPlayer(String player) {
		player= player.replaceAll(" ","_");
		if(GUI.isOnline(player)) {
			Player p = this.getPlayer(DataConversions.usernameToHash(player));
			p.rank = 6;
			kickPlayer(player);
		} else {
			GUI.writeValue(player, "rank", "6");
		}
		
	}



	/**
	 * Loads the npc handling classes
	 */
	private void loadNpcHandlers() {
		NpcHandlerDef[] handlerDefs = (NpcHandlerDef[])PersistenceManager.load("NpcHandlers.xml");
		for(NpcHandlerDef handlerDef : handlerDefs) {
			try {
				String className = handlerDef.getClassName();
				Class<?> c = Class.forName(className);
				if (c != null) {
					NpcHandler handler = (NpcHandler)c.newInstance();
					for(int npcID : handlerDef.getAssociatedNpcs()) {
						npcHandlers.put(npcID, handler);
					}
				}
			}
			catch (Exception e) {
				Logger.error(e);
			}
		}
	}

	/**
	 * Inserts a new shop into the world
	 */
	public void registerShop(final Shop shop) {
		shop.setEquilibrium();
		shops.add(shop);
	}

	public List<Shop> getShops() {
		return shops;
	}

	/**
	 * Gets a list of all shops
	 */
	public Shop getShop(Point location) {
		for(Shop shop : shops) {
			if(shop.withinShop(location)) {
				return shop;
			}
		}
		return null;
	}

	/*********************************************
	*************PvP Tournament*******************
	*********************************************/
	public void addPvpEntry(Player p){
		list.add(p);
		System.out.println(p.getUsername()+"="+list.indexOf(p));
	}

	Player winner = null;
	Player autoer = null;

	public void removePvpEntry(Player p){
		list.remove(p);
	}
	public boolean getPvpEntry(Player p){
		return list.contains(p);
	}
	public int getPvpSize(){
		return list.size();
	}
	public Player getWinner(){
		return winner;
	}
	public void setWinner(Player p){
		winner=p;
	}
	public Player getautoer(){
		return autoer;
	}
	public void setautoer(Player p){
		autoer=p;
	}
	public int getJackPot(){
		return jackpot;
	}
	public void setJackPot(int i){
		jackpot=i;
	}
	public void clearJackPot(){
		jackpot=0;
	}
	/*********************************************
	*********************END**********************
	*********************************************/

	/**
	 * Sets the instance of the server
	 */
	public void setServer(Server server) {
		this.server = server;
	}

	/**
	 * Gets the server instance
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * Sets the ClientUpdater instance
	 */
	public void setClientUpdater(ClientUpdater clientUpdater) {
		this.clientUpdater = clientUpdater;
	}

	/**
	 * Sets the DelayedEventHandler instance
	 */
	public void setDelayedEventHandler(DelayedEventHandler delayedEventHandler) {
		this.delayedEventHandler = delayedEventHandler;
	}

	/**
	 * Gets the ClientUpdater instance
	 */
	public ClientUpdater getClientUpdater() {
		return clientUpdater;
	}

	/**
	 * Gets the DelayedEventHandler instance
	 */
	public DelayedEventHandler getDelayedEventHandler() {
		return delayedEventHandler;
	}

	/**
	 * adds or removes the given entity from the relivant tiles
	 */
	public void setLocation(Entity entity, Point oldPoint, Point newPoint) {
		ActiveTile t;
		if (oldPoint != null) {
			t = getTile(oldPoint);
			t.remove(entity);
		}
		if (newPoint != null) {
			t = getTile(newPoint);
			t.add(entity);
		}
	}

	/**
	 * Are the given coords within the world boundaries
	 */
	public boolean withinWorld(int x, int y) {
		return x >= 0 && x < MAX_WIDTH && y >= 0 && y < MAX_HEIGHT;
	}

	/**
	 * Gets the tile value as point x, y
	 */
	public TileValue getTileValue(int x, int y) {
		if(!withinWorld(x, y)) {
			return null;
		}
		TileValue t = tileType[x][y];
		if (t == null) {
			t = new TileValue();
			tileType[x][y] = t;
		}
		return t;
	}

	/**
	 * Gets the active tile at point x, y
	 */
	public ActiveTile getTile(int x, int y) {
		if(!withinWorld(x, y)) {
			return null;
		}
		ActiveTile t = tiles[x][y];
		if (t == null) {
			t = new ActiveTile(x, y);
			tiles[x][y] = t;
		}
		return t;
	}

	/**
	 * Gets the tile at a point
	 */
	public ActiveTile getTile(Point p) {
		return getTile(p.getX(), p.getY());
	}

	/**
	 * Adds a DelayedEvent that will spawn a GameObject
	 */
	public void delayedSpawnObject(final GameObjectLoc loc, final int respawnTime) {
		delayedEventHandler.add(new SingleEvent(null, respawnTime) {
			public void action() {
				registerGameObject(new GameObject(loc));
			}
		});
	}

	/**
	 * Adds a DelayedEvent that will remove a GameObject
	 */
	public void delayedRemoveObject(final GameObject object, final int delay) {
		delayedEventHandler.add(new SingleEvent(null, delay) {
			public void action() {
				ActiveTile tile = getTile(object.getLocation());
				if(tile.hasGameObject() && tile.getGameObject().equals(object)) {
					unregisterGameObject(object);
				}
			}
		});
	}

	/**
	 * Registers a player with the world and informs other players on their login
	 */
	public void registerPlayer(Player p) {
		p.setInitialized();
		players.add(p);
	}

	/**
	 * Registers an npc with the world
	 */
	public void registerNpc(Npc n) {
		NPCLoc npc = n.getLoc();
		if(npc.startX < npc.minX || npc.startX > npc.maxX || npc.startY < npc.minY || npc.startY > npc.maxY || (getTileValue(npc.startX, npc.startY).mapValue & 64) != 0) {
			System.out.println("Fucked Npc: <id>" + npc.id + "</id><startX>" + npc.startX + "</startX><startY>" + npc.startY + "</startY>");
		}
		npcs.add(n);
	}

	/**
	 * Checks if the given player is logged in
	 */
	public boolean isLoggedIn(long usernameHash) {
		Player friend = getPlayer(usernameHash);
		if(friend != null) {
			return friend.loggedIn();
		}
		return false;
	}

	/**
	 * Registers an object with the world
	 */
	public void registerGameObject(GameObject o) {
		switch(o.getType()) {
		case 0:
			registerObject(o);
			break;
		case 1:
			registerDoor(o);
			break;
		}
	}

	/**
	 * Updates the map to include a new object
	 */
	public void registerObject(GameObject o) {
		if(o.getGameObjectDef().getType() != 1 && o.getGameObjectDef().getType() != 2) {
			return;
		}
		int dir = o.getDirection();
		int width, height;
		if(dir == 0 || dir == 4) {
			width = o.getGameObjectDef().getWidth();
			height = o.getGameObjectDef().getHeight();
		}
		else {
			height = o.getGameObjectDef().getWidth();
			width = o.getGameObjectDef().getHeight();
		}
		for(int x = o.getX();x < o.getX() + width;x++) {
			for(int y = o.getY();y < o.getY() + height;y++) {
				if(o.getGameObjectDef().getType() == 1) {
					getTileValue(x, y).objectValue |= 0x40;
				}
				else if(dir == 0) {
					getTileValue(x, y).objectValue |= 2;
					getTileValue(x - 1, y).objectValue |= 8;
				}
				else if(dir == 2) {
					getTileValue(x, y).objectValue |= 4;
					getTileValue(x, y + 1).objectValue |= 1;
				}
				else if(dir == 4) {
					getTileValue(x, y).objectValue |= 8;
					getTileValue(x + 1, y).objectValue |= 2;
				}
				else if(dir == 6) {
					getTileValue(x, y).objectValue |= 1;
					getTileValue(x, y - 1).objectValue |= 4;
				}
			}
		}
	}

	/**
	 * Updates the map to include a new door
	 */
	public void registerDoor(GameObject o) {
		if(o.getDoorDef().getDoorType() != 1) {
			return;
		}
		int dir = o.getDirection();
		int x = o.getX(), y = o.getY();
		if(dir == 0) {
			getTileValue(x, y).objectValue |= 1;
			getTileValue(x, y - 1).objectValue |= 4;
		}
		else if(dir == 1) {
			getTileValue(x, y).objectValue |= 2;
			getTileValue(x - 1, y).objectValue |= 8;
		}
		else if(dir == 2) {
			getTileValue(x, y).objectValue |= 0x10;
		}
		else if(dir == 3) {
			getTileValue(x, y).objectValue |= 0x20;
		}
	}

	/**
	 * Removes an object from the map
	 */
	public void unregisterObject(GameObject o) {
		if(o.getGameObjectDef().getType() != 1 && o.getGameObjectDef().getType() != 2) {
			return;
		}
		int dir = o.getDirection();
		int width, height;
		if (dir == 0 || dir == 4) {
			width = o.getGameObjectDef().getWidth();
			height = o.getGameObjectDef().getHeight();
		}
		else {
			height = o.getGameObjectDef().getWidth();
			width = o.getGameObjectDef().getHeight();
		}
		for(int x = o.getX();x < o.getX() + width;x++) {
			for(int y = o.getY();y < o.getY() + height;y++) {
				if(o.getGameObjectDef().getType() == 1) {
					getTileValue(x, y).objectValue &= 0xffbf;
				}
				else if(dir == 0) {
					getTileValue(x, y).objectValue &= 0xfffd;
					getTileValue(x - 1, y).objectValue &= 65535 - 8;
				}
				else if(dir == 2) {
					getTileValue(x, y).objectValue &= 0xfffb;
					getTileValue(x, y + 1).objectValue &= 65535 - 1;
				}
				else if(dir == 4) {
					getTileValue(x, y).objectValue &= 0xfff7;
					getTileValue(x + 1, y).objectValue &= 65535 - 2;
				}
				else if(dir == 6) {
					getTileValue(x, y).objectValue &= 0xfffe;
					getTileValue(x, y - 1).objectValue &= 65535 - 4;
				}
			}
		}
	}

	/**
	 * Removes a door from the map
	 */
	public void unregisterDoor(GameObject o) {
		if(o.getDoorDef().getDoorType() != 1) {
			return;
		}
		int dir = o.getDirection();
		int x = o.getX(), y = o.getY();
		if(dir == 0) {
			getTileValue(x, y).objectValue &= 0xfffe;
			getTileValue(x, y - 1).objectValue &= 65535 - 4;
		}
		else if(dir == 1) {
			getTileValue(x, y).objectValue &= 0xfffd;
			getTileValue(x - 1, y).objectValue &= 65535 - 8;
		}
		else if(dir == 2) {
			getTileValue(x, y).objectValue &= 0xffef;
		}
		else if(dir == 3) {
			getTileValue(x, y).objectValue &= 0xffdf;
		}
	}

	/**
	 * Registers an item to be removed after 3 minutes
	 */
	public void registerItem(final Item i) {
		if(i.getLoc() == null) {
			delayedEventHandler.add(new DelayedEvent(null, 180000) {
				public void run() {
					ActiveTile tile = getTile(i.getLocation());
					if(tile.hasItem(i)) {
						unregisterItem(i);
					}
					running = false;
				}
			});
		}
	}

	/**
	 * Removes a player from the server and saves their account
	 */
	public void unregisterPlayer(Player p) {
		p.setLoggedIn(false);
		p.resetAll();
		p.save();
		Mob opponent = p.getOpponent();
		if(opponent != null) {
			p.resetCombat(CombatState.ERROR);
			opponent.resetCombat(CombatState.ERROR);
		}
		delayedEventHandler.removePlayersEvents(p);
		players.remove(p);
		setLocation(p, p.getLocation(), null);
	}

	/**
	 * Removes an npc from the server
	 */
	public void unregisterNpc(Npc n) {
		if(hasNpc(n)) {
			npcs.remove(n);
		}
		setLocation(n, n.getLocation(), null);
	}

	/**
	 * Removes an object from the server
	 */
	public void unregisterGameObject(GameObject o) {
		o.remove();
		setLocation(o, o.getLocation(), null);
		switch(o.getType()) {
		case 0:
			unregisterObject(o);
			break;
		case 1:
			unregisterDoor(o);
			break;
		}
	}

	/**
	 * Removes an item from the server
	 */
	public void unregisterItem(Item i) {
		i.remove();
		setLocation(i, i.getLocation(), null);
	}

	/**
	 * Gets the list of players on the server
	 */
	public EntityList<Player> getPlayers() {
		return players;
	}

	/**
	 * Gets the list of npcs on the server
	 */
	public EntityList<Npc> getNpcs() {
		return npcs;
	}

	/**
	 * Counts how many players are currently connected
	 */
	public int countPlayers() {
		return players.size();
	}

	/**
	 * Counts how many npcs are currently here
	 */
	public int countNpcs() {
		return npcs.size();
	}

	/**
	 * Checks if the given npc is on the server
	 */
	public boolean hasNpc(Npc n) {
		return npcs.contains(n);
	}

	/**
	 * Checks if the given player is on the server
	 */
	public boolean hasPlayer(Player p) {
		return players.contains(p);
	}

	/**
	 * Gets a player by their username hash
	 */
	public Player getPlayer(long usernameHash) {
		for(Player p : players) {
			if(p.getUsernameHash() == usernameHash) {
				return p;
			}
		}
		return null;
	}
	public Player getPlayer(String username)
	{
		return getPlayer(DataConversions.usernameToHash(username));
	}

	/**
	 * Gets an Npc by their server index
	 */
	public Npc getNpc(int idx) {
		return npcs.get(idx);
	}

	/**
	 *  Gets an npc by their coords and id]
	 */
	public Npc getNpc(int id, int minX, int maxX, int minY, int maxY) {
		for(Npc npc : npcs) {
			if(npc.getID() == id && npc.getX() >= minX && npc.getX() <= maxX && npc.getY() >= minY && npc.getY() <= maxY) {
				return npc;
			}
		}
		return null;
	}

	/**
	 * Gets a Player by their server index
	 */
	public Player getPlayer(int idx) {
		return players.get(idx);
	}
}
