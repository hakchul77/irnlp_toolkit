/**
 * 
 */
package kr.jihee.irnlp_toolkit.ml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

import kr.jihee.java_toolkit.util.JString.JEntry;
import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFTrainerByLabelLikelihood;
import cc.mallet.fst.MaxLatticeDefault;
import cc.mallet.fst.SimpleTagger.SimpleTaggerSentence2FeatureVectorSequence;
import cc.mallet.fst.Transducer;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SaveDataInSource;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.pipe.iterator.LineGroupIterator;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.FeatureVectorSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Sequence;

/**
 * Wrapper of Mallet 2.0.7<br>
 * - URL : http://mallet.cs.umass.edu/
 * 
 * @author Jihee
 */
public class MalletWrapper {

	/**
	 * Wrapper of CRF in Mallet
	 * 
	 * @author Jihee
	 */
	public static class MalletCRFWrapper {

		public static double DEFAULT_PRIOR_VARIANCE = 10.0;
		public static String DEFAULT_LABEL = "O";
		public static boolean DEFAULT_TARGET_PROCESSING = true;

		public InstanceList train_data;
		public InstanceList test_data;
		public CRF model;

		/**
		 * 
		 * @param data_file
		 * @throws FileNotFoundException
		 */
		public void setTrainData(String data_file) throws FileNotFoundException {
			this.train_data = getInstanceList(data_file);
		}

		/**
		 * 
		 * @param data
		 */
		public void setTrainData(String[] data) {
			this.train_data = getInstanceList(data);
		}

		/**
		 * 
		 * @param data_file
		 * @throws FileNotFoundException
		 */
		public void setTestData(String data_file) throws FileNotFoundException {
			this.test_data = getInstanceList(data_file);
		}

		/**
		 * 
		 * @param data
		 */
		public void setTestData(String[] data) {
			this.test_data = getInstanceList(data);
		}

		/**
		 * 
		 * @param data_file
		 * @return
		 * @throws FileNotFoundException
		 */
		public InstanceList getInstanceList(String data_file) throws FileNotFoundException {
			InstanceList instances = new InstanceList(getPipe());
			instances.addThruPipe(new LineGroupIterator(new FileReader(new File(data_file)), Pattern.compile("^\\s*$"), true));
			return instances;
		}

		/**
		 * 
		 * @param data
		 * @return
		 */
		public InstanceList getInstanceList(String[] data) {
			InstanceList instances = new InstanceList(getPipe());
			instances.addThruPipe(new StringArrayIterator(data));
			return instances;
		}

		/**
		 * 
		 * @return
		 */
		private Pipe getPipe() {
			Pipe p = (this.model == null) ? new SimpleTaggerSentence2FeatureVectorSequence() : this.model.getInputPipe();
			p.setTargetProcessing(DEFAULT_TARGET_PROCESSING);
			return p;
		}

		/**
		 * 
		 * @param num_iterations
		 * @return
		 */
		public void train(int num_iterations) {
			model = new CRF(this.train_data.getPipe(), (Pipe) null);
			for (int i = 0; i < model.numStates(); i++)
				model.getState(i).setInitialWeight(Transducer.IMPOSSIBLE_WEIGHT);
			String startName = model.addOrderNStates(this.train_data, new int[] { 1 }, null, DEFAULT_LABEL, Pattern.compile("\\s"), Pattern.compile(".*"), true);
			model.getState(startName).setInitialWeight(0.0);

			CRFTrainerByLabelLikelihood crft = new CRFTrainerByLabelLikelihood(model);
			crft.setGaussianPriorVariance(DEFAULT_PRIOR_VARIANCE);
			crft.setUseSparseWeights(true);
			crft.setUseSomeUnsupportedTrick(true);

			for (int i = 1; i <= num_iterations; i++)
				if (crft.train(this.train_data, 1))
					break;
		}

		/**
		 * 
		 * @param num_best
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public List<CRFResult> test(int num_best) {
			List<CRFResult> groups = new ArrayList<CRFResult>();
			for (Instance instance : this.test_data) {
				FeatureVectorSequence input = (FeatureVectorSequence) instance.getData();
				List<Sequence<Object>> seqs = null;
				if (num_best > 1)
					seqs = new MaxLatticeDefault(model, input, null, 100000).bestOutputSequences(num_best);
				else
					seqs = Arrays.asList((Sequence<Object>) model.transduce(input));
				if (isError(seqs, input))
					System.err.println("[ERROR] Error output at " + input);

				List<List<String>> outputs = new ArrayList<List<String>>();
				for (Sequence<Object> seq : seqs)
					outputs.add(toTagStrings(seq));

				groups.add(new CRFResult(input, outputs));
			}

			return groups;
		}

		/**
		 * 
		 * @param seqs
		 * @param input
		 * @return
		 */
		private boolean isError(List<Sequence<Object>> seqs, FeatureVectorSequence input) {
			for (Sequence<Object> seq : seqs)
				if (seq.size() != input.size())
					return true;
			return false;
		}

		/**
		 * Transform a sequence output into a list of tag strings
		 * 
		 * @param output
		 * @return
		 */
		public static List<String> toTagStrings(Sequence<Object> output) {
			List<String> tags = new ArrayList<String>();
			for (int i = 0; i < output.size(); i++)
				tags.add(output.get(i).toString());
			return tags;
		}
	}

	/**
	 * Wrapper of LDA in Mallet
	 * 
	 * @author Jihee
	 */
	public static class MalletLDAWrapper {

		public static int DEFAULT_NUM_KEYWORDS = 20;
		public static double DEFAULT_ALPHA_SUM = 50.0;
		public static double DEFAULT_BETA = 0.01;

		public String keys_file;
		public InstanceList data;
		public ParallelTopicModel model;

		/**
		 * Constructor
		 * 
		 * @param keys_file
		 */
		public MalletLDAWrapper(String keys_file) {
			this.keys_file = keys_file;
		}

		/**
		 * 
		 * @param data_dir
		 */
		public void setInputData(String data_dir) {
			this.data = getInstanceList(data_dir);
		}

		/**
		 * 
		 * @param data
		 */
		public void setInputData(JEntry[] data) {
			this.data = getInstanceList(data);
		}

		/**
		 * 
		 * @param data_dir
		 * @return
		 */
		public InstanceList getInstanceList(String data_dir) {
			InstanceList instances = new InstanceList(getPipe());
			instances.addThruPipe(new FileIterator(new File[] { new File(data_dir) }, FileIterator.STARTING_DIRECTORIES, true));
			return instances;
		}

		/**
		 * 
		 * @param data
		 * @param model
		 * @return
		 */
		public InstanceList getInstanceList(JEntry[] data) {
			InstanceList instances = new InstanceList(getPipe());
			instances.addThruPipe(new JEntryArrayIterator(data));
			return instances;
		}

		/**
		 * 
		 * @param model
		 * @param targetProcessing
		 * @return
		 */
		private Pipe getPipe() {
			ArrayList<Pipe> pipes = new ArrayList<Pipe>();
			pipes.add(new Target2Label());
			pipes.add(new SaveDataInSource());
			pipes.add(new Input2CharSequence("UTF-8"));
			pipes.add(new CharSequence2TokenSequence(Pattern.compile("\\p{Alpha}+")));
			pipes.add(new TokenSequenceLowercase());
			pipes.add(new TokenSequenceRemoveStopwords(false, false));
			pipes.add(new TokenSequence2FeatureSequence());
			// pipes.add(new PrintInputAndTarget());
			return new SerialPipes(pipes);
		}

		/**
		 * 
		 * @param num_clusters
		 * @param num_iterations
		 * @return
		 * @throws IOException
		 */
		public List<LDAResult> cluster(int num_clusters, int num_iterations) throws IOException {
			model = new ParallelTopicModel(num_clusters, DEFAULT_ALPHA_SUM, DEFAULT_BETA);
			model.addInstances(this.data);
			model.setTopicDisplay(500, DEFAULT_NUM_KEYWORDS);
			model.setNumIterations(num_iterations);
			model.setOptimizeInterval(0);
			model.setBurninPeriod(200);
			model.setSymmetricAlpha(false);
			model.setNumThreads(1);
			model.estimate();

			if (this.keys_file != null)
				model.printTopWords(new File(this.keys_file), DEFAULT_NUM_KEYWORDS, false);

			IDSorter[] outputs = new IDSorter[num_clusters];
			List<LDAResult> groups = new ArrayList<LDAResult>();
			for (int id = 0; id < model.data.size(); id++) {
				TopicAssignment result = model.data.get(id);
				String name = result.instance.getName().toString();
				FeatureSequence input = (FeatureSequence) result.instance.getData();

				TreeMap<Integer, Integer> topic_counts = new TreeMap<Integer, Integer>();
				for (int topic = 0; topic < num_clusters; topic++)
					topic_counts.put(topic, 0);
				for (int topic : result.topicSequence.getFeatures())
					topic_counts.put(topic, topic_counts.get(topic) + 1);

				for (int topic = 0; topic < num_clusters; topic++) {
					double prob = (topic_counts.get(topic) + model.alpha[topic]) / (result.topicSequence.getFeatures().length + model.alphaSum);
					outputs[topic] = new IDSorter(topic, prob);
				}
				Arrays.sort(outputs);

				groups.add(new LDAResult(id, name, input, outputs));
			}

			return groups;
		}
	}

	/**
	 * Class of a result item of CRF
	 */
	public static class CRFResult {
		public FeatureVectorSequence input;
		public List<List<String>> outputs;

		public CRFResult(FeatureVectorSequence input, List<List<String>> outputs) {
			this.input = input;
			this.outputs = outputs;
		}
	}

	/**
	 * Class of a result item of LDA
	 */
	public static class LDAResult {
		public Integer id;
		public String name;
		public FeatureSequence input;
		public List<IDSorter> outputs;

		public LDAResult(int id, String name, FeatureSequence input, IDSorter[] outputs) {
			this.id = id;
			this.name = name;
			this.input = input;
			this.outputs = new ArrayList<IDSorter>();
			for (IDSorter output : outputs)
				this.outputs.add(new IDSorter(output.getID(), output.getWeight()));
		}
	}

	/**
	 * Class of an iterator of JEntry array
	 */
	public static class JEntryArrayIterator implements Iterator<Instance> {
		JEntry[] data;
		int index;

		public JEntryArrayIterator(JEntry[] data) {
			this.data = data;
			this.index = 0;
		}

		public boolean hasNext() {
			return index < data.length;
		}

		public Instance next() {
			return new Instance(data[index].getValue(), "MyTarget", data[index++].getKey(), null);
		}

		public void remove() {
			throw new IllegalStateException("This Iterator<Instance> does not support remove().");
		}
	}
}
