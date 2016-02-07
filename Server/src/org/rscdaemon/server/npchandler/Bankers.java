package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.event.ShortEvent;
import org.rscdaemon.server.model.MenuHandler;

public class Bankers implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handleNpc(final Npc npc, Player player) throws Exception {
		player.setBusy(true);
      		player.informOfNpcMessage(new ChatMessage(npc, "Good day, how may I help you?", player));
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
				owner.setBusy(false);
      				String[] options = new String[]{"I'd like to access my bank account please.", "What is this place?"};
      				final String[] options2 = new String[]{"And what do you do?", "Didn't you used to be called the bank of Varrock"};
      				owner.getActionSender().sendMenu(options);
      				owner.setMenuHandler(new MenuHandler(options) {
      					public void handleReply(final int option, final String reply) {
      					/*	if(owner.isBusy()) {
      							return;
      						}*/
      						owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
      						world.getDelayedEventHandler().add(new ShortEvent(owner) {
      						public void action() {
								if(option==0){
									owner.setBusy(true);
									owner.informOfNpcMessage(new ChatMessage(npc, "Certainly "+(owner.isMale() ? "sir" : "miss"), owner));
									world.getDelayedEventHandler().add(new ShortEvent(owner) {
      								public void action() {
								owner.setBusy(false);
								owner.setAccessingBank(true);
								owner.getActionSender().showBank();
							//	npc.unblock();
						}
					});
							}else{
							owner.setBusy(true);
								world.getDelayedEventHandler().add(new ShortEvent(owner) {
      								public void action() {
										owner.informOfNpcMessage(new ChatMessage(npc, "This is a branch of the bank of runescape", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
      										public void action() {
										owner.informOfNpcMessage(new ChatMessage(npc, "We have branches in many towns", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
      										public void action() {
										owner.getActionSender().sendMenu(options2);
									}
								});
							}
						});
										owner.setMenuHandler(new MenuHandler(options2) {
      										public void handleReply(final int option2, final String reply) {
												owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
												world.getDelayedEventHandler().add(new ShortEvent(owner) {
      												public void action() {
														if(option2==0){
														owner.informOfNpcMessage(new ChatMessage(npc, "We will look after your items and money for you", owner));
														world.getDelayedEventHandler().add(new ShortEvent(owner) {
      														public void action() {
																owner.informOfNpcMessage(new ChatMessage(npc, "So leave your valuables with us if you want to keep them safe", owner));
															//	npc.unblock();
																owner.setBusy(false);
															}
														});
													}else{
													owner.setBusy(true);
														owner.informOfNpcMessage(new ChatMessage(npc, "Yes we did, but people kept on coming into our branches outside of Varrock", owner));
														world.getDelayedEventHandler().add(new ShortEvent(owner) {
      														public void action() {
																owner.informOfNpcMessage(new ChatMessage(npc, "And telling us our signs were wrong", owner));
																world.getDelayedEventHandler().add(new ShortEvent(owner) {
      																public void action() {
																		owner.informOfNpcMessage(new ChatMessage(npc, "As if we didn't know what town we were in or something!", owner));
																//		npc.unblock();
																		owner.setBusy(false);
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
							}
							npc.unblock();
							owner.setBusy(false);
						}
      					});
      				}
      			});
      		}
	});
npc.blockedBy(player);
}

}