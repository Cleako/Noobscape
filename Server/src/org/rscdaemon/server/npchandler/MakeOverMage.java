package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class MakeOverMage implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handleNpc(final Npc npc, Player player) throws Exception {
      		player.informOfNpcMessage(new ChatMessage(npc, "Are you happy with your looks?", player));
      		player.setBusy(true);
		world.getDelayedEventHandler().add(new ShortEvent(player) {
			public void action() {
				owner.informOfNpcMessage(new ChatMessage(npc, "If not i can change them for the cheap cheap price of 3000 coins", owner));
				world.getDelayedEventHandler().add(new ShortEvent(owner) {
					public void action() {
						owner.setBusy(false);
						String[] options = new String[]{"I'm happy with how I look thank you", "Yes change my looks please"};
						owner.setMenuHandler(new MenuHandler(options) {
							public void handleReply(final int option, final String reply) {
								if(owner.isBusy()) {
									return;
								}
								owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
								owner.setBusy(true);
								world.getDelayedEventHandler().add(new ShortEvent(owner) {
									public void action() {
										owner.setBusy(false);
										switch(option) {
											case 1:
												if(owner.getInventory().countId(10) < 3000) {
													owner.informOfChatMessage(new ChatMessage(owner, "I'll just go get the cash", npc));
												}
												else if(owner.getInventory().remove(10, 3000) > -1) {
													owner.setChangingAppearance(true);
													owner.getActionSender().sendAppearanceScreen();
													owner.getActionSender().sendInventory();
												}
												break;
										}
										npc.unblock();
									}
								});
							}
						});
						owner.getActionSender().sendMenu(options);
					}
				});
			}
		});
		npc.blockedBy(player);
	}
	
}