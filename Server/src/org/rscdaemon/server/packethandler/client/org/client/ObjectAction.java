package org.rscdaemon.server.packethandler.client;

import org.rscdaemon.server.packethandler.PacketHandler;
import org.rscdaemon.server.model.*;
import org.rscdaemon.server.net.Packet;
import org.rscdaemon.server.net.RSCPacket;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.entityhandling.EntityHandler;
import org.rscdaemon.server.entityhandling.defs.GameObjectDef;
import org.rscdaemon.server.entityhandling.defs.extras.*;
import org.rscdaemon.server.event.ShortEvent;
import org.rscdaemon.server.event.Thieving;
import org.rscdaemon.server.event.SingleEvent;
import org.rscdaemon.server.event.WalkToObjectEvent;
import org.rscdaemon.server.util.Formulae;
import org.rscdaemon.server.states.Action;
import org.rscdaemon.server.states.CombatState;
import org.rscdaemon.server.event.*;
import org.apache.mina.common.IoSession;

public class ObjectAction implements PacketHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

//incExp
	public void handlePacket(Packet p, IoSession session) {
		Player player = (Player)session.getAttachment();
		int pID = ((RSCPacket)p).getID();
		//getLoginConnector
		if(player.isBusy()) {
			player.resetPath();
			return;
		}
		player.resetAll();
		final GameObject object = world.getTile(p.readShort(), p.readShort()).getGameObject();
		final int click = pID == 51 ? 0 : 1;
		if(object == null) {
			player.setSuspiciousPlayer(true);
			return;
		}
		player.setStatus(Action.USING_OBJECT);
		world.getDelayedEventHandler().add(new WalkToObjectEvent(player, object, false) {
			private void replaceGameObject(int newID, boolean open) {
				world.registerGameObject(new GameObject(object.getLocation(), newID, object.getDirection(), object.getType()));
				owner.getActionSender().sendSound(open ? "opendoor" : "closedoor");
			}
			
			private void doGate() {
				owner.getActionSender().sendSound("opendoor");
				world.registerGameObject(new GameObject(object.getLocation(), 181, object.getDirection(), object.getType()));
				world.delayedSpawnObject(object.getLoc(), 1000);
			}
			
			private int[] coordModifier(Player player, boolean up) {
				if(object.getGameObjectDef().getHeight() <= 1) {
					return new int[]{player.getX(), Formulae.getNewY(player.getY(), up)};
				}
				int[] coords = {object.getX(), Formulae.getNewY(object.getY(), up)};
				switch(object.getDirection()) {
					case 0:
						coords[1] -= (up ? -object.getGameObjectDef().getHeight() : 1);
						break;
					case 2:
						coords[0] -= (up ? -object.getGameObjectDef().getHeight() : 1);
						break;
					case 4:
						coords[1] += (up ? -1 : object.getGameObjectDef().getHeight());
						break;
					case 6:
						coords[0] += (up ? -1 : object.getGameObjectDef().getHeight());
						break;
				}
				return coords;
			}
			
			public void arrived() {
				
				try {
				owner.resetPath();
				GameObjectDef def = object.getGameObjectDef();
				if(owner.isBusy() || owner.isRanging() || !owner.nextTo(object) || def == null || owner.getStatus() != Action.USING_OBJECT) {
					return;
				}
				owner.resetAll();
				String command = (click == 0 ? def.getCommand1() : def.getCommand2()).toLowerCase();
				
				Point telePoint = EntityHandler.getObjectTelePoint(object.getLocation(), command);
				if(telePoint != null) {
			    		owner.teleport(telePoint.getX(), telePoint.getY(), false);
				}
				//Nothing interesting
				else if(object.getID() == 198 && object.getX() == 251 && object.getY() == 468) { // Prayer Guild Ladder
					if(owner.getMaxStat(5) < 31) {
						owner.setBusy(true);
						Npc abbot = world.getNpc(174, 249, 252, 458, 468);
						if(abbot != null) {
							owner.informOfNpcMessage(new ChatMessage(abbot, "Hello only people with high prayer are allowed in here", owner));
						}
						world.getDelayedEventHandler().add(new ShortEvent(owner) {
	        					public void action() {
	        						owner.setBusy(false);
	        						owner.getActionSender().sendMessage("You need a prayer level of 31 to enter");
	        					}
        					});
					}
					else {
						owner.teleport(251, 1411, false);
					}
				}
				else if(object.getID() == 223 && object.getX() == 274 && object.getY() == 566) { // Mining Guild Ladder
  					if(owner.getCurStat(14) < 66) {
  						owner.setBusy(true);
  						Npc dwarf = world.getNpc(191, 272, 277, 563, 567);
  						if(dwarf != null) {
  							owner.informOfNpcMessage(new ChatMessage(dwarf, "Hello only the top miners are allowed in here", owner));
  						}
  						world.getDelayedEventHandler().add(new ShortEvent(owner) {
        					public void action() {
        						owner.setBusy(false);
        						owner.getActionSender().sendMessage("You need a mining level of 66 to enter");
        					}
        				});
  					}
  					else {
  						owner.teleport(274, 3397, false);
  					}
				}
				/**
				 * Agility @author Yong Min
				 *	Ledge in Yanille
				 */
				else if(command.equals("balance on")) {
					if((owner.getX() > 599) && (owner.getY() > 3556) && (owner.getX() < 603) && (owner.getY() < 3558)) {
						if((owner.getCurStat(16) < 1) || owner.getCurStat(16) < 15) {
							owner.getActionSender().sendMessage("Your agility level is not high enough to cross this ledge");
							return;
						}
						if(DataConversions.random(1, 2) < 2) {
							owner.getActionSender().sendMessage("You balance on the slippery ledge");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getActionSender().sendMessage("and walk across");
									owner.teleport(601, 3563, false);
									owner.incExp(16, 3, true, true);
									owner.getActionSender().sendStat(16);
								}
							});
						}
						else {
							int damage = DataConversions.random(1, 10);
							int newHP = owner.getCurStat(3) - damage;
							owner.setLastDamage(damage);
							owner.setCurStat(3, newHP);
							owner.getActionSender().sendStat(3);
							owner.getActionSender().sendMessage("You slip and fall!");
							owner.informOfModifiedHits(owner);

							if(newHP <= 0) {
								owner.killedBy(owner, true);
							}
						}
						return;
					}
					if((owner.getX() > 599) && (owner.getY() > 3561) && (owner.getX() < 603) && (owner.getY() < 3565)) {
						if((owner.getCurStat(16) < 1) || owner.getCurStat(16) < 15) {
							owner.getActionSender().sendMessage("Your agility level is not high enough to cross this ledge");
							return;
						}
						if(DataConversions.random(1, 2) < 2) {
							owner.getActionSender().sendMessage("You balance on the slippery ledge");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getActionSender().sendMessage("and walk across");
									owner.teleport(601, 3557, false);
									owner.incExp(16, 3, true, true);
									owner.getActionSender().sendStat(16);
								}
							});
						}
						else {
							int damage = DataConversions.random(1, 10);
							int newHP = owner.getCurStat(3) - damage;
							owner.setLastDamage(damage);
							owner.setCurStat(3, newHP);
							owner.getActionSender().sendStat(3);
							owner.getActionSender().sendMessage("You slip and fall!");
							owner.informOfModifiedHits(owner);

							if(newHP <= 0) {
								owner.killedBy(owner, true);
							}
						}
						return;
					}
					/**
					 * Gnome Stronghold
					 *  Agility Course
					 * @author Yong Min
					 */
					if((command.equals("balance on")) && (object.getID() == 655)) { // Log
						if(owner.getMaxStat(16) < 1) { // Shouldn't happen
							owner.getActionSender().sendMessage("You are not high enough level to use this agility course");
							return;
						}
						owner.setBusy(true);
						if(DataConversions.random(1, 20) < 10) {
							owner.getActionSender().sendMessage("You stand on the slippery log");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getActionSender().sendMessage("and walk across");
									if((owner.getY() != 495) || (owner.getX() != 692)) {
										owner.teleport(692, 497, false);
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
												owner.teleport(692, 499, false);
												owner.incExp(16, 1, true, true);
												owner.getActionSender().sendStat(16);
												owner.setBusy(false);
											}
										});
									}
								}
							});
						}
						else {
							owner.teleport(695, 497, false);
							int damage = DataConversions.random(1, 10);
							int newHP = owner.getCurStat(3) - damage;
							owner.setLastDamage(damage);
							owner.setCurStat(3, newHP);
							owner.getActionSender().sendStat(3);
							owner.getActionSender().sendMessage("You slip and fall!");
							owner.informOfModifiedHits(owner);
							owner.setBusy(false);

							if(newHP <= 0) {
								owner.killedBy(owner, true);
							}
						}
						return;
					}
				}
				else if((command.equals("climb")) && (object.getID() == 647)) { // First Net
					owner.setBusy(true);
					Npc trainer = world.getNpc(579);
					if(trainer != null) {
						owner.informOfNpcMessage(new ChatMessage(trainer, "Move it! Move it!", owner));
					}
					world.getDelayedEventHandler().add(new ShortEvent(owner) {
						public void action() {
							owner.getActionSender().sendMessage("You climb the net");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getActionSender().sendMessage("and pull yourself up onto the platform");
									owner.teleport(693, 1449, false);
									owner.incExp(16, 1, true, true);
									owner.setBusy(false);
								}
							});
						}
					});
					return;
				}
				else if((command.equals("grab hold of")) && (object.getID() == 650)) { // Rope Swing
					owner.setBusy(true);
					owner.getActionSender().sendMessage("You reach out and grab the rope swing");
					world.getDelayedEventHandler().add(new ShortEvent(owner) {
						public void action() {
							if(owner.getY() != 2395 || owner.getX() != 689) {
								owner.getActionSender().sendMessage("You hold on tight");
								world.getDelayedEventHandler().add(new ShortEvent(owner) {
									public void action() {
										owner.incExp(16, 5, true, true);
										owner.getActionSender().sendStat(16);
										owner.getActionSender().sendMessage("and swing to the opposite platform");
										owner.teleport(685, 2396, false);
										owner.setBusy(false);
									}
								});
							}
						}
					});
					return;
				}
				else if((command.equals("climb down")) && (object.getID() == 649)) { // Watch Tower
					owner.setBusy(true);
					owner.getActionSender().sendMessage("You hang down from the tower");
					world.getDelayedEventHandler().add(new ShortEvent(owner) {
						public void action() {
							owner.getActionSender().sendMessage("and drop to the floor");
							owner.teleport(683, 506, false);
							owner.informOfChatMessage(new ChatMessage(owner, "Ooof", owner));
							owner.incExp(16, 6, true, true);
							owner.getActionSender().sendStat(16);
							owner.setBusy(false);
							return;
						}
					});
				}
				else if((command.equals("climb")) && (object.getID() == 653)) { // Second Net
					owner.setBusy(true);
					Npc trainer = world.getNpc(579);
					if(trainer != null) {
						owner.informOfNpcMessage(new ChatMessage(trainer, "My granny can move faster than you!", owner));
					}
					world.getDelayedEventHandler().add(new ShortEvent(owner) {
						public void action() {
							owner.getActionSender().sendMessage("You take a few steps back");
							owner.teleport(684, 504, true);
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getActionSender().sendMessage("and run towards the net");
									owner.teleport(683, 500, false);
									owner.incExp(16, 6, true, true);
									owner.getActionSender().sendStat(16);
									owner.setBusy(false);
								}
							});
						}
					});
					return;
				}
				else if((command.equals("enter")) && (object.getID() == 654)) { // Pipe
					owner.setBusy(true);
					owner.getActionSender().sendMessage("You squeeze into the pipe");
					world.getDelayedEventHandler().add(new ShortEvent(owner) {
						public void action() {
							owner.incExp(16, 3, true, true);
							owner.getActionSender().sendStat(16);
							owner.getActionSender().sendMessage("and shuffle down into it");
							owner.teleport(683, 494, false);
							Npc trainer = world.getNpc(579);
							if(trainer != null) {
								owner.informOfNpcMessage(new ChatMessage(trainer, "That's the way! Well done!", owner));
							}
							owner.setBusy(false);
						}
					});
					return;
				}
				/**
				 * End of Gnome Stronghold
				 *	 Agility Course
				 */
				else if(command.equals("climb-up") || command.equals("climb up") || command.equals("go up")) {
					int[] coords = coordModifier(owner, true);
					owner.teleport(coords[0], coords[1], false);
				}
				else if(command.equals("climb-down") || command.equals("climb down") || command.equals("go down")) {
					int[] coords = coordModifier(owner, false);
					owner.teleport(coords[0], coords[1], false);
				} else if (command.equals("steal from")) {
					if (object == null) { return; }
					if (owner.getSpam()) {
						return;
					} else {						
						owner.setSpam(true);
					Thieving thiev = new Thieving(owner, object);
					thiev.thieveStall();
					}
					
				} else if (command.equals("search for traps")) {
					Thieving thieving = new Thieving(owner, object);
					thieving.thieveChest();
				}
				else if(object.getID() == 52 && object.getGrainable()) {
					owner.getActionSender().sendMessage("The grain slides down the hopper and grinds up into flour.");
					world.registerItem(new Item(23, 166, 599, 1, owner));
					object.setGrainable(false);
					
				}
				else if(object.getID() == 173 && object.getGrainable()) {
					owner.getActionSender().sendMessage("The grain slides down the hopper and grinds up into flour.");
					world.registerItem(new Item(23, 179, 481, 1, owner));
					object.setGrainable(false);
					
				}
				else if(command.equals("rest")) {
					owner.getActionSender().sendMessage("You rest on the bed");
					world.getDelayedEventHandler().add(new ShortEvent(owner) {
						public void action() {
							owner.setFatigue(0);
							owner.getActionSender().sendFatigue();
							owner.getActionSender().sendMessage("You wake up - feeling refreshed");
						}
					});
				}
				else if(owner.getWitchPotionStatus() == 2) {
						owner.setBusy(true);
						final Npc hetty = world.getNpc(148);
						world.getDelayedEventHandler().add(new ShortEvent(owner) {
	        					public void action() {
	        						owner.setBusy(false);
	        						owner.informOfNpcMessage(new ChatMessage(hetty, "Hello only people with high prayer are allowed in here", owner));
								owner.isWitchPotionComplete();
								owner.setQuestPoints(owner.getQuestPoints() +1);
								owner.getActionSender().sendQuestPoints();
								owner.getActionSender().sendWitchPotionComplete();
								owner.incExp(6, 350, false, false);
								owner.getActionSender().sendStat(6);
								owner.getActionSender().sendMessage("@gre@Congratulations! You have just completed the: @or1@Witch's Potion @gre@quest!");
								owner.getActionSender().sendMessage("@gre@You gained @or1@1 @gre@quest point!");
						}
					});
				}
				else if(command.equals("pick up")) {
					switch(object.getID()) {
						case 946: // Cannon Base
							owner.setBusy(true);
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.resetPath();
									Bubble base = new Bubble(owner, 1032);
									for(Player p : owner.getViewArea().getPlayersInView()) {
										p.informOfBubble(base);
									}
									final GameObject cannonBase = new GameObject(object.getLocation(), 946, 0, 0);
									world.unregisterGameObject(cannonBase);
									owner.getActionSender().sendMessage("You pick up the cannon base!");
									owner.getInventory().add(new InvItem(1032, 1));
									owner.getActionSender().sendInventory();
									owner.setBusy(false);
								}
							});
							break;
						case 947: // Cannon Stand
							owner.setBusy(true);
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.resetPath();
									Bubble stand = new Bubble(owner, 1033);
									for(Player p : owner.getViewArea().getPlayersInView()) {
										p.informOfBubble(stand);
									}
									world.unregisterGameObject(object);
									final GameObject cannonStand = new GameObject(object.getLocation(), 946, 0, 0);
									world.registerGameObject(cannonStand);
									owner.getActionSender().sendMessage("You pick up the cannon stand!");
									owner.getInventory().add(new InvItem(1033, 1));
									owner.getActionSender().sendInventory();
									owner.setBusy(false);
								}
							});
							break;
						case 948: // Cannon Barrel
							owner.setBusy(true);
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.resetPath();
									Bubble barrel = new Bubble(owner, 1034);
									for(Player p : owner.getViewArea().getPlayersInView()) {
										p.informOfBubble(barrel);
									}
									world.unregisterGameObject(object);
									final GameObject cannonBarrel = new GameObject(object.getLocation(), 947, 0, 0);
									world.registerGameObject(cannonBarrel);
									owner.getActionSender().sendMessage("You pick up the cannon barrels!");
									owner.getInventory().add(new InvItem(1034, 1));
									owner.getActionSender().sendInventory();
									owner.setBusy(false);
								}
							});
							break;
						case 943: // MultiCannon
							owner.setBusy(true);
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.resetPath();
									Bubble multicannon = new Bubble(owner, 1035);
									for(Player p : owner.getViewArea().getPlayersInView()) {
										p.informOfBubble(multicannon);
									}
									world.unregisterGameObject(object);
									owner.getActionSender().sendMessage("You pick up the multicannon!");
									owner.getInventory().add(new InvItem(1032, 1));
									owner.getInventory().add(new InvItem(1033, 1));
									owner.getInventory().add(new InvItem(1034, 1));
									owner.getInventory().add(new InvItem(1035, 1));
									owner.getActionSender().sendInventory();
									owner.setBusy(false);
								}
							});
							break;
						}
					}
				else if(command.equals("search")) {
					switch(object.getID()) {
						case 185: // Port Sarim Food Shop Crate
							if(owner.getPiratesTreasureStatus() < 2) {
								owner.getActionSender().sendMessage("Nothing interesting happens..");
								return;
							}
							if(owner.getPiratesTreasureStatus() == 2) {
								owner.getActionSender().sendMessage("Amongst the banana's, you manage to find some rum!");
								world.getDelayedEventHandler().add(new ShortEvent(owner) {
									public void action() {
										owner.getInventory().add(new InvItem(318, 1));
										owner.getActionSender().sendInventory();
										owner.setPiratesTreasureStatus(3);
									}
								});
							}
							break;
						case 40:
							if(owner.getRestlessGhostStatus() < 2 || owner.getRestlessGhostStatus() >= 4) {
								owner.getActionSender().sendMessage("Nothing interesting happens..");
								return;
							}
							if(owner.getRestlessGhostStatus() == 2 || owner.getRestlessGhostStatus() == 3) {
							owner.getActionSender().sendMessage("Out of nowhere appears a restless ghost!");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									final Npc ghost = new Npc(15, owner.getX(), owner.getY(), owner.getX() - 4, owner.getX() + 4, owner.getY() - 4, owner.getY() + 4);
									ghost.setRespawn(false);
									world.registerNpc(ghost);
									world.getDelayedEventHandler().add(new SingleEvent(null, 60000) {
										public void action() {
											Mob opponent = ghost.getOpponent();
											if(opponent != null) {
												opponent.resetCombat(CombatState.ERROR);
											}
											ghost.resetCombat(CombatState.ERROR);
											world.unregisterNpc(ghost);
											ghost.remove();
											}
										});
									}
								});
							}
							break;
						case 203:
							owner.getActionSender().sendMessage("You search the coffin and find some human remains");
								break;
						case 77: // Varrock Drain
							owner.getActionSender().sendMessage("I can see a key but can't quite reach it..");
								break;
						case 141: // Morgan's Cupboard
							owner.getActionSender().sendMessage("You search the cupboard and find some garlic.");
							owner.getInventory().add(new InvItem(218, 1));
							owner.getActionSender().sendInventory();
								break;
						case 86: // Fountain @draynor manor
							if(owner.getErnestTheChickenStatus() == 3) {
								owner.getActionSender().sendMessage("You pull the pressure gauge out of the fountain!");
								owner.getInventory().add(new InvItem(175, 1));
								owner.getActionSender().sendInventory();
								return;
							}
							owner.informOfChatMessage(new ChatMessage(owner, "There seems to be pressure gauge in here", owner));
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
				      						public void action() {
							owner.informOfChatMessage(new ChatMessage(owner, "There are a lot of pirhanas in there though", owner));
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
				      						public void action() {
							owner.informOfChatMessage(new ChatMessage(owner, "I can't get the gauge out", owner));
							}
						});
					}
				});
			}
				}	
				else if(command.equals("close") || command.equals("open")) {
					switch(object.getID()) {
						case 58:
							replaceGameObject(57, false);
							return;
						case 57:
							replaceGameObject(58, true);
							return;
						case 63:
							replaceGameObject(64, false);
							return;
						case 64:
							replaceGameObject(63, true);
							return;
						case 79:
							replaceGameObject(78, false);
							return;
						case 78:
							replaceGameObject(79, true);
							return;
						case 60:
							replaceGameObject(59, true);
							return;
						case 59:
							replaceGameObject(60, false);
							return;
						case 203:
							owner.getActionSender().sendMessage("You close the coffin");
							replaceGameObject(202, false);
							return;
						case 202:
							owner.getActionSender().sendMessage("You open the coffin");
							replaceGameObject(203, true);
							return;
						case 137: // Members Gate (Doriks)
							if(object.getX() != 341 || object.getY() != 487) {
								return;
							}
							doGate();
							if(owner.getX() <= 341) {
								owner.teleport(342, 487, false);
							}
							else {
								owner.teleport(341, 487, false);
							}
							break;
						case 138: // Members Gate (Crafting Guild)
							if(object.getX() != 343 || object.getY() != 581) {
								return;
							}
							doGate();
							if(owner.getY() <= 580) {
								owner.teleport(343, 581, false);
							}
							else {
								owner.teleport(343, 580, false);
							}
							break;
						case 180: // Al-Kharid Gate
							if(object.getX() != 92 || object.getY() != 649) {
								return;
							}
							doGate();
							if(owner.getX() <= 91) {
								owner.teleport(92, 649, false);
							}
							else {
								owner.teleport(91, 649, false);
							}
							break;
						case 254: // Karamja Gate
							if(object.getX() != 434 || object.getY() != 682) {
								return;
							}
							doGate();
							if(owner.getX() <= 434) {
								owner.teleport(435, 682, false);
							}
							else {
								owner.teleport(434, 682, false);
							}
							break;
						case 563: // King Lanthlas Gate
							if(object.getX() != 660 || object.getY() != 551) {
								return;
							}
							doGate();
							if(owner.getY() <= 551) {
								owner.teleport(660, 552, false);
							}
							else {
								owner.teleport(660, 551, false);
							}
							break;
						case 626: // Gnome Stronghold Gate
							if(object.getX() != 703 || object.getY() != 531) {
								return;
							}
							doGate();
							if(owner.getY() <= 531) {
								owner.teleport(703, 532, false);
							}
							else {
								owner.teleport(703, 531, false);
							}
							break;
						case 305: // Edgeville Members Gate
							if(object.getX() != 196 || object.getY() != 3266) {
								return;
							}
							doGate();
							if(owner.getY() <= 3265) {
								owner.teleport(196, 3266, false);
							}
							else {
								owner.teleport(196, 3265, false);
							}
							break;
						case 1089: // Dig Site Gate
							if(object.getX() != 59 || object.getY() != 573) {
								return;
							}
							doGate();
							if(owner.getX() <= 58) {
								owner.teleport(59, 573, false);
							}
							else {
								owner.teleport(58, 573, false);
							}
							break;
						case 356: // Woodcutting Guild Gate
							if(object.getX() != 560 || object.getY() != 472) {
								return;
							}
							if(owner.getY() <= 472) {
								doGate();
								owner.teleport(560, 473, false);
							}
							else {
								if(owner.getCurStat(8) < 70) {
									owner.setBusy(true);
									Npc mcgrubor = world.getNpc(255, 556, 564, 473, 476);
									
									if(mcgrubor != null) {
										owner.informOfNpcMessage(new ChatMessage(mcgrubor, "Hello only the top woodcutters are allowed in here", owner));
									}
									world.getDelayedEventHandler().add(new ShortEvent(owner) {
				      						public void action() {
				      							owner.setBusy(false);
				      							owner.getActionSender().sendMessage("You need a woodcutting level of 70 to enter");
				      						}
			      						});
								}
								else {
									doGate();
									owner.teleport(560, 472, false);
								}
							}
							break;
						case 1079: // Legends Guild Gate @author Yong Min
							if(object.getX() != 512 || object.getY() != 550) {
								break;
							}
							if(owner.getY() <= 550) {
							doGate();
								owner.teleport(512, 551, false);
							}
							else {
								if(owner.getSkillTotal() < 1500) {
									owner.getActionSender().sendMessage("You need a skill total of 1500 or more to enter!");
								}
								else {
									doGate();
									owner.teleport(512, 550, false);
								}
							}
							break;		
						case 142: // Black Knight Big Door
							owner.getActionSender().sendMessage("The doors are locked");
							break;
						case 93: // Red dragon gate
							if(object.getX() != 140 || object.getY() != 180) {
								return;
							}
							doGate();
							if(owner.getY() <= 180) {
								owner.teleport(140, 181, false);
							}
							else {
								owner.teleport(140, 180, false);
							}
							break;
						case 508: // Lesser demon gate
							if(object.getX() != 285 || object.getY() != 185) {
								return;
							}
							doGate();
							if(owner.getX() <= 284) {
								owner.teleport(285, 185, false);
							}
							else {
								owner.teleport(284, 185, false);
							}
							break;
						case 319: // Lava Maze Gate
							if(object.getX() != 243 || object.getY() != 178) {
								return;
							}
							doGate();
							if(owner.getY() <= 178) {
								owner.teleport(243, 179, false);
							}
							else {
								owner.teleport(243, 178, false);
							}
							break;
						case 712: // Shilo inside gate
							if(object.getX() != 394 || object.getY() != 851) {
								return;
							}
							owner.teleport(383, 851, false);
							break;
						case 611: // Shilo outside gate
							if(object.getX() != 388 || object.getY() != 851) {
								return;
							}
							owner.teleport(394, 851, false);
							break;
						default:
							owner.getActionSender().sendMessage("Nothing interesting happens.");
							return;
					}
				}
				else if(command.equals("pick") || command.equals("pick banana")) {
					switch(object.getID()) {
						case 72: // Wheat
							owner.getActionSender().sendMessage("You get some grain");
							owner.getInventory().add(new InvItem(29, 1));
							break;
						case 191: // Potatos
							owner.getActionSender().sendMessage("You pick a potato");
							owner.getInventory().add(new InvItem(348, 1));
							break;
						case 313: // Flax
							owner.getActionSender().sendMessage("You uproot a flax plant");
							owner.getInventory().add(new InvItem(675, 1));
							break;
						case 183: // Banana
							owner.getActionSender().sendMessage("You pull a banana off the tree");
							owner.getInventory().add(new InvItem(249, 1));
							break;
						default:
							owner.getActionSender().sendMessage("Nothing interesting happens.");
							return;
					}
					owner.getActionSender().sendInventory();
					owner.getActionSender().sendSound("potato");
					owner.setBusy(true);
					world.getDelayedEventHandler().add(new SingleEvent(owner, 200) {
						public void action() {
							owner.setBusy(false);
						}
					});
				}
				else if(command.equals("mine") || command.equals("prospect")) {
					handleMining(click);
				}
				else if(command.equals("lure") || command.equals("bait") || command.equals("net") || command.equals("harpoon") || command.equals("cage")) {
					 handleFishing(click);
				}
				else if(command.equals("chop")) {
					handleWoodcutting(click);
				}
				else if(command.equals("hit")) {
				//case 49:  Dummy
				if(owner.getCurStat(0) < 7) {
				owner.getActionSender().sendMessage("You swing at the dummy!");
				world.getDelayedEventHandler().add(new ShortEvent(owner) {
				public void action() {
				owner.incExp(0, 3, true, true);
					}
				});
				}
				else {
					owner.getActionSender().sendMessage("You swing at the dummy, but it has no effect because you are too powerful.");
					}	
				
				}	
				else if(command.equals("recharge at")) {
      					owner.getActionSender().sendMessage("You recharge at the altar.");
      					owner.getActionSender().sendSound("recharge");
      					int maxPray = object.getID() == 200 ? owner.getMaxStat(5) + 2 : owner.getMaxStat(5);
      					if(owner.getCurStat(5) < maxPray) {
      						owner.setCurStat(5, maxPray);
      					}
      					owner.getActionSender().sendStat(5);
				}
				else if(command.equals("board")) {
					owner.getActionSender().sendMessage("You must talk to the owner about this.");
				}
				else {
					switch(object.getID()) {
						case 613: // Shilo cart
							if(object.getX() != 384 || object.getY() != 851) {
								return;
							}
							owner.setBusy(true);
							owner.getActionSender().sendMessage("You search for a way over the cart");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getActionSender().sendMessage("You climb across");
									if(owner.getX() <= 383) {
										owner.teleport(386, 851, false);
									}
									else {
										owner.teleport(383, 851, false);
									}
									owner.setBusy(false);
								}
							});
							break;
						case 643: // Gnome tree stone
							if(object.getX() != 416 || object.getY() != 161) {
								return;
							}
							owner.setBusy(true);
							owner.getActionSender().sendMessage("You twist the stone tile to one side");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getActionSender().sendMessage("It reveals a ladder, you climb down");
									owner.teleport(703, 3284, false);
									owner.setBusy(false);
								}
							});
							break;
						case 638: // First roots in gnome cave
							if(object.getX() != 701 || object.getY() != 3280) {
								return;
							}
							//door
							owner.setBusy(true);
							owner.getActionSender().sendMessage("You push the roots");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getActionSender().sendMessage("They wrap around you and drag you forwards");
									owner.teleport(701, 3278, false);
									owner.setBusy(false);
								}
							});
						case 639: // Second roots in gnome cave
							if(object.getX() != 701 || object.getY() != 3279) {
								return;
							}
							owner.setBusy(true);
							owner.getActionSender().sendMessage("You push the roots");
							world.getDelayedEventHandler().add(new ShortEvent(owner) {
								public void action() {
									owner.getActionSender().sendMessage("They wrap around you and drag you forwards");
									owner.teleport(701, 3281, false);
									owner.setBusy(false);
								}
							});
							break;
						default:
					}
				} } catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
			
			private void handleMining(int click) {
				final ObjectMiningDef def = EntityHandler.getObjectMiningDef(object.getID());
				if(click == 0) {
					if(def != null) {
						InvItem ore = new InvItem(def.getOreId());
						if(owner.getCurStat(14) < def.getReqLevel()) {
							owner.getActionSender().sendMessage("You need a mining level of " + def.getReqLevel() + " to mine this rock.");
						} else {
							int pickAxe = Formulae.getPickAxe(owner);
							if(pickAxe == -1) {
								owner.getActionSender().sendMessage("You need a pickaxe to mine this rock!");
							} else if(pickAxe == -2) {
								owner.getActionSender().sendMessage("You do not have a pickaxe which you have the level to use!");
							} else {
								owner.setBusy(true);
								owner.getActionSender().sendSound("mine");
								owner.getActionSender().sendMessage("You swing your pick at the rock...");
								Bubble bubble = new Bubble(owner, pickAxe);
								for(Player p : owner.getViewArea().getPlayersInView()) {
									p.informOfBubble(bubble);
								}
								world.getDelayedEventHandler().add(new MiningEvent(owner, def, object, pickAxe, Formulae.getPickaxeSwings(pickAxe)));
							}
						}
					} else {
						owner.getActionSender().sendMessage("There is currently no ore available in this rock.");
					}
				} else if(click == 1) {
					if((owner.getLocation().onTutorialIsland()) && (owner.getTutorialStatus() < 48)) {
						final InvItem ore = new InvItem(def.getOreId());
						owner.getActionSender().sendMessage("You prospect the rock for ores...");
						world.getDelayedEventHandler().add(new ShortEvent(owner) {
							public void action() {
								owner.getActionSender().sendMessage("This rock contains " + ore.getDef().getName() + ".");
								world.getDelayedEventHandler().add(new ShortEvent(owner) {
									public void action() {
										owner.getActionSender().sendMessage("Sometimes you won't find the ore but trying again may find it");
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
											public void action() {
												owner.getActionSender().sendMessage("If a rock contains a high level ore");
												world.getDelayedEventHandler().add(new ShortEvent(owner) {
													public void action() {
														owner.getActionSender().sendMessage("You will not find it until you increase your mining level");
														world.getDelayedEventHandler().add(new ShortEvent(owner) {
															public void action() {
																owner.setTutorialStatus(48);
																owner.getActionSender().sendTutorialStatus();
															}
														});
													}
												});
											}
										});
									}
								});
							}
						});
					} else {
						owner.getActionSender().sendMessage("You prospect the rock for ores...");
						world.getDelayedEventHandler().add(new ShortEvent(owner, def) {
							public void action() {
								if(def != null) {
									InvItem ore = new InvItem(def.getOreId());
									owner.getActionSender().sendMessage("This rock contains " + ore.getDef().getName() + ".");
								} else {
									owner.getActionSender().sendMessage("There is currently no ore available in this rock.");
								}
							}
						});
					}
				}
			}
			
			private void handleFishing(final int click) {
				final ObjectFishingDef def = EntityHandler.getObjectFishingDef(object.getID(), click);
				if(def == null) { // This shouldn't happen
					return;
				}
				if((owner.getLocation().onTutorialIsland()) && (owner.getInventory().countId(349) >= 3)) {
					owner.getActionSender().sendMessage("That's enough fishing! Speak with the fishing instructor!");
					return;
				}
				if(owner.getCurStat(10) < def.getReqLevel()) {
					owner.getActionSender().sendMessage("You need a fishing level of " + def.getReqLevel() + " to fish here.");
					return;
				}
				int netId = def.getNetId();
				if(owner.getInventory().countId(netId) <= 0) {
					owner.getActionSender().sendMessage("You need a " + EntityHandler.getItemDef(netId).getName() + " to catch these fish.");
					return;
				}
				final int baitId = def.getBaitId();
				if(baitId >= 0) {
					if(owner.getInventory().countId(baitId) <= 0) {
						owner.getActionSender().sendMessage("You don't have any " + EntityHandler.getItemDef(baitId).getName() + " left.");
						return;
					}
				}
				
				owner.setBusy(true);
				owner.getActionSender().sendSound("fish");
		    		Bubble bubble = new Bubble(owner, netId);
				for(Player p : owner.getViewArea().getPlayersInView()) {
					p.informOfBubble(bubble);
				}
				owner.getActionSender().sendMessage("You attempt to catch some fish");
				world.getDelayedEventHandler().add(new ShortEvent(owner) {
					public void action() {
						ObjectFishDef def = Formulae.getFish(object.getID(), owner.getCurStat(10), click);
						if(def != null) {
							if(baitId >= 0) {
								int idx = owner.getInventory().getLastIndexById(baitId);
								InvItem bait = owner.getInventory().get(idx);
								int newCount = bait.getAmount() - 1;
								if(newCount <= 0) {
									owner.getInventory().remove(idx);
								}
								else {
									bait.setAmount(newCount);
								}
							}
							InvItem fish = new InvItem(def.getId());
							owner.getInventory().add(fish);
							owner.getActionSender().sendMessage("You catch a " + fish.getDef().getName() + ".");
							owner.getActionSender().sendInventory();
							if((owner.getLocation().onTutorialIsland()) && (owner.getInventory().countId(349) >= 3)) {
								owner.setTutorialStatus(42);
								owner.getActionSender().sendTutorialStatus();
							} else {
								owner.incExp(10, def.getExp(), true, true);
								owner.getActionSender().sendStat(10);
							}
						}
						else {
							owner.getActionSender().sendMessage("You fail to catch anything.");
						}
						owner.setBusy(false);
					}
				});
			}
			
			private void handleWoodcutting(final int click) {
				final ObjectWoodcuttingDef def = EntityHandler.getObjectWoodcuttingDef(object.getID());
				if(def == null) { // This shoudln't happen
					return;
				}
				if(owner.getCurStat(8) < def.getReqLevel()) {
					owner.getActionSender().sendMessage("You need a woodcutting level of " + def.getReqLevel() + " to axe this tree.");
					return;
				}
				int axeId = -1;
				for(int a : Formulae.woodcuttingAxeIDs) {
					if(owner.getInventory().countId(a) > 0) {
						axeId = a;
						break;
					}
				}
				if(axeId < 0) {
					owner.getActionSender().sendMessage("You need an axe to chop this tree down.");
					return;
				}
				owner.setBusy(true);
		    		Bubble bubble = new Bubble(owner, axeId);
				for(Player p : owner.getViewArea().getPlayersInView()) {
					p.informOfBubble(bubble);
				}
				owner.getActionSender().sendMessage("You swing your " + EntityHandler.getItemDef(axeId).getName() + " at the tree...");
				final int axeID = axeId;
				world.getDelayedEventHandler().add(new ShortEvent(owner) {
					public void action() {
						if(Formulae.getLog(def, owner.getCurStat(8), axeID)) {
							InvItem log = new InvItem(def.getLogId());
							owner.getInventory().add(log);
							owner.getActionSender().sendMessage("You get some wood.");
							owner.getActionSender().sendInventory();
							owner.incExp(8, def.getExp(), true, true);
							owner.getActionSender().sendStat(8);
							if(DataConversions.random(1, 100) <= def.getFell()) {
								world.registerGameObject(new GameObject(object.getLocation(), 4, object.getDirection(), object.getType()));
								world.delayedSpawnObject(object.getLoc(), def.getRespawnTime() * 1000);
							}
						}
						else {
							owner.getActionSender().sendMessage("You slip and fail to hit the tree.");
						}
						owner.setBusy(false);
					}
				});
			}
		});
	}
	
}