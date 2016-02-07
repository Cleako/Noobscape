package org.rscdaemon.server.model;

import org.rscdaemon.server.event.*;
import org.rscdaemon.server.states.Action;
import org.rscdaemon.server.states.CombatState;
import org.rscdaemon.server.util.Logger;
import org.rscdaemon.server.model.Player;

import java.util.HashMap;

public abstract class Mob extends Entity {

	private int[][] mobSprites = new int[][]{
			{3, 2, 1},
			{4, -1, 0},
			{5, 6, 7}
	};
	protected int mobSprite = 1;
	/**
	 * Have we moved since last update?
	 */
	protected boolean hasMoved;
	/**
	 * Our combat level
	 */
	protected int combatLevel = 3;
	/**
	 * Has our appearance changed since last update?
	 */
	protected boolean ourAppearanceChanged = true;
	/**
	 * ID for our current appearance, used client side to detect changed
	 */
	protected int appearanceID = 0;
	/**
	 * Time of last movement, used for timeout
	 */
	protected long lastMovement = System.currentTimeMillis();
	/**
	 * If we are warned to move
	 */
	protected boolean warnedToMove = false;
	/**
	 * Used to block new requests when we are in the middle of one
	 */
	private boolean busy = false;
	/**
	 * Has the sprite changed?
	 */
	private boolean spriteChanged = false;
	/**
	 * Who they are in combat with
	 */
	private Mob combatWith = null;
	/**
	 * Timer used to track start and end of combat
	 */
	private long combatTimer = 0;
	/**
	 * The path we are walking
	 */
	private PathHandler pathHandler = new PathHandler(this);
	/**
	 * Amount of damage last dealt to the player
	 */
	private int lastDamage = 0;
	/**
	 * Tiles around us that we can see
	 */
	protected ViewArea viewArea = new ViewArea(this);
	/**
	 * Prayers that are currently turned on
	 */
	protected boolean[] activatedPrayers = new boolean[14];
	/**
	 * How many times we have hit our opponent
	 */
	private int hitsMade = 0;
	/**
	 * Set when the mob has been destroyed to alert players
	 */
	protected boolean removed = false;
	/**
	 * The end state of the last combat encounter
	 */
	private CombatState lastCombatState = CombatState.WAITING;
	/**
	 * Kill Stealing
	 */
	protected HashMap<Long, Integer> totalDamageTable = new HashMap();
	protected HashMap<Long, Integer> meleeDamageTable = new HashMap();
	protected HashMap<Long, Integer> rangeDamageTable = new HashMap();

	public void updateKillStealing(long index, int damage, int attackType) {
		if(totalDamageTable.containsKey(Long.valueOf(index)))
			totalDamageTable.put(Long.valueOf(index), Integer.valueOf(((Integer)totalDamageTable.get(Long.valueOf(index))).intValue() + damage));
		else {
			totalDamageTable.put(Long.valueOf(index), Integer.valueOf(damage));
		}
		switch(attackType) {
			case 0:
				if(meleeDamageTable.containsKey(Long.valueOf(index))) {
					meleeDamageTable.put(Long.valueOf(index), Integer.valueOf(((Integer)meleeDamageTable.get(Long.valueOf(index))).intValue() + damage));
					return;
				}
				meleeDamageTable.put(Long.valueOf(index), Integer.valueOf(damage));
				break;
			case 1:
				if(rangeDamageTable.containsKey(Long.valueOf(index))) {
					rangeDamageTable.put(Long.valueOf(index), Integer.valueOf(((Integer)rangeDamageTable.get(Long.valueOf(index))).intValue() + damage));
					return;
				}
				rangeDamageTable.put(Long.valueOf(index), Integer.valueOf(damage));
			case 2:
		}
	}
	
	public boolean isRemoved() {
		return removed;
	}
	
	public abstract void remove();
	
	public int getHitsMade() {
		return hitsMade;
	}

	public void incHitsMade() {
		hitsMade++;
	}
	
	public abstract int getCombatStyle();
	public abstract int getHits();
	public abstract int getAttack();
	public abstract int getDefense();
	public abstract int getStrength();
	public abstract void setHits(int lvl);
	public abstract void setAttack(int lvl);
	public abstract void setStrength(int lvl);
	public abstract void setDefense(int lvl);
	public abstract void killedBy(Mob mob, boolean stake);
	public abstract int getWeaponPowerPoints();
	public abstract int getWeaponAimPoints();
	public abstract int getArmourPoints();
	
	public void resetCombat(CombatState state) {
		for(DelayedEvent event : world.getDelayedEventHandler().getEvents()) {
			if(event instanceof FightEvent) {
				FightEvent fighting = (FightEvent)event;
				if(fighting.getOwner().equals(this) || fighting.getAffectedMob().equals(this)) {
					fighting.stop();
			      		break;
				}
			}
			else if(event instanceof DuelEvent) {
				DuelEvent dueling = (DuelEvent)event;
				if(dueling.getOwner().equals(this) || dueling.getAffectedPlayer().equals(this)) {
					dueling.stop();
			      		break;
				}
			}
		}
      		setBusy(false);
      		setSprite(4);
      		setOpponent(null);
      		setCombatTimer();
      		hitsMade = 0;
      		if(this instanceof Player) {
      			Player player = (Player)this;
      			player.setStatus(Action.IDLE);
      		}
      		lastCombatState = state;
	}
	
	public CombatState getCombatState() {
		return lastCombatState;
	}
	
	public boolean isPrayerActivated(int pID) {
		return activatedPrayers[pID];
	}
	
	public void setPrayer(int pID, boolean b) {
		activatedPrayers[pID] = b;
	}
	
	public ViewArea getViewArea() {
		return viewArea;
	}
	
	public int getLastDamage() {
		return lastDamage;
	}
	
	public void setLastDamage(int d) {
		lastDamage = d;
	}
	
	public boolean isBusy() {
		return busy;
	}
	
	public boolean inDuel = false;
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	public void warnToMove() {
		warnedToMove = true;
	}

	public boolean warnedToMove() {
		return warnedToMove;
	}
	
	public void setLastMoved() {
		lastMovement = System.currentTimeMillis();
	}

	public long getLastMoved() {
		return lastMovement;
	}

	public int getCombatLevel() {
		return combatLevel;
	}

	public void setCombatLevel(int level) {
		combatLevel = level;
		ourAppearanceChanged = true;
	}
	
	public void setAppearnceChanged(boolean b) {
		ourAppearanceChanged = b;
	}
	
	public void updateAppearanceID() {
		if(ourAppearanceChanged) {
			appearanceID++;
		}
	}
	
	public int getAppearanceID() {
		return appearanceID;
	}

	public void setLocation(Point p) {
		setLocation(p, false);
	}

	public void setLocation(Point p, boolean teleported) {
		if (!teleported) {
			updateSprite(p);
			hasMoved = true;
		}
		setLastMoved();
		warnedToMove = false;
		super.setLocation(p);
	}

	protected void updateSprite(Point newLocation) {
		try {
			int xIndex = getLocation().getX() - newLocation.getX() + 1;
			int yIndex = getLocation().getY() - newLocation.getY() + 1;
			setSprite(mobSprites[xIndex][yIndex]);
		}
		catch(Exception e) {
			Logger.error(e.getMessage());
		}
	}

	public int getSprite() {
		return mobSprite;
	}

	public void setSprite(int x) {
		spriteChanged = true;
		mobSprite = x;
	}
	
	public boolean spriteChanged() {
		return spriteChanged;
	}
	
	public void resetSpriteChanged() {
		spriteChanged = false;
	}
	
	public void setOpponent(Mob opponent) {
		combatWith = opponent;
	}
	
	public void setCombatTimer() {
		combatTimer = System.currentTimeMillis();
	}
	
	public long getCombatTimer() {
		return combatTimer;
	}
	
	public Mob getOpponent() {
		return combatWith;
	}
	
	public boolean inCombat() {
		return (mobSprite == 8 || mobSprite == 9) && combatWith != null;
	}

	public boolean hasMoved() {
		return hasMoved;
	}

	public void resetMoved() {
		hasMoved = false;
	}
	
	public boolean finishedPath() {
		return pathHandler.finishedPath();
	}
	
	public void updatePosition() {
		pathHandler.updatePosition();
	}
	
	public void resetPath() {
		pathHandler.resetPath();
	}

	public void setPath(Path path) {
		pathHandler.setPath(path);
	}
	
	public final boolean atObject(GameObject o) {
		int dir = o.getDirection();
      		int width, height;
      		if(o.getType() == 1) {
      			width = height = 1;
      		}
      		else if(dir == 0 || dir == 4) {
      			width = o.getGameObjectDef().getWidth();
      			height = o.getGameObjectDef().getHeight();
      		}
      		else {
      			height = o.getGameObjectDef().getWidth();
      			width = o.getGameObjectDef().getHeight();
      		}
		for(int x = 0;x < width;x++) {
			for(int y = 0;y < height;y++) {
				Point p = Point.location(o.getX() + x, o.getY() + y);
				int xDist = Math.abs(location.getX() - p.getX());
				int yDist = Math.abs(location.getY() - p.getY());
				int tDist = xDist + yDist;
				if(tDist <= 1) {
					return true;
				}
			}
		}
		return false;
	}

}
