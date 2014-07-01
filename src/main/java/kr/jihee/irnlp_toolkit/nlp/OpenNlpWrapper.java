/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

/**
 * Wrapper of OpenNLP 1.5.3<br>
 * = URL : http://opennlp.apache.org/
 * 
 * @author Jihee
 */
public class OpenNlpWrapper {

	public Properties prop;
	public SentenceDetectorME detector;
	public TokenizerME tokenizer;
	public POSTaggerME tagger;
	public ChunkerME chunker;
	public Parser parser;
	public List<NameFinderME> recognizers;

	public OpenNlpWrapper(String prop_file) throws IOException {
		prop = new Properties();
		prop.loadFromXML(new FileInputStream(prop_file));
	}

	public void loadSentDetector() throws IOException {
		String model_file = prop.getProperty("sent.model");
		System.err.printf("Loading sentence detector from %s ... ", model_file);
		detector = new SentenceDetectorME(new SentenceModel(new FileInputStream(model_file)));
		System.err.println("done");
	}

	public void loadTokenizer() throws IOException {
		String model_file = prop.getProperty("tok.model");
		System.err.printf("Loading tokenizer from %s ... ", model_file);
		tokenizer = new TokenizerME(new TokenizerModel(new FileInputStream(model_file)));
		System.err.println("done");
	}

	public void loadPosTagger() throws IOException {
		String model_file = prop.getProperty("pos.model");
		System.err.printf("Loading POS tagger from %s ... ", model_file);
		tagger = new POSTaggerME(new POSModel(new FileInputStream(model_file)));
		System.err.println("done");
	}

	public void loadChunker() throws IOException {
		String model_file = prop.getProperty("chunk.model");
		System.err.printf("Loading phrase chunker from %s ... ", model_file);
		chunker = new ChunkerME(new ChunkerModel(new FileInputStream(model_file)));
		System.err.println("done");
	}

	public void loadLexParser() throws IOException {
		String model_file = prop.getProperty("parse.model");
		System.err.printf("Loading parser from %s ... ", model_file);
		parser = ParserFactory.create(new ParserModel(new FileInputStream(model_file)));
		System.err.println("done");
	}

	public void loadEntityRecognizers() throws IOException {
		recognizers = new ArrayList<NameFinderME>();
		List<String> model_names = Arrays.asList("ner.person.model", "ner.organization.model", "ner.location.model", "ner.date.model", "ner.time.model", "ner.money.model", "ner.percentage.model");
		for (String model_name : model_names) {
			String model_file = prop.getProperty(model_name);
			System.err.printf("Loading named entity recognizer from %s ... ", model_file);
			recognizers.add(new NameFinderME(new TokenNameFinderModel(new FileInputStream(model_file))));
			System.err.println("done");
		}
	}

	public void loadAll() throws IOException {
		loadAll("ssplit, tokenize, pos, chunk, parse, ner");
	}

	public void loadAll(String annotator_spec) throws IOException {
		List<String> annotators = Arrays.asList(annotator_spec.toLowerCase().replaceAll("\\s", "").split(","));
		if (annotators.contains("ssplit"))
			loadSentDetector();
		if (annotators.contains("tokenize"))
			loadTokenizer();
		if (annotators.contains("pos"))
			loadPosTagger();
		if (annotators.contains("chunk"))
			loadChunker();
		if (annotators.contains("parse"))
			loadLexParser();
		if (annotators.contains("ner"))
			loadEntityRecognizers();
	}

	public String[] tokenize(String text) {
		return tokenizer.tokenize(text);
	}

	public String[] detect(String text) {
		return detector.sentDetect(text);
	}

	public String[] tag(String[] toks) {
		return tagger.tag(toks);
	}

	public String[] chunk(String[] toks, String[] tags) {
		return chunker.chunk(toks, tags);
	}

	public Parse parse(String sent) {
		return ParserTool.parseLine(sent, parser, 1)[0];
	}

	public Parse[] parse(String sent, int k) {
		return ParserTool.parseLine(sent, parser, k);
	}

	public ArrayList<Span> recognize(String[] toks) {
		ArrayList<Span> spans = new ArrayList<Span>();
		for (NameFinderME recognizer : recognizers)
			for (Span s : recognizer.find(toks))
				spans.add(s);
		return spans;
	}

	public void clearRecognizers() {
		for (NameFinderME recognizer : recognizers)
			recognizer.clearAdaptiveData();
	}

	public static String toChunkString(String[] toks, String[] tags, String[] chunks) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < chunks.length; i++) {
			if (i > 0 && !chunks[i].startsWith("I-") && !chunks[i - 1].equals("O"))
				sb.append(">");
			if (i > 0 && chunks[i].startsWith("B-"))
				sb.append(" <" + chunks[i].substring(2));
			else if (chunks[i].startsWith("B-"))
				sb.append("<" + chunks[i].substring(2));
			sb.append(" " + toks[i] + "/" + tags[i]);
		}
		if (!chunks[chunks.length - 1].equals("O"))
			sb.append(">");
		return sb.toString();
	}

	public static String toTreeString(Parse tree) {
		StringBuffer sb = new StringBuffer();
		tree.show(sb);
		return sb.toString();
	}
}
