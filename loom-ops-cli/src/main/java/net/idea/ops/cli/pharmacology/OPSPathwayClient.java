package net.idea.ops.cli.pharmacology;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import net.idea.opentox.cli.structure.Compound;
import net.idea.ops.cli.AbstractOPSClient;
import net.idea.ops.cli.OPSClient.pagination;
import net.idea.ops.cli.OPSClient.params;
import net.idea.ops.cli.assay.Pathway;

import org.apache.http.client.HttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.opentox.rest.RestException;

public class OPSPathwayClient extends AbstractOPSClient<Pathway> {

    public OPSPathwayClient() {
	this(null);
    }

    public OPSPathwayClient(HttpClient httpclient) {
	super(httpclient);
	resource = "/pathways/byCompound";
    }

    public List<Pathway> getTargetInfo(URL queryService, Pathway target) throws RestException, IOException {
	URL ref = new URL(String.format("%s%s", queryService, resource));
	return get(ref, mime_json, params.uri.name(), target.getResourceIdentifier().toExternalForm(), "_format",
		_format.json.name());
    }

    public Integer getPathwaysByCompoundCount(Compound cmp) throws RestException, IOException {
	return getPathwaysByCompoundCount(new URL(server_root), cmp);
    }

    public Integer getPathwaysByCompoundCount(URL queryService, Compound cmp) throws RestException, IOException {
	URL ref = new URL(String.format("%s%s/count", queryService, resource));
	return getCount("pathway_count", ref, mime_json, params.uri.name(), cmp.getResourceIdentifier()
		.toExternalForm(), "_format", _format.json.name());
    }

    public List<Pathway> getPathwaysByCompound(Compound cmp) throws RestException, IOException {
	return getPathwaysByCompound(new URL(server_root), cmp, 0, -1);
    }

    public List<Pathway> getPathwaysByCompound(Compound cmp, int page, int pagesize) throws RestException, IOException {
	return getPathwaysByCompound(new URL(server_root), cmp, page, pagesize);
    }

    public List<Pathway> getPathwaysByCompound(URL queryService, Compound cmp, int page, int pagesize)
	    throws RestException, IOException {
	URL ref = new URL(String.format("%s%s", queryService, resource));
	return get(ref, mime_json, params.uri.name(), cmp.getResourceIdentifier().toExternalForm(),
		pagination._page.name(), Integer.toString(page), pagination._pageSize.name(), (pagesize <= 0) ? "all"
			: Integer.toString(pagesize), "_format", _format.json.name());
    }

    @Override
    protected Pathway parseItem(ObjectMapper m, JsonNode result, JsonNode item) throws MalformedURLException {

	JsonNode node = item.get("identifier"); // wikipathway without version
	Pathway pathway = new Pathway();
	if (node != null)
	    pathway.setResourceIdentifier(new URL(node.getTextValue()));
	else { // this is wikipathways uri woith verison
	    node = item.get("_about");
	    pathway.setResourceIdentifier(new URL(node.getTextValue()));
	}

	node = item.get("title_en");
	if (node != null)
	    pathway.setTitle(node.getTextValue());
	else {
	    node = item.get("title");
	    if (node != null)
		pathway.setTitle(node.getTextValue());
	}

	node = item.get("page");
	if (node != null)
	    pathway.setWebpage(node.getTextValue());

	node = item.get("pathwayOntology");
	if (node != null && node instanceof ArrayNode)
	    for (int i = 0; i < ((ArrayNode) node).size(); i++) {
		JsonNode ontNode = ((ArrayNode) node).get(i);
		if (ontNode != null)
		    pathway.getOntology().add(ontNode.getTextValue());
	    }

	return pathway;
    }

}
