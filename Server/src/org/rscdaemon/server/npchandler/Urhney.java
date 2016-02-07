package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.ShortEvent;

public class Urhney implements NpcHandler {
	/**
	 * World instance
	 */
	 public static final World world = World.getWorld();
	 
	 public void handleNpc(final Npc npc, final Player player) throws Exception {
		if(player.getRestlessGhostStatus() == 0) { // Please go speak to the priest
			player.informOfNpcMessage(new ChatMessage(npc, "My friend is in need of some help. Please go speak to the priest.", player));
			return;
		}
		if(player.getRestlessGhostStatus() == 2) { // Recieved the amulet
			player.informOfNpcMessage(new ChatMessage(npc, "Don't forget to wield the amulet to hear the ghost!", player));
			return;
		}
		if(player.getRestlessGhostStatus() >= 3) { // Spoke to the ghost
			player.informOfChatMessage(new ChatMessage(player, "The amulet worked perfectly! Thank you!", npc));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					player.informOfNpcMessage(new ChatMessage(npc, "I knew it would work!", player));
					return;
				}
			});
		}
		if(player.getRestlessGhostStatus() == 1) { // Spoke to the priest
			player.informOfNpcMessage(new ChatMessage(npc, "What can I do for you?", player));
			player.setBusy(true);
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					player.setBusy(false);
					String[] option = new String[] { "I need help about ghost speech" };
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
									if(option == 0) { // I need help about the ghost
										final String ghostAmulet = "Amulet of GhostSpeak";
										player.informOfNpcMessage(new ChatMessage(npc, "The speech of a ghost is unheard by the human ear", player));
										world.getDelayedEventHandler().add(new ShortEvent(player) {
											public void action() {
												player.informOfNpcMessage(new ChatMessage(npc, "unless you are wielding a special item called " + ghostAmulet, player));
												world.getDelayedEventHandler().add(new ShortEvent(player) {
													public void action() {
														player.informOfChatMessage(new ChatMessage(player, "How can I get hold of the " + ghostAmulet, npc));
														world.getDelayedEventHandler().add(new ShortEvent(player) {
															public void action() {
																player.informOfNpcMessage(new ChatMessage(npc, "I have one lying around here somewhere", player));
																world.getDelayedEventHandler().add(new ShortEvent(player) {
																	public void action() {	
																		player.informOfNpcMessage(new ChatMessage(npc, "Ah! Here it is", player));
																		world.getDelayedEventHandler().add(new ShortEvent(player) {
																			public void action() {
																				player.getActionSender().sendMessage("You recieve a " + ghostAmulet + " from Urhney");
																				player.getInventory().add(new InvItem(24, 1));
																				player.getActionSender().sendInventory();
																				player.setRestlessGhostStatus(2);
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