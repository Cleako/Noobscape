package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class Kaqemeex implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
			if(player.getDruidicRitualStatus() == 3) {
				player.informOfNpcMessage(new ChatMessage(npc, "Sanfew informed me of your help. May you be blessed with the art of Herblaw!", player));
				player.setQuestPoints(player.getQuestPoints() +4);
				player.getActionSender().sendQuestPoints();
				player.isDruidicRitualComplete();
				player.getActionSender().sendDruidicRitualComplete();
				player.incExp(15, 250, false, false);
				player.getActionSender().sendStat(15);
				player.getActionSender().sendMessage("@gre@Congratulations! You have just completed the: @or1@Druidic Ritual @gre@quest!");
				player.getActionSender().sendMessage("@gre@You gained @or1@4 @gre@quest points!");
				player.getActionSender().sendMessage("@gre@You now have access to the Herblaw skill!");
				return;
			}	
			if(player.getDruidicRitualStatus() == 1) {
				player.informOfNpcMessage(new ChatMessage(npc, "Sanfew is located south of here in the town of Taverly", player));
				return;
			}
			if(player.getDruidicRitualStatus() == 4) {
				player.informOfNpcMessage(new ChatMessage(npc, "How's the herblaw coming along?", player));
				player.setBusy(true);
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						player.setBusy(false);
						String[] option = new String[]{"Fine thank you", "Not too good"};
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
												player.informOfNpcMessage(new ChatMessage(npc, "Practice makes perfect! Don't give it up!", player));
												return;
											}
											if(option == 0) {
												player.informOfNpcMessage(new ChatMessage(npc, "That's good to hear! My teachings paid off on you!", player));

										}
									}
								});
							}
						});
						player.getActionSender().sendMenu(option);
					}
				});
			} else if(player.getDruidicRitualStatus() == 0) {
      		player.informOfNpcMessage(new ChatMessage(npc, "Hello adventurer. How can I help you?", player));
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				String[] options = new String[]{"I am in search of a quest", "Nothing, sorry"};
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
									owner.informOfNpcMessage(new ChatMessage(npc, "As it happens, Sanfew is in need of a little help!", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
											owner.informOfNpcMessage(new ChatMessage(npc, "Head south into the town of Taverly.", owner));
											world.getDelayedEventHandler().add(new ShortEvent(owner) {
												public void action() {
												owner.informOfNpcMessage(new ChatMessage(npc, "Sanfew will be wandering around there.", owner));
												owner.setDruidicRitualStatus(1);
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