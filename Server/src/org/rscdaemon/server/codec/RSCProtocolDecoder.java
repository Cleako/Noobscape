package org.rscdaemon.server.codec;

import org.rscdaemon.server.net.RSCPacket;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * A decoder for the RSC protocol. Parses the incoming data from an
 * IoSession and outputs it as a <code>RSCPacket</code> object.
 */
public class RSCProtocolDecoder extends CumulativeProtocolDecoder {
	/**
	 * Parses the data in the provided byte buffer and writes it to
	 * <code>out</code> as a <code>RSCPacket</code>.
	 *
	 * @param session The IoSession the data was read from
	 * @param in	  The buffer
	 * @param out	 The decoder output stream to which to write the <code>RSCPacket</code>
	 * @return Whether enough data was available to create a packet
	 */
	protected boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) {
		if (in.remaining() >= 2) {
			int length = in.get();
			if (length >= 160) {
				length = (length - 160) * 256 + in.get();
			}
			if (length <= in.remaining()) {
				byte[] payload = new byte[length - 1];
				int id;
				if(length < 160) {
					if (length > 1) {
						payload[length - 2] = in.get();
						id = in.getUnsigned();
						if (length - 2 > 0) {
							in.get(payload, 0, length - 2);
						}
					}
					else {
						id = in.getUnsigned();
					}
				}
				else {
					id = in.getUnsigned();
					in.get(payload);
				}
				out.write(new RSCPacket(session, id, payload));
				return true;
			}
			else {
				in.rewind();
				return false;
			}
		}
		return false;
	}

	/**
	 * Releases the buffer used by the given session.
	 *
	 * @param session The session for which to release the buffer
	 * @throws Exception if failed to dispose all resources
	 */
	public void dispose(IoSession session) throws Exception {
		super.dispose(session);
	}
}
