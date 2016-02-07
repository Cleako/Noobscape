package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.event.Thieving;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.Mob;
import org.apache.mina.common.IoSession;

public class NpcCommand implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		int serverIndex = p.readShort();
		final Player player = (Player) session.getAttachment();
		if(player.isBusy()){return;}
		final Mob affectedMob = world.getNpc(serverIndex);
		final Npc affectedNpc = (Npc)affectedMob;
		if(affectedNpc == null || affectedMob == null || player == null)
			return;
		affectedNpc.getID();
		Thieving thiev = new Thieving(player, affectedNpc, affectedMob);
		thiev.beginPickpocket();
		return;
		
	}
		
}