package net.idea.loom.nm.nanowiki;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import ambit2.base.data.ILiteratureEntry;
import ambit2.base.data.Property;
import ambit2.base.data.StructureRecord;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.substance.ExternalIdentifier;
import ambit2.base.data.substance.SubstanceEndpointsBundle;
import ambit2.base.facet.BundleRoleFacet;
import ambit2.base.interfaces.ICiteable;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.relation.STRUCTURE_RELATION;
import ambit2.base.relation.composition.Proportion;
import ambit2.core.io.IRawReader;

/**
 * @author nina
 * @author egonw
 */
public class ENanoMapperRDFReader extends DefaultIteratingChemObjectReader
		implements IRawReader<IStructureRecord>, ICiteable {

	protected Model rdf;
	protected QueryExecution qe_materials;
	protected ResultSet materials;
	protected SubstanceRecord record;
	protected transient Logger logger;
	public static final Properties substance_types = new Properties();

	protected HashMap<String, BundleRoleFacet> bundles = new HashMap<String, BundleRoleFacet>();

	// private Map<String,Integer> components = new HashMap<>();
	// private Map<String,Integer> endpoints = new HashMap<>();

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger == null ? Logger.getLogger(getClass().getName()) : logger;
	}

	private String prefix = "DEMO";

	public ENanoMapperRDFReader(Reader reader, String prefix) throws CDKException, IOException {
		this(reader, prefix, null);
	}

	public ENanoMapperRDFReader(Reader reader, String prefix, Logger logger) throws CDKException, IOException {
		super();
		this.prefix = prefix;
		setReader(reader);
	}

	@Override
	public void setReader(Reader reader) throws CDKException {
		try {
			rdf = ModelFactory.createDefaultModel();
			rdf.read(reader, "http://ontology.enanomapper.net", "TURTLE");
			readBundles();

			Query query = QueryFactory.create(ENanoMapperSPARQLQueries.m_allmaterials.SPARQL());
			qe_materials = QueryExecutionFactory.create(query, rdf);

			materials = qe_materials.execSelect();
		} catch (IOException x) {
			throw new CDKException("Error while reading the eNanoMapper RDF:" + x.getMessage());
		}
	}

	@Override
	public void setReader(InputStream reader) throws CDKException {
		try {
			setReader(new InputStreamReader(reader, "UTF-8"));
		} catch (CDKException x) {
			throw x;
		} catch (Exception x) {
			throw new CDKException(x.getMessage(), x);
		}

	}

	@Override
	public IResourceFormat getFormat() {
		return null;
	}

	@Override
	public void close() throws IOException {
		if (qe_materials != null)
			qe_materials.close();
		rdf.close();
	}

	@Override
	public boolean hasNext() {
		if (materials == null)
			return false;
		if (materials.hasNext()) {
			QuerySolution qs = materials.next();
			System.out.println("next material: " + qs);
			Resource material = qs.getResource("material");
			record = new SubstanceRecord();
			record.setExternalids(new ArrayList<ExternalIdentifier>());
			try {
				parseMaterial(rdf, material, record);
				return true;
			} catch (IOException x) {
				logger.log(Level.WARNING, x.getMessage());
				record = null;
				return false;
			}
		} else {
			System.out.println("Nothing next");
			record = null;
			return false;
		}
	}

	@Override
	public Object next() {
		if (materials == null)
			return null;
		return record;
	}

	@Override
	public void setReference(ILiteratureEntry reference) {

	}

	@Override
	public ILiteratureEntry getReference() {
		return null;
	}

	@Override
	public IStructureRecord nextRecord() {
		return record;
	}

	private void parseMaterial(Model rdf, RDFNode material, SubstanceRecord record) throws IOException {
		String sparqlQuery = String.format(ENanoMapperSPARQLQueries.m_materialprops.SPARQL(),
				material.asResource().getURI());
		record.setSubstanceUUID(this.prefix + "-"
				+ UUID.nameUUIDFromBytes(material.asResource().getURI().toString().getBytes()).toString());
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query, rdf);
		try {
			ResultSet rs = qe.execSelect();
			QuerySolution solution = rs.next(); // only pick the first. If we
												// have more, the SPARQL is
												// wrong
			if (solution.contains("label")) {
				record.setSubstanceName(solution.get("label").asLiteral().getString());
			}
			if (solution.contains("type")) {
				record.setSubstancetype(solution.get("type").asResource().getLocalName());
			}
			if (solution.contains("owner")) {
				// this is the bundle URI
				BundleRoleFacet bundleFacet = bundles.get(solution.get("owner").asResource().getURI());
				if (bundleFacet != null && bundleFacet.getValue() != null) { // ok,
																				// we
																				// have
																				// more
																				// info
					SubstanceEndpointsBundle bundle = bundleFacet.getValue();
					String ownerName = bundle.getSource();
	
					record.setOwnerName(ownerName);
					record.setOwnerUUID(prefix + "-" + UUID.nameUUIDFromBytes(ownerName.getBytes()).toString());
					record.addFacet(bundleFacet);
				} else {
					String ownerName = solution.get("owner").toString();
					record.setOwnerName(ownerName);
					record.setOwnerUUID(prefix + "-" + UUID.nameUUIDFromBytes(ownerName.getBytes()).toString());
				}
			}
			List<ExternalIdentifier> identifiers = new ArrayList<>();
			if (solution.contains("sameAs")) {
				identifiers.add(new ExternalIdentifier("Same as", solution.get("sameAs").asResource().getURI()));
			}
			if (solution.contains("closeMatch")) {
				identifiers
						.add(new ExternalIdentifier("Close match", solution.get("closeMatch").asResource().getURI()));
			}
			if (solution.contains("relatedMatch")) {
				identifiers.add(
						new ExternalIdentifier("Related match", solution.get("relatedMatch").asResource().getURI()));
			}
			if (solution.contains("seeAlso")) {
				identifiers.add(new ExternalIdentifier("See also", solution.get("seeAlso").asResource().getURI()));
			}
			if (solution.contains("page")) {
				identifiers.add(new ExternalIdentifier("HOMEPAGE", solution.get("page").asResource().getURI()));
			}
			if (identifiers.size() > 0)
				record.setExternalids(identifiers);
		} finally {
			qe.close();
		}
		sparqlQuery = String.format(ENanoMapperSPARQLQueries.m_coating.SPARQL(), material.asResource().getURI());
		query = QueryFactory.create(sparqlQuery);
		qe = QueryExecutionFactory.create(query, rdf);
		try {
			ResultSet rs = qe.execSelect();
			while (rs.hasNext()) {
				QuerySolution solution = rs.next();
				StructureRecord structure = new StructureRecord();
				if (solution.contains("smiles")) {
					String smiles = solution.get("smiles").asLiteral().getValue().toString();
					System.out.println("SMILES: " + smiles);
					structure.setContent(smiles);
					structure.setFormat("INC");
					structure.setSmiles(structure.getContent());
				}
				System.out.println("struct: " + structure);

				if (solution.contains("componentName")) {
					String name = solution.get("componentName").asLiteral().getValue().toString();
					System.out.println("Name: " + name);
					structure.setRecordProperty(Property.getNameInstance(), name);
				}

				STRUCTURE_RELATION relation = STRUCTURE_RELATION.HAS_CONSTITUENT;
				if (solution.get("type") != null) {
					String typeURI = solution.get("type").asResource().getURI();
					if ("http://purl.bioontology.org/ontology/npo#NPO_1888".equals(typeURI)
							|| "http://purl.bioontology.org/ontology/npo#NPO_1617".equals(typeURI)  // core
							|| "http://purl.bioontology.org/ontology/npo#NPO_279".equals(typeURI)   // dendrimer core
							|| "http://purl.bioontology.org/ontology/npo#NPO_1861".equals(typeURI)  // inorganic core
							|| "http://purl.bioontology.org/ontology/npo#NPO_1864".equals(typeURI)  // metal oxide core
							|| "http://purl.bioontology.org/ontology/npo#NPO_1876".equals(typeURI)  // SPION core
							|| "http://purl.bioontology.org/ontology/npo#NPO_1863".equals(typeURI)  // metallic core
							|| "http://purl.bioontology.org/ontology/npo#NPO_1866".equals(typeURI)  // semiconducting core
							|| "http://purl.bioontology.org/ontology/npo#NPO_1865".equals(typeURI)  // SiO2 core
							|| "http://purl.bioontology.org/ontology/npo#NPO_1875".equals(typeURI)  // superparamagnetic core
							|| "http://purl.bioontology.org/ontology/npo#NPO_1860".equals(typeURI)  // organic core
							|| "http://purl.bioontology.org/ontology/npo#NPO_1862".equals(typeURI)  // polymeric core
							|| "http://purl.bioontology.org/ontology/npo#NPO_1617".equals(typeURI)) {
						relation = STRUCTURE_RELATION.HAS_CORE;
					} else if ("http://purl.bioontology.org/ontology/npo#NPO_1367".equals(typeURI)) {
						relation = STRUCTURE_RELATION.HAS_COATING;
					}
				}
				record.addStructureRelation(record.getSubstanceUUID(), structure, relation, new Proportion());
			}
		} finally {
			qe.close();
		}
		sparqlQuery = String.format(ENanoMapperSPARQLQueries.m_sparql.SPARQL(), material.asResource().getURI());
		query = QueryFactory.create(sparqlQuery);
		qe = QueryExecutionFactory.create(query, rdf);
		try {
			ResultSet rs = qe.execSelect();
			while (rs.hasNext()) {
				QuerySolution solution = rs.next();
				String endpoint = "";
				if (solution.contains("label"))
					endpoint = solution.get("label").asLiteral().getValue().toString();
				Protocol protocol = new Protocol(endpoint);
				Protocol._categories category = null;
				String bao = null;
				try {
					if (solution.contains("assayType"))
						bao = solution.get("assayType").asResource().getURI().toString();
					else if (solution.contains("type"))
						bao = solution.get("type").asResource().getURI().toString();

					category = GuessTerms.category(bao);

				} catch (Exception x) {
					System.err.println(x.getMessage());
					category = null;
				}

				// change the default for now
				if (category == null)
					category = Protocol._categories.UNKNOWN_TOXICITY_SECTION;
				protocol.setCategory(category.name());
				protocol.setTopCategory(category.getTopCategory());

				ProtocolApplication<Protocol, IParams, String, IParams, String> papp = new ProtocolApplication<Protocol, IParams, String, IParams, String>(
						protocol);

				papp.setDocumentUUID(this.prefix + "-"
						+ UUID.nameUUIDFromBytes(solution.get("mgroup").asResource().getURI().getBytes()).toString());
				System.out.println("ID: " + papp.getDocumentUUID());
				// set a reference
				if (solution.contains("source")) {
					String doiString = solution.get("source").asResource().getURI();
					// the ui is happier with ful URL....
					/*
					 * if (doiString.startsWith("https://")) doiString =
					 * doiString.substring(8); if
					 * (doiString.startsWith("http://")) doiString =
					 * doiString.substring(7);
					 * 
					 * if (doiString.startsWith("doi.org/")) doiString =
					 * "doiString.substring(8); if
					 * (doiString.startsWith("dx.doi.org/")) doiString =
					 * doiString.substring(11);
					 */
					papp.setReference(doiString);
				}
				// and now the actual measured value

				EffectRecord<String, IParams, String> effect = new EffectRecord<String, IParams, String>();
				effect.setConditions(new Params());
				// e.g. effect.getConditions().put("Species","Fish");

				/*
				 * String effectId =
				 * solution.get("endpoint").asResource().getLocalName(); //
				 * Works until starts putting same local names with different
				 * namespaces in the same document // what is the meaning of
				 * namespace here? there could be endpoint of the same name from
				 * different experiment, //this should be handled by specifying
				 * proper protocol (in the guidance field) int effectInt = 0; if
				 * (!endpoints.containsKey(effectInt)) { effectInt =
				 * endpoints.size() + 1; endpoints.put(effectId, effectInt); }
				 * else { effectInt = endpoints.get(effectId); }
				 * effect.setIdresult(effectInt);
				 */
				effect.setEndpoint(endpoint);
				if (solution.contains("value")) {
					String value = solution.get("value").asLiteral().getString();
					try {
						effect.setLoValue(Double.parseDouble(value));
					} catch (Exception x) {
						effect.setTextValue(value);
					}
				}
				if (solution.contains("range")) {
					String valueLit = solution.get("range").asLiteral().getValue().toString();
					if (valueLit.contains("-")) {
						String[] loHigh = valueLit.split("-");
						try {
							effect.setLoValue(Double.parseDouble(loHigh[0]));
							effect.setUpValue(Double.parseDouble(loHigh[1]));
						} catch (Exception x) {
							effect.setTextValue(valueLit);
						}
					} else if (valueLit.contains("±")) {
						String[] leHigh = valueLit.split("±");
						try {
							effect.setLoValue(Double.parseDouble(leHigh[0]));
							effect.setErrorValue(Double.parseDouble(leHigh[1]));
						} catch (Exception x) {
							effect.setTextValue(valueLit);
						}
					} // else: huh?
				}
				if (solution.contains("unit"))
					effect.setUnit(solution.get("unit").asLiteral().getValue().toString());
				papp.addEffect(effect);
				record.addMeasurement(papp);
				System.out.println("Added the measurement");
			}
		} finally {
			qe.close();
		}
	}

	protected HashMap<String, BundleRoleFacet> readBundles() throws IOException {
		Query query = QueryFactory.create(ENanoMapperSPARQLQueries.bundles_all.SPARQL());
		QueryExecution qe_bundles = QueryExecutionFactory.create(query, rdf);
		try {
			ResultSet results = qe_bundles.execSelect();
			while (results.hasNext()) {
				QuerySolution qs = results.next();
				SubstanceEndpointsBundle bundle = new SubstanceEndpointsBundle();
				bundle.setStatus("published");
				BundleRoleFacet facet = new BundleRoleFacet(null);
				facet.setValue(bundle);
				String bundle_uri = qs.get("b").asResource().getURI();
				bundle.setURL(bundle_uri);
				if (qs.contains("label"))
					bundle.setName(qs.get("label").asLiteral().getString());
				if (qs.contains("description"))
					bundle.setDescription(qs.get("description").asLiteral().getString());
				if (qs.contains("publisher"))
					bundle.setSource(qs.get("publisher").asLiteral().getString());
				if (qs.contains("license"))
					bundle.setLicenseURI(qs.get("license").asResource().getURI().toString());
				bundles.put(bundle_uri, facet);
			}
			return bundles;
		} catch (Exception exception) {
			return bundles;
		} finally {
			qe_bundles.close();
		}
	}
}

class GuessTerms {
	private GuessTerms() {

	}

	public static Protocol._categories category(String endpoint) throws Exception {
		//this is really not right, we should not be geting result outcomes as diameter and LC50 as assay types
		//Diameter , size etc should be PC_GRANULOMETRY_SECTION
		//Surface area should be SPECIFIC_SURFACE_AREA_SECTION
		//Fish toxicity should be EC_FISHTOX_SECTION
		//Daphnia tox should be EC_DAPHNIATOX_SECTION
		//Cell viability should be ENM_0000068_SECTION
		
		String categorystring = endpoint.replace("http://www.bioassayontology.org/bao#", "").replace("http://purl.bioontology.org/ontology/npo#","");
		try {
			return Protocol._categories.valueOf(categorystring+ "_SECTION");
		} catch (Exception x) {
			// BAO_0002145 LC50 definition is not exactly correct, in case of animal toxicity its 50% of population, and BAO def is about treated cells  
			if ("BAO_0002145".equals(categorystring)) 
				return Protocol._categories.UNKNOWN_TOXICITY_SECTION; 
			//EC%0
			if ("BAO_0000188".equals(categorystring)) 
				return Protocol._categories.UNKNOWN_TOXICITY_SECTION;
			//NOEC
			if ("ENM_0000060".equals(categorystring))
				return Protocol._categories.UNKNOWN_TOXICITY_SECTION;
			if ("BAO_0000190".equals(categorystring))
				return Protocol._categories.UNKNOWN_TOXICITY_SECTION;
			//tbchecked
			if ("NPO_1302".equals(categorystring))
				return Protocol._categories.ZETA_POTENTIAL_SECTION;
			//BAO_0001235
			//can't find what is BAO_0000190 is, but looks like TOX
			System.err.print(String.format("%s\t%s", x.getMessage(),endpoint));
			return null;
		}
	}
}
