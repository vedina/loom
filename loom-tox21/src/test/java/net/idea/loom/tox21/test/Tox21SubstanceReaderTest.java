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
		/*
		File[] files = new File[] {
				new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720681_data.csv")
		};
		*/	
				/*
				new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720516_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743292_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743288_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743122_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743091_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743054_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743139_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743067_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743242_data.csv") 
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743240_data.csv") 
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743226_data.csv") 
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743239_data.csv") 
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743241_data.csv") 
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743053_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743140_data.csv")				 
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720719_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743227_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743228_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743077_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743199_data.csv") 
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720552_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743219_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743078_data.csv")
				,new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743063_data.csv")
				
		};
		*/

		for (int i=0; i < files.length; i++) 
			if (files[i].getName().endsWith(".csv")) {
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
					System.out.println(((SubstanceRecord)mol).getPublicName());
					//System.out.println(((SubstanceRecord)mol).getCompanyName());
					//System.out.println(((SubstanceRecord)mol).getMeasurements().get(0).getEffects().size());
					//System.out.println(((SubstanceRecord)mol).getMeasurements());
					r++;
					if (r>2) break;
				}
				Assert.assertTrue(r>0);
			} finally {
				reader.close();
			}
		}
	}
	
}
