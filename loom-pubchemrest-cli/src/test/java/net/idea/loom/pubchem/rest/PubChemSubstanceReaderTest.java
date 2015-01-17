package net.idea.loom.pubchem.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

import ambit2.base.data.SubstanceRecord;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.RawIteratingWrapper;

/*
 * http://pubchem.ncbi.nlm.nih.gov/rest/pug/assay/aid/720685/description/json
 */
public class PubChemSubstanceReaderTest {
    @Test
    public void test() throws Exception {
	File dir = new File("F:/Downloads/Chemical data/TOXCAST/Tox21/");
	// File[] files = dir.listFiles();

	File[] files = new File[] { new File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720681_data.csv") };

	/*
	 * new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720516_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743292_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743288_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743122_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743091_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743054_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743139_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743067_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743242_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743240_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743226_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743239_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743241_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743053_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743140_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720719_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743227_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743228_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743077_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743199_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_720552_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743219_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743078_data.csv")
	 * ,new
	 * File("F:/Downloads/Chemical data/TOXCAST/Tox21/AID_743063_data.csv")
	 * 
	 * };
	 */

	for (int i = 0; i < files.length; i++)
	    if (files[i].getName().endsWith(".csv")) {
		System.out.print(i + 1);
		System.out.print(".");
		System.out.println(files[i].getPath());
		RawIteratingWrapper reader = null;
		InputStream jsonmeta = null;
		try {
		    String key = files[i].getName().replace(".csv", "").replace("_data", "");
		    URL meta = this.getClass().getClassLoader()
			    .getResource(String.format("net/idea/loom/pubchem/aid/%s.json", key));
		    if (meta == null)
			throw new FileNotFoundException(key);
		    jsonmeta = new FileInputStream(meta.getFile());
		    if (jsonmeta == null)
			throw new FileNotFoundException(meta.getFile());

		    reader = new PubChemAIDReader(files[i], jsonmeta);
		    ((PubChemAIDReader) reader).setReadPubchemScoreOnly(true);
		    int r = 0;
		    while (reader.hasNext()) {
			IStructureRecord mol = reader.nextRecord();
			Assert.assertTrue(mol instanceof SubstanceRecord);
			System.out.println(((SubstanceRecord) mol).getPublicName());
			// System.out.println(((SubstanceRecord)mol).getCompanyName());
			// System.out.println(((SubstanceRecord)mol).getMeasurements().get(0).getEffects().size());
			System.out.println(((SubstanceRecord) mol).getMeasurements());
			r++;
			if (r > 2)
			    break;
		    }
		    Assert.assertTrue(r > 0);
		} finally {
		    reader.close();
		    if (jsonmeta != null)
			jsonmeta.close();
		}
	    }
    }

}
