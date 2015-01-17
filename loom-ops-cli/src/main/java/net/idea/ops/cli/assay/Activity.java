package net.idea.ops.cli.assay;

import java.net.URL;

import net.idea.opentox.cli.AbstractURLResource;

public class Activity extends AbstractURLResource {

    /**
	 * 
	 */
    private static final long serialVersionUID = 8436633064700482394L;
    ActivityType type;

    public ActivityType getType() {
	return type;
    }

    public void setType(ActivityType type) {
	this.type = type;
    }

    public Activity(URL url) {
	super(url);
    }

    @Override
    public int hashCode() {
	return (getResourceIdentifier() == null) ? "".hashCode() : getResourceIdentifier().toExternalForm().hashCode();
    }
}
