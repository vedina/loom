package net.idea.ops.cli.pharmacology;

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
			value = onAssay.get("assay_organism");
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
	
}

