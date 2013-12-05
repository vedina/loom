package net.idea.ops.cli.assay;

import java.net.URL;

import net.idea.opentox.cli.AbstractURLResource;

public class Citation  extends AbstractURLResource {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4467634313071321838L;
	
	public Citation(String pubmedid) {
		super(null);
		setPubmedID(pubmedid);
	}
	
	public Citation(URL url) {
		super(url);
	}
	@Override
	public int hashCode() {
		return
		(getResourceIdentifier()==null)?"".hashCode():
		getResourceIdentifier().toExternalForm().hashCode();
	}
	
	protected void setPubmedID(String pubmedid) {
		try {
			if (pubmedid.startsWith("http")) setResourceIdentifier(new URL(pubmedid));
			else setResourceIdentifier(new URL("http://www.ncbi.nlm.nih.gov/pubmed/"+pubmedid));
		} catch (Exception x) {
			setResourceIdentifier(null);
		}
	}
}
