package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class Romeo implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
			if(player.getRomeoJulietStatus() == 3) {
				player.informOfNpcMessage(new ChatMessage(npc, "Thanks again for all your help!", player));
				return;
			}
			if(player.getRomeoJulietStatus() == 1) {
				player.informOfNpcMessage(new ChatMessage(npc, "Please come back after you've spoke to my Juliet!", player));
				return;
			}
			if(player.getRomeoJulietStatus() == 2) {
				player.informOfNpcMessage(new ChatMessage(npc, "Did you manage to find my beloved Juliet yet?", player));
				player.setBusy(true);
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						player.setBusy(false);
						String[] option = new String[]{"Yes, she's with her father", "No, sorry. I am still tracking her down"};
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
												player.informOfNpcMessage(new ChatMessage(npc, "Keep looking! I am very worried about her!", player));
												return;
											}
											if(option == 0 && player.getRomeoJulietStatus() == 2) {
												player.informOfNpcMessage(new ChatMessage(npc, "As long as she's safe. Thanks for helping me", player));
												player.isRomeoJulietComplete();
												player.setQuestPoints(player.getQuestPoints() +5);
												player.getActionSender().sendQuestPoints();
												player.getActionSender().sendRomeoJulietComplete();
												player.getActionSender().sendMessage("@gre@Congratulations! You have just completed the: @or1@Romeo & Juliet @gre@quest!");
												player.getActionSender().sendMessage("@gre@You gained @or1@5 @gre@quest points!");
											} else
													player.informOfNpcMessage(new ChatMessage(npc, "Stop wasting time and go find my Juliet!", player));
									}
								});
							}
						});
						player.getActionSender().sendMenu(option);
					}
				});
			} else if(player.getRomeoJulietStatus() == 0) {
      		player.informOfNpcMessage(new ChatMessage(npc, "Juliet, Oh Juliet, Wherefore are thou?", player));
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				String[] options = new String[]{"What seems to be the problem?", "I have other things to do, sorry"};
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
									owner.informOfNpcMessage(new ChatMessage(npc, "I have lost my ways and my wife", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
											owner.informOfNpcMessage(new ChatMessage(npc, "Please help me find her!", owner));
											owner.setRomeoJulietStatus(1);
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