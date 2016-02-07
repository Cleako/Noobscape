package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class WizardMizgog implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
			if(player.getImpCatcherStatus() == 2) {
				player.informOfNpcMessage(new ChatMessage(npc, "Thank you. You taught those imps a lesson or two!", player));
				return;
			}
			if(player.getImpCatcherStatus() == 1) {
				player.informOfNpcMessage(new ChatMessage(npc, "Have you found all my beads yet?", player));
				player.setBusy(true);
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						player.setBusy(false);
						String[] option = new String[]{"Yes, I have them here", "No, sorry. I am still trying to get them"};
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
											if(option == 0 && player.getInventory().countId(231) >= 1 && player.getInventory().countId(232) >= 1 && player.getInventory().countId(233) >= 1 && player.getInventory().countId(234) >= 1) {
												player.informOfNpcMessage(new ChatMessage(npc, "Thank you! Please take this as a reward for your help!", player));
													owner.getInventory().remove(231, 1);
													owner.getInventory().remove(232, 1);
													owner.getInventory().remove(233, 1);
													owner.getInventory().remove(234, 1);
													owner.getInventory().add(new InvItem(235, 1)); // Amulet of Accuracy
													owner.getActionSender().sendInventory();
													owner.incExp(6, 875, false, false);
													owner.getActionSender().sendStat(6);
													owner.isImpCatcherComplete();
													owner.setQuestPoints(owner.getQuestPoints() +1);
													owner.getActionSender().sendQuestPoints();
													owner.getActionSender().sendImpCatcherComplete();
													owner.getActionSender().sendMessage("@gre@Congratulations! You have just completed the: @or1@Imp Catcher @gre@quest!");
													owner.getActionSender().sendMessage("@gre@You gained @or1@1 @gre@quest points!");
													owner.getActionSender().sendMessage("@gre@You are rewarded with: @or1@Amulet of Accuracy");
											} else
													player.informOfNpcMessage(new ChatMessage(npc, "It appears you don't have all my beads yet!", player));
									}
								});
							}
						});
						player.getActionSender().sendMenu(option);
					}
				});
			} else if(player.getImpCatcherStatus() == 0) {
      		player.informOfNpcMessage(new ChatMessage(npc, "The imps have stolen my beads! Please find and return them!", player));
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				String[] options = new String[]{"I will get them. What color are they?", "I'm too busy, sorry"};
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
									owner.informOfNpcMessage(new ChatMessage(npc, "Thank you. I really appreciate you taking the time to help me!", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
											owner.informOfNpcMessage(new ChatMessage(npc, "They have stolen 4 beads in total,", owner));
											world.getDelayedEventHandler().add(new ShortEvent(owner) {
												public void action() {
												owner.informOfNpcMessage(new ChatMessage(npc, "The colors are: red, white, yellow and black!", owner));
												owner.setImpCatcherStatus(1);
												npc.unblock();
												}
											});
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