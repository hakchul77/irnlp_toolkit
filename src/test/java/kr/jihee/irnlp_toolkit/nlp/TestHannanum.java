/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.nlp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.HannanumWrapper.TaggedMorp;
import kr.jihee.irnlp_toolkit.nlp.HannanumWrapper.TaggedWord;
import kr.jihee.java_toolkit.util.JString;

/**
 * Unit test for functions using HanNanum
 * 
 * @author Jihee
 */
public class TestHannanum extends TestCase {

	public static final boolean TEST_CONFIG = true;
	public static final boolean TEST_BASIC = false;

	/**
	 * Configuration file Test
	 * 
	 * @throws IOException
	 */
	public void testConfiguration() throws IOException {
		System.out.println("\n----- testConfiguration() ------------------------------");
		if (!TEST_CONFIG)
			return;

		File config_file = new File(Env.HANNANUM_CFG);
		assertTrue(config_file.exists());
		System.out.printf(" + %s [%s]\n", config_file.getCanonicalPath(), config_file.exists() ? "OK" : "NO");
	}

	/**
	 * HannanumWrapper Test for basic functions
	 * 
	 * @throws IOException
	 */
	public void testHannanumWrapperForBasic() throws Exception {
		System.out.println("\n----- testHannanumWrapperForBasic() ------------------------------");
		if (!TEST_BASIC)
			return;

		String text = "삼성전자는 대한민국에 본사를 둔 전자 제품을 생산하는 다국적 기업이다.";
		text += " 삼성전자는 대한민국에서 가장 큰 규모의 전자 기업이며, 삼성그룹을 대표하는 기업으로서 삼성그룹 안에서도 가장 규모가 크고 실적이 좋은 기업이다.";

		HannanumWrapper nlp = new HannanumWrapper(Env.HANNANUM_CFG);
		nlp.loadAll("ssplit, pos");
		assertTrue(nlp.detector != null);
		assertTrue(nlp.tagger != null);

		assertEquals(2, nlp.detect(text).size());
		for (String sent : nlp.detect(text)) {
			List<TaggedWord> taggedWords = nlp.tag(sent);
			List<TaggedMorp> taggedMorps = HannanumWrapper.toTaggedMorps(taggedWords);
			TaggedWord.DEFAULT_DELIMITER = "__";
			System.out.println("\n[Sentence] " + sent);
			System.out.println("  <Analyzed> " + JString.join(" ", taggedMorps));
			System.out.println("  <Tagged> " + JString.join(" ", taggedWords));
		}

		nlp.unload();
	}
}
