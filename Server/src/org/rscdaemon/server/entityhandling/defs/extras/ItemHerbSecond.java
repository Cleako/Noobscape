package org.rscdaemon.server.entityhandling.defs.extras;

/**
 * The definition wrapper for items
 */
public class ItemHerbSecond {
	
	/**
	 * The level required to complete this potion
	 */
	public int requiredLvl;
	/**
	 * The exp given completing this potion
	 */
	public int exp;
	/**
	 * The ID of the potion created
	 */
	public int potionID;
	/**
	 * The ID of the unfinished potion required
	 */
	public int unfinishedID;
	/**
	* The ID of the second ingredient
	*/
	public int secondID;
	
	public int getSecondID() {
		return secondID;
	}
	
	public int getUnfinishedID() {
		return unfinishedID;
	}
	
	public int getPotionID() {
		return potionID;
	}
	
	public int getReqLevel() {
		return requiredLvl;
	}
	
	public int getExp() {
		return exp;
	}
}
