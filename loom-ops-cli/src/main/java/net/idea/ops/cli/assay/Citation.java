package net.idea.ops.cli.assay;

import net.idea.opentox.cli.AbstractURLResource;
import net.idea.opentox.cli.id.IIdentifier;
import net.idea.opentox.cli.id.Identifier;

public class Citation extends AbstractURLResource {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4467634313071321838L;

	public Citation(String pubmedid) {
		super(null);
		setPubmedID(pubmedid);
	}

	public Citation(IIdentifier url) {
		super(url);
	}

	@Override
	public int hashCode() {
		return (getResourceIdentifier() == null) ? "".hashCode()
				: getResourceIdentifier().toExternalForm().hashCode();
	}

	protected void setPubmedID(String pubmedid) {
		try {
			if (pubmedid.startsWith("http"))
				setResourceIdentifier(new Identifier(pubmedid));
			else
				setResourceIdentifier(new Identifier(
						"http://www.ncbi.nlm.nih.gov/pubmed/" + pubmedid));
		} catch (Exception x) {
			setResourceIdentifier(null);
		}
	}
}
