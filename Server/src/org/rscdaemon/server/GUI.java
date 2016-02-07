package org.rscdaemon.server;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
/*
 * Created by JFormDesigner on Thu Jan 22 18:57:24 CST 2009
 */

import org.rscdaemon.server.event.SaveEvent;
import org.rscdaemon.server.model.InvItem;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.World;
import org.rscdaemon.server.util.Config;
import org.rscdaemon.server.util.DataConversions;
import org.rscdaemon.server.util.Formulae;
import org.rscdaemon.server.util.Logger;


/**
 * @author xEnt
 */
public class GUI extends JPanel {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static World world = World.getWorld();

	public static void setSettings() {
		GameVars.serverName = GUI.textField6.getText();
		GameVars.serverLocation = GUI.textField4.getText();
		GameVars.clientVersion = Integer.valueOf(GUI.textField9.getText());
		GameVars.maxUsers = Integer.valueOf(GUI.textField7.getText());
		GameVars.portNumber = Integer.valueOf(GUI.textField11.getText());
		GameVars.useFatigue = checkBox1.isSelected();
		if(GameVars.expMultiplier != Double.valueOf(GUI.textField5.getText())) {
			GameVars.expMultiplier = Double.valueOf(GUI.textField5.getText());
			if(GameVars.serverRunning) {
				Logger.print("Server XP Rate has been changed to " + GameVars.expMultiplier + "x", 3);
				world.sendToAll("@yel@ Server @whi@XP Rate @yel@has been changed to @whi@" + GameVars.expMultiplier + "x");
			}
		}
		if(GameVars.rangedDelaySpeed != Integer.valueOf(GUI.textField8.getText())) {
			GameVars.rangedDelaySpeed = Integer.valueOf(GUI.textField8.getText());
			if(GameVars.serverRunning) {
				world.sendToAll("@yel@ Server @whi@Ranged Delay Speed @yel@has been changed to @whi@" + GameVars.rangedDelaySpeed);
				Logger.print("Server Ranged Delay Speed has been changed to " + GameVars.rangedDelaySpeed, 3);
			}
		}
		GameVars.saveAll = Integer.valueOf(GUI.textField12.getText());
	}

	public static void repaintSettings() {
		GUI.textField6.setText(GameVars.serverName);
		GUI.textField4.setText(GameVars.serverLocation);
		GUI.textField9.setText(GameVars.clientVersion + "");
		GUI.textField7.setText(GameVars.maxUsers + "");
		GUI.textField11.setText(GameVars.portNumber + "");
		GUI.textField5.setText(GameVars.expMultiplier + "");
		GUI.textField8.setText(GameVars.rangedDelaySpeed + "");
		GUI.textField12.setText(GameVars.saveAll + "");
		GUI.checkBox1.setSelected(GameVars.useFatigue);
	}
	public static void resetVars() {
		GameVars.modsOnline = 0;
		GameVars.adminsOnline = 0;
		GameVars.userPeak = 0;
		GameVars.usersOnline = 0;
		GameVars.serverRunning = false;
		GUI.label3.setForeground(Color.RED);
		GUI.label3.setText("Offline");
		GUI.label20.setText("(Peak " + GameVars.userPeak + ")");
		GUI.label10.setText("(" + GameVars.modsOnline + " Mods, " + GameVars.adminsOnline + " Admins)");
		GUI.label4.setText(GameVars.usersOnline + "/" + GameVars.maxUsers);
		GUI.label8.setText(GameVars.serverName);
		GUI.button2.setEnabled(false);
		GUI.button3.setEnabled(false);

	}

	public static void repaintVars() {
		if(world != null)
			GameVars.usersOnline = world.getPlayers().count();

		if(GameVars.usersOnline > GameVars.userPeak)
			GameVars.userPeak = GameVars.usersOnline;


		GUI.progressBar1.setMaximum(GameVars.maxUsers);
		GUI.progressBar1.setMinimum(0);
		GUI.progressBar1.setValue(GameVars.usersOnline);	
		GUI.label3.setText((GameVars.serverRunning ? "Online" : "Offline"));
		GUI.label3.setForeground((GameVars.serverRunning ? Color.GREEN : Color.RED));
		GUI.label4.setText(GameVars.usersOnline + "/" + GameVars.maxUsers);
		GUI.label20.setText("(Peak " + GameVars.userPeak + ")");
		GUI.label10.setText("(" + GameVars.modsOnline + " Mods, " + GameVars.adminsOnline + " Admins)");
		GUI.button1.setEnabled(GameVars.serverRunning ? false : true);
		GUI.button2.setEnabled(GameVars.serverRunning ? true : false);
		GUI.button4.setEnabled(GameVars.serverRunning ? true : false);
		GUI.button3.setEnabled(GameVars.serverRunning ? true : false);
		GUI.label8.setName(GameVars.serverName);
		GUI.textField1.setEnabled(GameVars.serverRunning ? true : false);

	}

	public static void writeSettings() {
		try {
			File f = new File("EasyRSC.cfg");
			Properties pr = new Properties();

			FileInputStream fis = new FileInputStream(f);
			pr.load(fis);
			fis.close();

			pr.setProperty("ServerName", GameVars.serverName);
			pr.setProperty("ServerLocation", GameVars.serverLocation);
			pr.setProperty("ClientVersion", GameVars.clientVersion + "");
			pr.setProperty("MaxPlayers", GameVars.maxUsers + "");
			pr.setProperty("PortNumber", GameVars.portNumber + "");
			pr.setProperty("UseFatigue", (GameVars.useFatigue ? 1 : 0) + "");
			pr.setProperty("ExpMultiplier", GameVars.expMultiplier + "");
			pr.setProperty("ArrowDelaySpeed", GameVars.rangedDelaySpeed + "");
			pr.setProperty("SaveAll", GameVars.saveAll + "");


			FileOutputStream fos = new FileOutputStream(f);
			pr.store(fos, "Server Settings");
			fos.close();
		} catch (Exception e) {
			Logger.print(e, 1);
		}
	}

	public GUI() {
		initComponents();
		String configFile = "EasyRSC.cfg";
		try {
			Config.initConfig(configFile);
		} catch (Exception e) {
			Logger.print(e, 1);
		}
		resetVars();
	}
	static String[] args = null;

	private void button1ActionPerformed(ActionEvent e) {

		try {

			Logger.print("EasyRSC Starting Up.", 3);
			GameVars.serverRunning = true;
			new Server();
		} catch (Exception r) {
			Logger.print(r.toString(), 1);
		}
	}

	private void button2ActionPerformed(ActionEvent e) {
		SaveEvent.saveAll();
		for(Player p : world.getPlayers()) {
			p.getActionSender().sendLogout();
		}
		GameEngine.kill();

	}

	private void list1ValueChanged(ListSelectionEvent e) {
		if(!list1.isSelectionEmpty()) {
			String name = list1.getSelectedValue().toString();
			lastClickedName = name;
			refreshWorldList(name);
		}

	}

	public static void refreshWorldList(String name) {

		if(name.contains("(Online)")) {
			name = name.replace(" (Online)", "").trim();
		}
		String rank = "";

		Player p = world.getPlayer(DataConversions.usernameToHash(name));
		if(p != null) { // Online.
			button7.setEnabled(true);
			button14.setEnabled(true);
			if(p.rank == 0) {
				rank = "(0) - Normal Player";
				label39.setForeground(Color.BLUE);
			} else if(p.rank == 2) {
				rank = "(2) - Player Moderator";
				label39.setForeground(Color.GREEN);
			} else if(p.rank == 3) {
				rank = "(3) - Moderator";
				label39.setForeground(Color.ORANGE);
			} else if(p.rank == 4) {
				rank = "(4) - Administrator";
				label39.setForeground(Color.RED);
			} else if(p.rank == 5) {
				rank = "(5) - MUTED";
				label39.setForeground(Color.MAGENTA);
			} else if(p.rank == 6) {
				rank = "(6) - BANNED";
				label39.setForeground(Color.MAGENTA);
			}
			button9.setText("Ban");
			
			if(p.isMuted())
				button11.setText("UnMute");
			else
				button11.setText("Mute");
			label39.setText(rank);
			button12.setEnabled(true);
			label27.setForeground(Color.GREEN);
			label27.setText("Online");
			label38.setText(p.getUsername().replaceAll(" ", "_"));
			label40.setText(p.getCurrentIP());
			label41.setText(p.getCombatLevel() + "");
			label42.setText(p.getSkillTotal() + "");
			label43.setText(p.getLocation().getX() + ", " + p.getLocation().getY());

		} else { // Offline.
			
	
			button12.setEnabled(false);
			button14.setEnabled(false);
			label27.setText("Offline");
			label27.setForeground(Color.RED);
			label38.setText(name);
			int rnk = Integer.valueOf(readValue(name, "rank"));
			boolean muted = false;
			boolean banned = false;
			if(rnk == 0) {
				rank = "(0) - Normal Player";
				label39.setForeground(Color.BLUE);
			} else if(rnk == 2) {
				rank = "(2) - Player Moderator";
				label39.setForeground(Color.GREEN);
			} else if(rnk == 3) {
				rank = "(3) - Moderator";
				label39.setForeground(Color.ORANGE);
			} else if(rnk == 4) {
				rank = "(4) - Administrator";
				label39.setForeground(Color.RED);
			} else if(rnk == 5) {
				rank = "(5) - MUTED";
				muted = true;
				button11.setText("UnMute");
				label39.setForeground(Color.MAGENTA);
			} else if(rnk == 6) {
				button9.setText("UnBan");
				rank = "(6) - BANNED";
				banned = true;
				label39.setForeground(Color.MAGENTA);
			}
			if(muted) {
				button11.setText("UnMute");
			} else  {
				button11.setText("Mute");
			}
			
			if(banned) {
				button9.setText("UnBan");
			} else {
				button9.setText("Ban");
			}
			
			
			label39.setText(rank);
			label40.setText(readValue(name, "ip"));
			button7.setEnabled(false);
			int[] stat = new int[7];
			for(int i=0; i < 7; i++) {
				stat[i] = Formulae.experienceToLevel(Integer.valueOf(readValue(name, "e" + (i + 1))));
			}
			label41.setText(Formulae.getCombatlevel(stat) + "");
			int skillTotal = 0;
			for(int i=0; i < 18; i++) {
				skillTotal+=Formulae.experienceToLevel(Integer.valueOf(readValue(name, "e" + (i + 1))));
			}
			label42.setText(skillTotal + "");
			label43.setText("--N/A--");

		}
	}

	private void button3ActionPerformed(ActionEvent e) {
		SaveEvent.saveAll();
	}

	private void button4ActionPerformed(ActionEvent e) {
		if(textField1.getText() != "") {
			for(Player p : world.getPlayers()) {
				p.getActionSender().sendMessage("@red@Announcement: @whi@" + textField1.getText());
			}
			textField1.setText("");
		}
	}
	public static String lastClickedName = null;
	public static void populateWorldList() {
		Object selected = null;
		if(list1.getSelectedIndex() != -1) {
			selected = list1.getSelectedValue();
		}

		model.clear();
		list1.setSelectedValue(null, false);
		ArrayList<String> online = new ArrayList<String>();
		online.clear();
		ArrayList<String> offline = new ArrayList<String>();
		offline.clear();
		File[] fa = new File("players/").listFiles();
		for(File f : fa) {
			if(f.getName().endsWith(".cfg")) {
				Player p = world.getPlayer(DataConversions.usernameToHash(f.getName().replace(".cfg", "")));
				if(p != null) {
					online.add(p.getUsername());
				} else {
					offline.add(f.getName().replace(".cfg", ""));
				}
			}
		}
		int count = online.size();
		for(int i=0; i < count; i++) {
			model.add(i, online.get(i) + " (Online)");
		}
		for(int i=0; i < offline.size(); i++) {
			model.add(count + i, offline.get(i));
		}
		if(selected != null) {
			list1.setSelectedValue(selected, true);
		}
	}

	public static boolean isOnline(String player) {
		Player p = world.getPlayer(DataConversions.usernameToHash(player));
		return p != null;
	}

	private void button5ActionPerformed(ActionEvent e) {
		setSettings();
		writeSettings();
		
	}

	private void button6ActionPerformed(ActionEvent e) {
		populateWorldList();
	}

	private void button9ActionPerformed(ActionEvent e) {
		Player p = world.getPlayer(DataConversions.usernameToHash(label38.getText()));
		if(p != null) {
			world.banPlayer(label38.getText());
		} else {
			if(Integer.valueOf(readValue(label38.getText(), "rank")) == 6) {
				world.unbanPlayer(label38.getText());
			} else {
				world.banPlayer(label38.getText());
			}		
		}
		
		
	}

	private void button10ActionPerformed(ActionEvent e) {
		world.deletePlayer(label38.getText());
	}
//Label44
	private void button11ActionPerformed(ActionEvent e) {
		if(isOnline(label38.getText())) {
			Player p = world.getPlayer(DataConversions.usernameToHash(label38.getText()));
			if(p.rank == 5) {
				world.unMutePlayer(p.getUsername().toLowerCase());
			} else {
				world.mutePlayer(p.getUsername().toLowerCase());
			}
		} else {
			if(Integer.valueOf(readValue(label38.getText(), "rank")) == 5) {
				world.unMutePlayer(label38.getText());
			} else {
				world.mutePlayer(label38.getText());
			}
		}
		
	
	}

	private void button12ActionPerformed(ActionEvent e) {
		world.kickPlayer(label38.getText());
	}

	private void radioButton1ActionPerformed(ActionEvent e) {
		if(!radioButton1.isSelected())
			radioButton1.setSelected(true);

		radioButton2.setSelected(false);
	}

	private void radioButton2ActionPerformed(ActionEvent e) {
		if(!radioButton2.isSelected())
			radioButton2.setSelected(true);

		radioButton1.setSelected(false);
	}

	public static String readValue(String user, String key) {
		try {
		//System.out.println("Test 4");
			String username = user.replaceAll(" ", "_");
			File f = new File("players/" + username.toLowerCase() + ".cfg");
			Properties pr = new Properties();

			FileInputStream fis = new FileInputStream(f);
			pr.load(fis);

			String ret = pr.getProperty(key);
			fis.close();
			return ret;
		} catch (Exception e) {
			Logger.print(e, 1);
		}
		return null;
	}

	public static void writeValue(String user, String key, String value)  {
		try {
		String username = user.replaceAll(" ", "_");
			File f = new File("players/" + username.toLowerCase() + ".cfg");
			Properties pr = new Properties();

			FileInputStream fis = new FileInputStream(f);
			pr.load(fis);
			fis.close();
			pr.setProperty(key, value);

			FileOutputStream fos = new FileOutputStream(f);
			pr.store(fos, "");
			fos.close();
		} catch (Exception e) {
			Logger.print(e, 1);
		}
	}

	private void button13ActionPerformed(ActionEvent e) {
		int id = Integer.valueOf(textField10.getText());
		int exp = Integer.valueOf(textField13.getText());
		String user = label38.getText();
		if(id < 0 || id > 18) {
			JOptionPane.showMessageDialog(null, "Invalid Skill ID, Must be 0-18.");
			return;
		}
		if(isOnline(user)) {
			Player p = world.getPlayer(DataConversions.usernameToHash(user));
			if(radioButton1.isSelected()) {
				p.incExp(id, exp, false, false);
				p.getActionSender().sendMessage("Administrator has just added " + exp + " experience to your " + Formulae.statArray[id] + " skill");
				Logger.print("Administrator has given " + user + " " + exp + " " + Formulae.statArray[id] + " experience", 3);
			} else {
				p.setExp(id, p.getExp(id) - exp);
				p.getActionSender().sendMessage("Administrator has just removed " + exp + " experience from your " + Formulae.statArray[id] + " skill");
				Logger.print("Administrator has removed " + exp + " " + Formulae.statArray[id] + "exp from " + user, 3);
			}
			p.getActionSender().sendStat(id);
		} else {
			int old = Integer.valueOf(readValue(user, "e" + (id + 1)));
			if(radioButton1.isSelected()) {
				writeValue(user, "e" + (id + 1), (old + exp) + "");
				Logger.print("Administrator has given " + user + " " + exp + " " + Formulae.statArray[id] + " experience", 3);
			} else {
				writeValue(user, "e" + (id + 1), (old - exp < 1 ? 0 : old - exp) + "");
				Logger.print("Administrator has removed " + exp + " " + Formulae.statArray[id] + "exp from " + user, 3);
			}
		}
		textField10.setText("");
		textField13.setText("");
	}

	private void radioButton3ActionPerformed(ActionEvent e) {
		if(!radioButton3.isSelected())
			radioButton3.setSelected(true);

		radioButton4.setSelected(false);
	}

	private void radioButton4ActionPerformed(ActionEvent e) {
		if(!radioButton4.isSelected())
			radioButton4.setSelected(true);

		radioButton3.setSelected(false);
	}

	private void button14ActionPerformed(ActionEvent e) {
		int id = Integer.valueOf(textField14.getText());
		int amount = Integer.valueOf(textField15.getText());
		String user = label38.getText();
		if(id < 0 || id > 1500) {
			JOptionPane.showMessageDialog(null, "Invalid ID.");
			return;
		}
		
		if(isOnline(user)) {
			Player p = world.getPlayer(DataConversions.usernameToHash(user));
			if(radioButton1.isSelected()) {
				 p.getBank().add(new InvItem(id, amount));
				 Logger.print("Administrator has added " + amount + "x " + new InvItem(id).getDef().name + " to " + p.getUsername() + "'s Bank", 2);
				 p.getActionSender().sendMessage("Administrator has added " + amount + "x " + new InvItem(id).getDef().name + " to your Bank");
			return;
			} 
			if(radioButton2.isSelected()) {
				p.getBank().remove(id, amount);
				Logger.print("Administrator has removed " + amount + "x " + new InvItem(id).getDef().name + " from " + p.getUsername() + "'s Bank", 2);
				p.getActionSender().sendMessage("Administrator has removed " + amount + "x " + new InvItem(id).getDef().name + " from your Bank");
				textField10.setText("");
			textField13.setText("");	
			return;
			}
					
		} 
	}

	private void button7ActionPerformed(ActionEvent e) {
		if(isOnline(label38.getText())) {
			Player p = world.getPlayer(DataConversions.usernameToHash(label38.getText()));
			Point po = new Point((Integer.valueOf(textField16.getText())), Integer.valueOf(textField17.getText()));
			p.teleport(po.x, po.y, true);
			p.getActionSender().sendMessage("You have been teleported by the server Administrator");
			refreshWorldList(p.getUsername());
			Logger.print("Server Admin has teleported " + p.getUsername() + " to " + po.x + ", " + po.y, 2);
		}
	}

	private void initComponents() {

		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Nub Nubbie
		tabbedPane1 = new JTabbedPane();
		panel1 = new JPanel();
		progressBar1 = new JProgressBar();
		label1 = new JLabel();
		button1 = new JButton();
		button2 = new JButton();
		label2 = new JLabel();
		label3 = new JLabel();
		label4 = new JLabel();
		label5 = new JLabel();
		label6 = new JLabel();
		label7 = new JLabel();
		label8 = new JLabel();
		textField1 = new JTextField();
		label9 = new JLabel();
		button3 = new JButton();
		button4 = new JButton();
		tabbedPane2 = new JTabbedPane();
		panel5 = new JPanel();
		scrollPane1 = new JScrollPane();
		textPane1 = new JTextPane();
		panel6 = new JPanel();
		scrollPane2 = new JScrollPane();
		textPane2 = new JTextPane();
		panel7 = new JPanel();
		scrollPane3 = new JScrollPane();
		textPane3 = new JTextPane();
		panel8 = new JPanel();
		scrollPane4 = new JScrollPane();
		textPane4 = new JTextPane();
		label10 = new JLabel();
		label20 = new JLabel();
		panel2 = new JPanel();
		textField5 = new JTextField();
		textField8 = new JTextField();
		textField9 = new JTextField();
		label14 = new JLabel();
		label17 = new JLabel();
		button5 = new JButton();
		textField4 = new JTextField();
		label19 = new JLabel();
		label21 = new JLabel();
		textField6 = new JTextField();
		label22 = new JLabel();
		textField7 = new JTextField();
		label23 = new JLabel();
		textField11 = new JTextField();
		label24 = new JLabel();
		textField12 = new JTextField();
		label25 = new JLabel();
		checkBox1 = new JCheckBox();
		panel3 = new JPanel();
		scrollPane5 = new JScrollPane();
		list1 = new JList(model);
		label26 = new JLabel();
		label27 = new JLabel();
		label28 = new JLabel();
		button6 = new JButton();
		button9 = new JButton();
		button10 = new JButton();
		button11 = new JButton();
		button12 = new JButton();
		label29 = new JLabel();
		label30 = new JLabel();
		label31 = new JLabel();
		label32 = new JLabel();
		label33 = new JLabel();
		label34 = new JLabel();
		textField10 = new JTextField();
		label35 = new JLabel();
		label36 = new JLabel();
		textField13 = new JTextField();
		label37 = new JLabel();
		radioButton1 = new JRadioButton();
		radioButton2 = new JRadioButton();
		button13 = new JButton();
		label38 = new JLabel();
		label39 = new JLabel();
		label40 = new JLabel();
		label41 = new JLabel();
		label42 = new JLabel();
		label43 = new JLabel();
		label44 = new JLabel();
		label45 = new JLabel();
		textField14 = new JTextField();
		label46 = new JLabel();
		textField15 = new JTextField();
		radioButton3 = new JRadioButton();
		radioButton4 = new JRadioButton();
		button14 = new JButton();
		label47 = new JLabel();
		textField16 = new JTextField();
		textField17 = new JTextField();
		label48 = new JLabel();
		label49 = new JLabel();
		button7 = new JButton();
		panel4 = new JPanel();
		panel9 = new JPanel();
		label11 = new JLabel();
		label12 = new JLabel();
		label13 = new JLabel();
		textField2 = new JTextField();


		// JFormDesigner evaluation mark
		setBorder(new javax.swing.border.CompoundBorder(
				new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
						"", javax.swing.border.TitledBorder.CENTER,
						javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
						java.awt.Color.red), getBorder())); addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});




						progressBar1.setMaximum(400);
						progressBar1.setMinimum(1);
						progressBar1.setValue(351);

						label1.setText("Users Online:");

						button1.setText("Start Server");
						button1.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button1ActionPerformed(e);
							}
						});

						button2.setText("Shutdown Server");
						button2.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button2ActionPerformed(e);
							}
						});
						//EasyRSC
						label2.setText("Server Status:");

						label3.setText("Offline");
						label3.setForeground(new Color(255, 0, 51));
						label3.setFont(new Font("Tahoma", Font.BOLD, 11));

						label4.setText("0/0");
						label4.setForeground(Color.red);

						label5.setText("Server Emulator:");

						label6.setText("EasyRSC v" + (double)GameVars.projectVersion);
						label6.setForeground(Color.blue);

						label7.setText("Server Name:");

						label8.setText("ServerName");
						label8.setForeground(new Color(0, 175, 255));
						label8.setFont(new Font("Tahoma", Font.BOLD, 11));

						textField1.addKeyListener(new KeyAdapter() {
							@Override
							public void keyTyped(KeyEvent e) {
								//textField1KeyTyped(e);
							}
						});

						label9.setText("Server Announcement");

						button3.setText("Save All");
						button3.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button3ActionPerformed(e);
							}
						});

						button4.setText("Send");
						button4.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button4ActionPerformed(e);
							}
						});



						scrollPane1.setViewportView(textPane1);

						GroupLayout panel5Layout = new GroupLayout(panel5);
						panel5.setLayout(panel5Layout);
						panel5Layout.setHorizontalGroup(
								panel5Layout.createParallelGroup()
								.addGroup(panel5Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
										.addContainerGap())
						);
						panel5Layout.setVerticalGroup(
								panel5Layout.createParallelGroup()
								.addGroup(panel5Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
										.addContainerGap())
						);
						tabbedPane2.addTab("All", panel5);



						scrollPane2.setViewportView(textPane2);

						GroupLayout panel6Layout = new GroupLayout(panel6);
						panel6.setLayout(panel6Layout);
						panel6Layout.setHorizontalGroup(
								panel6Layout.createParallelGroup()
								.addGroup(panel6Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(scrollPane2, GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
										.addContainerGap())
						);
						panel6Layout.setVerticalGroup(
								panel6Layout.createParallelGroup()
								.addGroup(panel6Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(scrollPane2, GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
										.addContainerGap())
						);
						tabbedPane2.addTab("Errors", panel6);



						scrollPane3.setViewportView(textPane3);

						GroupLayout panel7Layout = new GroupLayout(panel7);
						panel7.setLayout(panel7Layout);
						panel7Layout.setHorizontalGroup(
								panel7Layout.createParallelGroup()
								.addGroup(panel7Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(scrollPane3, GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
										.addContainerGap())
						);
						panel7Layout.setVerticalGroup(
								panel7Layout.createParallelGroup()
								.addGroup(panel7Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(scrollPane3, GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
										.addContainerGap())
						);
						tabbedPane2.addTab("Staff Actions", panel7);



						scrollPane4.setViewportView(textPane4);

						GroupLayout panel8Layout = new GroupLayout(panel8);
						panel8.setLayout(panel8Layout);
						panel8Layout.setHorizontalGroup(
								panel8Layout.createParallelGroup()
								.addGroup(panel8Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(scrollPane4, GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
										.addContainerGap())
						);
						panel8Layout.setVerticalGroup(
								panel8Layout.createParallelGroup()
								.addGroup(panel8Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(scrollPane4, GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
										.addContainerGap())
						);
						tabbedPane2.addTab("Other", panel8);


						label10.setText("(0 Mods, 0 Admin)");
						label10.setForeground(new Color(218, 181, 47));

						label20.setText("(Peak 0)");

						GroupLayout panel1Layout = new GroupLayout(panel1);
						panel1.setLayout(panel1Layout);
						panel1Layout.setHorizontalGroup(
								panel1Layout.createParallelGroup()
								.addGroup(panel1Layout.createSequentialGroup()
										.addGroup(panel1Layout.createParallelGroup()
												.addGroup(panel1Layout.createSequentialGroup()
														.addGap(18, 18, 18)
														.addGroup(panel1Layout.createParallelGroup()
																.addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
																		.addGroup(panel1Layout.createSequentialGroup()
																				.addComponent(label2)
																				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																				.addComponent(label3))
																				.addGroup(panel1Layout.createSequentialGroup()
																						.addComponent(label1)
																						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																						.addComponent(label4)
																						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																						.addComponent(label10)))
																						.addGroup(panel1Layout.createSequentialGroup()
																								.addComponent(label7)
																								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																								.addComponent(label8))
																								.addGroup(panel1Layout.createSequentialGroup()
																										.addComponent(label5)
																										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(label6)))
																										.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(label20)
																										.addGap(102, 102, 102)
																										.addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
																												.addComponent(button3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																												.addComponent(button1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																												.addComponent(button2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
																												.addGroup(panel1Layout.createSequentialGroup()
																														.addContainerGap()
																														.addComponent(progressBar1, GroupLayout.PREFERRED_SIZE, 485, GroupLayout.PREFERRED_SIZE))
																														.addGroup(panel1Layout.createSequentialGroup()
																																.addContainerGap()
																																.addComponent(label9))
																																.addGroup(panel1Layout.createSequentialGroup()
																																		.addContainerGap()
																																		.addComponent(textField1, GroupLayout.PREFERRED_SIZE, 178, GroupLayout.PREFERRED_SIZE)
																																		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(button4))
																																		.addGroup(panel1Layout.createSequentialGroup()
																																				.addContainerGap()
																																				.addComponent(tabbedPane2, GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)))
																																				.addContainerGap())
						);
						panel1Layout.setVerticalGroup(
								panel1Layout.createParallelGroup()
								.addGroup(panel1Layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(progressBar1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(panel1Layout.createParallelGroup()
												.addGroup(panel1Layout.createSequentialGroup()
														.addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																.addComponent(label2)
																.addComponent(label3))
																.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(label1)
																		.addComponent(label4)
																		.addComponent(label10)
																		.addComponent(label20))
																		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																				.addComponent(label5)
																				.addComponent(label6))
																				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																				.addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																						.addComponent(label7)
																						.addComponent(label8)))
																						.addGroup(panel1Layout.createSequentialGroup()
																								.addComponent(button1)
																								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																								.addComponent(button2)
																								.addGap(18, 18, 18)
																								.addComponent(button3)))
																								.addGap(21, 21, 21)
																								.addComponent(label9)
																								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																								.addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																										.addComponent(textField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																										.addComponent(button4))
																										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(tabbedPane2, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
																										.addContainerGap())
						);
						tabbedPane1.addTab("Main", panel1);



						label14.setText("Exp Multiplier");
						label14.setFont(new Font("Arial", Font.PLAIN, 12));

						label17.setText("Arrow Delay Speed");
						label17.setFont(new Font("Arial", Font.PLAIN, 12));

						button5.setText("Update ALL");
						button5.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button5ActionPerformed(e);
							}
						});

						label19.setText("Server Location");

						label21.setText("Client Version");

						label22.setText("Server Name");

						label23.setText("Max Players");

						label24.setText("Port Number");

						label25.setText("Save All(mins)");
						label25.setFont(new Font("Arial", Font.PLAIN, 12));

						checkBox1.setText("Use Fatigue?");

						GroupLayout panel2Layout = new GroupLayout(panel2);
						panel2.setLayout(panel2Layout);
						panel2Layout.setHorizontalGroup(
								panel2Layout.createParallelGroup()
								.addGroup(panel2Layout.createSequentialGroup()
										.addContainerGap()
										.addGroup(panel2Layout.createParallelGroup()
												.addGroup(panel2Layout.createSequentialGroup()
														.addComponent(checkBox1)
														.addGap(115, 115, 115)
														.addComponent(button5)
														.addContainerGap(216, Short.MAX_VALUE))
														.addGroup(panel2Layout.createSequentialGroup()
																.addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
																		.addComponent(textField9, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
																		.addComponent(textField4, GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
																		.addComponent(textField6, GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
																		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(panel2Layout.createParallelGroup()
																				.addComponent(label22)
																				.addComponent(label19)
																				.addComponent(label21))
																				.addGap(143, 143, 143)
																				.addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
																						.addComponent(label17)
																						.addComponent(label14)
																						.addComponent(label25))
																						.addGroup(panel2Layout.createParallelGroup()
																								.addGroup(panel2Layout.createSequentialGroup()
																										.addGap(18, 18, 18)
																										.addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
																												.addComponent(textField5, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
																												.addComponent(textField8, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)))
																												.addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
																														.addGap(18, 18, 18)
																														.addComponent(textField12, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)))
																														.addContainerGap())
																														.addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
																																.addComponent(textField7, GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
																																.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																.addComponent(label23)
																																.addGap(367, 367, 367))
																																.addGroup(panel2Layout.createSequentialGroup()
																																		.addComponent(textField11, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
																																		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																		.addComponent(label24)
																																		.addContainerGap(365, Short.MAX_VALUE))))
						);
						panel2Layout.setVerticalGroup(
								panel2Layout.createParallelGroup()
								.addGroup(panel2Layout.createSequentialGroup()
										.addContainerGap()
										.addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
												.addComponent(textField5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(label14)
												.addComponent(textField6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(label22))
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
														.addComponent(textField4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addComponent(label17)
														.addComponent(textField8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addComponent(label19))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																.addComponent(textField9, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
																.addComponent(label21)
																.addComponent(textField12, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																.addComponent(label25))
																.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																		.addComponent(textField7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																		.addComponent(label23))
																		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(panel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																				.addComponent(textField11, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																				.addComponent(label24))
																				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 196, Short.MAX_VALUE)
																				.addGroup(panel2Layout.createParallelGroup()
																						.addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
																								.addComponent(button5)
																								.addGap(25, 25, 25))
																								.addGroup(GroupLayout.Alignment.TRAILING, panel2Layout.createSequentialGroup()
																										.addComponent(checkBox1)
																										.addGap(36, 36, 36))))
						);
						tabbedPane1.addTab("Settings", panel2);




						list1.addListSelectionListener(new ListSelectionListener() {
							public void valueChanged(ListSelectionEvent e) {
								list1ValueChanged(e);
							}
						});
						scrollPane5.setViewportView(list1);

						label26.setText("Accounts");
						label26.setFont(new Font("Tahoma", Font.BOLD, 14));

						label27.setText("Offline");
						label27.setForeground(Color.red);
						label27.setFont(new Font("Tahoma", Font.BOLD, 11));

						label28.setText("Status:");

						button6.setText("Refresh Players");
						button6.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button6ActionPerformed(e);
							}
						});

						button9.setText("Ban");
						button9.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button9ActionPerformed(e);
							}
						});

						button10.setText("Delete");
						button10.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button10ActionPerformed(e);
							}
						});

						button11.setText("Mute");
						button11.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button11ActionPerformed(e);
							}
						});

						button12.setText("Kick");
						button12.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button12ActionPerformed(e);
							}
						});

						label29.setText("Name:");

						label30.setText("IP:");

						label31.setText("Level:");

						label32.setText("Skill Total");

						label33.setText("Rank:");

						label34.setText("Coords:");

						label35.setText("Add/Remove Experience");

						label36.setText("Skill ID:");

						label37.setText("Exp:");

						radioButton1.setText("Add");
						radioButton1.setSelected(true);
						radioButton1.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								radioButton1ActionPerformed(e);
							}
						});

						radioButton2.setText("Remove");
						radioButton2.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								radioButton2ActionPerformed(e);
							}
						});

						button13.setText("Go!");
						button13.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button13ActionPerformed(e);
							}
						});

						label38.setText("NameHere");
						label38.setForeground(Color.blue);

						label39.setText("RankHere");
						label39.setForeground(Color.blue);

						label40.setText("IPHere");
						label40.setForeground(Color.blue);

						label41.setText("LvlHere");
						label41.setForeground(Color.blue);

						label42.setText("SkTotalHere");
						label42.setForeground(Color.blue);

						label43.setText("CoordsHere");
						label43.setForeground(Color.blue);

						label44.setText("             Add/Remove Bank Item");

						label45.setText("Item ID:");

						label46.setText("Amount:");

						radioButton3.setText("Remove");
						radioButton3.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								radioButton3ActionPerformed(e);
							}
						});

						radioButton4.setText("Add");
						radioButton4.setSelected(true);
						radioButton4.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								radioButton4ActionPerformed(e);
							}
						});

						button14.setText("Go!");
						button14.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button14ActionPerformed(e);
							}
						});

						label47.setText("Teleport To:");

						label48.setText(" X");
						label48.setFont(new Font("Tahoma", Font.BOLD, 12));

						label49.setText(" Y");
						label49.setFont(new Font("Tahoma", Font.BOLD, 12));

						button7.setText("Warp");
						button7.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								button7ActionPerformed(e);
							}
						});

						GroupLayout panel3Layout = new GroupLayout(panel3);
						panel3.setLayout(panel3Layout);
						panel3Layout.setHorizontalGroup(
								panel3Layout.createParallelGroup()
								.addGroup(panel3Layout.createSequentialGroup()
										.addGroup(panel3Layout.createParallelGroup()
												.addGroup(panel3Layout.createSequentialGroup()
														.addGap(41, 41, 41)
														.addComponent(label26)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 337, Short.MAX_VALUE))
														.addGroup(GroupLayout.Alignment.TRAILING, panel3Layout.createSequentialGroup()
																.addContainerGap()
																.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
																		.addComponent(button6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addComponent(scrollPane5, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE))
																		.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
																				.addGroup(panel3Layout.createSequentialGroup()
																						.addGap(10, 10, 10)
																						.addGroup(panel3Layout.createParallelGroup()
																								.addGroup(panel3Layout.createSequentialGroup()
																										.addComponent(label29)
																										.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(label38))
																										.addGroup(panel3Layout.createSequentialGroup()
																												.addComponent(label28)
																												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																												.addComponent(label27))
																												.addGroup(panel3Layout.createSequentialGroup()
																														.addComponent(label33)
																														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																														.addComponent(label39))
																														.addGroup(panel3Layout.createSequentialGroup()
																																.addComponent(label30)
																																.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																																.addComponent(label40))
																																.addGroup(panel3Layout.createSequentialGroup()
																																		.addComponent(label31)
																																		.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																																		.addComponent(label41))
																																		.addGroup(panel3Layout.createSequentialGroup()
																																				.addComponent(label32)
																																				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																																				.addComponent(label42))
																																				.addGroup(panel3Layout.createSequentialGroup()
																																						.addGroup(panel3Layout.createParallelGroup()
																																								.addGroup(GroupLayout.Alignment.TRAILING, panel3Layout.createSequentialGroup()
																																										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																										.addComponent(label35))
																																										.addGroup(panel3Layout.createSequentialGroup()
																																												.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
																																														.addComponent(label36)
																																														.addComponent(label37))
																																														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																														.addGroup(panel3Layout.createParallelGroup()
																																																.addComponent(textField13, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
																																																.addComponent(textField10, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE))
																																																.addGap(5, 5, 5)))
																																																.addGroup(panel3Layout.createParallelGroup()
																																																		.addGroup(panel3Layout.createSequentialGroup()
																																																				.addGap(46, 46, 46)
																																																				.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
																																																						.addComponent(label46)
																																																						.addComponent(label45)
																																																						.addComponent(radioButton4))
																																																						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																																						.addGroup(panel3Layout.createParallelGroup()
																																																								.addComponent(textField15, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
																																																								.addComponent(textField14, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
																																																								.addComponent(radioButton3)))
																																																								.addGroup(panel3Layout.createSequentialGroup()
																																																										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																																										.addComponent(label44))))
																																																										.addGroup(panel3Layout.createSequentialGroup()
																																																												.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
																																																														.addGroup(GroupLayout.Alignment.LEADING, panel3Layout.createSequentialGroup()
																																																																.addComponent(label34)
																																																																.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																																																																.addComponent(label43))
																																																																.addGroup(GroupLayout.Alignment.LEADING, panel3Layout.createSequentialGroup()
																																																																		.addComponent(label47)
																																																																		.addGroup(panel3Layout.createParallelGroup()
																																																																				.addGroup(panel3Layout.createSequentialGroup()
																																																																						.addGap(20, 20, 20)
																																																																						.addComponent(label48, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE))
																																																																						.addGroup(panel3Layout.createSequentialGroup()
																																																																								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																																																																								.addComponent(textField16, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)))))
																																																																								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																																																								.addGroup(panel3Layout.createParallelGroup()
																																																																										.addGroup(panel3Layout.createSequentialGroup()
																																																																												.addGap(10, 10, 10)
																																																																												.addComponent(label49, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE))
																																																																												.addGroup(panel3Layout.createSequentialGroup()
																																																																														.addComponent(textField17, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
																																																																														.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																																																																														.addComponent(button7))))))
																																																																														.addGroup(panel3Layout.createSequentialGroup()
																																																																																.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																																																																.addComponent(radioButton1)
																																																																																.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																																																																																.addComponent(radioButton2))
																																																																																.addGroup(panel3Layout.createSequentialGroup()
																																																																																		.addGap(18, 18, 18)
																																																																																		.addComponent(button13, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
																																																																																		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																																																																																		.addComponent(button14, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
																																																																																		.addGap(13, 13, 13)))))
																																																																																		.addGap(73, 73, 73))
																																																																																		.addGroup(GroupLayout.Alignment.TRAILING, panel3Layout.createSequentialGroup()
																																																																																				.addContainerGap(433, Short.MAX_VALUE)
																																																																																				.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
																																																																																						.addComponent(button10, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
																																																																																						.addComponent(button9, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
																																																																																						.addComponent(button11, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
																																																																																						.addComponent(button12, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE))
																																																																																						.addContainerGap())
						);
						panel3Layout.setVerticalGroup(
								panel3Layout.createParallelGroup()
								.addGroup(panel3Layout.createSequentialGroup()
										.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
												.addGroup(GroupLayout.Alignment.LEADING, panel3Layout.createSequentialGroup()
														.addGap(5, 5, 5)
														.addComponent(label26)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(panel3Layout.createParallelGroup()
																.addGroup(panel3Layout.createSequentialGroup()
																		.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																				.addComponent(label28)
																				.addComponent(label27, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
																				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																				.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																						.addComponent(label29)
																						.addComponent(label38))
																						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																						.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																								.addComponent(label33)
																								.addComponent(label39))
																								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																								.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																										.addComponent(label30)
																										.addComponent(label40))
																										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																										.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																												.addComponent(label31)
																												.addComponent(label41))
																												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																												.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																														.addComponent(label32)
																														.addComponent(label42))
																														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																														.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																																.addComponent(label34)
																																.addComponent(label43))
																																.addGap(24, 24, 24)
																																.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																																		.addComponent(label47)
																																		.addComponent(textField16, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																		.addComponent(textField17, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																		.addComponent(button7))
																																		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																		.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																																				.addComponent(label48)
																																				.addComponent(label49)))
																																				.addComponent(scrollPane5, GroupLayout.PREFERRED_SIZE, 315, GroupLayout.PREFERRED_SIZE)))
																																				.addGroup(panel3Layout.createSequentialGroup()
																																						.addGap(28, 28, 28)
																																						.addComponent(button12)
																																						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																						.addComponent(button11)
																																						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																																						.addComponent(button9)
																																						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																						.addComponent(button10)
																																						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																																						.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																																								.addComponent(label35)
																																								.addComponent(label44))
																																								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																								.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																																										.addComponent(textField10, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																										.addComponent(label36)
																																										.addComponent(label45)
																																										.addComponent(textField14, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																																										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																										.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																																												.addComponent(textField13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																																												.addComponent(label37)
																																												.addComponent(label46)
																																												.addComponent(textField15, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																																												.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
																																												.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																																														.addComponent(radioButton1)
																																														.addComponent(radioButton2)
																																														.addComponent(radioButton3)
																																														.addComponent(radioButton4))))
																																														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																																														.addGroup(panel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																																																.addComponent(button6)
																																																.addComponent(button13)
																																																.addComponent(button14))
																																																.addGap(18, 18, 18))
						);
						tabbedPane1.addTab("World", panel3);



						GroupLayout panel4Layout = new GroupLayout(panel4);
						panel4.setLayout(panel4Layout);
						panel4Layout.setHorizontalGroup(
								panel4Layout.createParallelGroup()
								.addGap(0, 515, Short.MAX_VALUE)
						);
						panel4Layout.setVerticalGroup(
								panel4Layout.createParallelGroup()
								.addGap(0, 390, Short.MAX_VALUE)
						);
						tabbedPane1.addTab("Reports", panel4);



						label11.setText("EasyRSC Created by xEnt");
						label11.setFont(new Font("Tahoma", Font.BOLD, 16));

						label12.setText("to Report Bugs or Suggestion, visit Moparscape in the EasyRSC Topic: ");
						label12.setFont(new Font("Tahoma", Font.PLAIN, 14));

						label13.setText("Based off the RSCD v25 source by Reines");
						label13.setFont(new Font("Tahoma", Font.BOLD, 12));

						textField2.setText("http://www.moparscape.org/smf/index.php/topic,340170.0.html");

						GroupLayout panel9Layout = new GroupLayout(panel9);
						panel9.setLayout(panel9Layout);
						panel9Layout.setHorizontalGroup(
								panel9Layout.createParallelGroup()
								.addGroup(panel9Layout.createSequentialGroup()
										.addGroup(panel9Layout.createParallelGroup()
												.addGroup(panel9Layout.createSequentialGroup()
														.addGap(127, 127, 127)
														.addComponent(label11, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE))
														.addGroup(panel9Layout.createSequentialGroup()
																.addGap(99, 99, 99)
																.addComponent(label13, GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)))
																.addContainerGap(55, Short.MAX_VALUE))
																.addGroup(panel9Layout.createSequentialGroup()
																		.addGap(35, 35, 35)
																		.addComponent(label12, GroupLayout.PREFERRED_SIZE, 442, GroupLayout.PREFERRED_SIZE)
																		.addContainerGap(38, Short.MAX_VALUE))
																		.addGroup(panel9Layout.createSequentialGroup()
																				.addGap(83, 83, 83)
																				.addComponent(textField2, GroupLayout.PREFERRED_SIZE, 331, GroupLayout.PREFERRED_SIZE)
																				.addContainerGap(101, Short.MAX_VALUE))
						);
						panel9Layout.setVerticalGroup(
								panel9Layout.createParallelGroup()
								.addGroup(panel9Layout.createSequentialGroup()
										.addGap(32, 32, 32)
										.addComponent(label11, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(label13, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(label12)
										.addGap(18, 18, 18)
										.addComponent(textField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addContainerGap(230, Short.MAX_VALUE))
						);
						tabbedPane1.addTab("Credits", panel9);


						GroupLayout layout = new GroupLayout(this);
						setLayout(layout);
						layout.setHorizontalGroup(
								layout.createParallelGroup()
								.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(tabbedPane1, GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
										.addContainerGap())
						);
						layout.setVerticalGroup(
								layout.createParallelGroup()
								.addGroup(layout.createSequentialGroup()
										.addContainerGap()
										.addComponent(tabbedPane1, GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
										.addContainerGap())
						);



						super.setVisible(true);
						JFrame frame = new JFrame("EasyRSC v" + (double)GameVars.projectVersion + " Control Panel");
						frame.setSize(800, 800);
						GUI.progressBar1.setValue(GameVars.usersOnline);
						frame.setLocation(100, 100);
						frame.add(this);
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						frame.pack();
						button3.setEnabled(false);
						textField1.setEnabled(false);
						button4.setEnabled(false);
						frame.setResizable(false);
						frame.setVisible(true);
						GUI.populateWorldList();
						// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	public static void cout(String s, int i) {
		String type = "";
		if(i == 1) {
			type = "[Error] ";
			textPane2.setText(textPane2.getText() + "\r\n" + s);
		}
		else if(i == 2) // Staff Actions
		{
			type = "[Staff] ";
			textPane3.setText(textPane3.getText() + "\r\n" + s);
		}
		else if(i == 3) // Other
		{
			type = "[Other] ";
			textPane4.setText(textPane4.getText() + "\r\n" + s);
		}

		// All.
		textPane1.setText(textPane1.getText() + "\r\n" + type + s);
		textPane2.setCaretPosition(textPane2.getDocument().getLength());
		textPane3.setCaretPosition(textPane3.getDocument().getLength());
		textPane4.setCaretPosition(textPane4.getDocument().getLength());
		textPane1.setCaretPosition(textPane1.getDocument().getLength());
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Nub Nubbie
	public static JTabbedPane tabbedPane1;
	public static JPanel panel1;
	public static JProgressBar progressBar1;
	public static JLabel label1;
	public static JButton button1;
	public static JButton button2;
	public static JLabel label2;
	public static JLabel label3;
	public static JLabel label4;
	public static JLabel label5;
	public static JLabel label6;
	public static JLabel label7;
	public static JLabel label8;
	public static JTextField textField1;
	public static JLabel label9;
	public static JButton button3;
	public static JButton button4;
	public static JTabbedPane tabbedPane2;
	public static JPanel panel5;
	public static JScrollPane scrollPane1;
	public static JTextPane textPane1;
	public static JPanel panel6;
	public static JScrollPane scrollPane2;
	public static JTextPane textPane2;
	public static JPanel panel7;
	public static JScrollPane scrollPane3;
	public static JTextPane textPane3;
	public static JPanel panel8;
	public static JScrollPane scrollPane4;
	public static JTextPane textPane4;
	public static JLabel label10;
	public static JLabel label20;
	public static JPanel panel2;
	public static JTextField textField5;
	public static JTextField textField8;
	public static JTextField textField9;
	public static JLabel label14;
	public static JLabel label17;
	public static JButton button5;
	public static JTextField textField4;
	public static JLabel label19;
	public static JLabel label21;
	public static JTextField textField6;
	public static JLabel label22;
	public static JTextField textField7;
	public static JLabel label23;
	public static JTextField textField11;
	public static JLabel label24;
	public static JTextField textField12;
	public static JLabel label25;
	public static JCheckBox checkBox1;
	public static JPanel panel3;
	public static JScrollPane scrollPane5;
	public static JList list1;
	public static JLabel label26;
	public static JLabel label27;
	public static JLabel label28;
	public static JButton button6;
	public static JButton button9;
	public static JButton button10;
	public static JButton button11;
	public static JButton button12;
	public static JLabel label29;
	public static JLabel label30;
	public static JLabel label31;
	public static JLabel label32;
	public static JLabel label33;
	public static JLabel label34;
	public static JTextField textField10;
	public static JLabel label35;
	public static JLabel label36;
	public static JTextField textField13;
	public static JLabel label37;
	public static JRadioButton radioButton1;
	public static JRadioButton radioButton2;
	public static JButton button13;
	public static JLabel label38;
	public static JLabel label39;
	public static JLabel label40;
	public static JLabel label41;
	public static JLabel label42;
	public static JLabel label43;
	public static JLabel label44;
	public static JLabel label45;
	public static JTextField textField14;
	public static JLabel label46;
	public static JTextField textField15;
	public static JRadioButton radioButton3;
	public static JRadioButton radioButton4;
	public static JButton button14;
	public static JLabel label47;
	public static JTextField textField16;
	public static JTextField textField17;
	public static JLabel label48;
	public static JLabel label49;
	public static JButton button7;
	public static JPanel panel4;
	public static JPanel panel9;
	public static JLabel label11;
	public static JLabel label12;
	public static JLabel label13;
	public static DefaultListModel model = new DefaultListModel();
	public static JTextField textField2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
