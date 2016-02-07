package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class Sanfew implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
			if(player.getDruidicRitualStatus() == 3) {
				player.informOfNpcMessage(new ChatMessage(npc, "Go speak to Kaqemeex to gain the ability of Herblaw", player));
				return;
			}
			if(player.getDruidicRitualStatus() == 2) {
				player.informOfNpcMessage(new ChatMessage(npc, "Have you got the 4 pieces of enchanted meat yet?", player));
				player.setBusy(true);
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						player.setBusy(false);
						String[] option = new String[]{"Yes, I have them right here", "No sorry. I am still searching for them"};
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
												player.informOfNpcMessage(new ChatMessage(npc, "Ok, come back when you have them!", player));
												return;
											}
											if(option == 0 && player.getInventory().countId(505) > 0 && player.getInventory().countId(506) > 0 && player.getInventory().countId(507) > 0 && player.getInventory().countId(508) > 0) {
												player.informOfNpcMessage(new ChatMessage(npc, "Ah, thank you! Kaqemeex will now teach you the art of Herblaw", player));
												player.setDruidicRitualStatus(3);
												player.getInventory().remove(505, 1);
												player.getInventory().remove(506, 1);
												player.getInventory().remove(507, 1);
												player.getInventory().remove(508, 1);
												player.getActionSender().sendInventory();

										} else
											player.informOfNpcMessage(new ChatMessage(npc, "You don't have the enchanted meats!", player));
									}
								});
							}
						});
						player.getActionSender().sendMenu(option);
					}
				});
			} else if(player.getDruidicRitualStatus() == 1) {
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
									owner.informOfNpcMessage(new ChatMessage(npc, "Ah! Then I am the person to speak to! I need 4 types of raw meat", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
											owner.informOfNpcMessage(new ChatMessage(npc, "I need the meat of a Rat, Chicken, Bear and a Cow. It also must,", owner));
											world.getDelayedEventHandler().add(new ShortEvent(owner) {
												public void action() {
												owner.informOfNpcMessage(new ChatMessage(npc, "be dipped into the Cauldron of Thunder to enchant it! Good luck on your quest!", owner));
												owner.setDruidicRitualStatus(2);
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