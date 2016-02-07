package org.rscdaemon.server.entityhandling.defs.extras;

/**
 * The definition wrapper for herbs
 */
public class ItemUnIdentHerbDef {

	/**
	 * Herblaw level required to identify
	 */
	public int requiredLvl;
	/**
	 * The id of the herb this turns into
	 */
	private int newId;
	/**
	 * How much experience identifying gives
	 */
	public int exp;
	
	public int getExp() {
		return exp;
	}
	
	public int getNewId() {
		return newId;
	}
	
	public int getLevelRequired() {
		return requiredLvl;
	}
	
}
