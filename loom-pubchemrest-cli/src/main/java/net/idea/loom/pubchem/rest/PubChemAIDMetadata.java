package net.idea.loom.pubchem.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	    String tag = result.get("name").textValue();
	    tags.put(tag, result);
	}

    }

    public JsonNode getTag(String tag) {
	return metadata.get("assay").get("tags").get(tag);
    }

    public String getTitle() {
	return metadata.get("assay").get("descr").get("name").textValue();
    }

    public JsonNode getDoseResponse() {
	return metadata.get("assay").get("descr").get("dr");
    }

    public int getAID() {
	return metadata.get("assay").get("descr").get("aid").get("id").intValue();
    }

    public String getAIDSource_id() {
	return metadata.get("assay").get("descr").get("aid_source").get("db").get("source_id").get("str")
		.textValue();
    }

    public String getAIDSource_name() {
	return metadata.get("assay").get("descr").get("aid_source").get("db").get("name").textValue();
    }

    public String getAIDSource_date() {
	try {
	    JsonNode date = metadata.get("assay").get("descr").get("aid_source").get("db").get("date").get("std");
	    return String.format("%s-%s-%s", date.get("year").textValue(), date.get("month").textValue(), date
		    .get("day").textValue());
	} catch (Exception x) {
	    return null;
	}
    }

    public String getAIDSource_year() {
	try {
	    JsonNode date = metadata.get("assay").get("descr").get("aid_source").get("db").get("date").get("std");
	    return date.get("year").textValue();
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
		b.append(n.get(i).textValue());
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
		b.append(n.get(i).textValue());
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
		b.append(n.get(i).get("name").textValue());
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
		b.append(n.get(i).get("mol_id").intValue());
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
	return metadata.get("assay").get("descr").get("activity_outcome_method").textValue();
    }

    public String getprojectCategory() {
	return metadata.get("assay").get("descr").get("project_category").textValue();
    }

    /**
     * <xref> = xref / {RegistryID | RN | PubMedID | MMDBID | ProteinGI |
     * NucleotideGI | TaxonomyID | MIMID | GeneID | ProbeID | PatentID}
     */
    public ArrayNode getXRef() {
	return (ArrayNode) metadata.get("assay").get("descr").get("xref");
    }

}
