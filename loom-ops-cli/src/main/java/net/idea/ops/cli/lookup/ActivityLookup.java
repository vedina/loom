package net.idea.ops.cli.lookup;

import java.net.MalformedURLException;

import net.idea.opentox.cli.id.Identifier;
import net.idea.ops.cli.assay.Activity;

public class ActivityLookup extends Lookup<Activity> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7610841229782519772L;

	@Override
	protected Activity create(String key) throws MalformedURLException {
		return new Activity(new Identifier(key));
	}

}
