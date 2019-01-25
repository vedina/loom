package net.idea.loom.x.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;

import org.apache.tika.Tika;
import org.junit.Assert;
import org.junit.Test;

import net.idea.loom.x.Xonfiguration;

public class ExtractText {

	@Test
	public void test() throws Exception {
		File outdir = new File(Xonfiguration.getProperty("docs_path"));

		File folder = new File(Xonfiguration.getProperty("txt_path"));
		extract_text(folder, outdir, new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".xls");
			}
		});
	}

	public void extract_text(File infolder, File outdir, FilenameFilter filter) throws Exception {
		Tika tika = new Tika();
		// Parse all given files and print out the extracted text content

		File[] files = infolder.listFiles(filter);
		for (File file : files) {

			System.out.println(file);
			// String text = tika.parseToString(file);
			String line;
			File file_txt = new File(outdir, file.getName() + ".txt");
			try (BufferedWriter fw = new BufferedWriter(new FileWriter(file_txt))) {

				try (BufferedReader reader = new BufferedReader(tika.parse(file))) {
					StringBuilder b = null;
					while ((line = reader.readLine()) != null) {
						if (line.trim().length() > 0) {
							if (b == null)
								b = new StringBuilder();
							b.append(line);
						} else {
							if (b != null) {
								fw.write(b.toString());
								fw.write("\r\n");
							}
							b = null;
						}
					}
					if (b != null) {
						fw.write(b.toString());
						fw.write("\r\n");
					}
				}

			}
		}

	}
	@Test
	public void testConfig() {
		Assert.assertFalse(Xonfiguration.getProperty("models_path").indexOf("${")>=0);
	}
	
}
