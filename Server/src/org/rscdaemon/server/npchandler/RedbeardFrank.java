package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.ShortEvent;

public class RedbeardFrank implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
		if(player.getPiratesTreasureStatus() == 1 || player.getPiratesTreasureStatus() == 2) {
			player.informOfNpcMessage(new ChatMessage(npc, "Go get me that rum! I'm thirsty!", player));
			return;
		}
		if(player.getPiratesTreasureStatus() == 5 || player.getPiratesTreasureStatus() == 6) {
			player.informOfNpcMessage(new ChatMessage(npc, "Go get the treasure before someone else finds it!", player));
			return;
		}
		if(player.getPiratesTreasureStatus() == 7) {
			player.informOfNpcMessage(new ChatMessage(npc, "I hope you enjoyed the treasure you found!", player));
			return;
		}
		if(player.getPiratesTreasureStatus() == 4) {
			player.informOfNpcMessage(new ChatMessage(npc, "The chest is located in the Blue Moon Inn!", player));
			return;
		}
		if(player.getPiratesTreasureStatus() == 3) { // Got the rum
			player.informOfNpcMessage(new ChatMessage(npc, "Ah! Lovely! Thank you very much!", player));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					player.informOfChatMessage(new ChatMessage(player, "Now I did that for you, tell me where I can find some treasure!", npc));
					world.getDelayedEventHandler().add(new ShortEvent(player) {
						public void action() {
							player.informOfNpcMessage(new ChatMessage(npc, "Ah, yes! Go to the Blue Moon Inn and on the second floor is a chest", player));
							world.getDelayedEventHandler().add(new ShortEvent(player) {
								public void action() {
									player.informOfNpcMessage(new ChatMessage(npc, "Use this key on the chest and follow the instructions!", player));
									player.getInventory().add(new InvItem(382, 1));
									player.getInventory().remove(318, 1);
									player.getActionSender().sendInventory();
									player.setPiratesTreasureStatus(4);
									return;
								}
							});
						}
					});
				}
			});
		}
		if(player.getPiratesTreasureStatus() == 0) { // Just starting the quest
			player.informOfNpcMessage(new ChatMessage(npc, "G'day adventurer. How can I help you?", player));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					String[] option = new String[] { "What are you doing here?", "Where can I find some treasure?" };
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
												player.informOfNpcMessage(new ChatMessage(npc, "Nothing much, but I guess I could really do with a strong alcoholic drink!", player));
												return;
											}
											if(option == 1) {
												player.informOfNpcMessage(new ChatMessage(npc, "If you can do a little something for me, I will tell you where the treasure is!", player));
												world.getDelayedEventHandler().add(new ShortEvent(player) {
													public void action() {
														player.informOfChatMessage(new ChatMessage(player, "Sure. What can I do for you?", npc));
														world.getDelayedEventHandler().add(new ShortEvent(player) {
															public void action() {
																player.informOfNpcMessage(new ChatMessage(npc, "Take the boat to the island of Karamja and bring me some of their rum!", player));
																world.getDelayedEventHandler().add(new ShortEvent(player) {
																	public void action() {
																		player.informOfChatMessage(new ChatMessage(player, "Sure thing!", npc));
																		player.setPiratesTreasureStatus(1);
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