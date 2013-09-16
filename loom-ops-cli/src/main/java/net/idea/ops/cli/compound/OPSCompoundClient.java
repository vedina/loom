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
	/**
	 * Get compound info - we can use ChemSpider RDF uris!!
	 * curl -v  -X GET "https://beta.openphacts.org/compound?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_format=json"
{
 "format": "linked-data-api",
 "version": "0.2",
 "result": {
  "_about": "https://beta.openphacts.org/compound?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_format=json",
  "definition": "https://beta.openphacts.org/api-config",
  "extendedMetadataVersion": "https://beta.openphacts.org/compound?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_format=json&_metadata=all%2Cviews%2Cformats%2Cexecution%2Cbindings%2Csite",
  "primaryTopic": {
   "_about": "http://rdf.chemspider.com/236",
   "inchi": "InChI=1S/C6H6/c1-2-4-6-5-3-1/h1-6H",
   "inchikey": "UHOVQNZJYSORNB-UHFFFAOYSA-N",
   "smiles": "c1ccccc1",
   "inDataset": "http://www.chemspider.com",
   "hba": 0,
   "hbd": 0,
   "logp": 2.177,
   "psa": 0,
   "ro5_violations": 0,
   "exactMatch": [
    {
     "_about": "http://www.conceptwiki.org/concept/9bc59ebd-9e3a-47b2-a179-547efb6a66b8",
     "inDataset": "http://www.conceptwiki.org",
     "prefLabel_en": "Benzene",
     "prefLabel": "Benzene"
    },
    "http://rdf.chemspider.com/236",
    {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL277500",
     "full_mwt": 78.1118,
     "molform": "C6H6",
     "mw_freebase": 78.1118,
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf"
    }
   ],
   "isPrimaryTopicOf": "https://beta.openphacts.org/compound?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_format=json"
  }
 }
}
	 */
	
	/**
Get compound pharmacology count

	curl -v  -X GET "https://beta.openphacts.org/compound/pharmacology/count?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b"
	
{
 "format": "linked-data-api",
 "version": "0.2",
 "result": {
  "_about": "https://beta.openphacts.org/compound/pharmacology/count?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b",
  "definition": "https://beta.openphacts.org/api-config",
  "extendedMetadataVersion": "https://beta.openphacts.org/compound/pharmacology/count?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_metadata=all%2Cviews%2Cformats%2Cexecution%2Cbindings%2Csite",
  "primaryTopic": {
   "_about": "http://rdf.chemspider.com/236",
   "compoundPharmacologyTotalResults": 30,
   "isPrimaryTopicOf": "https://beta.openphacts.org/compound/pharmacology/count?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b"
  }
 }
}	
	 */
	
	/**
Get compound pharmacology paginated
curl -v  -X GET "https://beta.openphacts.org/compound/pharmacology/pages?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_page=0&_pageSize=5&_format=json"

{
 "format": "linked-data-api",
 "version": "0.2",
 "result": {
  "_about": "https://beta.openphacts.org/compound/pharmacology/pages?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_pageSize=5&_format=json&_page=1",
  "itemsPerPage": 5,
  "startIndex": 0,
  "isPartOf": {
   "_about": "https://beta.openphacts.org/compound/pharmacology/pages?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_pageSize=5&_format=json",
   "hasPart": "https://beta.openphacts.org/compound/pharmacology/pages?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_pageSize=5&_format=json&_page=1",
   "definition": "http://www.openphacts.org/api#compoundPharmacologyListEndpoint",
   "type": "http://purl.org/linked-data/api/vocab#List"
  },
  "modified": "Monday, 16-Sep-13 06:59:44 UTC",
  "extendedMetadataVersion": "https://beta.openphacts.org/compound/pharmacology/pages?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_page=0&_pageSize=5&_format=json&_metadata=all%2Cviews%2Cformats%2Cexecution%2Cbindings%2Csite",
  "items": [
   {
    "_about": "http://data.kasabi.com/dataset/chembl-rdf/activity/a1001513",
    "pmid": "2033592",
    "forMolecule": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL277500",
     "full_mwt": 78.1118,
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
     "exactMatch": [
      {
       "_about": "http://rdf.chemspider.com/236",
       "inchi": "InChI=1S/C6H6/c1-2-4-6-5-3-1/h1-6H",
       "inchikey": "UHOVQNZJYSORNB-UHFFFAOYSA-N",
       "smiles": "c1ccccc1",
       "inDataset": "http://rdf.chemspider.com/",
       "ro5_violations": 0
      },
      {
       "_about": "http://www.conceptwiki.org/concept/9bc59ebd-9e3a-47b2-a179-547efb6a66b8",
       "inDataset": "http://www.conceptwiki.org/",
       "prefLabel_en": "Benzene",
       "prefLabel": "Benzene"
      }
     ]
    },
    "onAssay": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL760870",
     "description": "Toxicity determined using Microtox Test",
     "target": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL612558",
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
     "organism": "Bacteria"
    },
    "relation": "=",
    "standardValue": 2.913,
    "activity_type": "Log EC50",
    "inDataset": "http://data.kasabi.com/dataset/chembl-rdf"
   },
   {
    "_about": "http://data.kasabi.com/dataset/chembl-rdf/activity/a1001514",
    "pmid": "2033592",
    "forMolecule": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL277500",
     "full_mwt": 78.1118,
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
     "exactMatch": [
      {
       "_about": "http://rdf.chemspider.com/236",
       "inchi": "InChI=1S/C6H6/c1-2-4-6-5-3-1/h1-6H",
       "inchikey": "UHOVQNZJYSORNB-UHFFFAOYSA-N",
       "smiles": "c1ccccc1",
       "inDataset": "http://rdf.chemspider.com/",
       "ro5_violations": 0
      },
      {
       "_about": "http://www.conceptwiki.org/concept/9bc59ebd-9e3a-47b2-a179-547efb6a66b8",
       "inDataset": "http://www.conceptwiki.org/",
       "prefLabel_en": "Benzene",
       "prefLabel": "Benzene"
      }
     ]
    },
    "onAssay": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL706713",
     "description": "Toxicity determined using Golden Orfe Fish Test",
     "target": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL612558",
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
     "organism": "Leuciscus idus"
    },
    "relation": "=",
    "standardValue": -0.417,
    "activity_type": "Log LC50",
    "inDataset": "http://data.kasabi.com/dataset/chembl-rdf"
   },
   {
    "_about": "http://data.kasabi.com/dataset/chembl-rdf/activity/a1001515",
    "pmid": "2033592",
    "forMolecule": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL277500",
     "full_mwt": 78.1118,
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
     "exactMatch": [
      {
       "_about": "http://rdf.chemspider.com/236",
       "inchi": "InChI=1S/C6H6/c1-2-4-6-5-3-1/h1-6H",
       "inchikey": "UHOVQNZJYSORNB-UHFFFAOYSA-N",
       "smiles": "c1ccccc1",
       "inDataset": "http://rdf.chemspider.com/",
       "ro5_violations": 0
      },
      {
       "_about": "http://www.conceptwiki.org/concept/9bc59ebd-9e3a-47b2-a179-547efb6a66b8",
       "inDataset": "http://www.conceptwiki.org/",
       "prefLabel_en": "Benzene",
       "prefLabel": "Benzene"
      }
     ]
    },
    "onAssay": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL858647",
     "description": "Toxicity determined using Tadpole Narcosis Test",
     "target": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL612558",
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
     "organism": "frog"
    },
    "relation": "=",
    "standardValue": -2.905,
    "activity_type": "Log C",
    "inDataset": "http://data.kasabi.com/dataset/chembl-rdf"
   },
   {
    "_about": "http://data.kasabi.com/dataset/chembl-rdf/activity/a1001516",
    "pmid": "2033592",
    "forMolecule": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL277500",
     "full_mwt": 78.1118,
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
     "exactMatch": [
      {
       "_about": "http://rdf.chemspider.com/236",
       "inchi": "InChI=1S/C6H6/c1-2-4-6-5-3-1/h1-6H",
       "inchikey": "UHOVQNZJYSORNB-UHFFFAOYSA-N",
       "smiles": "c1ccccc1",
       "inDataset": "http://rdf.chemspider.com/",
       "ro5_violations": 0
      },
      {
       "_about": "http://www.conceptwiki.org/concept/9bc59ebd-9e3a-47b2-a179-547efb6a66b8",
       "inDataset": "http://www.conceptwiki.org/",
       "prefLabel_en": "Benzene",
       "prefLabel": "Benzene"
      }
     ]
    },
    "onAssay": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL857822",
     "description": "The toxicity of compound was determined using Konemann's Industrial Pollutants Toxicity Test",
     "target": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL612558",
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf"
    },
    "relation": "=",
    "standardValue": 2.91,
    "activity_type": "Log LC50",
    "inDataset": "http://data.kasabi.com/dataset/chembl-rdf"
   },
   {
    "_about": "http://data.kasabi.com/dataset/chembl-rdf/activity/a1506326",
    "pmid": "15857133",
    "forMolecule": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL277500",
     "full_mwt": 78.1118,
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
     "exactMatch": [
      {
       "_about": "http://rdf.chemspider.com/236",
       "inchi": "InChI=1S/C6H6/c1-2-4-6-5-3-1/h1-6H",
       "inchikey": "UHOVQNZJYSORNB-UHFFFAOYSA-N",
       "smiles": "c1ccccc1",
       "inDataset": "http://rdf.chemspider.com/",
       "ro5_violations": 0
      },
      {
       "_about": "http://www.conceptwiki.org/concept/9bc59ebd-9e3a-47b2-a179-547efb6a66b8",
       "inDataset": "http://www.conceptwiki.org/",
       "prefLabel_en": "Benzene",
       "prefLabel": "Benzene"
      }
     ]
    },
    "onAssay": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL876466",
     "description": "Lipophilicity determined as logarithm of the partition coefficient in the alkane/water system",
     "target": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL612558",
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf"
    },
    "relation": "=",
    "standardValue": 2.29,
    "activity_type": "Log PNalk",
    "inDataset": "http://data.kasabi.com/dataset/chembl-rdf"
   }
  ],
  "type": "http://purl.org/linked-data/api/vocab#Page",
  "first": "https://beta.openphacts.org/compound/pharmacology/pages?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_pageSize=5&_format=json&_page=1",
  "next": "https://beta.openphacts.org/compound/pharmacology/pages?uri=http%3A%2F%2Frdf.chemspider.com%2F236&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_pageSize=5&_format=json&_page=2",
  "label": "Compound Pharmacology Paginated"
 }
}
	 */
}
