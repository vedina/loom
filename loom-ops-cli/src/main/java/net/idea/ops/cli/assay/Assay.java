package net.idea.ops.cli.assay;

import net.idea.opentox.cli.AbstractURLResource;
import net.idea.opentox.cli.id.IIdentifier;

public class Assay extends AbstractURLResource {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4130547421041600967L;
    protected String description;
    protected String organism;
    protected Target target;

    public Target getTarget() {
	return target;
    }

    public void setTarget(Target target) {
	this.target = target;
    }

    public String getOrganism() {
	return organism;
    }

    public void setOrganism(String organism) {
	this.organism = organism;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public Assay() {
	super(null);
    }

    public Assay(IIdentifier url) {
	super(url);
    }

    @Override
    public int hashCode() {
	return (getResourceIdentifier() == null) ? "".hashCode() : getResourceIdentifier().toExternalForm().hashCode();
    }

}
