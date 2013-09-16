package net.idea.ops.cli.lookup;

import java.net.MalformedURLException;
import java.util.Hashtable;

import net.idea.opentox.cli.AbstractURLResource;

public abstract class Lookup<T extends AbstractURLResource> extends Hashtable<String, T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8703996995394361092L;
	
	public synchronized T lookup(String key) throws MalformedURLException {
		T object = get(key);
		if (object==null) {
			object = create(key);
			put(key,object);
		}
		return object;
	}
	protected abstract T create(String key) throws MalformedURLException;
}
