package org.rscdaemon.server.entityhandling.defs.extras;

public class CerterDef {
	/**
	 * Type of stall
	 */
	private String type;
	/**
	 * Certs this stall can deal with
	 */
	private CertDef[] certs;

	public String getType() {
		return type;
	}
	
	public String[] getCertNames() {
		String[] names = new String[certs.length];
		for(int i = 0;i < certs.length;i++) {
			names[i] = certs[i].getName();
		}
		return names;
	}
	
	public int getCertID(int index) {
		if(index < 0 || index >= certs.length) {
			return -1;
		}
		return certs[index].getCertID();
	}
	
	public int getItemID(int index) {
		if(index < 0 || index >= certs.length) {
			return -1;
		}
		return certs[index].getItemID();
	}
}