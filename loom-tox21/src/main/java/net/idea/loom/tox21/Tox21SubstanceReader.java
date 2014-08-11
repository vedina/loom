package net.idea.loom.tox21;

import java.io.File;
import java.io.InputStream;

import net.idea.loom.pubchem.rest.PubChemAIDReader;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.IteratingDelimitedFileReader;

public class Tox21SubstanceReader  extends PubChemAIDReader {

	public Tox21SubstanceReader(IteratingDelimitedFileReader reader) {
		super(reader);
	}
	
	public Tox21SubstanceReader(File file) throws Exception {
		super(file,getMetadataStream(file));
	}
		
	protected static InputStream getMetadataStream(File file)  throws Exception {
		String key = file.getName().replace(".csv", "").replace("_data","");
		return Tox21SubstanceReader.class.getClassLoader().getResourceAsStream(String.format("net/idea/loom/tox21/%s.json",key));
	}
	@Override
	protected IStructureRecord createStructureRecord() {
		return new SubstanceRecord();
	}
	
}