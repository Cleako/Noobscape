package org.rscdaemon.server.packetbuilder.client;

import org.rscdaemon.server.packetbuilder.RSCPacketBuilder;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.RSCPacket;

import java.util.List;

public class NpcUpdatePacketBuilder {
	private Player playerToUpdate;
	
	/**
	 * Sets the player to update
	 */
	public void setPlayer(Player p) {
		playerToUpdate = p;
	}

	public RSCPacket getPacket() {
		List<Npc> npcsNeedingHitsUpdate = playerToUpdate.getNpcsRequiringHitsUpdate();
		List<ChatMessage> npcMessagesNeedingDisplayed = playerToUpdate.getNpcMessagesNeedingDisplayed();
		
		int updateSize = npcMessagesNeedingDisplayed.size() + npcsNeedingHitsUpdate.size();
		if (updateSize > 0) {
			RSCPacketBuilder updates = new RSCPacketBuilder();
			updates.setID(190);
			updates.addShort(updateSize);
			for(ChatMessage cm : npcMessagesNeedingDisplayed) {
				updates.addShort(cm.getSender().getIndex());
				updates.addByte((byte)1);
				updates.addShort(cm.getRecipient().getIndex());
				updates.addByte((byte)cm.getLength());
				updates.addBytes(cm.getMessage());
			}
			for(Npc n : npcsNeedingHitsUpdate) {
				updates.addShort(n.getIndex());
				updates.addByte((byte)2);
				updates.addByte((byte)n.getLastDamage());
				updates.addByte((byte)n.getHits());
				updates.addByte((byte)n.getDef().getHits());
			}
			return updates.toPacket();
		}
		return null;
	}
}
