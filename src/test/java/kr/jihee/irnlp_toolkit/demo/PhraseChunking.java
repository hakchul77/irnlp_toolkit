/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.demo;

import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.OpenNlpWrapper;

/**
 * Demo code for Phrase Chunking
 * 
 * @author Jihee
 */
public class PhraseChunking {

	/**
	 * Main function
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// data input
		String text = "John likes very much an ice cream cake of the shop.";

		// model loading
		OpenNlpWrapper nlp = new OpenNlpWrapper(Env.OPENNLP_CFG);
		nlp.loadAll("ssplit, tokenize, pos, chunk");

		// task run
		for (String sent : nlp.detect(text)) {
			String[] toks = nlp.tokenize(sent);
			String[] tags = nlp.tag(toks);
			String[] chunks = nlp.chunk(toks, tags);
			System.out.println(OpenNlpWrapper.toChunkString(toks, tags, chunks));
		}
	}
}
