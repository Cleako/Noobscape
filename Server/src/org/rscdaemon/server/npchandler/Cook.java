package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class Cook implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
			if(player.getCooksAssistantStatus() == 2) {
				player.informOfNpcMessage(new ChatMessage(npc, "Thank you. The Duke really enjoyed his birthday cake!", player));
				return;
			}
			if(player.getCooksAssistantStatus() == 1) {
				player.informOfNpcMessage(new ChatMessage(npc, "Have you got my ingredients?", player));
				player.setBusy(true);
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						player.setBusy(false);
						String[] option = new String[]{"Yes, I have them here", "No, sorry. I am still looking for them"};
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
												player.informOfNpcMessage(new ChatMessage(npc, "Ok. Come back when you have found the cake ingredients!", player));
												return;
											}
											if(option == 0 && player.getInventory().countId(19) > 0 && player.getInventory().countId(22) > 0 && player.getInventory().countId(136) > 0) {
													player.informOfNpcMessage(new ChatMessage(npc, "Thank you! You saved the Duke's birthday!", player));
													player.getInventory().remove(19, 1);
													player.getInventory().remove(22, 1);
													player.getInventory().remove(136, 1);
													player.getActionSender().sendInventory();
													player.isCooksAssistantComplete();
													player.setQuestPoints(player.getQuestPoints() +1);
													player.getActionSender().sendQuestPoints();
													player.getActionSender().sendCooksAssistantComplete();
													player.incExp(7, 250, false, false);
													player.getActionSender().sendStat(7);
													player.getActionSender().sendMessage("@gre@Congratulations! You have just completed the: @or1@Cook's Assistant @gre@quest!");
													player.getActionSender().sendMessage("@gre@You gained @or1@1 @gre@quest point!");
													player.getActionSender().sendMessage("@gre@You now have access to the @or1@Cook's Range@gre@!");
											} else
													player.informOfNpcMessage(new ChatMessage(npc, "It appears you don't have my ingredients. This is important!", player));
									}
								});
							}
						});
						player.getActionSender().sendMenu(option);
					}
				});
			} else if(player.getCooksAssistantStatus() == 0) {
      		player.informOfNpcMessage(new ChatMessage(npc, "Hello friend, how is the adventuring going?", player));
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				String[] options = new String[]{"I am getting strong and mighty", "I keep on dying", "Nice hat", "Can I use your range?"};
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
									owner.informOfNpcMessage(new ChatMessage(npc, "Glad to hear it", owner));
									return;
								}
								if(option == 1) {
									owner.informOfNpcMessage(new ChatMessage(npc, "Ah well at least you keep coming back to life!", owner));
									return;
								}
								if(option == 2) {
									owner.informOfNpcMessage(new ChatMessage(npc, "Err thank you - it's a pretty ordinary cooks hat really", owner));
									return;
								}
								if(option == 3) {
									owner.setCooksAssistantStatus(1);
									owner.informOfNpcMessage(new ChatMessage(npc, "Sure, but first I need you to do a favour for me. It's the Duke's birthday", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
											owner.informOfNpcMessage(new ChatMessage(npc, "but I seem to have lost some ingredients to make him a cake!", owner));
											world.getDelayedEventHandler().add(new ShortEvent(owner) {
												public void action() {
												owner.informOfNpcMessage(new ChatMessage(npc, "Bring me an egg, some milk and a pot of flour so I can finish the cake!", owner));
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