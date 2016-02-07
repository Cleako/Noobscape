package org.rscdaemon.server.model;

import org.rscdaemon.server.GUI;
import org.rscdaemon.server.GameVars;
import org.rscdaemon.server.util.Config;
import org.rscdaemon.server.util.Formulae;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.util.StatefulEntityCollection;
import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.packetbuilder.RSCPacketBuilder;
import org.rscdaemon.server.packetbuilder.client.MiscPacketBuilder;
import org.rscdaemon.server.entityhandling.EntityHandler;
import org.rscdaemon.server.entityhandling.defs.PrayerDef;
import org.rscdaemon.server.event.*;
import org.rscdaemon.server.states.Action;
import org.rscdaemon.server.states.CombatState;
import org.rscdaemon.server.util.Logger;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.Point;
import org.rscdaemon.server.clansystem.*;
import org.rscdaemon.server.*;

import org.apache.mina.common.IoSession;

import bsh.Interpreter;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * A single player.
 */
public final class Player extends Mob {

	public Thread interpreterThread = null;
	public int lastOption = -2;
	public String[] lastOptions = null;
	public Interpreter interpreter = new Interpreter();

	public void setSEvent(ShortEvent sEvent) {
		world.getDelayedEventHandler().add(sEvent);
	}

	/**
	 * The player's username
	 */
	private String username;
	/**
	 * The player's username hash
	 */
	private long usernameHash;
	/**
	 * The player's password
	 */
	private String password;
	/**
	 * Whether the player is currently logged in
	 */
	private boolean loggedIn = false;
	/**
	 * The IO session of this player
	 */
	private IoSession ioSession;
	/**
	 * Last time a 'ping' was received
	 */
	private long lastPing = System.currentTimeMillis();
	public int rank;
	/**
	 * The Players appearance
	 */
	private PlayerAppearance appearance;
	/**
	 * The items being worn by the player
	 */
	private int[] wornItems = new int[12];
	/**
	 * The current stat array
	 */
	private int[] curStat = new int[18];
	/**
	 * The max stat array
	 */
	private int[] maxStat = new int[18];
	/**
	 * The exp level array
	 */
	private int[] exp = new int[18];
	/**
	 * If the player has been sending suscicious packets
	 */
	private boolean suspicious = false;
	/**
	 * List of players this player 'knows' (recieved from the client) about
	 */
	private HashMap<Integer, Integer> knownPlayersAppearanceIDs = new HashMap<Integer, Integer>();
	/**
	 * Nearby players that we should be aware of
	 */
	private StatefulEntityCollection<Player> watchedPlayers = new StatefulEntityCollection<Player>();
	/**
	 * Nearby game objects that we should be aware of
	 */
	private StatefulEntityCollection<GameObject> watchedObjects = new StatefulEntityCollection<GameObject>();
	/**
	 * Nearby items that we should be aware of
	 */
	private StatefulEntityCollection<Item> watchedItems = new StatefulEntityCollection<Item>();
	/**
	 * Nearby npcs that we should be aware of
	 */
	private StatefulEntityCollection<Npc> watchedNpcs = new StatefulEntityCollection<Npc>();
	/**
	 * Inventory to hold items
	 */
	private Inventory inventory;
	/**
	 * Bank for banked items
	 */
	private Bank bank;
	/**
	 * Users privacy settings, chat block etc.
	 */
	private boolean[] privacySettings = new boolean[4];
	/**
	 * Users game settings, camera rotation preference etc
	 */
	private boolean[] gameSettings = new boolean[7]; // Why is 1 empty?
	/**
	 * Methods to send packets related to actions
	 */
	private MiscPacketBuilder actionSender;
	/**
	 * Unix time when the player last logged in
	 */
	private long lastLogin = 0;

	public boolean bad_login = false;
	/**
	 * Unix time when the player logged in
	 */
	private long currentLogin = 0;
	/**
	 * Stores the last IP address used
	 */
	private String lastIP = "0.0.0.0";
	/**
	 * Stores the current IP address used
	 */
	private String currentIP = "0.0.0.0";
	/**
	 * If the player is reconnecting after connection loss
	 */
	private boolean reconnecting = false;
	/**
	 * Controls if were allowed to accept appearance updates
	 */
	private boolean changingAppearance = false;
	/**
	 * Is the character male?
	 */
	private boolean maleGender;
	/**
	 * The player we last requested to trade with, or null for none
	 */
	private Player wishToTrade = null;
	/**
	 * The player we last requested to duel with, or null for none
	 */
	private Player wishToDuel = null;
	/**
	 * If the player is currently in a trade
	 */
	private boolean isTrading = false;
	/**
	 * If the player is currently in a duel
	 */
	private boolean isDueling = false;
	/**
	 * List of items offered in the current trade
	 */
	private ArrayList<InvItem> tradeOffer = new ArrayList<InvItem>();
	/**
	 * List of items offered in the current duel
	 */
	private ArrayList<InvItem> duelOffer = new ArrayList<InvItem>();
	/**
	 * If the first trade screen has been accepted
	 */
	private boolean tradeOfferAccepted = false;
	/**
	 * If the first duel screen has been accepted
	 */
	private boolean duelOfferAccepted = false;
	/**
	 * If the second trade screen has been accepted
	 */
	private boolean tradeConfirmAccepted = false;
	/**
	 * If the second duel screen has been accepted
	 */
	private boolean duelConfirmAccepted = false;
	/**
	 * Map of players on players friend list
	 */
	private ArrayList<String> friendList = new ArrayList<String>();
	/**
	 * List of usernameHash's of players on players ignore list
	 */
	private HashSet<String> ignoreList = new HashSet<String>();
	/**
	 * List of all projectiles needing displayed
	 */
	private ArrayList<Projectile> projectilesNeedingDisplayed = new ArrayList<Projectile>();
	/**
	 * List of players who have been hit
	 */
	private ArrayList<Player> playersNeedingHitsUpdate = new ArrayList<Player>();
	/**
	 * List of players who have been hit
	 */
	private ArrayList<Npc> npcsNeedingHitsUpdate = new ArrayList<Npc>();
	/**
	 * Chat messages needing displayed
	 */
	private ArrayList<ChatMessage> chatMessagesNeedingDisplayed = new ArrayList<ChatMessage>();
	/**
	 * NPC messages needing displayed
	 */
	private ArrayList<ChatMessage> npcMessagesNeedingDisplayed = new ArrayList<ChatMessage>();
	/**
	 * Bubbles needing displayed
	 */
	private ArrayList<Bubble> bubblesNeedingDisplayed = new ArrayList<Bubble>();
	/**
	 * The time of the last spell cast, used as a throttle
	 */
	private long lastSpellCast = 0;
	/**
	 * Players we have been attacked by signed login, used to check if we should get a skull for attacking back
	 */
	private HashMap<Long, Long> attackedBy = new HashMap<Long, Long>();
	/**
	 * Time last report was sent, used to throttle reports
	 */
	private long lastReport = 0;
	/**
	 * Time last suggestion was sent, used to throttle suggestions
	 */
	private long lastSuggestion = 0;
	/**
	 * Time of last charge spell
	 */
	private long lastCharge = 0;
	/**
	 * Combat style: 0 - all, 1 - str, 2 - att, 3 - def
	 */
	private int combatStyle = 0;
	/**
	 * Should we destroy this player?
	 */
	private boolean destroy = false;
	/**
	 * Session keys for the players session
	 */
	private int[] sessionKeys = new int[4];
	/**
	 * Is the player accessing their bank?
	 */
	private boolean inBank = false;
	/**
	 * A handler for any menu we are currently in
	 */
	private MenuHandler menuHandler = null;
	/**
	 * DelayedEvent responsible for handling prayer drains
	 */
	private DelayedEvent drainer;
	/**
	 * The drain rate of the prayers currently enabled
	 */
	private int drainRate = 0;
	/**
	 * DelayedEvent used for removing players skull after 20mins
	 */
	private DelayedEvent skullEvent = null;
	/**
	 * Killing Spree Ranks
	 */
	private String killingSpreeRank[] = {"No Rank", "Noob", "Pker", "Hitman", "Assassin", "Legend"};
	/**
	 * Amount of fatigue - 0 to 100
	 */
	private int fatigue = 0;
	/**
	 * Has the player been registered into the world?
	 */
	private boolean initialized = false;
	/**
	 * The shop (if any) the player is currently accessing
	 */
	private Shop shop = null;
	/**
	 * The npc we are currently interacting with
	 */
	private Npc interactingNpc = null;
	/**
	 * The ID of the owning account
	 */
	private int owner = 1;
	/**
	 * Queue of last 100 packets, used for auto detection purposes
	 */
	private LinkedList<RSCPacket> lastPackets = new LinkedList<RSCPacket>();
	/**
	 * When the users subscription expires (or 0 if they don't have one)
	 */
	private long subscriptionExpires = 0;
	/**
	 * Who we are currently following (if anyone)
	 */
	private Mob following;
	/**
	 * Event to handle following
	 */
	private DelayedEvent followEvent;
	/**
	 * Ranging event
	 */
	private RangeEvent rangeEvent;
	/**
	 * Last arrow fired
	 */
	private long lastArrow = 0;
	/**
	 * Last packet count time
	 */
	private long lastCount = 0;
	/**
	 * Amount of packets since last count
	 */
	private int packetCount = 0;
	/**
	 * List of chat messages to send
	 */
	private LinkedList<ChatMessage> chatQueue = new LinkedList<ChatMessage>();
	/**
	 * Time of last trade/duel request
	 */
	private long lastTradeDuelRequest = 0;
	/**
	 * The name of the client class they are connecting from
	 */
	private String className = "NOT_SET";
	/**
	 * The current status of the player
	 */
	private Action status = Action.IDLE;
	/**
	 * Duel options
	 */
	private boolean[] duelOptions = new boolean[4];
	/**
	 * Is a trade/duel update required?
	 */
	private boolean requiresOfferUpdate = false;
	/**
	 * Last Npc killed
	 */
	public long lastNpcKill = System.currentTimeMillis();
	/**
	 * Flags
	 */
	public String flag = null;
	/**
	 * Suggestions @author Yong Min
	 */
	public static final String DATE_FORMAT_NOW = "dd-MM-yyyy";
	public static final String TIME_FORMAT_NOW = "HH:mm:ss";

	Calendar calendar = Calendar.getInstance();
	SimpleDateFormat date = new SimpleDateFormat(DATE_FORMAT_NOW);
	SimpleDateFormat time = new SimpleDateFormat(TIME_FORMAT_NOW);

	public boolean canSendSuggestion() {
		return System.currentTimeMillis() - lastSuggestion > 300000;
	}

	public void setLastSuggestion() {
		lastSuggestion = System.currentTimeMillis();
	}

	public void sendSuggestion(String suggestion) {
		if(canSendSuggestion()) {
			try {
				getActionSender().sendMessage("Thank you. Your suggestion has been successfully added to our files.");
				BufferedWriter out = new BufferedWriter(new FileWriter("suggestions/" + "suggestions.txt", true));
				out.write("[" + date.format(calendar.getTime()) + "] " + "[" + time.format(calendar.getTime()) + "] " + "[" + getCurrentIP() + "] [" + getUsername() + "]" + " has suggested: " + suggestion);
				out.newLine();
				out.flush();
				out.close();
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
	
	/**
	 * Subscription System
	 */
	/**
	 * Check if the player is a subscriber or not
	 */
	public static boolean isSub(String user) {
		int rank = Integer.valueOf(GUI.readValue(user, "rank"));
		return rank == 1;
	}
	/**
	 * Set the user a 30 day subscription
	 */
	public static void setSub(String user) {
		if(GUI.isOnline(user)) {
			Player p = world.getPlayer(DataConversions.usernameToHash(user));
			p.rank = 1;
		} else {
			GUI.writeValue(user, "rank", "1");
		}
		java.util.Calendar cal = java.util.Calendar.getInstance();
		
		cal.add(Calendar.DATE, 30);
		GUI.writeValue(user, "sube", Long.toString(cal.getTime().getTime()));
	}
	public static void setSub(String user, int length) {
		if(GUI.isOnline(user)) {
			Player p = world.getPlayer(DataConversions.usernameToHash(user));
			p.rank = 1;
		} else {
			GUI.writeValue(user, "rank", "1");
		}
		java.util.Calendar cal = java.util.Calendar.getInstance();
		
		cal.add(Calendar.DATE, length);
		GUI.writeValue(user, "sube", Long.toString(cal.getTime().getTime()));
	}
	/**
	 * Unsubscribe the users subscription
	 */
	public static void unSetSub(String user) {
		if(GUI.isOnline(user)) {
			Player p = world.getPlayer(DataConversions.usernameToHash(user));
			p.rank = 0;
		} else {
			GUI.writeValue(user, "rank", "0");
		}
		GUI.writeValue(user, "sube", "");
	}
	/**
	 * Gets when the subscription for the user ends
	 */
	public static long getSubEnd(String user) {
		if(!isSub(user)) {
			return 0; // Shouldn't really happen
		}
		long getsube = Long.parseLong(GUI.readValue(user, "sube"));
		return getsube;
	}
	/**
	 * Gets the remaining subscription of the user
	 */
	public static int getRemSub(String user) {
		long sube = Long.parseLong(GUI.readValue(user, "sube"));
		java.util.Calendar cal = java.util.Calendar.getInstance();
		long subs = cal.getTime().getTime();
		if((sube - subs) < 0) {
			return 0;
		}
		return ((int)((sube - subs) / 86400000));
	}

	/**
	 * Mute System
	 */
	public int mute = 0;

	public int getMute() {
		return mute;
	}

	public void setMute(int i) {
		mute = i;
	}

	public void incMute() {
		mute++;
	}

	public boolean isMuted() {
		if (mute == 0) {
		return false; } 
		else return true;
	}

	/**
	 * Invisibility @author Yong Min
	 */
	private boolean isInvisible = false;

	public void goInvisible() {
		isInvisible = !isInvisible;
		if(!isInvisible) {
			loggedIn = true; }
	else loggedIn = false;
	}

	public void setInvisible(boolean invisible) {
		isInvisible = invisible;
	}

	public boolean isInvisible() {
		return isInvisible;
	}

	/**
	 * Quest Points @author Yong Min
	 */
	 private int questpoints = 0;

	public void setQuestPoints(int i){
		questpoints = i;
		actionSender.sendQuestPoints();
	}

	public int getQuestPoints() {
		return questpoints;
	}

	/**
	 * Clan System @author Yong Min
	 */
	private Clan myClan;
	private String clanName;
	private long clanNameHash;
	private long lastCreatedClan;
	private ClanInvite clanInvite;
	
	public boolean hasClan() {
		return myClan != null;
	}

	public void setLastCreatedClan() {
		lastCreatedClan = System.currentTimeMillis();
	}

	public void setLastCreatedClan(long l) {
		this.lastCreatedClan = l;
	}

	public long getClanNameHash() {
		return clanNameHash;
	}
	
	public void setClanNameHash(long l) {
		this.clanNameHash = l;
	}

	public boolean canCreateClan() {
		return System.currentTimeMillis() - lastCreatedClan >= 7200000L;
	}

	public void setClanInvite(ClanInvite claninv) {
		System.out.println("setClanInvite, ");
		this.getActionSender().sendMessage("You have been invited to join " + claninv.getClan().getName() + ". Type ::accept to join this clan.");
		this.clanInvite = claninv;
	}

	public boolean declineClanInvite() {
		if(clanInvite == null) {
			return false;
		}
		clanInvite = null;
		return true;
	}

	public boolean isClanLeader() {
		if(!hasClan()) {
			return false;
		}
		return myClan.getOwner().equals(this);
	}

	public void setClan(Clan c) {
		this.myClan = c;
		this.clanName = c.getName();
		this.clanNameHash = DataConversions.usernameToHash(this.clanName);
		super.ourAppearanceChanged = true;
	}

	public Clan getClan() {
		return myClan;
	}
	
	public boolean acceptClanInvite() {
		if(clanInvite == null || !clanName.equals("NULL")) {
			return false;
		}
		myClan = clanInvite.getClan();
		clanName = clanInvite.getClan().getName();
		clanNameHash = DataConversions.usernameToHash(clanName);
		clanInvite.getClan().add(this, " has just joined the clan!");
		clanInvite = null;
		getActionSender().sendMessage("You have just joined " + clanName);
		super.ourAppearanceChanged = true;
		return true;
	}
	
	public void resetClan() {
		myClan = null;
		clanName = "NULL";
		clanNameHash = DataConversions.usernameToHash(clanName);
		super.ourAppearanceChanged = true;
	}

	/**
	 * Kills and Deaths @author Yong Min
	 */
      private int kills = 0;
	private int deaths = 0;

	public int getKills() {
		return kills;
	}

	public int getDeaths() {
		return deaths;
	}

	public void setKills(int i) {
		kills = i;
	}

	public void setDeaths(int i) {
		deaths = i;
	}

	public void incKills() {
		kills++;
	}

	public void incDeaths() {
		deaths++;
	}

	/**
	 * Hidden Ground Items @author Yong Min
	 */
	private boolean hiddenItems = true;

	public boolean getHiddenItem() {
		return hiddenItems;
	}

	public void setHiddenItem(boolean hiddenitem) {
		hiddenItems = hiddenitem;
	}

	/**
	 * Godspell Casts @author Yong Min
	 */
       private int guthixspellcast = 0;
	 private int saradominspellcast = 0;
	 private int zamorakspellcast = 0;

	public void setGuthixSpellCast(int i) {
		guthixspellcast = i;
		actionSender.sendGuthixSpellCast();
	}

	public void setSaradominSpellCast(int i) {
		saradominspellcast = i;
		actionSender.sendSaradominSpellCast();
	}

	public void setZamorakSpellCast(int i) {
		zamorakspellcast = i;
		actionSender.sendZamorakSpellCast();
	}

	public int getGuthixSpellCast() {
		return guthixspellcast;
	}

	public int getSaradominSpellCast() {
		return saradominspellcast;
	}

	public int getZamorakSpellCast() {
		return zamorakspellcast;
	}

	/**
	 * Romeo and Juliet @author Yong Min
	 */
	public int romeostatus = 0;

	public void setRomeoJulietStatus(int romeostatus) {
		this.romeostatus = romeostatus;
	}

	public int getRomeoJulietStatus() {
		return romeostatus;
	}

	public int isRomeoJulietComplete() {
		return romeostatus = 3;
	}

	/**
	 * Sheep Shearer @author Yong Min
	 */
	public int shearerstatus = 0;

	public void setSheepShearerStatus(int shearerstatus) {
		this.shearerstatus = shearerstatus;
	}

	public int getSheepShearerStatus() {
		return shearerstatus;
	}

	public int isSheepShearerComplete() {
		return shearerstatus = 2;
	}

	/**
	 * Pirate's Treasure @author Yong Min
	 */
	public int piratestreasurestatus = 0;

	public void setPiratesTreasureStatus(int piratestreasurestatus) {
		this.piratestreasurestatus = piratestreasurestatus;
	}

	public int getPiratesTreasureStatus() {
		return piratestreasurestatus;
	}

	public int isPiratesTreasureComplete() {
		return piratestreasurestatus = 7;
	}

	/**
	 * Demon Slayer @author Yong Min
	 */
	public int demonslayerstatus = 0;

	public void setDemonSlayerStatus(int demonslayerstatus) {
		this.demonslayerstatus = demonslayerstatus;
	}

	public int getDemonSlayerStatus() {
		return demonslayerstatus;
	}

	public int isDemonSlayerComplete() {
		return demonslayerstatus = 6;
	}

	/**
	 * Druidic Ritual @author Yong Min
	 */
	public int druidicritualstatus = 0;

	public void setDruidicRitualStatus(int druidicritualstatus) {
		this.druidicritualstatus = druidicritualstatus;
	}

	public int getDruidicRitualStatus() {
		return druidicritualstatus;
	}

	public int isDruidicRitualComplete() {
		return druidicritualstatus = 4;
	}

	/**
	 * Witches Potion @author Yong Min
	 */
	public int witchpotstatus = 0;

	public void setWitchPotionStatus(int witchpotstatus) {
		this.witchpotstatus = witchpotstatus;
	}

	public int getWitchPotionStatus() {
		return witchpotstatus;
	}

	public int isWitchPotionComplete() {
		return witchpotstatus = 3;
	}

	/**
	 * Doric's Quest @author Yong Min
	 */
	public int doricsqueststatus = 0;

	public void setDoricsQuestStatus(int doricsqueststatus) {
		this.doricsqueststatus = doricsqueststatus;
	}

	public int getDoricsQuestStatus() {
		return doricsqueststatus;
	}

	public int isDoricsQuestComplete() {
		return doricsqueststatus = 2;
	}

	/**
	 * Cook's Assistant @author Yong Min
	 */
	public int cooksassisstatus = 0;

	public void setCooksAssistantStatus(int cooksassisstatus) {
		this.cooksassisstatus = cooksassisstatus;
	}

	public int getCooksAssistantStatus() {
		return cooksassisstatus;
	}

	public int isCooksAssistantComplete() {
		return cooksassisstatus = 2;
	}

	/**
	 * Imp Catcher @author Yong Min
	 */
	public int impcatcherstatus = 0;

	public void setImpCatcherStatus(int impcatcherstatus) {
		this.impcatcherstatus = impcatcherstatus;
	}

	public int getImpCatcherStatus() {
		return impcatcherstatus;
	}

	public int isImpCatcherComplete() {
		return impcatcherstatus = 2;
	}

	/**
	 * Restless Ghost @author Yong Min
	 */
	public int restlessghoststatus = 0;
	
	public void setRestlessGhostStatus(int restlessghoststatus) {
		this.restlessghoststatus = restlessghoststatus;
	}

	public int getRestlessGhostStatus() {
		return restlessghoststatus;
	}

	public int isRestlessGhostComplete() {
		return restlessghoststatus = 5;
	}
	
	/**
	 * Ernest the Chicken @author Yong Min
	 */
	public int erneststatus = 0;
	
	public void setErnestTheChickenStatus(int erneststatus) {
		this.erneststatus = erneststatus;
	}
	
	public int getErnestTheChickenStatus() {
		return erneststatus;
	}
	
	public int isErnestTheChickenComplete() {
		return erneststatus = 4;
	}
	
	/**
	 * Dragon Slayer @author Yong Min
	 */
	public int dragonslayer = 0;
	
	public void setDragonSlayerStatus(int dragonslayer) {
		this.dragonslayer = dragonslayer;
	}
	
	public int getDragonSlayerStatus() {
		return dragonslayer;
	}
	
	/**
	 * Tutorial Island Progression
	 *		@author Yong Min
	 */
	private int tutorial = 0;

	public void setTutorialStatus(int tutorial) {
		this.tutorial = tutorial;
	}
	
	public int getTutorialStatus() {
		return tutorial;
	}

	/**
	 * Task System @author Yong Min
	 */
	public boolean task = false;

	public int taskReward = 0;
	public int taskRewarde = 0;
	public int itemReward = 0;
	public int remaining = 0;
	public int completedtasks = 0;
	public int taskpoints = 0;

	public String moneyTask = "None, Get a task";

	public final String[] lowTaskNames = { "Unicorn", "Chicken", "cow", "Bear", "Man", "Rat", "mugger", "Giant Spider",
		"Jonny the beard", "skeleton", "Zombie", "Darkwizard", "Goblin", "farmer", "Thief", "Guard", "Scorpion", "Barbarian",
		"Witch", "Ghost", "Wizard", "Highwayman", "Monk", "Dwarf", "Imp", "Dark Warrior", "White wolf", "Chaos Druid"
	};
	
	public final String[] mediumTaskNames = { "Giant bat", "Skeleton", "Giant", "Black Knight", "Hobgoblin", "Gunthor the Brave",
		"Deadly Red spider", "Jailguard", "King Scorpion", "Pirate", "Monk of Zamorak", "Warrior", "Druid", "Poison Scorpion"
	};

	public final String[] highTaskNames = { "Lesser Demon", "White Knight", "Moss Giant", "Ice Giant", "Ice warrior", "Greater Demon",
		"chaos Dwarf", "Baby Blue Dragon", "Grey wolf", "Pack leader", "Guard Dog", "Ice spider", "Animated axe", "Ogre", "Knight", "Hero",
		"Paladin", "Shadow spider"
	};

	public final String[] eliteTaskNames = { "Dragon", "Red Dragon", "Blue Dragon", "Ice queen", "Black Demon", "Black Dragon", "Hellhound",
		"Fire Giant", "King Black Dragon"
	};

	public final int[] lowTaskItems = { 79, 80, 91, 92, 110, 111, 115, 116, 119, 120, 122, 123, 130, 131, 226, 227, 316 };

	public final int[] mediumTaskItems = {  81, 396, 397, 398, 399, 403, 405, 408, 409, 420, 526, 527, 615, 616, 617, 618, 657 };

	public final int[] highTaskItems = { 656, 81, 1288, 1006, 1262, 1213, 1214, 1215, 1216, 1217, 1218, 93, 112, 400, 401, 402, 404 };

	public final int[] eliteTaskItems = { 656, 81, 1288, 1006, 1262, 1213, 1214, 1215, 1216, 1217, 1218, 93, 112, 400, 401, 402, 404, 542,
		543, 544, 593, 594, 597, 795
 	};

	public String moneyTask() {
		return moneyTask;
	}

	public boolean getTask() {
		return task;
	}

	public void setTask(boolean task) {
		this.task = task;
	}

	public void setRandomNPC(Player player) {
		if(getCombatLevel() <= 25) {
			player.setMoneyTask(lowTaskNames[(int)(Math.random() * lowTaskNames.length)]);
			player.setRandomItem(lowTaskItems[(int)(Math.random() * lowTaskItems.length)]);
			player.setRandomCash();
			player.setRandomXp();
			player.setRemaining(DataConversions.random(30, 50));
			player.setTask(true);
			return;
		}
		if(getCombatLevel() <= 60 && getCombatLevel() > 25) {
			player.setMoneyTask(mediumTaskNames[(int)(Math.random() * mediumTaskNames.length)]);
			player.setRandomItem(mediumTaskItems[(int)(Math.random() * mediumTaskItems.length)]);
			player.setRandomCash();
			player.setRandomXp();
			player.setRemaining(DataConversions.random(20, 40));
			player.setTask(true);
			return;
		}
		if(getCombatLevel() <= 100 && getCombatLevel() > 60) {
			player.setMoneyTask(highTaskNames[(int)(Math.random() * highTaskNames.length)]);
			player.setRandomItem(highTaskItems[(int)(Math.random() * highTaskItems.length)]);
			player.setRandomCash();
			player.setRandomXp();
			player.setRemaining(DataConversions.random(10, 25));
			player.setTask(true);
			return;
		}
		if(getCombatLevel() <= 123 && getCombatLevel() > 100) {
			player.setMoneyTask(eliteTaskNames[(int)(Math.random() * eliteTaskNames.length)]);
			player.setRandomItem(eliteTaskItems[(int)(Math.random() * eliteTaskItems.length)]);
			player.setRandomCash();
			player.setRandomXp();
			player.setRemaining(DataConversions.random(5, 15));
			player.setTask(true);
			return;
		}
	}

	public void setRandomCash() {
		if(getCombatLevel() <= 25) {
			this.taskReward = DataConversions.random(1000, 10000);
			return;
		}
		if(getCombatLevel() <= 60 && getCombatLevel() > 25) {
			this.taskReward = DataConversions.random(10000, 20000);
			return;
		}
		if(getCombatLevel() <= 100 && getCombatLevel() > 60) {
			this.taskReward = DataConversions.random(20000, 35000);
			return;
		}
		if(getCombatLevel() <= 123 && getCombatLevel() > 100) {
			this.taskReward = DataConversions.random(35000, 50000);
			return;
		}
	}

	public void setRandomXp() {
		if(getCombatLevel() <= 25) {
			this.taskRewarde = 1; // 1 Experience Token
			return;
		}
		if(getCombatLevel() <= 60 && getCombatLevel() > 25) {
			this.taskRewarde = 2; // 2 Experience Tokens
			return;
		}
		if(getCombatLevel() <= 100 && getCombatLevel() > 60) {
			this.taskRewarde = 4; // 4 Experience Tokens
			return;
		}
		if(getCombatLevel() <= 123 && getCombatLevel() > 100) {
			this.taskRewarde = 10; // 10 Experience Tokens
			return;
		}
	}

	public int getTaskID() {
		if(moneyTask.equals("Skeleton")) {
			return 45;
		}
		return 0;
	}

	public final int getRemaining() {
		return remaining;
	}

	public void setRemaining(int amount) {
		this.remaining = amount;
		actionSender.sendRemaining();
	}

	public String getTaskNPC() {
		return moneyTask;
	}

	public void setRandomItem(int i) {
		this.itemReward = i;
	}

	public void setMoneyTask(String asd) {
		this.moneyTask = asd;
	}

	public int getRandomCash() {
		return taskReward;
	}

	public int getRandomXp() {
		return taskRewarde;
	}

	public int getRandomItem() {
		return itemReward;
	}

	public void setCompletedTasks(int i) {
		completedtasks = i;
		actionSender.sendCompletedTasks();
	}

	public int getCompletedTasks() {
		return completedtasks;
	}

	public void setTaskPoints(int i) {
		taskpoints = i;
		actionSender.sendTaskPoints();
	}

	public int getTaskPoints() {
		return taskpoints;
	}

	/**
	 * Killing Spree @author Yong Min
	 */
	private int killingspree = 0;

	public int getKillingSpree() {
		return killingspree;
	}

	public void setKillingSpree(int i) {
		killingspree = i;
	}

	public void incKillingSpree() {
		killingspree++;
	}

	public void setRequiresOfferUpdate(boolean b) {
		requiresOfferUpdate = b;
	}

	public boolean requiresOfferUpdate() {
		return requiresOfferUpdate;
	}

	public void setStatus(Action a) {
		status = a;
	}

	public Action getStatus() {
		return status;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public boolean [] npcThief = {false, false, false, false, false, false}; // Baker, Silver, Spices, Gem.
	private boolean packetSpam = false;
	public void setSpam(boolean spam) {  packetSpam = spam;   }
	public boolean getSpam() {  return packetSpam;  }

	public boolean tradeDuelThrottling() {
		long now = System.currentTimeMillis();
		if(now - lastTradeDuelRequest > 1000) {
			lastTradeDuelRequest = now;
			return false;
		}
		return true;
	}

	public void addMessageToChatQueue(byte[] messageData) {
		chatQueue.add(new ChatMessage(this, messageData));
		if(chatQueue.size() > 2) {
			destroy(false);
		}
	}

	public ChatMessage getNextChatMessage() {
		return chatQueue.poll();
	}

	public void setArrowFired() {
		lastArrow = System.currentTimeMillis();
	}



	public void setRangeEvent(RangeEvent event) {
		if(isRanging()) {
			resetRange();
		}
		rangeEvent = event;
		rangeEvent.setLastRun(lastArrow);
		world.getDelayedEventHandler().add(rangeEvent);
	}

	public boolean isRanging() {
		return rangeEvent != null;
	}

	public void resetRange() {
		if(rangeEvent != null) {
			rangeEvent.stop();
			rangeEvent = null;
		}
		setStatus(Action.IDLE);
	}

	public boolean canLogout() {
		return !isBusy() && System.currentTimeMillis() - getCombatTimer() > 10000;
	}

	public boolean isFollowing() {
		return followEvent != null && following != null;
	}

	public boolean isFollowing(Mob mob) {
		return isFollowing() && mob.equals(following);
	}

	public void setFollowing(Mob mob) {
		setFollowing(mob, 0);
	}

	public void setFollowing(final Mob mob, final int radius) {
		if(isFollowing()) {
			resetFollowing();
		}
		following = mob;
		followEvent = new DelayedEvent(this, 500) {
			public void run() {
				if(!owner.withinRange(mob) || mob.isRemoved() || (owner.isBusy() && !owner.isDueling())) {
					resetFollowing();
				}
				else if(!owner.finishedPath() && owner.withinRange(mob, radius)) {
					owner.resetPath();
				}
				else if(owner.finishedPath() && !owner.withinRange(mob, radius + 1)) {
					owner.setPath(new Path(owner.getX(), owner.getY(), mob.getX(), mob.getY()));
				}
			}
		};
		world.getDelayedEventHandler().add(followEvent);
	}

	public void resetFollowing() {
		following = null;
		if(followEvent != null) {
			followEvent.stop();
			followEvent = null;
		}
		resetPath();
	}

	public void setSkulledOn(Player player) {
		player.addAttackedBy(this);
		if(System.currentTimeMillis() - lastAttackedBy(player) > 1200000) {
			addSkull(1200000);
		}
	}

	public void setSubscriptionExpires(long expires) {
		subscriptionExpires = expires;
	}

	public int getDaysSubscriptionLeft() {
		long now = (System.currentTimeMillis() / 1000);
		if(subscriptionExpires == 0 || now >= subscriptionExpires) {
			return 0;
		}
		return (int)((subscriptionExpires - now) / 86400);
	}

	public void addPacket(RSCPacket p) {
		long now = System.currentTimeMillis();
		if(now - lastCount > 3000) {
			lastCount = now;
			packetCount = 0;
		}
		if(!DataConversions.inArray(Formulae.safePacketIDs, p.getID()) && packetCount++ >= 60) {
			destroy(false);
		}
		if(lastPackets.size() >= 60) {
			lastPackets.remove();
		}
		lastPackets.addLast(p);
	}

	public List<RSCPacket> getPackets() {
		return lastPackets;
	}

	public boolean isSuspicious() {
		return suspicious;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public int getOwner() {
		return owner;
	}

	public Npc getNpc() {
		return interactingNpc;
	}

	public void setNpc(Npc npc) {//System.out.println
		interactingNpc = npc;
	}

	public void remove() {
		removed = true;
	}

	public boolean initialized() {
		return initialized;
	}

	public void setInitialized() {
		initialized = true;
	}

	public int getDrainRate() {
		return drainRate;
	}

	public void setDrainRate(int rate) {
		drainRate = rate;
	}

	public int getRangeEquip() {
		for(InvItem item : inventory.getItems()) {
			if(item.isWielded() && (DataConversions.inArray(Formulae.bowIDs, item.getID()) || DataConversions.inArray(Formulae.xbowIDs, item.getID()))) {
				return item.getID();
			}
		}
		return -1;
	}

	public void resetAll() {
		resetAllExceptTradeOrDuel();
		resetTrade();
		resetDuel();
	}

	public void resetTrade() {
		Player opponent = getWishToTrade();
		if(opponent != null) {
			opponent.resetTrading();
		}
		resetTrading();
	}

	public void resetDuel() {
		Player opponent = getWishToDuel();
		if(opponent != null) {
			opponent.resetDueling();
		}
		resetDueling();
	}

	public void resetAllExceptTrading() {
		resetAllExceptTradeOrDuel();
		resetDuel();
	}

	public void resetAllExceptDueling() {
		resetAllExceptTradeOrDuel();
		resetTrade();
	}

	private void resetAllExceptTradeOrDuel() {
		if(getMenuHandler() != null) {
			resetMenuHandler();
		}
		if(accessingBank()) {
			resetBank();
		}
		if(accessingShop()) {
			resetShop();
		}
		if(interactingNpc != null) {
			interactingNpc.unblock();
		}
		if(isFollowing()) {
			resetFollowing();
		}
		if(isRanging()) {
			resetRange();
		}
		setStatus(Action.IDLE);
	}

	public void setMenuHandler(MenuHandler menuHandler) {
		menuHandler.setOwner(this);
		this.menuHandler = menuHandler;
	}

	public void setQuestMenuHandler(MenuHandler menuHandler)  {
		this.menuHandler = menuHandler;
		menuHandler.setOwner(this);
		actionSender.sendMenu(menuHandler.getOptions());
	}

	public void resetMenuHandler() {
		menuHandler = null;
		actionSender.hideMenu();
	}

	public MenuHandler getMenuHandler() {
		return menuHandler;
	}

	public boolean accessingShop() {
		return shop != null;
	}

	public void setAccessingShop(Shop shop) {
		this.shop = shop;
		if(shop != null) {
			shop.addPlayer(this);
		}
	}

	public void resetShop() {
		if(shop != null) {
			shop.removePlayer(this);
			shop = null;
			actionSender.hideShop();
		}
	}

	public boolean accessingBank() {
		return inBank;
	}

	public Shop getShop() {
		return shop;
	}

	public void setAccessingBank(boolean b) {
		inBank = b;
	}

	public void resetBank() {
		setAccessingBank(false);
		actionSender.hideBank();
	}

	public Player(IoSession ios) {

		ioSession = ios;
		currentIP = ((InetSocketAddress)ios.getRemoteAddress()).getAddress().getHostAddress();
		currentLogin = System.currentTimeMillis();
		actionSender = new MiscPacketBuilder(this);
		setBusy(true);
	}

	public void setServerKey(long key) {
		sessionKeys[2] = (int)(key >> 32);
		sessionKeys[3] = (int)key;
	}

	public boolean setSessionKeys(int[] keys) {
		boolean valid = (sessionKeys[2] == keys[2] && sessionKeys[3] == keys[3]);
		sessionKeys = keys;
		return valid;
	}
	//	save
	public boolean destroyed() {
		return destroy;
	}

	public void destroy(boolean force) {
		if(destroy) {
			return;
		}
		String user = this.getUsername();
		if(force || canLogout()) {
			if(user == null) {
				destroy = true;
				actionSender.sendLogout();
				return;
			}
			destroy = true;
			actionSender.sendLogout();
			GUI.writeValue(user, "loggedin", "false");
			if(this.isAdmin())
				GameVars.adminsOnline--;
			else if(this.rank == 3 || this.rank == 2)
				GameVars.modsOnline--;
		}
		else {
			final long startDestroy = System.currentTimeMillis();
			world.getDelayedEventHandler().add(new DelayedEvent(this, 3000) {
				public void run() {
					if(owner.canLogout() || (!(owner.inCombat() && owner.isDueling()) && System.currentTimeMillis() - startDestroy > 60000)) {
						owner.destroy(true);
						running = false;
					}
				}
			});
		}
	}

	public int getCombatStyle() {
		return combatStyle;
	}

	public void setCombatStyle(int style) {
		combatStyle = style;
	}

	public boolean muted;

	public void load(String username, String password, int uid, boolean reconnecting) {
		try {
		//String user = username.replaceAll("_");
			File f = new File("players/" + username + ".cfg");
			if(!f.exists()) {
				this.destroy(true);
				return;
			}
			setID(uid);
			this.password = password;
			this.reconnecting = reconnecting;
			usernameHash = DataConversions.usernameToHash(username);
			this.username = DataConversions.hashToUsername(usernameHash);
			//TODO
			//world.getServer().getLoginConnector().getActionSender().playerLogin(this);

			world.getDelayedEventHandler().add(new DelayedEvent(this, 60000) {
				public void run() {
					for(int statIndex = 0;statIndex < 18;statIndex++) {
						if(statIndex == 5) {
							continue;
						}
						int curStat = getCurStat(statIndex);
						int maxStat = getMaxStat(statIndex);
						if(curStat > maxStat) {
							setCurStat(statIndex, curStat - 1);
							getActionSender().sendStat(statIndex);
							checkStat(statIndex);
						}
						else if(curStat < maxStat) {
							setCurStat(statIndex, curStat + 1);
							getActionSender().sendStat(statIndex);
							checkStat(statIndex);
						}
					}
				}


				private void checkStat(int statIndex) {
					if(statIndex != 3 && owner.getCurStat(statIndex) == owner.getMaxStat(statIndex)) {
						owner.getActionSender().sendMessage("Your " + Formulae.statArray[statIndex] + " ability has returned to normal.");
					}
				}			
			});
			drainer = new DelayedEvent(this, Integer.MAX_VALUE) {
				public void run() {
					int curPrayer = getCurStat(5);
					if(getDrainRate() > 0 && curPrayer > 0) {
						incCurStat(5, -1);
						getActionSender().sendStat(5);
						if(curPrayer <= 1) {
							for(int prayerID = 0;prayerID < 14;prayerID++) { //Prayer was < 14
								setPrayer(prayerID, false);
							}
							setDrainRate(0);
							setDelay(Integer.MAX_VALUE);
							getActionSender().sendMessage("You have run out of prayer points. Return to a church to recharge");
							getActionSender().sendPrayers();
						}
					}
				}
			};
			world.getDelayedEventHandler().add(drainer);
			//setOwner(p.readInt()); SQL/PunBB Integration "Owner ID" Which i won't be needing.
			//player.setGroupID(p.readInt()); <-- Same.
			Properties props = new Properties();


			FileInputStream fis = new FileInputStream(f);
			props.load(fis);



			setSubscriptionExpires(0); // No sub atm.
			setLastIP(props.getProperty("ip"));
			setLastLogin(Long.parseLong(props.getProperty("ll"))); // Temporary.

			rank = Integer.parseInt(props.getProperty("rank"));
			if(this.isAdmin())
				GameVars.adminsOnline++;
			else if(this.rank == 3 || this.rank == 2)
				GameVars.modsOnline++;
			setLocation(Point.location(Integer.parseInt(props.getProperty("x")), Integer.parseInt(props.getProperty("y"))), true);
			setFatigue(Integer.parseInt(props.getProperty("fat")));
			setTutorialStatus(Integer.parseInt(props.getProperty("tutorial")));
			impcatcherstatus = Integer.parseInt(props.getProperty("impcatcherstatus"));
			doricsqueststatus = Integer.parseInt(props.getProperty("doricsqueststatus"));
			cooksassisstatus = Integer.parseInt(props.getProperty("cooksassisstatus"));
			romeostatus = Integer.parseInt(props.getProperty("romeostatus"));
			shearerstatus = Integer.parseInt(props.getProperty("shearerstatus"));
			witchpotstatus = Integer.parseInt(props.getProperty("witchpotstatus"));
			druidicritualstatus = Integer.parseInt(props.getProperty("druidicritualstatus"));
			demonslayerstatus = Integer.parseInt(props.getProperty("demonslayerstatus"));
			restlessghoststatus = Integer.parseInt(props.getProperty("restlessghoststatus"));
			piratestreasurestatus = Integer.parseInt(props.getProperty("piratestreasurestatus"));
			erneststatus = Integer.parseInt(props.getProperty("erneststatus"));
			dragonslayer = Integer.parseInt(props.getProperty("dragonslayer"));
			remaining = Integer.parseInt(props.getProperty("remaining"));
			moneyTask = props.getProperty("task");
			System.out.println(moneyTask);
			String s = props.getProperty("taskStatus");
			if(s.trim().equals("true")) setTask(true); else setTask(false);
			taskReward = Integer.parseInt(props.getProperty("taskReward"));
			taskRewarde = Integer.parseInt(props.getProperty("taskRewarde"));
			itemReward = Integer.parseInt(props.getProperty("itemReward"));
			setTaskPoints(Integer.parseInt(props.getProperty("taskpoints")));
			setCompletedTasks(Integer.parseInt(props.getProperty("completedtasks")));
			setGuthixSpellCast(Integer.parseInt(props.getProperty("guthixspellcast")));
			setSaradominSpellCast(Integer.parseInt(props.getProperty("saradominspellcast")));
			setZamorakSpellCast(Integer.parseInt(props.getProperty("zamorakspellcast")));
			setKillingSpree(Integer.parseInt(props.getProperty("killingspree")));
			setMute(Integer.parseInt(props.getProperty("mute")));
			setKills(Integer.parseInt(props.getProperty("kills")));
			setDeaths(Integer.parseInt(props.getProperty("deaths")));
			setQuestPoints(Integer.parseInt(props.getProperty("questpoints")));
			setCombatStyle(Integer.parseInt(props.getProperty("cs")));
			setPrivacySetting(0, Integer.parseInt(props.getProperty("ps0")) == 1);
			setPrivacySetting(1, Integer.parseInt(props.getProperty("ps1")) == 1);
			setPrivacySetting(2, Integer.parseInt(props.getProperty("ps2")) == 1);
			setPrivacySetting(3, Integer.parseInt(props.getProperty("ps3")) == 1);
			/**
	  		 * Clan System
			 */
			clanName = props.getProperty("clan");
			clanNameHash = DataConversions.usernameToHash(clanName);
			if(!clanName.equals("NULL")) {
				Clan c = world.getServer().getClanHandler().getClanByName(clanName);
				if(c.getOwnerName().equals(getUsername())) {
					c.setOwner(this);
				}
				c.add(this, " has logged in!");
				myClan = c;
			}
			setLastCreatedClan(Long.parseLong(props.getProperty("lcc")));
			//System.out.println("load lastCreatedClan: " + lastCreatedClan);
			setGameSetting(0, Integer.parseInt(props.getProperty("gs0")) == 1);
			setGameSetting(2, Integer.parseInt(props.getProperty("gs2")) == 1);
			setGameSetting(3, Integer.parseInt(props.getProperty("gs3")) == 1);
			setGameSetting(4, Integer.parseInt(props.getProperty("gs4")) == 1);
			setGameSetting(5, Integer.parseInt(props.getProperty("gs5")) == 1);
			setGameSetting(6, Integer.parseInt(props.getProperty("gs6")) == 1);

			flag = props.getProperty("flag");
			PlayerAppearance appearance = new PlayerAppearance(
					Integer.parseInt(props.getProperty("a1")),
					Integer.parseInt(props.getProperty("a2")), 
					Integer.parseInt(props.getProperty("a3")), 
					Integer.parseInt(props.getProperty("a4")),
					Integer.parseInt(props.getProperty("a5")),
					Integer.parseInt(props.getProperty("a6")));

			if(!appearance.isValid()) {
				destroy(true);
				getSession().close();
			}
			setAppearance(appearance);
			setWornItems(getPlayerAppearance().getSprites());

			setMale(Integer.parseInt(props.getProperty("male")) == 1);

			long skull = Long.parseLong(props.getProperty("skull"));
			if(skull > 0) {
				addSkull(skull);
			}

			for(int i = 0;i < 18;i++) {
				int exp = Integer.parseInt(props.getProperty("e" + (i + 1)));
				setExp(i, exp);
				setMaxStat(i, Formulae.experienceToLevel(exp));
				setCurStat(i, Integer.parseInt(props.getProperty("c" + (i + 1))));
			}
			setCombatLevel(Formulae.getCombatlevel(getMaxStats()));

			int count = Integer.parseInt(props.getProperty("fcount"));
			for(int i=0; i < count; i++) {
				this.getFriendList().add(props.getProperty("f" + i));
			}
			Inventory inventory = new Inventory(this);
			int invCount = Integer.parseInt(props.getProperty("icount"));
			for(int i = 0;i < invCount;i++) {
				int id = Integer.parseInt(props.getProperty("i" + i));
				int amount = Integer.parseInt(props.getProperty("ia" + i));
				int wear = Integer.parseInt(props.getProperty("iw" + i));
				if(id != 7000) {
					InvItem item = new InvItem(id, amount);
					if(wear == 1 && item.isWieldable()) {
						item.setWield(true);
						updateWornItems(item.getWieldableDef().getWieldPos(), item.getWieldableDef().getSprite());
					}
					inventory.add(item);

				}
			}
			setInventory(inventory);

			Bank bank = new Bank();
			int bnkCount = Integer.parseInt(props.getProperty("bcount"));
			for(int i = 0;i < bnkCount;i++) {
				int id = Integer.parseInt(props.getProperty("b" + i));
				int amount = Integer.parseInt(props.getProperty("ba" + i));
				if(id != 7000)
					bank.add(new InvItem(id, amount));
			}
			setBank(bank);
			if(!this.bad_login) {
				fis.close();	
				FileOutputStream fos = new FileOutputStream(f);
				props.setProperty("loggedin", "true");
				props.store(fos, "Character Data.");
				fos.close();
			}


			/* End of loading methods */

			world.registerPlayer(this);

			updateViewedPlayers();
			updateViewedObjects();

			org.rscdaemon.server.packetbuilder.client.MiscPacketBuilder sender = getActionSender();
			sender.sendServerInfo();
			sender.sendFatigue();
			sender.sendTutorialStatus();
			sender.sendImpCatcherComplete();
			sender.sendRomeoJulietComplete();
			sender.sendSheepShearerComplete();
			sender.sendWitchPotionComplete();
			sender.sendDoricsQuestComplete();
			sender.sendCooksAssistantComplete();
			sender.sendDruidicRitualComplete();
			sender.sendDemonSlayerComplete();
			sender.sendRestlessGhostComplete();
			sender.sendPiratesTreasureComplete();
			sender.sendErnestTheChickenComplete();
			sender.sendGuthixSpellCast();
			sender.sendSaradominSpellCast();
			sender.sendZamorakSpellCast();
			sender.sendKillingSpree();
			sender.sendMute();
			sender.sendTaskPoints();
			sender.sendCompletedTasks();
			sender.sendTaskStatus();
			sender.sendExperienceReward();
			sender.sendItemReward();
			sender.sendCashReward();
			sender.sendRemaining();
			sender.sendMoneyTask();
			sender.sendQuestPoints();
			sender.sendKills();
			sender.sendDeaths();
			sender.sendWorldInfo();
			sender.sendInventory();
			sender.sendEquipmentStats();
			sender.sendStats();
			sender.sendPrivacySettings();
			sender.sendGameSettings();
			sender.sendFriendList();
			sender.sendIgnoreList();
			sender.sendCombatStyle();



			GUI.populateWorldList();
			for(Player p : world.getPlayers()) {
				if(p.isFriendsWith(this.getUsername())) {
					p.getActionSender().sendFriendUpdate(this.getUsernameHash(), org.rscdaemon.server.util.Config.SERVER_NUM);
				}
			}
			for(String player : getFriendList()) {
				Player p = world.getPlayer(DataConversions.usernameToHash(player));
				if(p != null) {
					sender.sendFriendUpdate(p.getUsernameHash(), Config.SERVER_NUM);
				} else {
					sender.sendFriendUpdate(DataConversions.usernameToHash(player), 0);
				}
			}

			sender.sendMessage("    "); // ROFL at this, its to stop the stupid friends list saying xx logged out when someone logs in, ill fix it up later
			sender.sendMessage("    ");
			sender.sendMessage("    ");
			sender.sendMessage("@yel@Welcome to @whi@" + GameVars.serverName);
			sender.sendMessage("@yel@Powered by: @whi@" + "EasyRSC Emulator v" + (double)GameVars.projectVersion);
			sender.sendMessage("@yel@Online Players: @whi@" + (GameVars.usersOnline + 1) + "  @yel@Peak: @whi@" + (GameVars.userPeak + 1));
			world.getServer().addLogin();

			int timeTillShutdown = world.getServer().timeTillShutdown();
			if(timeTillShutdown > -1) {
				sender.startShutdown((int)(timeTillShutdown / 1000));
			}
			int timeTillDropParty = world.getServer().timeTillDropParty();
			if(timeTillDropParty > -1) {
				sender.startDropParty((int)(timeTillDropParty / 1000));
			}
			int timeTillWildSwitch = world.getServer().timeTillWildSwitch();
			if(timeTillWildSwitch > -1) {
				sender.startWildernessSwitch((int)(timeTillWildSwitch / 1000));
			}

			if(getLastLogin() == 0) {
				setChangingAppearance(true);
				sender.sendAppearanceScreen();
			}
			setLastLogin(System.currentTimeMillis());
			sender.sendLoginBox();

			setLoggedIn(true);
			setBusy(false);
			RSCPacketBuilder pb = new RSCPacketBuilder();
			pb.setBare(true);
			pb.addByte((byte)0);
			getSession().write(pb.toPacket());
		} catch (Exception e) {
			e.printStackTrace();
			Logger.print(e.toString(), 1);
		}

	}



	public void resetTrading() {
		if(isTrading()) {
			actionSender.sendTradeWindowClose();
			setStatus(Action.IDLE);
		}
		setWishToTrade(null);
		setTrading(false);
		setTradeOfferAccepted(false);
		setTradeConfirmAccepted(false);
		resetTradeOffer();
	}

	public void resetDueling() {
		if(isDueling()) {
			actionSender.sendDuelWindowClose();
			setStatus(Action.IDLE);
		}
		inDuel = false;
		setWishToDuel(null);
		setDueling(false);
		setDuelOfferAccepted(false);
		setDuelConfirmAccepted(false);
		resetDuelOffer();
		clearDuelOptions();
	}
	//mute
	public void clearDuelOptions() {
		for(int i = 0;i < 4;i++) {
			duelOptions[i] = false;
		}	}

	public void save() {
		try {

			if(!this.bad_login) {
				String username = this.getUsername().replaceAll(" ", "_");
				File f = new File("players/" + username.toLowerCase() + ".cfg");
				//System.out.println("test test 1");
				Properties pr = new Properties();

				FileInputStream fis = new FileInputStream(f);
				pr.load(fis);
				fis.close();


				pr.setProperty("rank",  "" + this.rank);
				pr.setProperty("x",  "" + this.getLocation().getX());
				pr.setProperty("y", "" + this.getLocation().getY());
				pr.setProperty("fat", "" + this.getFatigue());
				pr.setProperty("tutorial", "" + this.getTutorialStatus());
				pr.setProperty("remaining", "" + this.remaining);
				pr.setProperty("task", "" + this.moneyTask);
				pr.setProperty("taskStatus", "" + this.getTask());
				pr.setProperty("taskpoints", "" + this.getTaskPoints());
				pr.setProperty("taskReward", "" + this.getRandomCash());
				pr.setProperty("taskRewarde", "" + this.getRandomXp());
				pr.setProperty("itemReward", "" + this.getRandomItem());
				pr.setProperty("completedtasks", "" + this.getCompletedTasks());
				pr.setProperty("impcatcherstatus", "" + this.getImpCatcherStatus());
				pr.setProperty("romeostatus", "" + this.getRomeoJulietStatus());
				pr.setProperty("shearerstatus", "" + this.getSheepShearerStatus());
				pr.setProperty("witchpotstatus", "" + this.getWitchPotionStatus());
				pr.setProperty("doricsqueststatus", "" + this.getDoricsQuestStatus());
				pr.setProperty("cooksassisstatus", "" + this.getCooksAssistantStatus());
				pr.setProperty("druidicritualstatus", "" + this.getDruidicRitualStatus());
				pr.setProperty("demonslayerstatus", "" + this.getDemonSlayerStatus());
				pr.setProperty("restlessghoststatus", "" + this.getRestlessGhostStatus());
				pr.setProperty("piratestreasurestatus", "" + this.getPiratesTreasureStatus());
				pr.setProperty("erneststatus", "" + this.getErnestTheChickenStatus());
				pr.setProperty("dragonslayer", "" + this.getDragonSlayerStatus());
				pr.setProperty("guthixspellcast", "" + this.getGuthixSpellCast());
				pr.setProperty("saradominspellcast", "" + this.getSaradominSpellCast());
				pr.setProperty("zamorakspellcast", "" + this.getZamorakSpellCast());
				pr.setProperty("killingspree", "" + this.getKillingSpree());
				pr.setProperty("mute", "" + this.getMute());
				pr.setProperty("kills", "" + this.getKills());
				pr.setProperty("deaths", "" + this.getDeaths());
				pr.setProperty("questpoints", "" + this.getQuestPoints());
				pr.setProperty("ip", "" + this.getLastIP());
				pr.setProperty("ll", "" + this.getLastLogin());
				pr.setProperty("cs", "" + this.getCombatStyle());
				pr.setProperty("ps0", "" + (this.getPrivacySetting(0) ? 1 : 0));
				pr.setProperty("ps1", "" + (this.getPrivacySetting(1) ? 1 : 0));
				pr.setProperty("ps2", "" + (this.getPrivacySetting(2) ? 1 : 0));
				pr.setProperty("ps3", "" + (this.getPrivacySetting(3) ? 1 : 0));
				pr.setProperty("gs0", "" + (this.getGameSetting(0) ? 1 : 0));
				pr.setProperty("gs2", "" + (this.getGameSetting(2) ? 1 : 0));
				pr.setProperty("gs3", "" + (this.getGameSetting(3) ? 1 : 0));
				pr.setProperty("gs4", "" + (this.getGameSetting(4) ? 1 : 0));

				pr.setProperty("flag", (flag == null ? "" : flag));
				pr.setProperty("gs5", "" + (this.getGameSetting(5) ? 1 : 0));
				pr.setProperty("gs6", "" + (this.getGameSetting(6) ? 1 : 0));
				pr.setProperty("a1", "" + this.appearance.getHairColour());
				pr.setProperty("a2", "" + this.appearance.getTopColour());
				pr.setProperty("a3", "" + this.appearance.getTrouserColour());
				pr.setProperty("a4", "" + this.appearance.getSkinColour());
				pr.setProperty("a5", "" + this.appearance.head);
				pr.setProperty("a6", "" + this.appearance.body);
				pr.setProperty("male", "" + (this.isMale() ? 1 : 0));
				pr.setProperty("skull", "" + (this.getSkullTime() > 0 ? this.getSkullTime() : 0));
				
				/**
	 			 * Clan System @author Yong Min
				 */
				pr.setProperty("lcc", String.valueOf(lastCreatedClan));
				//System.out.println("Set lastCreatedClan: " + lastCreatedClan);
				pr.setProperty("clan", clanName);

				for(int i=0; i < 18; i++) {
					pr.setProperty("c" + (i + 1), "" + this.getCurStat(i));
					pr.setProperty("e" + (i + 1), "" + this.getExp(i));
				}



				int count = this.getInventory().size();
				pr.setProperty("icount", "" + count);
				for(int i=0; i < count; i++) {
					InvItem item = this.getInventory().get(i);			
					pr.setProperty("i" + i, "" + item.getID());
					pr.setProperty("ia" + i, "" + item.getAmount());
					pr.setProperty("iw" + i, "" + (item.isWielded() ? 1 : 0));			
				}

				count = this.getFriendList().size();
				pr.setProperty("fcount", "" + count);
				for(int i=0; i < count; i++) {
					pr.setProperty("f" + i, "" + this.getFriendList().get(i));
				}

				count = this.getBank().size();
				pr.setProperty("bcount", "" + count);
				for(int i=0; i < count; i++) {
					InvItem item = this.getBank().get(i);
					pr.setProperty("b" + i, "" + item.getID());
					pr.setProperty("ba" + i, "" + item.getAmount());
				}

				FileOutputStream fos = new FileOutputStream(f);
				pr.store(fos, "Character Data.");
				fos.close();

			}
		} catch (IOException e) {

			System.out.println(e);
		}
	}

	public void setCharged() {
		lastCharge = System.currentTimeMillis();
	}

	public boolean isCharged() {
		return System.currentTimeMillis() - lastCharge > 600000;
	}

	public boolean wielding(int id) {
		ListIterator iterator = getInventory().iterator();
		for(int slot = 0; iterator.hasNext(); slot++) {
			InvItem item = (InvItem)iterator.next();
			if(item.isWielded() && item.getID() == id) {
				return true;
				}
		}
		return false;
	}

	public boolean canReport() {
		return System.currentTimeMillis() - lastReport > 60000;
	}

	public void setLastReport() {
		lastReport = System.currentTimeMillis();
	}

	public void killedBy(Mob mob) {
		killedBy(mob, false);
	}

	public void killedBy(Mob mob, boolean stake) {
		boolean drop = true;
		if(!loggedIn) {
			Logger.error(username + " not logged in, but killed!");
			return;
		}
		if(mob instanceof Player) {
			Player player = (Player)mob;
			player.getActionSender().sendMessage("You have defeated " + getUsername() + "!");
			if(player.getLocation().wildernessLevel() > 0) {
				try {				
					String notify = "";
					if(player.flag != null)
						if((player.flag.length() == 2) && (!player.flag.equals("--")))
							notify += "#f" + player.flag + "#";
					notify = notify + "@or1@" + player.getUsername() + "@whi@ just killed @or1@" + getUsername() + "@whi@ with #i";
					int wep = player.getEquippedWeaponID();
					if(wep == -1)
						wep = 16;
					if(wep > 1289)
						wep = 100;
					notify = notify + wep + "# ";
					
					String fla = null;
					if(flag != null)
						if((flag.length() == 2) && (!flag.equals("--")))
							fla = flag;
					if(fla != null)
						notify = notify + "#f" + fla + "#";
					for(Player p : World.getWorld().getPlayers()) {
						p.getActionSender().sendMessage("%r-" + "@red@KILL NOTIFY@whi@: " + notify);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				player.kills++;
				player.killingspree++;
				deaths++;
				actionSender.sendDeaths();
		}
		player.getActionSender().sendSound("victory");

		ArrayList<Player> playersToSend = new ArrayList<Player>();

		for(Player p : world.getPlayers())
			playersToSend.add(p);

		for(Player pl : playersToSend)
		if(player.getKillingSpree() == 1) {
			pl.getActionSender().sendMessage("[@red@NOOBscape@whi@] @or1@" + player.getUsername() + "@whi@ has just gained the " + killingSpreeRank[1] + " killing spree rank!");
		}
		else if(player.getKillingSpree() == 5) {
			pl.getActionSender().sendMessage("[@red@NOOBscape@whi@] @or1@" + player.getUsername() + "@whi@ now has a killing spree of: @or1@" + player.getKillingSpree());
			pl.getActionSender().sendMessage("[@red@NOOBscape@whi@] @or1@" + player.getUsername() + "@whi@ has just gained the " + killingSpreeRank[2] + " killing spree rank!");
		}
		else if(player.getKillingSpree() == 10) {
			pl.getActionSender().sendMessage("[@red@NOOBscape@whi@] @or1@" + player.getUsername() + "@whi@ now has a killing spree of: @or1@" + player.getKillingSpree());
			pl.getActionSender().sendMessage("[@red@NOOBscape@whi@] @or1@" + player.getUsername() + "@whi@ has just gained the " + killingSpreeRank[3] + " killing spree rank!");
		}
		else if(player.getKillingSpree() == 15) {
			pl.getActionSender().sendMessage("[@red@NOOBscape@whi@] @or1@" + player.getUsername() + "@whi@ now has a killing spree of: @or1@" + player.getKillingSpree());
			pl.getActionSender().sendMessage("[@red@NOOBscape@whi@] @or1@" + player.getUsername() + "@whi@ has just gained the " + killingSpreeRank[4] + " killing spree rank!");
		}
		else if(player.getKillingSpree() == 20) {
			pl.getActionSender().sendMessage("[@red@NOOBscape@whi@] @or1@" + player.getUsername() + "@whi@ now has a killing spree of: @or1@" + player.getKillingSpree());
			pl.getActionSender().sendMessage("[@red@NOOBscape@whi@] @or1@" + player.getUsername() + "@whi@ has just gained the " + killingSpreeRank[5] + " killing spree rank!"); 
		} else {
			pl.getActionSender().sendMessage("@or1@" + player.getUsername() + "@whi@ has just @red@owned @or1@" + getUsername() + "@whi@ !");
			pl.getActionSender().sendMessage("@or1@" + getUsername() + "@whi@'s killing spree of " + getKillingSpree() + " has now ended.");
		}
			world.getDelayedEventHandler().add(new MiniEvent(player) {
				public void action() {
					owner.getActionSender().sendScreenshot();
                                        owner.actionSender.sendKills();
							owner.actionSender.sendKillingSpree();
				}
			});//setNpc
			//world.getServer().getLoginConnector().getActionSender().logKill(player.getUsernameHash(), usernameHash, stake);
			if(world.getServer().pvpIsRunning() && world.getPvpSize() == 2 && world.getPvpEntry(this)) {
				world.setWinner(player);
				world.removePvpEntry(player);
				world.removePvpEntry(this);
				player.getInventory().add(new InvItem(10, world.getJackPot()));
				player.getActionSender().sendInventory();
				world.getServer().stopDuel();
				world.clearJackPot();
				player.teleport(220, 445, false);
				drop=false;
			}
			else if(world.getServer().pvpIsRunning()){
				world.removePvpEntry(this);
				drop=false;
			}
		}
		Mob opponent = super.getOpponent();
		if(opponent != null) {
			opponent.resetCombat(CombatState.WON);
		}
		actionSender.sendDied();
		for(int i = 0;i < 18;i++) {
			curStat[i] = maxStat[i];
			actionSender.sendStat(i);
		}

		Player player = mob instanceof Player ? (Player)mob : null;
		if(stake) {
			for(InvItem item : duelOffer) {
				InvItem affectedItem = getInventory().get(item);
				if(affectedItem == null) {
					setSuspiciousPlayer(true);
					Logger.error("Missing staked item [" + item.getID() + ", " + item.getAmount() + "] from = " + usernameHash + "; to = " + player.getUsernameHash() + ";");
					continue;
				}
				if(affectedItem.isWielded()) {
					affectedItem.setWield(false);
					updateWornItems(affectedItem.getWieldableDef().getWieldPos(), getPlayerAppearance().getSprite(affectedItem.getWieldableDef().getWieldPos()));
				}
				getInventory().remove(item);
				world.registerItem(new Item(item.getID(), getX(), getY(), item.getAmount(), player));
			}
		}
		else {
			inventory.sort();
			ListIterator<InvItem> iterator = inventory.iterator();
			if(!isSkulled()) {
				for(int i = 0;i < 3 && iterator.hasNext();i++) {
					if((iterator.next()).getDef().isStackable()) {
						iterator.previous();
						break;
					}
				}
			}
			if(activatedPrayers[8] && iterator.hasNext()) {
				if(((InvItem)iterator.next()).getDef().isStackable()) {
					iterator.previous();
				}
			}
			for(int slot = 0; iterator.hasNext(); slot++) {
				if(isAdmin()) {
					break;
				}
				InvItem item = (InvItem)iterator.next();
				if(item.isWielded()) {
					item.setWield(false);
					updateWornItems(item.getWieldableDef().getWieldPos(), appearance.getSprite(item.getWieldableDef().getWieldPos()));
				}
				iterator.remove();
				world.registerItem(new Item(item.getID(), getX(), getY(), item.getAmount(), player));
			}
			removeSkull();
		}
		world.registerItem(new Item(20, getX(), getY(), 1, player));

		for(int x = 0;x < activatedPrayers.length;x++) {
			if(activatedPrayers[x]) {
				removePrayerDrain(x);
				activatedPrayers[x] = false;
			}
		}
		actionSender.sendPrayers();

		setLocation(Point.location(122, 647), true);
		Player affectedPlayer = world.getPlayer(usernameHash);
		Collection<Player> allWatched = watchedPlayers.getAllEntities();
		for (Player p : allWatched) {
			p.removeWatchedPlayer(this);
		}

		resetPath();
		resetCombat(CombatState.LOST);
		actionSender.sendWorldInfo();
		actionSender.sendEquipmentStats();
		actionSender.sendInventory();
			affectedPlayer.setKillingSpree(affectedPlayer.getKillingSpree() - affectedPlayer.getKillingSpree());
		affectedPlayer.actionSender.sendKillingSpree();
	}

	public void addAttackedBy(Player p) {
		attackedBy.put(p.getUsernameHash(), System.currentTimeMillis());
	}

	public long lastAttackedBy(Player p) {
		Long time = attackedBy.get(p.getUsernameHash());
		if(time != null) {
			return time;
		}
		return 0;
	}

	public void setCastTimer() {
		lastSpellCast = System.currentTimeMillis();
	}

	public void setSpellFail() {
		lastSpellCast = System.currentTimeMillis() + 20000;
	}

	public int getSpellWait() {
		return DataConversions.roundUp((double)(1200 - (System.currentTimeMillis() - lastSpellCast)) / 1000D);
	}

	public long getCastTimer() {
		return lastSpellCast;
	}

	public boolean castTimer() {
		return System.currentTimeMillis() - lastSpellCast > 1200;
	}
	//destroy
	public boolean checkAttack(Mob mob, boolean missile) {
		if(mob instanceof Player) {
			Player victim = (Player)mob;
			if((inCombat() && isDueling()) && (victim.inCombat() && victim.isDueling())) {
				Player opponent = (Player)getOpponent();
				if(opponent != null && victim.equals(opponent)) {
					return true;
				}
			}
			if(System.currentTimeMillis() - mob.getCombatTimer() < (mob.getCombatState() == CombatState.RUNNING || mob.getCombatState() == CombatState.WAITING ? 3000 : 500) && !mob.inCombat()) {
				return false;
			}
         	if(victim.hasClan() && victim.clanName.equals(clanName)) {
         		getActionSender().sendMessage("You can't attack members in your clan!");
         		return false;
         	}
			int myWildLvl = getLocation().wildernessLevel();
			int victimWildLvl = victim.getLocation().wildernessLevel();
			if(myWildLvl < 1 || victimWildLvl < 1) {
				actionSender.sendMessage("You cannot attack other players outside of the wilderness!");
				return false;
			}
			if((victim.isAdmin()) || (victim.isMod()) || (victim.isPMod()) || (victim.isEvent()) || (victim.isDeveloper())) {
				actionSender.sendMessage("@red@NOOBSCAPE: @whi@You can't attack NOOBScape staff!");
				resetFollowing();
				return false;
			}
			int combDiff = Math.abs(getCombatLevel() - victim.getCombatLevel());
			if(combDiff > myWildLvl) {
				actionSender.sendMessage("You must move to at least level " + combDiff + " wilderness to attack " + victim.getUsername() + "!");
				return false;
			}
			if(combDiff > victimWildLvl) {
				actionSender.sendMessage(victim.getUsername() + " is not in high enough wilderness for you to attack!");
				return false;
			}
			return true;
		}
		else if(mob instanceof Npc) {
			Npc victim = (Npc)mob;
			if(!victim.getDef().isAttackable()) {
				setSuspiciousPlayer(true);
				return false;
			}
			return true;
		}
		return true;
	}

	public void informOfBubble(Bubble b) {
		bubblesNeedingDisplayed.add(b);
	}

	public List<Bubble> getBubblesNeedingDisplayed() {
		return bubblesNeedingDisplayed;
	}

	public void clearBubblesNeedingDisplayed() {
		bubblesNeedingDisplayed.clear();
	}

	public void informOfChatMessage(ChatMessage cm) {
		chatMessagesNeedingDisplayed.add(cm);
	}

	public void sayMessage(String msg, Mob to) {
		ChatMessage cm = new ChatMessage(this, msg, to);
		chatMessagesNeedingDisplayed.add(cm);
	}

	public void informOfNpcMessage(ChatMessage cm) {
		npcMessagesNeedingDisplayed.add(cm);
	}

	public List<ChatMessage> getNpcMessagesNeedingDisplayed() {
		return npcMessagesNeedingDisplayed;
	}

	public List<ChatMessage> getChatMessagesNeedingDisplayed() {
		return chatMessagesNeedingDisplayed;
	}

	public void clearNpcMessagesNeedingDisplayed() {
		npcMessagesNeedingDisplayed.clear();
	}

	public void clearChatMessagesNeedingDisplayed() {
		chatMessagesNeedingDisplayed.clear();
	}

	public void informOfModifiedHits(Mob mob) {
		if(mob instanceof Player) {
			playersNeedingHitsUpdate.add((Player)mob);
		}
		else if(mob instanceof Npc) {
			npcsNeedingHitsUpdate.add((Npc)mob);
		}
	}

	public List<Player> getPlayersRequiringHitsUpdate() {
		return playersNeedingHitsUpdate;
	}

	public List<Npc> getNpcsRequiringHitsUpdate() {
		return npcsNeedingHitsUpdate;
	}

	public void clearPlayersNeedingHitsUpdate() {
		playersNeedingHitsUpdate.clear();
	}

	public void clearNpcsNeedingHitsUpdate() {
		npcsNeedingHitsUpdate.clear();
	}

	public void informOfProjectile(Projectile p) {
		projectilesNeedingDisplayed.add(p);
	}

	public List<Projectile> getProjectilesNeedingDisplayed() {
		return projectilesNeedingDisplayed;
	}

	public void clearProjectilesNeedingDisplayed() {
		projectilesNeedingDisplayed.clear();
	}

	public void addPrayerDrain(int prayerID) {
		PrayerDef prayer = EntityHandler.getPrayerDef(prayerID);
		drainRate += prayer.getDrainRate();
		drainer.setDelay((int)(240000 / drainRate));
	}

	public void removePrayerDrain(int prayerID) {
		PrayerDef prayer = EntityHandler.getPrayerDef(prayerID);
		drainRate -= prayer.getDrainRate();
		if(drainRate <= 0) {
			drainRate = 0;
			drainer.setDelay(Integer.MAX_VALUE);
		}
		else {
			drainer.setDelay((int)(240000 / drainRate));
		}
	}

	public boolean isFriendsWith(String username) {
		return friendList.contains(username);
	}

	public boolean isIgnoring(String user) {
		return ignoreList.contains(user);
	}

	public List<String> getFriendList() {
		return friendList;
	}

	public HashSet<String> getIgnoreList() {
		return ignoreList;
	}

	public void removeFriend(String user) {
		friendList.remove(user);
	}

	public void removeIgnore(String user) {
		ignoreList.remove(user);
	}

	public void addFriend(String name) {
		if(friendList.size() >= 50)
			getActionSender().sendMessage("Sorry your friends list is Full.");
		else
			friendList.add(name);
	}

	public void addIgnore(String user) {
		ignoreList.add(user);
	}

	public int friendCount() {
		return friendList.size();
	}

	public int ignoreCount() {
		return ignoreList.size();
	}

	public void setTradeConfirmAccepted(boolean b) {
		tradeConfirmAccepted = b;
	}

	public void setDuelConfirmAccepted(boolean b) {
		duelConfirmAccepted = b;
	}

	public boolean isTradeConfirmAccepted() {
		return tradeConfirmAccepted;
	}

	public boolean isDuelConfirmAccepted() {
		return duelConfirmAccepted;
	}

	public void setTradeOfferAccepted(boolean b) {
		tradeOfferAccepted = b;
	}

	public void setDuelOfferAccepted(boolean b) {
		duelOfferAccepted = b;
	}

	public boolean isTradeOfferAccepted() {
		return tradeOfferAccepted;
	}

	public boolean isDuelOfferAccepted() {
		return duelOfferAccepted;
	}

	public void resetTradeOffer() {
		tradeOffer.clear();
	}
	public void resetDuelOffer() {
		duelOffer.clear();
	}

	public void addToTradeOffer(InvItem item) {
		tradeOffer.add(item);
	}

	public void addToDuelOffer(InvItem item) {
		duelOffer.add(item);
	}

	public ArrayList<InvItem> getTradeOffer() {
		return tradeOffer;
	}

	public ArrayList<InvItem> getDuelOffer() {
		return duelOffer;
	}

	public void setTrading(boolean b) {
		isTrading = b;
	}

	public void setDueling(boolean b) {
		isDueling = b;
	}

	public boolean isTrading() {
		return isTrading;
	}

	public boolean isDueling() {
		return isDueling;
	}

	public void setWishToTrade(Player p) {
		wishToTrade = p;
	}

	public void setWishToDuel(Player p) {
		wishToDuel = p;
	}

	public Player getWishToTrade() {
		return wishToTrade;
	}

	public Player getWishToDuel() {
		return wishToDuel;
	}
	//	IoSession
	public void setDuelSetting(int i, boolean b) {
		duelOptions[i] = b;
	}

	public boolean getDuelSetting(int i) {
		try {
			for(InvItem item : duelOffer) {
				if(DataConversions.inArray(Formulae.runeIDs, item.getID())) {
					setDuelSetting(1, true);
					break;
				}
			}
			for(InvItem item : wishToDuel.getDuelOffer()) {
				if(DataConversions.inArray(Formulae.runeIDs, item.getID())) {
					setDuelSetting(1, true);
					break;
				}
			}
		}
		catch(Exception e) { }
		return duelOptions[i];
	}

	public void setMale(boolean male) {
		maleGender = male;
	}

	public boolean isMale() {
		return maleGender;
	}

	public void setChangingAppearance(boolean b) {
		changingAppearance = b;
	}

	public boolean isChangingAppearance() {
		return changingAppearance;
	}

	public boolean isReconnecting() {
		return reconnecting;
	}

	public void setLastLogin(long l) {
		lastLogin = l;
	}

	public long getLastLogin() {
		return lastLogin;
	}

	public int getDaysSinceLastLogin() {
		long now = Calendar.getInstance().getTimeInMillis() / 1000;
		return (int)((now - lastLogin) / 86400);
	}

	public long getCurrentLogin() {
		return currentLogin;
	}

	public void setLastIP(String ip) {
		lastIP = ip;
	}

	public String getCurrentIP() {
		return currentIP;
	}

	public String getLastIP() {
		return lastIP;
	}

	public void setGroupID(int id) {
		rank = id;
	}

	public int getGroupID() {
		return rank;
	}

	public boolean isSubscriber() {
		return rank == 1 || isEvent() || isDeveloper() || isPMod() || isMod() || isAdmin();
	}
	
	public boolean isStaff() {
		return isEvent() || isDeveloper() || isPMod() || isMod() || isAdmin();
	}

	public boolean isEvent() {
		return rank == 7;
	}

	public boolean isDeveloper() {
		return rank == 8;
	}

	public boolean isPMod() {
		return rank == 2;
	}

	public boolean isMod() {
		return rank == 3;
	}

	public boolean isAdmin() {
		return rank == 4;
	}

	public int getArmourPoints() {
		int points = 1;
		for(InvItem item : inventory.getItems()) {
			if(item.isWielded()) {
				points += item.getWieldableDef().getArmourPoints();
			}
		}
		return points < 1 ? 1 : points;
	}

	public int getWeaponAimPoints() {
		int points = 1;
		for(InvItem item : inventory.getItems()) {
			if(item.isWielded()) {
				points += item.getWieldableDef().getWeaponAimPoints();
			}
		}
		return points < 1 ? 1 : points;
	}

	public int getWeaponPowerPoints() {
		int points = 1;
		for(InvItem item : inventory.getItems()) {
			if(item.isWielded()) {
				points += item.getWieldableDef().getWeaponPowerPoints();
			}
		}
		return points < 1 ? 1 : points;
	}

	public int getMagicPoints() {
		int points = 1;
		for(InvItem item : inventory.getItems()) {
			if(item.isWielded()) {
				points += item.getWieldableDef().getMagicPoints();
			}
		}
		return points < 1 ? 1 : points;
	}

	public int getPrayerPoints() {
		int points = 1;
		for(InvItem item : inventory.getItems()) {
			if(item.isWielded()) {
				points += item.getWieldableDef().getPrayerPoints();
			}
		}
		return points < 1 ? 1 : points;
	}

	public int getRangePoints() {
		int points = 1;
		for(InvItem item : inventory.getItems()) {
			if(item.isWielded()) {
				points += item.getWieldableDef().getRangePoints();
			}
		}
		return points < 1 ? 1 : points;
	}

	public MiscPacketBuilder getActionSender() {
		return actionSender;
	}

	public int getEquippedWeaponID() {
		for (InvItem i : getInventory().getItems()) {
			if (i.isWielded()
					&& (i.getWieldableDef().getWieldPos() == 4 || i
							.getWieldableDef().getWieldPos() == 3))
				return i.getID();
		}
		return -1;
	}

	public int[] getWornItems() {
		return wornItems;
	}

	public void updateWornItems(int index, int id) {
		wornItems[index] = id;
		super.ourAppearanceChanged = true;
	}

	public void setWornItems(int[] worn) {
		wornItems = worn;
		super.ourAppearanceChanged = true;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory i) {
		inventory = i;
	}

	public Bank getBank() {
		return bank;
	}

	public void setBank(Bank b) {
		bank = b;
	}

	public void setGameSetting(int i, boolean b) {
		gameSettings[i] = b;
	}

	public boolean getGameSetting(int i) {
		return gameSettings[i];
	}

	public void setPrivacySetting(int i, boolean b) {
		privacySettings[i] = b;
	}

	public boolean getPrivacySetting(int i) {
		return privacySettings[i];
	}

	public long getLastPing() {
		return lastPing;
	}

	public IoSession getSession() {
		return ioSession;
	}

	public boolean loggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		if(loggedIn) {
			currentLogin = System.currentTimeMillis();
		}
		this.loggedIn = loggedIn;
	}

	public String getUsername() {
		return username;
	}

	public long getUsernameHash() {
		return usernameHash;
	}

	public String getPassword() {
		return password;
	}

	public void ping() {
		lastPing = System.currentTimeMillis();
	}

	public boolean isSkulled() {
		return skullEvent != null;
	}

	public PlayerAppearance getPlayerAppearance() {
		return appearance;
	}


	public void setAppearance(PlayerAppearance appearance) {
		this.appearance = appearance;
	}

	public int getSkullTime() {
		if(isSkulled()) {
			return skullEvent.timeTillNextRun();
		}
		return 0;
	}
	//	destroy
	public void addSkull(long timeLeft) {
		if(!isSkulled()) {
			skullEvent = new DelayedEvent(this, 1200000) {
				public void run() {
					removeSkull();
				}
			};
			world.getDelayedEventHandler().add(skullEvent);
			super.setAppearnceChanged(true);
		}
		skullEvent.setLastRun(System.currentTimeMillis() - (1200000 - timeLeft));
	}

	public void removeSkull() {
		if(!isSkulled()) {
			return;
		}
		super.setAppearnceChanged(true);
		skullEvent.stop();
		skullEvent = null;
	}

	public void setSuspiciousPlayer(boolean suspicious) {
		this.suspicious = suspicious;
	}

	public void addPlayersAppearanceIDs(int[] indicies, int[] appearanceIDs) {
		for (int x = 0; x < indicies.length; x++) {
			knownPlayersAppearanceIDs.put(indicies[x], appearanceIDs[x]);
		}
	}

	public List<Player> getPlayersRequiringAppearanceUpdate() {
		List<Player> needingUpdates = new ArrayList<Player>();
		needingUpdates.addAll(watchedPlayers.getNewEntities());
		if (super.ourAppearanceChanged) {
			needingUpdates.add(this);
		}
		for (Player p : watchedPlayers.getKnownEntities()) {
			if (needsAppearanceUpdateFor(p)) {
				needingUpdates.add(p);
			}
		}
		return needingUpdates;
	}

	private boolean needsAppearanceUpdateFor(Player p) {
		int playerServerIndex = p.getIndex();
		if (knownPlayersAppearanceIDs.containsKey(playerServerIndex)) {
			int knownPlayerAppearanceID = knownPlayersAppearanceIDs.get(playerServerIndex);
			if(knownPlayerAppearanceID != p.getAppearanceID()) {
				return true;
			}
		}
		else {
			return true;
		}
		return false;
	}

	public void updateViewedPlayers() {
		List<Player> playersInView = viewArea.getPlayersInView();
		for (Player p : playersInView) {
			if (p.getIndex() != getIndex() && p.loggedIn()) {
				this.informOfPlayer(p);
			}
			if (p.isInvisible()) {
				p.informOfPlayer(this);				
			}
		}
	}

	public void updateViewedObjects() {
		List<GameObject> objectsInView = viewArea.getGameObjectsInView();
		for (GameObject o : objectsInView) {
			if (!watchedObjects.contains(o) && !o.isRemoved() && withinRange(o)) {
				watchedObjects.add(o);
			}
		}
	}

	public void updateViewedItems() {
		List<Item> itemsInView = viewArea.getItemsInView();
		for (Item i : itemsInView) {
			if ((!(watchedItems.contains(i))) && (!(i.isRemoved())) && (withinRange(i)) && (i.visibleTo(this)) && (hiddenItems == true)) {
				watchedItems.add(i);
			}
		}
	}

	public void updateViewedNpcs() {
		List<Npc> npcsInView = viewArea.getNpcsInView();
		for (Npc n : npcsInView) {
			if ((!watchedNpcs.contains(n) || watchedNpcs.isRemoving(n)) && withinRange(n)) {
				watchedNpcs.add(n);
			}
		}
	}

	public void teleport(int x, int y, boolean bubble) {
		Mob opponent = super.getOpponent();
		if(inCombat()) {
			resetCombat(CombatState.ERROR);
		}
		if(opponent != null) {
			opponent.resetCombat(CombatState.ERROR);
		}
		for (Object o : getWatchedPlayers().getAllEntities()) {
			Player p = ((Player)o);
			if(bubble) {
				p.getActionSender().sendTeleBubble(getX(), getY(), false);
			}
			p.removeWatchedPlayer(this);
		}
		if(bubble) {
			actionSender.sendTeleBubble(getX(), getY(), false);
		}
		setLocation(Point.location(x, y), true);
		resetPath();
		actionSender.sendWorldInfo();
	}
	//destroy
	/**
	 * This is a 'another player has tapped us on the shoulder' method.
	 *
	 * If we are in another players viewArea, they should in theory be in ours.
	 * So they will call this method to let the player know they should probably
	 * be informed of them.
	 */
	public void informOfPlayer(Player p) {
		if ((!watchedPlayers.contains(p) || watchedPlayers.isRemoving(p)) && withinRange(p)) {
			watchedPlayers.add(p);
		}
	}

	public StatefulEntityCollection<Player> getWatchedPlayers() {
		return watchedPlayers;		
	}

	public StatefulEntityCollection<GameObject> getWatchedObjects() {
		return watchedObjects;		
	}

	public StatefulEntityCollection<Item> getWatchedItems() {
		return watchedItems;		
	}

	public StatefulEntityCollection<Npc> getWatchedNpcs() {
		return watchedNpcs;		
	}

	public void removeWatchedNpc(Npc n) {
		watchedNpcs.remove(n);
	}

	public void removeWatchedPlayer(Player p) {
		watchedPlayers.remove(p);
	}

	public void revalidateWatchedPlayers() {
		for (Player p : watchedPlayers.getKnownEntities()) {
			if (!withinRange(p) || !p.loggedIn()) {
				watchedPlayers.remove(p);
				knownPlayersAppearanceIDs.remove(p.getIndex());
			}
		}
	}

	public void revalidateWatchedObjects() {
		for (GameObject o : watchedObjects.getKnownEntities()) {
			if (!withinRange(o) || o.isRemoved()) {
				watchedObjects.remove(o);
			}
		}
	}

	public void revalidateWatchedItems() {
		for (Item i : watchedItems.getKnownEntities()) {
			if (!withinRange(i) || i.isRemoved() || !i.visibleTo(this)) {
				watchedItems.remove(i);
			}
		}
	}

	public void revalidateWatchedNpcs() {
		for (Npc n : watchedNpcs.getKnownEntities()) {
			if (!withinRange(n) || n.isRemoved()) {
				watchedNpcs.remove(n);
			}
		}
	}

	public boolean withinRange(Entity e) {
		int xDiff = location.getX() - e.getLocation().getX(); 
		int yDiff = location.getY() - e.getLocation().getY();
		return xDiff <= 16 && xDiff >= -15 && yDiff <= 16 && yDiff >= -15;
	}

	public int[] getCurStats() {
		return curStat;
	}

	public int getCurStat(int id) {
		return curStat[id];
	}

	public int getHits() {
		return getCurStat(3);
	}

	public int getAttack() {
		return getCurStat(0);
	}

	public int getDefense() {
		return getCurStat(1);
	}

	public int getStrength() {
		return getCurStat(2);
	}

	public void setHits(int lvl) {
		setCurStat(3, lvl);
	}

	public void setAttack(int lvl) {
		setCurStat(0, lvl);
	}

	public void setDefense(int lvl) {
		setCurStat(1, lvl);
	}

	public void setStrength(int lvl) {
		setCurStat(2, lvl);
	}

	public void setCurStat(int id, int lvl) {
		if(lvl <= 0) {
			lvl = 0;
		}
		curStat[id] = lvl;
	}

	public int getMaxStat(int id) {
		return maxStat[id];
	}

	public void setMaxStat(int id, int lvl) {
		if(lvl < 0) {
			lvl = 0;
		}
		maxStat[id] = lvl;
	}

	public int[] getMaxStats() {
		return maxStat;
	}

	public int getSkillTotal() {
		int total = 0;
		for(int stat : maxStat) {
			total += stat;
		}
		return total;
	}

	public void incCurStat(int i, int amount) {
		curStat[i] += amount;
		if(curStat[i] < 0) {
			curStat[i] = 0;
		}
	}

	public void incMaxStat(int i, int amount) {
		maxStat[i] += amount;
		if(maxStat[i] < 0) {
			maxStat[i] = 0;
		}
	}

	public void setFatigue(int fatigue) {
		this.fatigue = fatigue;
	}

	public int getFatigue() {
		return fatigue;
	}

	public void incExp(int i, int amount, boolean useFatigue, boolean multiplied) {
		if(GameVars.useFatigue) {
			if(useFatigue) {
				if(fatigue >= 100) {
					actionSender.sendMessage("@gre@You are too tired to gain experience, get some rest!");
					return;
				}
				if(fatigue >= 96) {
					actionSender.sendMessage("@gre@You start to feel tired, maybe you should rest soon.");
				}
				if(i >= 4) {
					setFatigue(getFatigue() + 1);
					actionSender.sendFatigue();
				}
			}
		}
		if(isSub(getUsername())) {
			amount *= GameVars.SubExpMultiplier;
		} else if(getLocation().onTutorialIsland()) {
			multiplied = false;
		} else if(multiplied) {
			amount *= GameVars.expMultiplier;
		}
		exp[i] += amount;
		if(exp[i] < 0) {
			exp[i] = 0;
		}
		int level = Formulae.experienceToLevel(exp[i]);
		if(level != maxStat[i]) {
			int advanced = level - maxStat[i];
			incCurStat(i, advanced);
			incMaxStat(i, advanced);
			actionSender.sendStat(i);
			actionSender.sendMessage("@gre@You just advanced " + advanced + " " + Formulae.statArray[i] + " level!");
			actionSender.sendSound("advance");
			world.getDelayedEventHandler().add(new MiniEvent(this) {
				public void action() {
					owner.getActionSender().sendScreenshot();
				}
			});
			if(maxStat[i] == 99) {
				world.sendToAll(getUsername() + " has just achieved level 99 in " + Formulae.statArray[i]);
			}
			int comb = Formulae.getCombatlevel(maxStat);
			if(comb != getCombatLevel()) {
				actionSender.sendMessage("@gre@Your combat level is now:@whi@ " + comb + "!");
				actionSender.sendSound("advance");
				setCombatLevel(comb);
			}
		}
	}
	//destroy
	public int[] getExps() {
		return exp;
	}

	public int getExp(int id) {
		return exp[id];
	}

	public void setExp(int id, int lvl) {
		if(lvl < 0) {
			lvl = 0;
		}
		exp[id] = lvl;
	}

	public void setExp(int[] lvls) {
		exp = lvls;
	}

	public boolean equals(Object o) {
		if (o instanceof Player) {
			Player p = (Player)o;
			return usernameHash == p.getUsernameHash();
		}
		return false;
	}

}