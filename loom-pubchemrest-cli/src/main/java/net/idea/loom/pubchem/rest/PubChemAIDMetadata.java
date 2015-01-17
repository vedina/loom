package net.idea.loom.pubchem.rest;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * http://pubchem.ncbi.nlm.nih.gov/pug_rest/PUG_REST.html
 * http://pubchem.ncbi.nlm.nih.gov/rest/pug/assay/aid/{aid}/description/json
 * 
 * @author nina
 * 
 */
public class PubChemAIDMetadata {
    protected JsonNode metadata;

    public PubChemAIDMetadata(JsonNode metadata) {
	this.metadata = metadata;
	refactorResults();
    }

    protected void refactorResults() {
	ObjectNode tags = (ObjectNode) metadata.get("assay").get("tags");
	ArrayNode results = (ArrayNode) metadata.get("assay").get("descr").get("results");
	for (int i = 0; i < results.size(); i++) {
	    JsonNode result = results.get(i);
	    String tag = result.get("name").getTextValue();
	    tags.put(tag, result);
	}

    }

    public JsonNode getTag(String tag) {
	return metadata.get("assay").get("tags").get(tag);
    }

    public String getTitle() {
	return metadata.get("assay").get("descr").get("name").getTextValue();
    }

    public JsonNode getDoseResponse() {
	return metadata.get("assay").get("descr").get("dr");
    }

    public int getAID() {
	return metadata.get("assay").get("descr").get("aid").get("id").getIntValue();
    }

    public String getAIDSource_id() {
	return metadata.get("assay").get("descr").get("aid_source").get("db").get("source_id").get("str")
		.getTextValue();
    }

    public String getAIDSource_name() {
	return metadata.get("assay").get("descr").get("aid_source").get("db").get("name").getTextValue();
    }

    public String getAIDSource_date() {
	try {
	    JsonNode date = metadata.get("assay").get("descr").get("aid_source").get("db").get("date").get("std");
	    return String.format("%s-%s-%s", date.get("year").getTextValue(), date.get("month").getTextValue(), date
		    .get("day").getTextValue());
	} catch (Exception x) {
	    return null;
	}
    }

    public String getAIDSource_year() {
	try {
	    JsonNode date = metadata.get("assay").get("descr").get("aid_source").get("db").get("date").get("std");
	    return date.get("year").getTextValue();
	} catch (Exception x) {
	    return null;
	}
    }

    public String getURI() {
	return String.format("http://pubchem.ncbi.nlm.nih.gov/assay/assay.cgi?aid=%d", getAID());
    }

    public ArrayNode getProtocol() {
	return (ArrayNode) metadata.get("assay").get("descr").get("protocol");
    }

    public String getProtocolAsText() {
	StringBuilder b = null;
	try {
	    ArrayNode n = (ArrayNode) metadata.get("assay").get("descr").get("protocol");
	    for (int i = 0; i < n.size(); i++) {
		if (b == null)
		    b = new StringBuilder();
		else
		    b.append("\n");
		b.append(n.get(i).getTextValue());
	    }
	    return b.toString();
	} catch (Exception x) {
	    return null;
	}
    }

    public ArrayNode getComment() {
	return (ArrayNode) metadata.get("assay").get("descr").get("comment");
    }

    public ArrayNode getDescription() {
	return (ArrayNode) metadata.get("assay").get("descr").get("description");
    }

    public String getDescriptionAsText() {
	StringBuilder b = null;
	try {
	    ArrayNode n = (ArrayNode) metadata.get("assay").get("descr").get("description");
	    for (int i = 0; i < n.size(); i++) {
		if (b == null)
		    b = new StringBuilder();
		else
		    b.append("\n");
		b.append(n.get(i).getTextValue());
	    }
	    return b.toString();
	} catch (Exception x) {
	    return null;
	}

    }

    public ArrayNode getTarget() {
	return (ArrayNode) metadata.get("assay").get("descr").get("target");
    }

    public String getTargetName() {
	StringBuilder b = null;
	try {
	    ArrayNode n = (ArrayNode) metadata.get("assay").get("descr").get("target");
	    if (n == null)
		return null;
	    for (int i = 0; i < n.size(); i++) {
		if (b == null)
		    b = new StringBuilder();
		else
		    b.append("\n");
		b.append(n.get(i).get("name").getTextValue());
	    }
	    return b.toString();
	} catch (Exception x) {
	    return null;
	}
    }

    public String getTargetMolID() {
	StringBuilder b = null;
	try {
	    ArrayNode n = (ArrayNode) metadata.get("assay").get("descr").get("target");
	    if (n == null)
		return null;
	    for (int i = 0; i < n.size(); i++) {
		if (b == null)
		    b = new StringBuilder();
		else
		    b.append("\n");
		b.append(n.get(i).get("mol_id").getIntValue());
	    }
	    return b.toString();
	} catch (Exception x) {
	    return null;
	}
    }

    /**
     * <assay type> = all | confirmatory | doseresponse | onhold | panel | rnai
     * | screening | summary | cellbased | biochemical | invivo | invitro |
     * activeconcentrationspecified
     * 
     * @return
     */
    public String getActivityOutcomeMethod() {
	return metadata.get("assay").get("descr").get("activity_outcome_method").getTextValue();
    }

    public String getprojectCategory() {
	return metadata.get("assay").get("descr").get("project_category").getTextValue();
    }

    /**
     * <xref> = xref / {RegistryID | RN | PubMedID | MMDBID | ProteinGI |
     * NucleotideGI | TaxonomyID | MIMID | GeneID | ProbeID | PatentID}
     */
    public ArrayNode getXRef() {
	return (ArrayNode) metadata.get("assay").get("descr").get("xref");
    }

}
