package net.idea.ops.cli.lookup;

import java.net.MalformedURLException;

import net.idea.opentox.cli.id.Identifier;
import net.idea.ops.cli.assay.Assay;

public class AssayLookup extends Lookup<Assay> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7114460514902615599L;

	@Override
	protected Assay create(String key) throws MalformedURLException {
		return new Assay(new Identifier(key));
	}

}
