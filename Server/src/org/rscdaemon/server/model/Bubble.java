package org.rscdaemon.server.model;

public class Bubble {
	/**
	 * Who the bubble belongs to
	 */
	private Player owner;
	/**
	 * What to draw in it
	 */
	private int itemID;

	public Bubble(Player owner, int itemID) {
		this.owner = owner;
		this.itemID = itemID;
	}
	
	public Player getOwner() {
		return owner;
	}

	public int getID() {
		return itemID;
	}

}
