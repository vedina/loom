package net.idea.loom.x.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.idea.loom.x.Xonfiguration;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class testImgAnalysis {
	@Test
	public void testImage() throws Exception {
		// create parser as per desired parser
		TikaConfig config;
		String configfile = "configInceptionV3Net.xml";
		// String configfile = "config_VGG16.xml";
		try (InputStream stream = testImgAnalysis.class.getClassLoader()
				.getResourceAsStream(String.format("net/idea/loom/x/%s", configfile))) {
			config = new TikaConfig(stream);
		}
		Tika parser = new Tika(config);

		File imageFile = new File("DSC_0509.JPG");
		// 12.59.57[R][0@0][0].jpg
		Metadata meta = new Metadata();
		long now = System.currentTimeMillis();
		parser.parse(imageFile, meta);

		System.out.println(Arrays.toString(meta.getValues("OBJECT")));
		System.out.println(String.format("%s", System.currentTimeMillis() - now));
	}

}
