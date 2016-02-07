package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class Doric implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
			if(player.getDoricsQuestStatus() == 2) {
				player.informOfNpcMessage(new ChatMessage(npc, "Thank you. I really appreciate your help!", player));
				return;
			}
			if(player.getDoricsQuestStatus() == 1) {
				player.informOfNpcMessage(new ChatMessage(npc, "Have you got the materials I need yet?", player));
				player.setBusy(true);
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						player.setBusy(false);
						String[] option = new String[]{"Yes, I have them here", "No, sorry. I am still collecting them"};
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
											if(option == 1) {
												player.informOfNpcMessage(new ChatMessage(npc, "Ok. Come back when you have found them!", player));
												return;
											}
											if(option == 0 && player.getDoricsQuestStatus() == 1 && player.getInventory().countId(149) > 5 && player.getInventory().countId(150) > 3 && player.getInventory().countId(151) > 1) {
													player.informOfNpcMessage(new ChatMessage(npc, "Thank you! You saved the Duke's birthday!", player));
													player.getInventory().remove(149, 1);
													player.getInventory().remove(149, 1);
													player.getInventory().remove(149, 1);
													player.getInventory().remove(149, 1);
													player.getInventory().remove(149, 1);
													player.getInventory().remove(149, 1);
													player.getInventory().remove(150, 1);
													player.getInventory().remove(150, 1);
													player.getInventory().remove(150, 1);
													player.getInventory().remove(150, 1);
													player.getInventory().remove(151, 1);
													player.getInventory().remove(151, 1);
													player.getActionSender().sendInventory();
													player.isDoricsQuestComplete();
													player.setQuestPoints(player.getQuestPoints() +1);
													player.getActionSender().sendQuestPoints();
													player.getActionSender().sendDoricsQuestComplete();
													player.incExp(14, 250, false, false);
													player.getActionSender().sendStat(14);
													player.getActionSender().sendMessage("@gre@Congratulations! You have just completed the: @or1@Doric's Quest @gre@quest!");
													player.getActionSender().sendMessage("@gre@You gained @or1@1 @gre@quest point!");
											} else
													player.informOfNpcMessage(new ChatMessage(npc, "It doesn't look like you have my materials at the moment!", player));
									}
								});
							}
						});
						player.getActionSender().sendMenu(option);
					}
				});
			} else if(player.getDoricsQuestStatus() == 0) {
      		player.informOfNpcMessage(new ChatMessage(npc, "Oh, it seems I am short on a few materials. Can you help me?", player));
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				String[] options = new String[]{"What do you need? I'll try find them", "I am too busy to help, sorry"};
				owner.setMenuHandler(new MenuHandler(options) {
					public void handleReply(final int option, final String reply) {
						if(owner.isBusy()) {
							return;
						}
						owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
						owner.setBusy(true);
						world.getDelayedEventHandler().add(new ShortEvent(owner) {
							public void action() {
								owner.setBusy(false);
								if(option == 0) {
									owner.informOfNpcMessage(new ChatMessage(npc, "Thank you! I am missing the following materials:", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
											owner.informOfNpcMessage(new ChatMessage(npc, "6 Clay, 4 Copper Ore, 2 Iron Ore. Return to me when you have found them!", owner));
											owner.setDoricsQuestStatus(1);
											npc.unblock();
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
				owner.getActionSender().sendMenu(options);
      			}
      		});
      		npc.blockedBy(player);
		}
	}
}