package org.rscdaemon.server.event;

import org.rscdaemon.server.model.*;

public class ObjectRemover extends DelayedEvent {
	public static final World world = World.getWorld();
	private GameObject object;

	public ObjectRemover(GameObject object, int delay) {
		super(null, delay);
		this.object = object;
	}

	public void run() {
		ActiveTile tile = world.getTile(object.getLocation());
		if(!tile.hasGameObject() || !tile.getGameObject().equals(object)) {
			super.running = false;
			return;
		}
		tile.remove(object);
		world.unregisterGameObject(object);
		super.running = false;
	}

	public boolean equals(Object o) {
		if(o instanceof ObjectRemover) {
			return ((ObjectRemover)o).getObject().equals(getObject());
		}
		return false;
	}

	public GameObject getObject() {
		return object;
	}

}