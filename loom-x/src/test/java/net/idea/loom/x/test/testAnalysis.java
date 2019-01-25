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

public class testAnalysis {
	@Test
	public void testImage() throws Exception {
		// create parser as per desired parser
		TikaConfig config;
		String configfile = "configInceptionV3Net.xml";
		// String configfile = "config_VGG16.xml";
		try (InputStream stream = testAnalysis.class.getClassLoader()
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

	@Test
	public void testText_docs() throws Exception {
		File root_path = new File(Xonfiguration.getProperty("txt_path"));
		File models_path = new File(Xonfiguration.getProperty("models_path"));
		File out_path = new File(Xonfiguration.getProperty("results_path"));
		ner(root_path,models_path,out_path);
	}

	public void ner(File root_path,File models_path,File out_path ) throws Exception {

		Tokenizer tokenizer = null;
		List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
		SentenceDetectorME sentenceDetector;
		String[] models = new String[] { "en-ner-person.bin", "en-ner-date.bin", "en-ner-organization.bin",
				"en-ner-location.bin", "en-ner-time.bin", "en-ner-money.bin", "en-ner-percentage.bin" };

		try (InputStream modelIn1 = new FileInputStream(new File(models_path, "en-token.bin"))) {
			tokenizer = new TokenizerME(new TokenizerModel(modelIn1));
		}
		try (InputStream modelIn = new FileInputStream(new File(models_path, "en-sent.bin"))) {
			sentenceDetector = new SentenceDetectorME(new SentenceModel(modelIn));
		}

		for (String model : models)
			try (InputStream modelIn2 = new FileInputStream(new File(models_path, model))) {
				TokenNameFinder finder = new NameFinderME(new TokenNameFinderModel(modelIn2));
				finders.add(finder);
			}

		ObjectMapper mapper = new ObjectMapper();
		JsonNode terms = mapper.createObjectNode();

		int r = 0;

		File[] files = root_path.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".txt");
			}
		});
		for (File file : files) {
			System.out.println(file);
			String content = FileUtils.readFileToString(file, Charset.forName("UTF-8"));

			String[] sentences = sentenceDetector.sentDetect(content);
			for (String sentence : sentences) {
				// System.out.println(sentence);

				String tokens[] = tokenizer.tokenize(sentence);

				for (TokenNameFinder finder : finders) {

					Span nameSpans[] = finder.find(tokens);

					String[] array = Span.spansToStrings(nameSpans, tokens);
					for (int i = 0; i < array.length; i++) {

						JsonNode set = terms.get(nameSpans[i].getType());

						if (set == null) {
							set = mapper.createObjectNode();
							((ObjectNode) terms).put(nameSpans[i].getType(), set);
						}
						JsonNode _files = (JsonNode) ((ObjectNode) set).get(array[i]);
						if (_files == null) {
							_files = mapper.createObjectNode();
							((ObjectNode) set).put(array[i], _files);
							((ObjectNode) _files).put(file.getName(), 1);
						} else {
							if (((ObjectNode) _files).get(file.getName()) == null)
								((ObjectNode) _files).put(file.getName(), 1);
							else {
								int num = ((ObjectNode) _files).get(file.getName()).asInt();
								((ObjectNode) _files).put(file.getName(), num + 1);
							}
						}

					}

				}

			}
			r++;
			// if (r > 1)
			// break;

		}
		System.out.println(terms);
		mapper.writeValue(new File(out_path,"terms.json"), terms);
	}
}
