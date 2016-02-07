package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.ShortEvent;

public class Adventurer implements NpcHandler {
	/**
	 * World Instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
		player.informOfNpcMessage(new ChatMessage(npc, "Would you like to go to a wild or town location?", player));
		world.getDelayedEventHandler().add(new ShortEvent(player) {
			public void action() {
				player.setBusy(false);
				String[] option = new String[] { "Town", "Wilderness" };
				final String[] towns = new String[] { "Edgeville", "Varrock", "Falador", "Seers", "Catherby", "Yanille", "Karamja", "Draynor", "Lumbridge" };
				final String[] wild = new String[] { "Castle", "Dragon Maze", "Mage Arena" };
				player.getActionSender().sendMenu(option);
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
									if(option == 0) { // Town
										player.informOfNpcMessage(new ChatMessage(npc, "Now select the town you want to go to", player));
										world.getDelayedEventHandler().add(new ShortEvent(player) {
											public void action() {
												player.getActionSender().sendMenu(towns);
												player.setMenuHandler(new MenuHandler(towns) {
													public void handleReply(final int towns, final String reply) {
														player.informOfChatMessage(new ChatMessage(player, reply, npc));
														world.getDelayedEventHandler().add(new ShortEvent(player) {
															public void action() {
																if(towns == 0) { // Edgeville
																	player.teleport(225, 447, false);
																	player.getActionSender().sendMessage("You arrive in Edgeville");
																} else if(towns == 1) { // Varrock
																	player.teleport(122, 503, false);
																	player.getActionSender().sendMessage("You arrive in Varrock");
																} else if(towns == 2) { // Falador
																	player.teleport(313, 550, false);
																	player.getActionSender().sendMessage("You arrive in Falador");
																} else if(towns == 3) { // Seers
																	player.teleport(501, 455, false);
																	player.getActionSender().sendMessage("You arrive in Seers");
																} else if(towns == 4) { // Catherby
																	player.teleport(440, 500, false);
																	player.getActionSender().sendMessage("You arrive in Catherby");
																} else if(towns == 5) { // Yanille
																	player.teleport(587, 761, false);
																	player.getActionSender().sendMessage("You arrive in Yanille");
																} else if(towns == 6) { // Karamja
																	player.teleport(371, 695, false);
																	player.getActionSender().sendMessage("You arrive in Karamja");
																} else if(towns == 7) { // Ardougne
																	player.teleport(585, 621, false);
																	player.getActionSender().sendMessage("You arrive in Ardougne");
																} else if(towns == 8) { // Draynor
																	player.teleport(214, 632, false);
																	player.getActionSender().sendMessage("You arrive in Draynor");
																} else if(towns == 9) { // Lumbridge
																	player.teleport(122, 647, false);
																	player.getActionSender().sendMessage("You arrive in Lumbridge");
																}
															}
														});
													}
												});
											}
										});
									} else if(option == 1) { // Wilderness
										player.informOfNpcMessage(new ChatMessage(npc, "Now select the wilderness location you want to go to", player));
										world.getDelayedEventHandler().add(new ShortEvent(player) {
											public void action() {
												player.getActionSender().sendMenu(wild);
													player.setMenuHandler(new MenuHandler(wild) {
														public void handleReply(final int wild, final String reply) {
															player.informOfChatMessage(new ChatMessage(player, reply, npc));
															world.getDelayedEventHandler().add(new ShortEvent(player) {
																public void action() {
																	if(wild == 0) { // Castle
																		player.teleport(271, 353, false);
																		player.getActionSender().sendMessage("You arrive at the Castle");
																	} else if(wild == 1) { // Dragon Maze
																		player.teleport(268, 197, false);
																		player.getActionSender().sendMessage("You arrive at the Dragon Maze");
																	} else if(wild == 2) { // Mage Arena
																		player.teleport(447, 3371, false);
																		player.getActionSender().sendMessage("You arrive at the Mage Arena");
																		player.setBusy(false);
																		npc.unblock();
																		}
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
						}
					});
					npc.blockedBy(player);
				}
			}