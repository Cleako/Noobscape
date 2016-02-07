package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.util.*;
import org.rscdaemon.server.event.WalkToMobEvent;
import org.rscdaemon.server.event.WalkToPointEvent;
import org.rscdaemon.server.states.Action;
import org.rscdaemon.server.entityhandling.EntityHandler;
import org.rscdaemon.server.entityhandling.defs.SpellDef;
import org.rscdaemon.server.entityhandling.defs.extras.ItemSmeltingDef;
import org.rscdaemon.server.entityhandling.defs.extras.ReqOreDef;
import org.rscdaemon.server.event.ObjectRemover;
import org.rscdaemon.server.states.CombatState;
import org.rscdaemon.server.event.*;
import org.rscdaemon.server.entityhandling.locs.*;

import org.apache.mina.common.IoSession;

import java.util.*;
import java.util.Map.Entry;

public class SpellHandler implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();
	private Random r = new Random();

	public final int Rand(int low, int high)
	{
       	return low + r.nextInt(high - low);
    }

	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player)session.getAttachment();
		int pID = ((RSCPacket)p).getID();
		if((player.isBusy() && !player.inCombat()) || player.isRanging()) {
			return;
		}
		if(player.isDueling() && player.getDuelSetting(1)) {
			player.getActionSender().sendMessage("Magic is disabled in this duel");
			return;
		}
		player.resetAllExceptDueling();
		int idx = p.readShort();
		if(idx < 0 || idx >= 52) {
			player.setSuspiciousPlayer(true);
			return;
		}
		if(!canCast(player)) {
			return;
		}
		SpellDef spell = EntityHandler.getSpellDef(idx);
		if(player.getCurStat(6) < spell.getReqLevel()) {
			player.setSuspiciousPlayer(true);
			player.getActionSender().sendMessage("Your magic ability is not high enough for this spell.");
			player.resetPath();
			return;
		}
		for(String p2pSpells : Formulae.p2pSpells) {
			if((spell.getSpellName() == p2pSpells) && (player.getLocation().wildernessLevel() > 0) && (!(world.getWildernessType()))) {
				player.getActionSender().sendMessage("You can't cast a @gre@P2P @whi@spell while the wilderness is @gre@F2P@whi@.");
				player.resetPath();
				return;
			}
		}
		if(!Formulae.castSpell(spell, player.getCurStat(6), player.getMagicPoints())) {
			player.getActionSender().sendMessage("The spell fails, you may try again in 20 seconds.");
			player.getActionSender().sendSound("spellfail");
			player.setSpellFail();
			player.resetPath();
			return;
		}
		switch(pID) {
			case 206: // Cast on self
				if(player.isDueling()) {
					player.getActionSender().sendMessage("This type of spell cannot be used in a duel.");
					return;
				}
				if(spell.getSpellType() == 0) {
					handleTeleport(player, spell, idx);
				}
				//if(spell.getSpellType() == 6) {
					handleGroundCast(player, spell, idx);
				//}
				break;
			case 55: // Cast on player
				if(spell.getSpellType() == 1 || spell.getSpellType() == 2) {
					Player affectedPlayer = world.getPlayer(p.readShort());
					if(affectedPlayer == null) { // This shouldn't happen
						player.resetPath();
						return;
					}
					if(player.withinRange(affectedPlayer, 5)) {
						player.resetPath();
					}
					handleMobCast(player, affectedPlayer, idx);
				}
			//					if(spell.getSpellType() == 6) {
			//						handleGroundCast(player, spell);
			//	}
				break;
			case 71: // Cast on npc
				if(spell.getSpellType() == 2) {
					Npc affectedNpc = world.getNpc(p.readShort());
					if(affectedNpc == null) { // This shouldn't happen
						player.resetPath();
						return;
					}
					if(player.withinRange(affectedNpc, 5)) {
						player.resetPath();
					}
					handleMobCast(player, affectedNpc, idx);
				}
			//					if(spell.getSpellType() == 6) {
			//						handleGroundCast(player, spell);
			//	}
				break;
			case 49: // Cast on inventory item
				if(player.isDueling()) {
					player.getActionSender().sendMessage("This type of spell cannot be used in a duel.");
					return;
				}
				if(spell.getSpellType() == 3) {
					InvItem item = player.getInventory().get(p.readShort());
					if(item == null) { // This shoudln't happen
						player.resetPath();
						return;
					}
					handleInvItemCast(player, spell, idx, item);
				}
			//					if(spell.getSpellType() == 6) {
			//						handleGroundCast(player, spell);
			//	}
				break;
			case 67: // Cast on door - type 4
				if(player.isDueling()) {
					player.getActionSender().sendMessage("This type of spell cannot be used in a duel.");
					return;
				}
				player.getActionSender().sendMessage("@or1@This type of spell is not yet implemented.");
			//					if(spell.getSpellType() == 6) {
			//						handleGroundCast(player, spell);
			//	}
				break;
			case 17: // Cast on game object - type 5
				if(player.isDueling()) {
					player.getActionSender().sendMessage("This type of spell cannot be used in a duel.");
					return;
				}
				player.getActionSender().sendMessage("@or1@This type of spell is not yet implemented.");
						//		if(spell.getSpellType() == 6) {
						//			handleGroundCast(player, spell);
			//	}
				break;
			case 104: // Cast on ground item
				if(player.isDueling()) {
					player.getActionSender().sendMessage("This type of spell cannot be used in a duel.");
					return;
				}
				ActiveTile t = world.getTile(p.readShort(), p.readShort());
				int itemId = p.readShort();
				Item affectedItem = null;
				for(Item i : t.getItems()) {
					if(i.getID() == itemId) {
						affectedItem = i;
						break;
					}
				}
				if(affectedItem == null) { // This shouldn't happen
					return;
				}
				handleItemCast(player, spell, idx, affectedItem);
				break;
			case 232: // Cast on ground - type 6
				if(player.isDueling()) {
					player.getActionSender().sendMessage("This type of spell cannot be used in a duel.");
					return;
				}
				//if(spell.getSpellType() == 6) {
					handleGroundCast(player, spell, idx);
				//}
				break;
		}
		player.getActionSender().sendInventory();
		player.getActionSender().sendStat(6);
	}

	private void handleMobCast(Player player, Mob affectedMob, final int spellID) {
	if (System.currentTimeMillis() - affectedMob.getCombatTimer() < 1000 && affectedMob.getCombatState() == CombatState.RUNNING || affectedMob.getCombatState() == CombatState.WAITING && System.currentTimeMillis() - affectedMob.getCombatTimer() < 1000)
	{
			player.resetPath();
  			return;
  		}
		if(!player.isBusy()) {
			player.setFollowing(affectedMob);
		}
		player.setStatus(Action.CASTING_MOB);
		world.getDelayedEventHandler().add(new WalkToMobEvent(player, affectedMob, 5) {
			public void arrived() {
				owner.resetPath();
				SpellDef spell = EntityHandler.getSpellDef(spellID);
				if(!canCast(owner) || affectedMob.getHits() <= 0 || !owner.checkAttack(affectedMob, true) || owner.getStatus() != Action.CASTING_MOB) {
					return;
				}
				owner.resetAllExceptDueling();
				switch(spellID) {



					case 19: // Crumble undead
						owner.getActionSender().sendMessage("@or1@This spell is not yet implemented.");
						break;




					case 25:
				boolean flagispro = false;
					ListIterator<?> iterator22 = owner.getInventory().iterator();
			        for(int slot = 0; iterator22.hasNext(); slot++)
			        {
			            InvItem cape = (InvItem)iterator22.next();
						if(cape.getID() == 1000 && cape.isWielded()) {flagispro=flagispro||true;}
					//	else {flag = false;}
					}
					if(flagispro){
						if(!owner.isCharged()){owner.getActionSender().sendMessage("@red@You are not charged!");}

						if(!checkAndRemoveRunes(owner, spell)) {
							return;
						}
						if(affectedMob instanceof Player && !owner.isDueling()) {
							Player affectedPlayer = (Player)affectedMob;
							owner.setSkulledOn(affectedPlayer);
				      		}
						
						int damag = Formulae.calcSpellHit(20, owner.getMagicPoints());
						if(affectedMob instanceof Player) {
							Player affectedPlayer = (Player)affectedMob;
							affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " is shooting at you!");
						}
				      		Projectile projectil = new Projectile(owner, affectedMob, 4);
				      		godSpellObject(affectedMob, 35);
				      		affectedMob.setLastDamage(damag);
				      		int newhp = affectedMob.getHits() - damag;
				      		affectedMob.setHits(newhp);
				      		ArrayList<Player> playersToInfor = new ArrayList<Player>();
				      		playersToInfor.addAll(owner.getViewArea().getPlayersInView());
				      		playersToInfor.addAll(affectedMob.getViewArea().getPlayersInView());
				      		for(Player p : playersToInfor) {
				      			p.informOfProjectile(projectil);
				      			p.informOfModifiedHits(affectedMob);
				      		}
				      		if(affectedMob instanceof Player) {
				      			Player affectedPlayer = (Player)affectedMob;
							affectedPlayer.getActionSender().sendStat(3);
						}
				      		if(newhp <= 0) {
				      			affectedMob.killedBy(owner, owner.isDueling());
				      		}
							owner.getActionSender().sendInventory();
							owner.getActionSender().sendStat(6);
				      		finalizeSpell(owner, spell);
						break;
					}
					else{owner.getActionSender().sendMessage("You need to be wearing the Iban Staff to cast this spell!");return;}
					case 33:
					int max = owner.isCharged() ? Rand(0, 25) : Rand(0, 10);
				boolean flag = false;
					ListIterator<?> iterator = owner.getInventory().iterator();
			        for(int slot = 0; iterator.hasNext(); slot++)
			        {
			            InvItem cape = (InvItem)iterator.next();
					int[] guthixCape = {1215};
					int[] guthixStaff = {1217};
					for(int i = 0; i < 1; i++) {
						if(owner.wielding(guthixStaff[i]) && owner.wielding(guthixCape[i])) {flag=flag||true;}
					//	else {flag = false;}
						}
					}
					if(owner.getLocation().inMageArena() && owner.getGuthixSpellCast() >= 100) {
						owner.getActionSender().sendMessage("You don't need to cast this in the arena anymore.");
					} else
					if(owner.getLocation().inMageArena() && owner.getGuthixSpellCast() >= 0 && owner.getGuthixSpellCast() <= 99) {
						owner.getActionSender().sendMessage("You gained @or1@1 @whi@guthix casting point.");
						owner.setGuthixSpellCast(owner.getGuthixSpellCast() +1);
						owner.getActionSender().sendGuthixSpellCast();
					} else
					if(owner.getGuthixSpellCast() < 100) {
						owner.getActionSender().sendMessage("You need to learn this spell in the mage arena before casting it outside.");
						return;
					}
					if(flag){
						if(!owner.isCharged()){owner.getActionSender().sendMessage("@red@You are not charged!");}
						if(!checkAndRemoveRunes(owner, spell)) {
							return;
						}
						if(affectedMob instanceof Player && !owner.isDueling()) {
							Player affectedPlayer = (Player)affectedMob;
							owner.setSkulledOn(affectedPlayer);
				      		}

						
						int damag = Formulae.calcGodSpells(owner, affectedMob);
						//int damag = Formulae.calcSpellHit(max, owner.getMagicPoints());
						if(affectedMob instanceof Player) {
							Player affectedPlayer = (Player)affectedMob;
							affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " is shooting at you!");
						}
				      		Projectile projectil = new Projectile(owner, affectedMob, 1);
				      		godSpellObject(affectedMob, 33);
				      		affectedMob.setLastDamage(damag);
				      		int newhp = affectedMob.getHits() - damag;
				      		affectedMob.setHits(newhp);
				      		ArrayList<Player> playersToInfor = new ArrayList<Player>();
				      		playersToInfor.addAll(owner.getViewArea().getPlayersInView());
				      		playersToInfor.addAll(affectedMob.getViewArea().getPlayersInView());
				      		for(Player p : playersToInfor) {
				      			p.informOfProjectile(projectil);
				      			p.informOfModifiedHits(affectedMob);
				      		}
				      		if(affectedMob instanceof Player) {
				      			Player affectedPlayer = (Player)affectedMob;
							affectedPlayer.getActionSender().sendStat(3);
						}
				      		if(newhp <= 0) {
				      			affectedMob.killedBy(owner, owner.isDueling());
				      		}
							owner.getActionSender().sendInventory();
							owner.getActionSender().sendStat(6);
				      		finalizeSpell(owner, spell);
						break;
					}
					else{owner.getActionSender().sendMessage("You need to be wearing the Guthix staff & cape to cast this spell!");return;}

					case 34:
				boolean bool = false;
					ListIterator<?> iterat = owner.getInventory().iterator();
			        for(int slot = 0; iterat.hasNext(); slot++)
			        {
			            InvItem cape = (InvItem)iterat.next();
					int[] saradominCape = {1214};
					int[] saradominStaff = {1218};
					for(int i = 0; i < 1; i++) {
						if(owner.wielding(saradominStaff[i]) && owner.wielding(saradominCape[i])) {bool=bool||true;}
					//	else {bool = false;}
						}
					}
					if(owner.getLocation().inMageArena() && owner.getSaradominSpellCast() >= 100) {
						owner.getActionSender().sendMessage("You don't need to cast this in the arena anymore.");
					} else
					if(owner.getLocation().inMageArena() && owner.getSaradominSpellCast() >= 0 && owner.getSaradominSpellCast() <= 99) {
						owner.getActionSender().sendMessage("You gained @or1@1 @whi@saradomin casting point.");
						owner.setSaradominSpellCast(owner.getSaradominSpellCast() +1);
						owner.getActionSender().sendSaradominSpellCast();
					} else
					if(owner.getSaradominSpellCast() < 100) {
						owner.getActionSender().sendMessage("You need to learn this spell in the mage arena before casting it outside.");
						return;
					}
					if(bool){
						if(!owner.isCharged()){owner.getActionSender().sendMessage("@red@You are not charged!");}

						if(!checkAndRemoveRunes(owner, spell)) {
							return;
						}
						if(affectedMob instanceof Player && !owner.isDueling()) {
							Player affectedPlayer = (Player)affectedMob;
							owner.setSkulledOn(affectedPlayer);
				      		}
						
						//int damag = Rand(0, 25);
						int damag = Formulae.calcGodSpells(owner, affectedMob);
						if(affectedMob instanceof Player) {
							Player affectedPlayer = (Player)affectedMob;
							affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " is shooting at you!");
						}
				      		Projectile projectil = new Projectile(owner, affectedMob, 1);
				      		godSpellObject(affectedMob, 34);
				      		affectedMob.setLastDamage(damag);
				      		int newhp = affectedMob.getHits() - damag;
				      		affectedMob.setHits(newhp);
				      		ArrayList<Player> playersToInfor = new ArrayList<Player>();
				      		playersToInfor.addAll(owner.getViewArea().getPlayersInView());
				      		playersToInfor.addAll(affectedMob.getViewArea().getPlayersInView());
				      		for(Player p : playersToInfor) {
				      			p.informOfProjectile(projectil);
				      			p.informOfModifiedHits(affectedMob);
				      		}
				      		if(affectedMob instanceof Player) {
				      			Player affectedPlayer = (Player)affectedMob;
							affectedPlayer.getActionSender().sendStat(3);
						}
				      		if(newhp <= 0) {
				      			affectedMob.killedBy(owner, owner.isDueling());
				      		}
							owner.getActionSender().sendInventory();
							owner.getActionSender().sendStat(6);
				      		finalizeSpell(owner, spell);
						break;
					}
					else{owner.getActionSender().sendMessage("You need to be wearing the Saradomin staff & cape to cast this spell!");return;}

					case 35:

				boolean flag2 = false;
					ListIterator<?> iterato = owner.getInventory().iterator();
			        for(int slot = 0; iterato.hasNext(); slot++)
			        {
			            InvItem cape = (InvItem)iterato.next();
					int[] zamorakCape = {1213};
					int[] zamorakStaff = {1216};
					for(int i = 0; i < 1; i++) {
						if(owner.wielding(zamorakStaff[i]) && owner.wielding(zamorakCape[i])) {flag2=flag2||true;}
					//	else {flag2 = false;}
						}
					}
					if(owner.getLocation().inMageArena() && owner.getZamorakSpellCast() >= 100) {
						owner.getActionSender().sendMessage("You don't need to cast this in the arena anymore.");
					} else
					if(owner.getLocation().inMageArena() && owner.getZamorakSpellCast() >= 0 && owner.getZamorakSpellCast() <= 99) {
						owner.getActionSender().sendMessage("You gained @or1@1 @whi@zamorak casting point.");
						owner.setZamorakSpellCast(owner.getZamorakSpellCast() +1);
						owner.getActionSender().sendZamorakSpellCast();
					} else
					if(owner.getZamorakSpellCast() < 100) {
						owner.getActionSender().sendMessage("You need to learn this spell in the mage arena before casting it outside.");
						return;
					}
					if(flag2){
						if(!owner.isCharged()){owner.getActionSender().sendMessage("@red@You are not charged!");}
							
						if(!checkAndRemoveRunes(owner, spell)) {
							return;
						}
						if(affectedMob instanceof Player && !owner.isDueling()) {
							Player affectedPlayer = (Player)affectedMob;
							owner.setSkulledOn(affectedPlayer);
				      		}
						//int damag = Rand(0, 25);
						int damag = Formulae.calcGodSpells(owner, affectedMob);
						if(affectedMob instanceof Player) {
							Player affectedPlayer = (Player)affectedMob;
							affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " is shooting at you!");
						}
				      		Projectile projectil = new Projectile(owner, affectedMob, 1);
				      		godSpellObject(affectedMob, 35);
				      		affectedMob.setLastDamage(damag);
				      		int newhp = affectedMob.getHits() - damag;
				      		affectedMob.setHits(newhp);
				      		ArrayList<Player> playersToInfor = new ArrayList<Player>();
				      		playersToInfor.addAll(owner.getViewArea().getPlayersInView());
				      		playersToInfor.addAll(affectedMob.getViewArea().getPlayersInView());
				      		for(Player p : playersToInfor) {
				      			p.informOfProjectile(projectil);
				      			p.informOfModifiedHits(affectedMob);
				      		}
				      		if(affectedMob instanceof Player) {
				      			Player affectedPlayer = (Player)affectedMob;
							affectedPlayer.getActionSender().sendStat(3);
						}
				      		if(newhp <= 0) {
				      			affectedMob.killedBy(owner, owner.isDueling());
				      		}
							owner.getActionSender().sendInventory();
							owner.getActionSender().sendStat(6);
				      		finalizeSpell(owner, spell);
						break;
						} 
					else{owner.getActionSender().sendMessage("You need to be wearing the Zamorak staff & cape to cast this spell");return;}

					
					//WEAKEN SPELLS
					
					
			case 1:
						if(!checkAndRemoveRunes(owner, spell)) {
							return;
			      		}

						if(affectedMob instanceof Player && !owner.isDueling()) {
							Player affectedPlayer = (Player)affectedMob;
							owner.setSkulledOn(affectedPlayer);
				      		}

				      		if(affectedMob instanceof Player) {
				Player affectedPlayer = (Player)affectedMob;
				//grabs stat
				double oldstat = affectedPlayer.getCurStat(0) * 0.05;
				//defines stat
				int newstat = DataConversions.roundUp(oldstat);
				//new stat after getting weakened
				int newrealstat = affectedPlayer.getCurStat(0) - newstat;
				//grab stop stat
				double stops = affectedPlayer.getMaxStat(0) * 0.90;
				//stop stat
				double stopping = DataConversions.roundUp(stops);

				if(affectedPlayer.getCurStat(0) <= stopping)
					{
				owner.getActionSender().sendMessage(affectedPlayer.getUsername() +" is already fully weakened in Attack.");
				break;
					}
					Projectile projectile = new Projectile(owner, affectedPlayer, 1);
					affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " is shooting at you!");
							affectedMob.setAttack(newrealstat);
							affectedPlayer.getActionSender().sendStat(0);
				      		ArrayList<Player> playersToInform = new ArrayList<Player>();
				      		playersToInform.addAll(owner.getViewArea().getPlayersInView());
				      		playersToInform.addAll(affectedMob.getViewArea().getPlayersInView());
				      		for(Player p : playersToInform) {
				      			p.informOfProjectile(projectile);
				      		}
							break;
						}
			case 5:
						if(!checkAndRemoveRunes(owner, spell)) {
							return;
			      		}

						if(affectedMob instanceof Player && !owner.isDueling()) {
							Player affectedPlayer = (Player)affectedMob;
							owner.setSkulledOn(affectedPlayer);
				      		}

				      		if(affectedMob instanceof Player) {
				Player affectedPlayer = (Player)affectedMob;
				//grabs stat
				double oldstat = affectedPlayer.getCurStat(2) * 0.05;
				//defines stat
				int newstat = DataConversions.roundUp(oldstat);
				//new stat after getting weakened
				int newrealstat = affectedPlayer.getCurStat(2) - newstat;
				//grab stop stat
				double stops = affectedPlayer.getMaxStat(2) * 0.95;
				//stop stat
				double stopping = DataConversions.roundUp(stops);
				double fiveper = affectedPlayer.getMaxStat(2) * 0.99;
				double fiveper2 = DataConversions.roundUp(fiveper);
				

				if(affectedPlayer.getCurStat(2) <= stopping || affectedPlayer.getCurStat(2) < fiveper2)
					{
				owner.getActionSender().sendMessage(affectedPlayer.getUsername() +" is already fully weakened in Strength.");
				break;
					}
					Projectile projectile = new Projectile(owner, affectedPlayer, 1);
					affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " is shooting at you!");
							affectedMob.setStrength(newrealstat);
							affectedPlayer.getActionSender().sendStat(2);
				      		ArrayList<Player> playersToInform = new ArrayList<Player>();
				      		playersToInform.addAll(owner.getViewArea().getPlayersInView());
				      		playersToInform.addAll(affectedMob.getViewArea().getPlayersInView());
				      		for(Player p : playersToInform) {
				      			p.informOfProjectile(projectile);
				      		}
							break;
						}

			case 9:
						if(!checkAndRemoveRunes(owner, spell)) {
							return;
			      		}

						if(affectedMob instanceof Player && !owner.isDueling()) {
							Player affectedPlayer = (Player)affectedMob;
							owner.setSkulledOn(affectedPlayer);
				      		}

				      		if(affectedMob instanceof Player) {
				Player affectedPlayer = (Player)affectedMob;
				//grabs stat
				double oldstat = affectedPlayer.getCurStat(1) * 0.05;
				//defines stat
				int newstat = DataConversions.roundUp(oldstat);
				//new stat after getting weakened
				int newrealstat = affectedPlayer.getCurStat(1) - newstat;
				//grab stop stat
				double stops = affectedPlayer.getMaxStat(1) * 0.90;
				//stop stat
				double stopping = DataConversions.roundUp(stops);

				if(affectedPlayer.getCurStat(1) <= stopping)
					{
				owner.getActionSender().sendMessage(affectedPlayer.getUsername() +" is already fully weakened in Defense.");
				break;
					}
					Projectile projectile = new Projectile(owner, affectedPlayer, 1);
					affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " is shooting at you!");
							affectedMob.setDefense(newrealstat);
							affectedPlayer.getActionSender().sendStat(1);
				      		ArrayList<Player> playersToInform = new ArrayList<Player>();
				      		playersToInform.addAll(owner.getViewArea().getPlayersInView());
				      		playersToInform.addAll(affectedMob.getViewArea().getPlayersInView());
				      		for(Player p : playersToInform) {
				      			p.informOfProjectile(projectile);
				      		}
							break;
						}


//VULNERABILITY

				case 42: 
						if(!checkAndRemoveRunes(owner, spell)) {
							return;
			      		}

						if(affectedMob instanceof Player && !owner.isDueling()) {
							Player affectedPlayer = (Player)affectedMob;
							owner.setSkulledOn(affectedPlayer);
				      		}

				      		if(affectedMob instanceof Player) {
				Player affectedPlayer = (Player)affectedMob;
				//grabs stat
				double oldstat = affectedPlayer.getCurStat(1) * 0.1;
				//defines stat
				int newstat = DataConversions.roundUp(oldstat);
				//new stat after getting weakened
				int newrealstat = affectedPlayer.getCurStat(1) - newstat;
				//grab stop stat
				double stops = affectedPlayer.getMaxStat(1) * 0.90;
				//stop stat
				double stopping = DataConversions.roundUp(stops);

				if(affectedPlayer.getCurStat(1) <= stopping)
					{
				owner.getActionSender().sendMessage(affectedPlayer.getUsername() +" is already fully weakened in Defense.");
				break;
					}
					Projectile projectile = new Projectile(owner, affectedPlayer, 1);
					affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " has lowered your Defense.");
							affectedMob.setDefense(newrealstat);
							affectedPlayer.getActionSender().sendStat(1);
				      		ArrayList<Player> playersToInform = new ArrayList<Player>();
				      		playersToInform.addAll(owner.getViewArea().getPlayersInView());
				      		playersToInform.addAll(affectedMob.getViewArea().getPlayersInView());
				      		for(Player p : playersToInform) {
				      			p.informOfProjectile(projectile);
				      		}
							break;
						}
				case 45:
						if(!checkAndRemoveRunes(owner, spell)) {
							return;
			      		}

						if(affectedMob instanceof Player && !owner.isDueling()) {
							Player affectedPlayer = (Player)affectedMob;
							owner.setSkulledOn(affectedPlayer);
				      		}

				      		if(affectedMob instanceof Player) {
				Player affectedPlayer = (Player)affectedMob;
				//grabs stat
				double oldstat = affectedPlayer.getCurStat(2) * 0.1;
				//defines stat
				int newstat = DataConversions.roundUp(oldstat);
				//new stat after getting weakened
				int newrealstat = affectedPlayer.getCurStat(2) - newstat;
				//grab stop stat
				double stops = affectedPlayer.getMaxStat(2) * 0.95;
				//stop stat
				double stopping = DataConversions.roundUp(stops);
				double fiveper = affectedPlayer.getMaxStat(2) * 0.99;
				double fiveper2 = DataConversions.roundUp(fiveper);
				

				if(affectedPlayer.getCurStat(2) <= stopping || affectedPlayer.getCurStat(2) < fiveper2)
					{
				owner.getActionSender().sendMessage(affectedPlayer.getUsername() +" is already fully weakened in Strength.");
				break;
					}
					Projectile projectile = new Projectile(owner, affectedPlayer, 1);
					affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " has lowered your Strength.");
							affectedMob.setStrength(newrealstat);
							affectedPlayer.getActionSender().sendStat(2);
				      		ArrayList<Player> playersToInform = new ArrayList<Player>();
				      		playersToInform.addAll(owner.getViewArea().getPlayersInView());
				      		playersToInform.addAll(affectedMob.getViewArea().getPlayersInView());
				      		for(Player p : playersToInform) {
				      			p.informOfProjectile(projectile);
				      		}
							break;
						}

				case 47:
						if(!checkAndRemoveRunes(owner, spell)) {
							return;
			      		}

						if(affectedMob instanceof Player && !owner.isDueling()) {
							Player affectedPlayer = (Player)affectedMob;
							owner.setSkulledOn(affectedPlayer);
				      		}

				      		if(affectedMob instanceof Player) {
				Player affectedPlayer = (Player)affectedMob;
				//grabs stat
				double oldstat = affectedPlayer.getCurStat(0) * 0.1;
				//defines stat
				int newstat = DataConversions.roundUp(oldstat);
				//new stat after getting weakened
				int newrealstat = affectedPlayer.getCurStat(0) - newstat;
				//grab stop stat
				double stops = affectedPlayer.getMaxStat(0) * 0.90;
				//stop stat
				double stopping = DataConversions.roundUp(stops);

				if(affectedPlayer.getCurStat(0) <= stopping)
					{
				owner.getActionSender().sendMessage(affectedPlayer.getUsername() +" is already fully weakened in Attack.");
				break;
					}
					Projectile projectile = new Projectile(owner, affectedPlayer, 1);
					affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " has lowered your Attack.");
							affectedMob.setAttack(newrealstat);
							affectedPlayer.getActionSender().sendStat(0);
				      		ArrayList<Player> playersToInform = new ArrayList<Player>();
				      		playersToInform.addAll(owner.getViewArea().getPlayersInView());
				      		playersToInform.addAll(affectedMob.getViewArea().getPlayersInView());
				      		for(Player p : playersToInform) {
				      			p.informOfProjectile(projectile);
				      		}
							break;
						}

					default:
					
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
						if(!checkAndRemoveRunes(owner, spell)) {
							return;
			      		}
						if(affectedMob instanceof Player && !owner.isDueling()) {
							Player affectedPlayer = (Player)affectedMob;
							owner.setSkulledOn(affectedPlayer);
				      		}
						int damage = Formulae.calcSpellHit(EntityHandler.getSpellAggressiveLvl(spellID), owner.getMagicPoints());
						if(affectedMob instanceof Player) {
							Player affectedPlayer = (Player)affectedMob;
							affectedPlayer.getActionSender().sendMessage(owner.getUsername() + " is shooting at you!");
						}
				      		Projectile projectile = new Projectile(owner, affectedMob, 1);
				      		affectedMob.setLastDamage(damage);
							affectedMob.updateKillStealing(owner.getUsernameHash(), damage, 2);
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
				      		if(newHp <= 0) {
				      			affectedMob.killedBy(owner, owner.isDueling());
				      		}
				      		finalizeSpell(owner, spell);
						break;
				}
				owner.getActionSender().sendInventory();
				owner.getActionSender().sendStat(6);
			}
		});
	}

	// END WEAKEN SPELLS
	
	public void godSpellObject(Mob affectedMob, int spell)
		{
			switch(spell)
			{
				case 33:
					GameObject guthix = new GameObject(affectedMob.getLocation(), 1142, 0, 0);
					world.registerGameObject(guthix);
					world.getDelayedEventHandler().add(new ObjectRemover(guthix, 500));
					break;
				case 34:
					GameObject sara = new GameObject(affectedMob.getLocation(), 1031, 0, 0);
					world.registerGameObject(sara);
					world.getDelayedEventHandler().add(new ObjectRemover(sara, 500));
					break;
				case 35:
					GameObject zammy = new GameObject(affectedMob.getLocation(), 1036, 0, 0);
					world.registerGameObject(zammy);
					world.getDelayedEventHandler().add(new ObjectRemover(zammy, 500));
					break;
				
				
			}
	}
	private void handleItemCast(Player player, final SpellDef spell, final int id, final Item affectedItem) {
		player.setStatus(Action.CASTING_GITEM);
		world.getDelayedEventHandler().add(new WalkToPointEvent(player, affectedItem.getLocation(), 5, true) {
			public void arrived() {
				owner.resetPath();
				ActiveTile tile = world.getTile(location);
				if(!canCast(owner) || !tile.hasItem(affectedItem) || owner.getStatus() != Action.CASTING_GITEM) {
					return;
				}
				owner.resetAllExceptDueling();
				switch(id) {
					case 16: // Telekinetic grab
						if(affectedItem.getLocation().inBounds(490, 464, 500, 471) || affectedItem.getLocation().inBounds(490, 1408, 500, 1415) || affectedItem.getLocation().inBounds(205, 427, 233, 460)) {
							owner.getActionSender().sendMessage("Telekinetic grab cannot be used here.");
							return;
						}
						if(!checkAndRemoveRunes(owner, spell)) {
							return;
						}
						owner.getActionSender().sendTeleBubble(location.getX(), location.getY(), true);
						for (Object o : owner.getWatchedPlayers().getAllEntities()) {
							Player p = ((Player)o);
							p.getActionSender().sendTeleBubble(location.getX(), location.getY(), true);
						}
						world.unregisterItem(affectedItem);
						finalizeSpell(owner, spell);
						owner.getInventory().add(new InvItem(affectedItem.getID(), affectedItem.getAmount()));
						break;
				}
				owner.getActionSender().sendInventory();
				owner.getActionSender().sendStat(6);
			}
		});
	}
	
	private void handleInvItemCast(Player player, SpellDef spell, int id, InvItem affectedItem) {
		switch(id) {
			case 3: // Enchant lvl-1 Sapphire amulet
				if(affectedItem.getID() == 302) {
					if(!checkAndRemoveRunes(player, spell)) {
						return;
					}
					player.getInventory().remove(affectedItem);
					player.getInventory().add(new InvItem(314));
					finalizeSpell(player, spell);
				}
				else {
					player.getActionSender().sendMessage("This spell cannot be used on this kind of item");
				}
				break;
			case 10: // Low level alchemy
				if(affectedItem.getID() == 10) {
					player.getActionSender().sendMessage("You cannot alchemy that");
					return;
				}
				if(!checkAndRemoveRunes(player, spell)) {
					return;
				}
				if(player.getInventory().remove(affectedItem) > 1) {
					int value = (int)(affectedItem.getDef().getBasePrice() * 0.4D * affectedItem.getAmount());
					player.getInventory().add(new InvItem(10, value)); // 40%
				}
				finalizeSpell(player, spell);
				break;
			case 13: // Enchant lvl-2 emerald amulet
				if(affectedItem.getID() == 303) {
					if(!checkAndRemoveRunes(player, spell)) {
						return;
					}
					player.getInventory().remove(affectedItem);
					player.getInventory().add(new InvItem(315));
					finalizeSpell(player, spell);
				}
				else {
					player.getActionSender().sendMessage("This spell cannot be used on this kind of item");
				}
				break;
			case 21: // Superheat item
        			ItemSmeltingDef smeltingDef = affectedItem.getSmeltingDef();
      				if(smeltingDef == null) {
      					player.getActionSender().sendMessage("This spell cannot be used on this kind of item");
      					return;
      				}
              			for(ReqOreDef reqOre : smeltingDef.getReqOres()) {
              				if(player.getInventory().countId(reqOre.getId()) < reqOre.getAmount()) {
              					if(affectedItem.getID() == 151) {
              						smeltingDef = EntityHandler.getItemSmeltingDef(9999);
              						break;
              					}
              					player.getActionSender().sendMessage("You need " + reqOre.getAmount() + " " + EntityHandler.getItemDef(reqOre.getId()).getName() + " to smelt a " + affectedItem.getDef().getName() + ".");
              					return;
              				}
      				}
            			if(player.getCurStat(13) < smeltingDef.getReqLevel()) {
            				player.getActionSender().sendMessage("You need a smithing level of " + smeltingDef.getReqLevel() + " to smelt this.");
            				return;
            			}
				if(!checkAndRemoveRunes(player, spell)) {
					return;
				}
      				InvItem bar = new InvItem(smeltingDef.getBarId());
      				if(player.getInventory().remove(affectedItem) > -1) {
      			      		for(ReqOreDef reqOre : smeltingDef.getReqOres()) {
      			      			for(int i = 0;i < reqOre.getAmount();i++) {
      			      				player.getInventory().remove(new InvItem(reqOre.getId()));
      			      			}
      			      		}
      			      		player.getActionSender().sendMessage("You make a " + bar.getDef().getName() + ".");
      					player.getInventory().add(bar);
      					player.incExp(13, smeltingDef.getExp(), true, true);
      					player.getActionSender().sendStat(13);
      					player.getActionSender().sendInventory();
      				}
      				finalizeSpell(player, spell);
				break;
			case 24: // Enchant lvl-3 ruby amulet
				if(affectedItem.getID() == 304) {
					if(!checkAndRemoveRunes(player, spell)) {
						return;
					}
					player.getInventory().remove(affectedItem);
					player.getInventory().add(new InvItem(316));
					finalizeSpell(player, spell);
				}
				else {
					player.getActionSender().sendMessage("This spell cannot be used on this kind of item");
				}
				break;
			case 28: // High level alchemy
				if(affectedItem.getID() == 10) {
					player.getActionSender().sendMessage("You cannot alchemy that");
					return;
				}
				if(!checkAndRemoveRunes(player, spell)) {
					return;
				}
				if(player.getInventory().remove(affectedItem) > -1) {
					int value = (int)(affectedItem.getDef().getBasePrice() * 0.6D * affectedItem.getAmount());
					player.getInventory().add(new InvItem(10, value)); // 60%
				}
				finalizeSpell(player, spell);
				break;
			case 30: // Enchant lvl-4 diamond amulet
				if(affectedItem.getID() == 305) {
					if(!checkAndRemoveRunes(player, spell)) {
						return;
					}
					player.getInventory().remove(affectedItem);
					player.getInventory().add(new InvItem(317));
					finalizeSpell(player, spell);
				}
				else {
					player.getActionSender().sendMessage("This spell cannot be used on this kind of item");
				}
				break;
			case 43: // Enchant lvl-5 dragonstone amulet
				if(affectedItem.getID() == 610) {
					if(!checkAndRemoveRunes(player, spell)) {
						return;
					}
					player.getInventory().remove(affectedItem);
					player.getInventory().add(new InvItem(522));
					finalizeSpell(player, spell);
				}
				else {
					player.getActionSender().sendMessage("This spell cannot be used on this kind of item");
				}
				break;
			case 49: //enchant lvl-6 dacionia amulet
				if(affectedItem.getID() == 1352) {
					if(!checkAndRemoveRunes(player, spell)) {
						return;
					}
					player.getInventory().remove(affectedItem);
					player.getInventory().add(new InvItem(1353));
					finalizeSpell(player, spell);
				}
				else {
					player.getActionSender().sendMessage("this spell cannot be used on this kind of item");
				}
				break;
		}
		if(affectedItem.isWielded()) {
			player.getActionSender().sendSound("click");
			affectedItem.setWield(false);
			player.updateWornItems(affectedItem.getWieldableDef().getWieldPos(), player.getPlayerAppearance().getSprite(affectedItem.getWieldableDef().getWieldPos()));
			player.getActionSender().sendEquipmentStats();
		}
	}
	
	private void handleGroundCast(Player player, SpellDef spell, int id) {
		switch(id) {
			case 7: // Bones to bananas
				if(!checkAndRemoveRunes(player, spell)) {
					return;
				}
				Iterator<InvItem> inventory = player.getInventory().iterator();
				int boneCount = 0;
				while(inventory.hasNext()) {
					InvItem i = inventory.next();
					if(i.getID() == 20) {
						inventory.remove();
						boneCount++;
					}
				}
				for(int i = 0;i < boneCount;i++) {
					player.getInventory().add(new InvItem(249));
				}
				finalizeSpell(player, spell);
				break;
			case 48: // Charge
				if(world.getTile(player.getLocation()).hasGameObject()) {
					player.getActionSender().sendMessage("You cannot charge here, please move to a different area.");
					return;
				}
				if(!checkAndRemoveRunes(player, spell)) {
					return;
				}
      				GameObject charge = new GameObject(player.getLocation(), 1147, 0, 0);
      				world.registerGameObject(charge);
      				world.delayedRemoveObject(charge, 500);
      				player.setCharged();
					player.resetPath();
      				finalizeSpell(player, spell);
				player.getActionSender().sendMessage("@red@You feel charged with magical power...");
				break;
		}
	}
	
	private void handleTeleport(Player player, SpellDef spell, int id) {
      		if(player.inCombat()) {
      			player.getActionSender().sendMessage("You cannot teleport while in combat.");
      			return;
      		}
		if(player.getLocation().wildernessLevel() >= 1 || (player.getLocation().inModRoom() && !player.isMod())) {
			player.getActionSender().sendMessage("A magical force stops you from teleporting in the wilderness");
			return;
		}
		if(!checkAndRemoveRunes(player, spell)) {
			return;
		}
		switch(id) {
			case 12: // Varrock
				player.teleport(122, 503, true);
				break;
			case 15: // Lumbridge
				player.teleport(118, 649, true);
				break;
			case 18: // Falador
				player.teleport(313, 550, true);
				break;
			case 22: // Camalot
				player.teleport(465, 456, true);
				break;
			case 26: // Ardougne
				player.teleport(585, 621, true);
				break;
			case 31: // Watchtower
				player.teleport(637, 2628, true);
				break;
			case 37: // Lost city
				player.teleport(131, 3544, true);
				break;
		}
		finalizeSpell(player, spell);
	}
	
	private static boolean canCast(Player player) {
		if(!player.castTimer()) {
			player.getActionSender().sendMessage("You must wait another " + player.getSpellWait() + " seconds to cast another spell.");
			player.resetPath();
			return false;
		}
		return true;
	}
	
	private static boolean checkAndRemoveRunes(Player player, SpellDef spell) {
		for(Entry<Integer, Integer> e : spell.getRunesRequired()) {
			boolean skipRune = false;
			for(InvItem staff : getStaffs(e.getKey())) {
				if(player.getInventory().contains(staff)) {
					for(InvItem item : player.getInventory().getItems()) {
						if(item.equals(staff) && item.isWielded()) {
							skipRune = true;
							break;
						}
					}
				}
			}
			if(skipRune) {
				continue;
			}
			if(player.getInventory().countId(((Integer)e.getKey()).intValue()) < ((Integer)e.getValue()).intValue()) {
				player.setSuspiciousPlayer(true);
				player.getActionSender().sendMessage("You don't have all the reagents you need for this spell");
				return false;
			}
		}
      		for(Entry<Integer, Integer> e : spell.getRunesRequired()) {
			boolean skipRune = false;
			for(InvItem staff : getStaffs(e.getKey())) {
				if(player.getInventory().contains(staff)) {
					for(InvItem item : player.getInventory().getItems()) {
						if(item.equals(staff) && item.isWielded()) {
							skipRune = true;
							break;
						}
					}
				}
			}
			if(skipRune) {
				continue;
			}
			player.getInventory().remove(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue());
      		}
		return true;
	}
	
	private void finalizeSpell(Player player, SpellDef spell) {
		player.getActionSender().sendSound("spellok");
		player.getActionSender().sendMessage("Cast spell successfully");
		player.incExp(6, spell.getExp(), true, true);
		player.setCastTimer();
	}
	
	private static InvItem[] getStaffs(int runeID) {
		InvItem[] items = staffs.get(runeID);
		if(items == null) {
			return new InvItem[0];
		}
		return items;
		
	}
	
	private static TreeMap<Integer, InvItem[]> staffs = new TreeMap<Integer, InvItem[]>();
	
	static {
		staffs.put(31, new InvItem[]{new InvItem(197), new InvItem(615), new InvItem(682)}); // Fire-Rune
		staffs.put(32, new InvItem[]{new InvItem(102), new InvItem(616), new InvItem(683)}); // Water-Rune
		staffs.put(33, new InvItem[]{new InvItem(101), new InvItem(617), new InvItem(684)}); // Air-Rune
		staffs.put(34, new InvItem[]{new InvItem(103), new InvItem(618), new InvItem(685)}); // Earth-Rune
	}
	
}