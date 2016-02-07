package org.rscdaemon.server.model;

import org.rscdaemon.server.entityhandling.EntityHandler;
import org.rscdaemon.server.entityhandling.defs.DoorDef;
import org.rscdaemon.server.entityhandling.defs.GameObjectDef;
import org.rscdaemon.server.entityhandling.locs.GameObjectLoc;

public class GameObject extends Entity {
	/**
	 * The direction the object points in
	 */
	private int direction;
	/**
	 * The type of object
	 */
	private int type;

	private boolean grainable = false;
	/**
	 * Location definition of the object
	 */
	private GameObjectLoc loc = null;
	/**
	 * Set when the item has been destroyed to alert players
	 */
	private boolean removed = false;
	
	public GameObject(GameObjectLoc loc) {
		direction = loc.direction;
		type = loc.type;
		this.loc = loc;
		super.setID(loc.id);
		super.setLocation(Point.location(loc.x, loc.y));
	}

	public void setGrainable(boolean b){
		grainable=b;
	}
	public boolean getGrainable(){
		return grainable;
	}
	
	public GameObject(Point location, int id, int direction, int type) {
		this(new GameObjectLoc(id, location.getX(), location.getY(), direction, type));
	}
	
	public boolean isRemoved() {
		return removed;
	}
	
	public void remove() {
		removed = true;
	}
	
	public GameObjectLoc getLoc() {
		return loc;
	}
	
	public GameObjectDef getGameObjectDef() {
		return EntityHandler.getGameObjectDef(super.getID());
	}
	
	public DoorDef getDoorDef() {
		return EntityHandler.getDoorDef(super.getID());
	}
	
	public boolean isTelePoint() {
		return EntityHandler.getObjectTelePoint(getLocation(), null) != null;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public boolean equals(Object o) {
		if(o instanceof GameObject) {
			GameObject go = (GameObject)o;
			return go.getLocation().equals(getLocation()) && go.getID() == getID() && go.getDirection() == getDirection() && go.getType() == getType();
		}
		return false;
	}
	
	public boolean isOn(int x, int y) {
      		int width, height;
      		if(type == 1) {
      			width = height = 1;
      		}
      		else if(direction == 0 || direction == 4) {
      			width = getGameObjectDef().getWidth();
      			height = getGameObjectDef().getHeight();
      		}
      		else {
      			height = getGameObjectDef().getWidth();
      			width = getGameObjectDef().getHeight();
      		}
		if(type == 0) { // Object
			return x >= getX() && x <= (getX() + width) && y >= getY() && y <= (getY() + height);
		}
		else { // Door
			return x == getX() && y == getY();
		}
	}
	
	public String toString() {
		return (type == 0 ? "GameObject" : "WallObject") + ":id = " + id + "; dir = " + direction + "; location = " + location.toString() + ";";
	}
	
}
