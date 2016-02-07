package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.event.WalkToMobEvent;
import org.rscdaemon.server.event.FightEvent;
import org.rscdaemon.server.event.RangeEvent;
import org.rscdaemon.server.util.Formulae;
import org.rscdaemon.server.states.*;

import org.apache.mina.common.IoSession;
import java.util.Iterator;
import java.util.ArrayList;

public class AttackHandler implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player)session.getAttachment();
		int pID = ((RSCPacket)p).getID();
		if(player.isBusy()) {
			player.resetPath();
			return;
		}
		player.resetAll();
		Mob affectedMob = null;
		int serverIndex = p.readShort();
		if(pID == 57) {
			affectedMob = world.getPlayer(serverIndex);
		}
		else if(pID == 73) {
			affectedMob = world.getNpc(serverIndex);
		}
  		if(affectedMob == null || affectedMob.equals(player)) { // This shouldn't happen
  			player.resetPath();
  			return;
  		}
		for(int dragonTrigger : Formulae.dragonIDs) {
			if(affectedMob.getID() == dragonTrigger) {
				player.getActionSender().sendMessage("The dragon breathes fire at you!");
				if(!(player.getInventory().wielding(420))) { // Not wielding the anti fire shield
					int damage = Formulae.getDragonFire(affectedMob.getStrength());
					int newHP = player.getHits() - damage;
					ArrayList playersToInform = new ArrayList();
					player.setHits(newHP);
					player.setLastDamage(damage);
					playersToInform.addAll(player.getViewArea().getPlayersInView());
					Player pl;
					for(Iterator drag = world.getPlayers().iterator(); drag.hasNext();) {
						pl = (Player)drag.next();
						pl.informOfModifiedHits(player);
					}
					player.getActionSender().sendStat(3);
					if(newHP <= 0) {
						player.killedBy(affectedMob, false);
						affectedMob.resetCombat(CombatState.WON);
						player.resetCombat(CombatState.LOST);
						affectedMob.resetPath();
						return;
					}
					break;
				}
				player.getActionSender().sendMessage("Your shield prevents some of the damage from the flames!");
			}
		}		
		if((affectedMob.getID() == 35) && (player.getInventory().wielding(52))) {
			player.getActionSender().sendMessage("The silverlight slightly weakens the demon.");
		}
  		//player.setFollowing(affectedMob);
  		player.setStatus(Action.ATTACKING_MOB);
  		if(player.getRangeEquip() < 0) {
  			world.getDelayedEventHandler().add(new WalkToMobEvent(player, affectedMob, 2) {
				public void arrived() {
					owner.resetPath();
					if(owner.isBusy() || affectedMob.isBusy() || !owner.nextTo(affectedMob) || !owner.checkAttack(affectedMob, false) || owner.getStatus() != Action.ATTACKING_MOB) {
						return;
					}
					owner.resetAll();
					owner.setStatus(Action.FIGHTING_MOB);
					if(affectedMob instanceof Player) {
						Player affectedPlayer = (Player)affectedMob;
						owner.setSkulledOn(affectedPlayer);
					      	affectedPlayer.resetAll();
					      	affectedPlayer.setStatus(Action.FIGHTING_MOB);
					      	affectedPlayer.getActionSender().sendMessage("You are under attack!");
			      		}
			      		affectedMob.resetPath();
			      		
			      		owner.setLocation(affectedMob.getLocation(), true);
		      	      		for(Player p : owner.getViewArea().getPlayersInView()) {
		      	      			p.removeWatchedPlayer(owner);
		      	      		}
			    		
			      		owner.setBusy(true);
			      		owner.setSprite(9);
			      		owner.setOpponent(affectedMob);
			      		owner.setCombatTimer();
			      		affectedMob.setBusy(true);
			      		affectedMob.setSprite(8);
			      		affectedMob.setOpponent(owner);
			      		affectedMob.setCombatTimer();
			      		FightEvent fighting = new FightEvent(owner, affectedMob);
			      		fighting.setLastRun(0);
			      		world.getDelayedEventHandler().add(fighting);
				}
  			});
  		}
  		else {
  			world.getDelayedEventHandler().add(new WalkToMobEvent(player, affectedMob, 5) {
				public void arrived() {
					owner.resetPath();
					if(owner.isBusy() || !owner.checkAttack(affectedMob, true) || owner.getStatus() != Action.ATTACKING_MOB) {
						return;
					}
					owner.resetAll();
					owner.setStatus(Action.RANGING_MOB);
			      		if(affectedMob instanceof Player) {
						Player affectedPlayer = (Player)affectedMob;
						owner.setSkulledOn(affectedPlayer);
						affectedPlayer.resetTrade();
						if(affectedPlayer.getMenuHandler() != null) {
							affectedPlayer.resetMenuHandler();
						}
						if(affectedPlayer.accessingBank()) {
							affectedPlayer.resetBank();
						}
						if(affectedPlayer.accessingShop()) {
							affectedPlayer.resetShop();
						}
						if(affectedPlayer.getNpc() != null) {
							affectedPlayer.getNpc().unblock();
							affectedPlayer.setNpc(null);
						}
			      		}
			      		if(affectedMob.isPrayerActivated(13)) {
			      			owner.getActionSender().sendMessage("Your missiles are blocked");
			      			return;
			      		}
			      		owner.setRangeEvent(new RangeEvent(owner, affectedMob));
				}
  			});
  		}
	}
}