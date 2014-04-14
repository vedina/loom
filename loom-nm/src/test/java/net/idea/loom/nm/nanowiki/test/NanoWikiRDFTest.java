package net.idea.loom.nm.nanowiki.test;

import java.io.File;
import java.io.FileReader;

import junit.framework.Assert;
import net.idea.loom.nm.nanowiki.NanoWikiRDFReader;

import org.junit.Test;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.interfaces.IStructureRecord;

public class NanoWikiRDFTest  {
	
	@Test
	public void test() throws Exception {
		NanoWikiRDFReader reader = null;
		try {
			reader = new NanoWikiRDFReader(new FileReader(new File("D:/src-other/nanowiki/backup.rdf")));
			while (reader.hasNext()) {
				IStructureRecord record = reader.nextRecord();
				Assert.assertTrue(record instanceof SubstanceRecord);
				//if (((SubstanceRecord)record).getMeasurements()!=null) System.out.println(((SubstanceRecord)record).getMeasurements());
			}
		} finally {
			reader.close();
		}
	}
}
	
