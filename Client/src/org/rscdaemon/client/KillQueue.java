package org.rscdaemon.client;

import java.util.LinkedList;

public class KillQueue {

	public LinkedList<KillThing> killthing = new LinkedList<KillThing>();
	
	public KillQueue() {
	
	}
	
	
	public void addKill(KillThing kill) {
		try {
			killthing.addFirst(kill);
			if(killthing.size() >= 6) {
				killthing.removeLast();
			}
		} catch(Exception e) {

		}
	}

	public void clean() {
		try {
			for(KillThing k : killthing) {
				if(System.currentTimeMillis() - k.timeShown > 6000) {
					killthing.remove(k);
				}
			}
		} catch(Exception e) {

		}
	}

}
