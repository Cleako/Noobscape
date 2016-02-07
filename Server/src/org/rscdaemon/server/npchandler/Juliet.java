package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class Juliet implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
			if(player.getRomeoJulietStatus() == 3) {
				player.informOfNpcMessage(new ChatMessage(npc, "Thanks again!", player));
				return;
			}
			if(player.getRomeoJulietStatus() == 0) {
				player.informOfNpcMessage(new ChatMessage(npc, "Romeo!? Wherefore are thou Romeo?", player));
				return;
			}
			if(player.getRomeoJulietStatus() == 2) {
				player.informOfNpcMessage(new ChatMessage(npc, "Did you go and tell Romeo I am fine and with my father?", player));
				player.setBusy(true);
				world.getDelayedEventHandler().add(new ShortEvent(player) {
					public void action() {
						player.setBusy(false);
						String[] option = new String[]{"Yes, I have told him", "No, not yet"};
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
												player.informOfNpcMessage(new ChatMessage(npc, "Please tell him and put his mind at ease", player));
												return;
											}
											if(option == 0 && player.getRomeoJulietStatus() == 2) {
												player.informOfNpcMessage(new ChatMessage(npc, "It doesn't seem you have told him yet! Go tell him please!", player));
											} else
													player.informOfNpcMessage(new ChatMessage(npc, "Thank you! You are very kind!", player));
									}
								});
							}
						});
						player.getActionSender().sendMenu(option);
					}
				});
			} else if(player.getRomeoJulietStatus() == 1) {
      		player.informOfNpcMessage(new ChatMessage(npc, "Romeo!? Wherefore are thou Romeo?", player));
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				String[] options = new String[]{"I know where he is", "I have no idea, sorry"};
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
									owner.informOfNpcMessage(new ChatMessage(npc, "Please tell me of his whereabouts!", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
											owner.informOfChatMessage(new ChatMessage(owner, "He's in Varrock. He's worried about you!", npc));
											world.getDelayedEventHandler().add(new ShortEvent(owner) {
												public void action() {
													owner.informOfNpcMessage(new ChatMessage(npc, "Please tell him i'm with my father and i'm fine", owner));
													owner.setRomeoJulietStatus(2);
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