package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.PlayerAppearance;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.Inventory;
import org.rscdaemon.server.net.Packet;

import org.apache.mina.common.IoSession;

public class PlayerAppearanceUpdater implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player) session.getAttachment();
		if(!player.isChangingAppearance()) {
			player.setSuspiciousPlayer(true);
			return;
		}
		player.setChangingAppearance(false);
		
		byte headGender = p.readByte();
		byte headType = p.readByte();
		byte bodyGender = p.readByte();
		
		p.readByte(); // wtf is this?
		
		int hairColour = (int)p.readByte();
		int topColour = (int)p.readByte();
		int trouserColour = (int)p.readByte();
		int skinColour = (int)p.readByte();
		
		int headSprite = headType + 1;
		int bodySprite = bodyGender + 1;
		
		PlayerAppearance appearance = new PlayerAppearance(hairColour, topColour, trouserColour, skinColour, headSprite, bodySprite);
		if(!appearance.isValid()) {
			player.setSuspiciousPlayer(true);
			return;
		}
		
      		player.setMale(headGender == 1);
      		
      		if(player.isMale()) {
      	      		Inventory inv = player.getInventory();
      	      		for(int slot = 0;slot < inv.size();slot++) {
      	      			InvItem i = inv.get(slot);
      	      			if(i.isWieldable() && i.getWieldableDef().getWieldPos() == 1 && i.isWielded() && i.getWieldableDef().femaleOnly()) {
      					i.setWield(false);
      					player.updateWornItems(i.getWieldableDef().getWieldPos(), player.getPlayerAppearance().getSprite(i.getWieldableDef().getWieldPos()));
      					player.getActionSender().sendUpdateItem(slot);
      					break;
      	      			}
      	      		}
      		}
      		
      		int[] oldWorn = player.getWornItems();
      		int[] oldAppearance = player.getPlayerAppearance().getSprites();
      		player.setAppearance(appearance);
      		int[] newAppearance = player.getPlayerAppearance().getSprites();
      		for(int i = 0;i < 12;i++) {
      			if(oldWorn[i] == oldAppearance[i]) {
      				player.updateWornItems(i, newAppearance[i]);
      			}
      		}
	}

}
