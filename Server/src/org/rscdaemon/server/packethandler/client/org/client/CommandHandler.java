package org.rscdaemon.server.packethandler.client;

/**
 * Imports
 */
import org.rscdaemon.server.GUI;
import org.rscdaemon.server.GameVars;

import org.rscdaemon.server.entityhandling.EntityHandler;

import org.rscdaemon.server.event.SingleEvent;

import org.rscdaemon.server.model.GameObject;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.Mob;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Point;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.Item;
import org.rscdaemon.server.model.ChatMessage;

import org.rscdaemon.server.net.Packet;

import org.rscdaemon.server.packethandler.PacketHandler;

import org.rscdaemon.server.states.CombatState;

import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.util.Formulae;
import org.rscdaemon.server.util.Logger;
import org.rscdaemon.server.util.Flags;

import org.rscdaemon.server.clansystem.ClanHandler;
import org.rscdaemon.server.clansystem.ClanInvite;
import org.rscdaemon.server.clansystem.Clan;

import org.apache.mina.common.IoSession;

import java.io.*;
import java.util.*;

public class CommandHandler implements PacketHandler {
	/**
	 * World Instance
	 */
	public static final World world = World.getWorld();
	
	String lastplayer;
	public long lasttime;
	public long lastmessage;
	static Properties props = new Properties();
	
	private int npcsInStressTest = 0;
	final int MAX_DROP_PARTY_AMOUNT = 10;
	public static String[] cussingWords = { "fuck" };
	
	public boolean isValidRanks(int rank) {
		int validRanks[] = { 2, 3, 4, 7, 8 };
		boolean valid = false;
		for(int i = 0; i < validRanks.length; i++) {
			if(validRanks[i] == rank) {
				valid = true;
			}
		}
		return valid;
	}
	
	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player)session.getAttachment();
		if(player.isBusy()) {
			player.resetPath();
			return;
		}
		player.resetAll();
		String s = new String(p.getData()).trim();
		int firstSpace = s.indexOf(" ");
		String cmd = s;
		String[] args = new String[0];
		if(firstSpace != -1) {
			cmd = s.substring(0, firstSpace).trim();
			args = s.substring(firstSpace + 1).trim().split(" ");
		}
		try {
			handleCommand(cmd.toLowerCase(), args, player);
		}
		catch (Exception e) { }
	}
	
	public boolean handleClanCommand(String cmd, String[] args, Player player) throws Exception {
		/**
		 * Clan Commands
		 */
		if(cmd.equalsIgnoreCase("claninvite")) {
			if(!player.isClanLeader()) {
				player.getActionSender().sendMessage("You need to be the clan leader to invite another player!");
				return true;
			}
			String username = "";
			for(int j = 0; j < args.length; j++) {
				username += args[j] + " ";
			}
			username = username.trim();
			Player p = world.getPlayer(DataConversions.usernameToHash(username));
			if(p == null) {
				player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
			} else {
				if(p.hasClan()) {
					player.getActionSender().sendMessage(username + " is already in a clan!");
				} else {
					p.setClanInvite(new ClanInvite(player.getClan()));
				}
			}
			return true;
		}
		if(cmd.equalsIgnoreCase("createclan")) {
			if(player.hasClan()) {
				player.getActionSender().sendMessage("You are already in a clan!");
				return true;
			}
			String clanName = "";
			for(int k = 0; k < args.length; k++) {
				clanName += args[k] + " ";
			}
			clanName = clanName.trim();
			
			if(clanName.length() > 12) {
				player.getActionSender().sendMessage("Your clan name can't be more than 12 characters!");
				return true;
			}
			if(ClanHandler.createClan(clanName, player)) {
				Clan c = new Clan(clanName);
				player.setClan(c);
				world.getServer().getClanHandler().addClan(clanName, player.getUsername());
				player.getActionSender().sendMessage("You have successfully created the clan @or1@" + clanName);
			} else {
				player.getActionSender().sendMessage("Error while creating clan!");
				return true;
			}
		}
		if(cmd.equalsIgnoreCase("accept")) {
			if(player.acceptClanInvite()) {
				player.getActionSender().sendMessage("You have accepted your clan invitation!");
			} else {
				player.getActionSender().sendMessage("Error while accepting clan invite!");
			}
			return true;
		}
		if(cmd.equalsIgnoreCase("decline")) {
			if(player.declineClanInvite()) {
				player.getActionSender().sendMessage("You have declined your clan invitation!");
			} else {
				player.getActionSender().sendMessage("Error while declining clan invitation!");
			}
			return true;
		}
		if(cmd.equalsIgnoreCase("leaveclan")) {
			if(!player.hasClan()) {
				player.getActionSender().sendMessage("You are not in a clan!");
				return true;
			}
			if(player.isClanLeader()) {
				player.getActionSender().sendMessage("Clan leaders can't leave the clan they created!");
			} else {
				player.getClan().remove(player, " has left the clan!");
				player.resetClan();
				player.getActionSender().sendMessage("Please relog for the changes to take full effect!");
			}
			return true;
		}
		if(cmd.equalsIgnoreCase("clankick")) {
			if(!player.hasClan()) {
				player.getActionSender().sendMessage("You are not in a clan!");
			} else {
				if(!player.isClanLeader()) {
					player.getActionSender().sendMessage("You are not the clan leader!");
					return true;
				}
				String playerName = "";
				for(int i = 0; i < args.length; i++) {
					playerName += args[i] + " ";
				}
				playerName = playerName.trim();
				long hash = DataConversions.usernameToHash(playerName);
				Player target = world.getPlayer(hash);
				if((target == null) || (target.getClan() == null)) {
					return true;
				}
				if(!target.getClan().equals(player.getClan())) {
					return true;
				}
				target.getClan().remove(target, " has been kicked out of the clan!");
				target.resetClan();
				target.getActionSender().sendMessage("You have been kicked out of the clan!");
			}
			return true;
		}
		return false;
	}
	
	public void handleCommand(String cmd, String[] args, Player player) throws Exception {
		if(handleClanCommand(cmd, args, player)) {
			return;
		}
		/**
		 * Normal Player Commands
		 */
		if(cmd.equalsIgnoreCase("stuck")) {
			if(player.getLocation().inModRoom() || (player.getLocation().wildernessLevel() > 0)) {
				player.getActionSender().sendMessage("You can't use the stuck command here!");
			} else if((System.currentTimeMillis() - player.getLastMoved() < 10000) || (System.currentTimeMillis() - player.getCastTimer() < 10000)) {
				player.getActionSender().sendMessage("There is a 10 second delay on using stuck!");
			} else if((!player.inCombat()) && (System.currentTimeMillis() - player.getCombatTimer() > 10000)) {
				player.setCastTimer();
				player.teleport(220, 440, true);
			} else {
				player.getActionSender().sendMessage("You can't use stuck for 10 seconds after combat!");
			}
			return;
		}
		if(cmd.equalsIgnoreCase("partyhall")) {
			for(Player p : world.getPlayers()) {
				if((p.getLocation().wildernessLevel() > 0) || (p.getLocation().inModRoom())) {
					player.getActionSender().sendMessage("You can't use the party hall command here!");
					return;
				}
				if(World.dropparty == 0) { // Drop party timer isn't running
					player.getActionSender().sendMessage("You can only use this command while the drop party timer is running!");
					return;
				}
				if(World.dropparty == 1) { // Drop party timer is running
					player.teleport(490, 1413, true);
					player.getActionSender().sendMessage("You arrive at the drop party hall.. Good luck!");
					return;
				}
			}
		}
		if(cmd.equalsIgnoreCase("setflag")) {
			if(args[0] == null) {
				player.getActionSender().sendMessage("Invalid args. Syntax: SETFLAG [flag initials]");
				return;
			}
			if((player.flag != null) && (player.flag.length() == 2)) {
				player.getActionSender().sendMessage("You can only set your flag once!");
				return;
			}
			String flg = args[0].trim().toUpperCase();
			if(Flags.flags.contains(flg)) {
				player.flag = flg;
				player.setAppearnceChanged(true);
			} else {
				player.getActionSender().sendMessage("Sorry, this flag does not exist!");
			}
		}
		if(cmd.equalsIgnoreCase("suggest")) {
			String suggestion = "";
			for(int i = 0; i < args.length; i++) {
				suggestion += args[i] + " ";
			}
			suggestion = suggestion.trim();
			if(!player.canSendSuggestion()) {
				player.getActionSender().sendMessage("There's a 5 minute delay on sending in new suggestions!");
				return;
			}
			if(args.length < 1) {
				player.getActionSender().sendMessage("Invalid suggestion!");
			} else {
				player.sendSuggestion(suggestion);
				player.setLastSuggestion();
			}
		}
		if(cmd.equalsIgnoreCase("online")) {
			int playerscounter = 0;
			String amount;
			for(Player p : world.getPlayers()) {
				playerscounter++;
			}
			if(playerscounter == 1) {
				amount = "player";
			} else {
				amount = "players";
			}
			player.getActionSender().sendMessage("There are currently @or1@" + playerscounter + "@whi@ " + amount + " online!");
			return;
		}
		if(cmd.equalsIgnoreCase("onlinelist")) {
			String amount;
			String playerslist = "";
			int playerscounter = 0;
			for(Player p : world.getPlayers()) {
				if(p.isAdmin()) {
					playerslist = "#adm# @yel@" + p.getUsername() + ", " + playerslist;
				} else if(p.isDeveloper()) {
					playerslist = "#dev# @red@" + p.getUsername() + ", " + playerslist;
				} else if(p.isMod()) {
					playerslist = "#mod# @red@" + p.getUsername() + ", " + playerslist;
				} else if(p.isPMod()) {
					playerslist = "#pmd# @gre@" + p.getUsername() + ", " + playerslist;
				} else if(p.isEvent()) {
					playerslist = "#evt# @blu@" + p.getUsername() + ", " + playerslist;
				} else if(p.isMuted()) {
					playerslist = "@cya@[M] " + p.getUsername() + ", " + playerslist;
				} else {
					playerslist = "@whi@" + p.getUsername() + ", " + playerslist;
				}
				playerscounter++;
			}
			if(playerscounter == 1) {
				amount = "player";
			} else {
				amount = "players";
			}
			player.getActionSender().sendAlert("There are currently @or1@" + playerscounter + " @whi@ " + amount + " online: " + playerslist, true);
			return;
		}
		if(cmd.equalsIgnoreCase("hideloot")) {
			if(player.getHiddenItem()) {
				player.setHiddenItem(false);
				for(Item hiddenitem : player.getWatchedItems().getKnownEntities()) {
					player.getWatchedItems().remove(hiddenitem);
				}
				player.getActionSender().sendMessage("Ground loot is now: @red@Invisible");
			} else {
				player.setHiddenItem(true);
				player.getActionSender().sendMessage("Ground loot is now: @gre@Visible");
			}
		}
		if(cmd.equalsIgnoreCase("wilderness")) {
			if(args.length < 1) {
				String wildernessType;
				if(world.getWildernessType())
					wildernessType = "@gre@P2P";
				else {
					wildernessType = "@gre@F2P";
				}
				player.getActionSender().sendMessage("The wilderness type is currently: " + wildernessType);
			} else if((player.isAdmin()) && (args.length == 1)) {
				if((args[0].equals("f2p")) && (!(world.isWildernessSwitching())) && (world.getWildernessType())) {
					Player p;
					for(Iterator id = world.getPlayers().iterator(); id.hasNext();) {
						p = (Player)id.next();
						p.getActionSender().sendAlert("The wilderness will change to @gre@F2P @whi@in 60 seconds! Your @gre@P2P @whi@items will be unwielded and if you're potted, your stats will be changed!", false);
						p.getActionSender().startWildernessSwitch(60);
						world.getServer().startWildernessSwitch();
						world.setWildernessSwitching(true);
					}
				} else if((args[0].equals("p2p")) && (!(world.isWildernessSwitching())) && (!(world.getWildernessType()))) {
					Player p;
					for(Iterator id = world.getPlayers().iterator(); id.hasNext();) {
						p = (Player)id.next();
						p.getActionSender().sendAlert("The wilderness will change to @gre@P2P @whi@in 60 seconds! You will now have access to all @gre@P2P @whi@armour, weapons, food and potions!", false);
						p.getActionSender().startWildernessSwitch(60);
						world.getServer().startWildernessSwitch();
						world.setWildernessSwitching(true);
						}
					} 
				} else {
					player.getActionSender().sendMessage("Invalid args. Syntax: WILDERNESS [f2p] / [p2p]");
				return;
			}
		}
		if(cmd.equalsIgnoreCase("wipebank")) {
			player.getBank().getItems().clear();
			player.getActionSender().sendMessage("Your bank has been cleared!");
			return;
		}
		if(cmd.equalsIgnoreCase("skull")) {
			player.addSkull(1200000);
			player.getActionSender().sendMessage("You are now skulled for 20 minutes!");
			return;
		}
		if(cmd.equalsIgnoreCase("fatigue")) {
			if(args.length < 1) {
				player.setFatigue(100);
				player.getActionSender().sendFatigue();
				player.getActionSender().sendMessage("Your fatigue has been set to 100%!");
			} else if((args.length == 1) && (player.isAdmin())) {
				long PlayerHash = DataConversions.usernameToHash(args[0]);
				Player p = world.getPlayer(PlayerHash);
				if(p != null) {
					p.setFatigue(100);
					p.getActionSender().sendFatigue();
					p.getActionSender().sendMessage("Your fatigue has been set to 100% by a member of staff!");
					player.getActionSender().sendMessage("You set " + p.getUsername() + "'s fatigue to 100%");
				}
			}
			return;
		}
		if(cmd.equalsIgnoreCase("spree")) {
			long PlayerHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(PlayerHash);
			if(p != null) {
				player.getActionSender().sendMessage(p.getUsername() + "'s killing spree is: " + p.getKillingSpree());
			} else {
				player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
			}
			return;
		}
		if(cmd.equalsIgnoreCase("wild")) {
			int wildcount = 0;
			for(Player p : world.getPlayers()) {
				if(p.getLocation().wildernessLevel() > 0) {
					wildcount++;
				}
			}
			String wildernessState;
			if(wildcount == 0) {
				wildernessState = "@red@INACTIVE";
			} else {
				wildernessState = "@gre@ACTIVE";
			}
			if(wildcount == 0) {
				player.getActionSender().sendMessage("The wilderness is currently " + wildernessState + " @whi@with @or1@" + wildcount + " @whi@players!");
				return;
			}
			if(wildcount == 1) {
				player.getActionSender().sendMessage("The wilderness is currently " + wildernessState + " @whi@with @or1@" + wildcount + " @whi@player!");
				return;
			} else {
				player.getActionSender().sendMessage("The wilderness is currently " + wildernessState + " @whi@with @or1@" + wildcount + " @whi@players!");
				return;
			}
		}
		if(cmd.equalsIgnoreCase("say")) {
			boolean waittime = false;
			if(lasttime == 0L) {
				lasttime = System.currentTimeMillis();
			}
			ArrayList informOfChatMessage = new ArrayList();
			Player p;
			for(Iterator i$ = world.getPlayers().iterator(); i$.hasNext(); informOfChatMessage.add(p)) {
				p = (Player)i$.next();
			}
			String newStr = "@gre@";
			for(int i = 0; i < args.length; i++) {
				newStr = (new StringBuilder()).append(newStr).append(args[i]).append(" ").toString();
			}
			for(int yong = 0; yong < cussingWords.length; yong++) {
			String filter = "";
			for(int min = 0; min < cussingWords[yong].length(); min++) {
				filter += "*";
			}
				newStr = newStr.replaceAll("(?i)" + cussingWords[yong], filter);
			}
			if(player.isMuted()) {
				player.getActionSender().sendMessage("You can't use this command while you're muted!");
				return;
			}
			if(player.isAdmin()) {
				newStr = (new StringBuilder()).append("@say@@adm@").append("@yel@     " + player.getUsername()).append(": ").append(newStr).toString();
			} else if(player.isDeveloper()) {
				newStr = (new StringBuilder()).append("@say@@dev@").append("@red@     " + player.getUsername()).append(": ").append(newStr).toString();
			} else if(player.isMod()) {
				newStr = (new StringBuilder()).append("@say@@mod@").append("@gry@     " + player.getUsername()).append(": ").append(newStr).toString();
			} else if(player.isPMod()) {
				newStr = (new StringBuilder()).append("@say@@pmd@").append("@gre@     " + player.getUsername()).append(": ").append(newStr).toString();
			} else if(player.isEvent()) {
				newStr = (new StringBuilder()).append("@say@@evt@").append("@blu@     " + player.getUsername()).append(": ").append(newStr).toString();
			} else if((System.currentTimeMillis() - lasttime > 20000L) || (lastplayer != player.getUsername())) {
				newStr = (new StringBuilder()).append("@say@@whi@[Member] ").append(player.getUsername()).append(": ").append(newStr).toString();
			} else {
				long timeremaining = 10L - (System.currentTimeMillis() - lasttime) / 1000L;
				player.getActionSender().sendMessage((new StringBuilder()).append("You need to wait ").append(timeremaining).append(" seconds before using ::say again.").toString());
                waittime = true;
			}
			if(!waittime) {
				lasttime = System.currentTimeMillis();
				lastplayer = player.getUsername();
				Player pl;
				for(Iterator i$ = informOfChatMessage.iterator(); i$.hasNext(); pl.getActionSender().sendMessage(newStr)) {
					pl = (Player)i$.next();
				}
			}
			return;
		}
		if(cmd.equalsIgnoreCase("event")) {
			if(world.eventStatus == 0) {
				player.getActionSender().sendMessage("There is currently no event open!");
			} else if((world.eventStatus == 1) && (player.getCombatLevel() != world.eventLevel)) {
				player.getActionSender().sendMessage("You don't meet the level requirements for this event!");
			} else {
				player.teleport(world.eventCoordX, world.eventCoordY, false);
				player.getActionSender().sendMessage("You have been successfully teleported to the event location! Good luck!");
			}
			return;
		}
		
		/**
		 * Administrator Commands
		 */
		if((cmd.equalsIgnoreCase("modroom")) && (player.isAdmin())) {
			player.teleport(70, 1640, true);
			return;
		}
		if((cmd.equalsIgnoreCase("kick")) && (player.isAdmin())) {
			long PlayerHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(PlayerHash);
			for(Player pl : world.getPlayers()) {
				if(p != null) {
					p.getActionSender().sendLogout();
					p.destroy(true);
					pl.getActionSender().sendMessage("%r-" + "@red@ANNOUNCEMENT: @whi@" + p.getUsername() + " has been kicked from the server!");
				} else {
					player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
				}
			}
			return;
		}
		if((cmd.equalsIgnoreCase("global")) && (player.isAdmin())) {
			String globalMsg = "";
			for(int i = 0; i < args.length; i++) {
				globalMsg = globalMsg + args[i] + " ";
			}
			for(Player p : world.getPlayers()) {
				p.getActionSender().sendAlert("#adm# @yel@" + player.getUsername() + "@whi@: " + globalMsg, false);
			}
			return;
		}
		if((cmd.equalsIgnoreCase("goto")) || (cmd.equalsIgnoreCase("summon")) && (player.isAdmin())) {
			boolean summon = cmd.equalsIgnoreCase("summon");
			if(args.length != 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: " + (summon ? "SUMMON" : "GOTO") + " [username]");
				return;
			}
			long usernameHash = DataConversions.usernameToHash(args[0]);
			Player affectedPlayer = world.getPlayer(usernameHash);
			if(affectedPlayer != null) {
				if(summon) {
					affectedPlayer.teleport(player.getX(), player.getY(), true);
					affectedPlayer.getActionSender().sendMessage("You have been summoned by " + player.getUsername());
				} else {
					player.teleport(affectedPlayer.getX(), affectedPlayer.getY(), true);
				}
			} else {
				player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
			}
			return;
		}
		if((cmd.equalsIgnoreCase("take")) || (cmd.equalsIgnoreCase("put")) && (player.isAdmin())) {
			if(args.length != 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: TAKE [username]");
				return;
			}
			Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
			if(affectedPlayer == null) {
				player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
				return;
			}
			affectedPlayer.teleport(78, 1642, true);
			world.sendToAll("%r-" + "@red@ANNOUNCEMENT@whi@: " + affectedPlayer.getUsername() + " has been jailed!");
			if((cmd.equalsIgnoreCase("take")) && (player.isAdmin())) {
				player.teleport(76, 1642, true);
			}
			return;
		}
		if((cmd.equalsIgnoreCase("send")) && (player.isAdmin())) {
			if(args.length != 3) {
				player.getActionSender().sendMessage("Invalid args. Syntax: SEND [username] [x] [y]");
				return;
			}
			long usernameHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(usernameHash);
			int x = Integer.parseInt(args[1]);
			int y = Integer.parseInt(args[2]);
			if((world.withinWorld(x, y)) && (p != null)) {
				p.getActionSender().sendMessage("You have been teleported by " + player.getUsername());
				p.teleport(x, y, true);
			} else {
				player.getActionSender().sendMessage("Invalid coordinates or player!");
			}
			return;
		}
		if((cmd.equalsIgnoreCase("ban")) || (cmd.equalsIgnoreCase("unban")) && (player.isAdmin())) {
			long PlayerHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(PlayerHash);
			boolean banned = cmd.equalsIgnoreCase("ban");
			if(args.length != 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: " + (banned ? "BAN" : "UNBAN") + " [username]");
				return;
			}
			if(banned) {
				if(Integer.valueOf(GUI.readValue(args[0], "rank")) == 6) {
					player.getActionSender().sendMessage("That player is already banned!");
					return;
				} else {
					world.banPlayer(args[0]);
					world.sendToAll("%r-" + "@red@ANNOUNCEMENT@whi@: " + p.getUsername() + " has been banned!");
				}
			} else {
				if(Integer.valueOf(GUI.readValue(args[0], "rank")) == 6) {
					world.unbanPlayer(args[0]);
				} else {
					player.getActionSender().sendMessage("That player is not banned!");
				}
			}
			return;
		}
		if((cmd.equalsIgnoreCase("shutdown")) && (player.isAdmin())) {
			world.getServer().kill();
			return;
		}
		if((cmd.equalsIgnoreCase("update")) && (player.isAdmin())) {
			String reason = "";
			if(args.length > 0) {
				for(String s : args) {
					reason += (s + " ");
				}
				reason = reason.substring(0, reason.length() - 1);
			}
			if(world.getServer().shutdownForUpdate()) {
				for(Player p : world.getPlayers()) {
					p.getActionSender().sendAlert("The server will be shutting down in 60 seconds: " + reason, false);
					p.getActionSender().startShutdown(60);
				}
			}
			return;
		}
		if((cmd.equalsIgnoreCase("appearance")) && (player.isAdmin())) {
			player.setChangingAppearance(true);
			player.getActionSender().sendAppearanceScreen();
			return;
		}
		if((cmd.equalsIgnoreCase("invisible")) && (player.isAdmin())) {
			player.goInvisible();
		}
		if((cmd.equalsIgnoreCase("demote")) && (player.isAdmin())) {
			long PlayerHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(PlayerHash);
			if(p == null) {
				player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
				return;
			}
			if(!p.isStaff()) {
				player.getActionSender().sendMessage("You can only demote a member of staff!");
				return;
			}
			for(Player pl : world.getPlayers()) {
				world.demotePlayer(args[0]);
				pl.getActionSender().sendMessage("%r-" + "@red@ANNOUNCEMENT@whi@: " + p.getUsername() + " has just been demoted!");
				p.getActionSender().sendLogout();
				p.destroy(true);
			}
			player.getActionSender().sendMessage("You have just demoted " + p.getUsername());
			return;
		}
		if((cmd.equalsIgnoreCase("promote")) && (player.isAdmin())) {
			long PlayerHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(PlayerHash);
			Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
			int rank = Integer.parseInt(args[1]);
			if(affectedPlayer == null) {
				player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
				return;
			}
			if((rank == 4) && (affectedPlayer.isAdmin())) {
				player.getActionSender().sendMessage("That player is already an administrator!");
				return;
			}
			if((rank == 3) && (affectedPlayer.isMod())) {
				player.getActionSender().sendMessage("That player is already a moderator!");
				return;
			}
			if((rank == 2) && (affectedPlayer.isPMod())) {
				player.getActionSender().sendMessage("That player is already a player moderator!");
				return;
			}
			if((rank == 7) && (affectedPlayer.isEvent())) {
				player.getActionSender().sendMessage("That player is already part of the event team!");
				return;
			}
			if((rank == 8) && (affectedPlayer.isDeveloper())) {
				player.getActionSender().sendMessage("That player is already a developer!");
				return;
			}
			if(!isValidRanks(rank)) {
				player.getActionSender().sendMessage("Invalid rank!");
				return;
			}
			if(rank == 4) { // Administrator
				for(Player admin : world.getPlayers()) {
					world.promoteAdmin(args[0]);
					admin.getActionSender().sendMessage("%r-" + "@red@PROMOTION@whi@: " + affectedPlayer.getUsername() + " has just been promoted to @adm@@yel@     Administrator");
				}
				p.getActionSender().sendMessage(player.getUsername() + " has promoted you to @adm@@yel@     Administrator");
				p.getActionSender().sendMessage("Please logout and back in for the effects to take place.");
				player.getActionSender().sendMessage("You have promoted " + affectedPlayer.getUsername() + " to @adm@@yel@     Administrator");
				return;
			}
			if(rank == 3) { // Moderator
				for(Player mod : world.getPlayers()) {
					world.promoteMod(args[0]);
					mod.getActionSender().sendMessage("%r-" + "@red@PROMOTION@whi@: " + affectedPlayer.getUsername() + " has just been promoted to @mod@@gry@     Moderator");
				}
				p.getActionSender().sendMessage(player.getUsername() + " has promoted you to @mod@@gry@     Moderator");
				p.getActionSender().sendMessage("Please logout and back in for the effects to take place.");
				player.getActionSender().sendMessage("You have promoted " + affectedPlayer.getUsername() + " to @mod@@gry@     Moderator");
				return;
			}
			if(rank == 2) { // Player Moderator
				for(Player pmod : world.getPlayers()) {
					world.promotePMod(args[0]);
					pmod.getActionSender().sendMessage("%r-" + "@red@PROMOTION@whi@: " + affectedPlayer.getUsername() + " has just been promoted to @pmd@@gre@     Player Moderator");
				}
				p.getActionSender().sendMessage(player.getUsername() + " has promoted you to @pmd@@gre@     Player Moderator");
				p.getActionSender().sendMessage("Please logout and back in for the effects to take place.");
				player.getActionSender().sendMessage("You have promoted " + affectedPlayer.getUsername() + " to @pmd@@gre@     Player Moderator");
				return;
			}
			if(rank == 7) { // Event Team
				for(Player event : world.getPlayers()) {
					world.promoteEvent(args[0]);
					event.getActionSender().sendMessage("%r-" + "@red@PROMOTION@whi@: " + affectedPlayer.getUsername() + " has just been promoted to @evt@@blu@     Event Staff");
				}
				p.getActionSender().sendMessage(player.getUsername() + " has promoted you to @evt@@blu@     Event Staff");
				p.getActionSender().sendMessage("Please logout and back in for the effects to take place.");
				player.getActionSender().sendMessage("You have promoted " + affectedPlayer.getUsername() + " to @evt@@blu@     Event Staff");
				return;
			}
			if(rank == 8) { // Developer
				for(Player developer : world.getPlayers()) {
					world.promoteDeveloper(args[0]);
					developer.getActionSender().sendMessage("%r-" + "@red@PROMOTION@whi@: " + affectedPlayer.getUsername() + " has just been promoted to @dev@@red@     Developer");
				}
			}
			p.getActionSender().sendMessage(player.getUsername() + " has promoted you to @dev@@red@     Developer");
			p.getActionSender().sendMessage("Please logout and back in for the effects to take place.");
			player.getActionSender().sendMessage("You have promoted " + affectedPlayer.getUsername() + " to @dev@@red@     Developer");
			return;
		}
		if((cmd.equalsIgnoreCase("stresstest")) && (player.isAdmin())) {
			int amount = Integer.parseInt(args[0]);
			if(amount > 200) {
				player.getActionSender().sendMessage("@red@WARNING@whi@: Spawning more than 200 NPCs may cause the server to lag or crash!");
				return;
			}
			npcsInStressTest += amount;
			int id = 0;
			for(int i = 0; i < amount; i++) {
				id = Formulae.Rand(0, EntityHandler.getNpcCount());
				world.registerNpc(new Npc(id, player.getX(), player.getY(), player.getX() - 20, player.getX() + 20, player.getY() - 20, player.getY() + 20));
			}
			player.getActionSender().sendMessage("There is currently : @or1@" + npcsInStressTest + "@whi@ NPCs in the stress test!");
			return;
		}
		if((cmd.equalsIgnoreCase("item")) && (player.isAdmin())) {
			if((args.length < 1) || (args.length > 2)) {
				player.getActionSender().sendMessage("Invalid args. Syntax: ITEM [id] [amount]");
				return;
			}
			int id = Integer.parseInt(args[0]);
			if(EntityHandler.getItemDef(id) != null) {
				int amount = 1;
				if((args.length == 2) && (EntityHandler.getItemDef(id).isStackable())) {
					amount = Integer.parseInt(args[1]);
				}
				InvItem item = new InvItem(id, amount);
				player.getInventory().add(item);
				player.getActionSender().sendInventory();
			} else {
				player.getActionSender().sendMessage("Invalid item ID!");
			}
			return;
		}
		if((cmd.equalsIgnoreCase("reset")) && (player.isAdmin())) {
			long PlayerHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(PlayerHash);
			int stat = Formulae.getStatIndex(args[1]);
			int level = Integer.parseInt(args[2]);
			
			if((level < 1) || (level > 40)) {
				player.getActionSender().sendMessage("Invalid " + Formulae.statArray[stat] + " level.");
				return;
			}
			if(p.getMaxStat(stat) > 39) {
				player.getActionSender().sendMessage("That player's " + Formulae.statArray[stat] + " level is too high to be reset!");
				return;
			}
			if(stat > 6) {
				player.getActionSender().sendMessage("You can only reset another player's combat stats!");
				return;
			}
			if(stat == 3) {
				player.getActionSender().sendMessage("You can't alter another player's hits level!");
				return;
			}
			p.setCurStat(stat, level);
			p.setMaxStat(stat, level);
			p.setExp(stat, Formulae.lvlToXp(level));
			
			if((stat == 0) || (stat == 1) || (stat == 2)) {
				int hitpointsexp = Formulae.lvlToXp(p.getMaxStat(0)) + Formulae.lvlToXp(p.getMaxStat(1)) + Formulae.lvlToXp(p.getMaxStat(2)) + 1154;
				int hitpointslevel = Formulae.experienceToLevel(hitpointsexp / 3);
				if(hitpointslevel < 10) {
					hitpointslevel = 10;
					p.setCurStat(3, 10);
					p.setMaxStat(3, 10);
					p.setExp(3, 1154);
				} else {
					p.setCurStat(3, hitpointslevel);
					p.setMaxStat(3, hitpointslevel);
					p.setExp(3, Formulae.lvlToXp(hitpointslevel));
				}
			}
			int combat = Formulae.getCombatlevel(p.getMaxStats());
			if(combat != p.getCombatLevel()) {
				p.setCombatLevel(combat);
			}
			p.getActionSender().sendStats();
			if(p.getUsername() == player.getUsername()) {
				player.getActionSender().sendMessage("You have updated your " + Formulae.statArray[stat] + " to level " + args[2] + ".");
			} else {
				p.getActionSender().sendMessage(player.getUsername() + " has updated your " + Formulae.statArray[stat] + " to level " + args[2] + ".");
				player.getActionSender().sendMessage("You have updated " + p.getUsername() + "'s " + Formulae.statArray[stat] + " to level " + args[2] + ".");
			}
			return;
		}
		if((cmd.equalsIgnoreCase("npc")) && (player.isAdmin())) {
			if(args.length != 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: NPC [id]");
				return;
			}
			int id = Integer.parseInt(args[0]);
			if(EntityHandler.getNpcDef(id) != null) {
				final Npc n = new Npc(id, player.getX(), player.getY(), player.getX() - 2, player.getX() + 2, player.getY() - 2, player.getY() + 2);
				n.setRespawn(false);
				world.registerNpc(n);
				world.getDelayedEventHandler().add(new SingleEvent(null, 60000) {
					public void action() {
						Mob opponent = n.getOpponent();
						if(opponent != null) {
							opponent.resetCombat(CombatState.ERROR);
						}
						n.resetCombat(CombatState.ERROR);
						world.unregisterNpc(n);
						n.remove();
					}
				});
			} else {
				player.getActionSender().sendMessage("Invalid NPC ID");
			}
			return;
		}
		if((cmd.equalsIgnoreCase("announcement")) && (player.isAdmin())) {
			String newAnnouncement = "";
			for(int i = 0; i < args.length; i++) {
				newAnnouncement = newAnnouncement + args[i] + " ";
			}
			for(Player p : World.getWorld().getPlayers()) {
				p.getActionSender().sendMessage("%r-" + "@red@ANNOUNCEMENT@whi@: " + newAnnouncement);
			}
			return;
		}
		if((cmd.equalsIgnoreCase("npctalk")) && (player.isAdmin())) {
			String newMessage = "";
			for(int i = 1; i < args.length; i++) {
				newMessage = newMessage = newMessage + args[i] + " ";
			}
			Npc npc = world.getNpc(Integer.parseInt(args[0]), player.getX() - 10, player.getX() + 10, player.getY() - 10, player.getY() + 10);
			if(npc != null) {
				Player p;
				for(Iterator yong = player.getViewArea().getPlayersInView().iterator(); yong.hasNext(); ) {
					p = (Player)yong.next();
					p.informOfNpcMessage(new ChatMessage(npc, newMessage, p));
				}
			} else {
				player.getActionSender().sendMessage("Invalid NPC ID");
			}
		}
		if((cmd.equalsIgnoreCase("mute")) && (player.isAdmin())) {
			boolean mute = cmd.equalsIgnoreCase("mute");
			if(args.length != 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: MUTE [username]");
				return;
			}
			if(mute) {
				if(Integer.valueOf(GUI.readValue(args[0], "mute")) == 1) {
					player.getActionSender().sendMessage("That player is already @red@MUTED");
					return;
				} else {
					world.mutePlayer(args[0]);
					Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
					player.getActionSender().sendMessage("You have @red@muted @whi@" + affectedPlayer.getUsername() + "!");
					affectedPlayer.getActionSender().sendMessage("You have been @red@muted @whi@by: " + player.getUsername());
					affectedPlayer.getActionSender().sendLogout();
					affectedPlayer.destroy(true);
				}
				Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
				for(Player muted : world.getPlayers()) {
					muted.getActionSender().sendMessage("%r-" + "@red@ANNOUNCEMENT: @whi@" + affectedPlayer.getUsername() + " has been @red@MUTED@whi@!");
				}
				if(affectedPlayer == null) {
					player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
					return;
				}
				affectedPlayer.getActionSender().sendMute();
				return;
			}
		}
		if((cmd.equalsIgnoreCase("unmute")) && (player.isAdmin())) {
			boolean unmute = cmd.equalsIgnoreCase("unmute");
			if(args.length != 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: UNMUTE [username]");
				return;
			}
			if(unmute) {
				if(Integer.valueOf(GUI.readValue(args[0], "mute")) == 0) {
					player.getActionSender().sendMessage("That player is not muted!");
					return;
				} else {
					if(Integer.valueOf(GUI.readValue(args[0], "mute")) == 1) {
						world.unMutePlayer(args[0]);
						Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
						player.getActionSender().sendMessage("You have @gre@unmuted @whi@" + affectedPlayer.getUsername());
						affectedPlayer.getActionSender().sendMessage("You have been @gre@unmuted@whi@!");
						affectedPlayer.getActionSender().sendLogout();
						affectedPlayer.destroy(true);
					}
					Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
					for(Player muted : world.getPlayers()) {
						muted.getActionSender().sendMessage("%r-" + "@red@ANNOUNCEMENT: @whi@" + affectedPlayer.getUsername() + " has been @gre@UNMUTED@whi@!");
					}
					if(affectedPlayer == null) {
						player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
						return;
					}
					affectedPlayer.getActionSender().sendMute();
					return;
				}
			}
		}
		if((cmd.equalsIgnoreCase("teleport")) && (player.isAdmin())) {
			if(args.length != 2) {
				if(args[0].equals("modroom")) {
					player.teleport(72, 1641, false);
				} else if(args[0].equals("edgeville")) {
					player.teleport(225, 447, false);
				} else if(args[0].equals("varrock")) {
					player.teleport(122, 503, false);
				} else if(args[0].equals("falador")) {
					player.teleport(313, 550, false);
				} else if(args[0].equals("seers")) {
					player.teleport(501, 455, false);
				} else if(args[0].equals("catherby")) {
					player.teleport(440, 500, false);
				} else if(args[0].equals("yanille")) {
					player.teleport(587, 761, false);
				} else if(args[0].equals("karamja")) {
					player.teleport(371, 695, false);
				} else if(args[0].equals("ardougne")) {
					player.teleport(585, 621, false);
				} else if(args[0].equals("draynor")) {
					player.teleport(214, 632, false);
				} else if(args[0].equals("lumbridge")) {
					player.teleport(122, 647, false);
				} else {
					player.getActionSender().sendMessage("Invalid location!");
				}
			}
			else if((args.length == 2) && (player.isAdmin())) {
				int x = Integer.parseInt(args[0]);
				int y = Integer.parseInt(args[1]);
				if(world.withinWorld(x, y)) {
					player.teleport(x, y, true);
				} else {
					player.getActionSender().sendMessage("Invalid coordinates!");
				}
				return;
			}
		}
		if((cmd.equalsIgnoreCase("setrate")) && (player.isAdmin())) {
			int exp = Integer.parseInt(args[0]);
			if(args.length == 1) {
				GameVars.expMultiplier = exp;
				world.sendToAll("%r-" + "@red@ANNOUNCEMENT@whi@: The server EXP rate has been changed to @or1@" + GameVars.expMultiplier + " @whi@x");
			}
			return;
		}
		if((cmd.equalsIgnoreCase("object")) && (player.isAdmin())) {
			if((args.length < 1) || (args.length > 2)) {
				player.getActionSender().sendMessage("Invalid args. Syntax: OBJECT [ID] [DIRECTION]");
				return;
			}
			int id = Integer.parseInt(args[0]);
			if(id < 0) {
				GameObject object = world.getTile(player.getLocation()).getGameObject();
				if(object != null) {
					world.unregisterGameObject(object);
				}
			} else if(EntityHandler.getGameObjectDef(id) != null) {
				int dir = args.length == 2 ? Integer.parseInt(args[1]) : 0;
				world.registerGameObject(new GameObject(player.getLocation(), id, dir, 0));
			} else {
				player.getActionSender().sendMessage("Invalid object ID!");
			}
			return;
		}
		if((cmd.equalsIgnoreCase("ipban")) && (player.isAdmin())) {
			long PlayerHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(PlayerHash);
			if(args.length > 1) {
				player.getActionSender().sendMessage("Error. Syntax: IPBAN [username]");
				return;
			}
			if(args[0] == null) {
				player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
				return;
			}
			if(args.length == 1) {
				try {
					BufferedWriter ipban = new BufferedWriter(new FileWriter("ipbans/" + "ipbans.txt", true));
					ipban.write(p.getCurrentIP() + "");
					ipban.newLine();
					ipban.flush();
					ipban.close();
					System.out.println("The IP for " + p.getUsername() + " was successfully added to the ban list!");
					player.getActionSender().sendMessage("You have successfully IP banned " + p.getUsername());
					world.sendToAll("%r-" + "@red@ANNOUNCEMENT@whi@: " + p.getUsername() + " has been IP banned!");
					for(Player pl : world.getPlayers()) {
						if(pl.getCurrentIP().equalsIgnoreCase(p.getCurrentIP())) {
							pl.getActionSender().sendLogout();
							pl.destroy(true);
						}
					}
					world.getServer().loadBannedIP();
				}
				catch (Exception ex) {
					System.out.println(ex);
				}
			}
		}
		if((cmd.equalsIgnoreCase("setstat")) && (player.isAdmin())) {
			if(args.length < 2) {
				player.getActionSender().sendMessage("Invalid args. Syntax: SETSTAT [stat] [level]");
				return;
			}
			int stat = Formulae.getStatIndex(args[0]);
			int level = Integer.parseInt(args[1]);
			
			if((level < 0) || (level > 99)) {
				player.getActionSender().sendMessage("Invalid " + Formulae.statArray[stat] + " level.");
				return;
			}
			player.setCurStat(stat, level);
			player.setMaxStat(stat, level);
			player.setExp(stat, Formulae.lvlToXp(level));
			
			if((stat == 0) || (stat == 1) || (stat == 2)) {
				int hitpointsXp = Formulae.lvlToXp(player.getMaxStat(0)) + Formulae.lvlToXp(player.getMaxStat(1)) + Formulae.lvlToXp(player.getMaxStat(2)) + 1154;
				int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp / 3);
				if(hitpointsLVL < 10) {
					hitpointsLVL = 10;
					player.setCurStat(3, 10);
					player.setMaxStat(3, 10);
					player.setExp(3, 1155);
				} else {
					player.setCurStat(3, hitpointsLVL);
					player.setMaxStat(3, hitpointsLVL);
					player.setExp(3, Formulae.lvlToXp(hitpointsLVL));
				}
			}
			int comb = Formulae.getCombatlevel(player.getMaxStats());
			if(comb != player.getCombatLevel()) {
				player.setCombatLevel(comb);
			}
			player.getActionSender().sendInventory();
			player.getActionSender().sendStats();
			player.getActionSender().sendMessage("Your " + Formulae.statArray[stat] + " has been set to level " + level);
		}
		if((cmd.equalsIgnoreCase("reloadbans")) && (player.isAdmin())) {
			if(args.length >= 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: RELOADBANS");
				return;
			}
			world.getServer().loadBannedIP();
			player.getActionSender().sendMessage("You have successfully reloaded the list of banned IP addresses!");
			return;
		}
		if((cmd.equalsIgnoreCase("startparty")) && (player.isAdmin())) {
			if(World.dropparty == 1) {
				player.getActionSender().sendMessage("There's already a drop party due to start soon!");
				return;
			}
			if(world.getServer().startDropParty()) {
				for(Player p : world.getPlayers()) {
					p.getActionSender().sendAlert("There'll be a drop party starting in 60 seconds! To get to the party hall, please type @or1@::partyhall @whi@. This command will only work while the timer is running.", false);
					p.getActionSender().startDropParty(60);
					World.dropparty = 1;
				}
			}
			return;
		}
		if((cmd.equalsIgnoreCase("drop")) && (player.isAdmin())) {
			if((args.length < 1) || (args.length > 2)) {
				player.getActionSender().sendMessage("Invalid args. Syntax: DROP [id] [amount]");
			} else {
				final int id = Integer.parseInt(args[0]);
				if(EntityHandler.getItemDef(id) != null) {
					final int amount = ((args.length == 2) ? Integer.parseInt(args[1]) : 1);
					if(amount > MAX_DROP_PARTY_AMOUNT) {
						player.getActionSender().sendMessage("@red@WARNING: Spawning this many items will cause the server to lag/crash!");
					} else {
						Random rand = new Random(System.currentTimeMillis());
						Point dropPoint;
						for(int i = 0; i < amount; i++) {
							do {
								int x = rand.nextInt(11) + 490;
								int y = rand.nextInt(8) + 1408;
								dropPoint = Point.location(x, y);
						} while (dropPoint.inBounds(490, 1410, 491, 1412));
							Item item = new Item(id, dropPoint.getX(), dropPoint.getY(), (EntityHandler.getItemDef(id).isStackable() ? amount : 1), null);
							world.registerItem(item);
							if(EntityHandler.getItemDef(id).isStackable()) {
								break;
							}
						}
						String message = "Out of nowhere appears ";
						char[] vowels = {'A', 'E', 'I', 'O', 'U', 'a', 'e', 'i', 'o', 'u'};
						boolean hasVowel = false;
						for(char vowel : vowels) {
							if(EntityHandler.getItemDef(id).getName().charAt(0) == vowel) {
								message += "an: ";
								hasVowel = true;
								break;
							}
						}
						if(!hasVowel) {
							message += "a: ";
						}
						message += "@gre@" + EntityHandler.getItemDef(id).getName() + "!";
						for(Player p : world.getPlayers()) {
							if(p.getLocation().inBounds(490, 1408, 500, 1415)) {
								p.getActionSender().sendMessage(message);
							}
						}
					}
				} else {
					player.getActionSender().sendMessage("Invalid item ID!");
				}
			}
		}
		if((cmd.equalsIgnoreCase("checkip")) && (player.isAdmin())) {
			if(args.length == 1) {
				Player p = world.getPlayer(DataConversions.usernameToHash(args[0]));
				if(p != null) {
					String message = " Players on " + p.getCurrentIP() + ": ";
					for(Player pl : world.getPlayers()) {
						if(pl.getCurrentIP().equalsIgnoreCase(p.getCurrentIP())) {
							message = message + pl.getUsername() + ", ";
						}
					}
					player.getActionSender().sendMessage(message);
				} else {
					player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
				}
			} else {
				player.getActionSender().sendMessage("Invalid args. Syntax: CHECKIP [username]");
			}
		}
		String massMessage;
		Iterator yong;
		Player allPlayers;
		if((cmd.equalsIgnoreCase("massplayertalk")) && (player.isAdmin())) {
			massMessage = "";
			for(int i = 0; i < args.length; i++) {
				massMessage = massMessage = massMessage + args[i] + " ";
			}
			for(yong = world.getPlayers().iterator(); yong.hasNext(); ) { allPlayers = (Player)yong.next();
				for(Player Players : world.getPlayers())
					allPlayers.informOfChatMessage(new ChatMessage(Players, massMessage, allPlayers));
			}
		}
		if((cmd.equalsIgnoreCase("sub")) && (player.isAdmin())) {
			try {
				if(args.length < 1) {
					player.getActionSender().sendMessage("Invalid args. Syntax: SUB [username]");
					return;
				}
				if(player.isSub(args[0])) {
					player.getActionSender().sendMessage("That player is already a subscriber!");
					return;
				}
				Player p = world.getPlayer(DataConversions.usernameToHash(args[0]));
				player.getActionSender().sendMessage("You have added a subscription to " + p.getUsername());
				player.setSub(args[0]);
				
				if(GUI.isOnline(args[0])) {
					p.getActionSender().sendMessage("An administrator has added a 30 day subscription to your account");
					p.getActionSender().sendMessage("Please logout and back in for the full effects to take place!");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if((cmd.equalsIgnoreCase("startevent")) && (player.isAdmin())) {
			int eventLevel = Integer.parseInt(args[0]);
			int eventCoordX = Integer.parseInt(args[1]);
			int eventCoordY = Integer.parseInt(args[2]);
			if(args.length != 3) {
				player.getActionSender().sendMessage("Invalid args. Syntax: STARTEVENT [level] [x, y]");
				return;
			}
			if(world.eventStatus == 1) {
				player.getActionSender().sendMessage("Please reset the current event before starting a new event!");
				return;
			}
			world.eventLevel = eventLevel;
			world.eventCoordX = eventCoordX;
			world.eventCoordY = eventCoordY;
			world.eventStatus = 1;
			player.getActionSender().sendMessage("You have set the event level to: @or1@" + eventLevel + "@whi@ and the coords to: @or1@" + eventCoordX + ", " + eventCoordY);
			for(Player p : world.getPlayers()) {
				p.getActionSender().sendAlert("A server Administrator has started an event. The event will be hosted at: @or1@" + eventCoordX + ", " + eventCoordY + "@whi@ which will have a level requirement of: @or1@" + eventLevel + "@whi@. If you meet the requirements, please use the @or1@::event @whi@command to be teleported to the event location!", false);
			}
			return;
		}
		if((cmd.equalsIgnoreCase("resetevent")) && (player.isAdmin())) {
			world.eventLevel = 0;
			world.eventStatus = 0;
			world.eventCoordX = 0;
			world.eventCoordY = 0;
			player.getActionSender().sendMessage("You have successfully reset the event system!");
			return;
		}
		if((cmd.equalsIgnoreCase("info")) && (player.isAdmin())) {
			String otherPlayers = "";
			if(args.length == 1) {
				Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
				if(affectedPlayer != null) {
					for(Player p : world.getPlayers()) {
						if(p.getCurrentIP().equalsIgnoreCase(p.getCurrentIP())) {
							otherPlayers = otherPlayers + p.getUsername() + ", ";
						}
					}
					player.getActionSender().sendAlert(affectedPlayer.getUsername() + " is currently at @or1@" + affectedPlayer.getLocation().toString() + "@whi@ (@or1@" + affectedPlayer.getLocation().getDescription() + "@whi@). They are currently logged in from the IP: @or1@" + affectedPlayer.getCurrentIP() + "@whi@. Their state is @or1@" + affectedPlayer.getStatus() + "@whi@. They last moved @or1@" + (int)((System.currentTimeMillis() - affectedPlayer.getLastMoved()) / 1000) + " @whi@seconds ago. Chat block is @or1@" + ((affectedPlayer.getPrivacySetting(0)) ? "on" : "off") + "@whi@. Their fatigue is at @or1@" + affectedPlayer.getFatigue() + "%@whi@. Other players logged on this IP are: @or1@" + otherPlayers, false);
				} else {
					player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
				}
			} else {
				player.getActionSender().sendMessage("Invalid args. Syntax: INFO [username]");
			}
			return;
		}
			
		/**
		 * Moderator Commands
		 */
		if((cmd.equalsIgnoreCase("modroom")) && (player.isMod())) {
			player.teleport(70, 1640, true);
			return;
		}
		if((cmd.equalsIgnoreCase("kick")) && (player.isMod())) {
			long PlayerHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(PlayerHash);
			for(Player pl : world.getPlayers()) {
				if(p != null) {
					p.getActionSender().sendLogout();
					p.destroy(true);
					pl.getActionSender().sendMessage("%r-" + "@red@ANNOUNCEMENT: @whi@" + p.getUsername() + " has been kicked from the server!");
				} else {
					player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
				}
			}
			return;
		}
		if((cmd.equalsIgnoreCase("global")) && (player.isMod())) {
			String globalMsg = "";
			for(int i = 0; i < args.length; i++) {
				globalMsg = globalMsg + args[i] + " ";
			}
			for(Player p : world.getPlayers()) {
				p.getActionSender().sendAlert("#mod# @gry@" + player.getUsername() + "@whi@: " + globalMsg, false);
			}
			return;
		}
		if((cmd.equalsIgnoreCase("goto")) || (cmd.equalsIgnoreCase("summon")) && (player.isMod())) {
			boolean summon = cmd.equalsIgnoreCase("summon");
			if(args.length != 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: " + (summon ? "SUMMON" : "GOTO") + " [username]");
				return;
			}
			long usernameHash = DataConversions.usernameToHash(args[0]);
			Player affectedPlayer = world.getPlayer(usernameHash);
			if(affectedPlayer != null) {
				if(summon) {
					affectedPlayer.teleport(player.getX(), player.getY(), true);
					affectedPlayer.getActionSender().sendMessage("You have been summoned by " + player.getUsername());
				} else {
					player.teleport(affectedPlayer.getX(), affectedPlayer.getY(), true);
				}
			} else {
				player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
			}
			return;
		}
		if((cmd.equalsIgnoreCase("take")) || (cmd.equalsIgnoreCase("put")) && (player.isMod())) {
			if(args.length != 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: TAKE [username]");
				return;
			}
			Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
			if(affectedPlayer == null) {
				player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
				return;
			}
			affectedPlayer.teleport(78, 1642, true);
			world.sendToAll("%r-" + "@red@ANNOUNCEMENT@whi@: " + affectedPlayer.getUsername() + " has been jailed!");
			if((cmd.equalsIgnoreCase("take")) && (player.isMod())) {
				player.teleport(76, 1642, true);
			}
			return;
		}
		if((cmd.equalsIgnoreCase("send")) && (player.isMod())) {
			if(args.length != 3) {
				player.getActionSender().sendMessage("Invalid args. Syntax: SEND [username] [x] [y]");
				return;
			}
			long usernameHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(usernameHash);
			int x = Integer.parseInt(args[1]);
			int y = Integer.parseInt(args[2]);
			if((world.withinWorld(x, y)) && (p != null)) {
				p.getActionSender().sendMessage("You have been teleported by " + player.getUsername());
				p.teleport(x, y, true);
			} else {
				player.getActionSender().sendMessage("Invalid coordinates or player!");
			}
			return;
		}
		if((cmd.equalsIgnoreCase("ban")) || (cmd.equalsIgnoreCase("unban")) && (player.isMod())) {
			long PlayerHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(PlayerHash);
			boolean banned = cmd.equalsIgnoreCase("ban");
			if(args.length != 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: " + (banned ? "BAN" : "UNBAN") + " [username]");
				return;
			}
			if(banned) {
				if(Integer.valueOf(GUI.readValue(args[0], "rank")) == 6) {
					player.getActionSender().sendMessage("That player is already banned!");
					return;
				} else {
					world.banPlayer(args[0]);
					world.sendToAll("%r-" + "@red@ANNOUNCEMENT@whi@: " + p.getUsername() + " has been banned!");
				}
			} else {
				if(Integer.valueOf(GUI.readValue(args[0], "rank")) == 6) {
					world.unbanPlayer(args[0]);
				} else {
					player.getActionSender().sendMessage("That player is not banned!");
				}
			}
			return;
		}
		if((cmd.equalsIgnoreCase("appearance")) && (player.isMod())) {
			player.setChangingAppearance(true);
			player.getActionSender().sendAppearanceScreen();
			return;
		}
		if((cmd.equalsIgnoreCase("reset")) && (player.isMod())) {
			long PlayerHash = DataConversions.usernameToHash(args[0]);
			Player p = world.getPlayer(PlayerHash);
			int stat = Formulae.getStatIndex(args[1]);
			int level = Integer.parseInt(args[2]);
			
			if((level < 1) || (level > 40)) {
				player.getActionSender().sendMessage("Invalid " + Formulae.statArray[stat] + " level.");
				return;
			}
			if(p.getMaxStat(stat) > 39) {
				player.getActionSender().sendMessage("That player's " + Formulae.statArray[stat] + " level is too high to be reset!");
				return;
			}
			if(stat > 6) {
				player.getActionSender().sendMessage("You can only reset another player's combat stats!");
				return;
			}
			if(stat == 3) {
				player.getActionSender().sendMessage("You can't alter another player's hits level!");
				return;
			}
			p.setCurStat(stat, level);
			p.setMaxStat(stat, level);
			p.setExp(stat, Formulae.lvlToXp(level));
			
			if((stat == 0) || (stat == 1) || (stat == 2)) {
				int hitpointsexp = Formulae.lvlToXp(p.getMaxStat(0)) + Formulae.lvlToXp(p.getMaxStat(1)) + Formulae.lvlToXp(p.getMaxStat(2)) + 1154;
				int hitpointslevel = Formulae.experienceToLevel(hitpointsexp / 3);
				if(hitpointslevel < 10) {
					hitpointslevel = 10;
					p.setCurStat(3, 10);
					p.setMaxStat(3, 10);
					p.setExp(3, 1154);
				} else {
					p.setCurStat(3, hitpointslevel);
					p.setMaxStat(3, hitpointslevel);
					p.setExp(3, Formulae.lvlToXp(hitpointslevel));
				}
			}
			int combat = Formulae.getCombatlevel(p.getMaxStats());
			if(combat != p.getCombatLevel()) {
				p.setCombatLevel(combat);
			}
			p.getActionSender().sendStats();
			if(p.getUsername() == player.getUsername()) {
				player.getActionSender().sendMessage("You have updated your " + Formulae.statArray[stat] + " to level " + args[2] + ".");
			} else {
				p.getActionSender().sendMessage(player.getUsername() + " has updated your " + Formulae.statArray[stat] + " to level " + args[2] + ".");
				player.getActionSender().sendMessage("You have updated " + p.getUsername() + "'s " + Formulae.statArray[stat] + " to level " + args[2] + ".");
			}
			return;
		}
		if((cmd.equalsIgnoreCase("announcement")) && (player.isMod())) {
			String newAnnouncement = "";
			for(int i = 0; i < args.length; i++) {
				newAnnouncement = newAnnouncement + args[i] + " ";
			}
			for(Player p : World.getWorld().getPlayers()) {
				p.getActionSender().sendMessage("%r-" + "@red@ANNOUNCEMENT@whi@: " + newAnnouncement);
			}
			return;
		}
		if((cmd.equalsIgnoreCase("mute")) && (player.isMod())) {
			boolean mute = cmd.equalsIgnoreCase("mute");
			if(args.length != 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: MUTE [username]");
				return;
			}
			if(mute) {
				if(Integer.valueOf(GUI.readValue(args[0], "mute")) == 1) {
					player.getActionSender().sendMessage("That player is already @red@MUTED");
					return;
				} else {
					world.mutePlayer(args[0]);
					Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
					player.getActionSender().sendMessage("You have @red@muted @whi@" + affectedPlayer.getUsername() + "!");
					affectedPlayer.getActionSender().sendMessage("You have been @red@muted @whi@by: " + player.getUsername());
					affectedPlayer.getActionSender().sendLogout();
					affectedPlayer.destroy(true);
				}
				Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
				for(Player muted : world.getPlayers()) {
					muted.getActionSender().sendMessage("%r-" + "@red@ANNOUNCEMENT: @whi@" + affectedPlayer.getUsername() + " has been @red@MUTED@whi@!");
				}
				if(affectedPlayer == null) {
					player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
					return;
				}
				affectedPlayer.getActionSender().sendMute();
				return;
			}
		}
		if((cmd.equalsIgnoreCase("unmute")) && (player.isMod())) {
			boolean unmute = cmd.equalsIgnoreCase("unmute");
			if(args.length != 1) {
				player.getActionSender().sendMessage("Invalid args. Syntax: UNMUTE [username]");
				return;
			}
			if(unmute) {
				if(Integer.valueOf(GUI.readValue(args[0], "mute")) == 0) {
					player.getActionSender().sendMessage("That player is not muted!");
					return;
				} else {
					if(Integer.valueOf(GUI.readValue(args[0], "mute")) == 1) {
						world.unMutePlayer(args[0]);
						Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
						player.getActionSender().sendMessage("You have @gre@unmuted @whi@" + affectedPlayer.getUsername());
						affectedPlayer.getActionSender().sendMessage("You have been @gre@unmuted@whi@!");
						affectedPlayer.getActionSender().sendLogout();
						affectedPlayer.destroy(true);
					}
					Player affectedPlayer = world.getPlayer(DataConversions.usernameToHash(args[0]));
					for(Player muted : world.getPlayers()) {
						muted.getActionSender().sendMessage("%r-" + "@red@ANNOUNCEMENT: @whi@" + affectedPlayer.getUsername() + " has been @gre@UNMUTED@whi@!");
					}
					if(affectedPlayer == null) {
						player.getActionSender().sendMessage("Invalid player. Please make sure they are online!");
						return;
					}
					affectedPlayer.getActionSender().sendMute();
					return;
				}
			}
		}
		if((cmd.equalsIgnoreCase("teleport")) && (player.isMod())) {
			if(args.length != 2) {
				if(args[0].equals("modroom")) {
					player.teleport(72, 1641, false);
				} else if(args[0].equals("edgeville")) {
					player.teleport(225, 447, false);
				} else if(args[0].equals("varrock")) {
					player.teleport(122, 503, false);
				} else if(args[0].equals("falador")) {
					player.teleport(313, 550, false);
				} else if(args[0].equals("seers")) {
					player.teleport(501, 455, false);
				} else if(args[0].equals("catherby")) {
					player.teleport(440, 500, false);
				} else if(args[0].equals("yanille")) {
					player.teleport(587, 761, false);
				} else if(args[0].equals("karamja")) {
					player.teleport(371, 695, false);
				} else if(args[0].equals("ardougne")) {
					player.teleport(585, 621, false);
				} else if(args[0].equals("draynor")) {
					player.teleport(214, 632, false);
				} else if(args[0].equals("lumbridge")) {
					player.teleport(122, 647, false);
				} else {
					player.getActionSender().sendMessage("Invalid location!");
				}
			}
			else if((args.length == 2) && (player.isMod())) {
				int x = Integer.parseInt(args[0]);
				int y = Integer.parseInt(args[1]);
				if(world.withinWorld(x, y)) {
					player.teleport(x, y, true);
				} else {
					player.getActionSender().sendMessage("Invalid coordinates!");
				}
				return;
			}
		}
	}
}