package org.rscdaemon.server.event;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.util.Logger;

public class SaveEvent {
	
	public static final World world = World.getWorld();
	
	public static void saveAll() {
		long cur = System.currentTimeMillis();
		for(Player p : world.getPlayers()) {
			p.save();
		}
		cur = System.currentTimeMillis() - cur;
		Logger.print(world.getPlayers().count() + " Players saved in " + cur + "ms", 3);
	}

}
