package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.net.Packet;
import org.apache.mina.common.IoSession;

public class RegisterAccount implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		
		String username = p.readString(20).trim();
		String password = p.readString(20).trim();
		String email = p.readString(20).trim();
		System.out.println(username);
		System.out.println(password);
		System.out.println(email);
		/*Player player = (Player)session.getAttachment();
		byte loginCode = 22;
		try {
			boolean reconnecting = (p.readByte() == 1);
			int clientVersion = p.readShort();
			RSCPacket loginPacket = DataConversions.decryptRSA(p.readBytes(p.readByte()));
  			int[] sessionKeys = new int[4];
  			for(int key = 0;key < sessionKeys.length;key++) {
  				sessionKeys[key] = loginPacket.readInt();
  			}
  			int uid = loginPacket.readInt();
  			String username = loginPacket.readString(20).trim();
  			loginPacket.skip(1);
  			String password = loginPacket.readString(20).trim();
  			loginPacket.skip(1);
  			int res = org.rscdaemon.server.io.PlayerLoader.getLogin(username, password);
  			if(world.countPlayers() >= org.rscdaemon.server.GameVars.maxUsers) {
  				loginCode = 10;
  			} else if(clientVersion != GameVars.clientVersion) {
  				loginCode = 4;
  			} else if(!player.setSessionKeys(sessionKeys)) {
  				loginCode = 5;
  				player.bad_login = true;
  			} else if(res == 0) {
  				loginCode = 2; // invalid username/pass.
  			} else if(res == 2) {
  				loginCode = 3; // user logged in.
  			} else if(res == 6) {
  				loginCode = 6;
  			}
  			else {
  				if(loginCode != 5)
  					player.bad_login = false;
  				player.load(username, password, uid, reconnecting);
  				return;
  				
  			}
		}
		catch(Exception e) {
			e.printStackTrace();
			//loginCode = 7;
		}
		if(loginCode != 22) {
		RSCPacketBuilder pb = new RSCPacketBuilder();
		pb.setBare(true);
		pb.addByte((byte)loginCode);
		session.write(pb.toPacket());
		player.destroy(true);
		}*/
	}
	
	
}
