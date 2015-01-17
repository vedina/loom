package net.idea.loom.nm.nanowiki.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import junit.framework.Assert;
import net.idea.loom.nm.nanowiki.NanoWikiRDFReader;

import org.junit.Test;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.study.ProtocolApplication;
import ambit2.base.interfaces.IStructureRecord;

public class NanoWikiRDFTest {

    @Test
    public void test() throws Exception {
	NanoWikiRDFReader reader = null;
	int records = 0;
	try {
	    reader = new NanoWikiRDFReader(new InputStreamReader(new FileInputStream(new File(
		    "D:/src-other/nanowiki/backup_public.rdf")), "UTF-8"));
	    while (reader.hasNext()) {
		IStructureRecord record = reader.nextRecord();
		Assert.assertTrue(record instanceof SubstanceRecord);
		// if (((SubstanceRecord)record).getMeasurements()!=null)
		// System.out.println(((SubstanceRecord)record).getMeasurements());
		SubstanceRecord material = (SubstanceRecord) record;
		System.out.print(material.getCompanyName());
		System.out.print("\t");
		System.out.print(material.getReference());
		System.out.print("\t");
		if (material.getMeasurements() == null)
		    System.out.print("\t");
		else {
		    System.out.print(material.getMeasurements().size());
		    System.out.print("\t");
		    for (ProtocolApplication papp : material.getMeasurements()) {
			System.out.print("Ref " + papp.getReference());
			System.out.print("\t");
		    }
		}
		System.out.println();

		records++;
	    }
	} finally {
	    reader.close();
	}
	System.out.println(records);
    }
}
