package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.event.WalkToMobEvent;
import org.rscdaemon.server.event.ShortEvent;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.states.Action;
import org.apache.mina.common.IoSession;

public class InvUseOnNpc implements PacketHandler {
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
		final Npc affectedNpc = world.getNpc(p.readShort());
		final InvItem item = player.getInventory().get(p.readShort());
		if(affectedNpc == null || item == null) { // This shouldn't happen
			return;
		}
		player.setFollowing(affectedNpc);
		player.setStatus(Action.USING_INVITEM_ON_NPC);
		world.getDelayedEventHandler().add(new WalkToMobEvent(player, affectedNpc, 1) {
			public void arrived() {
				owner.resetPath();
				if(!owner.getInventory().contains(item) || owner.isBusy() || owner.isRanging() || !owner.nextTo(affectedNpc) || affectedNpc.isBusy() || owner.getStatus() != Action.USING_INVITEM_ON_NPC) {
					return;
				}
				owner.resetAll();
				switch(affectedNpc.getID()) {
					case 2: // Sheep
						if(!itemId(new int[]{144})) {
							owner.getActionSender().sendMessage("Nothing interesting happens.");
		      					return;
						}
						owner.setBusy(true);
						affectedNpc.blockedBy(owner);
				      		affectedNpc.resetPath();
				      		showBubble();
				      		owner.getActionSender().sendMessage("You attempt to shear the sheep");
				      		world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      					public void action() {
		      						if(DataConversions.random(0, 4) != 0) {
		      							owner.getActionSender().sendMessage("You get some wool");
		      							owner.getInventory().add(new InvItem(145, 1));
		      							owner.getActionSender().sendInventory();
		      						}
		      						else {
		      							owner.getActionSender().sendMessage("The sheep manages to get away from you!");
		      						}
		      						owner.setBusy(false);
				      				affectedNpc.unblock();
		      					}
		      				});
						break;
					case 6: // Cow
						if(item.getID() != 21) {
							owner.getActionSender().sendMessage("Nothing interesting happens.");
		      				return;
						}
						owner.setBusy(true);
						if(owner.getInventory().remove(item) > -1) {
							showBubble();
							owner.getActionSender().sendMessage("You milk the cow.");
							owner.getActionSender().sendMessage("You did get some fresh milk.");
							owner.getInventory().add(new InvItem(22));
							owner.getActionSender().sendInventory();
						}
						owner.setBusy(false);
						break;
					case 160:// Thrander
						int newID;
						switch(item.getID()) {
							case 308: // Bronze top
								newID = 117;
								break;
							case 312: // Iron top
								newID = 8;
								break;
							case 309: // Steel top
								newID = 118;
								break;
							case 313: // Black top
								newID = 196;
								break;
							case 310: // Mithril top
								newID = 119;
								break;
							case 311: // Adamantite top
								newID = 120;
								break;
							case 407: // Rune top
								newID = 401;
								break;
							case 117: // Bronze body
								newID = 308;
								break;
							case 8: // Iron body
								newID = 312;
								break;
							case 118: // Steel body
								newID = 309;
								break;
							case 196: // Black body
								newID = 313;
								break;
							case 119: // Mithril body
								newID = 310;
								break;
							case 120: // Adamantite body
								newID = 311;
								break;
							case 401: // Rune body
								newID = 407;
								break;
							case 214: // Bronze skirt
								newID = 206;
								break;
							case 215: // Iron skirt
								newID = 9;
								break;
							case 225: // Steel skirt
								newID = 121;
								break;
							case 434: // Black skirt
								newID = 248;
								break;
							case 226: // Mithril skirt
								newID = 122;
								break;
							case 227: // Adamantite skirt
								newID = 123;
								break;
							case 406: // Rune skirt
								newID = 402;
								break;
							case 206: // Bronze legs
								newID = 214;
								break;
							case 9: // Iron legs
								newID = 215;
								break;
							case 121: // Steel legs
								newID = 225;
								break;
							case 248: // Black legs
								newID = 434;
								break;
							case 122: // Mithril legs
								newID = 226;
								break;
							case 123: // Adamantite legs
								newID = 227;
								break;
							case 402: // Rune legs
								newID = 406;
								break;
							default:
								owner.getActionSender().sendMessage("Nothing interesting happens.");
								return;
						}
						final InvItem newPlate = new InvItem(newID, 1);
						owner.getActionSender().sendMessage("Thrander hammers the armour");
						world.getDelayedEventHandler().add(new ShortEvent(owner) {
							public void action() {
								if(owner.getInventory().remove(item) > -1) {
									owner.getInventory().add(newPlate);
									owner.getActionSender().sendInventory();
								}
								affectedNpc.unblock();
							}
						});
						affectedNpc.blockedBy(owner);
						break;
					default:
						owner.getActionSender().sendMessage("Nothing interesting happens.");
						break;
				}
			}
			
			private boolean itemId(int[] ids) {
				return DataConversions.inArray(ids, item.getID());
			}
			
			private void showBubble() {
				Bubble bubble = new Bubble(owner, item.getID());
				for(Player p : owner.getViewArea().getPlayersInView()) {
					p.informOfBubble(bubble);
				}
			}
		});
	}
	
}