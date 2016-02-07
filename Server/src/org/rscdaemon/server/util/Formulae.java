package org.rscdaemon.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.rscdaemon.server.entityhandling.EntityHandler;
import org.rscdaemon.server.entityhandling.defs.SpellDef;
import org.rscdaemon.server.entityhandling.defs.extras.FiremakingDef;
import org.rscdaemon.server.entityhandling.defs.extras.ObjectFishDef;
import org.rscdaemon.server.entityhandling.defs.extras.ObjectMiningDef;
import org.rscdaemon.server.entityhandling.defs.extras.ObjectWoodcuttingDef;
import org.rscdaemon.server.model.Entity;
import org.rscdaemon.server.model.GameObject;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.Mob;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Point;

public class Formulae {

	public static final int[] experienceArray = {83, 174, 276, 388, 512, 650, 801, 969, 1154, 1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470, 5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833, 16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224, 41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721, 101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254, 224466, 247886, 273742, 302288, 333804, 368599, 407015, 449428, 496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895, 1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068, 2192818, 2421087, 2673114,2951373, 3258594, 3597792, 3972294, 4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614, 8771558, 9684577, 10692629, 11805606, 13034431, 14391160};
	public static final int[] eArray = {0, 0, 83, 174, 276, 388, 512, 650, 801, 969, 1154, 1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470, 5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833, 16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224, 41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721, 101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254, 224466, 247886, 273742, 302288, 333804, 368599, 407015, 449428, 496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895, 1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068, 2192818, 2421087, 2673114,2951373, 3258594, 3597792, 3972294, 4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614, 8771558, 9684577, 10692629, 11805606, 13034431, 14391160};
	public static final String[] statArray = {"attack", "defense", "strength", "hits", "ranged", "prayer", "magic", "cooking", "woodcut", "fletching", "fishing", "firemaking", "crafting", "smithing", "mining", "herblaw", "agility", "thieving"};
	public static final int[] woodcuttingAxeIDs = {405, 204, 203, 428, 88, 12, 87};
	public static final int[] miningAxeIDs = {1262, 1261, 1260, 1259, 1258, 156};
	public static final int[] miningAxeLevels = { 41, 31, 21, 6, 1, 1 };
	public static final int[] arrowIDs = {723, 647, 646, 645, 644, 643, 642, 641, 640, 639, 638, 574, 11};
	public static final int[] bowIDs = {188, 189, 648, 649, 650, 651, 652, 653, 654, 655, 656, 657};
	public static final int[] boltIDs = {786, 592, 190};
	public static final int[] xbowIDs = {59, 60};
	public static final int[] safePacketIDs = {70, 123, 128, 255};
	public static final int[] headSprites = {1,4,6,7,8};
	public static final int[] bodySprites = {2,5};
	public static final int[] runeIDs = {31, 32, 33, 34, 35, 36, 37, 38, 40, 41, 42, 46, 619, 825};

	public static final String[] p2pSpells = { "Wind wave", "Water wave", "Earth wave", "Fire wave", "Claws of Guthix", "Flames of Zamorak", "Saradomin Strike", "Charge", "Enfeeble", "Camelot teleport", "Ardougne teleport", "Stun", "Iban blast", "Vulnerability", "Enchant lvl-5 amulet", "Charge Water Orb", "Charge earth Orb", "Charge Fire Orb", "Charge air Orb" };
	public static int[] dragonIDs = { 196, 201, 202, 291, 477 };
	public static final int[] untradeableItems = { 575, 81 };

	public static final int[] potionsUnfinished = {454, 455, 456, 457, 458, 459, 460, 461, 462, 463};
	public static final int[] potions1Dose = {224, 476, 479, 482, 485, 488, 491, 494, 497, 500, 568, 571};
	public static final int[] potions2Dose = {223, 475, 478, 481, 484, 487, 490, 493, 496, 499, 567, 570};
	public static final int[] potions3Dose = {222, 474, 477, 480, 483, 486, 489, 492, 495, 498, 566, 569};
	private static Random r = new Random();

	public static int Rand(int low, int high) {
       	return low + r.nextInt(high - low);
    }
	
	public static int getPickAxe(Player player) {
		int miningLevel = player.getCurStat(14);
		int pick = -1;
		for(int index = 0; index < miningAxeIDs.length; index++) {
			if(player.getInventory().countId(miningAxeIDs[index]) > 0) {
				pick = -2;
				if(miningLevel >= miningAxeLevels[index]) {
					pick = miningAxeIDs[index];
					break;
				}
			}
		}
		return pick;
	}
	
	public static int getPickaxeSwings(int pickaxeId) {
		/**
		 * Set a default amount of pickaxe swings
		 * for any pickaxe that isn't in the switch
		 */
		int pickaxeSwings = 2;
		switch(pickaxeId) {
			case 1258: // Iron Pickaxe
				pickaxeSwings = 4;
				break;
			case 1259: // Steel Pickaxe
				pickaxeSwings = 6;
				break;
			case 1260: // Mithril Pickaxe
				pickaxeSwings = 8;
				break;
			case 1261: // Adamantite Pickaxe
				pickaxeSwings = 10;
				break;
			case 1262: // Rune Pickaxe
				pickaxeSwings = 12;
		}
		return pickaxeSwings;
	}

	public static int getPotionDose(int id) {
		if(DataConversions.inArray(potions1Dose, id)) {
			return 1;
		}
		if(DataConversions.inArray(potions2Dose, id)) {
			return 2;
		}
		if(DataConversions.inArray(potions3Dose, id)) {
			return 3;
		}
		return 0;
	}

	public static String getLvlDiffColour(int lvlDiff) {
		if(lvlDiff < -9) {
			return "@red@";
		}
		else if(lvlDiff < -6) {
			return "@or3@";
		}
		else if(lvlDiff < -3) {
			return "@or2@";
		}
		else if(lvlDiff < 0) {
			return "@or1@";
		}
		else if(lvlDiff > 9) {
			return "@gre@";
		}
		else if(lvlDiff > 6) {
			return "@gr3@";
		}
		else if(lvlDiff > 3) {
			return "@gr2@";
		}
		else if(lvlDiff > 0) {
			return "@gr1@";
		}
		return "@whi@";
	}

	public static int getStat(String stat)
	{
		for(int i = 0; i < statArray.length; i++) {
			if(statArray[i].equalsIgnoreCase(stat))
			{
				return i;
			}
		}
		return -1;
	}

	public static boolean catchThief(int lvl, int reqLevel)
	{
		double rand = ((r.nextDouble() * 100) + 1) / 100;
		double success = getMiningFailPercent((double)lvl, (double)reqLevel) / 100.0;

		if(success < 0.35)
			success = 0.35;

		if(reqLevel < 15)
		{
			if(lvl - reqLevel < 10)
			{
				if(Rand(1, 10) == 5)
					success = 1.0;
			}
		}

		//System.out.println("Rand: " + rand + "%, success: " + success + "% - " + (rand < success ? "success" : "fail"));

		if(rand < success)
			return false;

		return true;
	}

	@SuppressWarnings("unchecked")
	public static List<InvItem> getKeyChestLoot() {
		List<InvItem>[] possibleLoots = (List<InvItem>[])EntityHandler.getKeyChestLoots();
		return possibleLoots[DataConversions.random(0, possibleLoots.length - 1)];
	}

	public static boolean doorAtFacing(Entity e, int x, int y, int dir) {
		if(dir >= 0 && e instanceof GameObject) {
			GameObject obj = (GameObject)e;
			return obj.getType() == 1 && obj.getDirection() == dir && obj.isOn(x, y);
		}
		return false;
	}

	public static boolean objectAtFacing(Entity e, int x, int y, int dir) {
		if(dir >= 0 && e instanceof GameObject) {
			GameObject obj = (GameObject)e;
			return obj.getType() == 0 && obj.getDirection() == dir && obj.isOn(x, y);
		}
		return false;
	}

	public static int bitToDoorDir(int bit) {
		switch(bit) {
			case 1:
				return 0;
			case 2:
				return 1;
			case 4:
				return -1;
			case 8:
				return -1;
		}
		return -1;
	}

	public static int bitToObjectDir(int bit) {
		switch(bit) {
			case 1:
				return 6;
			case 2:
				return 0;
			case 4:
				return 2;
			case 8:
				return 4;
		}
		return -1;
	}

	public static int getNewY(int currentY, boolean up) {
		int height = getHeight(currentY);
		int newHeight;
		if(up) {
			if(height == 3) {
				newHeight = 0;
			}
			else if(height >= 2) {
				return currentY;
			}
			else {
				newHeight = height + 1;
			}
		}
		else {
			if(height == 0) {
				newHeight = 3;
			}
			else if(height >= 3) {
				return currentY;
			}
			else {
				newHeight = height - 1;
			}
		}
		return (newHeight * 944) + (currentY % 944);
	}

	/**
	 * Gets the empty jug ID
	 */
	public static int getEmptyJug(int fullJug) {
		switch(fullJug) {
			case 50:
				return 21;
			case 141:
				return 140;
			case 342:
				return 341;
		}
		return -1;
	}

	/**
	 * Returns a gem ID
	 */
	public static int getGem() {
		int rand = DataConversions.random(0, 100);
		if(rand < 10) {
			return 157;
		}
		else if(rand < 30) {
			return 158;
		}
		else if(rand < 60) {
			return 159;
		}
		else {
			return 160;
		}
	}

	/*
	 * Should the pot crack?
	 */
	public static boolean crackPot(int requiredLvl, int craftingLvl) {
		int levelDiff = craftingLvl - requiredLvl;
		if(levelDiff < 0) {
			return true;
		}
		if(levelDiff >= 20) {
			return false;
		}
		return DataConversions.random(0, levelDiff + 1) == 0;
	}

	/**
	 * Should the spell cast or fail?
	 */
	public static boolean castSpell(SpellDef def, int magicLevel, int magicEquip) {
		int levelDiff = magicLevel - def.getReqLevel();
		if(levelDiff < 0) {
			return false;
		}
		if(levelDiff >= 10) {
			return true;
		}
		return DataConversions.random(0, (levelDiff + 2) * 2) != 0;
	}

	/**
	 * Should the arrow be dropped or disappear
	 */
	public static boolean looseArrow(int damage) {
		return DataConversions.random(0, 6) == 0;
	}

	/**
	 * Gets the smithing exp for the given amount of the right bars
	 */
	public static int getSmithingExp(int barID, int barCount) {
		int[] exps = {13, 25, 37, 50, 83, 74};
		int type = getBarType(barID);
		if(type < 0) {
			return 0;
		}
		return exps[type] * barCount;
	}

	/**
	 * Gets the min level required to smith a bar
	 */
	public static int minSmithingLevel(int barID) {
		int[] levels = {1, 15, 30, 50, 70, 85};
		int type = getBarType(barID);
		if(type < 0) {
			return -1;
		}
		return levels[type];
	}

	/**
	 * Gets the type of bar we have
	 */
	public static int getBarType(int barID) {
		switch(barID) {
			case 169:
				return 0;
			case 170:
				return 1;
			case 171:
				return 2;
			case 173:
				return 3;
			case 174:
				return 4;
			case 408:
				return 5;
		}
		return -1;
	}

	public static int firemakingExp(int level, int baseExp) {
		return DataConversions.roundUp(baseExp + (level * 1.75D));
	}

	/**
	 * Should the fire light or fail?
	 */
	public static boolean lightLogs(FiremakingDef def, int firemakingLvl) {
		int levelDiff = firemakingLvl - def.getRequiredLevel();
		if(levelDiff < 0) {
			return false;
		}
		if(levelDiff >= 20) {
			return true;
		}
		return DataConversions.random(0, levelDiff + 1) != 0;
	}

	/**
	 * Generates a session id
	 */
	public static long generateSessionKey(byte userByte) {
		return DataConversions.getRandom().nextLong();
	}

	/**
	 * Given a stat string get its index
	 * returns -1 on failure
	 */
	public static int getStatIndex(String stat) {
		for(int index = 0;index < statArray.length;index++) {
			if(stat.equalsIgnoreCase(statArray[index])) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Calculate how much experience a Mob gives
	 */
	public static int combatExperience(Mob mob) {
		double exp = ((mob.getCombatLevel() * 10) + 10) * 0.22D;
		return (int)(mob instanceof Player? (exp / 4D) : exp);
	}

	/**
	 * Adds the prayers together to calculate what perecntage the stat should be increased
	 */
	private static double addPrayers(boolean first, boolean second, boolean third) {
		if(third) {
			return 1.15D;
		}
		if(second) {
			return 1.1D;
		}
		if(first) {
			return 1.05D;
		}
		return 1.0D;
	}

	/**
	 * Returns a power to assosiate with each arrow
	 */
	private static double arrowPower(int arrowID) {
		switch(arrowID) {
			case 11: //bronze arrows
			case 574: //poison bronze arrows
			case 190: //crossbow bolts
			case 592: //poison cross bow bolts
			case 1013: //bronze throwing dart
			case 1122: //poison bronze throwing dart
				return 0;
			case 638://iron arrows
			case 639://poison iron arrows
			case 1015: //iron throwing dart
			case 1123://poison iron throwing dart
				return 1;
			case 640://steel arrows
			case 641://poison steel arrows
			case 1024: //steel throwing dart
			case 1124: //poison steel throwing dart
			case 1076://bronze throwing dart
			case 1128://poison bronze throwing knife
			case 827://bronze spear
			case 1135://poison bronze spear
				return 2;
			case 642://mith arrows
			case 643://poison mith arrows
			case 786://pearle crossbow bolts
			case 1068://mith throwing dart
			case 1125: //poison mith throwing dart
			case 1075://iron throwing dart
			case 1129://poison iron throwing knife
			case 1088://iron spear
			case 1136://poison iron spear
				return 3;
			case 644://addy arrows
			case 645://poison addy arrows
			case 1069://addy throwing dart
			case 1126://poison addy throwing dart
			case 1077://steel throwing knife
			case 1130://poison steel throwing knife
			case 1089://steel spear
			case 1137://poison steel spear
				return 4;
			case 1081://black throwing knife
			case 1132://poison black throwing knife
				return 4.5;
			case 646://rune arrows
			case 647://poison rune arrows
			case 1070://rune throwing dart
			case 1127://poison rune throwing dart
			case 1078://mith throwing knife
			case 1131://poison mith throwing knife
			case 1090://mith spear
			case 1138://poison mith spear
				return 5;
			case 723://ice arrows
			case 1079://addy throwing knife
			case 1133://poison addy throwing knife
			case 1091://addy spear
			case 1139://poison addy spear
				return 6;
			case 1080://rune throwing knife
			case 1134://poison rune throwing knife
			case 1092://rune spear
			case 1140://poison rune spear
				return 7;
			case 785://lit arrow (not stackable, why not?)
				return 10;
			default:
				return 0;
		}
	}


	public static double getMiningFailPercent(double curLvl, double reqLvl)
	{
		double dif = curLvl - reqLvl; // Get difference
		return (3.27 * Math.pow(10, -6)) * Math.pow(dif, 4) + (-5.516 * Math.pow(10, -4)) * Math.pow(dif, 3) + 0.014307 * Math.pow(dif, 2) + 1.65560813 * dif + 18.2095966;
		//(3.27 * 10^-6)x^4 + (-5.516 * 10^-4)x^3 + 0.014307x^2 + 1.65560813x + 18.2095966'
	}
	/**
	 * Calculates what one mob should hit on another with range
	 */
	public static int calcRangeHit(int rangeLvl, int rangeEquip, int armourEquip, int arrowID) {
		int armourRatio = (int)(60D + ((double)((rangeEquip * 3D) - armourEquip) / 300D) * 40D);

		if(DataConversions.random(0, 100) > armourRatio && DataConversions.random(0, 1) == 0) {
			return 0;
		}

		int max = (int)(((double)rangeLvl * 0.15D) + 0.85D + arrowPower(arrowID));
		int peak = (int)(((double)max / 100D) * (double)armourRatio);
		int dip = (int)(((double)peak / 3D) * 2D);
		return DataConversions.randomWeighted(0, dip, peak, max);
	}

	public static int calcGodSpells(Mob attacker, Mob defender) {
		if(attacker instanceof Player){
			Player owner = (Player)attacker;
		int newAtt = (int)((owner.getMagicPoints())+owner.getCurStat(6));
		int newDef = (int)((addPrayers(defender.isPrayerActivated(0), defender.isPrayerActivated(3), defender.isPrayerActivated(9)) * defender.getDefense()/4D) + (defender.getArmourPoints() / 4D));
		int hitChance = DataConversions.random(0, 150 + (newAtt - newDef));
		//int hitChance = (int)(50D + (double)owner.getMagicPoints() - newDef);

		if(hitChance > (defender instanceof Npc ? 50 : 60)) {
			int max = owner.isCharged() ? Rand(15, 25) : Rand(0, 10);
			int maxProb = 5; // 5%
			int nearMaxProb = 10; // 10%
			int avProb = 80; // 80%
			int lowHit = 5; // 5%

			// Probablities are shifted up/down based on armour
			int shiftValue = (int)Math.round(defender.getArmourPoints() * 0.02D);
			maxProb -= shiftValue;
			nearMaxProb -= (int)Math.round(shiftValue * 1.5);
			avProb -= (int)Math.round(shiftValue * 2.0);
			lowHit += (int)Math.round(shiftValue * 3.5);

			int hitRange = DataConversions.random(0, 100);

			if(hitRange >= (100 - maxProb)) {
				return max;
			}
			else if(hitRange >= (100 - nearMaxProb)) {
				return DataConversions.roundUp(Math.abs((max - (max * (DataConversions.random(0, 10) * 0.01D)))));
			}
			else if(hitRange >= (100 - avProb)) {
				int newMax = (int)DataConversions.roundUp((max - (max * 0.1D)));
				return DataConversions.roundUp(Math.abs((newMax - (newMax * (DataConversions.random(0, 50) * 0.01D)))));
			}
			else {
				int newMax = (int)DataConversions.roundUp((max - (max * 0.5D)));
				return DataConversions.roundUp(Math.abs((newMax - (newMax * (DataConversions.random(0, 95) * 0.01D)))));
			}
		}
	}
	return 0;
}

	public static int styleBonus(Mob mob, int skill) {
		int style = mob.getCombatStyle();
		if(style == 0) {
			return 1;
		}
		return (skill == 0 && style == 2) || (skill == 1 && style == 3) || (skill == 2 && style == 1) ? 3 : 0;
	}

	/**
	 * Calulates what one mob should hit on another with meelee
	 */
	public static int calcFightHit(Mob attacker, Mob defender) {
		int newAtt = (int)((addPrayers(attacker.isPrayerActivated(2), attacker.isPrayerActivated(5), attacker.isPrayerActivated(11)) * attacker.getAttack()) + (attacker.getWeaponAimPoints() / 4D) + styleBonus(attacker, 0));
		int newDef = (int)((addPrayers(defender.isPrayerActivated(0), defender.isPrayerActivated(3), defender.isPrayerActivated(9)) * defender.getDefense()) + (defender.getArmourPoints() / 4D) + styleBonus(attacker, 1));

		int hitChance = DataConversions.random(0, 100) + (newAtt - newDef);

		if(hitChance > (defender instanceof Npc ? 50 : 60)) {
			int max = maxHit(attacker.getStrength(), attacker.getWeaponPowerPoints(), attacker.isPrayerActivated(1), attacker.isPrayerActivated(4), attacker.isPrayerActivated(10), styleBonus(attacker, 2));

			int maxProb = 5; // 5%
			int nearMaxProb = 10; // 10%
			int avProb = 80; // 80%
			int lowHit = 5; // 5%

			// Probablities are shifted up/down based on armour
			int shiftValue = (int)Math.round(defender.getArmourPoints() * 0.02D);
			maxProb -= shiftValue;
			nearMaxProb -= (int)Math.round(shiftValue * 1.5);
			avProb -= (int)Math.round(shiftValue * 2.0);
			lowHit += (int)Math.round(shiftValue * 3.5);

			int hitRange = DataConversions.random(0, 100);

			if(hitRange >= (100 - maxProb)) {
				return max;
			}
			else if(hitRange >= (100 - nearMaxProb)) {
				return DataConversions.roundUp(Math.abs((max - (max * (DataConversions.random(0, 10) * 0.01D)))));
			}
			else if(hitRange >= (100 - avProb)) {
				int newMax = (int)DataConversions.roundUp((max - (max * 0.1D)));
				return DataConversions.roundUp(Math.abs((newMax - (newMax * (DataConversions.random(0, 50) * 0.01D)))));
			}
			else {
				int newMax = (int)DataConversions.roundUp((max - (max * 0.5D)));
				return DataConversions.roundUp(Math.abs((newMax - (newMax * (DataConversions.random(0, 95) * 0.01D)))));
			}
		}
		return 0;
	}

	/**
	 * Should the web be cut?
	 */
	public static boolean cutWeb() {
		return DataConversions.random(0, 4) != 0;
	}

	/**
	 * Calculates what a spell should hit based on its strength and the magic equipment stats of the caster
	 */
	public static int calcSpellHit(int spellStr, int magicEquip) {
		int mageRatio = (int)(50D + (double)magicEquip);
		int max = (int)(((double)spellStr / 100D) * 70) + 1;
		int peak = (int)(((double)spellStr / 100D) * (double)mageRatio);
		int dip = (int)((peak / 3D) * 2D);
		return DataConversions.randomWeighted(0, dip, peak, max);
	}

	/**
	 * Decide if the food we are cooking should be burned or not
	 */
	public static boolean burnFood(int foodId, int cookingLevel) {
		int levelDiff = cookingLevel - EntityHandler.getItemCookingDef(foodId).getReqLevel();
		if(levelDiff < 0) {
			return true;
		}
		if(levelDiff >= 20) {
			return false;
		}
		return DataConversions.random(0, levelDiff + 1) == 0;
	}

	private static int offsetToPercent(int levelDiff) {
		return levelDiff > 40 ? 70 : 30 + levelDiff;
	}

	/**
	 * Decide what fish, if any, we should get from the water
	 */
	public static ObjectFishDef getFish(int waterId, int fishingLevel, int click) {
		ArrayList<ObjectFishDef> fish = new ArrayList<ObjectFishDef>();
		for(ObjectFishDef def : EntityHandler.getObjectFishingDef(waterId, click).getFishDefs()) {
			if(fishingLevel >= def.getReqLevel()) {
				fish.add(def);
			}
		}
		if(fish.size() <= 0) {
			return null;
		}
		ObjectFishDef thisFish = fish.get(DataConversions.random(0, fish.size() - 1));
		int levelDiff = fishingLevel - thisFish.getReqLevel();
		if(levelDiff < 0) {
			return null;
		}
		return DataConversions.percentChance(offsetToPercent(levelDiff)) ? thisFish : null;
	}

	/**
	 * Should we get a log from the tree?
	 */
	public static boolean getLog(ObjectWoodcuttingDef def, int woodcutLevel, int axeId) {
		int levelDiff = woodcutLevel - def.getReqLevel();
		if(levelDiff < 0) {
			return false;
		}
		switch(axeId) {
			case 87:
				levelDiff += 0;
				break;
			case 12:
				levelDiff += 2;
				break;
			case 428:
				levelDiff += 4;
				break;
			case 88:
				levelDiff += 6;
				break;
			case 203:
				levelDiff += 8;
				break;
			case 204:
				levelDiff += 10;
				break;
			case 405:
				levelDiff += 12;
				break;
		}
		if(def.getReqLevel() == 1 && levelDiff >= 40) {
			return true;
		}
		return DataConversions.percentChance(offsetToPercent(levelDiff));
	}

	/**
	 * Should we can get an ore from the rock?
	 */
	public static boolean getOre(ObjectMiningDef def, int miningLevel, int axeId) {
		int levelDiff = miningLevel - def.getReqLevel();
		if(levelDiff < 0) {
			return false;
		}
		int bonus = 0;
		switch(axeId) {
			case 156:
				bonus = 0;
				break;
			case 1258:
				bonus = 2;
				break;
			case 1259:
				bonus = 6;
				break;
			case 1260:
				bonus = 8;
				break;
			case 1261:
				bonus = 10;
				break;
			case 1262:
				bonus = 12;
				break;
		}
		return DataConversions.percentChance(offsetToPercent(levelDiff + bonus));
	}

	/**
	 * Check what height we are currently at on the map
	 */
	public static int getHeight(int y) {
		return (int)(y / 944);
	}

	/**
	 * Check what height we are currently at on the map
	 */
	public static int getHeight(Point location) {
		return getHeight(location.getY());
	}

	/**
	 *  Calculate the max hit possible with the given stats
	 */
	public static int maxHit(int strength, int weaponPower, boolean burst, boolean superhuman, boolean ultimate, int bonus) {
		double newStrength = (double)((strength * addPrayers(burst, superhuman, ultimate)) + bonus);
		return (int)(newStrength * (((double)weaponPower * 0.00175D) + 0.1D) + 1.05D);
	}
	
	/**
	 * Gets the dragon fire damage
	 */
	public static int getDragonFire(int strength) {
		return (int)(strength * 0.10175D + 1.05D);
	}

	/**
	 * Check what level the given experience corresponds to
	 */
	public static int experienceToLevel(int exp) {
		for(int level = 0; level < 98; level++) {
			if(exp >= experienceArray[level]) {
				continue;
			}
			return (level + 1);
		}
		return 99;
	}
		public static int lvlToXp(int level) {
			return eArray[level];
	}

	/**
	 * Calculate a mobs combat level based on their stats
	 */
	public static int getCombatlevel(int[] stats) {
		return getCombatLevel(stats[0], stats[1], stats[2], stats[3], stats[6], stats[5], stats[4]);
	}

	/**
	 * Calculate a mobs combat level based on their stats
	 */
	public static int getCombatLevel(int att, int def, int str, int hits, int magic, int pray, int range) {
		double attack = att + str;
		double defense = def + hits;
		double mage = pray + magic;
		mage /= 8D;

		if(attack < ((double)range * 1.5D)) {
			return (int)((defense / 4D) + ((double)range * 0.375D) + mage);
		}
		else {
			return (int)((attack / 4D) + (defense / 4D) + mage);
		}
	}

}