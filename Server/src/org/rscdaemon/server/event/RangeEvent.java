package org.rscdaemon.server.event;

import org.rscdaemon.server.GameVars;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Mob;
import org.rscdaemon.server.model.Item;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.Projectile;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.util.Formulae;
import org.rscdaemon.server.event.FightEvent;
import org.rscdaemon.server.states.Action;
import org.rscdaemon.server.event.NpcWalkEvent;

import java.util.ArrayList;

public class RangeEvent extends DelayedEvent {
	private Mob affectedMob;
	private boolean firstRun = true;
	
	public RangeEvent(Player owner, Mob affectedMob) {
		super(owner, GameVars.rangedDelaySpeed);
		this.affectedMob = affectedMob;
	}
	
	private Item getArrows(int id) {
		for(Item i : world.getTile(affectedMob.getLocation()).getItems()) {
			if(i.getID() == id && i.visibleTo(owner) && !i.isRemoved()) {
				return i;
			}
		}
		return null;
	}
	
	public static void rangePlayer(int rangeLevel, int arrowID, int rangePoints, Npc affectedMob, Player owner) {
	
		//NPC fights back
		int damage = 0;
		try {
			damage = Formulae.calcRangeHit(owner.getCurStat(4), rangePoints, owner.getArmourPoints(), arrowID);
		} catch(NullPointerException npe) {
			return;
		}
		Projectile projectile = new Projectile(owner, affectedMob, 2);
		Projectile projectile2 = new Projectile(affectedMob, owner, 2);
		owner.setLastDamage(damage);
		int newHp = owner.getCurStat(3) - damage;
		owner.setCurStat(3, newHp);
		ArrayList<Player> playersToInform = new ArrayList<Player>();
		playersToInform.addAll(owner.getViewArea().getPlayersInView());
		playersToInform.addAll(affectedMob.getViewArea().getPlayersInView());
		for(Player p : playersToInform)
		{
			p.informOfProjectile(projectile);
			p.informOfProjectile(projectile2);
			p.informOfModifiedHits(((Player)owner));
		}
		((Player)owner).getActionSender().sendStat(3);
			
			
		if (newHp <= 0)
		{
			owner.killedBy(owner);
		}
	}
	
	public void run() {
		int bowID = owner.getRangeEquip();
		if(!owner.loggedIn() || (affectedMob instanceof Player && !((Player)affectedMob).loggedIn()) || affectedMob.getHits() <= 0 || !owner.checkAttack(affectedMob, true) || bowID < 0) {
			owner.resetRange();
			return;
		}
		if(owner.withinRange(affectedMob, 5)) {
			if(owner.isFollowing()) {
				owner.resetFollowing();
			}
			if(!owner.finishedPath()) {
				owner.resetPath();
			}
		}
		else {
			owner.setFollowing(affectedMob);
			return;
		}
		boolean xbow = DataConversions.inArray(Formulae.xbowIDs, bowID);
		int arrowID = -1;
		for(int aID : (xbow ? Formulae.boltIDs : Formulae.arrowIDs)) {
			int slot = owner.getInventory().getLastIndexById(aID);
			if(slot < 0) {
				continue;
			}
			InvItem arrow = owner.getInventory().get(slot);
			if(arrow == null) { // This shouldn't happen
				continue;
			}
			arrowID = aID;
			int newAmount = arrow.getAmount() - 1;
			if(newAmount <= 0) {
				owner.getInventory().remove(slot);
				owner.getActionSender().sendInventory();
			}
			else {
				arrow.setAmount(newAmount);
				owner.getActionSender().sendUpdateItem(slot);
			}
			break;
		}
		if(arrowID < 0) {
			owner.getActionSender().sendMessage("You have run out of " + (xbow ? "bolts" : "arrows"));
			owner.resetRange();
			return;
		}
		if(affectedMob.isPrayerActivated(13)) {
			owner.getActionSender().sendMessage("Your missiles are blocked");
			owner.resetRange();
			return;
		}
		int damage = Formulae.calcRangeHit(owner.getCurStat(4), owner.getRangePoints(), affectedMob.getArmourPoints(), arrowID);
		if(!Formulae.looseArrow(damage)) {
			Item arrows = getArrows(arrowID);
			if(arrows == null) {
				world.registerItem(new Item(arrowID, affectedMob.getX(), affectedMob.getY(), 1, owner));
			}
			else {
				arrows.setAmount(arrows.getAmount() + 1);
			}
		}
		if(firstRun) {
			firstRun = false;
			if(affectedMob instanceof Player) {
				((Player)affectedMob).getActionSender().sendMessage(owner.getUsername() + " is shooting at you!");
			}
		}	
		if(affectedMob instanceof Npc && owner.withinRange(affectedMob, 6) && !affectedMob.isBusy() && !owner.isBusy()) {
			final Npc affectedNpc = (Npc)affectedMob;
			final Player victim = owner;
			if(victim != null) {
				world.getDelayedEventHandler().add(new NpcWalkEvent(owner, affectedNpc, 0) {
					public void arrived() {
						affectedNpc.resetPath();
						victim.resetPath();
						victim.resetAll();
						victim.setStatus(Action.FIGHTING_MOB);
						victim.getActionSender().sendMessage("You are under attack!");

						for(Player p : affectedNpc.getViewArea().getPlayersInView()) {
							p.removeWatchedNpc(affectedNpc);
						}

						victim.setBusy(true);
						victim.setSprite(9);
						victim.setOpponent(affectedNpc);
						victim.setCombatTimer();

						affectedNpc.setBusy(true);
						affectedNpc.setSprite(8);
						affectedNpc.setOpponent(victim);
						affectedNpc.setCombatTimer();
						FightEvent fighting = new FightEvent(victim, affectedNpc, true);
						fighting.setLastRun(0);
						world.getDelayedEventHandler().add(fighting);
					}
				});
			}
		}
		Projectile projectile = new Projectile(owner, affectedMob, 2);
  		affectedMob.setLastDamage(damage);
		affectedMob.updateKillStealing(owner.getUsernameHash(), damage, 1);
  		int newHp = affectedMob.getHits() - damage;
  		affectedMob.setHits(newHp);
  		ArrayList<Player> playersToInform = new ArrayList<Player>();
  		playersToInform.addAll(owner.getViewArea().getPlayersInView());
  		playersToInform.addAll(affectedMob.getViewArea().getPlayersInView());
  		for(Player p : playersToInform) {
  			p.informOfProjectile(projectile);
  			p.informOfModifiedHits(affectedMob);
  		}
  		if(affectedMob instanceof Player) {
  			Player affectedPlayer = (Player)affectedMob;
  			affectedPlayer.getActionSender().sendStat(3);
		}
		owner.getActionSender().sendSound("shoot");
		owner.setArrowFired();
  		if(newHp <= 0) {
  			affectedMob.killedBy(owner, false);
  			int exp = Formulae.combatExperience(affectedMob);
  			owner.incExp(4, exp, false, true);
  			owner.getActionSender().sendStat(4);
  			owner.resetRange();
  		}
	}
	
	public double getArrowMod(int arrowId) {
		double poisonDmgIncrease = 0.025;
		switch(arrowId) {
			case 11: return 1 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			case 574: return 1 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			case 638: return 1.05 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			case 639: return 1.05 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			case 640: return 1.1 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			case 641: return 1.1 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			case 642: return 1.2 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			case 643: return 1.2 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			case 786: return 1.3 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0); //pearl xbow bolts
			case 644: return 1.3 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			case 645: return 1.3 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			case 646: return 1.4 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			case 647: return 1.4 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
			default: return 1 + (isPoisioned(arrowId) ? poisonDmgIncrease : 0);
		}
	}

	public boolean isPoisioned(int arrowId) {
		switch(arrowId) {
			case 11: return false;
			case 574: return true;
			case 592: return true;
			case 638: return false;
			case 639: return true;
			case 640: return false;
			case 641: return true;
			case 642: return false;
			case 643: return true;
			case 644: return false;
			case 645: return true;
			case 646: return false;
			case 647: return true;
			default: return false;
		}
	}
	
	public Mob getAffectedMob() {
		return affectedMob;
	}
	
	public boolean equals(Object o) {
		if(o instanceof RangeEvent) {
			RangeEvent e = (RangeEvent)o;
			return e.belongsTo(owner);
		}
		return false;
	}
}