/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.nlp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;
import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.StanfordNlpWrapper;
import kr.jihee.java_toolkit.io.JXml;
import kr.jihee.java_toolkit.util.JString;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Unit test for functions using StanfordNLP
 * 
 * @author Jihee
 */
public class TestStanfordNLP extends TestCase {

	public static final boolean TEST_CONFIG = false;
	public static final boolean TEST_BASIC = false;
	public static final boolean TEST_PIPELINE = true;
	public static final boolean TEST_SUTIME = false;
	public static final boolean TEST_UTILITY = false;

	/**
	 * Configuration file Test
	 * 
	 * @throws IOException
	 */
	public void testConfiguration() throws IOException {
		System.out.println("\n----- testConfiguration() ------------------------------");
		if (!TEST_CONFIG)
			return;

		File config_file = new File(Env.STANFORDNLP_CFG);
		assertTrue(config_file.exists());
		System.out.printf(" + %s [%s]\n", config_file.getCanonicalPath(), config_file.exists() ? "OK" : "NO");
	}

	/**
	 * StanfordNlpWrapper Test for basic functions
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws XPathExpressionException
	 */
	public void testStanfordNlpWrapperForBasic() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		System.out.println("\n----- testStanfordNlpWrapperForBasic() ------------------------------");
		if (!TEST_BASIC)
			return;

		String text = "Samsung Electronics is a South Korean multinational electronics company headquartered in Suwon, South Korea.";
		text += " It is the flagship subsidiary of the Samsung Group.";

		StanfordNlpWrapper nlp = new StanfordNlpWrapper(Env.STANFORDNLP_CFG);
		nlp.loadPosTagger();
		nlp.loadLexParser();
		assertTrue(nlp.tagger != null);
		assertTrue(nlp.parser != null);

		assertEquals(2, StanfordNlpWrapper.detect(text).size());
		for (List<HasWord> words : StanfordNlpWrapper.detect(text)) {
			System.out.println("\n[Sentence] " + JString.join(" ", words));
			assertEquals(words.size(), nlp.tag(words).size());
			System.out.println("  <Tagged> " + JString.join(" ", nlp.tag(words)));

			JXml xml = new JXml(StanfordNlpWrapper.toTreeString(nlp.parse(words), "xmlTree"));
			assertEquals(1, xml.findElements("ROOT/S/VP").size());
			System.out.println("  <Parsed> " + StanfordNlpWrapper.toTreeString(nlp.parse(words), "oneline"));
		}
	}

	/**
	 * StanfordNlpWrapper Test for pipeline functions
	 * 
	 * @throws IOException
	 */
	public void testStanfordNlpWrapperForPipeline() throws IOException {
		System.out.println("\n----- testStanfordNlpWrapperForPipeline() ------------------------------");
		if (!TEST_PIPELINE)
			return;

		String text = "Samsung Electronics is a South Korean multinational electronics company headquartered in Suwon, South Korea.";
		text += " It is the flagship subsidiary of the Samsung Group.";

		StanfordNlpWrapper nlp = new StanfordNlpWrapper(Env.STANFORDNLP_CFG);
		nlp.loadAll("tokenize, ssplit, pos, lemma, ner, regexner, parse, dcoref");
		assertTrue(nlp.annotator != null);

		Annotation annotation = nlp.annotate(text);
		System.out.println("-toXml--------------------------------------------------------------------------");
		System.out.println(nlp.toXml(annotation));
		System.out.println("-toPrettyStr--------------------------------------------------------------------");
		System.out.println(nlp.toPrettyStr(annotation));

		assertEquals(2, annotation.get(SentencesAnnotation.class).size());
		for (CoreMap sentence : annotation.get(SentencesAnnotation.class)) {
			System.out.println("-TextAnnotation-----------------------------------------------------------------");
			System.out.println(sentence.get(TextAnnotation.class));

			System.out.println("-toTokens-----------------------------------------------------------------------");
			System.out.println(JString.join("\n", StanfordNlpWrapper.toTokens(sentence, text)));

			System.out.println("-toPhrases-----------------------------------------------------------------------");
			System.out.println(JString.join("\n", StanfordNlpWrapper.toPhrases(sentence, text)));

			System.out.println("-TreeAnnotation-----------------------------------------------------------------");
			System.out.println(sentence.get(TreeAnnotation.class).pennString().trim());

			System.out.println("-BasicDependenciesAnnotation----------------------------------------------------");
			System.out.println(sentence.get(BasicDependenciesAnnotation.class).toString().trim());

			System.out.println("-CollapsedDependenciesAnnotation------------------------------------------------");
			System.out.println(sentence.get(CollapsedDependenciesAnnotation.class).toString().trim());

			System.out.println("-CollapsedCCProcessedDependenciesAnnotation-------------------------------------");
			System.out.println(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class).toString().trim());
		}

		System.out.println("-toCoreferenceMap---------------------------------------------------------------");
		assertEquals(5, StanfordNlpWrapper.toCoreferenceMap(annotation).entrySet().size());
		for (Entry<Integer, List<CorefMention>> e : StanfordNlpWrapper.toCoreferenceMap(annotation).entrySet())
			for (CorefMention m : e.getValue())
				System.out.printf("%d\t%s\t%s\t%d\t%d\n", e.getKey(), m.mentionType, m.mentionSpan, m.sentNum, m.headIndex);
	}

	/**
	 * StanfordNlpWrapper Test for SUTime functions
	 * 
	 * @throws Exception
	 */
	public void testStanfordNlpWrapperForSUTime() throws Exception {
		System.out.println("\n----- testStanfordNlpWrapperForSUTime() ------------------------------");
		if (!TEST_SUTIME)
			return;

		String text = "Last summer, they met every Tuesday afternoon, from 1 pm to 3 pm. I went to school yesterday. I will come back two weeks later.";
		String date = "2014-01-01";

		StanfordNlpWrapper nlp = new StanfordNlpWrapper(Env.STANFORDNLP_CFG);
		nlp.loadTimeAnnotator();
		assertTrue(nlp.normalizer != null);

		JXml xml = new JXml(nlp.normalizeTime(text, date));
		assertEquals(6, xml.findElements("DOC/TEXT/TIMEX3").size());
		for (Element timex : xml.findElements("DOC/TEXT/TIMEX3"))
			System.out.println(JXml.toNodeString(timex));
	}

	/**
	 * StanfordNlpWrapper Test for utility functions
	 * 
	 * @throws Exception
	 */
	public void testStanfordNlpWrapperForUtility() throws IOException {
		System.out.println("\n----- testStanfordNlpWrapperForUtility() ------------------------------");
		if (!TEST_UTILITY)
			return;

		String text = "Samsung Electronics is a South Korean multinational electronics company headquartered in Suwon, South Korea.";

		StanfordNlpWrapper nlp = new StanfordNlpWrapper(Env.STANFORDNLP_CFG);
		nlp.loadAll("tokenize, ssplit, pos, parse");
		assertTrue(nlp.annotator != null);

		Annotation annotation = nlp.annotate(text);
		CoreMap sentence = annotation.get(SentencesAnnotation.class).get(0);

		System.out.println("-toTokenStrings-----------------------------------------------------------------");
		List<String> toks = StanfordNlpWrapper.toTokenStrings(sentence);
		System.out.println(JString.join(" ", toks));

		System.out.println("-CollapsedCCProcessedDependenciesAnnotation-------------------------------------");
		System.out.println(sentence.get(CollapsedCCProcessedDependenciesAnnotation.class).toString().trim());

		System.out.println("[TEST] findHeadIndexBetween-----------------------------------------------------");
		int idx1 = toks.indexOf("Samsung");
		int idx2 = toks.indexOf("Electronics") + 1;
		int idx3 = toks.indexOf("company") + 1;

		Integer head1 = StanfordNlpWrapper.findHeadBetween(sentence, idx1, idx2);
		assertEquals("Electronics", toks.get(head1));
		System.out.printf("  <Head between [%d..%d)> = %d ==> %s\n", idx1, idx2, head1, toks.get(head1));

		Integer head2 = StanfordNlpWrapper.findHeadBetween(sentence, idx1, idx3);
		assertEquals("company", toks.get(head2));
		System.out.printf("  <Head between [%d..%d)> = %d ==> %s\n", idx1, idx3, head2, toks.get(head2));
	}
}
