package org.rscdaemon.server.model;

import org.rscdaemon.server.event.DelayedEvent;
import org.rscdaemon.server.util.DataConversions;

import java.util.*;

public class Shop {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	/**
	 * The maximum size of a shop
	 */
	private static int MAX_SIZE = 40;
	
	private String name;
	private boolean general;
	private int sellModifier;
	private int buyModifier;
	private int minX, maxX, minY, maxY;
	private String greeting;
	private String[] options;
	private ArrayList<InvItem> items;
	private int[] equilibriumIds;
	private int[] equilibriumAmounts;
	private ArrayList<Player> players;
	private int respawnRate;
	
	public boolean shouldStock(int id) {
		if(general) {
			return true;
		}
		for(int eqID : equilibriumIds) {
			if(eqID == id) {
				return true;
			}
		}
		return false;
	}
	
	public void addPlayer(Player player) {
		players.add(player);
	}
	
	public void removePlayer(Player player) {
		players.remove(player);
	}
	
	public void initRestock() {
		players = new ArrayList<Player>();
		final Shop shop = this;
		world.getDelayedEventHandler().add(new DelayedEvent(null, respawnRate) {
			private int iterations = 0;
			public void run() {
				boolean changed = false;
				Iterator<InvItem> iterator = items.iterator();
				iterations++;
				while(iterator.hasNext()) {
					InvItem shopItem = iterator.next();
					int eq = shop.getEquilibrium(shopItem.getID());
					if((iterations % 4 == 0) && shopItem.getAmount() > eq) {
						shopItem.setAmount(shopItem.getAmount() - 1);
						if(shopItem.getAmount() <= 0 && !DataConversions.inArray(equilibriumIds, shopItem.getID())) {
							iterator.remove();
						}
						changed = true;
					}
					else if(shopItem.getAmount() < eq) {
						shopItem.setAmount(shopItem.getAmount() + 1);
						changed = true;
					}
				}
      				if(changed) {
      					shop.updatePlayers();
      				}
      			}
      		});
	}
	
	public void updatePlayers() {
		Iterator<Player> iterator = players.iterator();
      		while(iterator.hasNext()) {
      			Player p = iterator.next();
      			if(!equals(p.getShop())) {
      				iterator.remove();
      				continue;
      			}
      			p.getActionSender().showShop(this);
      		}
	}
	
	public int getEquilibrium(int id) {
		for(int idx = 0;idx < equilibriumIds.length;idx++) {
			if(equilibriumIds[idx] == id) {
				return equilibriumAmounts[idx];
			}
		}
		return 0;
	}
	
	public InvItem getFirstById(int id) {
		for(int index = 0;index < items.size();index++) {
			if(items.get(index).getID() == id) {
				return items.get(index);
			}
		}
		return null;
	}
	
	public void setEquilibrium() {
		equilibriumIds = new int[items.size()];
		equilibriumAmounts = new int[items.size()];
		for(int idx = 0;idx < items.size();idx++) {
			equilibriumIds[idx] = items.get(idx).getID();
			equilibriumAmounts[idx] = items.get(idx).getAmount();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getGreeting() {
		return greeting;
	}
	
	public String[] getOptions() {
		return options;
	}
	
	public boolean withinShop(Point p) {
		return p.getX() >= minX && p.getX() <= maxX && p.getY() >= minY && p.getY() <= maxY;
	}
	
	public ArrayList<InvItem> getItems() {
		return items;
	}
	
	public boolean contains(InvItem i) {
		return items.contains(i);
	}
	
	public int add(InvItem item) {
		if(item.getAmount() <= 0) {
			return -1;
		}
		for(int index = 0;index < items.size();index++) {
			if(item.equals(items.get(index))) {
				items.get(index).setAmount(items.get(index).getAmount() + item.getAmount());
				return index;
			}
		}
		items.add(item);
		return items.size() - 2;
	}
	
	public int remove(InvItem item) {
		Iterator<InvItem> iterator = items.iterator();
		for(int index = 0;iterator.hasNext();index++) {
			InvItem i = iterator.next();
			if(item.getID() == i.getID()) {
				if(item.getAmount() < i.getAmount()) {
					i.setAmount(i.getAmount() - item.getAmount());
				}
				else if(DataConversions.inArray(equilibriumIds, item.getID())) {
					i.setAmount(0);
				}
				else {
					iterator.remove();
				}
				return index;
			}
		}
		return -1;
	}
	
	public ListIterator<InvItem> iterator() {
		return items.listIterator();
	}
	
	public int countId(int id) {
		for(InvItem i : items) {
			if(i.getID() == id) {
				return i.getAmount();
			}
		}
		return 0;
	}
	
	public boolean full() {
		return items.size() >= MAX_SIZE;
	}
	
	public int size() {
		return items.size();
	}
	
	public boolean isGeneral() {
		return general;
	}
	
	public int getSellModifier() {
		return sellModifier;
	}
	
	public int getBuyModifier() {
		return buyModifier;
	}
	
	public int getRequiredSlots(List<InvItem> items) {
		int requiredSlots = 0;
		for(InvItem item : items) {
			if(items.contains(item)) {
				continue;
			}
			requiredSlots++;
		}
		return requiredSlots;
	}
	
	public int getRequiredSlots(InvItem item) {
		return (items.contains(item) ? 0 : 1);
	}
	
	public boolean canHold(InvItem item) {
		return (MAX_SIZE - items.size()) >= getRequiredSlots(item);
	}
	
	public boolean canHold(ArrayList<InvItem> items) {
		return (MAX_SIZE - items.size()) >= getRequiredSlots(items);
	}
	
	public boolean equals(Object o) {
		if(o instanceof Shop) {
			Shop shop = (Shop)o;
			return shop.getName().equals(name);
		}
		return false;
	}
	
}