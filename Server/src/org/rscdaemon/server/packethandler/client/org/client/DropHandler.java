package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.event.DelayedEvent;
import org.rscdaemon.server.states.Action;
import org.rscdaemon.server.util.Formulae;
import org.apache.mina.common.IoSession;

public class DropHandler implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player)session.getAttachment();
		if(player.isBusy()) {
			player.resetPath();
			return;
		}
		player.resetAll();
		final int idx = (int)p.readShort();
		if(idx < 0 || idx >= player.getInventory().size()) {
			player.setSuspiciousPlayer(true);
			return;
		}
		final InvItem item = player.getInventory().get(idx);
		if(item == null) {
			player.setSuspiciousPlayer(true);
			return;
		}
		for(int untradeableItem : Formulae.untradeableItems) {
			if(item.getID() == untradeableItem) {
				player.getActionSender().sendMessage("You can't drop an untradeable item!");
				return;
			}
		}
		player.setStatus(Action.DROPPING_GITEM);
		world.getDelayedEventHandler().add(new DelayedEvent(player, 500) {
			public void run() {
				if(owner.isBusy() || !owner.getInventory().contains(item) || owner.getStatus() != Action.DROPPING_GITEM) {
					running = false;
					return;
				}
				if(owner.hasMoved()) {
					return;
				}
				owner.getActionSender().sendSound("dropobject");
				owner.getInventory().remove(item);
				owner.getActionSender().sendInventory();
				world.registerItem(new Item(item.getID(), owner.getX(), owner.getY(), item.getAmount(), owner));
				running = false;
			}
		});
	}
}