package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.entityhandling.EntityHandler;
import org.rscdaemon.server.entityhandling.defs.extras.*;
import org.rscdaemon.server.event.WalkToObjectEvent;
import org.rscdaemon.server.event.ShortEvent;
import org.rscdaemon.server.event.MiniEvent;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.util.Formulae;
import org.rscdaemon.server.states.Action;

import org.apache.mina.common.IoSession;

import java.util.List;

public class InvUseOnObject implements PacketHandler {
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
		ActiveTile tile = world.getTile(p.readShort(), p.readShort());
		if(tile == null) {
			player.setSuspiciousPlayer(true);
			player.resetPath();
			return;
		}
		GameObject object = tile.getGameObject();
		InvItem item;
		switch(pID) {
			case 36: // Use Item on Door
				int dir = p.readByte();
				item = player.getInventory().get(p.readShort());
				if(object == null || object.getType() == 0 || item == null) { // This shoudln't happen
					player.setSuspiciousPlayer(true);
					return;
				}
				handleDoor(player, tile, object, dir, item);
				break;
			case 94: // Use Item on GameObject
				item = player.getInventory().get(p.readShort());
				if(object == null || object.getType() == 1 || item == null) { // This shoudln't happen
					player.setSuspiciousPlayer(true);
					return;
				}
				handleObject(player, tile, object, item);
				break;
		}
	}
	
	private void handleObject(final Player player, final ActiveTile tile, final GameObject object, final InvItem item) {
		player.setStatus(Action.USING_INVITEM_ON_OBJECT);
		world.getDelayedEventHandler().add(new WalkToObjectEvent(player, object, false) {
			public void arrived() {
				owner.resetPath();
				if(owner.isBusy() || owner.isRanging() || !owner.getInventory().contains(item) || !owner.nextTo(object) || !tile.hasGameObject() || !tile.getGameObject().equals(object) || owner.getStatus() != Action.USING_INVITEM_ON_OBJECT) {
					return;
				}
				owner.resetAll();
				String[] options;
		      		switch(object.getID()) {
						case 946: // Cannon Base
							if(!itemId(new int[]{1033})) {
								owner.getActionSender().sendMessage("Nothing interesting happens.");
								return;
							}
							owner.setBusy(true);
							Bubble base = new Bubble(owner, 1033);
							for(Player p : owner.getViewArea().getPlayersInView()) {
								p.informOfBubble(base);
							}
							owner.getActionSender().sendMessage("You add the stand to the cannon base..");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getInventory().remove(1033, 1);
									owner.getActionSender().sendInventory();
									world.unregisterGameObject(object);
									final GameObject cannonBase = new GameObject(object.getLocation(), 947, 0, 0);
									world.registerGameObject(cannonBase);
									owner.setBusy(false);
								}
							});
							break;
						case 947: // Cannon Barrel
							if(!itemId(new int[]{1034})) {
								owner.getActionSender().sendMessage("Nothing interesting happens.");
								return;
							}
							owner.setBusy(true);
							Bubble barrel = new Bubble(owner, 1034);
							for(Player p : owner.getViewArea().getPlayersInView()) {
								p.informOfBubble(barrel);
							}
							owner.getActionSender().sendMessage("You add the barrels to the cannon..");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getInventory().remove(1034, 1);
									owner.getActionSender().sendInventory();
									world.unregisterGameObject(object);
									final GameObject cannonBarrel = new GameObject(object.getLocation(), 948, 0, 0);
									world.registerGameObject(cannonBarrel);
									owner.setBusy(false);
								}
							});
							break;
						case 948: // Cannon Furnace
							if(!itemId(new int[]{1035})) {
								owner.getActionSender().sendMessage("Nothing interesting happens.");
								return;
							}
							owner.setBusy(true);
							Bubble furnace = new Bubble(owner, 1035);
							for(Player p : owner.getViewArea().getPlayersInView()) {
								p.informOfBubble(furnace);
							}
							owner.getActionSender().sendMessage("You add the furnace to the cannon..");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getInventory().remove(1035, 1);
									owner.getActionSender().sendInventory();
									world.unregisterGameObject(object);
									final GameObject cannonFurnace = new GameObject(object.getLocation(), 943, 0, 0);
									world.registerGameObject(cannonFurnace);
									owner.setBusy(false);
								}
							});
							break;
		      			case 282: // Fountain of Heroes
		      				if(item.getID() == 522) {
		      					owner.getActionSender().sendMessage("You dip the amulet in the fountain...");
		      					owner.setBusy(true);
		      					world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      						public void action() {
									owner.getActionSender().sendMessage("You feel more power coming from it than before.");
									world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      								public void action() {
		      									if(owner.getInventory().remove(item) > -1) {
												owner.getActionSender().sendMessage("You can now rub it to teleport.");
						      						owner.getInventory().add(new InvItem(597));
						      						owner.getActionSender().sendInventory();
						      					}
						      					owner.setBusy(false);
		      								}
		      							});
		      						}
		      					});
		      					break;
		      				}
		      			case 2: // Well
		      			case 466: // Well
		      			case 814: // Well
		      			case 48: // Sink
		      			case 26: // Fountain
		      			case 1130: // Fountain
		      				if(!itemId(new int[]{21, 140, 465}) && !itemId(Formulae.potionsUnfinished) && !itemId(Formulae.potions1Dose) && !itemId(Formulae.potions2Dose) && !itemId(Formulae.potions3Dose)) {
		      					owner.getActionSender().sendMessage("Nothing interesting happens.");
		      					return;
		      				}
		      				if(owner.getInventory().remove(item) > -1) {
		      					showBubble();
				      			owner.getActionSender().sendSound("filljug");
			      				switch(item.getID()) {
			      					case 21:
			      						owner.getInventory().add(new InvItem(50));
			      						break;
			      					case 140:
			      						owner.getInventory().add(new InvItem(141));
			      						break;
			      					default:
			      						owner.getInventory().add(new InvItem(464));
			      						break;
			      				}
			      				owner.getActionSender().sendInventory();
		      				}
		      				break;
						case 134: // Compost Heap @draynor manor
							if(item.getID() == 211) { // Spade
								if(player.getErnestTheChickenStatus() >= 2 && player.getErnestTheChickenStatus() < 6) {
									owner.setBusy(true);
									owner.getActionSender().sendMessage("You dig around in the compost heap and find a key");
									world.getDelayedEventHandler().add(new ShortEvent(owner) {
										public void action() {
											player.getInventory().add(new InvItem(732, 1));
											player.getActionSender().sendInventory();
										}
									});
									owner.setBusy(false);
								}
							}
							break;
						case 86: // Fountain @draynor manor
							if(item.getID() == 178) { // Poisoned Fish Food
								if(player.getErnestTheChickenStatus() >= 2 && player.getErnestTheChickenStatus() < 6) {
									owner.setBusy(true);
									owner.getActionSender().sendMessage("You pour the poisoned fish food into the fountain");
									world.getDelayedEventHandler().add(new ShortEvent(owner) {
										public void action() {
											owner.getActionSender().sendMessage("The fish start to eat the poisoned fish food!");
											world.getDelayedEventHandler().add(new ShortEvent(owner) {
												public void action() {
													owner.getActionSender().sendMessage("The fish begin to float up to the top. They are dead!");
													owner.getInventory().remove(178, 1);
													owner.getActionSender().sendInventory();
													owner.setErnestTheChickenStatus(3);
												}
											});
										}
									});
									owner.setBusy(false);
								}
							}
							break;
						case 77: // Varrock Drain
							if(item.getID() == 50) { // Bucket of Water
								owner.setBusy(true);
								showBubble();
								owner.getActionSender().sendMessage("You wash the key down the drain. The key lands in the sewers.");
								world.getDelayedEventHandler().add(new ShortEvent(owner) {
									public void action() {
										world.registerItem(new Item(51, 117, 3295, 1, owner));
										owner.getInventory().remove(50, 1);
										owner.getInventory().add(new InvItem(21, 1));
										owner.getActionSender().sendInventory();
									}
								});
								owner.setBusy(false);
							}
							break;
						case 187: // Pirate's Treasure Chest
							if(item.getID() == 382 && player.getPiratesTreasureStatus() == 4) {
								owner.setBusy(true);
								showBubble();
								owner.getActionSender().sendMessage("You unlock the chest and find a note inside.");
								world.getDelayedEventHandler().add(new ShortEvent(owner) {
									public void action() {
										owner.getInventory().remove(382, 1);
										owner.getInventory().add(new InvItem(926, 1));
										owner.getActionSender().sendInventory();
										owner.setPiratesTreasureStatus(5);
									}
								});
								owner.setBusy(false);
							}
							break;
						case 182: // Karamja Banana Crate
							if(item.getID() == 318 && player.getPiratesTreasureStatus() == 1) {
								owner.setBusy(true);
								owner.getActionSender().sendMessage("You hide the rum in the crate with a bunch of banana's");
								world.getDelayedEventHandler().add(new ShortEvent(owner) {
									public void action() {
										owner.getInventory().remove(318, 1);
										owner.getActionSender().sendInventory();
										owner.setPiratesTreasureStatus(2);
									}
								});
								owner.setBusy(false);
							}
							break;
						case 40: // Restless Ghost Coffin
							if(item.getID() == 27 && player.getRestlessGhostStatus() == 4) { // Skull
								owner.setBusy(true);
								showBubble();
								owner.getActionSender().sendMessage("You gently place the skull into the coffin");
								world.getDelayedEventHandler().add(new ShortEvent(owner) {
									public void action() {
										owner.getInventory().remove(27, 1);
										owner.getActionSender().sendInventory();
										owner.getActionSender().sendMessage("@gre@Congratulations@whi@! You have just completed @or1@The Restless Ghost @whi@quest!");
										owner.getActionSender().sendMessage("@gre@You gained @or1@1 @gre@quest point!");
										owner.setQuestPoints(owner.getQuestPoints() +1);
										owner.getActionSender().sendQuestPoints();
										owner.isRestlessGhostComplete();
										owner.getActionSender().sendRestlessGhostComplete();
										owner.incExp(5, 1125, false, false);
										owner.getActionSender().sendStat(5);
									}
								});
								owner.setBusy(false);
							}
							break;
		      			case 97: // Fire
							if(item.getID() == 132) { // Cooked Meat
								owner.setBusy(true);
								showBubble();
								owner.getActionSender().sendSound("cooking");
								owner.getActionSender().sendMessage("You cooked the meat on the  " + object.getGameObjectDef().getName() + ".");
			      				world.getDelayedEventHandler().add(new ShortEvent(owner) {
			      					public void action() {
									if(owner.getInventory().remove(item) > -1) {
				      						owner.getActionSender().sendMessage("You burn the meat");
				      						owner.getInventory().add(new InvItem(134, 1));
				      						owner.getActionSender().sendInventory();
										}
										owner.setBusy(false);
									}
							});
						}
						break;
		      			case 11:
		      			case 119:
		      			case 274:
		      			case 435:
		      			case 491: // Range
		      				if(item.getID() == 622) { // Seaweed (Glass)
		      					owner.setBusy(true);
			      				showBubble();
			      				owner.getActionSender().sendSound("cooking");
			      				owner.getActionSender().sendMessage("You put the seaweed on the  " + object.getGameObjectDef().getName() + ".");
			      				world.getDelayedEventHandler().add(new ShortEvent(owner) {
			      					public void action() {
									if(owner.getInventory().remove(item) > -1) {
				      						owner.getActionSender().sendMessage("The seaweed burns to ashes");
				      						owner.getInventory().add(new InvItem(624, 1));
				      						owner.getActionSender().sendInventory();
				      					}
				      					owner.setBusy(false);
			      					}
			      				});
		      				}
		      				else {
			      				final ItemCookingDef cookingDef = item.getCookingDef();
			      				if(cookingDef == null) {
			      					owner.getActionSender().sendMessage("Nothing interesting happens.");
			      					return;
			      				}
			      				if(owner.getCurStat(7) < cookingDef.getReqLevel()) {
			      					owner.getActionSender().sendMessage("You need a cooking level of " + cookingDef.getReqLevel() + " to cook this.");
			      					return;
			      				}
			      				owner.setBusy(true);
			      				showBubble();
			      				owner.getActionSender().sendSound("cooking");
			      				owner.getActionSender().sendMessage("You cook the " + item.getDef().getName() + " on the " + object.getGameObjectDef().getName() + ".");
			      				world.getDelayedEventHandler().add(new ShortEvent(owner) {
			      					public void action() {
									InvItem cookedFood = new InvItem(cookingDef.getCookedId());
									if(owner.getInventory().remove(item) > -1) {
										if(!Formulae.burnFood(item.getID(), owner.getCurStat(7))) {
											owner.getInventory().add(cookedFood);
											owner.getActionSender().sendMessage("The " + item.getDef().getName() + " is now nicely cooked.");
											owner.incExp(7, cookingDef.getExp(), true, true);
											owner.getActionSender().sendStat(7);
										}
										else {
											owner.getInventory().add(new InvItem(cookingDef.getBurnedId()));
											owner.getActionSender().sendMessage("You accidently burn the " + item.getDef().getName() + ".");
										}
										owner.getActionSender().sendInventory();
									}
									owner.setBusy(false);
			      					}
			      				});
		      				}
		      				break;
		      			case 118:
		      			case 813: // Furnace
		      				if(item.getID() == 172) { // Gold Bar (Crafting)
      							world.getDelayedEventHandler().add(new MiniEvent(owner) {
      		      		      				public void action() {
				      					owner.getActionSender().sendMessage("What would you like to make?");
				      					String[] options = new String[]{"Ring", "Necklace", "Amulet"};
				      					owner.setMenuHandler(new MenuHandler(options) {
		      								public void handleReply(int option, String reply) {
		      									if(owner.isBusy() || option < 0 || option > 2) {
		      										return;
		      									}
		      									final int[] moulds = {293, 295, 294};
		      									final int[] gems = {-1, 164, 163, 162, 161, 523};
		      									String[] options = {"Gold", "Sapphire", "Emerald", "Ruby", "Diamond", "Dragonstone"};
		      									final int craftType = option;
		      									if(owner.getInventory().countId(moulds[craftType]) < 1) {
      	        										owner.getActionSender().sendMessage("You need a " + EntityHandler.getItemDef(moulds[craftType]).getName() + " to make a " + reply);
      	        										return;
      	        									}
      	        									owner.getActionSender().sendMessage("What type of " + reply + " would you like to make?");
      	        									owner.setMenuHandler(new MenuHandler(options) {
      	        										public void handleReply(int option, String reply) {
      	        											if(owner.isBusy() || option < 0 || option > 5) {
      	        												return;
      	        											}
      	        											if(option != 0 && owner.getInventory().countId(gems[option]) < 1) {
      	        												owner.getActionSender().sendMessage("You don't have a " + reply + ".");
      	        												return;
      	        											}
      	        											ItemCraftingDef def = EntityHandler.getCraftingDef((option * 3) + craftType);
		      											if(def == null) {
		      												owner.getActionSender().sendMessage("Nothing interesting happens.");
		      												return;
		      											}
		      											if(owner.getCurStat(12) < def.getReqLevel()) {
		      												owner.getActionSender().sendMessage("You need at crafting level of " + def.getReqLevel() + " to make this");
		      												return;
		      											}
		      		      									if(owner.getInventory().remove(item) > -1 && (option == 0 || owner.getInventory().remove(gems[option], 1) > -1)) {
		      												showBubble();
		      												InvItem result = new InvItem(def.getItemID(), 1);
		      												owner.getActionSender().sendMessage("You make a " + result.getDef().getName());
		      												owner.getInventory().add(result);
		      												owner.incExp(12, def.getExp(), true, true);
		      												owner.getActionSender().sendStat(12);
		      												owner.getActionSender().sendInventory();
		      											}
		        									}
		        								});
		        								owner.getActionSender().sendMenu(options);
		      								}
		      							});
		      							owner.getActionSender().sendMenu(options);
      								}
      							});
		      				}
		      				else if(item.getID() == 384) { // Silver Bar (Crafting)
		      					world.getDelayedEventHandler().add(new MiniEvent(owner) {
      		      		      				public void action() {
				      					owner.getActionSender().sendMessage("What would you like to make?");
				      					String[] options = new String[]{"Holy Symbol of Saradomin", "UnHoly Symbol of Zamorak"};
				      					owner.setMenuHandler(new MenuHandler(options) {
		      								public void handleReply(int option, String reply) {
		      									if(owner.isBusy() || option < 0 || option > 1) {
		      										return;
		      									}
		      									int[] moulds = {386, 1026};
		      									int[] results = {44, 1027};
		      									if(owner.getInventory().countId(moulds[option]) < 1) {
		        									owner.getActionSender().sendMessage("You need a " + EntityHandler.getItemDef(moulds[option]).getName() + " to make a " + reply);
		        									return;
		        								}
		        								if(owner.getCurStat(12) < 16) {
		      		      		      						owner.getActionSender().sendMessage("You need a crafting level of 16 to make this");
		      		      		      						return;
		      		      		      					}
		      									if(owner.getInventory().remove(item) > -1) {
		      										showBubble();
		      										InvItem result = new InvItem(results[option]);
		      										owner.getActionSender().sendMessage("You make a " + result.getDef().getName());
		      										owner.getInventory().add(result);
		      										owner.incExp(12, 50, true, true);
		      										owner.getActionSender().sendStat(12);
		      										owner.getActionSender().sendInventory();
		      									}
		      								}
									});
									owner.getActionSender().sendMenu(options);
      								}
      							});
		      				}
		      				else if(item.getID() == 625) { // Sand (Glass)
		      					if(player.getInventory().countId(624) < 1) {
		      						owner.getActionSender().sendMessage("You need some soda ash to mix the sand with.");
		      						return;
		      					}
		      					owner.setBusy(true);
			      				showBubble();
			      				owner.getActionSender().sendMessage("You put the seaweed and the soda ash in the furnace.");
			      				world.getDelayedEventHandler().add(new ShortEvent(owner) {
			      					public void action() {
									if(player.getInventory().remove(624, 1) > -1 && player.getInventory().remove(item) > -1) {
				      						owner.getActionSender().sendMessage("It mixes to make some molten glass");
				      						owner.getInventory().add(new InvItem(623, 1));
				      						owner.incExp(12, 20, true, true);
				      						owner.getActionSender().sendStat(12);
				      						owner.getActionSender().sendInventory();
				      					}
				      					owner.setBusy(false);
			      					}
			      				});
		      				}
		      				else {
			      				ItemSmeltingDef smeltingDef = item.getSmeltingDef();
			      				if(smeltingDef == null) {
			      					owner.getActionSender().sendMessage("Nothing interesting happens.");
			      					return;
			      				}
				      			for(ReqOreDef reqOre : smeltingDef.getReqOres()) {
				      				if(owner.getInventory().countId(reqOre.getId()) < reqOre.getAmount()) {
				      					if(item.getID() == 151) {
				      						smeltingDef = EntityHandler.getItemSmeltingDef(9999);
				      						break;
				      					}
				      					owner.getActionSender().sendMessage("You need " + reqOre.getAmount() + " " + EntityHandler.getItemDef(reqOre.getId()).getName() + " to smelt a " + item.getDef().getName() + ".");
				      					return;
				      				}
			      				}
	      		      				if(owner.getCurStat(13) < smeltingDef.getReqLevel()) {
	      		      					owner.getActionSender().sendMessage("You need a smithing level of " + smeltingDef.getReqLevel() + " to smelt this.");
	      		      					return;
	      		      				}
			      				owner.setBusy(true);
			      				showBubble();
			      				owner.getActionSender().sendMessage("You smelt the " + item.getDef().getName() + " in the furnace.");
			      				final ItemSmeltingDef def = smeltingDef;
			      				world.getDelayedEventHandler().add(new ShortEvent(owner) {
			      					public void action() {
									InvItem bar = new InvItem(def.getBarId());
									if(owner.getInventory().remove(item) > -1) {
								      		for(ReqOreDef reqOre : def.getReqOres()) {
								      			for(int i = 0;i < reqOre.getAmount();i++) {
								      				owner.getInventory().remove(new InvItem(reqOre.getId()));
								      			}
								      		}
								      		if(item.getID() == 151 && def.getReqOres().length == 0 && DataConversions.random(0, 1) == 1) {
								      			owner.getActionSender().sendMessage("The ore is too impure and unable to be refined.");
								      		}
								      		else {
											owner.getInventory().add(bar);
											owner.getActionSender().sendMessage("You retrieve a " + bar.getDef().getName() + ".");
											owner.incExp(13, def.getExp(), true, true);
											owner.getActionSender().sendStat(13);
								      		}
										owner.getActionSender().sendInventory();
									}
									owner.setBusy(false);
			      					}
			      				});
		      				}
		      				break;
		      			case 50:
		      			case 177: // Anvil
							if((item.getID() == 1276) || (item.getID() == 1277)) {
								handleShieldBinding(item.getID(), item.getID(), owner.getCurStat(13), 0);
								return;
							}
		      				int minSmithingLevel = Formulae.minSmithingLevel(item.getID());
		      				if(minSmithingLevel < 0) {
		      					owner.getActionSender().sendMessage("Nothing interesting happens.");
		      					return;
		      				}
		      				if(owner.getInventory().countId(168) < 1) {
		      					owner.getActionSender().sendMessage("You need a hammer to work the metal with.");
		      					return;
		      				}
		      				if(owner.getCurStat(13) < minSmithingLevel) {
  		      					owner.getActionSender().sendMessage("You need a smithing level of " + minSmithingLevel + " to use this type of bar");
  		      					return;
  		      				}
      		      			options = new String[]{"Make Weapon", "Make Armour", "Make Missile Heads", "Cancel"};
							owner.setMenuHandler(new MenuHandler(options) {
							public void handleReply(int option, String reply) {
								if(owner.isBusy()) {
									return;
								}
								String[] options;
								switch(option) {
									case 0:
										owner.getActionSender().sendMessage("Choose a type of weapon to make");
										options = new String[]{"Dagger", "Throwing Knife", "Sword", "Axe", "Mace"};
										owner.setMenuHandler(new MenuHandler(options) {
											public void handleReply(int option, String reply) {
												if(owner.isBusy()) {
													return;
												}
												String[] options;
												switch(option) {
													case 0:
														handleSmithing(item.getID(), 0);
														break;
													case 1:
														handleSmithing(item.getID(), 1);
														break;
													case 2:
														owner.getActionSender().sendMessage("What sort of sword do you want to make?");
														options = new String[]{"Short Sword", "Long Sword (2 bars)", "Scimitar (2 bars)", "2-handed Sword (3 bars)"};
														owner.setMenuHandler(new MenuHandler(options) {
															public void handleReply(int option, String reply) {
																if(owner.isBusy()) {
																	return;
																}
																switch(option) {
																	case 0:
																		handleSmithing(item.getID(), 2);
																		break;
																	case 1:
																		handleSmithing(item.getID(), 3);
																		break;
																	case 2:
																		handleSmithing(item.getID(), 4);
																		break;
																	case 3:
																		handleSmithing(item.getID(), 5);
																		break;
																	default:
																		return;
																}
															}
														});
														owner.getActionSender().sendMenu(options);
														break;
													case 3:
														owner.getActionSender().sendMessage("What sort of axe do you want to make?");
														options = new String[]{"Hatchet", "Pickaxe", "Battle Axe (3 bars)"};
														owner.setMenuHandler(new MenuHandler(options) {
															public void handleReply(int option, String reply) {
																if(owner.isBusy()) {
																	return;
																}
																switch(option) {
																	case 0:
																		handleSmithing(item.getID(), 6);
																		break;
																	case 1:
																		handleSmithing(item.getID(), 7);
																		break;
																	case 2:
																		handleSmithing(item.getID(), 8);
																		break;
																	default:
																		return;
																}
															}
														});
														owner.getActionSender().sendMenu(options);
														break;
													case 4:
														handleSmithing(item.getID(), 9);
														break;
													default:
														return;
												}
											}
										});
										owner.getActionSender().sendMenu(options);
										break;
									case 1:
										owner.getActionSender().sendMessage("Choose a type of armour to make");
										options = new String[]{"Helmet", "Shield", "Armour"};
										owner.setMenuHandler(new MenuHandler(options) {
											public void handleReply(int option, String reply) {
												if(owner.isBusy()) {
													return;
												}
												switch(option) {
													case 0:
														owner.getActionSender().sendMessage("What sort of helmet do you want to make?");
														options = new String[]{"Medium Helmet", "Large Helmet (2 bars)"};
														owner.setMenuHandler(new MenuHandler(options) {
															public void handleReply(int option, String reply) {
																if(owner.isBusy()) {
																	return;
																}
																switch(option) {
																	case 0:
																		handleSmithing(item.getID(), 10);
																		break;
																	case 1:
																		handleSmithing(item.getID(), 11);
																		break;
      																	default:
      																		return;
      																}
      															}
      														});
      														owner.getActionSender().sendMenu(options);
      														break;
      													case 1:
      														owner.getActionSender().sendMessage("What sort of shield do you want to make?");
      														options = new String[]{"Square Shield (2 bars)", "Kite Shield (3 bars)"};
      														owner.setMenuHandler(new MenuHandler(options) {
      															public void handleReply(int option, String reply) {
      																if(owner.isBusy()) {
      																	return;
      																}
      																switch(option) {
      																	case 0:
      																		handleSmithing(item.getID(), 12);
      																		break;
      																	case 1:
      																		handleSmithing(item.getID(), 13);
      																		break;
      																	default:
      																		return;
      																}
      															}
      														});
      														owner.getActionSender().sendMenu(options);
      														break;
      													case 2:
      														owner.getActionSender().sendMessage("What sort of armour do you want to make?");
      														options = new String[]{"Chain Mail Body (3 bars)", "Plate Mail Body (5 bars)", "Plate Mail Legs (3 bars)", "Plated Skirt (3 bars)"};
      														owner.setMenuHandler(new MenuHandler(options) {
      															public void handleReply(int option, String reply) {
      																if(owner.isBusy()) {
      																	return;
      																}
      																switch(option) {
      																	case 0:
      																		handleSmithing(item.getID(), 14);
      																		break;
      																	case 1:
      																		handleSmithing(item.getID(), 15);
      																		break;
      																	case 2:
      																		handleSmithing(item.getID(), 16);
      																		break;
      																	case 3:
      																		handleSmithing(item.getID(), 17);
      																		break;
      																	default:
      																		return;
      																}
      															}
      														});
      														owner.getActionSender().sendMenu(options);
      														break;
      													default:
      														return;
      												}
      											}
      										});
      										owner.getActionSender().sendMenu(options);
      										break;
      									case 2:
      										options = new String[]{"Make 10 Arrow Heads", "Make 50 Arrow Heads (5 bars)", "Forge Dart Tips", "Cancel"};
      										owner.setMenuHandler(new MenuHandler(options) {
      											public void handleReply(int option, String reply) {
      												if(owner.isBusy()) {
      													return;
      												}
      												switch(option) {
      													case 0:
      														handleSmithing(item.getID(), 18);
      														break;
      													case 1:
      														handleSmithing(item.getID(), 19);
      														break;
      													case 2:
      														handleSmithing(item.getID(), 20);
      														break;
      													default:
      														return;
      												}
      											}
      										});
      										owner.getActionSender().sendMenu(options);
      										break;
      									default:
      										return;
      								}
      							}
      						});
      						owner.getActionSender().sendMenu(options);
		      				break;
						case 1189: // Cook's Range
							final ItemCookingDef cookingDef = item.getCookingDef();
							if(cookingDef == null) {
								owner.getActionSender().sendMessage("Nothing interesting happens.");
								return;
							}
							if((owner.getCooksAssistantStatus() <= 1) && (!owner.getLocation().onTutorialIsland())) {
								owner.getActionSender().sendMessage("You need to complete the Cook's Assistant Quest before you can use his range.");
								return;
							}
							if(owner.getCurStat(7) < cookingDef.getReqLevel()) {
								owner.getActionSender().sendMessage("You need a cooking level of " + cookingDef.getReqLevel() + " to cook this.");
								return;
							}
							if((owner.getCooksAssistantStatus() > 1) || (owner.getLocation().onTutorialIsland())) {
								owner.setBusy(true);
								showBubble();
								owner.getActionSender().sendSound("cooking");
								owner.getActionSender().sendMessage("You cook the " + item.getDef().getName() + " on the " + object.getGameObjectDef().getName() + ".");
								world.getDelayedEventHandler().add(new ShortEvent(owner) {
									public void action() {
									InvItem cookedFood = new InvItem(cookingDef.getCookedId());
									if(owner.getInventory().remove(item) > -1) {
										if(!Formulae.burnFood(item.getID(), owner.getCurStat(7))) {
											owner.getInventory().add(cookedFood);
											owner.getActionSender().sendMessage("The " + item.getDef().getName() + " is now nicely cooked.");
											owner.incExp(7, cookingDef.getExp(), true, true);
											owner.getActionSender().sendStat(7);
											if(!owner.getLocation().onTutorialIsland()) {
												owner.incExp(7, cookingDef.getExp(), true, true);
												owner.getActionSender().sendStat(7);
												owner.getActionSender().sendMessage("You gained @gre@double @whi@experience!");
											}
										} else {
												owner.getInventory().add(new InvItem(cookingDef.getBurnedId()));
												owner.getActionSender().sendMessage("You accidently burn the " + item.getDef().getName() + ".");
											}
											owner.getActionSender().sendInventory();
										}
										owner.setBusy(false);
									}
								});
							}
							break;
		      			case 236: // Cauldron of Thunder
		      				if(item.getID() == 133) {
		      					owner.getActionSender().sendMessage("You dip the meat into the Cauldron of Thunder.");
		      					owner.setBusy(true);
		      					world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      						public void action() {
									owner.getActionSender().sendMessage("The meat has now been enchanted.");
									world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      								public void action() {
		      									if(owner.getInventory().remove(item) > -1) {
						      						owner.getInventory().add(new InvItem(508));
						      						owner.getActionSender().sendInventory();
						      					}
						      					owner.setBusy(false);
		      								}
		      							});
		      						}
		      					});
		      					break;
		      				} else if(item.getID() == 502) {
		      					owner.getActionSender().sendMessage("You dip the meat into the Cauldron of Thunder.");
		      					owner.setBusy(true);
		      					world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      						public void action() {
									owner.getActionSender().sendMessage("The meat has now been enchanted.");
									world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      								public void action() {
		      									if(owner.getInventory().remove(item) > -1) {
						      						owner.getInventory().add(new InvItem(505));
						      						owner.getActionSender().sendInventory();
						      					}
						      					owner.setBusy(false);
		      								}
		      							});
		      						}
		      					});
		      					break;
		      				} else if(item.getID() == 503) {
		      					owner.getActionSender().sendMessage("You dip the meat into the Cauldron of Thunder.");
		      					owner.setBusy(true);
		      					world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      						public void action() {
									owner.getActionSender().sendMessage("The meat has now been enchanted.");
									world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      								public void action() {
		      									if(owner.getInventory().remove(item) > -1) {
						      						owner.getInventory().add(new InvItem(506));
						      						owner.getActionSender().sendInventory();
						      					}
						      					owner.setBusy(false);
		      								}
		      							});
		      						}
		      					});
		      					break;
		      				} else if(item.getID() == 504) {
		      					owner.getActionSender().sendMessage("You dip the meat into the Cauldron of Thunder.");
		      					owner.setBusy(true);
		      					world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      						public void action() {
									owner.getActionSender().sendMessage("The meat has now been enchanted.");
									world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      								public void action() {
		      									if(owner.getInventory().remove(item) > -1) {
						      						owner.getInventory().add(new InvItem(507));
						      						owner.getActionSender().sendInventory();
						      					}
						      					owner.setBusy(false);
		      								}
		      							});
		      						}
		      					});
		      				}
							break;
		      			case 121: // Spinning Wheel
		      				switch(item.getID()) {
		      					case 145: // Wool
		      						owner.getActionSender().sendMessage("You spin the sheeps wool into a nice ball of wool");
				      				world.getDelayedEventHandler().add(new MiniEvent(owner) {
		      		      					public void action() {
				      						if(owner.getInventory().remove(item) > -1) {
				      							owner.getInventory().add(new InvItem(207, 1));
				      							owner.incExp(12, 3, true, true);
				      							owner.getActionSender().sendStat(12);
				      							owner.getActionSender().sendInventory();
				      						}
				      						owner.setBusy(false);
				      					}
				      				});
		      						break;
		      					case 675: // Flax
		      						if(owner.getCurStat(12) < 10) {
		      							owner.getActionSender().sendMessage("You need a crafting level of 10 to spin flax");
		      							return;
		      						}
		      						owner.getActionSender().sendMessage("You make the flax into a bow string");
		      						world.getDelayedEventHandler().add(new MiniEvent(owner) {
		      		      					public void action() {
				      						if(owner.getInventory().remove(item) > -1) {
				      							owner.getInventory().add(new InvItem(676, 1));
				      							owner.incExp(12, 15, true, true);
				      							owner.getActionSender().sendStat(12);
				      							owner.getActionSender().sendInventory();
				      						}
				      						owner.setBusy(false);
				      					}
				      				});
		      						break;
		      					default:
		      						owner.getActionSender().sendMessage("Nothing interesting happens.");
		      						return;
		      				}
		      				owner.setBusy(true);
		      				showBubble();
		      				owner.getActionSender().sendSound("mechanical");
		      				break;
		      			case 248: // Crystal key chest
		      				if(item.getID() != 525) {
		      					owner.getActionSender().sendMessage("Nothing interesting happens.");
		      					return;
		      				}
		      				owner.getActionSender().sendMessage("You use the key to unlock the chest");
		      				owner.setBusy(true);
		      				showBubble();
		      				world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      					public void action() {
		      						if(owner.getInventory().remove(item) > -1) {
		      							owner.getInventory().add(new InvItem(542, 1));
		      							List<InvItem> loot = Formulae.getKeyChestLoot();
		      							for(InvItem i : loot) {
		      								if(i.getAmount() > 1 && !i.getDef().isStackable()) {
		      									for(int x = 0;x < i.getAmount();x++) {
		      										owner.getInventory().add(new InvItem(i.getID(), 1));
		      									}
		      								}
		      								else {
		      									owner.getInventory().add(i);
		      								}
		      							}
		      							owner.getActionSender().sendInventory();
		      						}
		      						owner.setBusy(false);
		      					}
		      				});
		      				break;
		      			case 302: // Sandpit
		      				if(item.getID() != 21) {
		      					owner.getActionSender().sendMessage("Nothing interesting happens.");
		      					return;
		      				}
		      				owner.getActionSender().sendMessage("You fill the bucket with sand.");
		      				owner.setBusy(true);
		      				showBubble();
		      				world.getDelayedEventHandler().add(new MiniEvent(owner) {
		      		      			public void action() {
				      				if(owner.getInventory().remove(item) > -1) {
				      					owner.getInventory().add(new InvItem(625, 1));
				      					owner.getActionSender().sendInventory();
				      				}
				      				owner.setBusy(false);
				      			}
				      		});
		      				break;
		      			case 179: // Potters Wheel
		      				if(item.getID() != 243) {
		      					owner.getActionSender().sendMessage("Nothing interesting happens.");
		      					return;
		      				}
		      				owner.getActionSender().sendMessage("What would you like to make?");
		      					options = new String[]{"Pot", "Pie Dish", "Bowl", "Cancel"};
		      					owner.setMenuHandler(new MenuHandler(options) {
								public void handleReply(int option, String reply) {
									if(owner.isBusy()) {
										return;
									}
									int reqLvl, exp;
									InvItem result;
									switch(option) {
										case 0:
											result = new InvItem(279, 1);
											reqLvl = 1;
											exp = 6;
											break;
										case 1:
											result = new InvItem(278, 1);
											reqLvl = 4;
											exp = 10;
											break;
										case 2:
											result = new InvItem(340, 1);
											reqLvl = 7;
											exp = 10;
											break;
										default:
											owner.getActionSender().sendMessage("Nothing interesting happens.");
											return;
									}
  									if(owner.getCurStat(12) < reqLvl) {
		      		      						owner.getActionSender().sendMessage("You need a crafting level of " + reqLvl + " to make this");
		      		      						return;
		      		      					}
  									if(owner.getInventory().remove(item) > -1) {
  										showBubble();
  										owner.getActionSender().sendMessage("You make a " + result.getDef().getName());
  										owner.getInventory().add(result);
  										owner.incExp(12, exp, true, true);
  										owner.getActionSender().sendStat(12);
  										owner.getActionSender().sendInventory();
  									}
								}
							});
							owner.getActionSender().sendMenu(options);
		      				break;
		      			case 52:
						case 173: // Cooking Guild Hopper
		      				if(item.getID() != 29) {
								owner.getActionSender().sendMessage("Nothing interesting happens.");
		      					return;
							}
							if(object.getGrainable()){
								owner.getActionSender().sendMessage("There is already a grain in this machine.");
								return;
							}
							if(owner.getInventory().remove(item) > -1) {
									object.setGrainable(true);
									owner.getActionSender().sendMessage("You put the grain into the hopper.");
									owner.getActionSender().sendInventory();
							}
						break;
		      			case 178: // Potters Oven
		      				int reqLvl, xp, resultID;
		      				switch(item.getID()) {
		      					case 279: // Pot
		      						resultID = 135;
		      						reqLvl = 1;
		      						xp = 7;
		      						break;
		      					case 278: // Pie Dish
		      						resultID = 251;
		      						reqLvl = 4;
		      						xp = 15;
		      						break;
		      					case 340: // Bowl
		      						resultID = 341;
		      						reqLvl = 7;
		      						xp = 15;
		      						break;
		      					default:
		      						owner.getActionSender().sendMessage("Nothing interesting happens.");
		      						return;
		      				}
		      				if(owner.getCurStat(12) < reqLvl) {
      		      				owner.getActionSender().sendMessage("You need a crafting level of " + reqLvl + " to make this");
      		      				return;
      		      			}
      		      			final InvItem result = new InvItem(resultID, 1);
      		      			final int exp = xp;
      		      			final boolean fail = Formulae.crackPot(reqLvl, owner.getCurStat(12));
		      				showBubble();
		      				owner.getActionSender().sendMessage("You place the " + item.getDef().getName() + " in the oven");
		      				owner.setBusy(true);
		      				world.getDelayedEventHandler().add(new ShortEvent(owner) {
      		      					public void action() {
      				      				if(owner.getInventory().remove(item) > -1) {
      				      					if(fail) {
      				      						owner.getActionSender().sendMessage("The " + result.getDef().getName() + " cracks in the oven, you throw it away.");
      				      					}
      				      					else {
	      		      							owner.getActionSender().sendMessage("You take out the " + result.getDef().getName());
	      		      							owner.getInventory().add(result);
	      		      							owner.incExp(12, exp, true, true);
	      		      							owner.getActionSender().sendStat(12);
      				      					}
      				      					owner.getActionSender().sendInventory();
      		      						}
      		      						owner.setBusy(false);
      		      					}
			      			});
		      				break;
		      			default:
		      				owner.getActionSender().sendMessage("Nothing interesting happens.");
		      				return;
		      		}
			}
			
			private void handleShieldBinding(int leftHalf, int rightHalf, int levelToMake, int bindingExp) {
				leftHalf = 1276;
				rightHalf = 1277;
				levelToMake = 90;
				bindingExp = 2000;
				if(owner.getInventory().countId(168) < 1) {
					owner.getActionSender().sendMessage("You need a hammer to bind the shield halves together.");
					return;
				}
				if(owner.getCurStat(13) < levelToMake) {
					player.getActionSender().sendMessage("You need a smithing level of " + levelToMake + " to bind the shield together!");
					return;
				}
				if((owner.getInventory().countId(leftHalf) < 1) || (owner.getInventory().countId(rightHalf) < 1)) {
					player.getActionSender().sendMessage("You don't have both halves of the shield!");
					return;
				}
				owner.getActionSender().sendSound("anvil");
				owner.getInventory().remove(leftHalf, 1);
				owner.getInventory().remove(rightHalf, 1);
				owner.getInventory().add(new InvItem(1278, 1));
				owner.getActionSender().sendMessage("You bind the shield halves together!");
				owner.getActionSender().sendInventory();
				owner.incExp(13, bindingExp, true, true);
				owner.getActionSender().sendStat(13);
			}
			
			private void handleSmithing(int barID, int toMake) {
				ItemSmithingDef def = EntityHandler.getSmithingDef((Formulae.getBarType(barID) * 21) + toMake);
				if(def == null) {
					owner.getActionSender().sendMessage("Nothing interesting happens.");
					return;
				}
				if(owner.getCurStat(13) < def.getRequiredLevel()) {
					owner.getActionSender().sendMessage("You need at smithing level of " + def.getRequiredLevel() + " to make this");
					return;
				}
				if(owner.getInventory().countId(barID) < def.getRequiredBars()) {
					owner.getActionSender().sendMessage("You don't have enough bars to make this.");
					return;
				}
				owner.getActionSender().sendSound("anvil");
				for(int x = 0;x < def.getRequiredBars();x++) {
					owner.getInventory().remove(new InvItem(barID, 1));
				}
	  		      	Bubble bubble = new Bubble(owner, item.getID());
				for(Player p : owner.getViewArea().getPlayersInView()) {
					p.informOfBubble(bubble);
				}
				if(EntityHandler.getItemDef(def.getItemID()).isStackable()) {
					owner.getActionSender().sendMessage("You hammer the metal into some " + EntityHandler.getItemDef(def.getItemID()).getName());
					owner.getInventory().add(new InvItem(def.getItemID(), def.getAmount()));
				}
				else {
					owner.getActionSender().sendMessage("You hammer the metal into a " + EntityHandler.getItemDef(def.getItemID()).getName());
					for(int x = 0;x < def.getAmount();x++) {
						owner.getInventory().add(new InvItem(def.getItemID(), 1));
					}
				}
				owner.incExp(13, Formulae.getSmithingExp(barID, def.getRequiredBars()), true, true);
				owner.getActionSender().sendStat(13);
				owner.getActionSender().sendInventory();
			}
			
			private boolean itemId(int[] ids) {
				return DataConversions.inArray(ids, item.getID());
			}
			
			private void showBubble() {
				Bubble bubble = new Bubble(owner, item.getID());
				for(Player p : owner.getViewArea().getPlayersInView()) {
					p.informOfBubble(bubble);
				}
			}
		});
	}
	
	private void handleDoor(final Player player, final ActiveTile tile, final GameObject object, final int dir, final InvItem item) {
		player.setStatus(Action.USING_INVITEM_ON_DOOR);
		world.getDelayedEventHandler().add(new WalkToObjectEvent(player, object, false) {
			private void doDoor() {
				owner.getActionSender().sendSound("opendoor");
				world.registerGameObject(new GameObject(object.getLocation(), 11, object.getDirection(), object.getType()));
				world.delayedSpawnObject(object.getLoc(), 1000);
			}
		
			public void arrived() {
				owner.resetPath();
				if(owner.isBusy() || owner.isRanging() || !owner.getInventory().contains(item) || !tile.hasGameObject() || !tile.getGameObject().equals(object) || owner.getStatus() != Action.USING_INVITEM_ON_DOOR) {
					return;
				}
				owner.resetAll();
		      		switch(object.getID()) {
		      			case 24: // Web
		      				ItemWieldableDef def = item.getWieldableDef();
		      				if((def == null || def.getWieldPos() != 4) && item.getID() != 13) {
		      					owner.getActionSender().sendMessage("Nothing interesting happens.");
		      					return;
		      				}
		      				owner.getActionSender().sendMessage("You try to destroy the web");
		      				owner.setBusy(true);
		      				world.getDelayedEventHandler().add(new ShortEvent(owner) {
		      					public void action() {
				      				if(Formulae.cutWeb()) {
				      					owner.getActionSender().sendMessage("You slice through the web.");
				      					world.unregisterGameObject(object);
				      					world.delayedSpawnObject(object.getLoc(), 15000);
				      				}
				      				else {
				      					owner.getActionSender().sendMessage("You fail to cut through it.");
				      				}
				      				owner.setBusy(false);
		      					}
		      				});
		      				break;
		      			case 23: // Giant place near barb village
		      				if(!itemId(new int[]{99})) {
		      					owner.getActionSender().sendMessage("Nothing interesting happens.");
		      					return;
		      				}
		      				owner.getActionSender().sendMessage("You unlock the door and go through it");
		      				doDoor();
		      				if(owner.getY() <= 484) {
		      					owner.teleport(owner.getX(), 485, false);
		      				}
		      				else {
		      					owner.teleport(owner.getX(), 484, false);
		      				}
		      				break;
						case 35: // Door in Draynor Manor
							if(!itemId(new int[]{732})) {
								owner.getActionSender().sendMessage("Nothing interesting happens.");
								return;
							}
							owner.getActionSender().sendMessage("You unlock the door and go through it");
							doDoor();
							if(owner.getX() == 212) {
								owner.teleport(211, owner.getY(), false);
							} else
							if(owner.getX() == 211) {
								owner.teleport(212, owner.getY(), false);
							}
							break;
		      			case 60: // Melzars maze
		      				if(!itemId(new int[]{421})) {
		      					owner.getActionSender().sendMessage("Nothing interesting happens.");
		      					return;
		      				}
		      				owner.getActionSender().sendMessage("You unlock the door and go through it");
		      				doDoor();
		      				if(owner.getX() <= 337) {
		      					owner.teleport(338, owner.getY(), false);
		      				}
		      				break;
		      			default:
		      				owner.getActionSender().sendMessage("Nothing interesting happens.");
		      				return;
				}
				owner.getActionSender().sendInventory();
			}
			
			private boolean itemId(int[] ids) {
				return DataConversions.inArray(ids, item.getID());
			}
		});
	}
	
}