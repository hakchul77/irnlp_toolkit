/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.nlp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.ClearNlpWrapper;
import kr.jihee.irnlp_toolkit.nlp.StanfordNlpWrapper;
import kr.jihee.irnlp_toolkit.nlp.ClearNlpWrapper.SRLNode;
import kr.jihee.java_toolkit.util.JString;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;

import edu.stanford.nlp.semgraph.SemanticGraph;

/**
 * Unit test for functions using ClearNLP
 * 
 * @author Jihee
 */
public class TestClearNLP extends TestCase {

	public static final boolean TEST_CONFIG = true;
	public static final boolean TEST_BASIC = false;
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

		File config_file = new File(Env.CLEARNLP_CFG);
		assertTrue(config_file.exists());
		System.out.printf(" + %s [%s]\n", config_file.getCanonicalPath(), config_file.exists() ? "OK" : "NO");
	}

	/**
	 * ClearNlpWrapper Test for basic functions
	 * 
	 * @throws IOException
	 */
	public void testClearNlpWrapperForBasic() throws IOException {
		System.out.println("\n----- testClearNlpWrapperForBasic() ------------------------------");
		if (!TEST_BASIC)
			return;

		String text = "Samsung Electronics is a South Korean multinational electronics company in Suwon, South Korea.";
		text += " It is the flagship subsidiary of the Samsung Group.";

		ClearNlpWrapper nlp = new ClearNlpWrapper(Env.CLEARNLP_CFG);
		nlp.loadAll("tokenize, ssplit, pos, parse");
		assertTrue(nlp.tokenizer != null);
		assertTrue(nlp.detector != null);
		assertTrue(nlp.tagger != null);
		assertTrue(nlp.parser != null);

		assertEquals(2, nlp.detect(text).size());
		for (List<String> toks : nlp.detect(text)) {
			DEPTree units = NLPGetter.toDEPTree(toks);
			System.out.println("\n[Sentence] " + JString.join(" ", toks));
			assertEquals(toks.size(), ClearNlpWrapper.toTaggedWords(nlp.tag(units)).size());
			System.out.println("  <Tagged> " + JString.join(" ", ClearNlpWrapper.toTaggedWords(nlp.tag(units))));
			assertEquals(toks.size(), ClearNlpWrapper.toTypedDependencies(nlp.parse(units)).size());
			System.out.println("  <Parsed> " + JString.join("; ", ClearNlpWrapper.toTypedDependencies(nlp.parse(units))));

			System.out.println("-parsed SemanticGraph-----------------------------------------------------------");
			SemanticGraph sgraph = StanfordNlpWrapper.toSemanticGraph(ClearNlpWrapper.toTypedDependencies(nlp.parse(units)), false);
			assertEquals(toks.size(), JString.trimAndIndent(sgraph.toString(), 2).split("\n").length);
			System.out.println(JString.trimAndIndent(sgraph.toString(), 2));
		}
	}

	/**
	 * ClearNlpWrapper Test for advanced functions
	 * 
	 * @throws IOException
	 */
	public void testClearNlpWrapperForAdvanced() throws IOException {
		System.out.println("\n----- testClearNlpWrapperForAdvanced() ------------------------------");
		if (!TEST_ADVANCED)
			return;

		String text = "Samsung Electronics is a South Korean multinational electronics company in Suwon, South Korea.";

		ClearNlpWrapper nlp = new ClearNlpWrapper(Env.CLEARNLP_CFG);
		nlp.loadAll("tokenize, ssplit, pos, parse, srl");
		assertTrue(nlp.tokenizer != null);
		assertTrue(nlp.detector != null);
		assertTrue(nlp.tagger != null);
		assertTrue(nlp.parser != null);
		assertTrue(nlp.labeler != null);

		List<String> toks = nlp.detect(text).get(0);
		DEPTree units = NLPGetter.toDEPTree(toks);
		units = nlp.tag(units);
		units = nlp.parse(units);
		units = nlp.label(units);
		System.out.println("\n[Sentence] " + JString.join(" ", toks));

		assertEquals(2, ClearNlpWrapper.toSemanticRoleLabels(units).size());
		System.out.println("  <Labeled> " + JString.join("; ", ClearNlpWrapper.toSemanticRoleLabels(units)));

		System.out.println("-labeled SemanticGraph----------------------------------------------------------");
		SemanticGraph sgraph = StanfordNlpWrapper.toSemanticGraph(ClearNlpWrapper.toSemanticRoleLabels(units));
		assertEquals(2, sgraph.edgeCount());
		assertEquals(3, sgraph.size());
		assertEquals("is", sgraph.getFirstRoot().word());
		System.out.println(JString.trimAndIndent(sgraph.toString(), 2));

		System.out.println("-dependent Nodes----------------------------------------------------------------");
		assertEquals("is", ClearNlpWrapper.getAllVerbs(units).get(0).form);
		StringBuffer sb = new StringBuffer();
		for (DEPNode verb : ClearNlpWrapper.getAllVerbs(units)) {
			sb.append("->(Verb) " + ClearNlpWrapper.toTaggedWord(verb) + "\n");
			for (SRLNode dep : ClearNlpWrapper.getDependents(verb))
				sb.append("  ->(Node) " + dep + "\n");
		}
		System.out.println(JString.trimAndIndent(sb.toString(), 2));

		System.out.println("-toStringSRL--------------------------------------------------------------------");
		assertEquals(toks.size(), units.toStringSRL().split("\n").length);
		System.out.println(units.toStringSRL());
	}
}
