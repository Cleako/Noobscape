package org.rscdaemon.server.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.io.File;

import org.rscdaemon.server.GUI;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.util.Logger;

/**
 * 
 * @author Ent
 * Loads Player details up.
 */
public class PlayerLoader {

	static Properties props = new Properties();

	public static int getLogin(String user, String pass) {
		try {

		
			//user = user.replaceAll("_", " ");	

			File f = new File("players/" + user.trim() + ".cfg");
			if(f.exists()) {
				FileInputStream fis = new FileInputStream(f);
				props.load(fis);
				if(Integer.valueOf(props.getProperty("rank")) == 6) {
					fis.close();
					return 6; // Banned.
				}

				if(props.getProperty("pass").equalsIgnoreCase(pass)) {
					if(props.getProperty("loggedin").equalsIgnoreCase("true")) {
						fis.close();
						return 2; // Already logged in.
					}
					if(Integer.valueOf(props.getProperty("rank")) == 1) {
						java.util.Calendar cal = java.util.Calendar.getInstance();
						System.out.println(Player.getRemSub(user));
						if(Player.getSubEnd(user) <= (cal.getTime().getTime())) {
							Player.unSetSub(user);
						}
					}
					fis.close();
					return 1; // Correct, Log in.

				} else  { // Bad password
					fis.close();
					return 0;
				}
			}
			
			if(user.startsWith("_")) {
				//System.out.println("test test test");
				return 0;
			}
			if(user.endsWith("_")) {
				//System.out.println("test test test");
				return 0;
			}				
			else {
				File fi = new File("players/" + user + ".cfg");
				copy(new File("players/Template"), fi);
				GUI.writeValue(user, "pass", pass);
				Logger.print("Account Created: " + user, 3);
				return 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			org.rscdaemon.server.util.Logger.print(e.toString(), 1);
			return 0;
		}
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
}
