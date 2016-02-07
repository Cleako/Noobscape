package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.ShortEvent;

public class Gypsy implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
			if(player.getDemonSlayerStatus() == 6) { // Quest Completed
				player.informOfNpcMessage(new ChatMessage(npc, "Thank you young one. You have saved our town from the evil Delrith!", player));
				return;
			}
			if(player.getDemonSlayerStatus() == 1) { // Not spoke with Sir Prysin
				player.informOfNpcMessage(new ChatMessage(npc, "Please go and speak with Sir Prysin. He'll be able to help!", player));
				return;
			}
			if(player.getDemonSlayerStatus() == 2) { // Spoke with Sir Prysin
				player.informOfNpcMessage(new ChatMessage(npc, "Please go find all the keys needed to unlock the silverlight!", player));
				return;
			}
			if(player.getDemonSlayerStatus() == 3 || player.getDemonSlayerStatus() == 4 || player.getDemonSlayerStatus() == 5) {
				player.informOfNpcMessage(new ChatMessage(npc, "We are all counting on you! Hurry before Delrith causes too much damage!", player));
				return;
			}
			if(player.getDemonSlayerStatus() == 0) { // Speaking to Gypsy for the first time
				player.informOfNpcMessage(new ChatMessage(npc, "Hello, young one. cross my palm with silver and your future will be revealed", player));
				player.setBusy(true);
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						player.setBusy(false);
							String[] option = new String[] { "Ok. Here you go", "Who are you calling young?", "No. I don't believe in that stuff" };
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
												if(option == 0 && player.getInventory().countId(10) < 1) { // The player doesn't have enough cash to give to the gypsy
													player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough gold. Come back when you have some!", player));
													return;
												}
												if(option == 1) { // Who are you calling young
													player.informOfNpcMessage(new ChatMessage(npc, "Well you certainly look a lot young than me " + player.getUsername(), player));
													return;
												}
												if(option == 2) { // I don't believe in that stuff
													player.informOfNpcMessage(new ChatMessage(npc, "That's your choice to believe or not to believe", player));
													world.getDelayedEventHandler().add(new ShortEvent(player) {
														public void action() {
															player.informOfNpcMessage(new ChatMessage(npc, "But if something bad happens, don't say I didn't warn you!", player));
															return;
														}
													});
												}
												if(option == 0 && player.getInventory().countId(10) >= 1) { // The player has enough cash and gives it to the gypsy for the future reading
													player.informOfNpcMessage(new ChatMessage(npc, "As I peer into the swirling mist of the crystal ball", player));
													world.getDelayedEventHandler().add(new ShortEvent(player) {
														public void action() {
															player.informOfNpcMessage(new ChatMessage(npc, "I can see images forming. I can see you", player));
																world.getDelayedEventHandler().add(new ShortEvent(player) {
																	public void action() {
																		player.informOfNpcMessage(new ChatMessage(npc, "You are holding a very impressive looking sword. I do recognize that sword", player));
																			world.getDelayedEventHandler().add(new ShortEvent(player) {
																				public void action() {
																					player.informOfNpcMessage(new ChatMessage(npc, "I can see a big dark shadow appearing now", player));
																						world.getDelayedEventHandler().add(new ShortEvent(player) {
																							public void action() {
																								player.informOfNpcMessage(new ChatMessage(npc, "Aaargh!", player));
																									world.getDelayedEventHandler().add(new ShortEvent(player) {
																										public void action() {
																											player.informOfChatMessage(new ChatMessage(player, "Are you alright?", npc));
																												world.getDelayedEventHandler().add(new ShortEvent(player) {
																													public void action() {
																														player.informOfNpcMessage(new ChatMessage(npc, "It's Delrith! Delrith is coming!", player));
																															world.getDelayedEventHandler().add(new ShortEvent(player) {
																																public void action() {
																																	player.informOfNpcMessage(new ChatMessage(npc, "Go find Sir Prysin and ask him how to stop Delrith!", player));
																																	player.getInventory().remove(10, 1);
																																	player.getActionSender().sendInventory();
																																	player.setDemonSlayerStatus(1);
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