package net.idea.tbwiki;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.idea.loom.common.DownloadTool;
import net.idea.loom.common.ICallBack;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class CompoundInformation {


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
	}
	

	public void process(String compoundURI, ICallBack<String,QuerySolution> callback) throws Exception{

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
			try {callback.done(compoundURI);} catch (Exception x) {}
			try {qexec.close(); } catch (Exception x) {}
			try {model.close(); } catch (Exception x) {}
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

		
}
