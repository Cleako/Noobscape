package org.rscdaemon.server.util;

import org.rscdaemon.server.model.Entity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class EntityList<T extends Entity> implements Iterable<T> {
	/**
	 * The Default capacity of an EntityList
	 */
	public static final int DEFAULT_CAPACITY = 2000;
	protected Object[] entities;
	protected Set<Integer> indicies = new HashSet<Integer>();
	protected int curIndex = 0;
	protected int capacity;

	public EntityList(int capacity) {
		entities = new Object[capacity];
		this.capacity = capacity;
	}

	public EntityList() {
		this(DEFAULT_CAPACITY);
	}

	public void remove(T entity) {
		entities[entity.getIndex()] = null;
		indicies.remove(entity.getIndex());
	}

	@SuppressWarnings("unchecked")
	public T remove(int index) {
		Object temp = entities[index];
		entities[index] = null;
		indicies.remove(index);
		return (T) temp;
	}

	@SuppressWarnings("unchecked")
	public T get(int index) {
		return (T) entities[index];
	}
	
	public void add(T entity) {
		if(entities[curIndex] != null) {
			increaseIndex();
			add(entity);
		}
		else {
			entities[curIndex] = entity;
			entity.setIndex(curIndex);
			indicies.add(curIndex);
			increaseIndex();
		}
	}

	public Iterator<T> iterator() {
		return new EntityListIterator<T>(entities, indicies, this);
	}

	public void increaseIndex() {
		curIndex++;
		if (curIndex >= capacity) {
			curIndex = 0;
		}
	}

	public boolean contains(T entity) {
		return indexOf(entity) > -1;
	}

	public int indexOf(T entity) {
		for (int index : indicies) {
			if (entities[index].equals(entity)) {
				return index;
			}
		}
		return -1;
	}

	public int count() {
		return indicies.size();
	}
	
	public int size() {
		return indicies.size();
	}
}
