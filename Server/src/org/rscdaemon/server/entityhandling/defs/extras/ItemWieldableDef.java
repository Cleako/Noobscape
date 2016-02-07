package org.rscdaemon.server.entityhandling.defs.extras;

import org.rscdaemon.server.entityhandling.EntityHandler;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

/**
 * The definition wrapper for items
 */
public class ItemWieldableDef {

	/**
	 * Sprite Id of the item
	 */
	public int sprite;
	/**
	 * Type of item
	 */
	public int type;
	/**
	 * Body position the item is drawn on
	 */
	private int wieldPos;
	/**
	 * How many armour points should be given
	 */
	public int armourPoints;
	/**
	 * How many weapon aim points should be given
	 */
	public int weaponAimPoints;
	/**
	 * How many weapon power points should be given
	 */
	public int weaponPowerPoints;
	/**
	 * How many magic points should be given
	 */
	public int magicPoints;
	/**
	 * How many prayer points should be given
	 */
	public int prayerPoints;
	/**
	 * How many range points should be given
	 */
	public int rangePoints;
	/**
	 * The level of each stat required to wield
	 */
	public HashMap<Integer, Integer> requiredStats;
	/**
	 * If the item can only be worn by females (used for female plates)
	 */
	private boolean femaleOnly;
	
	public int getSprite() {
		return sprite;
	}
	
	public int getType() {
		return type;
	}
	
	public int[] getAffectedTypes() {
		int[] affectedTypes = EntityHandler.getItemAffectedTypes(type);
		if(affectedTypes != null) {
			return affectedTypes;
		}
		return new int[0];
	}
	
	public int getWieldPos() {
		return wieldPos;
	}
	
	public int getArmourPoints() {
		return armourPoints;
	}
	
	public int getWeaponAimPoints() {
		return weaponAimPoints;
	}
	
	public int getWeaponPowerPoints() {
		return weaponPowerPoints;
	}
	
	public int getMagicPoints() {
		return magicPoints;
	}
	
	public int getPrayerPoints() {
		return prayerPoints;
	}
	
	public int getRangePoints() {
		return rangePoints;
	}
	
	public Set<Entry<Integer, Integer>> getStatsRequired() {
		return requiredStats.entrySet();
	}
	
	public boolean femaleOnly() {
		return femaleOnly;
	}
}
