package net.idea.loom.nm.nanowiki.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;
import net.idea.loom.nm.nanowiki.NanoWikiRDFReader;

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
	    reader = new NanoWikiRDFReader(new InputStreamReader(new FileInputStream(new File(
		    "F:/Downloads/Chemical Data/enanomapper/backup-01022015.rdf")), "UTF-8"));
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
		    logger.log(Level.WARNING, material.getCompanyName() + "\tSubstance without measurements");
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
			    logger.log(Level.WARNING, material.getCompanyName() + "\tProtocol application without protocol");
			
			if (papp.getReference()==null)
			    logger.log(Level.WARNING, material.getCompanyName() + "\tReference  not defined");
			if (papp.getReferenceOwner()==null)
			    logger.log(Level.WARNING, material.getCompanyName() + "\tJournal not defined");
			if (papp.getReferenceYear()==null)
			    logger.log(Level.WARNING, material.getCompanyName() + "\tPublication year not defined");
			if (papp.getEffects()==null || papp.getEffects().size()==0)
			logger.log(Level.WARNING, material.getCompanyName() + "\tProtocol application without effect records");
			for (EffectRecord effect : papp.getEffects()) {
			    if ((effect.getLoValue() != null) && (effect.getUnit() == null))
				logger.log(Level.WARNING, material.getCompanyName() + "\t" + effect.getEndpoint() + "\tValue without unit" );
			    m++;
			}

		    }
		    if (m <= 0)
			logger.log(Level.WARNING, material.getCompanyName() + "\tSubstance without measurements");
		}
		records++;
	    }
	} catch (Exception x) {
	    x.printStackTrace();
	} finally {
	    reader.close();
	}
	logger.log(Level.INFO, "Substance records read\t"+records);
    }
}
