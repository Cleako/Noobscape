package org.rscdaemon.server.codec;

import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.util.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Encodes the high level <code>RSCPacket</code> class into the proper
 * protocol data required for transmission.
 */
public class RSCProtocolEncoder implements ProtocolEncoder {
	/**
	 * Converts a <code>RSCPacket</code> object into the raw data needed
	 * for transmission.
	 *
	 * @param session The IO session associated with the packet
	 * @param message A <code>RSCPacket</code> to encode
	 * @param out	 The output stream to which to write the data
	 */
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) {
		if(!(message instanceof RSCPacket)) {
			Logger.error(new Exception("Wrong packet type! " + message.toString()));
			return;
		}
		RSCPacket p = (RSCPacket)message;
		byte[] data = p.getData();
		int packetLength = data.length;
		int dataLength = data.length;
		ByteBuffer buffer;
		if (!p.isBare()) {
			buffer = ByteBuffer.allocate(dataLength + 3);
			packetLength++;
			if (data.length >= 160) {
				buffer.put((byte) (160 + (packetLength / 256)));
				buffer.put((byte) (packetLength & 0xff));
			}
			else {
				buffer.put((byte) (packetLength));
				if (dataLength > 0) {
					dataLength--;
					buffer.put((byte) data[dataLength]);
				}
			}
			buffer.put((byte) p.getID());
		}
		else {
			buffer = ByteBuffer.allocate(dataLength);
		}
		buffer.put(data, 0, dataLength);
		buffer.flip();
		out.write(buffer);
	}

	/**
	 * Releases all resources used by this encoder.
	 *
	 * @param session The IO session
	 */
	public void dispose(IoSession session) {
	}
}
