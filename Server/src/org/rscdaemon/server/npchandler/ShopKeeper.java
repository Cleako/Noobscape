package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.model.Shop;
import org.rscdaemon.server.event.ShortEvent;

public class ShopKeeper implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handleNpc(final Npc npc, Player player) throws Exception {
		final Shop shop = world.getShop(npc.getLocation());
		if(shop == null) {
			return;
		}
		try {
		
		if(shop.getGreeting() != null) {
			
			if(shop.getGreeting().equals("Good day monsieur, Would you like ze nice freshly baked bread?")) {
				if(player.npcThief[0] == true) {
					player.informOfNpcMessage(new ChatMessage(npc, org.rscdaemon.server.event.Thieving.StealChats[org.rscdaemon.server.event.Thieving.Rands(org.rscdaemon.server.event.Thieving.StealChats.length)], player));
					return;
				}
			}
			if(player.npcThief[3] == true) {
				if(shop.getGreeting().equals("Silver! Silver!")) {
					player.informOfNpcMessage(new ChatMessage(npc, org.rscdaemon.server.event.Thieving.StealChats[org.rscdaemon.server.event.Thieving.Rands(org.rscdaemon.server.event.Thieving.StealChats.length)], player));
					return;
				}
			}
			if(player.npcThief[4] == true) {
				if(shop.getGreeting().equals("Get your exotic spices here, rare very valuable spices here")) {
					player.informOfNpcMessage(new ChatMessage(npc, org.rscdaemon.server.event.Thieving.StealChats[org.rscdaemon.server.event.Thieving.Rands(org.rscdaemon.server.event.Thieving.StealChats.length)], player));
					return;
				}
			}
			if(player.npcThief[5] == true) {
				if(shop.getGreeting().equals("Here, look at my lovely gems")) {
					player.informOfNpcMessage(new ChatMessage(npc, org.rscdaemon.server.event.Thieving.StealChats[org.rscdaemon.server.event.Thieving.Rands(org.rscdaemon.server.event.Thieving.StealChats.length)], player));
					return;
				}			
			}
      			player.informOfNpcMessage(new ChatMessage(npc, shop.getGreeting(), player));
      		}
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
      				owner.setMenuHandler(new MenuHandler(shop.getOptions()) {
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
      									owner.setAccessingShop(shop);
      									owner.getActionSender().showShop(shop);
      								}
      								npc.unblock();
      							}
      						});
      					}
      				});
      				owner.getActionSender().sendMenu(shop.getOptions());
      			}
      		});
      		npc.blockedBy(player);
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
		} } 
	
}