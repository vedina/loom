package net.idea.loom.pubchem.rest;

import net.idea.opentox.cli.IIdentifiableResource;

public class PubChemIdentifier implements IIdentifiableResource<String> {

    public PubChemIdentifier(String type, String value) {
	super();
	this.type = type;
	this.value = value;
    }

    protected String type;

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    protected String value;

    @Override
    public void setResourceIdentifier(String identifier) {
	this.value = identifier;
    }

    @Override
    public String getResourceIdentifier() {
	return value;
    }

    @Override
    public String toString() {
	return String.format("%s = %s", type, value);
    }
}
