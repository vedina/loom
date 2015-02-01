package net.idea.loom.nm.nanowiki.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

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
		// if (((SubstanceRecord)record).getMeasurements()!=null)
		// System.out.println(((SubstanceRecord)record).getMeasurements());
		SubstanceRecord material = (SubstanceRecord) record;
		/*
		System.out.print(material.getCompanyName());
		System.out.print("\t");
		System.out.print(material.getReference().getName());
		System.out.print("\t");
		System.out.print(material.getReference().getTitle());
		System.out.print("\t");
		System.out.print(material.getReference().getURL());
		System.out.print("\t");
		*/
		if (material.getMeasurements() == null) {
		    System.err.println("Substance without measurements\t"+ material.getCompanyName());
		} else {
		    if (material.getMeasurements().size()==0);
		    	System.err.println("Substance without measurements\t"+ material.getCompanyName());
		    for (ProtocolApplication<Protocol, IParams, String, IParams, String> papp : material.getMeasurements()) {
			// System.out.print("Protocol " + (
			// papp.getProtocol()?null:papp.getProtocol()));
			// System.out.print("\t");
			//System.out.print("Ref " + papp.getReference());
			//System.out.print("\t");
			Assert.assertTrue(papp.getEffects()!=null);
			Assert.assertTrue(papp.getEffects().size()>0);
			for (EffectRecord effect : papp.getEffects()) {
			    if ((effect.getLoValue()!=null) && (effect.getUnit()==null))
				System.err.println("Value without unit " + effect.getEndpoint() + "\t"+ material.getCompanyName());
			}
		    }
		}
		records++;
	    }
	} catch (Exception x) {
	    x.printStackTrace();
	} finally {
	    reader.close();
	}
	System.out.println(records);
    }
}
