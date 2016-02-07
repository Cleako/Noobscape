package org.rscdaemon.server.clansystem;

import org.rscdaemon.server.model.*;
import java.util.ArrayList;

public class Clan {
	
	public Clan(String name) {
		players = new ArrayList<Player>();
		this.name = name;
	}
	
	
	
	public void add(Player p, String reason) {
		System.out.println("add:" + p.getUsername());
		if(players.contains(p)) {
			return;
		}
		players.add(p);
		globalMessage(null, p.getUsername() + reason);
	}
	
	public void remove(Player p, String reason) {
		synchronized(players) {
			if(!players.contains(p)) {
				return;
			}
			players.remove(p);
			try {
				globalMessage(null, p.getUsername() + reason);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void globalMessage(Player sender, String s) {
		for(Player e : players) {
			e.getActionSender().sendMessage(Clan.getPrefix(name) + (sender == null ? "" : sender.getUsername()) + ": @yel@" + s);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public static String getPrefix(String s) {
		return "@say@@cya@[C] @whi@";
	}
	
	public void setOwner(Player p) {
		this.owner = p;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public String getOwnerName() {
		return ownerName;
	}
	
	public void setOwnerName(String s) {
		this.ownerName = s;
	}
	
	public boolean equals(Clan c) {
		return c.getName().equals(getName());
	}
	
	
	private String name;
	private String ownerName;
	private Player owner;
	private ArrayList<Player> players;
}
