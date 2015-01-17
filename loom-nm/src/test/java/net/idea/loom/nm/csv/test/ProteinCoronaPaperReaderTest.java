package net.idea.loom.nm.csv.test;

import java.io.File;
import java.io.FileReader;
import java.net.URL;

import junit.framework.Assert;
import net.idea.loom.nm.csv.CSV12Reader;
import net.idea.loom.nm.csv.CSV12SubstanceReader;

import org.junit.Test;

import ambit2.base.data.ILiteratureEntry._type;
import ambit2.base.data.LiteratureEntry;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.RawIteratingWrapper;

public class ProteinCoronaPaperReaderTest {
    @Test
    public void test() throws Exception {
	RawIteratingWrapper reader = null;
	try {
	    LiteratureEntry entry = new LiteratureEntry(
		    "Protein Corona Fingerprinting Predicts the Cellular Interaction of Gold and Silver Nanoparticles",
		    "http://dx.doi.org/10.1021/nn406018q");
	    entry.setType(_type.Dataset);

	    CSV12Reader chemObjectReader = new CSV12Reader(new FileReader(new File(
		    "D:/src-ideaconsult/Protein_Corona/MergedSheets.csv")), entry, "PRCR-");
	    reader = new CSV12SubstanceReader(chemObjectReader);
	    int r = 0;
	    while (reader.hasNext()) {
		IStructureRecord mol = reader.nextRecord();
		Assert.assertTrue(mol instanceof SubstanceRecord);
		SubstanceRecord substance = (SubstanceRecord) mol;
		Assert.assertNotNull(substance.getPublicName());
		System.out.println(substance.getPublicName());
		Assert.assertNotNull(substance.getCompanyName());
		Assert.assertNotNull(substance.getMeasurements());

		System.out.println(substance.getMeasurements());

		r++;
	    }
	    Assert.assertEquals(121, r);
	} finally {
	    reader.close();
	}
    }

    @Test
    public void test_CSV() throws Exception {
	RawIteratingWrapper reader = null;
	try {
	    LiteratureEntry entry = new LiteratureEntry("New test", "http://example.com");
	    entry.setType(_type.Dataset);

	    URL resource = getClass().getClassLoader().getResource("net/idea/loom/nm/csv/csvimport2.csv");
	    Assert.assertNotNull(resource);
	    CSV12Reader chemObjectReader = new CSV12Reader(new FileReader(resource.getFile()), entry, "TEST-");
	    reader = new CSV12SubstanceReader(chemObjectReader);
	    int r = 0;
	    while (reader.hasNext()) {
		IStructureRecord mol = reader.nextRecord();
		Assert.assertTrue(mol instanceof SubstanceRecord);
		SubstanceRecord substance = (SubstanceRecord) mol;
		Assert.assertNotNull(substance.getPublicName());
		System.out.println(substance.getPublicName());
		Assert.assertNotNull(substance.getCompanyName());
		Assert.assertNotNull(substance.getMeasurements());

		System.out.println(substance.getMeasurements());
		r++;
	    }
	    Assert.assertEquals(37, r);
	} finally {
	    reader.close();
	}
    }

}
