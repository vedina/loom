package net.idea.loom.nm.nanowiki.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import net.idea.loom.nm.nanowiki.NW;
import net.idea.loom.nm.nanowiki.NanoWikiRDFReader;
import net.idea.loom.nm.nanowiki.ProcessSolution;

import org.junit.Assert;
import org.junit.Test;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.base.io.DownloadTool;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class NanoWikiRDFTest {
	private static Logger logger = Logger.getAnonymousLogger();

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
		Assert.assertEquals(expectedsize, p.size());
		InputStreamReader reader = new InputStreamReader(new GZIPInputStream(
				new FileInputStream(getNanoWikiFile())), "UTF-8");
		try {

			Model rdf = ModelFactory.createDefaultModel();
			rdf.read(reader, "http://ontology.enanomapper.net", "RDF/XML");

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
	public void test() throws Exception {
		NanoWikiRDFReader reader = null;
		int records = 0;
		try {
			File file = getNanoWikiFile();
			reader = new NanoWikiRDFReader(new InputStreamReader(
					new GZIPInputStream(new FileInputStream(file)), "UTF-8"));
			while (reader.hasNext()) {
				IStructureRecord record = reader.nextRecord();
				Assert.assertTrue(record instanceof SubstanceRecord);
				SubstanceRecord material = (SubstanceRecord) record;
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
		logger.log(Level.INFO, "Substance records read\t" + records);
	}
}
