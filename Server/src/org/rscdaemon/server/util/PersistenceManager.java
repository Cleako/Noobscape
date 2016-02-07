package org.rscdaemon.server.util;

import com.thoughtworks.xstream.XStream;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class PersistenceManager {
	private static final XStream xstream = new XStream();

	static {
		setupAliases();
	}

	public static void setupAliases() {
		try {
			Properties aliases = new Properties();
			FileInputStream fis = new FileInputStream(new File(Config.CONF_DIR, "aliases.xml"));
			aliases.loadFromXML(fis);
			for (Enumeration<?> e = aliases.propertyNames(); e.hasMoreElements();) {
				String alias = (String) e.nextElement();
				Class<?> c = Class.forName((String) aliases.get(alias));
				xstream.alias(alias, c);
			}
		}
		catch (Exception ioe) {
			Logger.error(ioe);
		}
	}

	public static Object load(String filename) {
		try {
			InputStream is = new FileInputStream(new File(Config.CONF_DIR, filename));
			if (filename.endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
			Object rv = xstream.fromXML(is);
			return rv;
		}
		catch (IOException ioe) {
			Logger.error(ioe);
		}
		return null;
	}

	public static void write(String filename, Object o) {
		try {
			OutputStream os = new FileOutputStream(new File(Config.CONF_DIR, filename));
			if (filename.endsWith(".gz")) {
				os = new GZIPOutputStream(os);
			}
			xstream.toXML(o, os);
		}
		catch (IOException ioe) {
			Logger.error(ioe);
		}
	}
}
