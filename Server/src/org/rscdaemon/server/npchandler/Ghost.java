package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.ShortEvent;

public class Ghost implements NpcHandler {
	/**
	 * World instance
	 */
	 public static final World world = World.getWorld();
	 
	 public void handleNpc(final Npc npc, final Player player) throws Exception {
		if(player.getRestlessGhostStatus() < 2 || !player.wielding(24)) {
			player.getActionSender().sendMessage("The ghost looks angry. Perhaps you should go speak with the priest.");
			return;
		}
		if(player.getRestlessGhostStatus() == 3 && player.getInventory().countId(27) < 1) {
			player.informOfNpcMessage(new ChatMessage(npc, "Go find my skull before it's too late!", player));
			return;
		}
		if(player.getInventory().countId(27) >= 1 && player.getRestlessGhostStatus() == 3) {
			player.informOfNpcMessage(new ChatMessage(npc, "Please place my skull back into the coffin!", player));
			player.setRestlessGhostStatus(4);
			return;
		}
		if(player.getRestlessGhostStatus() >= 4) {
			player.informOfNpcMessage(new ChatMessage(npc, "Thank you! I can now rest in peace!", player));
			return;
		}
		if(player.getRestlessGhostStatus() == 2 && player.wielding(24)) {
			player.informOfChatMessage(new ChatMessage(player, "Hello ghost, how are you?", npc));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					player.informOfNpcMessage(new ChatMessage(npc, "Not very good actually", player));
					world.getDelayedEventHandler().add(new ShortEvent(player) {
						public void action() {
							player.informOfChatMessage(new ChatMessage(player, "What's the problem?", npc));
							world.getDelayedEventHandler().add(new ShortEvent(player) {
								public void action() {
									player.informOfNpcMessage(new ChatMessage(npc, "Did you just understand what I said?", player));
									player.setBusy(true);
									world.getDelayedEventHandler().add(new ShortEvent(player) {
										public void action() {
											player.setBusy(false);
											String[] option = new String[] { "Yes, now tell me what the problem is", "No, you sound like you are speaking nonsense to me", "Wow, this amulet works" };
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
																if(option == 0) { // Yes, I understood
																	player.informOfNpcMessage(new ChatMessage(npc, "Someone has stolen the skull out of my coffin", player));
																	world.getDelayedEventHandler().add(new ShortEvent(player) {
																		public void action() {
																			player.informOfNpcMessage(new ChatMessage(npc, "And until it's returned, I can not rest in peace!", player));
																			world.getDelayedEventHandler().add(new ShortEvent(player) {
																				public void action() {
																					player.informOfChatMessage(new ChatMessage(player, "Don't worry, I will get your skull back safetly!", npc));
																					player.setRestlessGhostStatus(3);
																					return;
																				}
																			});
																		}
																	});
																}
																if(option == 1) { // No, I don't understand
																	player.informOfNpcMessage(new ChatMessage(npc, "It looks as though you understand me perfectly fine!", player));
																	return;
																}
																if(option == 2) { // This amulet works
																	player.informOfNpcMessage(new ChatMessage(npc, "Oh it's your amulet that's doing it. I did wonder", player));
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
								});
							}
						});
					}
				});
			}
		}
	}