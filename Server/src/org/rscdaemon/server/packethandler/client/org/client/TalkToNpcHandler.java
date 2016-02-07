package org.rscdaemon.server.packethandler.client;

import java.lang.reflect.InvocationTargetException;
import java.util.ConcurrentModificationException;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.event.WalkToMobEvent;
import org.rscdaemon.server.npchandler.NpcHandler;
import org.rscdaemon.server.util.Logger;
import org.rscdaemon.server.states.Action;
import org.apache.mina.common.IoSession;

public class TalkToNpcHandler implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player)session.getAttachment();
		if(player.isBusy()) {
			player.resetPath();
			return;
		}
		player.resetAll();
		final Npc affectedNpc = world.getNpc(p.readShort());
  		if(affectedNpc == null) { // This shouldn't happen
  			return;
  		}
  		player.setFollowing(affectedNpc);
  		player.setStatus(Action.TALKING_MOB);
  		world.getDelayedEventHandler().add(new WalkToMobEvent(player, affectedNpc, 1) {
			public void arrived() {
				owner.resetPath();
				if(owner.isBusy() || owner.isRanging() || !owner.nextTo(affectedNpc) || owner.getStatus() != Action.TALKING_MOB) {
					return;
				}
				owner.resetAll();
				if(affectedNpc.isBusy()) {
					owner.getActionSender().sendMessage(affectedNpc.getDef().getName() + " is currently busy.");
					return;
				}
				affectedNpc.resetPath();
				NpcHandler handler = world.getNpcHandler(affectedNpc.getID());
				int sprite = 0;

				sprite = getSprite(owner.getLocation().getX(), owner.getLocation().getY(), affectedNpc.getLocation().getX(), affectedNpc.getLocation().getY());
				if(sprite != -1)
					owner.setSprite(sprite);
			//	owner.setNeedsUpdate(true);

				// NPC sprite
				sprite = getSprite(affectedNpc.getLocation().getX(), affectedNpc.getLocation().getY(), owner.getLocation().getX(), owner.getLocation().getY());
				if(sprite != -1)
					affectedNpc.setSprite(sprite);

				affectedNpc.resetPath();
				
				if(world.npcScripts.containsKey(affectedNpc.getID())) {
					owner.setBusy(true);
					affectedNpc.blockedBy(owner);
					if(owner.interpreterThread != null) {
						try {
							owner.interpreterThread.stop();
						} catch(Exception e) {

						}
					}

					owner.interpreterThread = new Thread(new Runnable() {
						public void run() {		
							try {
								new org.rscdaemon.server.model.Script(owner, affectedNpc);	
								owner.setBusy(false);

								affectedNpc.unblock();
							} 
							catch(Exception e) {
								if(!(e instanceof InvocationTargetException)) {
									e.printStackTrace();
								}
								System.out.println(affectedNpc.getID());
								affectedNpc.unblock();
								owner.setBusy(false);
							}
						}
					});
					owner.interpreterThread.start();
				} 
				else				
		      		if(handler != null) {
		      			try {
		      				handler.handleNpc(affectedNpc, owner);
		      			}
		      			catch(Exception e) {
						Logger.error("Exception with npc[" + affectedNpc.getIndex() + "] from " + owner.getUsername() + " [" + owner.getCurrentIP() + "]: " + e.getMessage());
						owner.getActionSender().sendLogout();
						owner.destroy(false);
		      			}
		      		}  else{owner.getActionSender().sendMessage("The " + affectedNpc.getDef().getName() + " does not appear interested in talking to you.");}
			}
      	});
	}
	public static int getSprite(int x1, int y1, int x2, int y2) {
	
		int newx = x1 - x2;
		int newy = y1 - y2;

		if(newx == -1 && newy == -1)
		{
			//TURN SOUTHWEST
			return 3;
		} else
		if(newx == -1 && newy == 0)
		{
			//TURN WEST
			return 2;
		} else
		if(newx == -1 && newy == 1)
		{
			//TURN NORTHWEST
			return 1;
		} else
		if(newx == 0 && newy == 1)
		{
			//TURN NORTH
			return 0;
		} else
		if(newx == 1 && newy == 1)
		{
			//TURN NORTHEAST
			return 7;
		} else
		if(newx == 1 && newy == 0)
		{
			//TURN EAST
			return 6;
		} else
		if(newx == 1 && newy == -1)
		{
			//TURN SOUTHEAST
			return 5;
		} else
		if(newx == 0 && newy == -1)
		{
			//TURN SOUTH
			return 4;
		}

		return -1;
	}
}