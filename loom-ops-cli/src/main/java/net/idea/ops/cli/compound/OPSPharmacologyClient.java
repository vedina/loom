package net.idea.ops.cli.compound;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.idea.opentox.cli.structure.Compound;
import net.idea.ops.cli.AbstractOPSClient;
import net.idea.ops.cli.OPSClient.pagination;
import net.idea.ops.cli.OPSClient.params;
import net.idea.ops.cli.assay.Activity;
import net.idea.ops.cli.assay.Assay;
import net.idea.ops.cli.assay.AssayResult;
import net.idea.ops.cli.assay.Target;
import net.idea.ops.cli.lookup.ActivityLookup;
import net.idea.ops.cli.lookup.ActivityTypeLookup;
import net.idea.ops.cli.lookup.AssayLookup;
import net.idea.ops.cli.lookup.CitationsLookup;
import net.idea.ops.cli.lookup.CompoundLookup;
import net.idea.ops.cli.lookup.DatasetLookup;
import net.idea.ops.cli.lookup.TargetLookup;

import org.apache.http.client.HttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.opentox.rest.RestException;

public class OPSPharmacologyClient extends AbstractOPSClient<AssayResult> {
	protected TargetLookup targets = new TargetLookup();
	protected AssayLookup assays = new AssayLookup();
	protected ActivityLookup activities = new ActivityLookup();
	protected ActivityTypeLookup activityTypes = new ActivityTypeLookup();
	public ActivityTypeLookup getActivityTypes() {
		return activityTypes;
	}

	protected CitationsLookup citations = new CitationsLookup();
	protected CompoundLookup compounds = new CompoundLookup();
	protected DatasetLookup datasets = new DatasetLookup();
	
	public CitationsLookup getCitations() {
		return citations;
	}

	public void setCitations(CitationsLookup citations) {
		this.citations = citations;
	}

	public TargetLookup getTargets() {
		return targets;
	}

	public AssayLookup getAssays() {
		return assays;
	}

	public ActivityLookup getActivities() {
		return activities;
	}

	
	public CompoundLookup getCompounds() {
		return compounds;
	}

	public OPSPharmacologyClient() {
		this(null);
	}
		
	public OPSPharmacologyClient(HttpClient httpclient) {
		super(httpclient);
		resource = "/pharmacology";
	}

	public List<AssayResult> getCompoundPharmacology(URL queryService,Compound cmp,int page,int pagesize) throws RestException,IOException {
		URL ref = new URL(String.format("%s/compound%s/pages",queryService,resource));
		return get(ref,mime_json,
				params.uri.name(),cmp.getResourceIdentifier().toExternalForm(),
				pagination._page.name(),Integer.toString(page),
				pagination._pageSize.name(),Integer.toString(pagesize),
				"_format",_format.json.name());
	}	
	
	public List<AssayResult> getTargetPharmacology(URL queryService,Target target,int page,int pagesize) throws RestException,IOException {
		URL ref = new URL(String.format("%s/target%s/pages",queryService,resource));
		return get(ref,mime_json,
				params.uri.name(),target.getResourceIdentifier().toExternalForm(),
				pagination._page.name(),Integer.toString(page),
				pagination._pageSize.name(),Integer.toString(pagesize),
				"_orderBy","?compound_chembl",
				"_format",_format.json.name());
	}	
	

	@Override
	protected List<AssayResult> processPayload(InputStream in, String mediaType)
			throws RestException, IOException {
		List<AssayResult> list = null;
		if (mime_json.equals(mediaType)) {
			 ObjectMapper m = new ObjectMapper();
			 JsonNode node = m.readTree(in);
			 JsonNode format = (JsonNode)node.get("format");
			 if (!"linked-data-api".equals(format.getTextValue())) return null;
			 JsonNode result = node.get("result");
			 JsonNode uri = result.get("items");
			 if (uri instanceof ArrayNode) {
				 ArrayNode items = ((ArrayNode) uri);
				 list = new ArrayList<AssayResult>();
				 for (int i=0; i < items.size(); i ++) {
					 list.add(parsePharmacologyItem(m,result,items.get(i)));
				 }
			 } 
			 return list;
		} else return super.processPayload(in, mediaType);
	}	
	
	protected AssayResult parsePharmacologyItem(ObjectMapper m,JsonNode result,JsonNode item) throws MalformedURLException {
		
		Activity activity;
		JsonNode node = item.get("_about");
		if (node !=null && (node.getTextValue()!=null)) {
			activity = activities.lookup(node.getTextValue());
			node = item.get("activity_type");
			if (node!=null && (node.getTextValue()!=null)) {
				activity.setType(activityTypes.lookup(node.getTextValue()));
			}
		} else activity = null;
		//Results themselves
		AssayResult readout = new AssayResult();
		readout.setActivity(activity);
		node = item.get("pmid");
		if (node!=null && (node.getTextValue()!=null)) 
			readout.setCitation(citations.lookup(node.getTextValue()));
		node = item.get("relation");
		if (node!=null && (node.getTextValue()!=null)) readout.setRelation(node.getTextValue());
		node = item.get("standardValue");
		if (node!=null) readout.setStandardValue(node.asDouble());
		node = item.get("standardUnits");
		if (node!=null && (node.getTextValue()!=null)) readout.setStandardUnits(node.getTextValue());
		node = item.get("activity_value");
		if (node!=null) readout.setActivityValue(node.asDouble());
		node = item.get("inDataset");
		if (node!=null) readout.setInDataset(datasets.lookup(node.getTextValue()));			
		
		//Compound
		Compound compound = null;
		JsonNode forMolecule = item.get("forMolecule");
		/*
		 * we prefer the concept wiki URI, but will take anything else as afallback
		 */
		JsonNode uri = forMolecule.get("_about");
		if (uri!=null && uri.getTextValue()!=null) 
			compound = 	compounds.lookup(uri.getTextValue());
		
		//compound details
		JsonNode exactMatch = forMolecule.get("exactMatch");
		if (exactMatch instanceof ArrayNode) {
			for (int i=0; i <((ArrayNode) exactMatch).size(); i++) {
				node = ((ArrayNode) exactMatch).get(i);
				uri = node.get("_about");
				if (uri!=null && uri.getTextValue()!=null) {
					if (compound==null) compound = compounds.lookup(uri.getTextValue());
					else compound.setResourceIdentifier(new URL(uri.getTextValue()));
					compounds.put(uri.getTextValue(),compound);
				}
				JsonNode prefLabel = node.get("prefLabel");
				if (prefLabel!=null) compound.setName(prefLabel.getTextValue());
				prefLabel = node.get("prefLabel_en");
				if (prefLabel!=null) compound.setName(prefLabel.getTextValue());
				JsonNode inchi = node.get("inchi");
				if (inchi!=null) compound.setInChI(inchi.getTextValue());
				JsonNode inchikey = node.get("inchikey");
				if (inchikey!=null) compound.setInChIKey(inchikey.getTextValue());
				JsonNode smiles = node.get("smiles");
				if (smiles!=null) compound.setSMILES(smiles.getTextValue());
			}
		}
		if (forMolecule.get("full_mwt")!=null)
			compound.getProperties().put("full_mwt",forMolecule.get("full_mwt").asText());
		readout.setCompound(compound);
		//assay details
		JsonNode onAssay = item.get("onAssay");
		uri = onAssay.get("_about");
		if (uri!=null && uri.getTextValue()!=null) {
			//assay
			Assay assay = assays.lookup(uri.getTextValue());
			JsonNode value = onAssay.get("description");
			if (value!=null) assay.setDescription(value.getTextValue());
			value = onAssay.get("organism");
			if (value!=null) assay.setOrganism(value.getTextValue());
			//target
			value = onAssay.get("target");
			if (value!=null) {
				if (value instanceof ObjectNode) {
					uri = ((ObjectNode)value).get("_about");
					if (uri!=null && uri.getTextValue()!=null) {
						assay.setTarget(parseTarget(((ObjectNode)value)));
					}
				} else if (value.getTextValue()!=null) {
					Target target = targets.lookup(value.getTextValue());
					assay.setTarget(target);
				}
			}
			readout.setAssay(assay);
		}
		
		return readout;
	}
	
	protected Target parseTarget(ObjectNode node) throws MalformedURLException {
		Target target = null;
		JsonNode uri = ((ObjectNode)node).get("_about");
		if (uri!=null && uri.getTextValue()!=null) {
			JsonNode match = ((ObjectNode)node).get("exactMatch");
			if (match!=null && (match instanceof ObjectNode)) {
				JsonNode matchedURI = ((ObjectNode)match).get("_about");
				if (matchedURI!=null && (matchedURI.getTextValue()!=null)) {
					target = targets.lookup(matchedURI.getTextValue());
					JsonNode label = ((ObjectNode)match).get("prefLabel");
					if (label!=null) target.setPrefLabel(label.getTextValue());
					label = ((ObjectNode)match).get("prefLabel_en");
					if (label!=null) target.setPrefLabelEN(label.getTextValue());	
					
					label = ((ObjectNode)match).get("inDataset");
					if (label!=null) target.setInDataset(datasets.lookup(label.getTextValue()));
				}
			}
			//put another pointer to the target
			if (target!=null) targets.put(uri.getTextValue(), target);
			else target = targets.lookup(uri.getTextValue());
			
			uri = ((ObjectNode)node).get("title");
			if (uri!=null) target.setTitle(uri.getTextValue());
			

			return target;
		} else return null;
	}
	/**
	 * 
	 * curl -v  -X GET "https://beta.openphacts.org/target/pharmacology/pages?uri=http%3A%2F%2Fwww.conceptwiki.org%2Fconcept%2Fcb62ca8a-3939-443b-ab77-9a8d45820d2c&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_page=0&_pageSize=3&_format=json"
	 * 
{
 "format": "linked-data-api",
 "version": "0.2",
 "result": {
  "_about": "https://beta.openphacts.org/target/pharmacology/pages?uri=http%3A%2F%2Fwww.conceptwiki.org%2Fconcept%2Fcb62ca8a-3939-443b-ab77-9a8d45820d2c&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_pageSize=3&_format=json&_page=1",
  "itemsPerPage": 3,
  "startIndex": 0,
  "isPartOf": {
   "_about": "https://beta.openphacts.org/target/pharmacology/pages?uri=http%3A%2F%2Fwww.conceptwiki.org%2Fconcept%2Fcb62ca8a-3939-443b-ab77-9a8d45820d2c&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_pageSize=3&_format=json",
   "hasPart": "https://beta.openphacts.org/target/pharmacology/pages?uri=http%3A%2F%2Fwww.conceptwiki.org%2Fconcept%2Fcb62ca8a-3939-443b-ab77-9a8d45820d2c&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_pageSize=3&_format=json&_page=1",
   "definition": "http://www.openphacts.org/api#targetPharmacologyListEndpoint",
   "type": "http://purl.org/linked-data/api/vocab#List"
  },
  "modified": "Monday, 16-Sep-13 07:10:52 UTC",
  "extendedMetadataVersion": "https://beta.openphacts.org/target/pharmacology/pages?uri=http%3A%2F%2Fwww.conceptwiki.org%2Fconcept%2Fcb62ca8a-3939-443b-ab77-9a8d45820d2c&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_page=0&_pageSize=3&_format=json&_metadata=all%2Cviews%2Cformats%2Cexecution%2Cbindings%2Csite",
  "items": [
   {
    "_about": "http://data.kasabi.com/dataset/chembl-rdf/activity/a1410289",
    "pmid": "15658870",
    "forMolecule": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL435507",
     "full_mwt": 295.332,
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
     "exactMatch": [
      {
       "_about": "http://www.conceptwiki.org/concept/5852e21b-b907-47a1-9a4f-cb476b80fd68",
       "inDataset": "http://www.conceptwiki.org/",
       "prefLabel_en": "2-(2-methylmorpholin-4-yl)benzo(h)chromen-4-one",
       "prefLabel": "2-(2-methylmorpholin-4-yl)benzo(h)chromen-4-one"
      },
      {
       "_about": "http://rdf.chemspider.com/8192962",
       "inchi": "InChI=1S/C18H17NO3/c1-12-11-19(8-9-21-12)17-10-16(20)15-7-6-13-4-2-3-5-14(13)18(15)22-17/h2-7,10,12H,8-9,11H2,1H3",
       "inchikey": "RFWCIJWQGHFULK-UHFFFAOYSA-N",
       "smiles": "O=C2\\C=C(/Oc1c3c(ccc12)cccc3)N4CC(OCC4)C",
       "inDataset": "http://rdf.chemspider.com/",
       "ro5_violations": 0
      }
     ]
    },
    "onAssay": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL832925",
     "description": "Inhibition of ATM kinase using rabbit polyclonal antisera; No inhibition observed at 100 uM",
     "target": {
      "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL3797",
      "title": "Serine-protein kinase ATM",
      "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
      "target_organism": "Homo sapiens",
      "exactMatch": {
       "_about": "http://www.conceptwiki.org/concept/cb62ca8a-3939-443b-ab77-9a8d45820d2c",
       "inDataset": "http://www.conceptwiki.org/",
       "prefLabel_en": "ATM (Homo sapiens)",
       "prefLabel": "ATM (Homo sapiens)"
      }
     },
     "assay_organism": "Oryctolagus cuniculus",
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf"
    },
    "relation": ">",
    "standardUnits": "nM",
    "standardValue": 100000,
    "activity_type": "IC50",
    "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
    "activity_value": 100000
   },
   {
    "_about": "http://data.kasabi.com/dataset/chembl-rdf/activity/a1412179",
    "pmid": "15658870",
    "forMolecule": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL98350",
     "full_mwt": 307.343,
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
     "exactMatch": [
      {
       "_about": "http://www.conceptwiki.org/concept/9e422415-fedb-4cbd-ab44-abe7d424c341",
       "inDataset": "http://www.conceptwiki.org/",
       "prefLabel_en": "LY-294,002",
       "prefLabel": "LY-294,002"
      },
      {
       "_about": "http://rdf.chemspider.com/3835",
       "inchi": "InChI=1S/C19H17NO3/c21-17-13-18(20-9-11-22-12-10-20)23-19-15(7-4-8-16(17)19)14-5-2-1-3-6-14/h1-8,13H,9-12H2",
       "inchikey": "CZQHHVNHHHRRDU-UHFFFAOYSA-N",
       "smiles": "O=C1\\C=C(/Oc2c1cccc2c3ccccc3)N4CCOCC4",
       "inDataset": "http://rdf.chemspider.com/",
       "ro5_violations": 0
      }
     ]
    },
    "onAssay": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL828043",
     "description": "Inhibition of ATM kinase using rabbit polyclonal antisera",
     "target": {
      "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL3797",
      "title": "Serine-protein kinase ATM",
      "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
      "target_organism": "Homo sapiens",
      "exactMatch": {
       "_about": "http://www.conceptwiki.org/concept/cb62ca8a-3939-443b-ab77-9a8d45820d2c",
       "inDataset": "http://www.conceptwiki.org/",
       "prefLabel_en": "ATM (Homo sapiens)",
       "prefLabel": "ATM (Homo sapiens)"
      }
     },
     "assay_organism": "Oryctolagus cuniculus",
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf"
    },
    "relation": ">",
    "standardUnits": "nM",
    "standardValue": 100000,
    "activity_type": "IC50",
    "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
    "activity_value": 100000
   },
   {
    "_about": "http://data.kasabi.com/dataset/chembl-rdf/activity/a1412286",
    "pmid": "15658870",
    "forMolecule": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL104468",
     "full_mwt": 281.306,
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
     "exactMatch": [
      {
       "_about": "http://www.conceptwiki.org/concept/71bcdaba-d690-4904-b8eb-7dd61b615269",
       "inDataset": "http://www.conceptwiki.org/",
       "prefLabel_en": "2-(morpholin-4-yl)benzo(h)chromen-4-one",
       "prefLabel": "2-(morpholin-4-yl)benzo(h)chromen-4-one"
      },
      {
       "_about": "http://rdf.chemspider.com/8036228",
       "inchi": "InChI=1S/C17H15NO3/c19-15-11-16(18-7-9-20-10-8-18)21-17-13-4-2-1-3-12(13)5-6-14(15)17/h1-6,11H,7-10H2",
       "inchikey": "KKTZALUTXUZPSN-UHFFFAOYSA-N",
       "smiles": "O=C2\\C=C(/Oc1c3c(ccc12)cccc3)N4CCOCC4",
       "inDataset": "http://rdf.chemspider.com/",
       "ro5_violations": 0
      }
     ]
    },
    "onAssay": {
     "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL832925",
     "description": "Inhibition of ATM kinase using rabbit polyclonal antisera; No inhibition observed at 100 uM",
     "target": {
      "_about": "http://data.kasabi.com/dataset/chembl-rdf/chemblid/CHEMBL3797",
      "title": "Serine-protein kinase ATM",
      "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
      "target_organism": "Homo sapiens",
      "exactMatch": {
       "_about": "http://www.conceptwiki.org/concept/cb62ca8a-3939-443b-ab77-9a8d45820d2c",
       "inDataset": "http://www.conceptwiki.org/",
       "prefLabel_en": "ATM (Homo sapiens)",
       "prefLabel": "ATM (Homo sapiens)"
      }
     },
     "assay_organism": "Oryctolagus cuniculus",
     "inDataset": "http://data.kasabi.com/dataset/chembl-rdf"
    },
    "relation": ">",
    "standardUnits": "nM",
    "standardValue": 100000,
    "activity_type": "IC50",
    "inDataset": "http://data.kasabi.com/dataset/chembl-rdf",
    "activity_value": 100000
   }
  ],
  "type": "http://purl.org/linked-data/api/vocab#Page",
  "first": "https://beta.openphacts.org/target/pharmacology/pages?uri=http%3A%2F%2Fwww.conceptwiki.org%2Fconcept%2Fcb62ca8a-3939-443b-ab77-9a8d45820d2c&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_pageSize=3&_format=json&_page=1",
  "next": "https://beta.openphacts.org/target/pharmacology/pages?uri=http%3A%2F%2Fwww.conceptwiki.org%2Fconcept%2Fcb62ca8a-3939-443b-ab77-9a8d45820d2c&app_id=3ea7e54f&app_key=c90a01325a209dab09cd7cddfbbd874b&_pageSize=3&_format=json&_page=2",
  "label": "Target Pharmacology Paginated"
 }
}

	 */
}

