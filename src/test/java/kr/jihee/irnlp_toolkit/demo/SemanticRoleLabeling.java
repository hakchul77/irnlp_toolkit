/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.demo;

import java.util.List;

import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.ClearNlpWrapper;
import kr.jihee.irnlp_toolkit.nlp.ClearNlpWrapper.SRLNode;
import kr.jihee.java_toolkit.util.JString;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;

/**
 * Demo code for Semantic Role Labeling (SRL)
 * 
 * @author Jihee
 */
public class SemanticRoleLabeling {

	/**
	 * Main function
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// data input
		String text = "Samsung Electronics is a South Korean multinational electronics company in Suwon, South Korea.";

		// model loading
		ClearNlpWrapper nlp = new ClearNlpWrapper(Env.CLEARNLP_CFG);
		nlp.loadAll("tokenize, ssplit, pos, parse, srl");

		// task run
		for (List<String> toks : nlp.detect(text)) {
			DEPTree units = NLPGetter.toDEPTree(toks);
			units = nlp.tag(units);
			units = nlp.parse(units);
			units = nlp.label(units);
			
			System.out.println("-toStringSRL--------------------------------------------------------------------");
			System.out.println(units.toStringSRL());
			
			System.out.println("-dependent Nodes----------------------------------------------------------------");
			StringBuffer sb = new StringBuffer();
			for (DEPNode verb : ClearNlpWrapper.getAllVerbs(units)) {
				sb.append("->(Verb) " + ClearNlpWrapper.toTaggedWord(verb) + "\n");
				for (SRLNode dep : ClearNlpWrapper.getDependents(verb))
					sb.append("  ->(Node) " + dep + "\n");
			}
			System.out.println(JString.trimAndIndent(sb.toString(), 2));
		}
	}
}
