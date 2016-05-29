package net.idea.loom.nm.nanowiki;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public class ProcessSolution {
	public int process(ResultSet rs) {
		int records = 0;
		processHeader(rs);
		while (rs.hasNext()) {
			records++;
			QuerySolution qs = rs.next();
			process(rs, qs);
		}
		return records;
	}

	void processHeader(ResultSet rs) {
		for (String name : rs.getResultVars()) {
			// System.out.print(name);
			// System.out.print("\t");
		}
	}

	public void process(ResultSet rs, QuerySolution qs) {
		/*
		 * for (String name : rs.getResultVars()) { RDFNode node = qs.get(name);
		 * if (node == null) ; else if (node.isLiteral())
		 * System.out.print(node.asLiteral().getString()); else if
		 * (node.isResource()) System.out.print(node.asResource().getURI());
		 * else System.out.print(node.asNode().getName());
		 * System.out.print("\t"); } System.out.println();
		 */
	}

	public static int execQuery(Model rdf, String sparqlQuery,
			ProcessSolution processor) {
		// System.out.println(sparqlQuery);
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query, rdf);
		int records = 0;
		try {
			ResultSet rs = qe.execSelect();
			records = processor.process(rs);
		} finally {
			qe.close();
		}
		return records;
	}
	public void start() {
		
	}
	public void done() {

	}
}