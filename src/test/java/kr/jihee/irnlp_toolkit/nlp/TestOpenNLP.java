/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.nlp;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.OpenNlpWrapper;
import kr.jihee.java_toolkit.util.JString;

/**
 * Unit test for functions using OpenNLP
 * 
 * @author Jihee
 */
public class TestOpenNLP extends TestCase {

	public static final boolean TEST_CONFIG = true;
	public static final boolean TEST_BASIC = true;
	public static final boolean TEST_ADVANCED = false;

	/**
	 * Configuration file Test
	 * 
	 * @throws IOException
	 */
	public void testConfiguration() throws IOException {
		System.out.println("\n----- testConfiguration() ------------------------------");
		if (!TEST_CONFIG)
			return;

		File config_file = new File(Env.OPENNLP_CFG);
		assertTrue(config_file.exists());
		System.out.printf(" + %s [%s]\n", config_file.getCanonicalPath(), config_file.exists() ? "OK" : "NO");
	}

	/**
	 * OpenNlpWrapper Test for basic functions
	 * 
	 * @throws IOException
	 */
	public void testOpenNlpWrapperForBasic() throws IOException {
		System.out.println("\n----- testOpenNlpWrapperForBasic() ------------------------------");
		if (!TEST_BASIC)
			return;

		String text = "Samsung Electronics is a South Korean multinational electronics company headquartered in Suwon, South Korea.";
		text += " It is the flagship subsidiary of the Samsung Group.";

		OpenNlpWrapper nlp = new OpenNlpWrapper(Env.OPENNLP_CFG);
		nlp.loadAll("ssplit, tokenize, pos, chunk, parse");
		assertTrue(nlp.detector != null);
		assertTrue(nlp.tokenizer != null);
		assertTrue(nlp.tagger != null);
		assertTrue(nlp.chunker != null);
		assertTrue(nlp.parser != null);

		assertEquals(2, nlp.detect(text).length);
		for (String sent : nlp.detect(text)) {
			String[] toks = nlp.tokenize(sent);
			String[] tags = nlp.tag(toks);
			String[] chunks = nlp.chunk(toks, tags);
			System.out.println("\n[Sentence] " + sent);
			System.out.println("  <Chunked> " + OpenNlpWrapper.toChunkString(toks, tags, chunks));
			System.out.println("  <Parsed> " + OpenNlpWrapper.toTreeString(nlp.parse(sent)));
		}
	}

	/**
	 * OpenNlpWrapper Test for advanced functions
	 * 
	 * @throws IOException
	 */
	public void testOpenNlpWrapperForAdvanced() throws IOException {
		System.out.println("\n----- testOpenNlpWrapperForAdvanced() ------------------------------");
		if (!TEST_ADVANCED)
			return;

		String text = "Samsung Electronics is a South Korean multinational electronics company headquartered in Suwon, South Korea.";
		text += " It is the flagship subsidiary of the Samsung Group.";

		OpenNlpWrapper nlp = new OpenNlpWrapper(Env.OPENNLP_CFG);
		nlp.loadAll("ssplit, tokenize, ner");
		assertTrue(nlp.detector != null);
		assertTrue(nlp.tokenizer != null);
		assertTrue(nlp.recognizers != null);

		assertEquals(2, nlp.detect(text).length);
		for (String sent : nlp.detect(text)) {
			String[] toks = nlp.tokenize(sent);
			System.out.println("\n[Sentence] " + sent);
			System.out.println("  <Recognized> " + JString.join(", ", nlp.recognize(toks)));
		}
	}
}
