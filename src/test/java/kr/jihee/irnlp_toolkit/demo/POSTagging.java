/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.demo;

import java.util.ArrayList;
import java.util.List;

import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.StanfordNlpWrapper;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * Demo code for POS Tagging
 * 
 * @author Jihee
 */
public class POSTagging {

	/**
	 * Main function
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// data input
		String text = "John loves Mary. She loves him too.";
		
		// model loading
		StanfordNlpWrapper nlp = new StanfordNlpWrapper(Env.STANFORDNLP_CFG);
		nlp.loadPosTagger();

		// task run
		for (List<HasWord> words : StanfordNlpWrapper.detect(text)) {
			ArrayList<String> strs = new ArrayList<String>();
			for (TaggedWord taggedWord : nlp.tag(words))
				strs.add(String.format("%s/%s", taggedWord.word(), taggedWord.tag()));
			System.out.println(String.join(" ", strs));
		}
	}
}
