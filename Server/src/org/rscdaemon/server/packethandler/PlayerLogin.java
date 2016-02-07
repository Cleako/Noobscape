package org.rscdaemon.server.packethandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.packetbuilder.RSCPacketBuilder;
import org.rscdaemon.server.util.*;
import org.apache.mina.common.IoSession;

public class PlayerLogin implements PacketHandler {
	/**
	 * World instance
	 */
	private static final World world = World.getWorld();
	/**
	 * The player to update
	 */
	private Player player;
	
	public PlayerLogin(Player player) {
		this.player = player;
	}
	
      	public void handlePacket(Packet p, IoSession session) throws Exception {
      		byte loginCode = p.readByte();
      		RSCPacketBuilder pb = new RSCPacketBuilder();
      		pb.setBare(true);
      		pb.addByte(loginCode);
      		player.getSession().write(pb.toPacket());
      		if(loginCode == 0 || loginCode == 1 || loginCode == 99) {
      			
      			player.setOwner(p.readInt());
      			player.setGroupID(p.readInt());
      			
      			player.setSubscriptionExpires(p.readLong());
      			
      			player.setLastIP(DataConversions.IPToString(p.readLong()));
      			player.setLastLogin(p.readLong());
      			
      			player.setLocation(Point.location(p.readShort(), p.readShort()), true);
      			
      			player.setFatigue(p.readShort());
				player.setKills(p.readShort());
				player.setDeaths(p.readShort());
				player.setGuthixSpellCast(p.readShort());
				player.setSaradominSpellCast(p.readShort());
				player.setZamorakSpellCast(p.readShort());
				player.setQuestPoints(p.readShort());
				player.setTaskPoints(p.readShort());
				player.setRemaining(p.readShort());
				player.setCompletedTasks(p.readShort());
      			player.setCombatStyle((int)p.readByte());
				player.setMoneyTask(p.readString());      			

      			player.setPrivacySetting(0, p.readByte() == 1);
      			player.setPrivacySetting(1, p.readByte() == 1);
      			player.setPrivacySetting(2, p.readByte() == 1);
      			player.setPrivacySetting(3, p.readByte() == 1);
      			
      			player.setGameSetting(0, p.readByte() == 1);
      			player.setGameSetting(2, p.readByte() == 1);
      			player.setGameSetting(3, p.readByte() == 1);
      			player.setGameSetting(4, p.readByte() == 1);
      			player.setGameSetting(5, p.readByte() == 1);
      			player.setGameSetting(6, p.readByte() == 1);
      			
      			PlayerAppearance appearance = new PlayerAppearance(p.readShort(), p.readShort(), p.readShort(), p.readShort(), p.readShort(), p.readShort());
      			if(!appearance.isValid()) {
      				loginCode = 7;
      				player.destroy(true);
      				player.getSession().close();
      			}
      			player.setAppearance(appearance);
      			player.setWornItems(player.getPlayerAppearance().getSprites());
      			
      			player.setMale(p.readByte() == 1);
      			long skull = p.readLong();
      			if(skull > 0) {
      				player.addSkull(skull);
      			}
      			
      			for(int i = 0;i < 18;i++) {
      				int exp = (int)p.readLong();
      				player.setExp(i, exp);
      				player.setMaxStat(i, Formulae.experienceToLevel(exp));
      				player.setCurStat(i, p.readShort());
      			}
      			player.setCombatLevel(Formulae.getCombatlevel(player.getMaxStats()));
      			
      			Inventory inventory = new Inventory(player);
      			int invCount = p.readShort();
      			for(int i = 0;i < invCount;i++) {
      				InvItem item = new InvItem(p.readShort(), p.readInt());
      				if(p.readByte() == 1 && item.isWieldable()) {
      					item.setWield(true);
      					player.updateWornItems(item.getWieldableDef().getWieldPos(), item.getWieldableDef().getSprite());
      				}
      				inventory.add(item);
      			}
      			player.setInventory(inventory);
      			
      			Bank bank = new Bank();
      			int bnkCount = p.readShort();
      			for(int i = 0;i < bnkCount;i++) {
      				bank.add(new InvItem(p.readShort(), p.readInt()));
      			}
      			player.setBank(bank);
      			
      			int friendCount = p.readShort();
      			for(int i = 0;i < friendCount;i++) {
      				player.addFriend(org.rscdaemon.server.util.DataConversions.hashToUsername(p.readLong()));
      			}
      			
      			int ignoreCount = p.readShort();
      			for(int i = 0;i < ignoreCount;i++) {
      				player.addIgnore(org.rscdaemon.server.util.DataConversions.hashToUsername(p.readLong()));
      			}
      			
      			/* End of loading methods */
      			
      			world.registerPlayer(player);
      			
      			player.updateViewedPlayers();
      			player.updateViewedObjects();
      			
      			org.rscdaemon.server.packetbuilder.client.MiscPacketBuilder sender = player.getActionSender();
      			sender.sendServerInfo();
      			sender.sendFatigue();
				sender.sendKills();
				sender.sendDeaths();
				sender.sendQuestPoints();
				sender.sendGuthixSpellCast();
				sender.sendSaradominSpellCast();
				sender.sendZamorakSpellCast();
				sender.sendKillingSpree();
				sender.sendImpCatcherComplete();
				sender.sendRomeoJulietComplete();
				sender.sendSheepShearerComplete();
				sender.sendWitchPotionComplete();
				sender.sendDoricsQuestComplete();
				sender.sendDruidicRitualComplete();
				sender.sendCooksAssistantComplete();
				sender.sendDemonSlayerComplete();
				sender.sendRestlessGhostComplete();
				sender.sendPiratesTreasureComplete();
				sender.sendErnestTheChickenComplete();
				sender.sendRemaining();
				sender.sendMoneyTask();
				sender.sendTaskPoints();
				sender.sendCompletedTasks();
				sender.sendTaskStatus();
				sender.sendExperienceReward();
				sender.sendItemReward();
				sender.sendCashReward();
      			sender.sendWorldInfo(); // sends info
      			sender.sendInventory();
      			sender.sendEquipmentStats();
      			sender.sendStats();
      			sender.sendPrivacySettings();
      			sender.sendGameSettings();
      			sender.sendFriendList();
      			sender.sendIgnoreList();
      			sender.sendCombatStyle();
      			int timeTillShutdown = world.getServer().timeTillShutdown();
      			if(timeTillShutdown > -1) {
      				sender.startShutdown((int)(timeTillShutdown / 1000));
      			}
				int timeTillWildSwitch = world.getServer().timeTillWildSwitch();
				if(timeTillWildSwitch > -1) {
					sender.startWildernessSwitch((int)(timeTillWildSwitch / 1000));
				}
				int timeTillDropParty = world.getServer().timeTillDropParty();
				if(timeTillDropParty > -1) {
					sender.startDropParty((int)(timeTillDropParty / 1000));
				}
      			if(player.getLastLogin() == 0L) {
      				player.setChangingAppearance(true);
      				sender.sendAppearanceScreen();
      			}
      			sender.sendLoginBox();
      			
      			
      			player.setLoggedIn(true);
      			player.setBusy(false);
      		}
      		else {
      			player.destroy(true);
      		}
      	}
}