package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.net.Packet;
import org.apache.mina.common.IoSession;

public class Ping implements PacketHandler {
	public void handlePacket(Packet p, IoSession session) throws Exception {
		// do nothing, simply receiving the packet triggers a ping
	}
}
