package net.idea.loom.x;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class Xrecognition {

	SentenceDetectorME sentenceDetector;
	Tokenizer tokenizer = null;
	List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();

	public Xrecognition(File modelsPath) throws FileNotFoundException, IOException {
		loadModels(modelsPath);
	}

	protected void loadModels(File models_path) throws FileNotFoundException, IOException {

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

	}

	public JsonNode process(File root_path, File terms_file) throws Exception {

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
		mapper.writeValue(terms_file, terms);
		System.out.println(terms_file);
		return terms;
	}
}
