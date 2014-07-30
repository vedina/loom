package net.idea.loom.nm.nanowiki.test;

import java.io.File;
import java.io.FileReader;

import junit.framework.Assert;
import net.idea.loom.nm.csv.CSV12Reader;
import net.idea.loom.nm.csv.CSV12SubstanceReader;

import org.junit.Test;

import ambit2.base.data.ILiteratureEntry._type;
import ambit2.base.data.LiteratureEntry;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.RawIteratingWrapper;

public class ProteinCoronaPaperReaderTest  {
	@Test
	public void test() throws Exception {
		RawIteratingWrapper reader = null;
		try {
			LiteratureEntry entry = new LiteratureEntry("Protein Corona Fingerprinting Predicts the Cellular Interaction of Gold and Silver Nanoparticles","http://dx.doi.org/10.1021/nn406018q");
    		entry.setType(_type.Dataset);
    		
			CSV12Reader chemObjectReader = new CSV12Reader(new FileReader(
					new File("D:/src-ideaconsult/Protein_Corona/MergedSheets.csv")),entry,"PRCR-");
			reader = new CSV12SubstanceReader(chemObjectReader);
			int r = 0;
			while (reader.hasNext()) {
				IStructureRecord mol = reader.nextRecord();
				Assert.assertTrue(mol instanceof SubstanceRecord);
				System.out.println(((SubstanceRecord)mol).getPublicName());
				System.out.println(((SubstanceRecord)mol).getMeasurements());
				r++;
			}
			Assert.assertEquals(120,r);
		} finally {
			reader.close();
		}
	}
	
}
