package net.idea.ops.cli.assay;

import java.util.ArrayList;
import java.util.List;

import net.idea.opentox.cli.AbstractURLResource;

public class Pathway extends AbstractURLResource {
    protected String title;
    protected List<String> ontology = new ArrayList<String>();

    public List<String> getOntology() {
	return ontology;
    }

    public void setOntology(List<String> ontology) {
	this.ontology = ontology;
    }

    protected String webpage;

    public String getWebpage() {
	return webpage;
    }

    public void setWebpage(String webpage) {
	this.webpage = webpage;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = -2745448846499704583L;

    @Override
    public String toString() {
	return getTitle();
    }
}
