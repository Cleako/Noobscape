package org.rscdaemon.server;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.event.*;

import java.util.ArrayList;
import java.util.Iterator;

public final class DelayedEventHandler {
	private static World world = World.getWorld();
	private ArrayList<DelayedEvent> toAdd = new ArrayList<DelayedEvent>();
	private ArrayList<DelayedEvent> events = new ArrayList<DelayedEvent>();
	
	public DelayedEventHandler() {
		world.setDelayedEventHandler(this);
	}
	
	public boolean contains(DelayedEvent event) {
		return events.contains(event);
	}
	
	public ArrayList<DelayedEvent> getEvents() {
		return events;
	}
	
	public void add(DelayedEvent event) {
		if(!events.contains(event)) {
			toAdd.add(event);
		}
	}
	
	public void remove(DelayedEvent event) {
		events.remove(event);
	}
	
	public void removePlayersEvents(Player player) {
		Iterator<DelayedEvent> iterator = events.iterator();
		while(iterator.hasNext()) {
			DelayedEvent event = iterator.next();
			if(event.belongsTo(player)) {
				iterator.remove();
			}
		}
	}
	
	public void doEvents() {
		if(toAdd.size() > 0) {
			events.addAll(toAdd);
			toAdd.clear();
		}
		Iterator<DelayedEvent> iterator = events.iterator();
		while(iterator.hasNext()) {
			DelayedEvent event = iterator.next();
			if(event.shouldRun()) {
				event.run();
				event.updateLastRun();
			}
			if(event.shouldRemove()) {
				iterator.remove();
			}
		}
	}

}