package org.rscdaemon.server.clansystem;

import org.apache.mina.common.IoSession;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.util.Logger;
import org.rscdaemon.server.packethandler.client.CommandHandler;


public class ClanChat implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player) session.getAttachment();
		int sh = p.readShort();
		Player affectedPlayer = null;
			String[] args = new String[0];
		if (sh != 1313) {
			affectedPlayer = world.getPlayer(sh);
			if (affectedPlayer == null) {
				player.setSuspiciousPlayer(true);
				return;
			}
		}
		byte type = p.readByte();
			if (type == 1) { // clan chat!
			String cmd = p.readString();
			if (cmd.substring(0, 1).equals("/")) {
				String meh = cmd.substring(1, cmd.length());
				if(!player.hasClan()) {
				player.getActionSender().sendMessage("You are not in a clan!");
			} 
			else {
				player.getClan().globalMessage(player, meh);
			}
		}
		}
	}

}
