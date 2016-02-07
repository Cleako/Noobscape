package org.rscdaemon.server.event;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.util.Formulae;
import org.rscdaemon.server.util.*;
import org.rscdaemon.server.states.CombatState;
import org.rscdaemon.server.entityhandling.defs.NPCDef;
import org.rscdaemon.server.model.Npc;

import java.util.ArrayList;

public class FightEvent extends DelayedEvent {
	private Mob affectedMob;
	private int hits;
	private int firstHit;
	
	public FightEvent(Player owner, Mob affectedMob) {
		this(owner, affectedMob, false);
	}
	
	public FightEvent(Player owner, Mob affectedMob, boolean attacked) {
		super(owner, 1000);
		this.affectedMob = affectedMob;
		firstHit = attacked ? 1 : 0;
		hits = 0;
	}
	
	public void run() {
		if(!owner.loggedIn() || (affectedMob instanceof Player && !((Player)affectedMob).loggedIn())) {
			owner.resetCombat(CombatState.ERROR);
			affectedMob.resetCombat(CombatState.ERROR);
			return;
		}
		Mob attacker, opponent;
		if(hits++ % 2 == firstHit) {
			attacker = owner;
			opponent = affectedMob;
		}
		else {
			attacker = affectedMob;
			opponent = owner;
		}
		if(opponent.getHits() <= 0) {
			attacker.resetCombat(CombatState.WON);
      			opponent.resetCombat(CombatState.LOST);
      			return;
		}
		attacker.incHitsMade();
		if(attacker instanceof Npc && opponent.isPrayerActivated(12)) {
			return;
		}					
		int damage = Formulae.calcFightHit(attacker, opponent);
		int hitsRemaining = opponent.getHits();
		if(damage > hitsRemaining) {
			damage = hitsRemaining;
		}
		if((attacker instanceof Player) && (opponent instanceof Npc)) {
			opponent.updateKillStealing(owner.getUsernameHash(), damage, 0);
		}
  		opponent.setLastDamage(damage);
  		int newHp = opponent.getHits() - damage;
  		opponent.setHits(newHp);
  		ArrayList<Player> playersToInform = new ArrayList<Player>();
  		playersToInform.addAll(opponent.getViewArea().getPlayersInView());
  		playersToInform.addAll(attacker.getViewArea().getPlayersInView());
		  		for(Player p : playersToInform) {
  			p.informOfModifiedHits(opponent);
		}
  		String combatSound = damage > 0 ? "combat1b" : "combat1a";
  		if(opponent instanceof Player) {
  			Player opponentPlayer = ((Player)opponent);
  			opponentPlayer.getActionSender().sendStat(3);
  			opponentPlayer.getActionSender().sendSound(combatSound);
		}
		if(attacker instanceof Player) {
			Player attackerPlayer = (Player)attacker;
			attackerPlayer.getActionSender().sendSound(combatSound);
		}
		for(Player pl : world.getPlayers()) {
			if((opponent.getID() == 47) && (pl.getLocation().onTutorialIsland())) {
				if(pl.getHits() <= 3) {
					pl.setHits(pl.getHits() + 3);
					pl.getActionSender().sendStat(3);
				} else if(newHp <= 0) {
					opponent.killedBy(attacker, false);
					pl.getActionSender().sendMessage("You have killed the rat!");
					pl.getActionSender().sendMessage("Please go speak to the combat instructor");
					pl.setTutorialStatus(22);
					pl.getActionSender().sendTutorialStatus();
					return;
				}
			}
		}
		for(Player player : world.getPlayers()) {
			if((opponent.getID() == 35) && (player.getDemonSlayerStatus() == 5) && (player.wielding(52)) && (newHp <= 0)) {
				opponent.killedBy(attacker, false);
				player.getActionSender().sendMessage("@gre@Congratulations@whi@! You have just completed the @or1@Demon Slayer@whi@ quest!");
				player.getActionSender().sendMessage("You gained @or1@3 @whi@quest points!");
				player.isDemonSlayerComplete();
				player.getActionSender().sendDemonSlayerComplete();
				player.setQuestPoints(player.getQuestPoints() + 3);
				player.getActionSender().sendQuestPoints();
				return;
			}
		} 
		if(newHp <= 0) {
  			opponent.killedBy(attacker, false);
  			if(attacker instanceof Player) {
  				Player attackerPlayer = (Player)attacker;
	      			int exp = DataConversions.roundUp(Formulae.combatExperience(opponent) / 4D);
	      			switch(attackerPlayer.getCombatStyle()) {
					case 0:
						for(int x = 0; x < 3; x++) {
							attackerPlayer.incExp(x, exp, true, true);
							attackerPlayer.getActionSender().sendStat(x);
							attackerPlayer.getActionSender().sendRemaining();
						}
						break;
					case 1:
						attackerPlayer.incExp(2, exp * 3, true, true);
						attackerPlayer.getActionSender().sendStat(2);
						attackerPlayer.getActionSender().sendRemaining();
						break;
					case 2:
						attackerPlayer.incExp(0, exp * 3, true, true);
						attackerPlayer.getActionSender().sendStat(0);
						attackerPlayer.getActionSender().sendRemaining();
						break;
					case 3:
						attackerPlayer.incExp(1, exp * 3, true, true);
						attackerPlayer.getActionSender().sendStat(1);
						attackerPlayer.getActionSender().sendRemaining();
						break;
				}
				attackerPlayer.setFatigue(attackerPlayer.getFatigue() + 1);
				attackerPlayer.getActionSender().sendFatigue();
				attackerPlayer.incExp(3, exp, true, true);
				attackerPlayer.getActionSender().sendStat(3);
  			}
  			attacker.resetCombat(CombatState.WON);
  			opponent.resetCombat(CombatState.LOST);
  		}
	}
	
	public Mob getAffectedMob() {
		return affectedMob;
	}
	
	public boolean equals(Object o) {
		if(o instanceof FightEvent) {
			FightEvent e = (FightEvent)o;
			return e.belongsTo(owner) && e.getAffectedMob().equals(affectedMob);
		}
		return false;
	}
}