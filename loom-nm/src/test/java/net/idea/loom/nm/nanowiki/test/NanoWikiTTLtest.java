package net.idea.loom.nm.nanowiki.test;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import ambit2.base.io.DownloadTool;

public class NanoWikiTTLtest extends NanoWikiRDFTest {
	@Override
	public File getNanoWikiFile() throws Exception {
		String nw3 = "nmdata.ttl";
		URL url = new URL("https://raw.githubusercontent.com/egonw/enmrdf/master/data.ttl");
		Assert.assertNotNull(url);
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		File file = new File(baseDir, nw3);
		if (!file.exists())
			DownloadTool.download(url, file);
		Assert.assertTrue(file.exists());
		return file;
	}

	@Override
	protected String getNanoWikiFormat() {
		return "TTL";
	}
	
	@Test
	public void parse() throws Exception {
		//TODO update numbers
	}
}
