package net.idea.loom.nm.nanowiki;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
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
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.substance.ExternalIdentifier;
import ambit2.base.data.substance.SubstanceEndpointsBundle;
import ambit2.base.facet.BundleRoleFacet;
import ambit2.base.interfaces.ICiteable;
import ambit2.base.interfaces.IStructureRecord;
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
	protected Logger logger;
	public static final Properties substance_types = new Properties();
	
	protected HashMap<String, BundleRoleFacet> bundles = new HashMap<String, BundleRoleFacet>();

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger == null ? Logger.getLogger(getClass().getName())
				: logger;
	}

	public ENanoMapperRDFReader(Reader reader) throws CDKException, IOException {
		this(reader, null);
	}

	public ENanoMapperRDFReader(Reader reader, Logger logger) throws CDKException,
			IOException {
		super();
		setReader(reader);
		InputStream in = null;
		try {
			in = getClass().getClassLoader().getResourceAsStream(
					"net/idea/loom/nm/nanowiki/substance_type.properties");
			substance_types.load(in);
		} finally {
			if (in != null)
				in.close();
		}
	}

	@Override
	public void setReader(Reader reader) throws CDKException {
		try {
			rdf = ModelFactory.createDefaultModel();
			rdf.read(reader, "http://ontology.enanomapper.net", "TURTLE");

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
			Resource bundle = qs.getResource("bundle");
			record = new SubstanceRecord();
			record.setExternalids(new ArrayList<ExternalIdentifier>());
			if (bundle != null) {
				BundleRoleFacet bf = bundles.get(bundle.getURI());
				if (bf == null) {
					bf = new BundleRoleFacet(null);
					SubstanceEndpointsBundle b = new SubstanceEndpointsBundle();
					bf.setValue(b);
					bundles.put(bundle.getURI(), bf);
				}
				record.addFacet(bf);
			}
			try {
				parseMaterial(rdf, material, record);
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

	private void parseMaterial(Model rdf, RDFNode material,
   	  SubstanceRecord record) throws IOException {
	}

}
