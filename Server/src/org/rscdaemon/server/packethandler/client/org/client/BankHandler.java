package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.entityhandling.EntityHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.net.RSCPacket;
import org.apache.mina.common.IoSession;

public class BankHandler implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player)session.getAttachment();
		int pID = ((RSCPacket)p).getID();
		if(player.isBusy() || player.isRanging() || player.isTrading() || player.isDueling()) {
			player.resetBank();
			return;
		}
  		if(!player.accessingBank()) {
  			player.setSuspiciousPlayer(true);
  			player.resetBank();
  			return;
  		}
  		Bank bank = player.getBank();
  		Inventory inventory = player.getInventory();
  		InvItem item;
  		int itemID, amount, slot;
		switch(pID) {
			case 48: // Close bank
				player.resetBank();
				break;
			case 198: // Deposit item
				itemID = p.readShort();
				amount = p.readInt();
				if(amount < 1 || inventory.countId(itemID) < amount) {
		      			player.setSuspiciousPlayer(true);
		      			return;
				}
				if(EntityHandler.getItemDef(itemID).isStackable()) {
					item = new InvItem(itemID, amount);
					if(bank.canHold(item) && inventory.remove(item) > -1) {
						bank.add(item);
					}
					else {
						player.getActionSender().sendMessage("You don't have room for that in your bank");
					}
				}
				else {
					for(int i = 0;i < amount;i++) {
						int idx = inventory.getLastIndexById(itemID);
						item = inventory.get(idx);
						if(item == null) { // This shouldn't happen
							break;
						}
						if(bank.canHold(item) && inventory.remove(item) > -1) {
							bank.add(item);
						}
						else {
							player.getActionSender().sendMessage("You don't have room for that in your bank");
							break;
						}
					}
				}
				slot = bank.getFirstIndexById(itemID);
				if(slot > -1) {
					player.getActionSender().sendInventory();
					player.getActionSender().updateBankItem(slot, itemID, bank.countId(itemID));
				}
				break;
			case 183: // Withdraw item
				itemID = p.readShort();
				amount = p.readInt();
				if(amount < 1 || bank.countId(itemID) < amount) {
		      			player.setSuspiciousPlayer(true);
		      			return;
				}
				slot = bank.getFirstIndexById(itemID);
				if(EntityHandler.getItemDef(itemID).isStackable()) {
					item = new InvItem(itemID, amount);
					if(inventory.canHold(item) && bank.remove(item) > -1) {
						inventory.add(item);
					}
					else {
						player.getActionSender().sendMessage("You don't have room for that in your inventory");
					}
				}
				else {
					for(int i = 0;i < amount;i++) {
						if(bank.getFirstIndexById(itemID) < 0) { // This shouldn't happen
							break;
						}
						item = new InvItem(itemID, 1);
						if(inventory.canHold(item) && bank.remove(item) > -1) {
							inventory.add(item);
						}
						else {
							player.getActionSender().sendMessage("You don't have room for that in your inventory");
							break;
						}
					}
				}
				if(slot > -1) {
					player.getActionSender().sendInventory();
					player.getActionSender().updateBankItem(slot, itemID, bank.countId(itemID));
				}
				break;
		}
	}
	
}