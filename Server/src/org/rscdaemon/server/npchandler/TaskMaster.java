package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.*;

public class TaskMaster implements NpcHandler {
		/**
		 * World instance
		 */
		 public static final World world = World.getWorld();
		 
		 public void handleNpc(final Npc npc, Player player) throws Exception {
				player.informOfNpcMessage(new ChatMessage(npc, "Would you like to begin a new task?", player));
				player.setBusy(true);
				final String[] answer = { "Yes. Give me a task", "I've come to collect my reward", "No thank you" };
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						owner.setBusy(false);
						owner.setMenuHandler(new MenuHandler(answer) {
							public void handleReply(final int option, final String reply) {
								owner.setBusy(true);
								owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
								world.getDelayedEventHandler().add(new ShortEvent(owner) {
									public void action() {
										if(option == 0) {
											if(!owner.getTask()) {
												owner.setRandomNPC(owner);
													owner.informOfNpcMessage(new ChatMessage(npc, "You need to kill " + owner.getRemaining() +" " + owner.getTaskNPC() + "", owner));
													world.getDelayedEventHandler().add(new ShortEvent(owner) {
														public void action() {
															owner.informOfNpcMessage(new ChatMessage(npc, "You will be rewarded with " + owner.getRandomCash() + " coins" + " and " + owner.getRandomXp() + " exp!", owner));
															world.getDelayedEventHandler().add(new ShortEvent(owner) {
																public void action() {
																owner.informOfNpcMessage(new ChatMessage(npc, "Come back when you are finished to collect your reward.", owner));
																	owner.getActionSender().sendMoneyTask();
																	owner.getActionSender().sendTaskStatus();
																	owner.getActionSender().sendExperienceReward();
																	owner.getActionSender().sendCashReward();
																	owner.getActionSender().sendItemReward();
																	owner.setBusy(false);
																	npc.unblock();
																}
															});
														}
													});
											} else {
														owner.informOfNpcMessage(new ChatMessage(npc, "You already have a task set.", owner));
														owner.setBusy(false);
														npc.unblock();
													}
												}
												if(option == 1) {
													if(!owner.getTask()) {
														owner.setBusy(false);
														owner.informOfNpcMessage(new ChatMessage(npc, "You don't have a completed task!", owner));
														return;
													}
													if(owner.getRemaining() > 0) {
														owner.informOfNpcMessage(new ChatMessage(npc, "You still have to kill " + owner.getRemaining() + " " + owner.getTaskNPC() + ".", owner));
														owner.setBusy(false);
														npc.unblock();
														return;
													}
											else {
													if(owner.getTask() && owner.getRemaining() <= 0) {
														owner.setTask(false);
														owner.getInventory().add(new InvItem(10, owner.getRandomCash()));
														owner.getInventory().add(new InvItem(owner.getRandomItem(), 1));
														owner.getInventory().add(new InvItem(1329, owner.getRandomXp()));
														owner.getActionSender().sendInventory();
														owner.setTaskPoints(owner.getTaskPoints() +1);
														owner.getActionSender().sendTaskPoints();
														owner.setCompletedTasks(owner.getCompletedTasks() +1);
														owner.getActionSender().sendCompletedTasks();
														owner.informOfNpcMessage(new ChatMessage(npc, "Congratulations you have completed the task, Here's your reward.", owner));
														owner.getActionSender().sendTaskStatus();
														owner.getActionSender().sendExperienceReward();
														owner.getActionSender().sendCashReward();
														owner.getActionSender().sendItemReward();
														owner.setBusy(false);
														npc.unblock();
													}
												}
										} else {
												owner.setBusy(false);
											}
										}
									});
								}
							});
							owner.getActionSender().sendMenu(answer);
						}
					});
					npc.blockedBy(player);
				}
			}