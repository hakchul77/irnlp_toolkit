/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.demo;

import java.util.ArrayList;
import java.util.List;

import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.ml.MalletWrapper.LDAResult;
import kr.jihee.irnlp_toolkit.ml.MalletWrapper.MalletLDAWrapper;
import kr.jihee.java_toolkit.util.JString;
import cc.mallet.types.IDSorter;

/**
 * Demo code for Topic Modeling using Latent Dirichlet Allocation (LDA)
 * 
 * @author Jihee
 */
public class TopicModeling {

	/**
	 * Main function
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// file setting
		String keys_file = Env.SAMPLE_DIR + "keys.txt";
		String docs_dir = Env.SAMPLE_DIR + "wikipedia";
		int num_topics = 5;
		int num_iterations = 1000;

		// input
		System.out.println("-Input--------------------------------------------------------------------------");
		MalletLDAWrapper lda = new MalletLDAWrapper(keys_file);
		lda.setInputData(docs_dir);

		// clustering
		System.out.println("-Clustering---------------------------------------------------------------------");
		MalletLDAWrapper.DEFAULT_NUM_KEYWORDS = 50;
		for (LDAResult result : lda.cluster(num_topics, num_iterations)) {
			List<String> dists = new ArrayList<String>();
			for (IDSorter scoredTopic : result.outputs)
				dists.add(String.format("%d(%.4f)", scoredTopic.getID(), scoredTopic.getWeight()));
			result.name = result.name.replaceAll("file:/.+/(.+)", "$1");
			System.out.println(JString.join("\t", result.id, result.name, JString.join("\t", dists)));
		}
	}
}
