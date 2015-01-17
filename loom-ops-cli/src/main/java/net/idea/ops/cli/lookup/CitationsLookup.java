package net.idea.ops.cli.lookup;

import java.net.MalformedURLException;

import net.idea.ops.cli.assay.Citation;

public class CitationsLookup extends Lookup<Citation> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7114460514902615599L;

    @Override
    protected Citation create(String key) throws MalformedURLException {
	return new Citation(key);
    }

}
