package net.idea.loom.isa.test;

import java.io.File;

import net.idea.loom.isa.ISAReader;

import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.junit.Assert;
import org.junit.Test;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.interfaces.IStructureRecord;

public class ISAReaderTest {

	@Test
	public void test() throws Exception {
		ISAReader reader = null;
		ISAConfigurationSet.setConfigPath("C://ToxBank//ISAcreator.SEURAT-v1.7.2//ISAcreator.SEURAT//Configurations//toxbank-config");
		int records = 0;
		try {
			reader = new ISAReader(new File("C://ToxBank//ISAcreator.SEURAT-v1.7.2//ISAcreator.SEURAT//isatab files//qHTSexample"));
			//reader = new ISAReader(new File("C://ToxBank//ISAcreator.SEURAT-v1.7.2//ISAcreator.SEURAT//isatab files//BII-I-1"));
			
			while (reader.hasNext()) {
				IStructureRecord record = reader.nextRecord();
				Assert.assertTrue(record instanceof SubstanceRecord);
				
				//if (((SubstanceRecord)record).getMeasurements()!=null) System.out.println(((SubstanceRecord)record).getMeasurements());
				/*
				SubstanceRecord material = (SubstanceRecord)record;
				System.out.print(material.getCompanyName());
				System.out.print("\t");
				System.out.print(material.getReference());
				System.out.print("\t");
				if (material.getMeasurements()==null)
					System.out.print("\t");
				else {
					System.out.print(material.getMeasurements().size());
					System.out.print("\t");
					for (ProtocolApplication papp : material.getMeasurements()) {
						System.out.print("Ref "+ papp.getReference());
						System.out.print("\t");
					}
				}
				*/
				System.out.println();
				
				records++;
				
			}
		} finally {
			if (reader !=null) reader.close();
		}
		Assert.assertTrue(records>0);
		System.out.println(records);
	}
}
