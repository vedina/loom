package net.idea.loom.nm.nanowiki;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.idea.i5.io.I5CONSTANTS;
import net.idea.i5.io.I5_ROOT_OBJECTS;

import org.junit.Assert;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.io.formats.IResourceFormat;
import org.openscience.cdk.io.iterator.DefaultIteratingChemObjectReader;

import ambit2.base.data.ILiteratureEntry;
import ambit2.base.data.LiteratureEntry;
import ambit2.base.data.Property;
import ambit2.base.data.StructureRecord;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Params;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.study.Value;
import ambit2.base.data.substance.ExternalIdentifier;
import ambit2.base.data.substance.ParticleTypes;
import ambit2.base.interfaces.ICiteable;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.relation.STRUCTURE_RELATION;
import ambit2.base.relation.composition.Proportion;
import ambit2.core.io.IRawReader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * TODO bundles
 * http://127.0.0.1/mediawiki/index.php/Special:URIResolver/Property
 * -3AHas_Bundle IDs (go to external ids) COD
 * http://127.0.0.1/mediawiki/index.php
 * /Special:URIResolver/Property-3AHas_COD_ID Former JRC
 * http://127.0.0.1/mediawiki/index.php/Special:URIResolver/Property-3AFormerly
 * 
 * @author nina
 * 
 */
public class NanoWikiRDFReader extends DefaultIteratingChemObjectReader
		implements IRawReader<IStructureRecord>, ICiteable {

	protected Model rdf;
	protected QueryExecution qe_materials;
	protected ResultSet materials;
	protected SubstanceRecord record;
	protected Logger logger;
	public static final Properties substance_types = new Properties();

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger == null ? Logger.getLogger(getClass().getName())
				: logger;
	}

	public NanoWikiRDFReader(Reader reader) throws CDKException, IOException {
		this(reader, null);
	}

	public NanoWikiRDFReader(Reader reader, Logger logger) throws CDKException,
			IOException {
		super();
		setReader(reader);
		InputStream in = null;
		try {
			in = getClass().getClassLoader().getResourceAsStream(
					"net/idea/loom/nm/nanowiki/substance_type.properties");
			Assert.assertNotNull(in);
			substance_types.load(in);
		} finally {
			if (in != null)
				in.close();
		}
	}

	public static String generateUUIDfromString(String prefix, String id) {
		return prefix
				+ "-"
				+ (id == null ? UUID.randomUUID() : UUID.nameUUIDFromBytes(id
						.getBytes()));
	}

	@Override
	public void setReader(Reader reader) throws CDKException {
		try {
			rdf = ModelFactory.createDefaultModel();
			rdf.read(reader, "http://ontology.enanomapper.net", "RDF/XML");

			Query query = QueryFactory.create(NW.m_allmaterials.SPARQL());
			qe_materials = QueryExecutionFactory.create(query, rdf);

			materials = qe_materials.execSelect();
		} catch (IOException x) {
			throw new CDKException(x.getMessage());
		} finally {
			try {
				reader.close();
			} catch (Exception x) {
			}
		}
	}

	@Override
	public void setReader(InputStream reader) throws CDKException {
		try {
			setReader(new InputStreamReader(reader, "UTF-8"));
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
			Resource material = qs.getResource("m");
			record = new SubstanceRecord();
			record.setExternalids(new ArrayList<ExternalIdentifier>());
			try {
				parseMaterial(rdf, material, record);
				parseCoatings(rdf, material, record);
				return true;
			} catch (IOException x) {
				logger.log(Level.WARNING, x.getMessage());
				record = null;
				return false;
			}
		} else {
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

	private void parseCoatings(Model rdf, RDFNode material,
			SubstanceRecord record) throws IOException {
		ProcessSolution.execQuery(rdf, String.format(NW.m_coating.SPARQL(),
				material.asResource().getURI()), new ProcessCoatings(rdf,
				material, record));
	}

	private void parseMaterial(Model rdf, RDFNode material,

	SubstanceRecord record) throws IOException {
		ProcessSolution.execQuery(rdf, String.format(
				NW.m_materialprops.SPARQL(), material.asResource().getURI(),
				material.asResource().getURI(),
				material.asResource().getURI(),
				material.asResource().getURI(),
				material.asResource().getURI(),
				material.asResource().getURI(),
				material.asResource().getURI(),
				material.asResource().getURI(),
				material.asResource().getURI(),
				// new nw3
				material.asResource().getURI(), material.asResource().getURI(),
				material.asResource().getURI(), material.asResource().getURI(),
				material.asResource().getURI(), material.asResource().getURI(),
				material.asResource().getURI(), material.asResource().getURI(),
				material.asResource().getURI(), material.asResource().getURI(),
				material.asResource().getURI()), new ProcessMaterial(rdf,
				material, record));
	}

}

class ProcessCondition extends ProcessSolution {
	EffectRecord<String, IParams, String> effect;

	public ProcessCondition(EffectRecord<String, IParams, String> effect) {
		super();
		this.effect = effect;
	}

	@Override
	void processHeader(ResultSet rs) {
	}

	@Override
	public void process(ResultSet rs, QuerySolution qs) {
		Value v = null;
		try {
			Literal endpoint = qs.get("endpointLabel").asLiteral();
			Literal value = qs.get("value").asLiteral();
			RDFNode valueUnit = qs.get("valueUnit");
			if (endpoint == null)
				return;
			if (value == null)
				return;

			v = new Value();
			try {
				v.setLoValue(Double.parseDouble(value.getString()));
			} catch (Exception x) {
				v.setLoValue(value.getString());
			}
			if (valueUnit != null)
				v.setUnits(valueUnit.asLiteral().getString());
			IParams conditions = effect.getConditions();
			if (effect.getConditions() == null) {
				conditions = new Params();
				effect.setConditions(conditions);
			}
			if ("PH".equals(endpoint.getString().toUpperCase()))
				conditions.put(I5CONSTANTS.pH, v);
			else
				conditions.put(endpoint.getString(), v);

		} catch (Exception x) {
			x.printStackTrace();
		}

	}
}

class ProcessMeasurement extends ProcessSolution {
	SubstanceRecord record;
	ILiteratureEntry citation;
	Model rdf;

	public ProcessMeasurement(Model rdf, SubstanceRecord record) {
		this.record = record;
		this.citation = record.getReference();
		this.rdf = rdf;
	}

	@Override
	void processHeader(ResultSet rs) {
	}

	enum endpoints {
		BET {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.SPECIFIC_SURFACE_AREA;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.SPECIFIC_SURFACE_AREA;
			}
		},
		HOMO {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_UNKNOWN;
			}

		},
		LUMO {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_UNKNOWN;
			}

		},

		Boiling_Point {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_BOILING;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.BOILINGPOINT;
			}
		},
		Melting_Point {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_MELTING;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.eMELTINGPOINT;
			}
		},
		Zeta_Potential {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.ZETA_POTENTIAL;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.eZETA_POTENTIAL;
			}
		},
		Isoelectric_Point {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.ZETA_POTENTIAL;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.eISOELECTRIC_POINT;
			}
		},
		Aggregation {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.AGGLOMERATION_AGGREGATION;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.eAGGLO_AGGR_SIZE;
			}
		},
		Primary_Particle_Size {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_GRANULOMETRY;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.pPARTICLESIZE;
			}
		},
		Mean_Particle_Size {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_GRANULOMETRY;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.pPARTICLESIZE;
			}
		},
		Particle_Size {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_GRANULOMETRY;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.pPARTICLESIZE;
			}
		},
		Average_Length {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_GRANULOMETRY;
			}

			@Override
			public String getTag() {
				return "Average Length";
			}
		},
		Diameter {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_GRANULOMETRY;
			}

			@Override
			public String getTag() {
				return "Diameter";
			}
		},
		Z_Average_Diameter {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_GRANULOMETRY;
			}
		},
		Inner_Diameter {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_GRANULOMETRY;
			}
		},
		Width {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_GRANULOMETRY;
			}
		},
		Hydrodynamic_size {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_GRANULOMETRY;
			}

			@Override
			public String getTag() {
				// hydrodynamic and aerodynamic size is the same
				// MMAD is Mass median aerodynamic diameter - is this what
				// NanoWiki assumes here?
				return I5CONSTANTS.pMMAD;
			}
		},
		Thickness {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.PC_GRANULOMETRY;
			}
		},
		Shape {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.ASPECT_RATIO_SHAPE;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.eSHAPE;
			}
		},
		Specific_Surface_Area {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.SPECIFIC_SURFACE_AREA;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.SPECIFIC_SURFACE_AREA;
			}
		},
		Surface_Area {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.SPECIFIC_SURFACE_AREA;
			}

			@Override
			public String getTag() {
				return I5CONSTANTS.SPECIFIC_SURFACE_AREA;
			}
		},
		Toxicity {
			// what kind of toxicity endpoint???
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				// best guess
				return I5_ROOT_OBJECTS.TO_GENETIC_IN_VITRO;
			}
		},
		Toxicity_Classifier {
			// what kind of toxicity endpoint???
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				// best guess
				return I5_ROOT_OBJECTS.TO_GENETIC_IN_VITRO;
			}

			@Override
			public String getTag() {
				return name().replace("_", " ");
			}
		},
		Oxidation_State_Concentration {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				// best guess
				return I5_ROOT_OBJECTS.UNKNOWN_TOXICITY;
			}
		},
		IC50 {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				// best guess
				return I5_ROOT_OBJECTS.UNKNOWN_TOXICITY;
			}
		},
		Log_Reciprocal_EC50 {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.BAO_0003009;
				// Cell_Viability_Assay
			}
		},
		Cytotoxicity {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.BAO_0002993;
				// Cytotoxicity_Assay
			}
		},
		GI50, Log_GI50, Negative_Log_GI50 {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.BAO_0002100;
				// Cell_Growth_Assay
			}

		},
		Particles_Per_Cell {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				// best guess
				return I5_ROOT_OBJECTS.UNKNOWN_TOXICITY;
			}
		},
		Percentage_Non_2DViable_Cells, Percentage_Viable_Cells {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.BAO_0003009;
				// Cell_Viability_Assay
			}

			@Override
			public String getUnit() {
				return "%";
			}
		},
		Concentration_in_cell {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				// best guess
				return I5_ROOT_OBJECTS.BAO_0002993;
			}
		},
		LDH_Release {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.BAO_0003009;
				// Cell_Membrane_Integrity_Assay
			}
		},
		Metabolic_Activity {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.BAO_0003009;
				// Metabolic_Activity_Assay
			}
		},
		DNA_in_Tail {
			@Override
			public I5_ROOT_OBJECTS getCategory() {
				return I5_ROOT_OBJECTS.BAO_0002167;
				// DNA_Damage_Assay
			}
		},
		Concentration_in_culture_medium {

		},
		Bioassay_Profile {
		// ????
		};
		public I5_ROOT_OBJECTS getCategory() {
			return I5_ROOT_OBJECTS.UNKNOWN_TOXICITY;
		}

		public String getTag() {
			return name();
		}

		public String getUnit() {
			return null;
		}
	}

	@Override
	public void process(ResultSet rs, QuerySolution qs) {

		String endpoint = null;
		try {
			endpoint = qs.get("endpoint").asResource().getLocalName()
					.replace("_", " ");
		} catch (Exception x) {
			endpoint = qs.get("endpoint").toString();
		}

		String assayType = null;
		String bao = null;
		String celline = null;
		try {
			assayType = qs.get("assayType").asResource().getURI();
		} catch (Exception x) {
		}
		try {
			if (assayType == null)
				assayType = qs.get("assayType1").asResource().getURI();
		} catch (Exception x) {
		}
		try {
			bao = qs.get("bao").asResource().getURI();
		} catch (Exception x) {
		}

		if (bao == null) {
			try {
				bao = qs.get("bao1").asResource().getURI();
			} catch (Exception x) {
			}

		}

		try {
			if (qs.get("t_celline") != null)
				celline = qs.get("t_celline").asLiteral().getString();
		} catch (Exception x) {
			x.printStackTrace();
		}

		Protocol protocol = new Protocol(endpoint);
		String measuredEndpoint = endpoint;
		I5_ROOT_OBJECTS category = null;

		try {
			if (bao != null) {
				category = I5_ROOT_OBJECTS.valueOf(bao.replace(
						"http://www.bioassayontology.org/bao#", ""));
			}
		} catch (Exception x) {
		}
		try {

			endpoints ep = endpoints.valueOf(endpoint.replace("-", "_")
					.replace(" ", "_"));
			if (category == null)
				category = ep.getCategory();
			measuredEndpoint = ep.getTag();
		} catch (Exception x) {
			x.printStackTrace();
		}

		if (category == null)
			category = I5_ROOT_OBJECTS.UNKNOWN_TOXICITY;
		protocol.setCategory(category.name() + "_SECTION");
		protocol.setTopCategory(category.getTopCategory());

		RDFNode method = qs.get("method");
		try {
			String p = method.asResource().getLocalName().trim();
			if (!"".equals(p))
				protocol.addGuideline(method.asResource().getLocalName());
		} catch (Exception x) {
			// x.printStackTrace();
		}

		if (method == null)
			try {
				method = qs.get("assaymethod");
				String p = method.asResource().getLocalName();
				if (p != null)
					protocol.addGuideline(p.replace("_", " ").trim());
			} catch (Exception x) {
			}

		ProtocolApplication<Protocol, IParams, String, IParams, String> papp = category
				.createExperimentRecord(protocol);
		// papp.setReliability(reliability)
		try {
			if (method != null)
				papp.getParameters().put(I5CONSTANTS.methodType,
						method.asResource().getLocalName().replace("_", " "));
		} catch (Exception x) {

		}
		papp.setDocumentUUID(NanoWikiRDFReader.generateUUIDfromString("NWKI",
				null));
		try {
			if (qs.get("year") != null)
				papp.setReference(qs.get("year").asLiteral().getString());
		} catch (Exception x) {
		}
		try {
			if (qs.get("doilink") != null)
				papp.setReference(qs.get("doilink").asResource().getURI());
			else
				papp.setReference(qs.get("study").asResource().getURI());

			if (qs.get("year") != null)
				papp.setReferenceYear(qs.get("year").asLiteral().getString());
			/*
			 * if (qs.get("assayJournalLabel") != null)
			 * papp.setReferenceOwner(qs.get("assayJournalLabel").asLiteral()
			 * .getString());
			 */

		} catch (Exception x) {
			if (citation != null) {
				papp.setReference(citation.getURL());
				papp.setReferenceOwner(citation.getTitle());
			}
		}

		papp.setReferenceOwner("NanoWiki");
		Resource measurement = qs.get("measurement").asResource();

		try {
			papp.setCompanyName(measurement.getURI());
		} catch (Exception x) {
		}
		try {
			papp.setInterpretationResult(qs.get("resultInterpretation")
					.asLiteral().getString());
		} catch (Exception x) {
		}

		if (celline != null)
			papp.getParameters().put("Cell line", celline);

		EffectRecord<String, IParams, String> effect = category
				.createEffectRecord();
		effect.setEndpoint(measuredEndpoint);

		try {
			effect.setTextValue(qs.get("resultInterpretation").asLiteral()
					.getString());
		} catch (Exception x) {
		}

		try {
			RDFNode valueMin = qs.get("valueMin");
			if (valueMin != null) {
				effect.setLoValue(valueMin.asLiteral().getDouble());
				effect.setLoQualifier(">=");
			}
		} catch (Exception x) {
		}
		try {
			RDFNode valueMax = qs.get("valueMax");
			if (valueMax != null) {
				effect.setUpValue(valueMax.asLiteral().getDouble());
				effect.setUpQualifier("<=");
			}
		} catch (Exception x) {
		}
		RDFNode value = qs.get("value");
		try {
			if (value != null)
				effect.setLoValue(Double.parseDouble(value.asLiteral()
						.getString()));
		} catch (Exception x) {
			effect.setTextValue(value.asLiteral().getString());
			papp.setInterpretationResult(value.asLiteral().getString());
		}

		RDFNode valueError = qs.get("valueError");
		try {
			effect.setStdDev(Double.parseDouble(valueError.asLiteral()
					.getString()));
		} catch (Exception x) {
		}

		try {
			if (qs.get("valueUnit") != null) {
				effect.setUnit(qs.get("valueUnit").asLiteral().getString());
			}
		} catch (Exception x) {
			x.printStackTrace();
		}

		RDFNode dose = qs.get("dose");
		if (dose != null) {
			IParams v = new Params();
			try {
				v.setLoValue(Double.parseDouble(dose.asLiteral().getString()));
			} catch (Exception x) {
				v.setLoValue(null);
			}
			try {
				v.setUnits(qs.get("doseUnit").asLiteral().getString());
			} catch (Exception x) {
			}
			IParams conditions = effect.getConditions();
			if (effect.getConditions() == null)
				conditions = new Params();
			conditions.put(I5CONSTANTS.cDoses, v);
			effect.setConditions(conditions);
		}

		try {
			ProcessSolution.execQuery(rdf, String.format(
					NW.m_condition.SPARQL(), measurement.getURI()),
					new ProcessCondition(effect));

			papp.addEffect(effect);
		} catch (Exception x) {
			x.printStackTrace();
		}
		// qs.get("label");
		// qs.get("definedBy");
		record.addMeasurement(papp);
	}

}

/*
 * class ProcessNMMeasurement extends ProcessSolution { SubstanceRecord record;
 * String endpoint; I5_ROOT_OBJECTS category; ILiteratureEntry citation;
 * 
 * public ProcessNMMeasurement(SubstanceRecord record, I5_ROOT_OBJECTS category,
 * String endpoint) { this.record = record; this.endpoint = endpoint;
 * this.category = category; this.citation = record.getReference(); }
 * 
 * @Override void processHeader(ResultSet rs) { }
 * 
 * @Override public void process(ResultSet rs, QuerySolution qs) { RDFNode value
 * = qs.get("value"); if (value == null && qs.get("valueMin") == null) return;
 * 
 * String assayType = null; String bao = null; try { assayType =
 * qs.get("assayType").asResource().getURI(); } catch (Exception x) { } try {
 * bao = qs.get("bao").asResource().getURI(); } catch (Exception x) { }
 * 
 * Protocol protocol = new Protocol(endpoint);
 * 
 * protocol.setCategory(category.name() + "_SECTION");
 * protocol.setTopCategory(category.getTopCategory());
 * 
 * RDFNode method = qs.get("method"); try {
 * protocol.addGuideline(method.asResource().getLocalName()); } catch (Exception
 * x) { }
 * 
 * ProtocolApplication<Protocol, IParams, String, IParams, String> papp =
 * category .createExperimentRecord(protocol);
 * papp.setDocumentUUID(NanoWikiRDFReader.generateUUIDfromString("NWKI", null));
 * papp.setSubstanceUUID(record.getSubstanceUUID()); ReliabilityParams
 * reliability = new ReliabilityParams();
 * reliability.setStudyResultType("experimental result");
 * papp.setReliability(reliability); try { if (qs.get("year") != null)
 * papp.setReference(qs.get("year").asLiteral().getString()); } catch (Exception
 * x) { } try { if (qs.get("doilink") != null)
 * papp.setReference(qs.get("doilink").asResource().getURI()); else
 * papp.setReference(qs.get("study").asResource().getURI());
 * 
 * } catch (Exception x) { if (citation != null) {
 * papp.setReference(citation.getURL()); //
 * papp.setReferenceOwner(citation.getTitle()); } }
 * 
 * papp.setReferenceOwner("NanoWiki"); try { if (method != null)
 * papp.getParameters().put(I5CONSTANTS.methodType,
 * method.asResource().getLocalName()); } catch (Exception x) { }
 * 
 * EffectRecord effect = category.createEffectRecord();
 * effect.setEndpoint(endpoint); // effect.setConditions(new Params());
 * 
 * try { if (value != null) {
 * effect.setLoValue(Double.parseDouble(value.asLiteral() .getString()));
 * effect.setLoQualifier("="); } } catch (Exception x) {
 * effect.setTextValue(value.asLiteral().getString()); } try {
 * effect.setStdDev(qs.get("valueError").asLiteral().getDouble()); } catch
 * (Exception x) { }
 * 
 * try { effect.setLoValue(qs.get("valueMin").asLiteral().getDouble()); ;
 * effect.setLoQualifier(">="); } catch (Exception x) { } try {
 * effect.setUpValue(qs.get("valueMax").asLiteral().getDouble());
 * effect.setUpQualifier("<="); } catch (Exception x) { }
 * 
 * try { effect.setUnit(qs.get("valueUnit").asLiteral().getString()); } catch
 * (Exception x) { }
 * 
 * papp.addEffect(effect); record.addMeasurement(papp); } }
 */
// "SELECT distinct ?coating ?chemical ?smiles\n"+
class ProcessCoatings extends ProcessSolution {
	SubstanceRecord record;
	Model rdf;
	RDFNode material;
	String composition_uuid;

	public ProcessCoatings(Model rdf, RDFNode material, SubstanceRecord record) {
		this.record = record;
		this.rdf = rdf;
		this.material = material;
		composition_uuid = record.getSubstanceUUID();
	}

	@Override
	void processHeader(ResultSet rs) {
	}

	@Override
	public void process(ResultSet rs, QuerySolution qs) {

		// now add the same info as measurement - at least to test the approach
		Protocol protocol = I5_ROOT_OBJECTS.SURFACE_CHEMISTRY
				.getProtocol("Unknown");
		ProtocolApplication<Protocol, IParams, String, IParams, String> experiment = I5_ROOT_OBJECTS.SURFACE_CHEMISTRY
				.createExperimentRecord(protocol);
		experiment.setDocumentUUID(NanoWikiRDFReader.generateUUIDfromString(
				"NWKI", null));
		// record.addMeasurement(experiment);// should be one and the same

		try {
			experiment
					.setReference(qs.get("study").asResource().getLocalName());
		} catch (Exception x) {
		}

		experiment.setReferenceOwner("NanoWiki");

		// experiment...
		EffectRecord<String, IParams, String> erecord;

		if (record.getRelatedStructures() == null
				|| (record.getRelatedStructures().size() < 2)) {
			if (record.getFormula() != null && !"".equals(record.getFormula())) {
				erecord = I5_ROOT_OBJECTS.SURFACE_CHEMISTRY
						.createEffectRecord();
				erecord.setEndpoint("ATOMIC COMPOSITION");
				erecord.setTextValue(record.getFormula());
				erecord.getConditions().put("TYPE", new Value("CORE"));
				erecord.getConditions().put("ELEMENT_OR_GROUP",
						new Value(record.getFormula()));
				experiment.addEffect(erecord);
			}
		}
		// coating
		IStructureRecord coating = new StructureRecord();

		record.addStructureRelation(composition_uuid, coating,
				STRUCTURE_RELATION.HAS_COATING, new Proportion());
		try {
			coating.setRecordProperty(Property.getTradeNameInstance("COATING"),
					qs.get("coating").asResource().getLocalName());
		} catch (Exception x) {
		}
		;
		try {
			coating.setRecordProperty(Property.getNameInstance(),
					qs.get("chemical").asResource().getLocalName());
		} catch (Exception x) {
		}
		;
		try {
			coating.setContent(qs.get("smiles").asLiteral().getString());
			coating.setFormat("INC");
			coating.setSmiles(coating.getContent());
		} catch (Exception x) {
		}
		;
		try {
			coating.setRecordProperty(
					Property.getI5UUIDInstance(),
					NanoWikiRDFReader.generateUUIDfromString("NWKI",
							qs.get("chemical").asResource().getLocalName()));
		} catch (Exception x) {
			coating.setRecordProperty(Property.getI5UUIDInstance(),
					NanoWikiRDFReader.generateUUIDfromString("NWKI", null));
		}

		erecord = I5_ROOT_OBJECTS.SURFACE_CHEMISTRY.createEffectRecord();
		erecord.setEndpoint("ATOMIC COMPOSITION");
		try {
			erecord.getConditions().put("ELEMENT_OR_GROUP",
					new Value(coating.getContent()));
		} catch (Exception x) {
		}
		;
		try {
			erecord.setTextValue(qs.get("chemical").asResource().getLocalName());
		} catch (Exception x) {
		}
		;
		erecord.getConditions().put("TYPE", new Value("COATING"));

		try {
			erecord.getConditions().put("COATING_DESCRIPTION",
					new Value(qs.get("coating").asResource().getLocalName()));
		} catch (Exception x) {
		}
		try {
			erecord.getConditions().put("DESCRIPTION",
					new Value(qs.get("chemical").asResource().getLocalName()));
		} catch (Exception x) {
		}
		experiment.addEffect(erecord);
	}
}

class ProcessMaterial extends ProcessSolution {
	SubstanceRecord record;
	Model rdf;
	RDFNode material;

	public ProcessMaterial(Model rdf, RDFNode material, SubstanceRecord record) {
		this.record = record;
		this.rdf = rdf;
		this.material = material;
	}

	@Override
	void processHeader(ResultSet rs) {
	}

	@Override
	public void process(ResultSet rs, QuerySolution qs) {
		String name = null;
		try {
			name = qs.get("label2").asLiteral().getString();
		} catch (Exception x) {
		}
		;

		record.setReferenceSubstanceUUID(NanoWikiRDFReader
				.generateUUIDfromString("NWKI", name));
		record.setSubstanceUUID(NanoWikiRDFReader.generateUUIDfromString(
				"NWKI", name));
		// ?source variable is a pointer to the paper the material
		// try
		// {record.setOwnerName(qs.get("source").asResource().getLocalName());}
		// catch (Exception x) {};
		record.setOwnerName("NanoWiki");
		record.setOwnerUUID("NWKI-"
				+ UUID.nameUUIDFromBytes(record.getOwnerName().getBytes())
						.toString());
		try {
			record.setSubstancetype(qs.get("type").asResource().getURI());
		} catch (Exception x) {
		}
		;
		try {
			record.setSubstanceName(name);
		} catch (Exception x) {
		}
		;
		try {
			String label = qs.get("label").asLiteral().getString();
			record.setPublicName(label);
			Integer.parseInt(label);
			record.setPublicName("");
		} catch (Exception x) {

		}
		;
		try {
			record.getExternalids().add(
					new ExternalIdentifier("Has_Identifier", qs.get("id")
							.asLiteral().getString()));
		} catch (Exception x) {
		}

		try {
			record.getExternalids().add(
					new ExternalIdentifier("Alternative Identifier", qs
							.get("altid").asLiteral().getString()));
		} catch (Exception x) {
		}
		try {
			record.getExternalids().add(
					new ExternalIdentifier("Sigma Aldrich", qs
							.get("aldrich_id").asLiteral().getString()));
		} catch (Exception x) {
		}
		try {
			record.getExternalids().add(
					new ExternalIdentifier("ChEMBL", qs.get("chembl_id")
							.asLiteral().getString()));
		} catch (Exception x) {
		}
		try {
			record.getExternalids().add(
					new ExternalIdentifier("PubChem SID", qs.get("pubchem_sid")
							.asLiteral().getString()));
		} catch (Exception x) {
		}
		try {
			record.getExternalids().add(
					new ExternalIdentifier("PubChem CID", qs.get("pubchem_cid")
							.asLiteral().getString()));
		} catch (Exception x) {
		}
		try {
			record.getExternalids().add(
					new ExternalIdentifier("COD", qs.get("cod_id").asLiteral()
							.getString()));
		} catch (Exception x) {
		}
		String material_formula = null;
		try {
			material_formula = qs.get("composition").asLiteral().getString();
		} catch (Exception x) {
		}
		try {
			record.getExternalids().add(
					new ExternalIdentifier("Same as", qs.get("same_as")
							.asResource().getURI()));
		} catch (Exception x) {
		}
		try {
			record.getExternalids().add(
					new ExternalIdentifier("Close match", qs.get("close_match")
							.asResource().getURI()));
		} catch (Exception x) {
		}
		String material_coating = null;
		try {
			material_coating = qs.get("coating").asResource().getLocalName();
			record.getExternalids().add(
					new ExternalIdentifier("Coating", material_coating));
		} catch (Exception x) {
		}

		try {
			record.getExternalids().add(
					new ExternalIdentifier("DATASET", "NanoWiki"));
		} catch (Exception x) {
		}

		try {
			record.getExternalids().add(
					new ExternalIdentifier("SOURCE", qs.get("source")
							.asResource().getLocalName()));
		} catch (Exception x) {
		}
		try {
			record.getExternalids().add(
					new ExternalIdentifier("HOMEPAGE", qs.get("homepage")
							.asResource().getLocalName()));
		} catch (Exception x) {
		}
		String material_cas = null;
		try {
			material_cas = qs.get("cas").asLiteral().getString();
		} catch (Exception x) {
		}

		String material_smiles = null;
		try {
			material_smiles = qs.get("smiles").asLiteral().getString();
			System.out.println(record.getPublicName() + "\t" + material_smiles);
		} catch (Exception x) {
		}

		if (record.getSubstanceName().startsWith("JRC2011")) {
			ExternalIdentifier e = new ExternalIdentifier(
					"JRC Representative Manufactured Nanomaterials", record
							.getSubstanceName().replace("JRC2011 ", ""));
			record.getExternalids().add(e);
		}
		ParticleTypes particletype = null;
		try {
			try {
				record.setFormula(qs.get("composition").asLiteral().getString());
			} catch (Exception x) {
				record.setFormula(null);
			}
			IStructureRecord core = null;
			if (record.getFormula() != null) {
				String composition_uuid = record.getSubstanceUUID();
				core = new StructureRecord();
				// if there are no coating, will consider this is a component,
				// not a core
				Proportion p = new Proportion();
				if (material_coating == null) {
					p.setTypical_value(100.0);
					p.setTypical_unit("%");
				}
				record.addStructureRelation(
						composition_uuid,
						core,
						material_coating == null ? STRUCTURE_RELATION.HAS_CONSTITUENT
								: STRUCTURE_RELATION.HAS_CORE, p);
				try {
					core.setRecordProperty(Property.getI5UUIDInstance(),
							NanoWikiRDFReader.generateUUIDfromString("NWKI",
									record.getFormula()));
				} catch (Exception x) {
					core.setRecordProperty(Property.getI5UUIDInstance(),
							NanoWikiRDFReader.generateUUIDfromString("NWKI",
									null));
				}
				core.setFormula(record.getFormula());
				if (material_smiles != null) {
					core.setContent(material_smiles);
					core.setFormat("INC");
					core.setSmiles(material_smiles);
				}
				if (material_cas != null)
					core.setRecordProperty(Property.getCASInstance(),
							material_cas);
			}

			// now guess for the rest
			if (record.getSubstancetype() != null) {
				String term = NanoWikiRDFReader.substance_types
						.getProperty(record.getSubstancetype()
								.replace(":", "|"));
				try {
					particletype = ParticleTypes.valueOf(term);
					record.setSubstancetype(particletype.getAnnotation());
				} catch (Exception x) {
					x.printStackTrace();
				}
			} else if ("Glass wool".equals(record.getPublicName().trim())) {
				particletype = ParticleTypes.CHEBI_131191;
				record.setSubstancetype(particletype.getAnnotation());
			} else if ("Asbestos".equals(record.getPublicName().trim())) {
				particletype = ParticleTypes.CHEBI_46661;
				record.setSubstancetype(particletype.getAnnotation());
			} else if ("Alloy".equals(record.getSubstancetype())) {
				// do nothing
				particletype = ParticleTypes.Alloy;
				record.setSubstancetype(particletype.getAnnotation());
			} else if (record.getSubstancetype() == null
					|| "".equals(record.getSubstancetype().trim())) {
				particletype = ParticleTypes.NPO_199;
				record.setSubstancetype(particletype.getAnnotation());
			} else {
				System.out.println(record.getSubstancetype());
			}

			if (core != null)
				for (ParticleTypes ptype : ParticleTypes.values()) {
					if (ptype.getFormula() == null) {
						continue;
					} else if (ptype.getFormula().equals(core.getFormula())) {
						core.setSmiles(getSmiles(material_smiles, ptype));
						if (core.getSmiles() == null) {
							try {
								core.setContent(core.getSmiles());
								core.setFormat("INC");
								core.setSmiles(core.getContent());
							} catch (Exception x) {
							}
						}
						if (core.getRecordProperty(Property.getCASInstance()) == null)
							if (ptype.getCAS() != null)
								core.setRecordProperty(
										Property.getCASInstance(),
										ptype.getCAS());
						if (ptype.getEINECS() != null)
							core.setRecordProperty(
									Property.getEINECSInstance(),
									ptype.getEINECS());
						
						particletype = ptype;
						break;

					}
				}
			if (particletype != null) {
				record.setSubstancetype(particletype.name());
				StringBuilder b = new StringBuilder();
				b.append(particletype.toString());
				if (record.getPublicName() != null
						&& (b.toString().indexOf(record.getPublicName()) < 0)) {
					b.append(" ");
					b.append(record.getPublicName());
				}
				record.setPublicName(b.toString());
			}

			// todo more info
			try {
				core.setRecordProperty(Property.getNameInstance(),
						record.getFormula());
			} catch (Exception x) {
			}

		} catch (Exception x) {
			x.printStackTrace();
		}
		;
		try {
			LiteratureEntry ref = new LiteratureEntry(
					qs.get("journal_title") == null ? null : qs
							.get("journal_title").asLiteral().getString(),
					qs.get("doilink") == null ? null : (qs.get("doilink"))
							.asResource().getURI());
			record.setReference(ref);
		} catch (Exception x) {
			// System.out.println(record.getCompanyName());
			// x.printStackTrace();
			record.setReference(null);
		}
		try {
			parseMeasurement(rdf, material, record);
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	private String getSmiles(String material_smiles, ParticleTypes ptype) {
		return material_smiles == null ? ptype.getSMILES() : material_smiles;
	}

	private void parseMeasurement(Model rdf, RDFNode material,
			SubstanceRecord record) throws IOException {
		execQuery(rdf, String.format(NW.m_sparql.SPARQL(), material
				.asResource().getURI()), new ProcessMeasurement(rdf, record));
	}

}
