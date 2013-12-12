package net.idea.ops.cli.test;

import java.net.URL;
import java.util.List;

import net.idea.opentox.cli.structure.Compound;
import net.idea.ops.cli.assay.AssayResult;
import net.idea.ops.cli.assay.Target;
import net.idea.ops.cli.pharmacology.OPSPharmacologyClient;

import org.junit.Assert;
import org.junit.Test;

public class OPSPharmacologyClientTest extends AbstractOPSClientTest<AssayResult,OPSPharmacologyClient> {

	@Override
	protected OPSPharmacologyClient getOPSClient() throws Exception {
		return opscli.getPharmacologyClient();
	}
	
	@Test
	public void testCompoundPharmacology() throws Exception {
		Compound cmp = new Compound(new URL("http://rdf.chemspider.com/236"));
		OPSPharmacologyClient cli = getOPSClient();
		List<AssayResult> list = cli.getCompoundPharmacology(new URL(TEST_SERVER),cmp,0,5);
		Assert.assertTrue(list.size()>0);
		Assert.assertEquals(5,list.size());
		for (AssayResult assayResult: list) {
			Compound compound = assayResult.getCompound();
			Assert.assertNotNull(compound.getResourceIdentifier());
			Assert.assertNotNull(compound.getInChI());
			Assert.assertNotNull(compound.getSMILES());
			Assert.assertNotNull(compound.getName());
			System.out.println(assayResult);
		}
	}	
	
	@Test
	public void testCompoundPharmacologyCount() throws Exception {
		Compound cmp = new Compound(new URL("http://rdf.chemspider.com/236"));
		OPSPharmacologyClient cli = getOPSClient();
		Integer count = cli.getCompoundPharmacologyCount(new URL(TEST_SERVER),cmp);
		Assert.assertTrue(count.intValue()>0);
	}	
	
	@Test
	public void testTargetPharmacologyCount() throws Exception {
		Target target = new Target(new URL("http://www.uniprot.org/uniprot/P19099"));
		OPSPharmacologyClient cli = getOPSClient();
		Integer count = cli.getTargetPharmacologyCount(new URL(TEST_SERVER),target);
		Assert.assertTrue(count.intValue()>0);
	}	
	@Test
	public void testTargetPharmacology() throws Exception {
		Target target = new Target(new URL("http://www.uniprot.org/uniprot/P19099"));
		OPSPharmacologyClient cli = getOPSClient();
		int pagesize =100;
		List<AssayResult> list = cli.getTargetPharmacology(new URL(TEST_SERVER),target,0,pagesize);
		Assert.assertTrue(list.size()>0);
		Assert.assertEquals(pagesize,list.size());
		for (AssayResult assayResult: list) {
			Compound compound = assayResult.getCompound();
			Assert.assertNotNull(compound.getResourceIdentifier());
			Assert.assertNotNull(compound.getInChI());
			Assert.assertNotNull(compound.getSMILES());
			Assert.assertNotNull(compound.getName());
			System.out.println(assayResult);
		}
		System.out.println("Activity types\t"+cli.getActivityTypes().size());
		System.out.println(cli.getActivityTypes());
		System.out.println("Activities\t"+cli.getActivities().size());
		System.out.println(cli.getActivities());
		System.out.println("Targets\t"+cli.getTargets().size());
		System.out.println(cli.getTargets());
		System.out.println("Citations\t"+cli.getCitations().size());
		System.out.println(cli.getCitations());
		System.out.println("Compounds\t"+cli.getCompounds().size());
		for (String cmp : cli.getCompounds().keySet()) {
			System.out.print(cmp);
			System.out.print("\t");
			System.out.println(cli.getCompounds().get(cmp));
		}
		System.out.println("Assays\t"+cli.getAssays().size());
		System.out.println(cli.getAssays());
	}	

}
