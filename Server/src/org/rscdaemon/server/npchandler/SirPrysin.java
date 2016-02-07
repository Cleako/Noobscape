package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.ShortEvent;

public class SirPrysin implements NpcHandler {
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
		if(player.getDemonSlayerStatus() == 2 || player.getDemonSlayerStatus() == 3) { // Spoke to Sir Prysin
			player.informOfNpcMessage(new ChatMessage(npc, "Please come back to me when you have found all 3 keys to unlock the silverlight chest", player));
			return;
		}
		if(player.getDemonSlayerStatus() >= 4 && player.getInventory().countId(25) <= 0 && player.getInventory().countId(26) <= 0 && player.getInventory().countId(51) <= 0) {
			player.informOfNpcMessage(new ChatMessage(npc, "Please go and get all 3 required keys. Delrith is dangerous!", player));
		} else
		if(player.getDemonSlayerStatus() >= 4 && player.getInventory().countId(25) >= 1 && player.getInventory().countId(26) >= 1 && player.getInventory().countId(51) >= 1) {
			player.informOfChatMessage(new ChatMessage(player, "Here you go. I have all the keys needed to unlock the silverlight!", npc));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Please take the silverlight and go slay Delrith!", player));
					player.getInventory().remove(25, 1);
					player.getInventory().remove(26, 1);
					player.getInventory().remove(51, 1);
					player.getInventory().add(new InvItem(52, 1));
					player.getActionSender().sendInventory();
					player.setDemonSlayerStatus(5);
					return;
				}
			});
		} else
		if(player.getDemonSlayerStatus() == 0) {
			player.informOfNpcMessage(new ChatMessage(npc, "Go speak with the gypsy. She looks like she's in trouble!", player));
			return;
		}
		if(player.getDemonSlayerStatus() == 1) { // Speaking to Sir Prysin for the first time
			player.informOfNpcMessage(new ChatMessage(npc, "What can I do for you?", player));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					String[] option = new String[] { "Gypsy Aries sent me about the Delrith", "Give me the key you have", "Nothing" };
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
												player.informOfNpcMessage(new ChatMessage(npc, "Stop wasting my time. I am a rather busy person!", player));
												return;
											}
											if(option == 1) { // Sir Prysin's Key
												player.informOfNpcMessage(new ChatMessage(npc, "My key is in the safe hands of Captain Rovin. Find him and tell him I sent you", player));
												return;
											}
											if(option == 0) { // Gypsy sent me
												player.informOfNpcMessage(new ChatMessage(npc, "Delrith you say? I thought he'd disappeared from this land", player));
												world.getDelayedEventHandler().add(new ShortEvent(player) {
													public void action() {
														player.informOfNpcMessage(new ChatMessage(npc, "But I am unsure of how I can be helping you", player));
														world.getDelayedEventHandler().add(new ShortEvent(player) {
															public void action() {
																player.informOfChatMessage(new ChatMessage(player, "The gypsys crystal ball shows me holding the silverlight", npc));
																world.getDelayedEventHandler().add(new ShortEvent(player) {
																	public void action() {
																		player.informOfChatMessage(new ChatMessage(player, "If you give me the silverlight, I will slay that beast!", npc));
																		world.getDelayedEventHandler().add(new ShortEvent(player) {
																			public void action() {
																				player.informOfNpcMessage(new ChatMessage(npc, "I locked the silverlight in my chest which requires 3 different keys to open", player));
																				world.getDelayedEventHandler().add(new ShortEvent(player) {
																					public void action() {
																						player.informOfNpcMessage(new ChatMessage(npc, "Bring me the 3 keys and I will gladly give you the silverlight to kill Delrith!", player));
																						player.setDemonSlayerStatus(2);
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