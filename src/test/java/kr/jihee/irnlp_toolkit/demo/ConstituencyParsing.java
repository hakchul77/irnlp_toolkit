/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.demo;

import java.util.List;

import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.StanfordNlpWrapper;
import edu.stanford.nlp.ling.HasWord;

/**
 * Demo code for Constituency Parsing
 * 
 * @author Jihee
 */
public class ConstituencyParsing {

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
		nlp.loadLexParser();

		// task run
		for (List<HasWord> words : StanfordNlpWrapper.detect(text))
			System.out.println(StanfordNlpWrapper.toTreeString(nlp.parse(words), "oneline"));
	}
}
