package net.idea.tbwiki;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import net.idea.loom.common.Bucket;
import net.idea.loom.common.DownloadTool;
import net.idea.loom.common.ICallBack;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class CompoundInformation implements ICallBack<String, QuerySolution, Map<String,Object>> {
	protected Bucket bucket = new Bucket();
	protected String user;
	protected String password;
	private static final String sparqlEndpoint = "http://wiki.toxbank.net/wiki/Special:SPARQLEndpoint";
	private static final String sparql = 
	"SELECT ?uri ?p ?o\n"+ 
	"WHERE { \n"+
	"?uri <http://semantic-mediawiki.org/swivt/1.0#page> \"%s\".\n"+
	"?uri ?p ?o.\n"+
	"}\n";
	
	public CompoundInformation(String user,String password) {
		super();
		this.user = user;
		this.password = password;
		bucket.setHeader(new String[] {
			"Has_Smiles","Has_InChI","Has_InChIKey",
			"Has_SigmaAldrich_Id","Has_DrugBank_Id","Has_Leadscope_Id","Has_ChemSpider_Id",
			"Has_ChEBI_Id","Has_KEGG_Id","Has_CAS","Has_ChEMBL_Id","Has_PubChem_CID","Has_Open-2DTG-2DGate_Id",
			"Accepted_by_ToxCast",
			"Has_category","Has_toxic_effect","Has_target","Has_Toxicity_List",
			"Has_pKa",
		});
	}
	
	public Bucket process(String compoundURI) throws Exception{
		return process(compoundURI, this);
	}
	public Bucket process(String compoundURI, ICallBack<String,QuerySolution,Map<String,Object>> callback) throws Exception{
		bucket.clear();
		QueryEngineHTTP qexec = null;
		ResultSet rs = null;
		Model model = null;
		try {
			model = ModelFactory.createDefaultModel();
			//System.out.println(String.format(sparqlQuery,compoundURI,param));
			String sparql = String.format(loadSPARQL("net/idea/tbwiki/sparql/CompoundInformation.sparql"),compoundURI);
			//String sparql = String.format(CompoundInformation.sparql,compoundURI);
			qexec = new QueryEngineHTTP(sparqlEndpoint,sparql);
			qexec.setBasicAuthentication(user,password.toCharArray());
			rs = qexec.execSelect();
			while (rs.hasNext()) {
				callback.process(compoundURI,rs.next());
			}
			// Take ResultSet and serialize it into JSON Output Stream
		} catch (Exception x) {
			throw x;
		} finally {
			try {callback.done(bucket);} catch (Exception x) {}
			try {qexec.close(); } catch (Exception x) {}
			try {model.close(); } catch (Exception x) {}
			return bucket;
		}
	}
	
	protected String loadSPARQL(String resourceName) throws IOException {
		InputStream stream = null;
		try {
			stream = this.getClass().getClassLoader().getResourceAsStream(resourceName);
			if (stream==null) throw new IOException("Error loading "+resourceName);
			return loadSPARQL(stream);
		} catch (IOException x) {
			throw x;
		} finally {
			try {stream.close(); } catch (Exception x) {}
		}
	}
	
	protected String loadSPARQL(InputStream stream) throws IOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DownloadTool.download(stream,out);
			return out.toString();
		} catch (IOException x) {
			throw x;
		}
	}	
	@Override
	public Map<String, Object> process(String identifier, QuerySolution row) {
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

		return bucket;
	}
	@Override
	public Map<String, Object> done(Map<String, Object> result) {
		return bucket;
	}
		
}
