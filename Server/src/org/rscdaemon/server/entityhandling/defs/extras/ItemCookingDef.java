package org.rscdaemon.server.entityhandling.defs.extras;

/**
 * The definition wrapper for items
 */
public class ItemCookingDef {

	/**
	 * The exp cooking this item gives
	 */
	public int exp;
	/**
	 * The id of the cooked version
	 */
	public int cookedId;
	/**
	 * The id of the burned version
	 */
	public int burnedId;
	/**
	 * The level required to cook this
	 */
	public int requiredLvl;
	
	public int getExp() {
		return exp;
	}
	
	public int getCookedId() {
		return cookedId;
	}
	
	public int getBurnedId() {
		return burnedId;
	}
	
	public int getReqLevel() {
		return requiredLvl;
	}
	
}
