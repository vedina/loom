package net.idea.ops.cli.lookup;

import java.net.MalformedURLException;

import net.idea.ops.cli.assay.ActivityType;

public class ActivityTypeLookup extends Lookup<ActivityType> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7610841229782519772L;

    @Override
    protected ActivityType create(String key) throws MalformedURLException {
	return new ActivityType(key);
    }

}
