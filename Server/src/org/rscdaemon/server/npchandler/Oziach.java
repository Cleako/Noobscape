package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class Oziach implements NpcHandler {
	/**
	 * World Instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
		player.informOfNpcMessage(new ChatMessage(npc, "Aye. Tis a fair day my friend", player));
		player.setBusy(true);
		world.getDelayedEventHandler().add(new ShortEvent(player) {
			public void action() {
				player.setBusy(false);
				String[] options = new String[]{"Can you sell me a rune platebody?", "I'm not your friend", "Yes, it's a very nice day"};
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
									if(option == 1) { // I'm not your friend
										player.informOfNpcMessage(new ChatMessage(npc, "I'm not your friend either!", player));
										return;
									}
									if(option == 2) { // It's a nice day
										player.informOfNpcMessage(new ChatMessage(npc, "Yes, it is isn't it", player));
										return;
									}
									if(option == 0) { // Rune platebody
										player.informOfNpcMessage(new ChatMessage(npc, "So, how does thee know I 'ave some?", player));
										world.getDelayedEventHandler().add(new ShortEvent(player) {
											public void action() {
												player.informOfChatMessage(new ChatMessage(player, "The guildmaster of the Champion's Guild told me", npc));
												world.getDelayedEventHandler().add(new ShortEvent(player) {
													public void action() {
														player.informOfNpcMessage(new ChatMessage(npc, "Yes, I suppose he would, wouldn't he?", player));
														world.getDelayedEventHandler().add(new ShortEvent(player) {
															public void action() {
																player.informOfNpcMessage(new ChatMessage(npc, "He's always sending people up to bother me!", player));
																world.getDelayedEventHandler().add(new ShortEvent(player) {
																	public void action() {
																		player.informOfNpcMessage(new ChatMessage(npc, "Well, i'm not going to just let anyone wear my rune platemail!", player));
																		world.getDelayedEventHandler().add(new ShortEvent(player) {
																			public void action() {
																				player.informOfNpcMessage(new ChatMessage(npc, "Get out of my face and go kill the dragon to prove yourself!", player));
																				world.getDelayedEventHandler().add(new ShortEvent(player) {
																					public void action() {
																						player.informOfChatMessage(new ChatMessage(player, "But how can I defeat the dragon?", npc));
																						world.getDelayedEventHandler().add(new ShortEvent(player) {
																							public void action() {
																								player.informOfNpcMessage(new ChatMessage(npc, "Go talk to the guildmaster. He'll help if you are keen for a quest!", player));
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
								}
							});
						}
					});
					player.getActionSender().sendMenu(options);
				}
			});
			npc.blockedBy(player);
		}
	}