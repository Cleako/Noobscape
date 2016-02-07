package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.ShortEvent;

public class CaptainRovin implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
		if(player.getDemonSlayerStatus() == 6) { // Quest Complete
			player.informOfNpcMessage(new ChatMessage(npc, "Well done brave adventurer. You surely showed Delrith who is boss around here!", player));
			return;
		}
		if(player.getDemonSlayerStatus() == 5) { // Got the silverlight
			player.informOfNpcMessage(new ChatMessage(npc, "Please go slay Delrith with the silverlight!", player));
			return;
		}
		if(player.getDemonSlayerStatus() == 4) {
			player.informOfNpcMessage(new ChatMessage(npc, "Perhaps you should get all the keys to Sir Prysin as soon as possible!", player));
			return;
		}
		if(player.getDemonSlayerStatus() == 0) {
			player.informOfNpcMessage(new ChatMessage(npc, "Go speak with the gypsy. She looks like she's in trouble!", player));
			return;
		}
		if(player.getDemonSlayerStatus() == 1) {
			player.informOfNpcMessage(new ChatMessage(npc, "I heard Delrith is around town. Go find and inform Sir Prysin!", player));
			return;
		}
		if(player.getDemonSlayerStatus() == 3) {
			player.informOfNpcMessage(new ChatMessage(npc, "Take the keys back to Sir Prysin to recieve the silverlight sword!", player));
			return;
		}
		if(player.getDemonSlayerStatus() == 2) {
			player.informOfNpcMessage(new ChatMessage(npc, "How can I help you?", player));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					String[] option = new String[] { "What are you doing up here?", "I am looking for the silverlight key", "You can't help me" };
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
											if(option == 2) { // Nothing
												player.informOfNpcMessage(new ChatMessage(npc, "Then go away! I need to keep the guards in order!", player));
												return;
											}
											if(option == 0) { // What are you doing up here
												player.informOfNpcMessage(new ChatMessage(npc, "I am training these guards and keeping them in top shape!", player));
												return;
											}
											if(option == 1) { // Looking for the key
												player.informOfNpcMessage(new ChatMessage(npc, "Ah, yes. Sir Prysin informed of you would be coming to see me", player));
												world.getDelayedEventHandler().add(new ShortEvent(player) {
													public void action() {
														player.informOfNpcMessage(new ChatMessage(npc, "The Delrith is no push over, I hope you know what you are getting into", player));
														world.getDelayedEventHandler().add(new ShortEvent(player) {
															public void action() {
																player.informOfChatMessage(new ChatMessage(player, "Let me worry about defeating the Delrith. You stick to coaching the guards!", npc));
																world.getDelayedEventHandler().add(new ShortEvent(player) {
																	public void action() {
																		player.informOfNpcMessage(new ChatMessage(npc, "Ok soldier! Here's the key!", player));
																			player.getInventory().add(new InvItem(26, 1));
																				player.getActionSender().sendInventory();
																					player.setDemonSlayerStatus(3);
																					npc.unblock();
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
										owner.getActionSender().sendMenu(option);
									}
								});
								npc.blockedBy(player);
							}
						}
					}