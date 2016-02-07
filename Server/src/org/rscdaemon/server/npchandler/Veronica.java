package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.ShortEvent;

public class Veronica implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	public void handleNpc(final Npc npc, final Player player) throws Exception {
		if(player.getErnestTheChickenStatus() == 1) {
			player.informOfNpcMessage(new ChatMessage(npc, "Please go and find Ernest!", player));
			return;
		}
		if(player.getErnestTheChickenStatus() == 4) {
			player.informOfNpcMessage(new ChatMessage(npc, "Thank you for saving my husband Ernest!", player));
			return;
		}
		if(player.getErnestTheChickenStatus() == 2 || player.getErnestTheChickenStatus() == 3) {
			player.informOfChatMessage(new ChatMessage(player, "Ernest is a chicken. An experiment went wrong!", npc));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					player.informOfNpcMessage(new ChatMessage(npc, "Please do something!", player));
					return;
				}
			});
		}
		if(player.getErnestTheChickenStatus() == 0) {
			player.informOfNpcMessage(new ChatMessage(npc, "Oh please help me! I have lost Ernest! Please help me find him!", player));
			world.getDelayedEventHandler().add(new ShortEvent(player) {
				public void action() {
					player.informOfChatMessage(new ChatMessage(player, "Who is Ernest?", npc));
					world.getDelayedEventHandler().add(new ShortEvent(player) {
						public void action() {
							player.informOfNpcMessage(new ChatMessage(npc, "Ernest is my husabnd! He is missing! Please go find him!", player));
							player.setErnestTheChickenStatus(1);
							npc.unblock();
						}
					});
				}
			});
			npc.blockedBy(player);
		}
	}
}