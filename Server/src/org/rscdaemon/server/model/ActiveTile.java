package org.rscdaemon.server.model;

import java.util.ArrayList;
import java.util.List;

public class ActiveTile {
	/**
	* A list of all players currently on this tile
	*/
	private List<Player> players = new ArrayList<Player>();
	/**
	 * A list of all npcs currently on this tile
	 */
	private List<Npc> npcs = new ArrayList<Npc>();
	/**
	 * A list of all items currently on this tile
	 */
	private List<Item> items = new ArrayList<Item>();
	/**
	 * The object currently on this tile (can only have 1 at a time)
	 */
	private GameObject object = null;
	/**
	 * World instance
	 */
	private static World world = World.getWorld();
	/**
	 * The x and y coordinates of this tile
	 */
	private int x, y;
	
	/**
	 * Constructs a new tile at the given coordinates
	 */
	public ActiveTile(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Add an entity to the tile
	 */
	public void add(Entity entity) {
		if (entity instanceof Player) {
			players.add((Player)entity);
		}
		else if (entity instanceof Npc) {
			npcs.add((Npc)entity);
		}
		else if (entity instanceof Item) {
			items.add((Item)entity);
		}
		else if (entity instanceof GameObject) {
			if(object != null) {
				world.unregisterGameObject(object);
			}
/*
			else {
				GameObject go = (GameObject)entity;
				System.out.println("  <GameObjectLoc>");
				System.out.println("    <id>" + go.getLoc().getId() + "</id>");
				System.out.println("    <x>" + go.getLoc().getX() + "</x>");
				System.out.println("    <y>" + go.getLoc().getY() + "</y>");
				System.out.println("    <direction>" + go.getLoc().getDirection() + "</direction>");
				System.out.println("    <type>" + go.getLoc().getType() + "</type>");
				System.out.println("  </GameObjectLoc>");
			}
*/
			object = (GameObject)entity;
		}
	}
	
	/**
	 * Remove an entity from the tile
	 */
	public void remove(Entity entity) {
		if (entity instanceof Player) {
			players.remove(entity);
		}
		else if (entity instanceof Npc) {
			npcs.remove(entity);
		}
		else if (entity instanceof Item) {
			items.remove(entity);
		}
		else if (entity instanceof GameObject) {
			object = null;
		}
	}

	public boolean hasPlayersExcept(Player player) {
		List playersExcept = players;
		playersExcept.remove(player);
		return ((playersExcept != null) && (playersExcept.size() > 0));
	}
	
	public boolean hasPlayers() {
		return players != null && players.size() > 0;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public boolean hasGameObject() {
		return object != null;
	}
	
	public GameObject getGameObject() {
		return object;
	}
	
	public List<Item> getItems() {
		return items;
	}
	
	public boolean hasItem(Item item) {
		return items.contains(item);
	}
	
	public boolean hasItems() {
		return items != null && items.size() > 0;
	}

	public List<Npc> getNpcs() {
		return npcs;
	}
	
	public boolean hasNpcs() {
		return npcs != null && npcs.size() > 0;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
