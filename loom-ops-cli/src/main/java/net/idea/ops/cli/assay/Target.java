package net.idea.ops.cli.assay;

import java.net.URL;

import net.idea.opentox.cli.AbstractURLResource;
import net.idea.opentox.cli.dataset.Dataset;

/**
 * Target
 * @author nina
 *
 */
public class Target extends AbstractURLResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3642704198653144268L;
	protected String targetFamily;
	public String getTargetFamily() {
		return targetFamily;
	}

	public void setTargetFamily(String targetFamily) {
		this.targetFamily = targetFamily;
	}
	protected String title;
	protected String prefLabel;
	protected String prefLabelEN;
	
	protected String organism;
	protected Dataset inDataset;
	
	public Dataset getInDataset() {
		return inDataset;
	}

	public void setInDataset(Dataset inDataset) {
		this.inDataset = inDataset;
	}

	public String getOrganism() {
		return organism;
	}

	public void setOrganism(String organism) {
		this.organism = organism;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	public String getPrefLabel() {
		return prefLabel;
	}

	public void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}

	public String getPrefLabelEN() {
		return prefLabelEN;
	}

	public void setPrefLabelEN(String prefLabelEN) {
		this.prefLabelEN = prefLabelEN;
	}
	public Target() {
		super(null);
	}
	
	public Target(URL url) {
		super(url);
	}
	@Override
	public int hashCode() {
		return
		(getResourceIdentifier()==null)?"".hashCode():
		getResourceIdentifier().toExternalForm().hashCode();
	}
}
