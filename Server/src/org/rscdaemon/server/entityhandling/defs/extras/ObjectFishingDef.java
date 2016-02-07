package org.rscdaemon.server.entityhandling.defs.extras;

/**
 * The definition wrapper for fishing spots
 */
public class ObjectFishingDef {

	/**
	 * The fish that can be caught here
	 */
	public ObjectFishDef[] defs;
	/**
	 * The Id of the net required to fish with
	 */
	public int netId;
	/**
	 * The If of any bait required to go with the net
	 */
	public int baitId;
	
	public int getNetId() {
		return netId;
	}
	
	public int getBaitId() {
		return baitId;
	}
	
	public int getReqLevel() {
		int requiredLevel = 99;
		for(ObjectFishDef def : defs) {
			if(def.getReqLevel() < requiredLevel) {
				requiredLevel = def.getReqLevel();
			}
		}
		return requiredLevel;
	}
	
	public ObjectFishDef[] getFishDefs() {
		return defs;
	}
	
}
