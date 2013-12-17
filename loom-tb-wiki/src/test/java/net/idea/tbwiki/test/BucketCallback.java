package net.idea.tbwiki.test;

import net.idea.loom.common.Bucket;
import net.idea.loom.common.ICallBack;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class BucketCallback implements ICallBack<String, QuerySolution>{
	protected Bucket bucket = new Bucket();
	public Bucket getBucket() {
		return bucket;
	}

	public void setBucket(Bucket bucket) {
		this.bucket = bucket;
	}

	protected String[] header = 
		new String[] {
			"Has_Smiles","Has_InChI","Has_InChIKey",
			"Has_SigmaAldrich_Id","Has_DrugBank_Id","Has_Leadscope_Id","Has_ChemSpider_Id",
			"Has_ChEBI_Id","Has_KEGG_Id","Has_CAS","Has_ChEMBL_Id","Has_PubChem_CID","Has_Open-2DTG-2DGate_Id",
			"Accepted_by_ToxCast",
			"Has_category","Has_toxic_effect","Has_target","Has_Toxicity_List",
			"Has_pKa"
		};
	public String[] getHeader() {
		return header;
	}
	
	public BucketCallback() {
		bucket.setHeader(header);
	}
	@Override
	public void process(String identifier, QuerySolution row) {
		RDFNode node = row.get("p");
		RDFNode object = row.get("o");

		String key = node.asResource().getURI().toString().
		replace("http://wiki.toxbank.net/wiki/Special:URIResolver/Property-3A","").trim();
		Object value = object.isLiteral()?object.asLiteral().getString():object.asResource().getURI();
		if ("http://www.w3.org/2000/01/rdf-schema#seeAlso".equals(key)) {
			//System.out.println("seeAlso\t" + value);
		} else 	if ("http://www.w3.org/2002/07/owl#sameAs".equals(key)) {
			//System.out.println("sameAs\t" + value);
		} else 
			bucket.put(key, value);

	}

	@Override
	public void done(String identifier) {
	}


}
