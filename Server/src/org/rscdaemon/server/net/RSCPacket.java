package org.rscdaemon.server.net;

import org.apache.mina.common.IoSession;

/**
 * An immutable packet object.
 */
public final class RSCPacket extends Packet {
	/**
	 * The ID of the packet
	 */
	private int pID;

	public RSCPacket(IoSession session, int pID, byte[] pData, boolean bare) {
		super(session, pData, bare);
		this.pID = pID;
	}

	public RSCPacket(IoSession session, int pID, byte[] pData) {
		this(session, pID, pData, false);
	}

	/**
	 * Returns the packet ID.
	 *
	 * @return The packet ID
	 */
	public int getID() {
		return pID;
	}

	/**
	 * Returns this packet in string form.
	 *
	 * @return A <code>String</code> representing this packet
	 */
	public String toString() {
		return super.toString() + " pid = " + pID;
	}
	
} 

