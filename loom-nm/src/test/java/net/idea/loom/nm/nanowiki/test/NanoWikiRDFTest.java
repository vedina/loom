package net.idea.loom.nm.nanowiki.test;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import net.idea.loom.nm.nanowiki.NanoWikiRDFReader;

import org.junit.Assert;
import org.junit.Test;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.EffectRecord;
import ambit2.base.data.study.IParams;
import ambit2.base.data.study.Protocol;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.interfaces.IStructureRecord;

public class NanoWikiRDFTest {
    private static Logger logger = Logger.getAnonymousLogger();

    @Test
    public void test() throws Exception {
	NanoWikiRDFReader reader = null;
	int records = 0;
	try {
		URL url = new URL("http://files.figshare.com/5228257/nanowiki.cczero.3.rdf.gz");
		Assert.assertNotNull(url);

	    reader = new NanoWikiRDFReader(new InputStreamReader(new GZIPInputStream(url.openStream()), "UTF-8"));
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
		    logger.log(Level.WARNING, material.getSubstanceName() + "\tSubstance without measurements");
		} else {
		    int m = 0;
		    for (ProtocolApplication<Protocol, IParams, String, IParams, String> papp : material
			    .getMeasurements()) {
			// System.out.print("Protocol " + (
			// papp.getProtocol()?null:papp.getProtocol()));
			// System.out.print("\t");
			// System.out.print("Ref " + papp.getReference());
			// System.out.print("\t");
			if (papp.getProtocol()==null)
			    logger.log(Level.WARNING, material.getSubstanceName() + "\tProtocol application without protocol");
			
			if (papp.getReference()==null)
			    logger.log(Level.WARNING, material.getSubstanceName() + "\tReference  not defined");
			if (papp.getReferenceOwner()==null)
			    logger.log(Level.WARNING, material.getSubstanceName() + "\tJournal not defined");
			if (papp.getReferenceYear()==null)
			    logger.log(Level.WARNING, material.getSubstanceName() + "\tPublication year not defined");
			if (papp.getEffects()==null || papp.getEffects().size()==0)
			logger.log(Level.WARNING, material.getSubstanceName() + "\tProtocol application without effect records");
			for (EffectRecord effect : papp.getEffects()) {
			    if ((effect.getLoValue() != null) && (effect.getUnit() == null))
				logger.log(Level.WARNING, material.getSubstanceName() + "\t" + effect.getEndpoint() + "\tValue without unit" );
			    m++;
			}

		    }
		    if (m <= 0)
			logger.log(Level.WARNING, material.getSubstanceName() + "\tSubstance without measurements");
		}
		records++;
	    }
	} catch (Exception x) {
	    logger.log(Level.SEVERE,x.getMessage(),x);
	} finally {
	    if (reader!=null) reader.close();
	}
	logger.log(Level.INFO, "Substance records read\t"+records);
    }
}
