package net.idea.loom.common;

public interface ICallBack<ID,V,RESULT> {
	public RESULT process(ID identifier,V value);
	public RESULT done(RESULT result);
}
