package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.apache.mina.common.IoSession;

public class ChatHandler implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player sender = (Player)session.getAttachment();
		if(sender.isMuted()) {
			sender.getActionSender().sendMessage("You are @red@MUTED@whi@. Nobody will see your messages.");
			return;
		}
		if(sender.getLocation().onTutorialIsland()) {
			sender.getActionSender().sendMessage("You can only communicate with other players in the mainland!");
			return;
		}
		sender.addMessageToChatQueue(p.getData());
	}
	
}