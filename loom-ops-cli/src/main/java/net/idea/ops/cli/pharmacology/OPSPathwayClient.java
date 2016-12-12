package net.idea.ops.cli.pharmacology;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.opentox.rest.RestException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import net.idea.opentox.cli.id.Identifier;
import net.idea.opentox.cli.structure.Compound;
import net.idea.ops.cli.AbstractOPSClient;
import net.idea.ops.cli.OPSClient;
import net.idea.ops.cli.OPSClient.params;
import net.idea.ops.cli.assay.Pathway;

public class OPSPathwayClient extends AbstractOPSClient<Pathway> {

	public OPSPathwayClient() {
		this(null);
	}

	public OPSPathwayClient(HttpClient httpclient) {
		super(httpclient);
		resource = "/pathways/byCompound";
	}

	public List<Pathway> getTargetInfo(URL queryService, Pathway target)
			throws RestException, IOException {
		Identifier ref = new Identifier(String.format("%s%s", queryService,
				resource));
		return get(ref, mime_json, params.uri.name(), target
				.getResourceIdentifier().toExternalForm(), "_format",
				_format.json.name());
	}

	public Integer getPathwaysByCompoundCount(Compound cmp)
			throws RestException, IOException {
		return getPathwaysByCompoundCount(new URL(server_root), cmp);
	}

	public Integer getPathwaysByCompoundCount(URL queryService, Compound cmp)
			throws RestException, IOException {
		Identifier ref = new Identifier(String.format("%s%s/count",
				queryService, resource));
		return getCount("pathway_count", ref, mime_json, params.uri.name(), cmp
				.getResourceIdentifier().toExternalForm(), "_format",
				_format.json.name());
	}

	public List<Pathway> getPathwaysByCompound(Compound cmp)
			throws RestException, IOException {
		return getPathwaysByCompound(new URL(server_root), cmp, 0, -1);
	}

	public List<Pathway> getPathwaysByCompound(Compound cmp, int page,
			int pagesize) throws RestException, IOException {
		return getPathwaysByCompound(new URL(server_root), cmp, page, pagesize);
	}

	public List<Pathway> getPathwaysByCompound(URL queryService, Compound cmp,
			int page, int pagesize) throws RestException, IOException {
		Identifier ref = new Identifier(String.format("%s%s", queryService,
				resource));
		return get(ref, mime_json, params.uri.name(), cmp
				.getResourceIdentifier().toExternalForm(),
				OPSClient.pagination._page.name(), Integer.toString(page),
				OPSClient.pagination._pageSize.name(),
				(pagesize <= 0) ? "all" : Integer.toString(pagesize),
				"_format", _format.json.name());
	}

	@Override
	protected Pathway parseItem(ObjectMapper m, JsonNode result, JsonNode item)
			throws MalformedURLException {

		JsonNode node = item.get("identifier"); // wikipathway without version
		Pathway pathway = new Pathway();
		if (node != null)
			pathway.setResourceIdentifier(new Identifier(node.textValue()));
		else { // this is wikipathways uri woith verison
			node = item.get("_about");
			pathway.setResourceIdentifier(new Identifier(node.textValue()));
		}

		node = item.get("title_en");
		if (node != null)
			pathway.setTitle(node.textValue());
		else {
			node = item.get("title");
			if (node != null)
				pathway.setTitle(node.textValue());
		}

		node = item.get("page");
		if (node != null)
			pathway.setWebpage(node.textValue());

		node = item.get("pathwayOntology");
		if (node != null && node instanceof ArrayNode)
			for (int i = 0; i < ((ArrayNode) node).size(); i++) {
				JsonNode ontNode = ((ArrayNode) node).get(i);
				if (ontNode != null)
					pathway.getOntology().add(ontNode.textValue());
			}

		return pathway;
	}

}
