package org.rscdaemon.server.npchandler;

/**
 * The definition of a npc handler, for use with XStream.
 */
public class NpcHandlerDef {
	/**
	 * The IDs of npcs which the handler is responsible for
	 */
	public int[] ids;
	/**
	 * The name of the handler class
	 */
	public String className;

	/**
	 * Constructs a new packet handler definition, entailing that
	 * npcs with the given IDs are to be handled by the specified
	 * class.
	 *
	 * @param ids	   The IDs of npcs to be handled
	 * @param className The name of the packet handler class
	 */
	public NpcHandlerDef(int[] ids, String className) {
		this.ids = ids;
		this.className = className;
	}

	/**
	 * Returns the name of the npc handler class.
	 *
	 * @return The name of the npc handler's class
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Returns the IDs of npcs to be handled.
	 *
	 * @return An <code>int</code> array containing the IDs of
	 *         npcs to be handled.
	 */
	public int[] getAssociatedNpcs() {
		return ids;
	}
}
																							 
