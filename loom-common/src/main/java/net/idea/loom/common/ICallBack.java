package net.idea.loom.common;

public interface ICallBack<ID,V> {
	public void process(ID identifier,V value);
	public void done(ID identifier);
}
