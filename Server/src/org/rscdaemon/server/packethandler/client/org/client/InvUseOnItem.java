package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.entityhandling.EntityHandler;
import org.rscdaemon.server.entityhandling.defs.extras.*;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.util.Formulae;
import org.rscdaemon.server.event.MiniEvent;
import org.apache.mina.common.IoSession;

public class InvUseOnItem implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handlePacket(Packet p, IoSession session) throws Exception {
		Player player = (Player)session.getAttachment();
		if(player.isBusy()) {
			player.resetPath();
			return;
		}//incExp
		player.resetAll();
		InvItem item1 = player.getInventory().get(p.readShort());
		InvItem item2 = player.getInventory().get(p.readShort());
		if(item1 == null || item2 == null) {
			player.setSuspiciousPlayer(true);
			return;
		}
		ItemHerbSecond secondDef = null;
		if((secondDef = EntityHandler.getItemHerbSecond(item1.getID(), item2.getID())) != null && doHerbSecond(player, item1, item2, secondDef)) {
			return;
		}
		else if((secondDef = EntityHandler.getItemHerbSecond(item2.getID(), item1.getID())) != null && doHerbSecond(player, item2, item1, secondDef)) {
			return;
		}
		else if(item1.getID() == 381 && attachFeathers(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 381 && attachFeathers(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 167 && doCutGem(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 167 && doCutGem(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 464 && doHerblaw(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 464 && doHerblaw(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 13 && doLogCut(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 13 && doLogCut(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 676 && doBowString(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 676 && doBowString(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 637 && doArrowHeads(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 637 && doArrowHeads(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 207 && useWool(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 207 && useWool(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 39 && makeLeather(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 39 && makeLeather(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 468 && doGrind(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 468 && doGrind(player, item2, item1)) {
			return;
		}
		else if((item1.getID() == 50 || item1.getID() == 141 || item1.getID() == 342) && useWater(player, item1, item2)) {
			return;
		}
		else if((item2.getID() == 50 || item2.getID() == 141 || item2.getID() == 342) && useWater(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 621 && doGlassBlowing(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 621 && doGlassBlowing(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 526 && combineKeys(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 526 && combineKeys(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 141 && mixFlour(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 141 && mixFlour(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 321 && addTomato(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 321 && addTomato(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 323 && addCheese(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 323 && addCheese(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 250 && addPastry(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 250 && addPastry(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 252 && addCookingApple(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 252 && addCookingApple(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 132 && addCookedmeat(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 132 && addCookedmeat(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 236 && addRedberries(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 236 && addRedberries(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 239 && addYellowDye(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 239 && addYellowDye(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 238 && addRedDye(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 238 && addRedDye(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 272 && addBlueDye(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 272 && addBlueDye(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 282 && addOrangeDye(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 282 && addOrangeDye(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 515 && addGreenDye(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 515 && addGreenDye(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 516 && addPurpleDye(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 516 && addPurpleDye(player, item2, item1)) {
			return;
		}
		else if(item1.getID() == 177 && addFoodPoison(player, item1, item2)) {
			return;
		}
		else if(item2.getID() == 177 && addFoodPoison(player, item2, item1)) {
			return;
		}
		else {
			player.getActionSender().sendMessage("Nothing interesting happens.");
		}
	}
	
	private boolean combineKeys(Player player, final InvItem firstHalf, final InvItem secondHalf) {
		if(secondHalf.getID() != 527) {
			return false;
		}
		if(player.getInventory().remove(firstHalf) > -1 && player.getInventory().remove(secondHalf) > -1) {
			player.getActionSender().sendMessage("You combine the key halves to make a crystal key.");
			player.getInventory().add(new InvItem(525, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}
	
	private boolean addFoodPoison(Player player, final InvItem foodPoison, final InvItem fishFood) {
		if(fishFood.getID() != 176) {
			return false;
		}
		if(player.getInventory().remove(foodPoison) > -1 && player.getInventory().remove(fishFood) > -1) {
			player.getActionSender().sendMessage("You pour the food poison into the fish food to poison it.");
			player.getInventory().add(new InvItem(178, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addYellowDye(Player player, final InvItem Dye, final InvItem Cape) {
		int newID;
		switch(Cape.getID()) {
			case 183: // Red Cape
				newID = 512;
				break;
			case 209: // Black Cape
				newID = 512;
				break;
			case 229: // Blue Cape
				newID = 512;
				break;
			case 511: // Green Cape
				newID = 512;
				break;
			case 513: // Orange Cape
				newID = 512;
				break;
			case 514: // Purple Cape
				newID = 512;
				break;
			case 272: // Blue Dye
				newID = 515;
				break;
			case 238: // Red Dye
				newID = 282;
				break;
			default:
				return false;
		}
		if(player.getInventory().remove(Cape) > -1 && player.getInventory().remove(Dye) > -1) {
			player.getActionSender().sendMessage("You add the dye to the " + Cape.getDef().getName());
			player.getInventory().add(new InvItem(newID, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addRedDye(Player player, final InvItem Dye, final InvItem Cape) {
		int newID;
		switch(Cape.getID()) {
			case 512: // Yellow Cape
				newID = 183;
				break;
			case 209: // Black Cape
				newID = 183;
				break;
			case 229: // Blue Cape
				newID = 183;
				break;
			case 511: // Green Cape
				newID = 183;
				break;
			case 513: // Orange Cape
				newID = 183;
				break;
			case 514: // Purple Cape
				newID = 183;
				break;
			case 272: // Blue Dye
				newID = 516;
				break;
			default:
				return false;
		}
		if(player.getInventory().remove(Cape) > -1 && player.getInventory().remove(Dye) > -1) {
			player.getActionSender().sendMessage("You add the dye to the " + Cape.getDef().getName());
			player.getInventory().add(new InvItem(newID, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addBlueDye(Player player, final InvItem Dye, final InvItem Cape) {
		int newID;
		switch(Cape.getID()) {
			case 183: // Red Cape
				newID = 229;
				break;
			case 209: // Black Cape
				newID = 229;
				break;
			case 512: // Yellow Cape
				newID = 229;
				break;
			case 511: // Green Cape
				newID = 229;
				break;
			case 513: // Orange Cape
				newID = 229;
				break;
			case 514: // Purple Cape
				newID = 229;
				break;
			case 273: // Goblin Armour
				newID = 275;
				break;
			default:
				return false;
		}
		if(player.getInventory().remove(Cape) > -1 && player.getInventory().remove(Dye) > -1) {
			player.getActionSender().sendMessage("You add the dye to the " + Cape.getDef().getName());
			player.getInventory().add(new InvItem(newID, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addOrangeDye(Player player, final InvItem Dye, final InvItem Cape) {
		int newID;
		switch(Cape.getID()) {
			case 183: // Red Cape
				newID = 513;
				break;
			case 209: // Black Cape
				newID = 513;
				break;
			case 229: // Blue Cape
				newID = 513;
				break;
			case 511: // Green Cape
				newID = 513;
				break;
			case 512: // Yellow Cape
				newID = 513;
				break;
			case 514: // Purple Cape
				newID = 513;
				break;
			case 273: // Goblin Armour
				newID = 274;
				break;
			default:
				return false;
		}
		if(player.getInventory().remove(Cape) > -1 && player.getInventory().remove(Dye) > -1) {
			player.getActionSender().sendMessage("You add the dye to the " + Cape.getDef().getName());
			player.getInventory().add(new InvItem(newID, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addGreenDye(Player player, final InvItem Dye, final InvItem Cape) {
		int newID;
		switch(Cape.getID()) {
			case 183: // Red Cape
				newID = 511;
				break;
			case 209: // Black Cape
				newID = 511;
				break;
			case 229: // Blue Cape
				newID = 511;
				break;
			case 512: // Yellow Cape
				newID = 511;
				break;
			case 513: // Orange Cape
				newID = 511;
				break;
			case 514: // Purple Cape
				newID = 511;
				break;
			default:
				return false;
		}
		if(player.getInventory().remove(Cape) > -1 && player.getInventory().remove(Dye) > -1) {
			player.getActionSender().sendMessage("You add the dye to the " + Cape.getDef().getName());
			player.getInventory().add(new InvItem(newID, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addPurpleDye(Player player, final InvItem Dye, final InvItem Cape) {
		int newID;
		switch(Cape.getID()) {
			case 183: // Red Cape
				newID = 514;
				break;
			case 209: // Black Cape
				newID = 514;
				break;
			case 229: // Blue Cape
				newID = 514;
				break;
			case 511: // Green Cape
				newID = 514;
				break;
			case 513: // Orange Cape
				newID = 514;
				break;
			case 512: // Yellow Cape
				newID = 514;
				break;
			default:
				return false;
		}
		if(player.getInventory().remove(Cape) > -1 && player.getInventory().remove(Dye) > -1) {
			player.getActionSender().sendMessage("You add the dye to the " + Cape.getDef().getName());
			player.getInventory().add(new InvItem(newID, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addCookingApple(Player player, final InvItem CookingApple, final InvItem Shell) {
		if(CookingApple.getID() != 252) {
			return false;
		}
		if(player.getInventory().remove(CookingApple) > -1 && player.getInventory().remove(Shell) > -1) {
			player.getActionSender().sendMessage("You add the cooking apple to the pie shell.");
			player.getInventory().add(new InvItem(254, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addCookedmeat(Player player, final InvItem Cookedmeat, final InvItem Shell) {
		if(Cookedmeat.getID() != 132) {
			return false;
		}
		if(player.getInventory().remove(Cookedmeat) > -1 && player.getInventory().remove(Shell) > -1) {
			player.getActionSender().sendMessage("You add the cooked meat to the pie shell.");
			player.getInventory().add(new InvItem(255, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addRedberries(Player player, final InvItem Redberries, final InvItem Shell) {
		if(Redberries.getID() != 236) {
			return false;
		}
		if(player.getInventory().remove(Redberries) > -1 && player.getInventory().remove(Shell) > -1) {
			player.getActionSender().sendMessage("You add the redberries to the pie shell.");
			player.getInventory().add(new InvItem(256, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addPastry(Player player, final InvItem Pastry, final InvItem Dish) {
		if(Pastry.getID() != 250) {
			return false;
		}
		if(player.getInventory().remove(Pastry) > -1 && player.getInventory().remove(Dish) > -1) {
			player.getActionSender().sendMessage("You put the pastry dough inside the pie dish.");
			player.getInventory().add(new InvItem(253, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addTomato(Player player, final InvItem Base, final InvItem Tomato) {
		if(Tomato.getID() != 320) {
			return false;
		}
		if(player.getInventory().remove(Base) > -1 && player.getInventory().remove(Tomato) > -1) {
			player.getActionSender().sendMessage("You add the tomato to the pizza base.");
			player.getInventory().add(new InvItem(323, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}

	private boolean addCheese(Player player, final InvItem UnfinishedPizza, final InvItem Cheese) {
		if(Cheese.getID() != 319) {
			return false;
		}
		if(player.getInventory().remove(UnfinishedPizza) > -1 && player.getInventory().remove(Cheese) > -1) {
			player.getActionSender().sendMessage("You add the cheese to the incomplete pizza.");
			player.getInventory().add(new InvItem(324, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}
		

	private boolean mixFlour(Player player, final InvItem Water, final InvItem Flour) {
		if(Flour.getID() != 23) {
			return false;
		}
		world.getDelayedEventHandler().add(new MiniEvent(player) {
			public void action() {
				String[] options = new String[]{"Pizza Base", "Pastry Dough", "Cancel"};
				owner.setMenuHandler(new MenuHandler(options) {
					public void handleReply(final int option, final String reply) {
						InvItem result;
						switch(option) {
							case 0: // Pizza Base
								result = new InvItem(321, 1);
								break;
							case 1: // Pastry Dough
								result = new InvItem(250, 1);
								break;
							default:
								return;
						}
						if(owner.getInventory().remove(Flour) > -1) {
							owner.getInventory().remove(Water);
							owner.getInventory().add(result);
							owner.getActionSender().sendMessage("You make a " + result.getDef().getName());
							owner.getActionSender().sendInventory();
						}
					}
				});
				owner.getActionSender().sendMenu(options);
			}
		});
		return true;
	}
	
	private boolean doGlassBlowing(Player player, final InvItem pipe, final InvItem glass) {
		if(glass.getID() != 623) {
			return false;
		}
		world.getDelayedEventHandler().add(new MiniEvent(player) {
			public void action() {
				String[] options = new String[]{"Beer Glass", "Vial", "Orb", "Cancel"};
				owner.setMenuHandler(new MenuHandler(options) {
					public void handleReply(final int option, final String reply) {
						InvItem result;
						int reqLvl, exp;
						switch(option) {
							case 0:
								result = new InvItem(620, 1);
								reqLvl = 1;
								exp = 18;
								break;
							case 1:
								result = new InvItem(465, 1);
								reqLvl = 33;
								exp = 35;
								break;
							case 2:
								result = new InvItem(611, 1);
								reqLvl = 46;
								exp = 53;
								break;
							default:
								return;
						}
						if(owner.getCurStat(12) < reqLvl) {
							owner.getActionSender().sendMessage("You need a crafting level of " + reqLvl + " to make a " + result.getDef().getName() + ".");
      							return;
						}
						if(owner.getInventory().remove(glass) > -1) {
							owner.getActionSender().sendMessage("You make a " + result.getDef().getName());
							owner.getInventory().add(result);
							owner.incExp(12, exp, true, true);
							owner.getActionSender().sendStat(12);
							owner.getActionSender().sendInventory();
						}
					}
				});
				owner.getActionSender().sendMenu(options);
			}
		});
		return true;
	}
	
	private boolean useWater(Player player, final InvItem water, final InvItem item) {
		int jugID = Formulae.getEmptyJug(water.getID());
		if(jugID == -1) { // This shouldn't happen
			return false;
		}
		switch(item.getID()) {
			case 149: // Clay
				if(player.getInventory().remove(water) > -1 && player.getInventory().remove(item) > -1) {
					player.getActionSender().sendMessage("You soften the clay.");
					player.getInventory().add(new InvItem(jugID, 1));
					player.getInventory().add(new InvItem(243, 1));
					player.getActionSender().sendInventory();
				}
				break;
			default:
				return false;
		}
		return true;
	}
	
	private boolean doGrind(Player player, final InvItem mortar, final InvItem item) {
		int newID;
		switch(item.getID()) {
			case 466: // Unicorn Horn
				newID = 473;
				break;
			case 467: // Blue dragon scale
				newID = 472;
				break;
			default:
				return false;
		}
		if(player.getInventory().remove(item) > -1) {
			player.getActionSender().sendMessage("You grind up the " + item.getDef().getName());
			player.getInventory().add(new InvItem(newID, 1));
			player.getActionSender().sendInventory();
		}
		return true;
	}
	
	private boolean doHerbSecond(Player player, final InvItem second, final InvItem unfinished, final ItemHerbSecond def) {
		if(unfinished.getID() != def.getUnfinishedID()) {
			return false;
		}
		if(player.getCurStat(15) < def.getReqLevel()) {
			player.getActionSender().sendMessage("You need a herblaw level of " + def.getReqLevel() + " to mix those");
			return true;
		}
		if(player.getInventory().remove(second) > -1 && player.getInventory().remove(unfinished) > -1) {
			player.getActionSender().sendMessage("You mix the " + second.getDef().getName() + " with the " + unfinished.getDef().getName());
			player.getInventory().add(new InvItem(def.getPotionID(), 1));
			player.incExp(15, def.getExp(), true, true);
			player.getActionSender().sendStat(15);
			player.getActionSender().sendInventory();
		}
		return true;
	}
	
	private boolean makeLeather(Player player, final InvItem needle, final InvItem leather) {
		if(leather.getID() != 148) {
			return false;
		}
		if(player.getInventory().countId(43) < 1) {
			player.getActionSender().sendMessage("You need some thread to make anything out of leather");
			return true;
		}
		if(DataConversions.random(0, 5) == 0) {
			player.getInventory().remove(43, 1);
		}
		world.getDelayedEventHandler().add(new MiniEvent(player) {
			public void action() {
				String[] options = new String[]{"Armour", "Gloves", "Boots", "Cancel"};
				owner.setMenuHandler(new MenuHandler(options) {
					public void handleReply(final int option, final String reply) {
						InvItem result;
						int reqLvl, exp;
						switch(option) {
							case 0:
								result = new InvItem(15, 1);
								reqLvl = 14;
								exp = 25;
								break;
							case 1:
								result = new InvItem(16, 1);
								reqLvl = 1;
								exp = 14;
								break;
							case 2:
								result = new InvItem(17, 1);
								reqLvl = 7;
								exp = 17;
								break;
							default:
								return;
						}
						if(owner.getCurStat(12) < reqLvl) {
							owner.getActionSender().sendMessage("You need a crafting level of " + reqLvl + " to make " + result.getDef().getName() + ".");
      							return;
						}
						if(owner.getInventory().remove(leather) > -1) {
							owner.getActionSender().sendMessage("You make some " + result.getDef().getName());
							owner.getInventory().add(result);
							owner.incExp(12, exp, true, true);
							owner.getActionSender().sendStat(12);
							owner.getActionSender().sendInventory();
						}
					}
				});
				owner.getActionSender().sendMenu(options);
			}
		});
		return true;
	}
	
	private boolean useWool(Player player, final InvItem woolBall, final InvItem item) {
		int newID;
		switch(item.getID()) {
			case 44: // Holy Symbol of saradomin
				newID = 45;
				break;
			case 1027: // Unholy Symbol of Zamorak
				newID = 1028;
				break;
			case 296: // Gold Amulet
				newID = 301;
				break;
			case 297: // Sapphire Amulet
				newID = 302;
				break;
			case 298: // Emerald Amulet
				newID = 303;
				break;
			case 299: // Ruby Amulet
				newID = 304;
				break;
			case 300: // Diamond Amulet
				newID = 305;
				break;
			case 524: // Dragonstone Amulet
				newID = 610;
				break;
			default:
				return false;
		}
		final int newId = newID;
		world.getDelayedEventHandler().add(new MiniEvent(player) {
			public void action() {
				if(owner.getInventory().remove(woolBall) > -1 && owner.getInventory().remove(item) > -1) {
					owner.getActionSender().sendMessage("You string the amulet");
					owner.getInventory().add(new InvItem(newId, 1));
					owner.getActionSender().sendInventory();
				}
			}
		});
		return true;
	}
	
	private boolean attachFeathers(Player player, final InvItem feathers, final InvItem item) {
		int amount = 10;
      		if(feathers.getAmount() < amount) {
      			amount = feathers.getAmount();
      		}
      		if(item.getAmount() < amount) {
      			amount = item.getAmount();
      		}
      		InvItem newItem;
      		int exp;
      		ItemDartTipDef tipDef = null;
      		if(item.getID() == 280) {
      			newItem = new InvItem(637, amount);
      			exp = amount;
      		}
      		else if((tipDef = EntityHandler.getItemDartTipDef(item.getID())) != null) {
      			newItem = new InvItem(tipDef.getDartID(), amount);
      			exp = (int)(tipDef.getExp() * (double)amount);
      		}
      		else {
			return false;
      		}
      		final int amt = amount;
      		final int xp = exp;
      		final InvItem newItm = newItem;
		world.getDelayedEventHandler().add(new MiniEvent(player) {
			public void action() {
				if(owner.getInventory().remove(feathers.getID(), amt) > -1 && owner.getInventory().remove(item.getID(), amt) > -1) {
					owner.getActionSender().sendMessage("You attach the feathers to the " + item.getDef().getName());
					owner.getInventory().add(newItm);
					owner.incExp(9, xp, true, true);
					owner.getActionSender().sendStat(9);
					owner.getActionSender().sendInventory();
				}
			}
		});
		return true;
	}
	
	private boolean doCutGem(Player player, final InvItem chisel, final InvItem gem) {
		final ItemGemDef gemDef = EntityHandler.getItemGemDef(gem.getID());
		if(gemDef == null) {
      			return false;
		}
		if(player.getCurStat(12) < gemDef.getReqLevel()) {
			player.getActionSender().sendMessage("You need a crafting level of " + gemDef.getReqLevel() + " to cut this gem");
      			return true;
		}
		world.getDelayedEventHandler().add(new MiniEvent(player) {
			public void action() {
				if(owner.getInventory().remove(gem) > -1) {
					InvItem cutGem = new InvItem(gemDef.getGemID(), 1);
					owner.getActionSender().sendMessage("You cut the " + cutGem.getDef().getName());
					owner.getActionSender().sendSound("chisel");
					owner.getInventory().add(cutGem);
					owner.incExp(12, gemDef.getExp(), true, true);
					owner.getActionSender().sendStat(12);
					owner.getActionSender().sendInventory();
				}
			}
		});
		return true;
	}
	
	private boolean doArrowHeads(Player player, final InvItem headlessArrows, final InvItem arrowHeads) {
		final ItemArrowHeadDef headDef = EntityHandler.getItemArrowHeadDef(arrowHeads.getID());
		if(headDef == null) {
      			return false;
		}
		if(player.getCurStat(9) < headDef.getReqLevel()) {
			player.getActionSender().sendMessage("You need a fletching level of " + headDef.getReqLevel() + " to attach those.");
      			return true;
		}
		int amount = 10;
		if(headlessArrows.getAmount() < amount) {
			amount = headlessArrows.getAmount();
		}
		if(arrowHeads.getAmount() < amount) {
			amount = arrowHeads.getAmount();
		}
		final int amt = amount;
		world.getDelayedEventHandler().add(new MiniEvent(player) {
			public void action() {
				if(owner.getInventory().remove(headlessArrows.getID(), amt) > -1 && owner.getInventory().remove(arrowHeads.getID(), amt) > -1) {
					owner.getActionSender().sendMessage("You attach the heads to the arrows");
					owner.getInventory().add(new InvItem(headDef.getArrowID(), amt));
					owner.incExp(9, (int)(headDef.getExp() * (double)amt), true, true);
					owner.getActionSender().sendStat(9);
					owner.getActionSender().sendInventory();
				}
			}
		});
		return true;
	}
	
	private boolean doBowString(Player player, final InvItem bowString, final InvItem bow) {
		final ItemBowStringDef stringDef = EntityHandler.getItemBowStringDef(bow.getID());
		if(stringDef == null) {
      			return false;
		}
		if(player.getCurStat(9) < stringDef.getReqLevel()) {
			player.getActionSender().sendMessage("You need a fletching level of " + stringDef.getReqLevel() + " to do that.");
      			return true;
		}
		world.getDelayedEventHandler().add(new MiniEvent(player) {
			public void action() {
				if(owner.getInventory().remove(bowString) > -1 && owner.getInventory().remove(bow) > -1) {
					owner.getActionSender().sendMessage("You add the bow string to the bow");
					owner.getInventory().add(new InvItem(stringDef.getBowID(), 1));
					owner.incExp(9, stringDef.getExp(), true, true);
					owner.getActionSender().sendStat(9);
					owner.getActionSender().sendInventory();
				}
			}
		});
		return true;
	}
	
	private boolean doLogCut(Player player, final InvItem knife, final InvItem log) {
		final ItemLogCutDef cutDef = EntityHandler.getItemLogCutDef(log.getID());
		if(cutDef == null) {
      			return false;
		}
		world.getDelayedEventHandler().add(new MiniEvent(player) {
			public void action() {
				String[] options = new String[]{"Arrow shafts", "Shortbow", "Longbow", "Cancel"};
				owner.setMenuHandler(new MenuHandler(options) {
					public void handleReply(final int option, final String reply) {
						InvItem result;
						int reqLvl, exp;
						switch(option) {
							case 0:
								result = new InvItem(280, cutDef.getShaftAmount());
								reqLvl = cutDef.getShaftLvl();
								exp = cutDef.getShaftExp();
								break;
							case 1:
								result = new InvItem(cutDef.getShortbowID(), 1);
								reqLvl = cutDef.getShortbowLvl();
								exp = cutDef.getShortbowExp();
								break;
							case 2:
								result = new InvItem(cutDef.getLongbowID(), 1);
								reqLvl = cutDef.getLongbowLvl();
								exp = cutDef.getLongbowExp();
								break;
							default:
								return;
						}
						if(owner.getCurStat(9) < reqLvl) {
							owner.getActionSender().sendMessage("You need a fletching level of " + reqLvl + " to cut that.");
      							return;
						}
						if(owner.getInventory().remove(log) > -1) {
							owner.getActionSender().sendMessage("You make a " + result.getDef().getName());
							owner.getInventory().add(result);
							owner.incExp(9, exp, true, true);
							owner.getActionSender().sendStat(9);
							owner.getActionSender().sendInventory();
						}
					}
				});
				owner.getActionSender().sendMenu(options);
			}
		});
		return true;
	}
	
	private boolean doHerblaw(Player player, final InvItem vial, final InvItem herb) {
		final ItemHerbDef herbDef = EntityHandler.getItemHerbDef(herb.getID());
      		if(herbDef == null) {
      			return false;
      		}
      		if(player.getCurStat(15) < herbDef.getReqLevel()) {
      			player.getActionSender().sendMessage("You need a herblaw level of " + herbDef.getReqLevel() + " to mix those.");
      			return true;
      		}
      		world.getDelayedEventHandler().add(new MiniEvent(player) {
			public void action() {
				if(owner.getInventory().remove(vial) > -1 && owner.getInventory().remove(herb) > -1) {
		      			owner.getActionSender().sendMessage("You add the " + herb.getDef().getName() + " to the water");
		      			owner.getInventory().add(new InvItem(herbDef.getPotionId(), 1));
		      			owner.incExp(15, herbDef.getExp(), true, true);
		      			owner.getActionSender().sendStat(15);
		      			owner.getActionSender().sendInventory();
		      		}
		      	}
		});
      		return true;
	}
}