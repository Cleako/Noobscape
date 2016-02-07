package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.net.RSCPacket;
import org.apache.mina.common.IoSession;

public class ShopHandler implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player)session.getAttachment();
		int pID = ((RSCPacket)p).getID();
		if(player.isBusy()) {
			player.resetShop();
			return;
		}
  		final Shop shop = player.getShop();
  		if(shop == null) {
  			player.setSuspiciousPlayer(true);
  			player.resetShop();
  			return;
  		}
  		int value;
  		InvItem item;
  		switch(pID) {
			case 253: // Close shop
				player.resetShop();
				break;
			case 128: // Buy item
				item = new InvItem(p.readShort(), 1);
				value = p.readInt();
				if(value != ((shop.getBuyModifier() * item.getDef().getBasePrice()) / 100) || shop.countId(item.getID()) < 1) {
					return;
				}
				if(player.getInventory().countId(10) < value) {
					player.getActionSender().sendMessage("You don't have enough money to buy that!");
					return;
				}
				if((Inventory.MAX_SIZE - player.getInventory().size()) + player.getInventory().getFreedSlots(new InvItem(10, value)) < player.getInventory().getRequiredSlots(item)) {
					player.getActionSender().sendMessage("You don't have room for that in your inventory");
					return;
				}
				if(player.getInventory().remove(10, value) > -1) {
					shop.remove(item);
					player.getInventory().add(item);
					player.getActionSender().sendSound("coins");
					player.getActionSender().sendInventory();
					shop.updatePlayers();
				}
				break;
			case 255: // Sell item
				item = new InvItem(p.readShort(), 1);
				value = p.readInt();
				if(value != ((shop.getSellModifier() * item.getDef().getBasePrice()) / 100) || player.getInventory().countId(item.getID()) < 1) {
					return;
				}
				if(!shop.shouldStock(item.getID())) {
					return;
				}
				if(!shop.canHold(item)) {
					player.getActionSender().sendMessage("The shop is currently full!");
					return;
				}
				if(player.getInventory().remove(item) > -1) {
					player.getInventory().add(new InvItem(10, value));
					shop.add(item);
					player.getActionSender().sendSound("coins");
					player.getActionSender().sendInventory();
					shop.updatePlayers();
				}
				break;
			}
      	}
}