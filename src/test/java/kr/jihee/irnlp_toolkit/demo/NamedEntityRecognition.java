/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.demo;

import java.util.ArrayList;

import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.StanfordNlpWrapper;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Demo code for Named Entity Recognition (NER)
 * 
 * @author Jihee
 */
public class NamedEntityRecognition {

	/**
	 * Main function
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// data input
		String text = "John Brown bought 300 shares of Microsoft in Redmond at $5,000 in July, 2012.";

		// model loading
		StanfordNlpWrapper nlp = new StanfordNlpWrapper(Env.STANFORDNLP_CFG);
		nlp.loadAll("tokenize, ssplit, pos, lemma, ner, regexner");

		// task run
		Annotation annotation = nlp.annotate(text);
		for (CoreMap sent : annotation.get(SentencesAnnotation.class)) {
			ArrayList<String> strs = new ArrayList<String>();
			for (CoreLabel tokInfo : sent.get(TokensAnnotation.class))
				strs.add(String.format("%s/%s", tokInfo.word(), tokInfo.ner()));
			System.out.println(String.join(" ", strs));
		}
	}
}
