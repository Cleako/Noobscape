package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class MonkHealer implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handleNpc(final Npc npc, Player player) throws Exception {
      		player.informOfNpcMessage(new ChatMessage(npc, "Greetings traveller", player));
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				String[] options = new String[]{"Can you heal me? I'm injured"};
				owner.setMenuHandler(new MenuHandler(options) {
					public void handleReply(final int option, final String reply) {
						if(owner.isBusy()) {
							return;
						}
						owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
						owner.setBusy(true);
						world.getDelayedEventHandler().add(new ShortEvent(owner) {
							public void action() {
								if(option == 0) {
      									owner.informOfNpcMessage(new ChatMessage(npc, "Ok", owner));
      									owner.getActionSender().sendMessage("The monk places his hands on your head");
      									world.getDelayedEventHandler().add(new ShortEvent(owner) {
      										public void action() {
      											owner.setBusy(false);
      											owner.getActionSender().sendMessage("You feel a little better");
											int newHp = owner.getCurStat(3) + 10;
											if(newHp > owner.getMaxStat(3)) {
												newHp = owner.getMaxStat(3);
											}
										      	owner.setCurStat(3, newHp);
										      	owner.getActionSender().sendStat(3);
      											npc.unblock();
      										}
      									});
								}
								else {
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