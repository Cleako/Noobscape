package org.rscdaemon.server.event;

import org.rscdaemon.server.Server;
import org.rscdaemon.server.entityhandling.defs.ItemDef;
import org.rscdaemon.server.entityhandling.defs.extras.ObjectMiningDef;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.util.*;

public class MiningEvent extends DelayedEvent {
	private ObjectMiningDef def;
	private GameObject object;
	private int maxSwings;
	private InvItem ore;
	private int swings;
	private int axeId;
	
	public MiningEvent(Player player, ObjectMiningDef def, GameObject object, int axeId, int maxSwings) {
		super(player, 1500);
		this.maxSwings = maxSwings;
		this.axeId = axeId;
		this.def = def;
		this.ore = new InvItem(def.getOreId());
		this.object = object;
		this.swings = 0;
	}
	
	public void run() {
		if((owner == null) || (ore == null) || (def == null) || (object == null)) {
			owner.setBusy(false);
			running = false;
		} else if(swings < maxSwings) {
			if(swings != 0) {
				owner.getActionSender().sendSound("mine");
				Bubble bubble = new Bubble(owner, axeId);
				for(Player p : owner.getViewArea().getPlayersInView()) {
					p.informOfBubble(bubble);
				}
				owner.getActionSender().sendMessage("You swing your pick at the rock...");
			}
			if(Formulae.getOre(def, owner.getCurStat(14), axeId)) {
				if(DataConversions.random(0, (owner.wielding(597)) ? 100 : 200) == 0) {
					InvItem gem = new InvItem(Formulae.getGem(), 1);
					owner.getInventory().add(gem);
					owner.getActionSender().sendMessage("You found a gem!");
				} else {
					owner.getInventory().add(ore);
					owner.getActionSender().sendMessage("You manage to obtain some " + this.ore.getDef().getName() + ".");
					owner.incExp(14, def.getExp(), true, true);
					owner.getActionSender().sendStat(14);
					world.registerGameObject(new GameObject(object.getLocation(), 98, object.getDirection(), object.getType()));
					world.delayedSpawnObject(object.getLoc(), def.getRespawnTime() * 1000);
				}
				owner.getActionSender().sendInventory();
				owner.setBusy(false);
				running = false;
			} else {
				owner.getActionSender().sendMessage("You only succeed in scratching the rock...");
				swings += 1;
			}
		} else {
			owner.setBusy(false);
			running = false;
		}
	}
}