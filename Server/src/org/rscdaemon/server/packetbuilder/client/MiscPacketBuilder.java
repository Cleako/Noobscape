package org.rscdaemon.server.packetbuilder.client;

import org.rscdaemon.server.GameVars;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.packetbuilder.RSCPacketBuilder;
import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.util.Formulae;
import org.rscdaemon.server.util.Config;

import java.util.ArrayList;
import java.util.List;

public class MiscPacketBuilder {
	/**
	 * The player we are creating packets for
	 */
	private Player player;
	/**
	 * List of packets waiting to be sent to the user
	 */
	private List<RSCPacket> packets = new ArrayList<RSCPacket>();
	
	/**
	 * Constructs a new MiscPacketBuilder
	 */
	public MiscPacketBuilder(Player player) {
		this.player = player;
	}
	
	/**
	 * Gets a List of new packets since the last update
	 */
	public List<RSCPacket> getPackets() {
		return packets;
	}
	
	/**
	 * Clears old packets that have already been sent
	 */
	public void clearPackets() {
		packets.clear();
	}
	
	/**
	 * Tells the client to save a screenshot
	 */
	public void sendScreenshot() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(181);
		packets.add(s.toPacket());
	}
	
	/**
	 * Sends the players combat style
	 */
	public void sendCombatStyle() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(129);
		s.addByte((byte)player.getCombatStyle());
		packets.add(s.toPacket());
	}

	/**
	 * Wilderness Switch
	 */
	public void startWildernessSwitch(int seconds) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(174);
		s.addShort((int)(((double)seconds / 32D) * 50));
		packets.add(s.toPacket());
	}
	
	/**
	 * Updates the tutorial status percentage
	 */
	public void sendTutorialStatus() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(133);
		s.addShort(player.getTutorialStatus());
		packets.add(s.toPacket());
	}
	
	/**
	 * Updates the fatigue percentage
	 */
	public void sendFatigue() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(126);
		s.addShort(player.getFatigue());
		packets.add(s.toPacket());
	}

	/**
	 * Current Task - Yong Min
	 */
	public void sendMoneyTask() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(205);
		s.addBytes(player.moneyTask().getBytes());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the kills whats left - Yong Min
	 */
	public void sendRemaining() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(204);
		s.addShort(player.getRemaining());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the current task - Yong Min
	 */
	public void sendmoneyTask(String asd, boolean task) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(205);
		s.addBytes(asd.getBytes());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the task points - Yong Min
	 */
	public void sendTaskPoints() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(202);
		s.addShort(player.getTaskPoints());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the completed tasks - Yong Min
	 */
	public void sendCompletedTasks() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(203);
		s.addShort(player.getCompletedTasks());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the task status @author Yong Min
	 */
	public void sendTaskStatus() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(214);
		if(player.getTask() == true) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}	

	/**
	 * Updates the task exp - Yong Min
	 */
	public void sendExperienceReward() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(215);
		s.addShort(player.getRandomXp());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the task cash - Yong Min
	 */
	public void sendCashReward() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(216);
		s.addShort(player.getRandomCash());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the task item - Yong Min
	 */
	public void sendItemReward() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(217);
		s.addShort(player.getRandomItem());
		packets.add(s.toPacket());
	}

	/**
	 * Demon Slayer @author Yong Min
	 */
	public void sendDemonSlayerComplete() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(146);
		if(player.getDemonSlayerStatus() == 6) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}

	/**
	 * Restless Ghost @author Yong Min
	 */
	public void sendRestlessGhostComplete() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(149);
		if(player.getRestlessGhostStatus() == 5) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}

	/**
	 * Pirate's Treasure @author Yong Min
	 */
	public void sendPiratesTreasureComplete() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(150);
		if(player.getPiratesTreasureStatus() == 7) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}
	
	/**
	 * Ernest the Chicken @author Yong Min
	 */
	public void sendErnestTheChickenComplete() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(151);
		if(player.getErnestTheChickenStatus() == 4) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}

	/**
	 * Druidic Ritual @author Yong Min
	 */
	public void sendDruidicRitualComplete() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(137);
		if(player.getDruidicRitualStatus() == 4) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}

	/**
	 * Romeo & Juliet @author Yong Min
	 */
	public void sendRomeoJulietComplete() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(140);
		if(player.getRomeoJulietStatus() == 3) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}

	/**
	 * Sheep Shearer @author Yong Min
	 */
	public void sendSheepShearerComplete() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(141);
		if(player.getSheepShearerStatus() == 2) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}

	/**
	 * Imp Catcher @author Yong Min
	 */
	public void sendImpCatcherComplete() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(138);
		if(player.getImpCatcherStatus() == 2) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}

	/**
	 * Witches Potion @author Yong Min
	 */
	public void sendWitchPotionComplete() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(142);
		if(player.getWitchPotionStatus() == 3) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}

	/**
	 * Doric's Quest @author Yong Min
	 */
	public void sendDoricsQuestComplete() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(143);
		if(player.getDoricsQuestStatus() == 2) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}

	/**
	 * Cook's Assistant @author Yong Min
	 */
	public void sendCooksAssistantComplete() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(144);
		if(player.getCooksAssistantStatus() == 2) {
			s.addShort(1);
		}else{s.addShort(0);}
		packets.add(s.toPacket());
	}

	/**
	 * Mute System
	 */
	public void sendMute() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(2000);
		s.addShort(player.getMute());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the quest points @author Yong Min
	 */
	public void sendQuestPoints() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(128);
		s.addShort(player.getQuestPoints());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the kills @author Yong Min
	 */
	public void sendKills() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(132);
		s.addShort(player.getKills());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the deaths @author Yong Min
	 */
	public void sendDeaths() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(134);
		s.addShort(player.getDeaths());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the killing spree @author Yong Min
	 */
	public void sendKillingSpree() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(206);
		s.addShort(player.getKillingSpree());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the guthix casts @author Yong Min
	 */
	public void sendGuthixSpellCast() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(210);
		s.addShort(player.getGuthixSpellCast());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the saradomin casts @author Yong Min
	 */
	public void sendSaradominSpellCast() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(213);
		s.addShort(player.getSaradominSpellCast());
		packets.add(s.toPacket());
	}

	/**
	 * Updates the zamorak casts @author Yong Min
	 */
	public void sendZamorakSpellCast() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(212);
		s.addShort(player.getZamorakSpellCast());
		packets.add(s.toPacket());
	}
	
	/**
	 * Hides a question menu
	 */
	public void hideMenu() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(127);
		packets.add(s.toPacket());
	}
	
	/**
	 * Shows a question menu
	 */
	public void sendMenu(String[] options) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(223);
		s.addByte((byte)options.length);
		for(String option : options) {
			s.addByte((byte)option.length());
			s.addBytes(option.getBytes());
		}
		packets.add(s.toPacket());
	}
	
	/**
	 * Show the bank window
	 */
	public void showBank() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(93);
		s.addByte((byte)player.getBank().size());
		s.addByte((byte)Bank.MAX_SIZE);
		for(InvItem i : player.getBank().getItems()) {
			s.addShort(i.getID());
			s.addInt(i.getAmount());
		}
		packets.add(s.toPacket());
	}
	
	/**
	* Hides the bank windows
	*/
	public void hideBank() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(171);
		packets.add(s.toPacket());
	}
	
	/**
	 * Updates the id and amount of an item in the bank
	 */
	public void updateBankItem(int slot, int newId, int amount) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(139);
		s.addByte((byte)slot);
		s.addShort(newId);
		s.addInt(amount);
		packets.add(s.toPacket());
	}
	
	/**
	 * Show the bank window
	 */
	public void showShop(Shop shop) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(253);
		s.addByte((byte)shop.size());
		s.addByte((byte)(shop.isGeneral() ? 1 : 0));
		s.addByte((byte)shop.getSellModifier());
		s.addByte((byte)shop.getBuyModifier());
		for(InvItem i : shop.getItems()) {
			s.addShort(i.getID());
			s.addShort(i.getAmount());
		}
		packets.add(s.toPacket());
	}
	
	/**
	 * Hides the shop window
	 */
	public void hideShop() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(220);
		packets.add(s.toPacket());
	}
	
	/**
	 * Sends a system update message
	 */
	public void startShutdown(int seconds) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(172);
		s.addShort((int)(((double)seconds / 32D) * 50));
		packets.add(s.toPacket());
	}

	/**
	 * Drop Party
	 */
	public void startDropParty(int seconds) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(175);
		s.addShort((int)(((double)seconds / 32D) * 50));
		packets.add(s.toPacket());
	}

	/**
	 * PvP tournament timer.
	 */
	public void startPvp(int seconds) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(173);
		s.addShort((int)(((double)seconds / 32D) * 50));
		System.out.println("Seconds:  "+seconds+"\tpID:  555\tData sent:  "+(int)(((double)seconds / 32D) * 50)+"\tData Recieved:  "+(int)(((double)seconds / 32D) * 50)*32);
		packets.add(s.toPacket());
	}
	
	/**
	 * Sends a message box
	 */
	public void sendAlert(String message, boolean big) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(big ? 64 : 148);
		s.addBytes(message.getBytes());
		packets.add(s.toPacket());
	}
	
	/**
	 * Sends a sound effect
	 */
	public void sendSound(String soundName) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(11);
		s.addBytes(soundName.getBytes());
		packets.add(s.toPacket());
	}
	
	/**
	 * Alert the client that they just died
	 */
	public void sendDied() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(165);
		packets.add(s.toPacket());
	}
	
	/**
	 * Send a private message
	 */
	public void sendPrivateMessage(long usernameHash, byte[] message) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(170);
		s.addLong(usernameHash);
		s.addBytes(message);
		packets.add(s.toPacket());
	}
	
	/**
	 * Updates a friends login status
	 */
	public void sendFriendUpdate(long usernameHash, int world) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(25);
		s.addLong(usernameHash);
		s.addByte((byte)(world == Config.SERVER_NUM ? 99 : world));
		packets.add(s.toPacket());
	}
	
	/**
	 * Sends the whole friendlist
	 */
	public void sendFriendList() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(249);
		s.addByte((byte)player.getFriendList().size());
		for(String friend : player.getFriendList()) {
			
			s.addLong(org.rscdaemon.server.util.DataConversions.usernameToHash(friend));
			s.addByte((byte)99);
		}
		packets.add(s.toPacket());
	}
	
	/**
	 * Sends the whole ignore list
	 */
	public void sendIgnoreList() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(2);
		s.addByte((byte)player.getIgnoreList().size());
		for(String user : player.getIgnoreList()) {
			s.addLong(org.rscdaemon.server.util.DataConversions.usernameToHash(user));
		}
		packets.add(s.toPacket());
	}
	
	public void sendTradeAccept() {
		Player with = player.getWishToTrade();
		if(with == null) { // This shouldn't happen
			return;
		}
	      	RSCPacketBuilder s = new RSCPacketBuilder();
	      	s.setID(251);
	      	s.addLong(with.getUsernameHash());
	      	s.addByte((byte)with.getTradeOffer().size());
	      	for(InvItem item : with.getTradeOffer()) {
	      		s.addShort(item.getID());
	      		s.addInt(item.getAmount());
	      	}
	      	s.addByte((byte)player.getTradeOffer().size());
	      	for(InvItem item : player.getTradeOffer()) {
	      		s.addShort(item.getID());
	      		s.addInt(item.getAmount());
	      	}
	      	packets.add(s.toPacket());
	}
	
	public void sendDuelAccept() {
		Player with = player.getWishToDuel();
		if(with == null) { // This shouldn't happen
			return;
		}
	      	RSCPacketBuilder s = new RSCPacketBuilder();
	      	s.setID(147);
	      	s.addLong(with.getUsernameHash());
	      	s.addByte((byte)with.getDuelOffer().size());
	      	for(InvItem item : with.getDuelOffer()) {
	      		s.addShort(item.getID());
	      		s.addInt(item.getAmount());
	      	}
	      	s.addByte((byte)player.getDuelOffer().size());
	      	for(InvItem item : player.getDuelOffer()) {
	      		s.addShort(item.getID());
	      		s.addInt(item.getAmount());
	      	}
	      	
                s.addByte((byte)(player.getDuelSetting(0) ? 1 : 0)); // duelCantRetreat = data[i7++] & 0xff;
                s.addByte((byte)(player.getDuelSetting(1) ? 1 : 0)); // duelUseMagic = data[i7++] & 0xff;
                s.addByte((byte)(player.getDuelSetting(2) ? 1 : 0)); // duelUsePrayer = data[i7++] & 0xff;
                s.addByte((byte)(player.getDuelSetting(3) ? 1 : 0)); // duelUseWeapons = data[i7++] & 0xff;
                
	      	packets.add(s.toPacket());
	}
	
	public void sendTradeAcceptUpdate() {
		Player with = player.getWishToTrade();
		if(with == null) { // This shouldn't happen
			return;
		}
	      	RSCPacketBuilder s1 = new RSCPacketBuilder();
	      	s1.setID(18);
	      	s1.addByte((byte)(player.isTradeOfferAccepted() ? 1 : 0));
	      	packets.add(s1.toPacket());
	      	
	      	RSCPacketBuilder s2 = new RSCPacketBuilder();
	      	s2.setID(92);
	      	s2.addByte((byte)(with.isTradeOfferAccepted() ? 1 : 0));
	      	packets.add(s2.toPacket());
	}
	
	public void sendDuelAcceptUpdate() {
		Player with = player.getWishToDuel();
		if(with == null) { // This shouldn't happen
			return;
		}
	      	RSCPacketBuilder s1 = new RSCPacketBuilder();
	      	s1.setID(97);
	      	s1.addByte((byte)(player.isDuelOfferAccepted() ? 1 : 0));
	      	packets.add(s1.toPacket());
	      	
	      	RSCPacketBuilder s2 = new RSCPacketBuilder();
	      	s2.setID(65);
	      	s2.addByte((byte)(with.isDuelOfferAccepted() ? 1 : 0));
	      	packets.add(s2.toPacket());
	}
	
	public void sendDuelSettingUpdate() {
	      	RSCPacketBuilder s = new RSCPacketBuilder();
	      	s.setID(198);
	      	s.addByte((byte)(player.getDuelSetting(0) ? 1 : 0));
	      	s.addByte((byte)(player.getDuelSetting(1) ? 1 : 0));
	      	s.addByte((byte)(player.getDuelSetting(2) ? 1 : 0));
	      	s.addByte((byte)(player.getDuelSetting(3) ? 1 : 0));
	      	packets.add(s.toPacket());
	}
	
	public void sendTradeItems() {
		Player with = player.getWishToTrade();
		if(with == null) { // This shouldn't happen
			return;
		}
		ArrayList<InvItem> items = with.getTradeOffer();
	      	RSCPacketBuilder s = new RSCPacketBuilder();
	      	s.setID(250);
	      	s.addByte((byte)items.size());
	      	for(InvItem item : items) {
	      		s.addShort(item.getID());
	      		s.addInt(item.getAmount());
	      	}
	      	packets.add(s.toPacket());
	}
	
	public void sendDuelItems() {
		Player with = player.getWishToDuel();
		if(with == null) { // This shouldn't happen
			return;
		}
		ArrayList<InvItem> items = with.getDuelOffer();
	      	RSCPacketBuilder s = new RSCPacketBuilder();
	      	s.setID(63);
	      	s.addByte((byte)items.size());
	      	for(InvItem item : items) {
	      		s.addShort(item.getID());
	      		s.addInt(item.getAmount());
	      	}
	      	packets.add(s.toPacket());
	}
	
	public void sendTradeWindowOpen() {
		Player with = player.getWishToTrade();
		if(with == null) { // This shouldn't happen
			return;
		}
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(4);
		s.addShort(with.getIndex());
		packets.add(s.toPacket());
	}
	
	public void sendDuelWindowOpen() {
		Player with = player.getWishToDuel();
		if(with == null) { // This shouldn't happen
			return;
		}
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(229);
		s.addShort(with.getIndex());
		packets.add(s.toPacket());
	}
	
	public void sendTradeWindowClose() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(187);
		packets.add(s.toPacket());
	}
	
	public void sendDuelWindowClose() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(160);
		packets.add(s.toPacket());
	}
	
	public void sendAppearanceScreen() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(207);
		packets.add(s.toPacket());
	}
	
	public void sendServerInfo() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(110);
		s.addLong(Config.START_TIME);
		s.addBytes(GameVars.serverLocation.getBytes());
		packets.add(s.toPacket());
	}
	
	public void sendTeleBubble(int x, int y, boolean grab) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(23);
		s.addByte((byte)(grab ? 1 : 0));
		s.addByte((byte)(x - player.getX()));
		s.addByte((byte)(y - player.getY()));
		packets.add(s.toPacket());
	}
	
	public void sendMessage(String message) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(48);
		s.addBytes(message.getBytes());
		packets.add(s.toPacket());
	}
	
	public void sendRemoveItem(int slot) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(191);
		s.addByte((byte)slot);
		packets.add(s.toPacket());
	}
	
	public void sendUpdateItem(int slot) {
		InvItem item = player.getInventory().get(slot);
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(228);
		s.addByte((byte)slot);
		s.addShort(item.getID() + (item.isWielded() ? 32768 : 0));
		if(item.getDef().isStackable()) {
			s.addInt(item.getAmount());
		}
		packets.add(s.toPacket());
	}
	
	public void sendInventory() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(114);
		s.addByte((byte)player.getInventory().size());
		for(InvItem item : player.getInventory().getItems()) {
			s.addShort(item.getID() + (item.isWielded() ? 32768 : 0));
			if(item.getDef().isStackable()) {
				s.addInt(item.getAmount());
			}
		}
		packets.add(s.toPacket());
	}
	
	/**
	 * Updates the equipment status
	 */
	public void sendEquipmentStats() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(177);
		s.addShort(player.getArmourPoints());
		s.addShort(player.getWeaponAimPoints());
		s.addShort(player.getWeaponPowerPoints());
		s.addShort(player.getMagicPoints());
		s.addShort(player.getPrayerPoints());
		s.addShort(player.getRangePoints());
		packets.add(s.toPacket());
	}
	
	/**
	 * Updates just one stat
	 */
	public void sendStat(int stat) {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(208);
		s.addByte((byte)stat);
		s.addByte((byte)player.getCurStat(stat));
		s.addByte((byte)player.getMaxStat(stat));
		s.addInt(player.getExp(stat));
		packets.add(s.toPacket());
	}
	
	/**
	 * Updates the users stats
	 */
	public void sendStats() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(180);
		for(int lvl : player.getCurStats())
			s.addByte((byte)lvl);
		for(int lvl : player.getMaxStats())
			s.addByte((byte)lvl);
		for(int exp : player.getExps())
			s.addInt(exp);
		packets.add(s.toPacket());
	}
	
	/**
	 * Sent when the user changes coords incase they moved up/down a level
	 */
	public void sendWorldInfo() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(131);
		s.addShort(player.getIndex());
		s.addShort(2304);
		s.addShort(1776);
		s.addShort(Formulae.getHeight(player.getLocation()));
		s.addShort(944);
		packets.add(s.toPacket());
	}
	
	/**
	 * Sends the prayer list of activated/deactivated prayers
	 */
	public void sendPrayers() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(209);
		for(int x = 0;x < 14;x++) { //was 14
			s.addByte((byte)(player.isPrayerActivated(x) ? 1 : 0));
		}
		packets.add(s.toPacket());
	}
	
	/**
	 * Updates game settings, ie sound effects etc
	 */
	public void sendGameSettings() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(152);
		s.addByte((byte)(player.getGameSetting(0) ? 1 : 0));
		s.addByte((byte)(player.getGameSetting(2) ? 1 : 0));
		s.addByte((byte)(player.getGameSetting(3) ? 1 : 0));
		s.addByte((byte)(player.getGameSetting(4) ? 1 : 0));
		s.addByte((byte)(player.getGameSetting(5) ? 1 : 0));
		s.addByte((byte)(player.getGameSetting(6) ? 1 : 0));
		packets.add(s.toPacket());
	}
	
	/**
	 * Updates privacy settings, ie pm block etc
	 */
	public void sendPrivacySettings() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(158);
		s.addByte((byte)(player.getPrivacySetting(0) ? 1 : 0));
		s.addByte((byte)(player.getPrivacySetting(1) ? 1 : 0));
		s.addByte((byte)(player.getPrivacySetting(2) ? 1 : 0));
		s.addByte((byte)(player.getPrivacySetting(3) ? 1 : 0));
		packets.add(s.toPacket());
	}
	
	/**
	 * Confirm logout allowed
	 */
	public RSCPacket sendLogout() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(222);
		RSCPacket packet = s.toPacket();
		packets.add(packet);
		return packet;
	}
	
	/**
	 * Deny logging out
	 */
	public void sendCantLogout() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(136);
		packets.add(s.toPacket());
	}
	
	/**
	 * Displays the login box and last IP and login date
	 */
	public void sendLoginBox() {
		RSCPacketBuilder s = new RSCPacketBuilder();
		s.setID(248);
		s.addShort(player.getDaysSinceLastLogin());
		s.addShort(player.getDaysSubscriptionLeft());
		s.addBytes(player.getLastIP().getBytes());
		packets.add(s.toPacket());
	}
}
