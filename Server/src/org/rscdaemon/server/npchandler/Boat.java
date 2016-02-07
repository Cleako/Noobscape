package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.model.Point;
import org.rscdaemon.server.event.ShortEvent;

public class Boat implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	
	private static final String[] destinationNames = {
		"Karamja", "Brimhaven", "Port Sarim", "Ardougne",
		"Port Khazard", "Catherby", "Shilo"
	};
	private static final Point[] destinationCoords = {
		Point.location(324, 713), Point.location(467, 649), Point.location(268, 650), Point.location(538, 616),
		Point.location(541, 702), Point.location(439, 506), Point.location(471, 853)
	};

	public void handleNpc(final Npc npc, Player player) throws Exception {
      		player.informOfNpcMessage(new ChatMessage(npc, "G'day sailor, where would you like to go?", player));
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				owner.setMenuHandler(new MenuHandler(destinationNames) {
					public void handleReply(final int option, final String reply) {
						if(owner.isBusy() || option < 0 || option >= destinationNames.length) {
							npc.unblock();
							return;
						}
						owner.informOfChatMessage(new ChatMessage(owner, reply + " please", npc));
						owner.setBusy(true);
						world.getDelayedEventHandler().add(new ShortEvent(owner) {
							public void action() {
      								owner.getActionSender().sendMessage("You board the ship");
      								world.getDelayedEventHandler().add(new ShortEvent(owner) {
      									public void action() {
      										Point p = destinationCoords[option];
      										owner.teleport(p.getX(), p.getY(), false);
      										owner.getActionSender().sendMessage("The ship arrives at " + reply);
										owner.setBusy(false);
      										npc.unblock();
      									}
      								});
							}
						});
					}
				});
				owner.getActionSender().sendMenu(destinationNames);
      			}
      		});
      		npc.blockedBy(player);
	}
	
}