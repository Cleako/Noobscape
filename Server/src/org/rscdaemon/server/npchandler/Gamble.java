package org.rscdaemon.server.npchandler;
import java.util.Random;
import java.util.Scanner;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.ChatMessage;
import org.rscdaemon.server.model.MenuHandler;
import org.rscdaemon.server.event.ShortEvent;

public class Gamble implements NpcHandler {
	/**
	 * World instance
	 */
	public static final World world = World.getWorld();

	public void handleNpc(final Npc npc,final Player player) throws Exception {
      		player.informOfNpcMessage(new ChatMessage(npc, "Would you like to gamble?", player));
      		player.informOfNpcMessage(new ChatMessage(npc, "The more you gamble, the better the prize!", player));
      		player.setBusy(true);
      		world.getDelayedEventHandler().add(new ShortEvent(player) {
      			public void action() {
      				owner.setBusy(false);
				String[] options = new String[]{"I will Gamble 100k.", "I will Gamble 500k.", "I will Gamble 1m", "No, thank you."};
				owner.setMenuHandler(new MenuHandler(options) {
					public void handleReply(final int option, final String reply) {
						if(owner.isBusy()) {
							return;
						}
						owner.informOfChatMessage(new ChatMessage(owner, reply, npc));
						owner.setBusy(true);
						world.getDelayedEventHandler().add(new ShortEvent(owner) {
							public void action() {
								owner.setBusy(false);
								if(option == 0 ) {
									if(player.getInventory().countId(10) > 99999)
									{
										owner.informOfNpcMessage(new ChatMessage(npc, "Thank you, and good luck!", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
      					                public void action() {
										owner.setBusy(false);
										Random generator = new Random();
										Scanner in = new Scanner(System.in);
										int number = generator.nextInt(22) + 1;
										if(number == 0)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(400, 1));//Rune Chain
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Rune Chain");
										}
										if(number == 1)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 2)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 3)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(75, 1));//Rune Longsword
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Rune Long Sword!");
										}
										if(number == 4)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 5)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(112, 1));//Rune Large Helmet
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Rune Large Helmet!");
										}
										if(number == 6)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 7)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 8)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(400, 1));//Rune Chain
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Rune Chain!");
										}
										if(number == 9)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 10)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 11)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(525, 1));//Crystal Key
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Crystal Key!");
										}
										if(number == 12)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 13)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(402, 1));//Rune Legs
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@Rune Legs!");
										}
										if(number == 14)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 15)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(518, 20));//Coal Certs
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You win @whi@20 Coal Certificates!");
										}
										if(number == 16)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 17)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 18)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(81, 1));//Rune 2-Handed Sword
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Rune 2-Handed Sword!");
										}
										if(number == 19)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 20)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(10, 50000));//Coins
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@half of your coins back!");
										}
      					                if(number == 21)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(401, 1));//Rune Plate
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Rune Plate");
										}
										if(number == 22)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 23)
										{
											player.getInventory().remove(10, 100000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
									
											}
										
										});
								    }else{
								    	owner.informOfNpcMessage(new ChatMessage(npc, "You dont have enought money, come back when your not out of cash!", owner));
								    }
								}
								if(option == 1) {
										if(player.getInventory().countId(10) > 499999)
									{
										owner.informOfNpcMessage(new ChatMessage(npc, "Thank you Sir, Good Luck to you!", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
      					                public void action() {
										owner.setBusy(false);
										Random generator = new Random();
										Scanner in = new Scanner(System.in);
										int number = generator.nextInt(22) + 1;
										if(number == 0)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1378, 30));// Range Exp Certs
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@30 Range Experience Certificates!");
										}
										if(number == 1)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 2)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1363, 1));//Rune Rune Plate(m)
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Full Rune Plate(m) package!");
										}
										if(number == 3)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 4)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 5)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(619, 250));//Bloods
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@250 Blood Runes!");
										}
										if(number == 6)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 7)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(10, 250000));//  Cash
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@Half of your cash back!");
										}
										if(number == 8)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 9)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 10)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(597, 1));//Dragonstone Amulet
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Dragonstone Amulet!");
										}
										if(number == 11)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(518, 80));//Coal Certs
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You win @whi@80 Coal Certificates!");
										}
										if(number == 12)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 13)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(10, 150000));//Coins
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@150,000 Coins!");
										}
										if(number == 14)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 15)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1373, 40));// Attack Exp Certs
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@40 Attack Experience Certificates!");
										}
										if(number == 16)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 17)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(525, 1));// Crystal Key
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Crystal Key!");
										}
										if(number == 18)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 19)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(593, 1));//Dragon Long
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Dragon Long Sword!");
										}
										if(number == 20)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 21)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 22)
										{
											player.getInventory().remove(10, 500000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
      					                
									
											}
										
										});
								    }else{
								    	owner.informOfNpcMessage(new ChatMessage(npc, "You dont have enought money, come back when your not out of cash.", owner));
								    }
								}
								if(option == 2) {
										if(player.getInventory().countId(10) > 999999)
									{
										owner.informOfNpcMessage(new ChatMessage(npc, "Thank you Sir, Good Luck to you!", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
      					                public void action() {
										owner.setBusy(false);
										Random generator = new Random();
										Scanner in = new Scanner(System.in);
										int number = generator.nextInt(40) + 1;
										if(number == 0)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 1)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(593, 1));//Dragon Sword
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Dragon Sword!");
										}
										if(number == 2)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 3)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(10, 2000000));//Coins
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@2,000,000 Coins!");
										}
										if(number == 4)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 5)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(594, 1));//Dragon Axe
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won a @whi@Dragon Axe!");
										}
										if(number == 6)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 7)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(10, 500000));//Coins
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@500,000 Coins!");
										}
										if(number == 8)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 9)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(619, 500));//Bloods
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@500 Bloods!");
										}
										if(number == 10)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 11)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1374, 100));// Defense Exp Certs
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@100 Defense Experience Certificates!");
										}
										if(number == 12)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 13)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(518, 200));//Coal Certs
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You win @whi@200 Coal Certificates!");
										}
										if(number == 14)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 15)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1270, 25));//Dragon Bone Certs
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@25 Dragon Bone Certificates!");
										}
										if(number == 16)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 17)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1310, 100));//Magic Logs
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@100 Magic Log Certificates!");
										}
										if(number == 18)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 19)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(31, 5000));//Fire Rune
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@5,000 Fire Runes!");
										}
										if(number == 20)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 21)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(33, 5000));//Air Rune
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@5,000 Air Runes!");
										}
										if(number == 22)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 23)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(10, 1000000));//Cash
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@1,000,000 Coins! You lost nothing!");
										}
										if(number == 24)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 25)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1366, 1));//  Party Hat
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won the @whi@Jackpot Prize!");
										}
										if(number == 26)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 27)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 28)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(10, 750000));//750,000 Cions
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won @whi@750,000 Coins!");
										}
										if(number == 29)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 30)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 31)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 32)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 33)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 34)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 35)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!!");
										}
										if(number == 36)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!!");
										}
										if(number == 37)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 38)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 39)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
										if(number == 40)
										{
											player.getInventory().remove(10, 1000000);
											player.getInventory().add(new InvItem(1155, 1));//Old Boot!
											player.getActionSender().sendInventory();
											player.getActionSender().sendMessage("@gre@You won an @whi@Old Boot!");
										}
      					                
										
											}
										
										});
								    }else{
								    	owner.informOfNpcMessage(new ChatMessage(npc, "You dont have enough money, come back when your have some!", owner));
								    }
								}
								if(option == 3) {
										owner.informOfNpcMessage(new ChatMessage(npc, "Please do come back when you have some more cash!", owner));
										world.getDelayedEventHandler().add(new ShortEvent(owner) {
										public void action() {
										
											}
										});
								}
								else {
									npc.unblock();
								}
							}
						});
					}				
				});
				owner.getActionSender().sendMenu(options);
					
      			}
      		});
      		npc.blockedBy(player);
	}
	
}