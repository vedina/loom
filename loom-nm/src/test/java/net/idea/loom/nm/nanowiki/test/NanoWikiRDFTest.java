package net.idea.loom.nm.nanowiki.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import net.idea.loom.nm.nanowiki.NW;
import net.idea.loom.nm.nanowiki.NanoWikiRDFReader;
import net.idea.loom.nm.nanowiki.ProcessSolution;
import net.idea.modbcum.i.facet.IFacet;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.data.substance.ExternalIdentifier;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.io.DownloadTool;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class NanoWikiRDFTest {
	private static Logger logger = Logger.getAnonymousLogger();

	static final String loggingProperties = "config/logging.properties";
	static final String log4jProperties = "config/log4j.properties";

	@Before
	public void init() throws Exception {
		InputStream in = null;
		try {
			URL url = getClass().getClassLoader()
					.getResource(loggingProperties);
			System.setProperty("java.util.logging.config.file", url.getFile());
			in = new FileInputStream(new File(url.getFile()));
			LogManager.getLogManager().readConfiguration(in);
			logger.log(
					Level.INFO,
					String.format("Logging configuration loaded from %s",
							url.getFile()));
		} catch (Exception x) {
			System.err
					.println("logging configuration failed " + x.getMessage());
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception x) {
			}
		}

		// now log4j for those who use it
		in = null;
		try {
			in = NanoWikiRDFTest.class.getClassLoader().getResourceAsStream(
					log4jProperties);
			PropertyConfigurator.configure(in);

		} catch (Exception x) {
			logger.log(Level.WARNING, x.getMessage());
		} finally {
			try {
				in.close();
			} catch (Exception x) {
			}
		}
	}

	public File getNanoWikiFile() throws Exception {
		String nw3 = "nanowiki.cczero.3.rdf.gz";
		URL url = new URL("https://ndownloader.figshare.com/files/5228257");
		Assert.assertNotNull(url);
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		File file = new File(baseDir, nw3);
		if (!file.exists())
			DownloadTool.download(url, file);
		Assert.assertTrue(file.exists());
		return file;
	}

	@Test
	public void testSourceProperties() throws Exception {
		testProperties("properties_source", 14);

	}

	@Test
	public void testMaterialProperties() throws Exception {
		testProperties("properties_material", 28);

	}

	@Test
	public void testMeasurementProperties() throws Exception {
		testProperties("properties_measurement", 20);
	}

	@Test
	public void testmeasurementMethod() throws Exception {
		testProperties("measurement_method", 4);
	}

	@Test
	public void testmeasurementUnits() throws Exception {
		testProperties("measurement_units", 16);
	}

	@Test
	public void testmeasurementEndpoint() throws Exception {
		testProperties("measurement_endpoint", 42);
	}

	@Test
	public void testSubstanceType() throws Exception {
		testProperties("substance_type", 10);
	}

	@Test
	public void testBundles() throws Exception {
		testProperties("bundle", 9);
	}
	
	protected String getNanoWikiFormat() {
		return "RDF/XML";
	}

	public void testProperties(String resource, int expectedsize)
			throws Exception {
		final Properties p = new Properties();
		InputStream in = null;
		try {
			in = getClass().getClassLoader().getResourceAsStream(
					"net/idea/loom/nm/nanowiki/" + resource + ".properties");
			Assert.assertNotNull(in);
			p.load(in);
			System.out.println(p);
		} finally {
			if (in != null)
				in.close();
		}
		File nanowikiFile = getNanoWikiFile();
		Assert.assertEquals(expectedsize, p.size());
		InputStreamReader reader = null;
		if (nanowikiFile.getName().endsWith(".gz"))
			reader = new InputStreamReader(new GZIPInputStream(
					new FileInputStream(nanowikiFile)), "UTF-8");
		else
			reader = new InputStreamReader(new FileInputStream(nanowikiFile),
					"UTF-8");
		try {

			Model rdf = ModelFactory.createDefaultModel();
			rdf.read(reader, "http://ontology.enanomapper.net",getNanoWikiFormat());

			ProcessSolution.execQuery(rdf, NW.SPARQL(resource),
					new ProcessSolution() {
						int props = 0;

						@Override
						public void process(ResultSet rs, QuerySolution qs) {
							if (qs.get("p").isResource()) {
								String uri = qs.get("p").asResource().getURI();
								Assert.assertNotNull(p.getProperty(uri.replace(
										":", "|")));
								System.out.println(uri);
							} else {
								String val = qs.get("p").asLiteral()
										.getString();
								Assert.assertNotNull(p.getProperty(val));
								System.out.println(val);
							}
							props++;
						}

						@Override
						public void done() {
							Assert.assertEquals(props, p.size());
						}
					});
		} finally {
			reader.close();
		}
	}

	@Test
	public void parse() throws Exception {
		NanoWikiRDFReader reader = null;
		int records = 0;
		int measurements = 0;
		int effectrecords = 0;
		Multiset<String> histogram = HashMultiset.create();
		Multiset<String> substancetypes = HashMultiset.create();
		Multiset<IFacet> bundles = HashMultiset.create();

		try {
			File nanowikiFile = getNanoWikiFile();
			if (nanowikiFile.getName().endsWith(".gz"))
				reader = new NanoWikiRDFReader(new InputStreamReader(new GZIPInputStream(
						new FileInputStream(nanowikiFile)), "UTF-8"),logger,getNanoWikiFormat());
			else
				reader = new NanoWikiRDFReader(new InputStreamReader(new FileInputStream(nanowikiFile),
						"UTF-8"),logger,getNanoWikiFormat());
			
			while (reader.hasNext()) {
				IStructureRecord record = reader.nextRecord();
				Assert.assertTrue(record instanceof SubstanceRecord);
				SubstanceRecord material = (SubstanceRecord) record;

				Assert.assertNotNull(material.getPublicName(),
						material.getSubstancetype());
				substancetypes.add(material.getSubstancetype());
				for (ExternalIdentifier id : material.getExternalids()) {
					histogram.add(id.getSystemDesignator());
				}
				if (material.getFacets() != null)
					for (IFacet facet : material.getFacets())
						bundles.add(facet);

				/*
				 * System.out.print(material.getCompanyName());
				 * System.out.print("\t");
				 * System.out.print(material.getReference().getName());
				 * System.out.print("\t");
				 * System.out.print(material.getReference().getTitle());
				 * System.out.print("\t");
				 * System.out.print(material.getReference().getURL());
				 * System.out.print("\t");
				 */
				if (material.getMeasurements() == null) {
					logger.log(Level.WARNING, material.getSubstanceName()
							+ "\tSubstance without measurements");
				} else {
					int m = 0;
					for (ProtocolApplication<Protocol, IParams, String, IParams, String> papp : material
							.getMeasurements()) {
						// System.out.print("Protocol " + (
						// papp.getProtocol()?null:papp.getProtocol()));
						// System.out.print("\t");
						// System.out.print("Ref " + papp.getReference());
						// System.out.print("\t");
						if (papp.getProtocol() == null)
							logger.log(
									Level.WARNING,
									material.getSubstanceName()
											+ "\tProtocol application without protocol");

						if (papp.getReference() == null)
							logger.log(Level.WARNING,
									material.getSubstanceName()
											+ "\tReference  not defined");
						if (papp.getReferenceOwner() == null)
							logger.log(Level.WARNING,
									material.getSubstanceName()
											+ "\tJournal not defined");
						if (papp.getReferenceYear() == null)
							logger.log(Level.WARNING,
									material.getSubstanceName()
											+ "\tPublication year not defined");
						if (papp.getEffects() == null
								|| papp.getEffects().size() == 0)
							logger.log(
									Level.WARNING,
									material.getSubstanceName()
											+ "\tProtocol application without effect records");
						for (EffectRecord effect : papp.getEffects()) {
							if ((effect.getLoValue() != null)
									&& (effect.getUnit() == null))
								logger.log(Level.WARNING,
										material.getSubstanceName() + "\t"
												+ effect.getEndpoint()
												+ "\tValue without unit");
							m++;
						}
						measurements++;
						effectrecords += m;
					}

					if (m <= 0)
						logger.log(Level.WARNING, material.getSubstanceName()
								+ "\tSubstance without measurements");
				}
				records++;
			}
		} catch (Exception x) {
			logger.log(Level.SEVERE, x.getMessage(), x);
		} finally {
			if (reader != null)
				reader.close();
		}
		System.out.println(bundles);

		System.out.println(substancetypes);
		System.out.println(histogram);
		// Assert.assertEquals(48, bundles.size());
		Assert.assertEquals(12, histogram.count("Sigma Aldrich"));
		Assert.assertEquals(4, histogram.count("ChEMBL"));
		// Assert.assertEquals(histogram.count("PubChem CID"), 4);
		Assert.assertEquals(4, histogram.count("PubChem SID"));
		Assert.assertEquals(8, histogram.count("COD"));
		Assert.assertEquals(22, histogram
				.count("JRC Representative Manufactured Nanomaterials"));
		Assert.assertEquals(25, histogram.count("HOMEPAGE"));
		Assert.assertEquals(390, histogram.count("SOURCE"));
		Assert.assertEquals(5, histogram.count("Close match"));
		Assert.assertEquals(4, histogram.count("Same as"));

		// [Alternative Identifier x 51, Coating x 68, SOURCE x 390, Composition
		// x 391, PubChem SID x 4, DATASET x 407, Has_Identifier x 407]
		// all materials without renamed JRC ones
		Assert.assertEquals(403, records);
		Assert.assertEquals(854, measurements);
		Assert.assertEquals(2485, effectrecords);

		logger.log(Level.INFO, "Substance records read\t" + records);
	}
}
