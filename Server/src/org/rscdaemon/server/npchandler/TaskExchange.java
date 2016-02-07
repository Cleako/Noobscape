package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;
import org.rscdaemon.server.util.Formulae;

public class TaskExchange implements NpcHandler {
		/**
		 * World instance
		 */
		 public static final World world = World.getWorld();
		 
		 public void handleNpc(final Npc npc, final Player player) throws Exception {
				if(player.getTaskPoints() >= 1 || player.getInventory().countId(1329) >= 1) {
					player.informOfNpcMessage(new ChatMessage(npc, "What would you like to exchange?", player));
					player.setBusy(true);
					world.getDelayedEventHandler().add(new ShortEvent(player) {
						public void action() {
							player.setBusy(false);
							String[] options = new String[] { "What can I exchange?", "Experience Tokens", "Cancel Current Task" };
							final String[] options2 = new String[] { "Attack", "Strength", "Defense", "Prayer", "Ranged", "Magic" };
							final String[] options3 = new String[] { "[5k EXP] 1 Token", "[10k EXP] 2 Tokens", "[50k EXP] 10 Tokens", "[100K EXP] 20 Tokens", "[1M EXP] 200 Tokens" };
							player.getActionSender().sendMenu(options);
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
												if(option == 0) { // What can I exchange?
													player.informOfNpcMessage(new ChatMessage(npc, "By using 3 of your task points, you can cancel your current task", player));
													world.getDelayedEventHandler().add(new ShortEvent(player) {
														public void action() {
															player.informOfNpcMessage(new ChatMessage(npc, "or by completing tasks, you can use your experience tokens", player));
															world.getDelayedEventHandler().add(new ShortEvent(player) {
																public void action() {
																	player.informOfNpcMessage(new ChatMessage(npc, "and exchange them for experience in 6 different combat skills.", player));
																	return;
																}
															});
														}
													});
												}
												if(option == 2) { // Cancel Current Task
													if(!player.getTask() || player.getTaskPoints() < 3) {
														player.informOfNpcMessage(new ChatMessage(npc, "You either don't have a task or you don't have enough task points.", player));
														player.setBusy(false);
													}
											else {
												player.setTaskPoints(owner.getTaskPoints() -3);
												player.getActionSender().sendTaskPoints();
												player.setTask(false);
												player.informOfNpcMessage(new ChatMessage(npc, "Your current task has been cancelled successfully.", player));
												player.getActionSender().sendTaskStatus();
												player.setRemaining(owner.getRemaining() - owner.getRemaining());
												owner.getActionSender().sendRemaining();
												owner.setBusy(false);
												return;
											}
										}
										if(option == 1) { // Experience Tokens
											player.informOfNpcMessage(new ChatMessage(npc, "Please choose the skill you want to increase...", player));
											world.getDelayedEventHandler().add(new ShortEvent(player) {
												public void action() {
													player.getActionSender().sendMenu(options2);
													player.setMenuHandler(new MenuHandler(options2) {
														public void handleReply(final int option2, final String reply) {
															player.informOfChatMessage(new ChatMessage(player, reply, npc));
																world.getDelayedEventHandler().add(new ShortEvent(player) {
																	public void action() {
																
										if(option2 == 0 || option2 == 1 || option2 == 2 || option2 == 3 || option2 == 4 || option2 == 5) { // Stats
											player.informOfNpcMessage(new ChatMessage(npc, "Please choose the amount of experience you want to exchange...", player));
												world.getDelayedEventHandler().add(new ShortEvent(player) {
													public void action() {
														player.getActionSender().sendMenu(options3);
															player.setMenuHandler(new MenuHandler(options3) {
																public void handleReply(final int option3, final String reply) {
																	player.informOfChatMessage(new ChatMessage(player, reply, npc));
																		world.getDelayedEventHandler().add(new ShortEvent(player) {
																			public void action() {
																				
										if(option2 == 0 && option3 == 0) { // Attack - 5,000 Experience
											if(player.getInventory().countId(1329) < 1) {
												player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
												return;
											}
											player.setBusy(true);
											player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
												world.getDelayedEventHandler().add(new ShortEvent(player) {
													public void action() {
														player.getInventory().remove(1329, 1);
														player.getActionSender().sendInventory();
														player.incExp(0, 5000, false, false);
														player.getActionSender().sendStat(0);
														int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
														int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
														if(hitpointsLVL < 10) {
															if(hitpointsLVL > player.getCurStat(3)) {
																player.setCurStat(3, 10);
															}
															hitpointsLVL = 10;
															player.setMaxStat(3, 10);
															player.setExp(3, 1155);
															player.getActionSender().sendStats();
														}
														else {
															if(hitpointsLVL > player.getCurStat(3)) {
																player.setCurStat(3, hitpointsLVL);
															}
															player.setMaxStat(3, hitpointsLVL);
															player.setExp(3, hitpointsXp);
															player.getActionSender().sendStats();
														}
														int comb = Formulae.getCombatlevel(player.getMaxStats());
														if(comb != player.getCombatLevel()) {
															player.setCombatLevel(comb);
															player.getActionSender().sendStats();
														}
														npc.unblock();
														player.setBusy(false);
														return;
													}
												});
											}
											else if(option2 == 0 && option3 == 1) { // Attack - 10,000 Experience
												if(player.getInventory().countId(1329) < 2) {
													player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
													return;
												}
												player.setBusy(true);
												player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
													world.getDelayedEventHandler().add(new ShortEvent(player) {
														public void action() {
															player.getInventory().remove(1329, 2);
															player.getActionSender().sendInventory();
															player.incExp(0, 10000, false, false);
															player.getActionSender().sendStat(0);
															int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
															int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
															if(hitpointsLVL < 10) {
																if(hitpointsLVL > player.getCurStat(3)) {
																	player.setCurStat(3, 10);
																}
																hitpointsLVL = 10;
																player.setMaxStat(3, 10);
																player.setExp(3, 1155);
																player.getActionSender().sendStats();
															}
															else {
																if(hitpointsLVL > player.getCurStat(3)) {
																	player.setCurStat(3, hitpointsLVL);
																}
																player.setMaxStat(3, hitpointsLVL);
																player.setExp(3, hitpointsXp);
																player.getActionSender().sendStats();
															}
															int comb = Formulae.getCombatlevel(player.getMaxStats());
															if(comb != player.getCombatLevel()) {
																player.setCombatLevel(comb);
																player.getActionSender().sendStats();
															}
															npc.unblock();
															player.setBusy(false);
															return;
														}
													});
												}
												else if(option2 == 0 && option3 == 2) { // Attack - 50,000 Experience
													if(player.getInventory().countId(1329) < 10) {
														player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
														return;
													}
													player.setBusy(true);
													player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
														world.getDelayedEventHandler().add(new ShortEvent(player) {
															public void action() {
																player.getInventory().remove(1329, 10);
																player.getActionSender().sendInventory();
																player.incExp(0, 50000, false, false);
																player.getActionSender().sendStat(0);
																int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																if(hitpointsLVL < 10) {
																	if(hitpointsLVL > player.getCurStat(3)) {
																		player.setCurStat(3, 10);
																	}
																	hitpointsLVL = 10;
																	player.setMaxStat(3, 10);
																	player.setExp(3, 1155);
																	player.getActionSender().sendStats();
																}
																else {
																	if(hitpointsLVL > player.getCurStat(3)) {
																		player.setCurStat(3, hitpointsLVL);
																	}
																	player.setMaxStat(3, hitpointsLVL);
																	player.setExp(3, hitpointsXp);
																	player.getActionSender().sendStats();
																}
																int comb = Formulae.getCombatlevel(player.getMaxStats());
																if(comb != player.getCombatLevel()) {
																	player.setCombatLevel(comb);
																	player.getActionSender().sendStats();
																}
																npc.unblock();
																player.setBusy(false);
																return;
															}
														});
													}
													else if(option2 == 0 && option3 == 3) { // Attack - 100,000 Experience
														if(player.getInventory().countId(1329) < 20) {
															player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
															return;
														}
														player.setBusy(true);
														player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
															world.getDelayedEventHandler().add(new ShortEvent(player) {
																public void action() {
																	player.getInventory().remove(1329, 20);
																	player.getActionSender().sendInventory();
																	player.incExp(0, 100000, false, false);
																	player.getActionSender().sendStat(0);
																	int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																	int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																	if(hitpointsLVL < 10) {
																		if(hitpointsLVL > player.getCurStat(3)) {
																			player.setCurStat(3, 10);
																		}
																		hitpointsLVL = 10;
																		player.setMaxStat(3, 10);
																		player.setExp(3, 1155);
																		player.getActionSender().sendStats();
																	}
																	else {
																		if(hitpointsLVL > player.getCurStat(3)) {
																			player.setCurStat(3, hitpointsLVL);
																		}
																		player.setMaxStat(3, hitpointsLVL);
																		player.setExp(3, hitpointsXp);
																		player.getActionSender().sendStats();
																	}
																	int comb = Formulae.getCombatlevel(player.getMaxStats());
																	if(comb != player.getCombatLevel()) {
																		player.setCombatLevel(comb);
																		player.getActionSender().sendStats();
																	}
																	npc.unblock();
																	player.setBusy(false);
																	return;
																}
															});
														}
														else if(option2 == 0 && option3 == 4) { // Attack - 1,000,000 Experience
															if(player.getInventory().countId(1329) < 200) {
																player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																return;
															}
															player.setBusy(true);
															player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																world.getDelayedEventHandler().add(new ShortEvent(player) {
																	public void action() {
																		player.getInventory().remove(1329, 200);
																		player.getActionSender().sendInventory();
																		player.incExp(0, 1000000, false, false);
																		player.getActionSender().sendStat(0);
																		int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																		int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																		if(hitpointsLVL < 10) {
																			if(hitpointsLVL > player.getCurStat(3)) {
																				player.setCurStat(3, 10);
																			}
																			hitpointsLVL = 10;
																			player.setMaxStat(3, 10);
																			player.setExp(3, 1155);
																			player.getActionSender().sendStats();
																		}
																		else {
																			if(hitpointsLVL > player.getCurStat(3)) {
																				player.setCurStat(3, hitpointsLVL);
																			}
																			player.setMaxStat(3, hitpointsLVL);
																			player.setExp(3, hitpointsXp);
																			player.getActionSender().sendStats();
																		}
																		int comb = Formulae.getCombatlevel(player.getMaxStats());
																		if(comb != player.getCombatLevel()) {
																			player.setCombatLevel(comb);
																			player.getActionSender().sendStats();
																		}
																		npc.unblock();
																		player.setBusy(false);
																		return;
																	}
																});
															}
															else if(option2 == 1 && option3 == 0) { // Strength - 5,000 Experience
																if(player.getInventory().countId(1329) < 1) {
																	player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																	return;
																}
																player.setBusy(true);
																player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																	world.getDelayedEventHandler().add(new ShortEvent(player) {
																		public void action() {
																			player.getInventory().remove(1329, 1);
																			player.getActionSender().sendInventory();
																			player.incExp(2, 5000, false, false);
																			player.getActionSender().sendStat(2);
																			int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																			int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																			if(hitpointsLVL < 10) {
																				if(hitpointsLVL > player.getCurStat(3)) {
																					player.setCurStat(3, 10);
																				}
																				hitpointsLVL = 10;
																				player.setMaxStat(3, 10);
																				player.setExp(3, 1155);
																				player.getActionSender().sendStats();
																			}
																			else {
																				if(hitpointsLVL > player.getCurStat(3)) {
																					player.setCurStat(3, hitpointsLVL);
																				}
																				player.setMaxStat(3, hitpointsLVL);
																				player.setExp(3, hitpointsXp);
																				player.getActionSender().sendStats();
																			}
																			int comb = Formulae.getCombatlevel(player.getMaxStats());
																			if(comb != player.getCombatLevel()) {
																				player.setCombatLevel(comb);
																				player.getActionSender().sendStats();
																			}
																			npc.unblock();
																			player.setBusy(false);
																			return;
																		}
																	});
																}
																else if(option2 == 1 && option3 == 1) { // Strength - 10,000 Experience
																	if(player.getInventory().countId(1329) < 2) {
																		player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																		return;
																	}
																	player.setBusy(true);
																	player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																		world.getDelayedEventHandler().add(new ShortEvent(player) {
																			public void action() {
																				player.getInventory().remove(1329, 2);
																				player.getActionSender().sendInventory();
																				player.incExp(2, 10000, false, false);
																				player.getActionSender().sendStat(2);
																				int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																				int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																				if(hitpointsLVL < 10) {
																					if(hitpointsLVL > player.getCurStat(3)) {
																						player.setCurStat(3, 10);
																					}
																					hitpointsLVL = 10;
																					player.setMaxStat(3, 10);
																					player.setExp(3, 1155);
																					player.getActionSender().sendStats();
																				}
																				else {
																					if(hitpointsLVL > player.getCurStat(3)) {
																						player.setCurStat(3, hitpointsLVL);
																					}
																					player.setMaxStat(3, hitpointsLVL);
																					player.setExp(3, hitpointsXp);
																					player.getActionSender().sendStats();
																				}
																				int comb = Formulae.getCombatlevel(player.getMaxStats());
																				if(comb != player.getCombatLevel()) {
																					player.setCombatLevel(comb);
																					player.getActionSender().sendStats();
																				}
																				npc.unblock();
																				player.setBusy(false);
																				return;
																			}
																		});
																	} 
																	else if(option2 == 1 && option3 == 2) { // Strength - 50,000 Experience
																		if(player.getInventory().countId(1329) < 10) {
																			player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																			return;
																		}
																		player.setBusy(true);
																		player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																			world.getDelayedEventHandler().add(new ShortEvent(player) {
																				public void action() {
																					player.getInventory().remove(1329, 10);
																					player.getActionSender().sendInventory();
																					player.incExp(2, 50000, false, false);
																					player.getActionSender().sendStat(2);
																					int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																					int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																					if(hitpointsLVL < 10) {
																						if(hitpointsLVL > player.getCurStat(3)) {
																							player.setCurStat(3, 10);
																						}
																						hitpointsLVL = 10;
																						player.setMaxStat(3, 10);
																						player.setExp(3, 1155);
																						player.getActionSender().sendStats();
																					}
																					else {
																						if(hitpointsLVL > player.getCurStat(3)) {
																							player.setCurStat(3, hitpointsLVL);
																						}
																						player.setMaxStat(3, hitpointsLVL);
																						player.setExp(3, hitpointsXp);
																						player.getActionSender().sendStats();
																					}
																					int comb = Formulae.getCombatlevel(player.getMaxStats());
																					if(comb != player.getCombatLevel()) {
																						player.setCombatLevel(comb);
																						player.getActionSender().sendStats();
																					}
																					npc.unblock();
																					player.setBusy(false);
																					return;
																				}
																			});
																		}
																		else if(option2 == 1 && option3 == 3) { // Strength - 100,000 Experience
																			if(player.getInventory().countId(1329) < 20) {
																				player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																				return;
																			}
																			player.setBusy(true);
																			player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																				world.getDelayedEventHandler().add(new ShortEvent(player) {
																					public void action() {
																						player.getInventory().remove(1329, 20);
																						player.getActionSender().sendInventory();
																						player.incExp(2, 100000, false, false);
																						player.getActionSender().sendStat(2);
																						int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																						int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																						if(hitpointsLVL < 10) {
																							if(hitpointsLVL > player.getCurStat(3)) {
																								player.setCurStat(3, 10);
																							}
																							hitpointsLVL = 10;
																							player.setMaxStat(3, 10);
																							player.setExp(3, 1155);
																							player.getActionSender().sendStats();
																						}
																						else {
																							if(hitpointsLVL > player.getCurStat(3)) {
																								player.setCurStat(3, hitpointsLVL);
																							}
																							player.setMaxStat(3, hitpointsLVL);
																							player.setExp(3, hitpointsXp);
																							player.getActionSender().sendStats();
																						}
																						int comb = Formulae.getCombatlevel(player.getMaxStats());
																						if(comb != player.getCombatLevel()) {
																							player.setCombatLevel(comb);
																							player.getActionSender().sendStats();
																						}
																						npc.unblock();
																						player.setBusy(false);
																						return;
																					}
																				});
																			}
																			else if(option2 == 1 && option3 == 4) { // Strength - 1,000,000 Experience
																				if(player.getInventory().countId(1329) < 200) {
																					player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																					return;
																				}
																				player.setBusy(true);
																				player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																					world.getDelayedEventHandler().add(new ShortEvent(player) {
																						public void action() {
																							player.getInventory().remove(1329, 200);
																							player.getActionSender().sendInventory();
																							player.incExp(2, 1000000, false, false);
																							player.getActionSender().sendStat(2);
																							int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																							int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																							if(hitpointsLVL < 10) {
																								if(hitpointsLVL > player.getCurStat(3)) {
																									player.setCurStat(3, 10);
																								}
																								hitpointsLVL = 10;
																								player.setMaxStat(3, 10);
																								player.setExp(3, 1155);
																								player.getActionSender().sendStats();
																							}
																							else {
																								if(hitpointsLVL > player.getCurStat(3)) {
																									player.setCurStat(3, hitpointsLVL);
																								}
																								player.setMaxStat(3, hitpointsLVL);
																								player.setExp(3, hitpointsXp);
																								player.getActionSender().sendStats();
																							}
																							int comb = Formulae.getCombatlevel(player.getMaxStats());
																							if(comb != player.getCombatLevel()) {
																								player.setCombatLevel(comb);
																								player.getActionSender().sendStats();
																							}
																							npc.unblock();
																							player.setBusy(false);
																							return;
																						}
																					});
																				}
																				else if(option2 == 2 && option3 == 0) { // Defense - 5,000 Experience
																					if(player.getInventory().countId(1329) < 1) {
																						player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																						return;
																					}
																					player.setBusy(true);
																					player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																						world.getDelayedEventHandler().add(new ShortEvent(player) {
																							public void action() {
																								player.getInventory().remove(1329, 1);
																								player.getActionSender().sendInventory();
																								player.incExp(1, 5000, false, false);
																								player.getActionSender().sendStat(1);
																								int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																								int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																								if(hitpointsLVL < 10) {
																									if(hitpointsLVL > player.getCurStat(3)) {
																										player.setCurStat(3, 10);
																									}
																									hitpointsLVL = 10;
																									player.setMaxStat(3, 10);
																									player.setExp(3, 1155);
																									player.getActionSender().sendStats();
																								}
																								else {
																									if(hitpointsLVL > player.getCurStat(3)) {
																										player.setCurStat(3, hitpointsLVL);
																									}
																									player.setMaxStat(3, hitpointsLVL);
																									player.setExp(3, hitpointsXp);
																									player.getActionSender().sendStats();
																								}
																								int comb = Formulae.getCombatlevel(player.getMaxStats());
																								if(comb != player.getCombatLevel()) {
																									player.setCombatLevel(comb);
																									player.getActionSender().sendStats();
																								}
																								npc.unblock();
																								player.setBusy(false);
																								return;
																							}
																						});
																					}
																					else if(option2 == 2 && option3 == 1) { // Defense - 10,000 Experience
																						if(player.getInventory().countId(1329) < 2) {
																							player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																							return;
																						}
																						player.setBusy(true);
																						player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																							world.getDelayedEventHandler().add(new ShortEvent(player) {
																								public void action() {
																									player.getInventory().remove(1329, 2);
																									player.getActionSender().sendInventory();
																									player.incExp(1, 10000, false, false);
																									player.getActionSender().sendStat(1);
																									int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																									int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																									if(hitpointsLVL < 10) {
																										if(hitpointsLVL > player.getCurStat(3)) {
																											player.setCurStat(3, 10);
																										}
																										hitpointsLVL = 10;
																										player.setMaxStat(3, 10);
																										player.setExp(3, 1155);
																										player.getActionSender().sendStats();
																									}
																									else {
																										if(hitpointsLVL > player.getCurStat(3)) {
																											player.setCurStat(3, hitpointsLVL);
																										}
																										player.setMaxStat(3, hitpointsLVL);
																										player.setExp(3, hitpointsXp);
																										player.getActionSender().sendStats();
																									}
																									int comb = Formulae.getCombatlevel(player.getMaxStats());
																									if(comb != player.getCombatLevel()) {
																										player.setCombatLevel(comb);
																										player.getActionSender().sendStats();
																									}
																									npc.unblock();
																									player.setBusy(false);
																									return;
																								}
																							});
																						}
																						else if(option2 == 2 && option3 == 2) { // Defense - 50,000 Experience
																							if(player.getInventory().countId(1329) < 10) {
																								player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																								return;
																							}
																							player.setBusy(true);
																							player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																								world.getDelayedEventHandler().add(new ShortEvent(player) {
																									public void action() {
																										player.getInventory().remove(1329, 10);
																										player.getActionSender().sendInventory();
																										player.incExp(1, 50000, false, false);
																										player.getActionSender().sendStat(1);
																										int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																										int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																										if(hitpointsLVL < 10) {
																											if(hitpointsLVL > player.getCurStat(3)) {
																												player.setCurStat(3, 10);
																											}
																											hitpointsLVL = 10;
																											player.setMaxStat(3, 10);
																											player.setExp(3, 1155);
																											player.getActionSender().sendStats();
																										}
																										else {
																											if(hitpointsLVL > player.getCurStat(3)) {
																												player.setCurStat(3, hitpointsLVL);
																											}
																											player.setMaxStat(3, hitpointsLVL);
																											player.setExp(3, hitpointsXp);
																											player.getActionSender().sendStats();
																										}
																										int comb = Formulae.getCombatlevel(player.getMaxStats());
																										if(comb != player.getCombatLevel()) {
																											player.setCombatLevel(comb);
																											player.getActionSender().sendStats();
																										}
																										npc.unblock();
																										player.setBusy(false);
																										return;
																									}
																								});
																							}
																							else if(option2 == 2 && option3 == 3) { // Defense - 100,000 Experience
																								if(player.getInventory().countId(1329) < 20) {
																									player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																									return;
																								}
																								player.setBusy(true);
																								player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																									world.getDelayedEventHandler().add(new ShortEvent(player) {
																										public void action() {
																											player.getInventory().remove(1329, 20);
																											player.getActionSender().sendInventory();
																											player.incExp(1, 100000, false, false);
																											player.getActionSender().sendStat(1);
																											int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																											int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																											if(hitpointsLVL < 10) {
																												if(hitpointsLVL > player.getCurStat(3)) {
																													player.setCurStat(3, 10);
																												}
																												hitpointsLVL = 10;
																												player.setMaxStat(3, 10);
																												player.setExp(3, 1155);
																												player.getActionSender().sendStats();
																											}
																											else {
																												if(hitpointsLVL > player.getCurStat(3)) {
																													player.setCurStat(3, hitpointsLVL);
																												}
																												player.setMaxStat(3, hitpointsLVL);
																												player.setExp(3, hitpointsXp);
																												player.getActionSender().sendStats();
																											}
																											int comb = Formulae.getCombatlevel(player.getMaxStats());
																											if(comb != player.getCombatLevel()) {
																												player.setCombatLevel(comb);
																												player.getActionSender().sendStats();
																											}
																											npc.unblock();
																											player.setBusy(false);
																											return;
																										}
																									});
																								}
																								else if(option2 == 2 && option3 == 4) { // Defense - 1,000,000 Experience
																									if(player.getInventory().countId(1329) < 200) {
																										player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																										return;
																									}
																									player.setBusy(true);
																									player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																										world.getDelayedEventHandler().add(new ShortEvent(player) {
																											public void action() {
																												player.getInventory().remove(1329, 200);
																												player.getActionSender().sendInventory();
																												player.incExp(1, 1000000, false, false);
																												player.getActionSender().sendStat(1);
																												int hitpointsXp = 1154 + (player.getExp(0) + player.getExp(1) + player.getExp(2)) / 3;
																												int hitpointsLVL = Formulae.experienceToLevel(hitpointsXp);
																												if(hitpointsLVL < 10) {
																													if(hitpointsLVL > player.getCurStat(3)) {
																														player.setCurStat(3, 10);
																													}
																													hitpointsLVL = 10;
																													player.setMaxStat(3, 10);
																													player.setExp(3, 1155);
																													player.getActionSender().sendStats();
																												}
																												else {
																													if(hitpointsLVL > player.getCurStat(3)) {
																														player.setCurStat(3, hitpointsLVL);
																													}
																													player.setMaxStat(3, hitpointsLVL);
																													player.setExp(3, hitpointsXp);
																													player.getActionSender().sendStats();
																												}
																												int comb = Formulae.getCombatlevel(player.getMaxStats());
																												if(comb != player.getCombatLevel()) {
																													player.setCombatLevel(comb);
																													player.getActionSender().sendStats();
																												}
																												npc.unblock();
																												player.setBusy(false);
																												return;
																											}
																										});
																									} 
																									else if(option2 == 3 && option3 == 0) { // Prayer - 5,000 Experience
																										if(player.getInventory().countId(1329) < 1) {
																											player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																											return;
																										}
																										player.setBusy(true);
																										player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																											world.getDelayedEventHandler().add(new ShortEvent(player) {
																												public void action() {
																													player.getInventory().remove(1329, 1);
																													player.getActionSender().sendInventory();
																													player.incExp(5, 5000, false, false);
																													player.getActionSender().sendStat(5);
																													npc.unblock();
																													player.setBusy(false);
																													return;
																												}
																											});
																										}
																										else if(option2 == 3 && option3 == 1) { // Prayer - 10,000 Experience
																											if(player.getInventory().countId(1329) < 2) {
																												player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																												return;
																											}
																											player.setBusy(true);
																											player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																												world.getDelayedEventHandler().add(new ShortEvent(player) {
																													public void action() {
																														player.getInventory().remove(1329, 2);
																														player.getActionSender().sendInventory();
																														player.incExp(5, 10000, false, false);
																														player.getActionSender().sendStat(5);
																														npc.unblock();
																														player.setBusy(false);
																														return;
																													}
																												});
																											}
																											else if(option2 == 3 && option3 == 2) { // Prayer - 50,000 Experience
																												if(player.getInventory().countId(1329) < 10) {
																													player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																													return;
																												}
																												player.setBusy(true);
																												player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																													world.getDelayedEventHandler().add(new ShortEvent(player) {
																														public void action() {
																															player.getInventory().remove(1329, 10);
																															player.getActionSender().sendInventory();
																															player.incExp(5, 50000, false, false);
																															player.getActionSender().sendStat(5);
																															npc.unblock();
																															player.setBusy(false);
																															return;
																														}
																													});
																												}
																												else if(option2 == 3 && option3 == 3) { // Prayer - 100,000 Experience
																													if(player.getInventory().countId(1329) < 20) {
																														player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																														return;
																													}
																													player.setBusy(true);
																													player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																														world.getDelayedEventHandler().add(new ShortEvent(player) {
																															public void action() {
																																player.getInventory().remove(1329, 20);
																																player.getActionSender().sendInventory();
																																player.incExp(5, 100000, false, false);
																																player.getActionSender().sendStat(5);
																																npc.unblock();
																																player.setBusy(false);
																																return;
																															}
																														});
																													}
																													else if(option2 == 3 && option3 == 4) { // Prayer - 1,000,000 Experience
																														if(player.getInventory().countId(1329) < 200) {
																															player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																															return;
																														}
																														player.setBusy(true);
																														player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																															world.getDelayedEventHandler().add(new ShortEvent(player) {
																																public void action() {
																																	player.getInventory().remove(1329, 200);
																																	player.getActionSender().sendInventory();
																																	player.incExp(5, 1000000, false, false);
																																	player.getActionSender().sendStat(5);
																																	npc.unblock();
																																	player.setBusy(false);
																																	return;
																																}
																															});
																														}
																														else if(option2 == 4 && option3 == 0) { // Ranged - 5,000 Experience
																															if(player.getInventory().countId(1329) < 1) {
																																player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																																return;
																															}
																															player.setBusy(true);
																															player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																																world.getDelayedEventHandler().add(new ShortEvent(player) {
																																	public void action() {
																																		player.getInventory().remove(1329, 1);
																																		player.getActionSender().sendInventory();
																																		player.incExp(4, 5000, false, false);
																																		player.getActionSender().sendStat(4);
																																		npc.unblock();
																																		player.setBusy(false);
																																		return;
																																	}
																																});
																															}
																															else if(option2 == 4 && option3 == 1) { // Ranged - 10,000 Experience
																																if(player.getInventory().countId(1329) < 2) {
																																	player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																																	return;
																																}
																																player.setBusy(true);
																																player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																																	world.getDelayedEventHandler().add(new ShortEvent(player) {
																																		public void action() {
																																			player.getInventory().remove(1329, 2);
																																			player.getActionSender().sendInventory();
																																			player.incExp(4, 10000, false, false);
																																			player.getActionSender().sendStat(4);
																																			npc.unblock();
																																			player.setBusy(false);
																																			return;
																																		}
																																	});
																																}
																																else if(option2 == 4 && option3 == 2) { // Ranged - 50,000 Experience
																																	if(player.getInventory().countId(1329) < 10) {
																																		player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																																		return;
																																	}
																																	player.setBusy(true);
																																	player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																																		world.getDelayedEventHandler().add(new ShortEvent(player) {
																																			public void action() {
																																				player.getInventory().remove(1329, 10);
																																				player.getActionSender().sendInventory();
																																				player.incExp(4, 50000, false, false);
																																				player.getActionSender().sendStat(4);
																																				npc.unblock();
																																				player.setBusy(false);
																																				return;
																																			}
																																		});
																																	}
																																	else if(option2 == 4 && option3 == 3) { // Ranged - 100,000 Experience
																																		if(player.getInventory().countId(1329) < 20) {
																																			player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																																			return;
																																		}
																																		player.setBusy(true);
																																		player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																																			world.getDelayedEventHandler().add(new ShortEvent(player) {
																																				public void action() {
																																					player.getInventory().remove(1329, 20);
																																					player.getActionSender().sendInventory();
																																					player.incExp(4, 100000, false, false);
																																					player.getActionSender().sendStat(4);
																																					npc.unblock();
																																					player.setBusy(false);
																																					return;
																																				}
																																			});
																																		}
																																		else if(option2 == 4 && option3 == 4) { // Ranged - 1,000,000 Experience
																																			if(player.getInventory().countId(1329) < 200) {
																																				player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																																				return;
																																			}
																																			player.setBusy(true);
																																			player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																																				world.getDelayedEventHandler().add(new ShortEvent(player) {
																																					public void action() {
																																						player.getInventory().remove(1329, 200);
																																						player.getActionSender().sendInventory();
																																						player.incExp(4, 1000000, false, false);
																																						player.getActionSender().sendStat(4);
																																						npc.unblock();
																																						player.setBusy(false);
																																						return;
																																					}
																																				});
																																			}
																																			else if(option2 == 5 && option3 == 0) { // Magic - 5,000 Experience
																																				if(player.getInventory().countId(1329) < 1) {
																																					player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																																					return;
																																				}
																																				player.setBusy(true);
																																				player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																																					world.getDelayedEventHandler().add(new ShortEvent(player) {
																																						public void action() {
																																							player.getInventory().remove(1329, 1);
																																							player.getActionSender().sendInventory();
																																							player.incExp(6, 5000, false, false);
																																							player.getActionSender().sendStat(6);
																																							npc.unblock();
																																							player.setBusy(false);
																																							return;
																																						}
																																					});
																																				}
																																				else if(option2 == 5 && option3 == 1) { // Magic - 10,000 Experience
																																					if(player.getInventory().countId(1329) < 2) {
																																						player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																																						return;
																																					}
																																					player.setBusy(true);
																																					player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																																						world.getDelayedEventHandler().add(new ShortEvent(player) {
																																							public void action() {
																																								player.getInventory().remove(1329, 2);
																																								player.getActionSender().sendInventory();
																																								player.incExp(6, 10000, false, false);
																																								player.getActionSender().sendStat(6);
																																								npc.unblock();
																																								player.setBusy(false);
																																								return;
																																							}
																																						});
																																					}
																																					else if(option2 == 5 && option3 == 2) { // Magic - 50,000 Experience
																																						if(player.getInventory().countId(1329) < 10) {
																																							player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																																							return;
																																						}
																																						player.setBusy(true);
																																						player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																																							world.getDelayedEventHandler().add(new ShortEvent(player) {
																																								public void action() {
																																									player.getInventory().remove(1329, 10);
																																									player.getActionSender().sendInventory();
																																									player.incExp(6, 50000, false, false);
																																									player.getActionSender().sendStat(6);
																																									npc.unblock();
																																									player.setBusy(false);
																																									return;
																																								}
																																							});
																																						}
																																						else if(option2 == 5 && option3 == 3) { // Magic - 100,000 Experience
																																							if(player.getInventory().countId(1329) < 20) {
																																								player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																																								return;
																																							}
																																							player.setBusy(true);
																																							player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																																								world.getDelayedEventHandler().add(new ShortEvent(player) {
																																									public void action() {
																																										player.getInventory().remove(1329, 20);
																																										player.getActionSender().sendInventory();
																																										player.incExp(6, 100000, false, false);
																																										player.getActionSender().sendStat(6);
																																										npc.unblock();
																																										player.setBusy(false);
																																										return;
																																									}
																																								});
																																							}
																																							else if(option2 == 5 && option3 == 4) { // Magic - 1,000,000 Experience
																																								if(player.getInventory().countId(1329) < 200) {
																																									player.informOfNpcMessage(new ChatMessage(npc, "You don't have enough experience tokens.", player));
																																									return;
																																								}
																																								player.setBusy(true);
																																								player.informOfNpcMessage(new ChatMessage(npc, "Thank you. Your experience has been successfully updated.", player));
																																									world.getDelayedEventHandler().add(new ShortEvent(player) {
																																										public void action() {
																																											player.getInventory().remove(1329, 200);
																																											player.getActionSender().sendInventory();
																																											player.incExp(6, 1000000, false, false);
																																											player.getActionSender().sendStat(6);
																																											npc.unblock();
																																											player.setBusy(false);
																																											return;
																																										}
																																									});
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
														npc.unblock();
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
      		}