package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;
import org.rscdaemon.server.event.DelayedEvent;

public class Apothecary implements NpcHandler {

	public static final World world = World.getWorld();
	
	private static final String[] Alts = { "Can you make a strength potion?", "Can I have a some potions for free?", "Have you got any good potions to give away?" };
	
	public void handleNpc(final Npc npc, Player player) throws Exception {
	player.informOfNpcMessage(new ChatMessage(npc, "Hello there, I am the Apothecary, What can I do for you stranger?", player));
	player.setBusy(true);
	world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				owner.setMenuHandler(new MenuHandler(Alts) {
					public void handleReply(final int option, final String reply) {
						if(owner.isBusy() || option < 0 || option >= Alts.length) {
							npc.unblock();
							return;
						}
						
						
						owner.setBusy(true);
						world.getDelayedEventHandler().add(new ShortEvent(owner) {
							public void action() {
      								world.getDelayedEventHandler().add(new ShortEvent(owner) {
      									public void action() {
      										switch(option) {
      										case 0:
      										owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
      										owner.informOfNpcMessage(new ChatMessage(npc, "Yes. For a small cost", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
												InvItem redEggs = owner.getInventory().get(owner.getInventory().getLastIndexById(219));
												InvItem limps = owner.getInventory().get(owner.getInventory().getLastIndexById(220));
												if(redEggs == null || limps == null || owner.getInventory().countId(10) < 5) {
													owner.informOfNpcMessage(new ChatMessage(npc, "But you don't have the right ingredients.", owner));
													running = false;
													owner.setBusy(false);
													npc.unblock();
												}
												else if(redEggs != null && limps != null && owner.getInventory().countId(10) >= 5) {
													owner.getInventory().remove(219, 1);
													owner.getInventory().remove(10, 300);
													owner.getInventory().remove(220, 1);
													owner.getActionSender().sendMessage("@whi@You hand the ingredients over to the Apothecary");
													owner.getInventory().add(new InvItem(221, 1));
													owner.getActionSender().sendInventory();
													owner.getActionSender().sendMessage("@whi@You recive a strength potion.");
													owner.setBusy(false);
													npc.unblock();
												}
												else {
													running = false;
													owner.setBusy(false);
													npc.unblock();
												}
											}
										});
										npc.unblock();
										break;
										case 1:
										owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
										owner.informOfNpcMessage(new ChatMessage(npc, "Uh... no....", owner));
										owner.setBusy(false);
										npc.unblock();
										break;
										case 2:
										owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
										owner.informOfNpcMessage(new ChatMessage(npc, "Yes, try this potion.", owner));
										owner.getInventory().add(new InvItem(58, 1));
										owner.getActionSender().sendInventory();
										owner.setBusy(false);
										npc.unblock();
										break;
      										}
      									}
      								});
							}
						});
					}
				});
				owner.getActionSender().sendMenu(Alts);
      			}
      		});
      		npc.blockedBy(player);
	}
	
}