package org.rscdaemon.server.clansystem;

public class ClanInvite {
	
	public ClanInvite(Clan c) {
		this.c = c;
	}
	
	public Clan getClan() {
		return c;
	}
	
	private Clan c;
}
