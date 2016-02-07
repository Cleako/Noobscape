package org.rscdaemon.server.model;

import org.rscdaemon.server.util.Formulae;
import org.rscdaemon.server.util.DataConversions;

public class PlayerAppearance {

	private byte hairColour;
	private byte topColour;
	private byte trouserColour;
	private byte skinColour;
	
	public int head;
	public int body;
	
	public PlayerAppearance(int hairColour, int topColour, int trouserColour, int skinColour, int head, int body) {
		this.hairColour = (byte)hairColour;
		this.topColour = (byte)topColour;
		this.trouserColour = (byte)trouserColour;
		this.skinColour = (byte)skinColour;
		this.head = head;
		this.body = body;
	}
	
	public int getSprite(int pos) {
		switch(pos) {
			case 0:
				return head;
			case 1:
				return body;
			case 2:
				return 3;
			default:
				return 0;
		}
	}
	
	public int[] getSprites() {
		return new int[]{head, body, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	}
	
	public byte getHairColour() {
		return hairColour;
	}
	
	public byte getTopColour() {
		return topColour;
	}
	
	public byte getTrouserColour() {
		return trouserColour;
	}
	
	public byte getSkinColour() {
		return skinColour;
	}
	
	public boolean isValid() {
		if(!DataConversions.inArray(Formulae.headSprites, head) || !DataConversions.inArray(Formulae.bodySprites, body)) {
			return false;
		}
		if(hairColour < 0 || topColour < 0 || trouserColour < 0 || skinColour < 0) {
			return false;
		}
		if(hairColour > 9 || topColour > 14 || trouserColour > 14 || skinColour > 4) {
			return false;
		}
		return true;
	}

}
