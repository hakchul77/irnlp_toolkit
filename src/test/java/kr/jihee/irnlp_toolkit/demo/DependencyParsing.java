/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.demo;

import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.StanfordNlpWrapper;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Demo code for Constituency Parsing
 * 
 * @author Jihee
 */
public class DependencyParsing {

	/**
	 * Main function
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// data input
		String text = "John may like an ice cream cake of the shop very much.";

		// model loading
		StanfordNlpWrapper nlp = new StanfordNlpWrapper(Env.STANFORDNLP_CFG);
		nlp.loadAll("tokenize, ssplit, parse");

		// task run
		Annotation annotation = nlp.annotate(text);
		for (CoreMap sent : annotation.get(SentencesAnnotation.class))
			System.out.println(sent.get(BasicDependenciesAnnotation.class).toString().trim());
	}
}
