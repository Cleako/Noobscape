package org.rscdaemon.server.event;

import org.rscdaemon.server.model.Npc;
import org.rscdaemon.server.model.Player;
import org.rscdaemon.server.model.ChatMessage;

public abstract class DelayedQuestChat extends DelayedEvent {
	public int curIndex;
	public String[] messages;
	public Npc npc;
	public Player owner;
	public DelayedQuestChat(Npc npc, Player owner, String[] messages) {
		super(null, 2200);
		this.owner = owner;
		this.npc = npc;
		this.messages = messages;
		curIndex = 0;
	}
	
	public void run() {
		owner.informOfNpcMessage(new ChatMessage(npc, messages[curIndex], owner));
		curIndex++;
		if (curIndex == messages.length) {
			finished();
			stop();
			return;
		}
	}
	
	public abstract void finished();
}