package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.util.*;
import org.rscdaemon.server.entityhandling.EntityHandler;
import org.rscdaemon.server.model.InvItem;
import org.apache.mina.common.IoSession;

import java.util.*;
import java.util.Map.Entry;

public class WieldHandler implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player)session.getAttachment();
		int pID = ((RSCPacket)p).getID();
		if(player.isBusy() && !player.inCombat()) {
			return;
		}
		if(player.isDueling() && player.getDuelSetting(3)) {
			player.getActionSender().sendMessage("Armour is disabled in this duel");
			return;
		}
		player.resetAllExceptDueling();
		int idx = (int)p.readShort();
		if(idx < 0 || idx >= 30) {
			player.setSuspiciousPlayer(true);
			return;
		}
		InvItem item = player.getInventory().get(idx);
		if(item == null || !item.isWieldable()) {
			player.setSuspiciousPlayer(true);
			return;
		}
		switch(pID) {
			case 181:
				if(!item.isWielded()) {
					wieldItem(player, item);
				}
				break;
			case 92:
				if(item.isWielded()) {
					unWieldItem(player, item, true);
				}
				break;
		}
		player.getActionSender().sendInventory();
		player.getActionSender().sendEquipmentStats();
	}
	
	private void wieldItem(Player player, InvItem item) {
		if(!(world.getWildernessType()) && (player.getLocation().wildernessLevel() > 0)) {
		for(int p2p : world.getP2PItems()) {
			if(item.getID() == p2p) {
				player.getActionSender().sendMessage("The wilderness is currently @gre@F2P@whi@. Your @gre@P2P @whi@items can't be used.");
				return;
				}
			}
		}
		String youNeed = "";
		for(Entry<Integer, Integer> e : item.getWieldableDef().getStatsRequired()) {
			if(player.getMaxStat(e.getKey()) < e.getValue()) {
				youNeed += ((Integer)e.getValue()).intValue() + " " + Formulae.statArray[((Integer)e.getKey()).intValue()] + ", ";
			}
		}
		if(!youNeed.equals("")) {
			player.getActionSender().sendMessage("You must have at least " + youNeed.substring(0, youNeed.length() - 2) + " to use this item.");
			return;
		}
		if(EntityHandler.getItemWieldableDef(item.getID()).femaleOnly() && player.isMale()) {
			player.getActionSender().sendMessage("Quit the cross-dressing.");
			return;
		}
		int[] capeIDs = {1215, 1214, 1213};
		int[] staffIDs = {1217, 1218, 1216};
		String[] gods = {"Guthix", "Saradomin", "Zamorak"};
		for(int i = 0; i < 3; i++) {
			if(!DataConversions.inArray(capeIDs, item.getID()) && !DataConversions.inArray(staffIDs, item.getID())) { break; }
			if(player.wielding(staffIDs[i])) {
				if(item.getID() != capeIDs[i] && !DataConversions.inArray(staffIDs, item.getID())) {
				player.getActionSender().sendMessage("You can only wear " + gods[i] + " Cape with this staff!");
				return;
				}
			}
			if(player.wielding(capeIDs[i])) {
				if(item.getID() != staffIDs[i] && !DataConversions.inArray(capeIDs, item.getID())) {
				player.getActionSender().sendMessage("You can only wear the staff of " + gods[i] + " with this cape!");
				return;
				}
			}
		}
      	ArrayList<InvItem> items = player.getInventory().getItems();
      	for(InvItem i : items) {
      		if(item.wieldingAffectsItem(i) && i.isWielded()) {
      			unWieldItem(player, i, false);
      		}
      	}
      	item.setWield(true);
      	player.getActionSender().sendSound("click");
      	player.updateWornItems(item.getWieldableDef().getWieldPos(), item.getWieldableDef().getSprite());
	}
	
	private void unWieldItem(Player player, InvItem item, boolean sound) {
		item.setWield(false);
		if(sound) {
			player.getActionSender().sendSound("click");
		}
		player.updateWornItems(item.getWieldableDef().getWieldPos(), player.getPlayerAppearance().getSprite(item.getWieldableDef().getWieldPos()));
	}
	
}