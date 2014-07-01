/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.demo;

import kr.jihee.irnlp_toolkit.Env;
import kr.jihee.irnlp_toolkit.ml.MalletWrapper.CRFResult;
import kr.jihee.irnlp_toolkit.ml.MalletWrapper.MalletCRFWrapper;

/**
 * Demo code for Sequence Tagging using Conditional Random Fields (CRF)
 * 
 * @author Jihee
 */
public class SequenceTagging {

	/**
	 * Main function
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// data setting
		String train_file = Env.SAMPLE_DIR + "np_train.txt";
		String test_file = Env.SAMPLE_DIR + "np_test.txt";
		MalletCRFWrapper.DEFAULT_TARGET_PROCESSING = true;

		// training
		System.out.println("-Training-----------------------------------------------------------------------");
		MalletCRFWrapper crf = new MalletCRFWrapper();
		crf.setTrainData(train_file);
		crf.train(500);

		// test
		System.out.println("-Test---------------------------------------------------------------------------");
		crf.setTestData(test_file);
		for (CRFResult result : crf.test(1)) {
			System.out.println(result.input.toString().trim());
			System.out.println(String.join(" ", result.outputs.get(0)));
			System.out.println();
		}
	}
}
