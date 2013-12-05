package net.idea.ops.cli.compound;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.idea.opentox.cli.structure.Compound;
import net.idea.ops.cli.AbstractOPSClient;

import org.apache.http.client.HttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.opentox.rest.RestException;

/**
 * Reads {@link Compound} via OPS Compound API
 * https://dev.openphacts.org/docs
 * @author nina
 *
 * @param <POLICY_RULE>
 */
public class OPSCompoundClient extends AbstractOPSClient<Compound> {
	public enum QueryType  {
		exact,
		similarity,
		substructure
	};
	public enum searchOptions {
		Molecule,
		MatchType,
		SimilarityType,
		Threshold;
		public String key() {
			return "searchOptions."+name();
		}
	};
	public enum commonOptions {
		Complexity,
		Isotopic,
		HasSpectra,
		HasPatents
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
	 * @param queryService
	 * @param term
	 * @return
	 * @throws RestException
	 * @throws IOException
	 */
	public List<Compound> searchExactStructures(URL queryService, String term) throws RestException,IOException {
		URL ref = new URL(String.format("%s%s/%s",queryService,resource,QueryType.exact.name()));
		return get(ref,mime_json,
				searchOptions.Molecule.key(),term,
				"_format",_format.json.name());
	}	
	/**
	 * /structure/similarity
	 * @param queryService
	 * @param term
	 * @return
	 * @throws RestException
	 * @throws IOException
	 */
	public List<Compound> searchSimilarStructures(URL queryService, String term, double threshold,int limit) throws RestException,IOException {
		URL ref = new URL(String.format("%s%s/%s",queryService,resource,QueryType.similarity.name()));
		return get(ref,mime_json,
					searchOptions.Molecule.key(),term,
					searchOptions.Threshold.key(),Double.toString(threshold),
					"_format",_format.json.name(),
					resultOptions.Limit.name(),Integer.toString(limit));
	}	
	
	public List<Compound> searchSubstructures(URL queryService, String term,int limit ) throws RestException,IOException {
		URL ref = new URL(String.format("%s%s/%s",queryService,resource,QueryType.substructure.name()));
		return get(ref,mime_json,
					searchOptions.Molecule.key(),term,
					resultOptions.Limit.name(),Integer.toString(limit),
					"_format",_format.json.name());
	}
	
	public List<Compound> searchStructuresByInchikey(URL queryService, String term) throws RestException,IOException {
		URL ref = new URL(String.format("%s%s",queryService,resource));
		return get(ref,mime_json,
				"inchi_key",term,
				"_format",_format.json.name());

	}
	public List<Compound> searchStructuresByInchi(URL queryService, String term) throws RestException,IOException {
		URL ref = new URL(String.format("%s%s",queryService,resource));
		return get(ref,mime_json,
				"inchi",term,
				"_format",_format.json.name());
	}
	public List<Compound> searchStructuresBySMILES(URL queryService, String term) throws RestException,IOException {
		URL ref = new URL(String.format("%s%s",queryService,resource));
		return get(ref,mime_json,
				"smiles",term,
				"_format",_format.json.name());
	}

	/*

	public List<Compound> getIdentifiers(URL queryService, URL compound) throws Exception {
		URL ref = new URL(String.format("%s/query/compound/url/all?search=%s",queryService,URLEncoder.encode(compound.toExternalForm())));
		return get(ref,mime_json);
	}
	
	public List<Compound> getIdentifiersAndLinks(URL queryService, URL compound) throws Exception {
		URL ref = new URL(String.format("%s/query/compound/url/allnlinks?search=%s",queryService,URLEncoder.encode(compound.toExternalForm())));
		return get(ref,mime_json);
	}

	*/
	

	@Override
	protected List<Compound> processPayload(InputStream in, String mediaType)
			throws RestException, IOException {
		List<Compound> list = null;
		if (mime_json.equals(mediaType)) {
			 ObjectMapper m = new ObjectMapper();
			 JsonNode node = m.readTree(in);
			 JsonNode format = (JsonNode)node.get("format");
			 if (!"linked-data-api".equals(format.getTextValue())) return null;
			 JsonNode version = (JsonNode)node.get("version");
			 api_version = version==null?"1.2":version.getTextValue();			 
			 Compound compound = null;
			 JsonNode result = node.get("result");
			 JsonNode primaryTopic = result.get("primaryTopic");
			 JsonNode uri = primaryTopic.get("result");
			 if (uri instanceof ArrayNode) {
				 ArrayNode results = ((ArrayNode) uri);
				 list = new ArrayList<Compound>();
				 for (int i=0; i < results.size(); i ++) {
					 compound = new Compound(new URL(results.get(i).getTextValue()));
					 list.add(compound);
				 }
			 } else {
				 if (uri==null) uri = primaryTopic.get("_about");
				 if (uri != null) {
					 compound = new Compound();
					 compound.setResourceIdentifier(new URL(uri.getTextValue()));
					 list = new ArrayList<Compound>();
					 list.add(compound);
				 }
				 if (primaryTopic.get("Molecule")!=null) compound.setSMILES(primaryTopic.get("Molecule").getTextValue());
				 if (primaryTopic.get("smiles")!=null) compound.setSMILES(primaryTopic.get("smiles").getTextValue());
				 if (primaryTopic.get("inchi")!=null) compound.setInChI(primaryTopic.get("inchi").getTextValue());
				 if (primaryTopic.get("inchi_key")!=null) compound.setInChIKey(primaryTopic.get("inchi_key").getTextValue());
			 }
			 return list;
		} else if (mime_rdfxml.equals(mediaType)) {
			return super.processPayload(in, mediaType);
		} else if (mime_n3.equals(mediaType)) {
			return super.processPayload(in, mediaType);
		} else if (mime_csv.equals(mediaType)) {
			/*
			Substance substance = new Substance();
			String line = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while ((line = reader.readLine())!=null) {
				QuotedTokenizer st = new QuotedTokenizer(line,',');
				while (st.hasMoreTokens()) header.add(st.nextToken().trim());
				break;
			}
			//QuotedTokenizer tokenizer = new QuotedTokenizer(text, delimiter);
			 */
			return super.processPayload(in, mediaType);
		} else return super.processPayload(in, mediaType);
	}
	/*	
	public List<Compound> getIdentifiers(URL queryService, URL compound) throws Exception {
		URL ref = new URL(String.format("%s/query/compound/url/all?search=%s",queryService,URLEncoder.encode(compound.toExternalForm())));
		return get(ref,mime_json);
	}
	*/

}
