package org.rscdaemon.server.npchandler;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;

public interface NpcHandler {
	public void handleNpc(final Npc npc, Player player) throws Exception;
}
