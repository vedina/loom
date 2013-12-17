package net.idea.loom.common;

public interface ICallBack<ID,V> {
	public V process(ID identifier,V value);
}
