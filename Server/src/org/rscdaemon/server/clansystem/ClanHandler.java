package org.rscdaemon.server.clansystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.rscdaemon.server.model.*;
import org.rscdaemon.server.util.Logger;

public class ClanHandler {
	public ClanHandler() {
		clans = new ArrayList<Clan>();
		loadClans();
	}
	
	public void loadClans() {
		File folder = new File(".\\clans\\");
		if(!folder.exists()) 
			System.out.println("folder null");
		int numberOfClans = 0;
		for(File clanFile : folder.listFiles()) {
			if(!clanFile.getName().toLowerCase().endsWith(".cfg")) {
				continue;
			}
			try {
				FileInputStream fis = new FileInputStream(clanFile);
				Properties p = new Properties();
				p.load(fis);
				String ownerName, clanName;
				ownerName = p.getProperty("owner");
				clanName = p.getProperty("name");
				if(ownerName.equals("NULL") || clanName.equals("NULL")) {
					System.out.println("WARNING: "+clanFile.getName()+" is invalid");
					continue;
				}
				addClan(clanName, ownerName);
				++numberOfClans;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		System.out.println("Loaded " + numberOfClans + " clans");
	}
	
	public boolean addClan(String name, String ownerName) {
	try {
		if(getClanByName(name) != null) {
			return false;
		}
		} catch(Throwable t) { t.printStackTrace();}
		Clan c = new Clan(name);
		c.setOwnerName(ownerName);
		return clans.add(c);
	}
	
	public boolean removeClan(String name) {
		synchronized(clans) {
			Clan c = getClanByName(name);
			if(c == null) {
				return false;
			}
			return clans.remove(c);
		}
	}
	
	public Player getOwnerByClanName(String name) {
		return getClanByName(name).getOwner();
	}
	
	public Clan getClanByName(String name) {
		for(Clan c : clans) {
			if(c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}
	
	public static boolean createClan(String name, Player owner) {
		try {
			if(!owner.canCreateClan()) {
				owner.getActionSender().sendMessage("You must wait 2 hours before creating a new clan");
				return false;
			}
			File f = new File(".\\clans\\" + name + ".cfg");
			if(f.exists()) {
				return false;
			}
			copy(new File("clans\\Template"), f);
			Properties p = new Properties();
			FileInputStream fis;
			
			p.load(fis = new FileInputStream(f));
			fis.close();
			p.setProperty("owner", owner.getUsername());
			p.setProperty("name", name);
			FileOutputStream fos = new FileOutputStream(f);
			p.store(fos, "");
			fos.flush();
			fos.close();
			owner.setLastCreatedClan();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	static void copy(File src, File dst) throws IOException {
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);

			byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			in.close();
			out.close();
		} catch (Exception e) {
			Logger.print(e, 1);
		}
	}
	
	private ArrayList<Clan> clans;
}
