package org.rscdaemon.server.model;

import java.util.*;

public class Inventory {
	/**
	 * World instance
	 */
	private static World world = World.getWorld();
	/**
	 * The maximum size of an inventory
	 */
	public static final int MAX_SIZE = 30;
	private Player player;
	private ArrayList<InvItem> list = new ArrayList<InvItem>();
	
	public Inventory() { }
	
	public Inventory(Player player) {
		this.player = player;
	}
	
	public ArrayList<InvItem> getItems() {
		return list;
	}
	
	public boolean wielding(int id) {
		for(InvItem i : list) {
			if(i.getID() == id && i.isWielded()) {
				return true;
			}
		}
		return false;
	}
	
	public int add(InvItem item) {
		if(item.getAmount() <= 0) {
			return -1;
		}
		if(item.getDef().isStackable()) {
			for(int index = 0;index < list.size();index++) {
				if(item.equals(list.get(index))) {
					list.get(index).setAmount(list.get(index).getAmount() + item.getAmount());
					return index;
				}
			}
		}
		else if(item.getAmount() > 1) {
			item.setAmount(1);
		}
		if(this.full()) {
			player.getActionSender().sendMessage("Your Inventory is full, the " + item.getDef().getName() + " drops to the ground!");
			world.registerItem(new Item(item.getID(), player.getX(), player.getY(), item.getAmount(), player));
			return -1;
		}
		list.add(item);
		return list.size() - 2;
	}
	
	public int remove(int id, int amount) {
		int size = list.size();
		ListIterator<InvItem> iterator = list.listIterator(size);
		for(int index = size - 1;iterator.hasPrevious();index--) {
			InvItem i = iterator.previous();
			if(id == i.getID()) {
				if(i.getDef().isStackable() && amount < i.getAmount()) {
					i.setAmount(i.getAmount() - amount);
				}
				else {
					if(i.isWielded()) {
						player.getActionSender().sendSound("click");
						i.setWield(false);
						player.updateWornItems(i.getWieldableDef().getWieldPos(), player.getPlayerAppearance().getSprite(i.getWieldableDef().getWieldPos()));
						player.getActionSender().sendEquipmentStats();
					}
					iterator.remove();
				}
				return index;
			}
		}
		return -1;
	}
	
	public int remove(InvItem item) {
		return remove(item.getID(), item.getAmount());
	}
	
	public void remove(int index) {
		InvItem item = get(index);
		if(item == null) {
			return;
		}
		remove(item.getID(), item.getAmount());
	}
	
	public void sort() {
		Collections.sort(list);
	}
	
	public ListIterator<InvItem> iterator() {
		return list.listIterator();
	}
	
	public int getLastIndexById(int id) {
		for(int index = list.size() - 1;index >= 0;index--) {
			if(list.get(index).getID() == id) {
				return index;
			}
		}
		return -1;
	}
	
	public int countId(int id) {
		int temp = 0;
		for(InvItem i : list) {
			if(i.getID() == id) {
				temp += i.getAmount();
			}
		}
		return temp;
	}
	
	public boolean full() {
		return list.size() >= MAX_SIZE;
	}
	
	public boolean contains(InvItem i) {
		return list.contains(i);
	}
	
	public InvItem get(InvItem item) {
		for(int index = list.size() - 1;index >= 0;index--) {
			if(list.get(index).equals(item)) {
				return list.get(index);
			}
		}
		return null;
	}
	
	public InvItem get(int index) {
		if(index < 0 || index >= list.size()) {
			return null;
		}
		return list.get(index);
	}

	public int size() {
		return list.size();
	}
	
	public int getFreedSlots(List<InvItem> items) {
		int freedSlots = 0;
		for(InvItem item : items) {
			freedSlots += getFreedSlots(item);;
		}
		return freedSlots;
	}
	
	public int getFreedSlots(InvItem item) {
		return (item.getDef().isStackable() && countId(item.getID()) > item.getAmount() ? 0 : 1);
	}
	
	public int getRequiredSlots(List<InvItem> items) {
		int requiredSlots = 0;
		for(InvItem item : items) {
			requiredSlots += getRequiredSlots(item);
		}
		return requiredSlots;
	}
	
	public int getRequiredSlots(InvItem item) {
		return (item.getDef().isStackable() && list.contains(item) ? 0 : 1);
	}
	
	public boolean canHold(InvItem item) {
		return (MAX_SIZE - list.size()) >= getRequiredSlots(item);
	}

}
