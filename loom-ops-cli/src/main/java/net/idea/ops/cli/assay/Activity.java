package net.idea.ops.cli.assay;

import java.net.URL;

import net.idea.opentox.cli.AbstractURLResource;

public class Activity extends AbstractURLResource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8436633064700482394L;
	String activityType;
	
	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
	
	public Activity(URL url) {
		super(url);
	}
	
	@Override
	public int hashCode() {
		return
		(getResourceIdentifier()==null)?"".hashCode():
		getResourceIdentifier().toExternalForm().hashCode();
	}
}
