package net.idea.ops.cli.compound;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.opentox.rest.RestException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import net.idea.opentox.cli.id.IIdentifier;
import net.idea.opentox.cli.id.Identifier;
import net.idea.opentox.cli.structure.Compound;
import net.idea.ops.cli.AbstractOPSClient;
import net.idea.ops.cli.AbstractOPSClient._format;
import net.idea.ops.cli.compound.OPSCompoundClient.QueryType;

/**
 * Reads {@link Compound} via OPS Compound API https://dev.openphacts.org/docs
 * 
 * @author nina
 * 
 * @param <POLICY_RULE>
 */
public class OPSCompoundClient extends AbstractOPSClient<Compound> {
	public enum QueryType {
		exact, similarity, substructure
	};

	public enum searchOptions {
		Molecule, MatchType, SimilarityType, Threshold;
		public String key() {
			return "searchOptions." + name();
		}
	};

	public enum commonOptions {
		Complexity, Isotopic, HasSpectra, HasPatents
	};

	public OPSCompoundClient() {
		this(null);
	}

	public OPSCompoundClient(HttpClient httpclient) {
		super(httpclient);
		resource = "/structure";
	}

	/**
	 * /structure/exact
	 * 
	 * @param queryService
	 * @param term
	 * @return
	 * @throws RestException
	 * @throws IOException
	 */
	public List<Compound> searchExactStructures(URL queryService, String term)
			throws RestException, IOException {
		IIdentifier ref = new Identifier(String.format("%s%s/%s", queryService,
				resource, QueryType.exact.name()));
		return get(ref, mime_json, searchOptions.Molecule.key(), term,
				"_format", _format.json.name());
	}

	/**
	 * /structure/similarity
	 * 
	 * @param queryService
	 * @param term
	 * @return
	 * @throws RestException
	 * @throws IOException
	 */
	public List<Compound> searchSimilarStructures(URL queryService,
			String term, double threshold, int limit) throws RestException,
			IOException {
		Identifier ref = new Identifier(String.format("%s%s/%s", queryService,
				resource, QueryType.similarity.name()));
		return get(ref, mime_json, searchOptions.Molecule.key(), term,
				searchOptions.Threshold.key(), Double.toString(threshold),
				"_format", _format.json.name(), resultOptions.Limit.name(),
				Integer.toString(limit));
	}

	public List<Compound> searchSubstructures(URL queryService, String term,
			int limit) throws RestException, IOException {
		Identifier ref = new Identifier(String.format("%s%s/%s", queryService,
				resource, QueryType.substructure.name()));
		return get(ref, mime_json, searchOptions.Molecule.key(), term,
				resultOptions.Limit.name(), Integer.toString(limit), "_format",
				_format.json.name());
	}

	public List<Compound> searchStructuresByInchikey(String term)
			throws RestException, IOException {
		return searchStructuresByInchikey(new URL(server_root), term);
	}

	public List<Compound> searchStructuresByInchikey(URL queryService,
			String term) throws RestException, IOException {
		Identifier ref = new Identifier(String.format("%s%s", queryService,
				resource));
		return get(ref, mime_json, "inchi_key", term, "_format",
				_format.json.name());

	}

	public List<Compound> searchStructuresByInchi(URL queryService, String term)
			throws RestException, IOException {
		Identifier ref = new Identifier(String.format("%s%s", queryService,
				resource));
		return get(ref, mime_json, "inchi", term, "_format",
				_format.json.name());
	}

	public List<Compound> searchStructuresBySMILES(URL queryService, String term)
			throws RestException, IOException {
		Identifier ref = new Identifier(String.format("%s%s", queryService,
				resource));
		return get(ref, mime_json, "smiles", term, "_format",
				_format.json.name());
	}

	/*
	 * 
	 * public List<Compound> getIdentifiers(URL queryService, URL compound)
	 * throws Exception { URL ref = new
	 * URL(String.format("%s/query/compound/url/all?search=%s"
	 * ,queryService,URLEncoder.encode(compound.toExternalForm()))); return
	 * get(ref,mime_json); }
	 * 
	 * public List<Compound> getIdentifiersAndLinks(URL queryService, URL
	 * compound) throws Exception { URL ref = new
	 * URL(String.format("%s/query/compound/url/allnlinks?search=%s"
	 * ,queryService,URLEncoder.encode(compound.toExternalForm()))); return
	 * get(ref,mime_json); }
	 */

	@Override
	public List<Compound> processPayload(InputStream in, String mediaType)
			throws RestException, IOException {
		List<Compound> list = null;
		if (mime_json.equals(mediaType)) {
			ObjectMapper m = new ObjectMapper();
			JsonNode node = m.readTree(in);
			JsonNode format = (JsonNode) node.get("format");
			if (!"linked-data-api".equals(format.textValue()))
				return null;
			JsonNode version = (JsonNode) node.get("version");
			api_version = version == null ? "1.2" : version.textValue();
			Compound compound = null;
			JsonNode result = node.get("result");
			JsonNode primaryTopic = result.get("primaryTopic");
			JsonNode uri = primaryTopic.get("result");
			if (uri instanceof ArrayNode) {
				ArrayNode results = ((ArrayNode) uri);
				list = new ArrayList<Compound>();
				for (int i = 0; i < results.size(); i++) {
					compound = new Compound(new Identifier(results.get(i)
							.textValue()));
					list.add(compound);
				}
			} else {
				if (uri == null)
					uri = primaryTopic.get("_about");
				if (uri != null) {
					compound = new Compound();
					compound.setResourceIdentifier(new Identifier(uri.textValue()));
					list = new ArrayList<Compound>();
					list.add(compound);
				}
				if (primaryTopic.get("Molecule") != null)
					compound.setSMILES(primaryTopic.get("Molecule")
							.textValue());
				if (primaryTopic.get("smiles") != null)
					compound.setSMILES(primaryTopic.get("smiles")
							.textValue());
				if (primaryTopic.get("inchi") != null)
					compound.setInChI(primaryTopic.get("inchi").textValue());
				if (primaryTopic.get("inchi_key") != null)
					compound.setInChIKey(primaryTopic.get("inchi_key")
							.textValue());
			}
			return list;
		} else if (mime_rdfxml.equals(mediaType)) {
			return super.processPayload(in, mediaType);
		} else if (mime_n3.equals(mediaType)) {
			return super.processPayload(in, mediaType);
		} else if (mime_csv.equals(mediaType)) {
			/*
			 * Substance substance = new Substance(); String line = null;
			 * BufferedReader reader = new BufferedReader(new
			 * InputStreamReader(in)); while ((line = reader.readLine())!=null)
			 * { QuotedTokenizer st = new QuotedTokenizer(line,','); while
			 * (st.hasMoreTokens()) header.add(st.nextToken().trim()); break; }
			 * //QuotedTokenizer tokenizer = new QuotedTokenizer(text,
			 * delimiter);
			 */
			return super.processPayload(in, mediaType);
		} else
			return super.processPayload(in, mediaType);
	}

	/*
	 * public List<Compound> getIdentifiers(URL queryService, URL compound)
	 * throws Exception { URL ref = new
	 * URL(String.format("%s/query/compound/url/all?search=%s"
	 * ,queryService,URLEncoder.encode(compound.toExternalForm()))); return
	 * get(ref,mime_json); }
	 */

	@Override
	protected Compound parseItem(ObjectMapper m, JsonNode result, JsonNode item)
			throws MalformedURLException {
		return null;
	}
}
