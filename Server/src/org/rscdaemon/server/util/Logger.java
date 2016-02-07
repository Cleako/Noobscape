package org.rscdaemon.server.util;

import org.rscdaemon.server.model.World;

public class Logger {
	/**
	 * World instance
	 */
	private static final World world = World.getWorld();
	
	public static void print(Object o, int i) {	
		org.rscdaemon.server.GUI.cout(o.toString(), i);		
	}
	
	public static void connection(Object o) {
//		System.out.println(o.toString());
	}
	
	public static void mod(Object o) {
		org.rscdaemon.server.GUI.cout(o.toString(), 2);	
	}
	
	public static void event(Object o) {
		
	}
	
	public static void error(Object o) {
		if(o instanceof Exception) {
			Exception e = (Exception)o;
			e.printStackTrace();
			org.rscdaemon.server.GUI.cout(e.getMessage(), 1);
			org.rscdaemon.server.GUI.cout(e.getStackTrace().toString(), 1);
			if(world == null || !world.getServer().isInitialized()) {
				System.exit(1);
			}
			else {
				//world.getServer().kill();
			}
			return;
		}
	}
}
