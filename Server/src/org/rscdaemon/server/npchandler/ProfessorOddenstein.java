package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.ShortEvent;
import org.rscdaemon.server.event.SingleEvent;

public class ProfessorOddenstein implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
		if(player.getErnestTheChickenStatus() == 0) {
			player.informOfNpcMessage(new ChatMessage(npc, "Please go away! I am having machine malfunction problems!", player));
			return;
		}
		if(player.getErnestTheChickenStatus() == 4) {
			player.informOfNpcMessage(new ChatMessage(npc, "Thanks for finding my machine parts!", player));
			return;
		}
		if(player.getErnestTheChickenStatus() == 3) {
			if(player.getInventory().countId(213) >= 1 && player.getInventory().countId(175) >= 1 && player.getInventory().countId(208) >= 1) {
				player.informOfNpcMessage(new ChatMessage(npc, "Thank you! I can now repair my machine and change Ernest back into a human", player));
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						int ernest = 92;
						final Npc e = new Npc(ernest, player.getX(), player.getY(), player.getX() - 2, player.getX() + 2, player.getY() - 2, player.getY() + 2);
						e.setRespawn(false);
						world.registerNpc(e);
						player.setErnestTheChickenStatus(4);
						player.getActionSender().sendErnestTheChickenComplete();
						player.getActionSender().sendMessage("@gre@Congratulations! You just completed the @or1@Ernest the Chicken@gre@ quest!");
						player.getActionSender().sendMessage("@gre@You gained @or1@4 @gre@quest points!");
						player.setQuestPoints(player.getQuestPoints() +4);
						player.getActionSender().sendQuestPoints();
						player.getInventory().remove(213, 1);
						player.getInventory().remove(175, 1);
						player.getInventory().remove(208, 1);
						player.getInventory().add(new InvItem(10, 300));
						player.getActionSender().sendInventory();
						world.getDelayedEventHandler().add(new SingleEvent(null, 60000) {
							public void action() {
								world.unregisterNpc(e);
								e.remove();
							}
						});
					}
				});
			}
		}				
		if(player.getErnestTheChickenStatus() == 2) {
			player.informOfNpcMessage(new ChatMessage(npc, "Please come back when you have found my materials!", player));
			return;
		}
		if(player.getErnestTheChickenStatus() == 1) {
			player.informOfChatMessage(new ChatMessage(player, "Do you know where I can find Ernest? Veronica is worried about him", npc));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					player.informOfNpcMessage(new ChatMessage(npc, "Ernest was helping me with an experiment that went wrong!", player));
					world.getDelayedEventHandler().add(new ShortEvent(player) {
						public void action() {
							player.informOfChatMessage(new ChatMessage(player, "What do you mean it went wrong?", npc));
							world.getDelayedEventHandler().add(new ShortEvent(player) {
								public void action() {
									player.informOfNpcMessage(new ChatMessage(npc, "Ernest is right over there, he is a chicken!", player));
									world.getDelayedEventHandler().add(new ShortEvent(player) {
										public void action() {
											player.informOfChatMessage(new ChatMessage(player, "What do you mean he's a chicken? Change him back right away!", npc));
											world.getDelayedEventHandler().add(new ShortEvent(player) {
												public void action() {
													player.informOfNpcMessage(new ChatMessage(npc, "It's not as easy as that, the imps have stolen some of my machine parts!", player));
													world.getDelayedEventHandler().add(new ShortEvent(player) {
														public void action() {
															player.informOfNpcMessage(new ChatMessage(npc, "Please find my machine parts and return them to me immediately!", player));
															player.setErnestTheChickenStatus(2);
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
			npc.blockedBy(player);
		}
	}
}