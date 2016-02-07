package org.rscdaemon.server.model;

public class Projectile {
	/**
	 * Who fired the projectile
	 */
	private Mob caster;
	/**
	 * Who the projectile is being fired at
	 */
	private Mob victim;
	/**
	 * The type: 1 = magic, 2 = ranged
	 */
	private int type;

	public Projectile(Mob caster, Mob victim, int type) {
		this.caster = caster;
		this.victim = victim;
		this.type = type;
	}

	public Mob getCaster() {
		return caster;
	}
	
	public Mob getVictim() {
		return victim;
	}
	
	public int getType() {
		return type;
	}

}
