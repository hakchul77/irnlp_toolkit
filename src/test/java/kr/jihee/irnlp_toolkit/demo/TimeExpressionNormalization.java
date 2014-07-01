/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.demo;

import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.nlp.StanfordNlpWrapper;
import kr.jihee.java_toolkit.io.JFile;
import kr.jihee.java_toolkit.io.JXml;

import org.w3c.dom.Element;

/**
 * Demo code for Time Expression Normalization
 * 
 * @author Jihee
 */
public class TimeExpressionNormalization {

	/**
	 * Main function
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// data input
		String text = JFile.read(Env.SAMPLE_DIR + "news.txt");
		String date = "2014-09-26";

		// model loading
		StanfordNlpWrapper nlp = new StanfordNlpWrapper(Env.STANFORDNLP_CFG);
		nlp.loadTimeAnnotator();

		// task run
		System.out.println("-XML document-------------------------------------------------------------------");
		JXml xml = new JXml(nlp.normalizeTime(text, date));
		System.out.println(xml.toString());
		
		System.out.println("-TIMEX3 elements----------------------------------------------------------------");
		for (Element timex : xml.findElements("DOC/TEXT/TIMEX3"))
			System.out.println(JXml.toNodeString(timex));
	}
}
