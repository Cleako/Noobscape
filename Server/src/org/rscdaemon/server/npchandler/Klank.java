package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class Klank implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handleNpc(final Npc npc, Player player) throws Exception {
      		player.informOfNpcMessage(new ChatMessage(npc, "Would you like me to put your shield together?", player));
      		player.informOfNpcMessage(new ChatMessage(npc, "I can do it for a small fee of 1 million coins.", player));
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				String[] options = new String[]{"Yes, please.", "No, thank you."};
				owner.setMenuHandler(new MenuHandler(options) {
					public void handleReply(final int option, final String reply) {
						if(owner.isBusy()) {
							return;
						}
						owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
						owner.setBusy(true);
						world.getDelayedEventHandler().add(new ShortEvent(owner) {
							public void action() {
								switch(option) {
									case 0: //Yes
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
													if(owner.getInventory().countId(1276) < 1) {
													}
													if(owner.getInventory().countId(1277) < 1) {
													owner.getActionSender().sendMessage("You don't have both halfs of the shield.");
													owner.setBusy(false);
													npc.unblock();
													}
													else if(owner.getInventory().countId(10) < 1000000) {
													owner.getActionSender().sendMessage("You do not have enough cash.");
													running = false;
													owner.setBusy(false);
													npc.unblock();
												}
											else {
												owner.getInventory().remove(1276, 1);
												owner.getInventory().remove(1277, 1);
												owner.getInventory().remove(10, 1000000);
												owner.getInventory().add(new InvItem(1278, 1));
												owner.getActionSender().sendInventory();
												npc.unblock();
												owner.setBusy(false);
											}}
										});
										return;
									case 1: //No
										owner.setBusy(false);
										owner.informOfNpcMessage(new ChatMessage(npc, "Ok. Come back when you change your mind.", owner));
										npc.unblock();
										break;
									default:
										owner.setBusy(false);
										npc.unblock();
										return;
								}
								
							}
						});
					}
				});
				owner.getActionSender().sendMenu(options);
			}
		});
		npc.blockedBy(player);
	}
	
}