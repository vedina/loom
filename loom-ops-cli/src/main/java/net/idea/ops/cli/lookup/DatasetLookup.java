package net.idea.ops.cli.lookup;

import java.net.MalformedURLException;
import java.net.URL;

import net.idea.opentox.cli.dataset.Dataset;

public class DatasetLookup extends Lookup<Dataset> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8627930094390595214L;

    @Override
    protected Dataset create(String key) throws MalformedURLException {
	return new Dataset(new URL(key));
    }

}
