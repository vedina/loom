package net.idea.loom.tox21.test;

import java.io.File;

import junit.framework.Assert;
import net.idea.loom.tox21.Tox21SubstanceReader;

import org.junit.Test;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.RawIteratingWrapper;

public class Tox21SubstanceReaderTest  {
	@Test
	public void test() throws Exception {
		File dir = new File("F:/Downloads/Chemical data/TOXCAST/Tox21/");
		File[] files = dir.listFiles();
		//new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743203_data.csv")
		for (int i=0; i < files.length; i++) {
			System.out.print(i+1);
			System.out.print(".");
			System.out.println(files[i].getPath());
			RawIteratingWrapper reader = null;
			try {
				reader = new Tox21SubstanceReader(files[i]);
				int r = 0;
				while (reader.hasNext()) {
					IStructureRecord mol = reader.nextRecord();
					Assert.assertTrue(mol instanceof SubstanceRecord);
					//System.out.println(((SubstanceRecord)mol).getPublicName());
					//System.out.println(((SubstanceRecord)mol).getCompanyName());
					//System.out.println(((SubstanceRecord)mol).getMeasurements().get(0).getEffects().size());
					r++;
				}
				Assert.assertTrue(r>0);
			} finally {
				reader.close();
			}
		}
	}
	
}
