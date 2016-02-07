package org.rscdaemon.server.packetbuilder.client;

import org.rscdaemon.server.packetbuilder.RSCPacketBuilder;
import org.rscdaemon.server.model.Item;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.util.StatefulEntityCollection;
import org.rscdaemon.server.util.DataConversions;
import java.util.Collection;

public class ItemPositionPacketBuilder {
	private Player playerToUpdate;
	
	/**
	 * Sets the player to update
	 */
	public void setPlayer(Player p) {
		playerToUpdate = p;
	}

	public RSCPacket getPacket() {
		StatefulEntityCollection<Item> watchedItems = playerToUpdate.getWatchedItems();
		if(watchedItems.changed()) {
			Collection<Item> newItems = watchedItems.getNewEntities();
			Collection<Item> knownItems = watchedItems.getKnownEntities();
			RSCPacketBuilder packet = new RSCPacketBuilder();
			packet.setID(109);
			for(Item i : knownItems) {
				if(watchedItems.isRemoving(i)) {
					byte[] offsets = DataConversions.getObjectPositionOffsets(i.getLocation(), playerToUpdate.getLocation());
//					if(it's miles away) {
//						packet.addByte((byte)255);
//						packet.addByte((byte)sectionX);
//						packet.addByte((byte)sectionY);
//					}
//					else {
						packet.addShort(i.getID() + 32768);
						packet.addByte(offsets[0]);
						packet.addByte(offsets[1]);
//					}
				}
			}
			for(Item i : newItems) {
				byte[] offsets = DataConversions.getObjectPositionOffsets(i.getLocation(), playerToUpdate.getLocation());
				packet.addShort(i.getID());
				packet.addByte(offsets[0]);
				packet.addByte(offsets[1]);
			}
			return packet.toPacket();
		}
		return null;
	}
}

