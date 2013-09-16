package net.idea.ops.cli.test;

import java.net.URL;
import java.util.List;

import net.idea.opentox.cli.structure.Compound;
import net.idea.ops.cli.compound.OPSCompoundClient;

import org.junit.Assert;
import org.junit.Test;


public class OPSCompoundClientTest extends AbstractOPSClientTest<Compound,OPSCompoundClient> {

	@Override
	protected OPSCompoundClient getOPSClient() {
		return opscli.getCompoundClient();
	}
	
	
	@Test
	public void testSearchSimilar() throws Exception {
		
		OPSCompoundClient cli = getOPSClient();
		List<Compound> list = cli.searchSimilarStructures(new URL(TEST_SERVER),"c1ccccc1",0.75,50);
		Assert.assertTrue(list.size()>0);
		Assert.assertEquals(8,list.size());
	}	
	
	@Test
	public void testSearchSubstructure() throws Exception {
		
		OPSCompoundClient cli = getOPSClient();
		List<Compound> list = cli.searchSubstructures(new URL(TEST_SERVER),"c1ccccc1",5);
		Assert.assertTrue(list.size()>0);
		Assert.assertEquals(5,list.size());
	}		
	
	@Test
	public void testSearchExact() throws Exception {
		OPSCompoundClient cli = getOPSClient();
		List<Compound> list = cli.searchExactStructures(new URL(TEST_SERVER),"c1ccccc1");
		Assert.assertTrue(list.size()>0);
		Assert.assertNotNull(list.get(0).getSMILES());
	}
	
	@Test
	public void testSearchByInChI() throws Exception {
		OPSCompoundClient cli = getOPSClient();
		List<Compound> list = cli.searchStructuresByInchi(new URL(TEST_SERVER),"InChI=1S/C9H8O4/c1-6(10)13-8-5-3-2-4-7(8)9(11)12/h2-5H,1H3,(H,11,12)");
		Assert.assertTrue(list.size()>0);
		Assert.assertNotNull(list.get(0).getInChI());
	}
	@Test
	public void testSearchBySMILES() throws Exception {
		OPSCompoundClient cli = getOPSClient();
		List<Compound> list = cli.searchStructuresBySMILES(new URL(TEST_SERVER),"CC(=O)Oc1ccccc1C(=O)O");
		Assert.assertTrue(list.size()>0);
		Assert.assertNotNull(list.get(0).getSMILES());
	}
	
	@Test
	public void testSearchByInChIKey() throws Exception {
		OPSCompoundClient cli = getOPSClient();
		List<Compound> list = cli.searchStructuresByInchikey(new URL(TEST_SERVER),"BSYNRYMUTXBXSQ-UHFFFAOYSA-N");
		Assert.assertTrue(list.size()>0);
		Assert.assertNotNull(list.get(0).getInChIKey());
	}
		
	/*
	@Test
	public void testReadIdentifiers() throws Exception {
		OPSCompoundClient opsClient = getOPSClient();
		//get the first record
		List<Compound> substances = opsClient.getIdentifiersAndLinks(
				new URL(String.format("%s", TEST_SERVER)),//bosentan
				new URL(String.format("%s%s/1", TEST_SERVER,Resources.compound))
				);	
		Assert.assertNotNull(substances);
		for (Compound s : substances) {
			Assert.assertNotNull(s.getResourceIdentifier());
			System.out.println(s.getName());
			System.out.println(s.getResourceIdentifier());
			System.out.println(s.getProperties().get(Compound.opentox_ChEBI));
		}
	}
	*/
}