package net.idea.ops.cli.lookup;

import java.net.MalformedURLException;
import java.net.URL;

import net.idea.ops.cli.assay.Target;

public class TargetLookup extends Lookup<Target> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8024550925404253449L;

    @Override
    protected Target create(String key) throws MalformedURLException {
	return new Target(new URL(key));
    }

}
