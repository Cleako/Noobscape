package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.ShortEvent;

public class WizardTraiborn implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
			if(player.getDemonSlayerStatus() == 0) {
				player.informOfNpcMessage(new ChatMessage(npc, "Go speak with the gypsy. She looks like she's in trouble!", player));
				return;
			}
			if(player.getDemonSlayerStatus() == 6) { // Quest Completed
				player.informOfNpcMessage(new ChatMessage(npc, "I was informed of your bravery. Well done brave adventurer!", player));
				return;
			}
			if(player.getDemonSlayerStatus() == 1) {
				player.informOfNpcMessage(new ChatMessage(npc, "Sir Prysin is waiting for you. Please go find and speak with him", player));
				return;
			}
			if(player.getDemonSlayerStatus() == 2) {
				player.informOfNpcMessage(new ChatMessage(npc, "Go collect Sir Prysin's key from Captain Rovin", player));
				return;
			}
			if(player.getDemonSlayerStatus() == 4) { // Got the key from traiborn
				player.informOfNpcMessage(new ChatMessage(npc, "Find the rest of the keys and go find sir prysin", player));
				return;
			}
			if(player.getDemonSlayerStatus() == 5) {
				player.informOfNpcMessage(new ChatMessage(npc, "Go find and slay the Delirth!", player));
				return;
			}
			if(player.getInventory().countId(20) >= 25 && player.getDemonSlayerStatus() == 3) {
				player.informOfNpcMessage(new ChatMessage(npc, "Thank you! I can now perform the ritual!", player));
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						player.getActionSender().sendMessage("Traiborn begins chanting the ritual.");
						player.informOfNpcMessage(new ChatMessage(npc, "Here you go. Good luck!", player));
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().remove(20, 1);
						player.getInventory().add(new InvItem(25, 1));
						player.getActionSender().sendInventory();
						player.setDemonSlayerStatus(4);
					}
				});
			} else if(player.getDemonSlayerStatus() == 3) {
				player.informOfNpcMessage(new ChatMessage(npc, "Hello there, how can I help you?", player));
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						String[] option = new String[] { "I am looking for the silverlight key" };
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
												if(option == 0) {
													player.informOfNpcMessage(new ChatMessage(npc, "The key I have is locked away safely in the wardrobe", player));
													world.getDelayedEventHandler().add(new ShortEvent(player) {
														public void action() {
															player.informOfNpcMessage(new ChatMessage(npc, "which a simple drazier style ritual should suffice", player));
																world.getDelayedEventHandler().add(new ShortEvent(player) {
																	public void action() {
																		player.informOfNpcMessage(new ChatMessage(npc, "Hmm, the main problem with that is i'll need 25 sets of bones", player));
																			world.getDelayedEventHandler().add(new ShortEvent(player) {
																				public void action() {
																					player.informOfNpcMessage(new ChatMessage(npc, "in order to perform the ritual to unlock the wardrobe", player));
																						world.getDelayedEventHandler().add(new ShortEvent(player) {
																							public void action() {
																								player.informOfNpcMessage(new ChatMessage(npc, "But I don't have the 25 sets of bones!", player));
																									world.getDelayedEventHandler().add(new ShortEvent(player) {
																										public void action() {
																											player.informOfChatMessage(new ChatMessage(player, "I will go and collect the 25 sets of bones", npc));
																												world.getDelayedEventHandler().add(new ShortEvent(player) {
																													public void action() {
																														player.informOfNpcMessage(new ChatMessage(npc, "Great news! If you bring me the 25 sets of bones,", player));
																															world.getDelayedEventHandler().add(new ShortEvent(player) {
																																public void action() {
																																	player.informOfNpcMessage(new ChatMessage(npc, "I will be able to give you the key that you are looking for!", player));
																																	npc.unblock();
																																}
																															});
																													}
																											});
																									}
																							});
																					}
																			});
																	}
															});
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
				owner.getActionSender().sendMenu(option);
      			}
      		});
      		npc.blockedBy(player);
		}
	}
}