package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.event.WalkToMobEvent;
import org.rscdaemon.server.event.ShortEvent;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.states.Action;
import org.apache.mina.common.IoSession;

import java.util.ArrayList;

public class InvUseOnPlayer implements PacketHandler {
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
		final Player affectedPlayer = world.getPlayer(p.readShort());
		final InvItem item = player.getInventory().get(p.readShort());
		if(affectedPlayer == null || item == null) { // This shouldn't happen
			return;
		}
		player.setFollowing(affectedPlayer);
		player.setStatus(Action.USING_INVITEM_ON_PLAYER);
		world.getDelayedEventHandler().add(new WalkToMobEvent(player, affectedPlayer, 1) {
			public void arrived() {
				owner.resetPath();
				if(!owner.getInventory().contains(item) || !owner.nextTo(affectedPlayer) || owner.isBusy() || owner.isRanging() || owner.getStatus() != Action.USING_INVITEM_ON_PLAYER) {
					return;
				}
				owner.resetAll();
		      		switch(item.getID()) {
		      			case 575: // Christmas cracker
				      		owner.setBusy(true);
				      		affectedPlayer.setBusy(true);
				      		owner.resetPath();
				      		affectedPlayer.resetPath();
		      				Bubble crackerBubble = new Bubble(owner, 575);
				      		for(Player p : owner.getViewArea().getPlayersInView()) {
				      			p.informOfBubble(crackerBubble);
				      		}
		      				owner.getActionSender().sendMessage("You pull the cracker with " + affectedPlayer.getUsername() + "...");
		      				affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " is pulling a cracker with you...");
		      				world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      					public void action() {
				      				InvItem phat = new InvItem(DataConversions.random(576, 581));
				      				if(DataConversions.random(0, 1) == 1) {
				      					owner.getActionSender().sendMessage("Out comes a " + phat.getDef().getName() + "!");
				      					affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " got the contents!");
				      					owner.getInventory().add(phat);
				      				}
				      				else {
				      					owner.getActionSender().sendMessage(affectedPlayer.getUsername() + " got the contents!");
				      					affectedPlayer.getActionSender().sendMessage("Out comes a " + phat.getDef().getName() + "!");
				      					affectedPlayer.getInventory().add(phat);
				      				}
				      				owner.getInventory().remove(item);
				      				owner.setBusy(false);
				      				affectedPlayer.setBusy(false);
						      		owner.getActionSender().sendInventory();
						      		affectedPlayer.getActionSender().sendInventory();
		      					}
		      				});
		      				break;
							case 981: //Gnome Ball Passing
								for(InvItem item : affectedPlayer.getInventory().getItems()) {
									if((item.isWielded()) && (item.getWieldableDef().getWieldPos() == 4)) {
										owner.getActionSender().sendMessage(affectedPlayer.getUsername() + "'s hands are full!");
										return;
									}
								}
								for(InvItem item : owner.getInventory().getItems()) {
									if((item.isWielded()) && (item.getWieldableDef().getWieldPos() == 4)) {
										owner.getActionSender().sendMessage("You must free your right hand to pass the gnome ball!");
										return;
									}
								}
								if((affectedPlayer.isBusy()) || (affectedPlayer.isRanging()) || (affectedPlayer.inCombat())) {
									owner.getActionSender().sendMessage(affectedPlayer.getUsername() + " is busy at the moment!");
									return;
								}
								if(affectedPlayer.getInventory().full()) {
									owner.getActionSender().sendMessage(affectedPlayer.getUsername() + "'s inventory is currently full!");
									return;
								}
								Projectile projectile = new Projectile(owner, affectedMob, 3);
								ArrayList<Player> playersToInform = new ArrayList<Player>();
								playersToInform.addAll(owner.getViewArea().getPlayersInView());
								playersToInform.addAll(affectedMob.getViewArea().getPlayersInView());
								
								for(Player p : playersToInform) {
									p.informOfProjectile(projectile);
								}
								owner.setBusy(true);
								owner.getActionSender().sendMessage("You pass the gnome ball to " + affectedPlayer.getUsername() + "!");
								owner.setBusy(false);
								owner.getInventory().remove(item);
								affectedPlayer.getInventory().add(item);
								affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " has passed you a gnome ball!");
								affectedPlayer.getActionSender().sendInventory();
								owner.getActionSender().sendInventory();
							break;
		      			default:
		      				owner.getActionSender().sendMessage("Nothing interesting happens.");
		      				break;
		      		}
			}
		});
	}
	
}