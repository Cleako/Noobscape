package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.ShortEvent;

public class Priest implements NpcHandler {
	/**
	 * World instance
	 */
	 public static World world = World.getWorld();
	 
	 public void handleNpc(final Npc npc, final Player player) throws Exception {
		if(player.getRestlessGhostStatus() == 1) { // Spoke to the priest already
			player.informOfNpcMessage(new ChatMessage(npc, "The hermit is located in Lumbridge swamp. Go find him immediately.", player));
			return;
		}
		if(player.getRestlessGhostStatus() == 2) { // Spoke to the hermit and recieved the amulet of ghost speak
			player.informOfNpcMessage(new ChatMessage(npc, "Please wield the amulet and find out what is troubling the ghost.", player));
			return;
		}
		if(player.getRestlessGhostStatus() == 3) { // Spoke to the ghost - need to find the ghost's skull
			player.informOfChatMessage(new ChatMessage(player, "I have spoken to the ghost and it seems his skull has been stolen!", npc));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					player.informOfNpcMessage(new ChatMessage(npc, "Please go find his skull so he can rest in peace!", player));
					return;
				}
			});
		}
		if(player.getRestlessGhostStatus() == 4) { // Retrieved the skull and informed the ghost
			player.informOfNpcMessage(new ChatMessage(npc, "Please put the ghost's skull back into the rightful place!", player));
			return;
		}
		if(player.getRestlessGhostStatus() == 0 || player.getRestlessGhostStatus() == 5) {
			player.informOfNpcMessage(new ChatMessage(npc, "Welcome to the church of holy Saradomin", player));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					String[] options = new String[] { "Who's Saradomin?", "Nice place you've got here", "I'm looking for a quest" };
						player.setMenuHandler(new MenuHandler(options) {
							public void handleReply(final int option, final String reply) {
								if(player.isBusy()) {
									return;
								}
								player.informOfChatMessage(new ChatMessage(player, reply, npc));
								player.setBusy(true);
								world.getDelayedEventHandler().add(new ShortEvent(player) {
									public void action() {
										player.setBusy(false);
											if(option == 0) { // Who's Saradomin
												player.informOfNpcMessage(new ChatMessage(npc, "Surely you have heard of the God, Saradomin?", player));
												world.getDelayedEventHandler().add(new ShortEvent(player) {
													public void action() {
														player.informOfNpcMessage(new ChatMessage(npc, "Maybe you could try your hand at that", player));
														world.getDelayedEventHandler().add(new ShortEvent(player) {
															public void action() {
																player.informOfNpcMessage(new ChatMessage(npc, "He who creates the forces of goodness and purity in this world?", player));
																world.getDelayedEventHandler().add(new ShortEvent(player) {
																	public void action() {
																		player.informOfNpcMessage(new ChatMessage(npc, "I cannot believe your ignorance!", player));
																		world.getDelayedEventHandler().add(new ShortEvent(player) {
																			public void action() {
																				player.informOfNpcMessage(new ChatMessage(npc, "This is the God with more follows than any other!", player));
																				world.getDelayedEventHandler().add(new ShortEvent(player) {
																					public void action() {
																						player.informOfNpcMessage(new ChatMessage(npc, "At least in these parts!", player));
																						world.getDelayedEventHandler().add(new ShortEvent(player) {
																							public void action() {
																								player.informOfNpcMessage(new ChatMessage(npc, "He who along with his brothers Guthix and Zamorak created this world", player));
																								return;
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
											if(option == 1) { // Nice place you've got here
												player.informOfNpcMessage(new ChatMessage(npc, "It is, isn't it?", player));
												world.getDelayedEventHandler().add(new ShortEvent(player) {
													public void action() {
														player.informOfNpcMessage(new ChatMessage(npc, "It was built 230 years ago", player));
														return;
													}
												});
											}
											if(option == 2) { // I am looking for a quest
												player.informOfNpcMessage(new ChatMessage(npc, "That's lucky. I need someone to do a quest for me", player));
												world.getDelayedEventHandler().add(new ShortEvent(player) {
													public void action() {
														player.informOfChatMessage(new ChatMessage(player, "Ok i'll help", npc));
														world.getDelayedEventHandler().add(new ShortEvent(player) {
															public void action() {
																player.informOfNpcMessage(new ChatMessage(npc, "Ok the problem is, there is a ghost in the church graveyeard", player));
																world.getDelayedEventHandler().add(new ShortEvent(player) {
																	public void action() {
																		player.informOfNpcMessage(new ChatMessage(npc, "I would like you to get rid of it", player));
																		world.getDelayedEventHandler().add(new ShortEvent(player) {
																			public void action() {
																				player.informOfNpcMessage(new ChatMessage(npc, "If you need any help", player));
																				world.getDelayedEventHandler().add(new ShortEvent(player) {
																					public void action() {
																						player.informOfNpcMessage(new ChatMessage(npc, "My friend father Urhney is an expert on ghosts", player));
																						world.getDelayedEventHandler().add(new ShortEvent(player) {
																							public void action() {
																								player.informOfNpcMessage(new ChatMessage(npc, "I believe he is currently living as a hermit", player));
																								world.getDelayedEventHandler().add(new ShortEvent(player) {
																									public void action() {
																										player.informOfNpcMessage(new ChatMessage(npc, "He has a little shack somewhere in the swamps south of here", player));
																										world.getDelayedEventHandler().add(new ShortEvent(player) {
																											public void action() {
																												player.informOfNpcMessage(new ChatMessage(npc, "I'm sure if you told him that I sent you he'd be willing to help", player));
																												world.getDelayedEventHandler().add(new ShortEvent(player) {
																													public void action() {
																														player.informOfNpcMessage(new ChatMessage(npc, "My name is father Aereck by the way", player));
																														world.getDelayedEventHandler().add(new ShortEvent(player) {
																															public void action() {
																																player.informOfNpcMessage(new ChatMessage(npc, "Be careful going through the swamps", player));
																																world.getDelayedEventHandler().add(new ShortEvent(player) {
																																	public void action() {
																																		player.informOfNpcMessage(new ChatMessage(npc, "I have heard they can be quite dangerous", player));
																																		player.setRestlessGhostStatus(1);
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
																				}
																			});
																		}
																	});
																}
															});
														}
													});
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