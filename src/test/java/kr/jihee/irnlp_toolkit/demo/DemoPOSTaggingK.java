/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.demo;

import java.util.List;

import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.HannanumWrapper;
import kr.jihee.irnlp_toolkit.nlp.HannanumWrapper.TaggedMorp;
import kr.jihee.irnlp_toolkit.nlp.HannanumWrapper.TaggedWord;
import kr.jihee.java_toolkit.util.JString;

/**
 * Demo code for POS Tagging for Korean language
 * 
 * @author Jihee
 */
public class DemoPOSTaggingK {

	/**
	 * Main function
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// data input
		String text = "철수는 영희를 좋아한다. 그녀도 그를 역시 좋아한다.";
		
		// model loading
		HannanumWrapper nlp = new HannanumWrapper(Env.HANNANUM_CFG);
		nlp.loadAll("ssplit, pos");

		// task run
		for (String sent : nlp.detect(text)) {
			List<TaggedWord> taggedWords = nlp.tag(sent);
			List<TaggedMorp> taggedMorps = HannanumWrapper.toTaggedMorps(taggedWords);
			System.out.println(JString.join(" ", taggedMorps));
		}

		nlp.unload();
	}
}
