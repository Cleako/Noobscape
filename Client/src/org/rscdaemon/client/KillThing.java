package org.rscdaemon.client;

public class KillThing {
	
	public String killStr;
	public long timeShown;
	
	
	public KillThing(String kill) {
		killStr = kill;
		timeShown = System.currentTimeMillis();
	}

}
