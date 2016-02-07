package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.event.WalkToPointEvent;
import org.rscdaemon.server.event.*;
import org.rscdaemon.server.states.Action;
import org.apache.mina.common.IoSession;

public class PickupItem implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player) session.getAttachment();
		if(player.isBusy()) {
			player.resetPath();
			return;
		}
		player.resetAll();
		Point location = Point.location(p.readShort(), p.readShort());
		int id = p.readShort();
		final ActiveTile tile = world.getTile(location);
		final Item item = getItem(id, tile, player);
		if(item == null) {
			player.setSuspiciousPlayer(true);
			player.resetPath();
			return;
		}
		player.setStatus(Action.TAKING_GITEM);
		world.getDelayedEventHandler().add(new WalkToPointEvent(player, location, 1, false) {
			public void arrived() {
				if(owner.isBusy() || owner.isRanging() || !tile.hasItem(item) || !owner.nextTo(item) || owner.getStatus() != Action.TAKING_GITEM) {
					return;
				}
				if(item.getID() == 501 && item.getX() == 333 && item.getY() == 434) {
					Npc affectedNpc = world.getNpc(140, 328, 333, 433, 438);
						Npc affectedNpc2 = world.getNpc(140, 328, 333, 433, 438);
							Npc affectedNpc3 = world.getNpc(140, 328, 333, 433, 438);
								Npc affectedNpc4 = world.getNpc(140, 328, 333, 433, 438);
									Npc affectedNpc5 = world.getNpc(140, 328, 333, 433, 438);
										Npc affectedNpc6 = world.getNpc(140, 328, 333, 433, 438);
											Npc affectedNpc7 = world.getNpc(140, 328, 333, 433, 438);
								if(affectedNpc == null) {
									affectedNpc = affectedNpc2;
									affectedNpc = affectedNpc3;
									affectedNpc = affectedNpc4;
									affectedNpc = affectedNpc5;
									affectedNpc = affectedNpc6;
									affectedNpc = affectedNpc7;
								} System.out.println(owner.getLocation().getX() +" "+ owner.getLocation().getY());
								if(affectedNpc != null && item.getX() == 333 && item.getY() == 434) {
									owner.getActionSender().sendMessage("STOP!");
									affectedNpc.resetPath();
									owner.resetPath();
									owner.resetAll();
									owner.setStatus(Action.FIGHTING_MOB);
									owner.getActionSender().sendMessage("You are under attack!");

									affectedNpc.setLocation(owner.getLocation(), true);
									for(Player p : affectedNpc.getViewArea().getPlayersInView()) {
										p.removeWatchedNpc(affectedNpc);
									}

									owner.setBusy(true);
									owner.setSprite(9);
									owner.setOpponent(affectedNpc);
									owner.setCombatTimer();

									affectedNpc.setBusy(true);
									affectedNpc.setSprite(8);
									affectedNpc.setOpponent(owner);
									affectedNpc.setCombatTimer();
									FightEvent fighting = new FightEvent(owner, affectedNpc, true);
									fighting.setLastRun(0);
									world.getDelayedEventHandler().add(fighting);
					return;
					}
				}
				if(item.getID() == 23) {
					owner.getActionSender().sendMessage("You need a pot to hold this flour in!");
					return;
				}
				if(item.getID() == 1285 && item.getX() == 89 && item.getY() == 516) {
				
						Npc tseller = world.getNpc(780, 89, 90, 517, 520);
						if(tseller != null) {
							owner.setBusy(true);
							owner.informOfNpcMessage(new ChatMessage(tseller, "Hey ! Get your hands off that tea !", owner));
										owner.informOfNpcMessage(new ChatMessage(tseller, "That's for display purposes only", owner));
													owner.informOfNpcMessage(new ChatMessage(tseller, "Im not running a charity here !", owner));
																owner.setBusy(false);
																return;	
						}
				}
				owner.resetAll();
				InvItem invItem = new InvItem(item.getID(), item.getAmount());
				if(!owner.getInventory().canHold(invItem)) {
					owner.getActionSender().sendMessage("You cannot pickup this item, your inventory is full!");
					return;
				}
				world.unregisterItem(item);
				owner.getActionSender().sendSound("takeobject");
				owner.getInventory().add(invItem);
				owner.getActionSender().sendInventory();
			}
		});
	}
	
	private Item getItem(int id, ActiveTile tile, Player player) {
		for(Item i : tile.getItems()) {
			if(i.getID() == id && i.visibleTo(player)) {
				return i;
			}
		}
		return null;
	}

}
