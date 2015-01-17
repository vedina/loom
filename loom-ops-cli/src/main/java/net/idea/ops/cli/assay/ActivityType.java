package net.idea.ops.cli.assay;

import net.idea.opentox.cli.AbstractURLResource;

public class ActivityType extends AbstractURLResource {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8436633064700482394L;
    private String label;

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public ActivityType(String label) {
	super(null);
	setLabel(label);
    }

    @Override
    public int hashCode() {
	return label.hashCode();
    }
}
