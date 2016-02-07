package org.rscdaemon.server;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.packetbuilder.client.*;
import org.rscdaemon.server.util.EntityList;

import java.util.List;

public final class ClientUpdater {
	private static World world = World.getWorld();
	private EntityList<Player> players = world.getPlayers();
	private EntityList<Npc> npcs = world.getNpcs();
	private PlayerPositionPacketBuilder playerPositionBuilder = new PlayerPositionPacketBuilder();
	private PlayerUpdatePacketBuilder playerApperanceBuilder = new PlayerUpdatePacketBuilder();
	private GameObjectPositionPacketBuilder gameObjectPositionBuilder = new GameObjectPositionPacketBuilder();
	private WallObjectPositionPacketBuilder wallObjectPositionPacketBuilder = new WallObjectPositionPacketBuilder();
	private ItemPositionPacketBuilder itemPositionBuilder = new ItemPositionPacketBuilder();
	private NpcPositionPacketBuilder npcPositionPacketBuilder = new NpcPositionPacketBuilder();
	private NpcUpdatePacketBuilder npcApperanceBuilder = new NpcUpdatePacketBuilder();

	public ClientUpdater() {
		world.setClientUpdater(this);
	}

	public void updateClients() {
		GUI.repaintVars();
		updateNpcPositions();
		updatePlayersPositions();
		updateMessageQueues();
		updateOffers();
		GUI.populateWorldList();
		if(GUI.lastClickedName != null)
			GUI.refreshWorldList(GUI.lastClickedName);
		for(Player p : players) {
			updateTimeouts(p);

			// Must be done in the right order!
			updatePlayerPositions(p);
			updateNpcPositions(p);
			updateGameObjects(p);
			updateWallObjects(p);
			updateItems(p);
			
			updatePlayerApperances(p);
			updateNpcApperances(p);
		}
		updateCollections();
	}
	
	/**
	 * Update the position of npcs, and check if who (and what) they are aware of needs updated
	 */
	private void updateNpcPositions() {
		for(Npc n : npcs) {
			n.resetMoved();
			n.updatePosition();
			n.updateAppearanceID();
		}
	}
	
	/**
	 * Update the position of players, and check if who (and what) they are aware of needs updated
	 */
	private void updatePlayersPositions() {
		for(Player p : players) {
			p.resetMoved();
			p.updatePosition();
			p.updateAppearanceID();
		}
		for(Player p : players) {
			p.revalidateWatchedPlayers();
			p.revalidateWatchedObjects();
			p.revalidateWatchedItems();
			p.revalidateWatchedNpcs();
			p.updateViewedPlayers();
			p.updateViewedObjects();
			p.updateViewedItems();
			p.updateViewedNpcs();
		}
	}
	
	/**
	 * Updates the messages queues for each player
	 */
	private void updateMessageQueues() {
		for(Player sender : players) {
			ChatMessage message = sender.getNextChatMessage();
			if(message == null || !sender.loggedIn()) {
				continue;
			}
			for(Player recipient : sender.getViewArea().getPlayersInView()) {
				if(sender.getIndex() == recipient.getIndex() || !recipient.loggedIn()) {
					continue;
				}
				if(recipient.getPrivacySetting(0) && !recipient.isFriendsWith(sender.getUsername()) && !sender.isPMod()) {
					continue;
				}
				if(recipient.isIgnoring(sender.getUsername()) && !sender.isPMod()) {
					continue;
				}
				recipient.informOfChatMessage(message);
			}
		}
	}
	
	public void updateOffers() {
		for(Player player : players) {
			if(!player.requiresOfferUpdate()) {
				continue;
			}
			player.setRequiresOfferUpdate(false);
			if(player.isTrading()) {
				Player affectedPlayer = player.getWishToTrade();
				if(affectedPlayer == null) {
					continue;
				}
				affectedPlayer.getActionSender().sendTradeItems();
			}
			else if(player.isDueling()) {
				Player affectedPlayer = player.getWishToDuel();
				if(affectedPlayer == null) {
					continue;
				}
				player.getActionSender().sendDuelSettingUpdate();
				affectedPlayer.getActionSender().sendDuelSettingUpdate();
				affectedPlayer.getActionSender().sendDuelItems();
			}
		}
	}
	
	/**
	 * Sends queued packets to each player
	 */
	public void sendQueuedPackets() {
		for(Player p : players) {
			List<RSCPacket> packets = p.getActionSender().getPackets();
			for(RSCPacket packet : packets) {
				p.getSession().write(packet);
			}
			p.getActionSender().clearPackets();
			if(p.destroyed()) {
				p.getSession().close();
				p.remove();
			}
		}
	}
	
	/**
	 * Checks the player has moved within the last 5mins
	 */
	private void updateTimeouts(Player p) {
		if(p.destroyed()) {
			return;
		}
		long curTime = System.currentTimeMillis();
	      	if(curTime - p.getLastPing() >= 30000) {
			p.destroy(false);
	      	}
		else if(p.warnedToMove()) {
			if(curTime - p.getLastMoved() >= 960000 && p.loggedIn()) {
				p.destroy(false);
			}
		}
	      	else if(curTime - p.getLastMoved() >= 900000) {
			p.getActionSender().sendMessage("@cya@You have not moved for 15 mins, please move to a new area to avoid logout.");
			p.warnToMove();
	      	}
	}
	
	/**
	 * Sends updates for npcs to the given player
	 */
	private void updateNpcPositions(Player p) {
		npcPositionPacketBuilder.setPlayer(p);
		RSCPacket temp = npcPositionPacketBuilder.getPacket();
		if(temp != null) {
			p.getSession().write(temp);
		}
	}
	
	/**
	 * Update appearance of any npcs the given player should be aware of
	 */
	private void updateNpcApperances(Player p) {
		npcApperanceBuilder.setPlayer(p);
		RSCPacket temp = npcApperanceBuilder.getPacket();
		if (temp != null) {
			p.getSession().write(temp);
		}
	}
	
	/**
	 * Sends updates for wall objects to the given player
	 */
	private void updateWallObjects(Player p) {
		wallObjectPositionPacketBuilder.setPlayer(p);
		RSCPacket temp = wallObjectPositionPacketBuilder.getPacket();
		if(temp != null) {
			p.getSession().write(temp);
		}
	}
	
	/**
	 * Sends updates for game objects to the given player
	 */
	private void updateGameObjects(Player p) {
		gameObjectPositionBuilder.setPlayer(p);
		RSCPacket temp = gameObjectPositionBuilder.getPacket();
		if(temp != null) {
			p.getSession().write(temp);
		}
	}
	
	/**
	 * Sends updates for game items to the given player
	 */
	private void updateItems(Player p) {
		itemPositionBuilder.setPlayer(p);
		RSCPacket temp = itemPositionBuilder.getPacket();
		if(temp != null) {
			p.getSession().write(temp);
		}
	}
	
	/**
	 * Update positions of the given player, and any players they should be aware of
	 */
	private void updatePlayerPositions(Player p) {
		playerPositionBuilder.setPlayer(p);
		RSCPacket temp = playerPositionBuilder.getPacket();
		if(temp != null) {
			p.getSession().write(temp);
		}
	}
	
	/**
	 * Update appearance of the given player, and any players they should be aware of
	 */
	private void updatePlayerApperances(Player p) {
		playerApperanceBuilder.setPlayer(p);
		RSCPacket temp = playerApperanceBuilder.getPacket();
		if (temp != null) {
			p.getSession().write(temp);
		}
	}
	
	/**
	 * Updates collections, new becomes known, removing is removed etc.
	 */
	private void updateCollections() {
		for (Player p : players) {
			if(p.isRemoved() && p.initialized()) {
				world.unregisterPlayer(p);
			}
		}
		for (Player p : players) {
			p.getWatchedPlayers().update();
			p.getWatchedObjects().update();
			p.getWatchedItems().update();
			p.getWatchedNpcs().update();
			
			p.clearProjectilesNeedingDisplayed();
			p.clearPlayersNeedingHitsUpdate();
			p.clearNpcsNeedingHitsUpdate();
			p.clearChatMessagesNeedingDisplayed();
			p.clearNpcMessagesNeedingDisplayed();
			p.clearBubblesNeedingDisplayed();
			
			p.resetSpriteChanged();
			p.setAppearnceChanged(false);
		}
		for(Npc n : npcs) {
			n.resetSpriteChanged();
			n.setAppearnceChanged(false);
		}
	}
}
