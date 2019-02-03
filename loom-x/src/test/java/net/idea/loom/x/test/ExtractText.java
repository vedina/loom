package net.idea.loom.x.test;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.apache.tika.parser.ner.grobid.GrobidNERecogniser;
import org.junit.Assert;
import org.junit.Test;

import net.idea.loom.x.Xonfiguration;
import net.idea.loom.x.Xrecognition;
import net.idea.loom.x.Xtract;

public class ExtractText {

	@Test
	public void testExtractText() throws Exception {
		File docs_dir = new File(Xonfiguration.getProperty("docs_path"));

		File txt_dir = new File(Xonfiguration.getProperty("txt_path"));
		Xtract x = new Xtract();
		x.process(docs_dir, txt_dir, ".pdf");
	}

	@Test
	public void testConfig() {
		Assert.assertFalse(Xonfiguration.getProperty("models_path").indexOf("${") >= 0);
		Assert.assertFalse(Xonfiguration.getProperty("terms_path").indexOf("${") >= 0);
	}

	@Test
	public void testExtractTerms() throws Exception {
		Xrecognition xr = new Xrecognition(new File(Xonfiguration.getProperty("models_path")));
		File root_path = new File(Xonfiguration.getProperty("txt_path"));
		File terms_path = new File(Xonfiguration.getProperty("terms_path"));
		System.out.println(terms_path.getAbsolutePath());
		xr.process(root_path, terms_path);
	}

	@Test
	public void testExtractTerms_grobid() throws Exception {
		GrobidNERecogniser xr = new GrobidNERecogniser();
		File root_path = new File(Xonfiguration.getProperty("txt_path"));
		Map<String, Set<String>> terms = xr
				.recognise(new File(root_path, "nanogenotox_deliverable_3.pdf.txt").getAbsolutePath());
		//https://wiki.apache.org/tika/GrobidQuantitiesParser
		// File terms_path = new File(Xonfiguration.getProperty("terms_path"));
		// System.out.println(terms_path.getAbsolutePath());
		System.out.println(terms);
	}

}
