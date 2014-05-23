package net.idea.loom.nm.csv;

import java.util.Iterator;
import java.util.UUID;

import org.openscience.cdk.interfaces.IAtomContainer;

import ambit2.base.data.LiteratureEntry;
import ambit2.base.data.SubstanceRecord;
import ambit2.base.data.substance.ExternalIdentifier;
import ambit2.base.interfaces.IStructureRecord;
import ambit2.core.io.RawIteratingWrapper;
import ambit2.core.io.StringArrayHeader;

public class ProteinCoronaSubstanceReader  extends RawIteratingWrapper<ProteinCoronaPaperReader> {

	public ProteinCoronaSubstanceReader(ProteinCoronaPaperReader reader) {
		super(reader);
	}
	@Override
	protected IStructureRecord createStructureRecord() {
		return new SubstanceRecord();
	}


	@Override
	protected Object transform(Object o) {
		if (o instanceof IAtomContainer) try {

			r.clear();
			r.setFormat("SDF");
			java.util.Set<Object> keys = ((IAtomContainer)o).getProperties().keySet();
			Iterator  i = keys.iterator();
			while (i.hasNext()) {
				Object key = i.next();
				if (key instanceof StringArrayHeader) {
					((StringArrayHeader)key).assign((SubstanceRecord)r, ((IAtomContainer)o).getProperties().get(key));
				}
				//System.out.println(key.getClass().getName() + " : " + key);
//				r.setProperty(key, ((IAtomContainer)o).getProperties().get(key));
			}
			if (((SubstanceRecord)r).getCompanyUUID()==null)
				((SubstanceRecord)r).setCompanyUUID(reader.getPrefix()+UUID.randomUUID());
			
			//owner is the dataset
			((SubstanceRecord)r).setOwnerName(reader.getReference().getName());
			((SubstanceRecord)r).setOwnerUUID(reader.getPrefix() + UUID.nameUUIDFromBytes(reader.getReference().getURL().toString().getBytes()));
			//ids.add(new ExternalIdentifier("DOI","http://dx.doi.org/10.1021/nn406018q"));
			
			((IAtomContainer)o).getProperties().clear();


			r.setContent(writer.process((IAtomContainer)o));
			Object ref = ((IAtomContainer)o).getProperty("REFERENCE");
			if (ref instanceof LiteratureEntry)
				r.setReference((LiteratureEntry)ref);
			else r.setReference(getReference());
			
			return r;  
		} catch (Exception x) {
			r.clear();
			r.setFormat("SDF");
			r.setContent(null);
			r.setReference(getReference());
			return r;  
		} else return o;
	}
}