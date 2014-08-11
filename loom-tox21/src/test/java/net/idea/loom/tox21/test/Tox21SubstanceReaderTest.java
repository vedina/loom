package net.idea.loom.tox21.test;

import java.io.File;

import junit.framework.Assert;
import net.idea.loom.tox21.Tox21SubstanceReader;

import org.junit.Test;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.RawIteratingWrapper;

/*
 * http://pubchem.ncbi.nlm.nih.gov/rest/pug/assay/aid/720685/description/json
 */
public class Tox21SubstanceReaderTest  {
	@Test
	public void test() throws Exception {
		File dir = new File("F:/Downloads/Chemical data/TOXCAST/Tox21/");
		//File[] files = dir.listFiles();
		//"Please refer to other AIDs 743210, 743209, 720687, 720685, 720678 and 720681, for detailed assay protocols."
		File[] files = new File[] {
				new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720681_data.csv"),
				new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743228_data.csv"),
				new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743210_data.csv"),
				new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743209_data.csv"),
				new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720687_data.csv"),
				new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720685_data.csv"),
				new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720678_data.csv")
		};
		
			
		for (int i=0; i < files.length; i++) if (files[i].getName().endsWith(".csv")) {
			long now = System.currentTimeMillis();
			System.out.print(i+1);
			System.out.print(".");
			System.out.print(files[i].getPath());
			RawIteratingWrapper reader = null;
			long values = 0;
			int r = 0;			
			try {
				reader = new Tox21SubstanceReader(files[i]);
				while (reader.hasNext()) {
					IStructureRecord mol = reader.nextRecord();
					Assert.assertTrue(mol instanceof SubstanceRecord);
					//System.out.println(((SubstanceRecord)mol).getPublicName());
					//System.out.println(((SubstanceRecord)mol).getCompanyName());
					//System.out.println(((SubstanceRecord)mol).getMeasurements().get(0).getEffects().size());
					values += ((SubstanceRecord)mol).getMeasurements().get(0).getEffects().size();
					r++;
					//if (r>2) break;
				}
				Assert.assertTrue(r>0);
			} finally {
				reader.close();
			}
			System.out.print("\t");
			System.out.print(r);
			System.out.print("\t");
			System.out.print(values);
			System.out.print("\t");
			System.out.print((System.currentTimeMillis()-now));
			System.out.println(" ms.");
		}
	}
	
}
