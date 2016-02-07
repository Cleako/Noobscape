package org.rscdaemon.server.event;

import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.entityhandling.defs.extras.ObjectMiningDef;

public abstract class ShortEvent extends SingleEvent {
	
	public ShortEvent(Player owner) {
		super(owner, 1500);
	}
	
	public ShortEvent(Player owner, ObjectMiningDef def) {
		super(owner, 1500);
	}
	
	public abstract void action();

}