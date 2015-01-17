package net.idea.ops.cli.test;

import java.net.URL;
import java.util.List;

import net.idea.opentox.cli.structure.Compound;
import net.idea.ops.cli.assay.Pathway;
import net.idea.ops.cli.pharmacology.OPSPathwayClient;

import org.junit.Assert;
import org.junit.Test;

public class OPSPathwayClientTest extends AbstractOPSClientTest<Pathway, OPSPathwayClient> {

    @Override
    protected OPSPathwayClient getOPSClient() throws Exception {
	return opscli.getPathwayClient();
    }

    @Test
    public void testPathwayByCompoundCount() throws Exception {
	Compound cmp = new Compound(new URL("http://www.conceptwiki.org/concept/5d814567-347f-49f7-88cf-4ae4fe035690"));
	OPSPathwayClient cli = getOPSClient();
	Integer count = cli.getPathwaysByCompoundCount(new URL(TEST_SERVER), cmp);
	Assert.assertTrue(count.intValue() > 0);
    }

    @Test
    public void testPathwayByCompound() throws Exception {
	Compound cmp = new Compound(new URL("http://www.conceptwiki.org/concept/5d814567-347f-49f7-88cf-4ae4fe035690"));
	OPSPathwayClient cli = getOPSClient();
	List<Pathway> list = cli.getPathwaysByCompound(cmp);
	Assert.assertTrue(list.size() > 0);
	Assert.assertEquals(1, list.size());
	for (Pathway pathway : list) {

	    Assert.assertNotNull(pathway.getResourceIdentifier());
	    Assert.assertEquals("Arylamine metabolism", pathway.getTitle());
	    Assert.assertNotNull(pathway.getOntology());
	    Assert.assertTrue(pathway.getOntology().size() > 0);
	    Assert.assertNotNull(pathway.getWebpage());
	}
    }
}
