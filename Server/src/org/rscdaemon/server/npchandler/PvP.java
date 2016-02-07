package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.model.Point;
import org.rscdaemon.server.event.ShortEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class PvP implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	private static final String[] options = {
		"Sure", "No thanks."
	};

	public void handleNpc(final Npc npc, Player player) throws Exception {
			if(!world.getPvpEntry(player)){
      			player.informOfNpcMessage(new ChatMessage(npc, "Would you like to be entered into the PvP tournament?", player));
			} else {
				player.informOfNpcMessage(new ChatMessage(npc, "Would you like to be removed from the PvP tournament?", player));
			}
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				owner.setMenuHandler(new MenuHandler(options) {
					public void handleReply(final int option, final String reply) {
						if(owner.isBusy() || option < 0) {
							npc.unblock();
							return;
						}
						owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
						owner.setBusy(true);
						world.getDelayedEventHandler().add(new ShortEvent(owner) {
							public void action() {
								if(world.getServer().pvpIsRunning()){
									owner.informOfNpcMessage(new ChatMessage(npc, "Sorry, this event has already started. Please wait for it to finish.", owner));
									owner.setBusy(false);
									npc.unblock();
									return;
								}
								if(option==0 && !world.getPvpEntry(owner)){
								if(owner.getInventory().countId(10) < 50000) {
													owner.informOfChatMessage(new ChatMessage(owner, "I'll just go get my cash to pay for the entry fee.", npc));
													owner.setBusy(false);
													npc.unblock();
												}
									else if(owner.getInventory().remove(10, 50000) > -1){
										world.setJackPot(world.getJackPot()+50000);
										world.addPvpEntry(owner);
										owner.setBusy(false);
										npc.unblock();
										owner.getActionSender().sendInventory();
										owner.teleport(589, 665, false);
										if(world.getPvpSize()==2){
											for(Player p : world.getPlayers()) {
												p.getActionSender().sendMessage("The PvP tournament will be starting in 2 minutes.");
												p.getActionSender().startPvp(120);
												
											}
											world.getServer().pvpTimerStart(126);
										}
										else if(world.getPvpSize()>2 && world.getServer().waitingIsRunning()){
											int timeTillPvp = world.getServer().timeTillPvp();
											if(timeTillPvp > -1) {
												owner.getActionSender().startPvp((int)(timeTillPvp / 1000));
											}
										}
									}else{
										owner.getActionSender().sendMessage("You do not have sufficient funds.");
										owner.setBusy(false);
										npc.unblock();}
								}
								else if(option==0 && world.getPvpEntry(owner)){
									if(world.getPvpSize()==2){
										for(Player p : world.getPlayers()) {
											p.getActionSender().sendMessage("The PvP tournament has been delayed due to a lack of players.");
											p.getActionSender().startPvp(0);
										}
										world.getServer().stopPvp();
									}
									owner.getActionSender().startPvp(0);
									owner.teleport(220, 445, false);
									world.removePvpEntry(owner);
									owner.setBusy(false);
									npc.unblock();
								}
								else{owner.setBusy(false);npc.unblock();}
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