/**
 * 
 */
package kr.jihee.irnlp_toolkit.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import kr.ac.kaist.swrc.jhannanum.comm.Eojeol;
import kr.ac.kaist.swrc.jhannanum.comm.PlainSentence;
import kr.ac.kaist.swrc.jhannanum.comm.Sentence;
import kr.ac.kaist.swrc.jhannanum.exception.ResultTypeException;
import kr.ac.kaist.swrc.jhannanum.hannanum.Workflow;
import kr.ac.kaist.swrc.jhannanum.plugin.MajorPlugin.MorphAnalyzer.ChartMorphAnalyzer.ChartMorphAnalyzer;
import kr.ac.kaist.swrc.jhannanum.plugin.MajorPlugin.PosTagger.HmmPosTagger.HMMTagger;
import kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.MorphemeProcessor.UnknownMorphProcessor.UnknownProcessor;
import kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PlainTextProcessor.SentenceSegmentor.SentenceSegmentor;
import kr.ac.kaist.swrc.jhannanum.plugin.SupplementPlugin.PosProcessor.SimplePOSResult22.SimplePOSResult22;
import kr.jihee.java_toolkit.util.JString;

/**
 * Wrapper of HanNanum 0.8.4<br>
 * = URL : http://semanticweb.kaist.ac.kr/home/index.php/HanNanum
 * 
 * @author Jihee
 */
public class HannanumWrapper {

	public Properties prop;
	public Workflow detector;
	public Workflow tagger;

	/**
	 * Constructor
	 * 
	 * @param prop_file
	 * @throws IOException
	 */
	public HannanumWrapper(String prop_file) throws IOException {
		prop = new Properties();
		prop.loadFromXML(new FileInputStream(prop_file));
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void loadSentDetector() throws Exception {
		detector = new Workflow();
		detector.appendPlainTextProcessor(new SentenceSegmentor(), null);
		detector.activateWorkflow(true);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void loadPosTagger() throws Exception {
		tagger = new Workflow();
		tagger.setMorphAnalyzer(new ChartMorphAnalyzer(), prop.getProperty("morp.json"));
		tagger.appendMorphemeProcessor(new UnknownProcessor(), null);
		tagger.setPosTagger(new HMMTagger(), prop.getProperty("pos.json"));
		tagger.appendPosProcessor(new SimplePOSResult22(), null);
		tagger.activateWorkflow(true);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void loadAll() throws Exception {
		loadAll("ssplit, pos");
	}

	/**
	 * 
	 * @param annotator_spec
	 * @throws Exception
	 */
	public void loadAll(String annotator_spec) throws Exception {
		List<String> annotators = Arrays.asList(annotator_spec.toLowerCase().replaceAll("\\s", "").split(","));
		if (annotators.contains("ssplit"))
			loadSentDetector();
		if (annotators.contains("pos"))
			loadPosTagger();
	}

	/**
	 * 
	 */
	public void unload() {
		if (detector != null)
			detector.close();
		if (tagger != null)
			tagger.close();
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	public List<String> detect(String text) {
		detector.analyze(text);
		try {
			List<String> sentences = new ArrayList<String>();
			for (PlainSentence result : detector.getResultOfDocument(new PlainSentence(0, 0, true)))
				sentences.add(result.getSentence());
			return sentences;
		} catch (ResultTypeException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param sent
	 * @return
	 */
	public List<TaggedWord> tag(String sent) {
		tagger.analyze(sent);
		try {
			List<Sentence> results = tagger.getResultOfDocument(new Sentence(0, 0, true));
			if (results.size() != 1)
				throw new IllegalArgumentException("The number of results is expected as 1 but " + results.size());
			Sentence result = results.get(0);
			return toTaggedWords(result);
		} catch (ResultTypeException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Transform a sentence(문장) into a list of tagged eojeol(어절)
	 * 
	 * @param sent
	 * @return
	 */
	public static List<TaggedWord> toTaggedWords(Sentence sent) {
		String[] forms = sent.getPlainEojeols();
		Eojeol[] eojeols = sent.getEojeols();
		List<TaggedWord> taggedWords = new ArrayList<TaggedWord>();
		for (int i = 0; i < sent.length; i++)
			taggedWords.add(new TaggedWord(forms[i], eojeols[i]));
		return taggedWords;
	}

	/**
	 * Transform a sentence(문장) into a list of tagged morpheme(형태소)
	 * 
	 * @param sent
	 * @return
	 */
	public static List<TaggedMorp> toTagggedMorps(Sentence sent) {
		List<TaggedMorp> taggedMorps = new ArrayList<TaggedMorp>();
		for (Eojeol eojeol : sent.getEojeols())
			taggedMorps.addAll(toTagggedMorps(eojeol));
		return taggedMorps;
	}

	/**
	 * Transform a list of tagged eojeol(어절) into a list of tagged morpheme(형태소)
	 * 
	 * @param taggedWords
	 * @return
	 */
	public static List<TaggedMorp> toTaggedMorps(List<TaggedWord> taggedWords) {
		List<TaggedMorp> taggedMorps = new ArrayList<TaggedMorp>();
		for (TaggedWord taggedWord : taggedWords)
			taggedMorps.addAll(taggedWord.taggedMorps);
		return taggedMorps;
	}

	/**
	 * Transform an eojeol(어절) into a list of tagged morpheme(형태소)
	 * 
	 * @param eojeol
	 * @return
	 */
	public static List<TaggedMorp> toTagggedMorps(Eojeol eojeol) {
		String[] morps = eojeol.getMorphemes();
		String[] tags = eojeol.getTags();
		List<TaggedMorp> taggedMorps = new ArrayList<TaggedMorp>();
		for (int i = 0; i < eojeol.length; i++)
			taggedMorps.add(new TaggedMorp(morps[i], tags[i]));
		return taggedMorps;
	}

	/**
	 * Tagged eojeol(어절) data
	 * 
	 * @author Jihee
	 */
	public static class TaggedWord {
		public static String DEFAULT_DELIMITER = "\t";
		public String plain;
		public List<TaggedMorp> taggedMorps;

		public TaggedWord(String plain, Eojeol eojeol) {
			this.plain = plain;
			this.taggedMorps = toTagggedMorps(eojeol);
		}

		public String toString() {
			return toString(DEFAULT_DELIMITER);
		}

		public String toString(String sep) {
			return String.join(sep, Arrays.asList(plain, JString.join("+", taggedMorps)));
		}
	}

	/**
	 * Tagged morpheme(형태소) data
	 * 
	 * @author Jihee
	 */
	public static class TaggedMorp {
		public static String DEFAULT_DELIMITER = "/";
		public String morp;
		public String tag;

		public TaggedMorp(String morp, String tag) {
			this.morp = morp;
			this.tag = tag;
		}

		public String toString() {
			return toString(DEFAULT_DELIMITER);
		}

		public String toString(String sep) {
			return String.join(sep, Arrays.asList(morp, tag));
		}
	}
}
