package org.rscdaemon.server.net;

import org.rscdaemon.server.GameEngine;
import org.rscdaemon.server.codec.RSCCodecFactory;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.net.PacketQueue;
import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.util.Logger;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;

import java.net.InetSocketAddress;

/**
 * Handles the protocol events fired from MINA.
 */
public class RSCConnectionHandler implements IoHandler {
	/**
	 * A reference to the game engine's packet queue
	 */
	private PacketQueue<RSCPacket> packets;

	/**
	 * Creates a new connection handler for the given engine.
	 *
	 * @param engine The engine in use
	 */
	public RSCConnectionHandler(GameEngine engine) {
		packets = (PacketQueue<RSCPacket>)engine.getPacketQueue();
	}

	/**
	 * Invoked whenever an exception is thrown by MINA or this IoHandler.
	 *
	 * @param session The associated session
	 * @param cause   The exception thrown
	 */
	public void exceptionCaught(IoSession session, Throwable cause) {
	}

	/**
	 * Invoked whenever a packet is ready to be added to the queue.
	 *
	 * @param session The IO session on which the packet was received
	 * @param message The packet
	 */
	public void messageReceived(IoSession session, Object message) {
		Player player = (Player)session.getAttachment();
		if(session.isClosing() || player.destroyed()) {
			return;
		}
		RSCPacket p = (RSCPacket) message;
		player.addPacket(p);
		packets.add(p);
	}

	/**
	 * Invoked whenever a packet is sent.
	 *
	 * @param session The associated session
	 * @param message The packet sent
	 */
	public void messageSent(IoSession session, Object message) {
	}

	/**
	 * Invoked whenever an IO session is closed. This must handle unregistering
	 * the disconnecting player from the engine.
	 *
	 * @param session The IO session which has been closed
	 */
	public void sessionClosed(IoSession session) {
		Player player = (Player)session.getAttachment();
		if(!player.destroyed()) {
			player.destroy(false);
		}
	}

	/**
	 * Invoked whenever an IO session is created.
	 *
	 * @param session The session opened
	 */
	public void sessionCreated(IoSession session) {
		session.getFilterChain().addFirst("protocolFilter", new ProtocolCodecFilter(new RSCCodecFactory()));
		Logger.connection("Connection from: " + ((InetSocketAddress)session.getRemoteAddress()).getAddress().getHostAddress());
	}

	/**
	 * Invoked when the idle status of a session changes.
	 *
	 * @param session The session in question
	 * @param status  The new idle status
	 */
	public void sessionIdle(IoSession session, IdleStatus status) {
		Player player = (Player)session.getAttachment();
		if(!player.destroyed()) {
			player.destroy(false);
		}
	}

	/**
	 * Invoked when a new session is opened.
	 *
	 * @param session The session opened
	 */
	public void sessionOpened(IoSession session) {
		session.setAttachment(new Player(session));
		session.setIdleTime(IdleStatus.BOTH_IDLE, 30);
		session.setWriteTimeout(30);
	}
}
