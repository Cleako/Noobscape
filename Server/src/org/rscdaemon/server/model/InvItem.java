package org.rscdaemon.server.model;

import org.rscdaemon.server.entityhandling.EntityHandler;
import org.rscdaemon.server.entityhandling.defs.ItemDef;
import org.rscdaemon.server.entityhandling.defs.extras.*;

public class InvItem extends Entity implements Comparable<InvItem> {
	
	private int amount;
	private boolean wielded = false;

	public InvItem(int id) {
		setID(id);
		setAmount(1);
	}

	public InvItem(int id, int amount) {
		setID(id);
		setAmount(amount);
	}
	
	public ItemSmeltingDef getSmeltingDef() {
		return EntityHandler.getItemSmeltingDef(id);
	}
	
	public ItemCookingDef getCookingDef() {
		return EntityHandler.getItemCookingDef(id);
	}
	
	public ItemUnIdentHerbDef getUnIdentHerbDef() {
		return EntityHandler.getItemUnIdentHerbDef(id);
	}
	
	public ItemWieldableDef getWieldableDef() {
		return EntityHandler.getItemWieldableDef(id);
	}
	
	public ItemDef getDef() {
		return EntityHandler.getItemDef(id);
	}
	
	public boolean isWieldable() {
		return EntityHandler.getItemWieldableDef(id) != null;
	}
	
	public boolean isEdible() {
		return EntityHandler.getItemEdibleHeals(id) > 0;
	}
	
	public boolean isWielded() {
		return wielded;
	}
	
	public void setWield(boolean wielded) {
		this.wielded = wielded;
	}
	
	public void setAmount(int amount) {
		if(amount < 0) {
			amount = 0;
		}
		this.amount = amount;	
	}
	
	public int getAmount() {
		return amount;
	}
	
	public boolean wieldingAffectsItem(InvItem i) {
		if(!i.isWieldable() || !isWieldable()) {
			return false;
		}
      		for(int affected : getWieldableDef().getAffectedTypes()) {
      			if(i.getWieldableDef().getType() == affected) {
      				return true;
      			}
      		}
      		return false;
	}
	
	public int eatingHeals() {
		if(!isEdible()) {
			return 0;
		}
		return EntityHandler.getItemEdibleHeals(id);
	}
	
	public boolean equals(Object o) {
		if (o instanceof InvItem) {
			InvItem item = (InvItem)o;
			return item.getID() == getID();
		}
		return false;
	}
	
	public int compareTo(InvItem item) {
		if(item.getDef().isStackable()) {
			return -1;
		}
		if(getDef().isStackable()) {
			return 1;
		}
		return item.getDef().getBasePrice() - getDef().getBasePrice();
	}

}
