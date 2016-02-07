package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.net.RSCPacket;

import org.apache.mina.common.IoSession;

public class FriendHandler implements PacketHandler {
	/**
	 * World Instance
	 */
	public static final World world = World.getWorld();
	
	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player)session.getAttachment();
		int pID = ((RSCPacket)p).getID();
		
		player.getUsernameHash();
		long f = p.readLong();
		boolean isOnline = world.getPlayers().contains(world.getPlayer(f));
		
		String friend = DataConversions.hashToUsername(f);
		
		switch(pID) {
			case 168: // Add Friend
				if(player.friendCount() >= 50) {
					player.getActionSender().sendMessage("Your friend list is full!");
					return;
				}
				for(int i = 0; i < player.friendCount(); i++) {
					String s = (String) player.getFriendList().get(i);
					if(s.equals(friend)) {
						player.getActionSender().sendMessage("This person is already in your friends list!");
						return;
					}
				}
				if(isOnline) {
					player.getActionSender().sendFriendUpdate(f, org.rscdaemon.server.util.Config.SERVER_NUM);
				} else {
					player.getActionSender().sendFriendUpdate(f, 0);
				}
				player.addFriend(friend);
				break;
			case 52: // Remove Friend
				player.removeFriend(friend);
				break;
			case 25: // Add Ignore
				if(player.ignoreCount() >= 50) {
					player.getActionSender().sendMessage("Your ignore list is full!");
					return;
				}
				player.addIgnore(friend);
				break;
			case 108: // Remove Ignore
				player.removeIgnore(friend);
				break;
			case 254: // Send Private Message
				Player pe = world.getPlayer(f);
				byte[] remaining = p.getRemainingData();
				if(player.getLocation().onTutorialIsland()) {
					player.getActionSender().sendMessage("You can only communicate with other players in the mainland!");
				} else if((player.isMuted()) && (!pe.isStaff())) {
					player.getActionSender().sendMessage("You can't communicate with other players while you're muted!");
				} else if((pe.isMuted()) && (!player.isStaff())) {
					player.getActionSender().sendMessage("You can't communicate with a muted player!");
				} else {
					if((player.getFriendList().contains(friend)) && (!player.getIgnoreList().contains(friend)) && (isOnline)) {
						pe.getActionSender().sendPrivateMessage(player.getUsernameHash(), remaining);
					} else {
						player.getActionSender().sendMessage("That player is not online!");
					}
					break;
				}
			}
		}
	}