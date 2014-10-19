package net.idea.loom.pubchem.rest;

import java.util.List;

import org.junit.Test;

public class PubChemRESTClientTest {
	@Test
	public void test() throws Exception {
		PubChemRESTClient cli = new PubChemRESTClient();
		List t = cli.getSubstanceSynonyms("144208813");
		System.out.println(t);
		t = cli.getSubstanceSynonyms("144213119");
		System.out.println(t);
		t = cli.getSubstanceSynonyms("144209664");
		System.out.println(t);
		cli.close();
	}
}
