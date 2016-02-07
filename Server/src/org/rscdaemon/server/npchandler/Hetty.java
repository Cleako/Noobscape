package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class Hetty implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
			if(player.getWitchPotionStatus() == 3) {
				player.informOfNpcMessage(new ChatMessage(npc, "Thank you again for your help!", player));
				return;
			}
			if(player.getWitchPotionStatus() == 2) {
				player.informOfNpcMessage(new ChatMessage(npc, "Please take a drink from the cauldron!", player));
				return;
			}
			if(player.getWitchPotionStatus() == 1) {
				player.informOfNpcMessage(new ChatMessage(npc, "Have you got the supplies to finish my evil brew?", player));
				player.setBusy(true);
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						player.setBusy(false);
						String[] option = new String[]{"Yes, I have them all here", "I am still trying to find them"};
						player.setMenuHandler(new MenuHandler(option) {
							public void handleReply(final int option, final String reply) {
								if(player.isBusy()) {
									return;
								}
								player.informOfChatMessage(new ChatMessage(player, reply, npc));
								player.setBusy(true);
								world.getDelayedEventHandler().add(new ShortEvent(player) {
									public void action() {
										player.setBusy(false);
											if(option == 1) {
												player.informOfNpcMessage(new ChatMessage(npc, "Ok. Come back when you have found my supplies!", player));
												return;
											}
											if(option == 0 && player.getInventory().countId(134) > 0 && player.getInventory().countId(241) > 0 && player.getInventory().countId(271) > 0 && player.getInventory().countId(270) > 0) {
													player.informOfNpcMessage(new ChatMessage(npc, "Thank you! I can now begin my evil brew!", player));
													player.getInventory().remove(134, 1);
													player.getInventory().remove(241, 1);
													player.getInventory().remove(271, 1);
													player.getInventory().remove(270, 1);
													player.getActionSender().sendInventory();
													player.setWitchPotionStatus(2);
													player.getActionSender().sendMessage("Herry begins placing the supplies into the cauldron.");
											} else
													player.informOfNpcMessage(new ChatMessage(npc, "You don't have my supplies!", player));
									}
								});
							}
						});
						player.getActionSender().sendMenu(option);
					}
				});
			} else if(player.getWitchPotionStatus() == 0) {
      		player.informOfNpcMessage(new ChatMessage(npc, "I need some supplies to finish my witches evil brew. Can you go get them for me?", player));
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				String[] options = new String[]{"Yes, what do you need?", "I'm too busy, sorry"};
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
								if(option == 0) {
									owner.informOfNpcMessage(new ChatMessage(npc, "Thank you. I need the following items:", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
											owner.informOfNpcMessage(new ChatMessage(npc, "1 Burnt Meat, 1 Onion, 1 Rat's Tail and the Eye of a Newt.", owner));
											world.getDelayedEventHandler().add(new ShortEvent(owner) {
												public void action() {
												owner.informOfNpcMessage(new ChatMessage(npc, "I will wait here for you to return!", owner));
												owner.setWitchPotionStatus(1);
												npc.unblock();
												}
											});
										}
									});
								} else {
											owner.setBusy(false);
											npc.unblock();
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
}