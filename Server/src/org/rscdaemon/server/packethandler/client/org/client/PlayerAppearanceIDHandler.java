package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.net.Packet;
import org.apache.mina.common.IoSession;

public class PlayerAppearanceIDHandler implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		int mobCount = p.readShort();
		int[] indicies = new int[mobCount];
		int[] appearanceIDs = new int[mobCount];
		for (int x = 0; x < mobCount; x++) {
			indicies[x] = p.readShort();
			appearanceIDs[x] = p.readShort();
		}
		Player player = (Player) session.getAttachment();
		player.addPlayersAppearanceIDs(indicies, appearanceIDs);
	}

}
