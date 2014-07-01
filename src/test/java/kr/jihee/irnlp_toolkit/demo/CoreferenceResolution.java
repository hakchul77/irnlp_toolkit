/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.demo;

import java.util.List;
import java.util.Map;

import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.StanfordNlpWrapper;
import kr.jihee.java_toolkit.io.JFile;
import kr.jihee.java_toolkit.util.JString;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.pipeline.Annotation;

/**
 * Demo code for Dependency Parsing
 * 
 * @author Jihee
 */
public class CoreferenceResolution {

	/**
	 * Main function
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// data input
		String text = JFile.read(Env.SAMPLE_DIR + "news.txt");

		// model loading
		StanfordNlpWrapper nlp = new StanfordNlpWrapper(Env.STANFORDNLP_CFG);
		nlp.loadAll("tokenize, ssplit, pos, lemma, ner, regexner, parse, dcoref");

		// task run
		Annotation annotation = nlp.annotate(text);
		Map<Integer, List<CorefMention>> mention_map = StanfordNlpWrapper.toCoreferenceMap(annotation);
		for (Integer id : mention_map.keySet()) {
			List<CorefMention> mentions = mention_map.get(id);
			if (mentions.size() > 1)
				for (CorefMention m : mentions)
					System.out.println(JString.join("\t", id, m.mentionType, m.mentionSpan, m.sentNum, m.headIndex));
		}
	}
}
