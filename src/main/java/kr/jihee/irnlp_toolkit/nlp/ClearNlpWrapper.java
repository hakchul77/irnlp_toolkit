/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.nlp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipFile;

import kr.jihee.irnlp_toolkit.Env;

import org.apache.log4j.PropertyConfigurator;

import com.clearnlp.component.dep.AbstractDEPParser;
import com.clearnlp.component.pos.AbstractPOSTagger;
import com.clearnlp.component.pred.AbstractPredicateIdentifier;
import com.clearnlp.component.role.AbstractRolesetClassifier;
import com.clearnlp.component.srl.AbstractSRLabeler;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPLib;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.dependency.srl.SRLArc;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.nlp.NLPMode;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.clearnlp.util.pair.ObjectDoublePair;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * Wrapper of Stanford CoreNLP 3.3.1<br>
 * = URL : http://nlp.stanford.edu/software/corenlp.shtml
 * 
 * @author Jihee
 */
public class ClearNlpWrapper {

	public Properties prop;
	public AbstractTokenizer tokenizer;
	public AbstractSegmenter detector;
	public AbstractPOSTagger tagger;
	public AbstractDEPParser parser;
	public AbstractPredicateIdentifier identifier;
	public AbstractRolesetClassifier classifier;
	public AbstractSRLabeler labeler;

	/**
	 * SRL label data
	 * 
	 * @author Jihee
	 */
	public static class SRLNode {

		public DEPNode node = null;
		public int depth = -1;

		public SRLNode(DEPNode node, int depth) {
			this.node = node;
			this.depth = depth;
		}

		public Map<String, Object> toMap() {
			return ClearNlpWrapper.toMap(node, depth);
		}

		public String toString() {
			return this.toMap().toString();
		}
	}

	public ClearNlpWrapper(String prop_file) throws IOException {
		PropertyConfigurator.configure(Env.LOG4J_CFG);
		prop = new Properties();
		prop.loadFromXML(new FileInputStream(prop_file));
	}

	public void loadTokenizer() throws IOException {
		tokenizer = NLPGetter.getTokenizer(AbstractReader.LANG_EN);
	}

	public void loadSentDetector() throws IOException {
		detector = NLPGetter.getSegmenter(AbstractReader.LANG_EN, tokenizer);
	}

	public void loadPosTagger() throws IOException {
		String model_path = prop.getProperty("pos.model");
		if (!model_path.toLowerCase().endsWith(".zip"))
			tagger = (AbstractPOSTagger) NLPGetter.getComponent(model_path, AbstractReader.LANG_EN, NLPMode.MODE_POS);
		else
			tagger = (AbstractPOSTagger) NLPGetter.getComponent(new ZipFile(model_path), AbstractReader.LANG_EN, NLPMode.MODE_POS);
	}

	public void loadDepParser() throws IOException {
		String model_path = prop.getProperty("dep.model");
		if (!model_path.toLowerCase().endsWith(".zip"))
			parser = (AbstractDEPParser) NLPGetter.getComponent(model_path, AbstractReader.LANG_EN, NLPMode.MODE_DEP);
		else
			parser = (AbstractDEPParser) NLPGetter.getComponent(new ZipFile(model_path), AbstractReader.LANG_EN, NLPMode.MODE_DEP);
	}

	private void loadPredIdentifier() throws IOException {
		String model_path = prop.getProperty("pred.model");
		if (!model_path.toLowerCase().endsWith(".zip"))
			identifier = (AbstractPredicateIdentifier) NLPGetter.getComponent(model_path, AbstractReader.LANG_EN, NLPMode.MODE_PRED);
		else
			identifier = (AbstractPredicateIdentifier) NLPGetter.getComponent(new ZipFile(model_path), AbstractReader.LANG_EN, NLPMode.MODE_PRED);
	}

	private void loadRoleClassifier() throws IOException {
		String model_path = prop.getProperty("role.model");
		if (!model_path.toLowerCase().endsWith(".zip"))
			classifier = (AbstractRolesetClassifier) NLPGetter.getComponent(model_path, AbstractReader.LANG_EN, NLPMode.MODE_ROLE);
		else
			classifier = (AbstractRolesetClassifier) NLPGetter.getComponent(new ZipFile(model_path), AbstractReader.LANG_EN, NLPMode.MODE_ROLE);
	}

	public void loadSrlLabeler() throws IOException {
		loadPredIdentifier();
		loadRoleClassifier();
		String model_path = prop.getProperty("srl.model");
		if (!model_path.toLowerCase().endsWith(".zip"))
			labeler = (AbstractSRLabeler) NLPGetter.getComponent(model_path, AbstractReader.LANG_EN, NLPMode.MODE_SRL);
		else
			labeler = (AbstractSRLabeler) NLPGetter.getComponent(new ZipFile(model_path), AbstractReader.LANG_EN, NLPMode.MODE_SRL);
	}

	public void loadAll() throws IOException {
		loadAll("tokenize, ssplit, pos, parse, srl");
	}

	public void loadAll(String annotator_spec) throws IOException {
		List<String> annotators = Arrays.asList(annotator_spec.toLowerCase().replaceAll("\\s", "").split(","));
		if (annotators.contains("tokenize"))
			loadTokenizer();
		if (annotators.contains("ssplit"))
			loadSentDetector();
		if (annotators.contains("pos"))
			loadPosTagger();
		if (annotators.contains("parse"))
			loadDepParser();
		if (annotators.contains("srl"))
			loadSrlLabeler();
	}

	public List<String> tokenize(String sent) {
		return tokenizer.getTokens(sent);
	}

	public List<List<String>> detect(String text) {
		return detector.getSentences(new BufferedReader(new StringReader(text)));
	}

	public DEPTree tag(DEPTree units) {
		tagger.process(units);
		return units;
	}

	public DEPTree parse(DEPTree units) {
		parser.process(units);
		return units;
	}

	public List<DEPTree> parse(DEPTree units, int k) {
		List<ObjectDoublePair<DEPTree>> parsed_pairs = parser.getParsedTrees(units, true);
		List<DEPTree> parsed_trees = new ArrayList<DEPTree>();
		for (int i = 0; i < Math.min(k, parsed_pairs.size()); i++)
			parsed_trees.add((DEPTree) parsed_pairs.get(i).o);
		return parsed_trees;
	}

	public DEPTree label(DEPTree units) {
		identifier.process(units);
		classifier.process(units);
		labeler.process(units);
		return units;
	}

	/**
	 * Transform a DEPNode instance into a Word instance
	 * 
	 * @param unit
	 * @return
	 */
	public static Word toWord(DEPNode unit) {
		return new Word(unit.form);
	}

	/**
	 * Transform a DEPNode instance into a TaggedWord instance
	 * 
	 * @param unit
	 * @return
	 */
	public static TaggedWord toTaggedWord(DEPNode unit) {
		return new TaggedWord(unit.form, unit.pos);
	}

	/**
	 * Transform a DEPNode instance into a CoreLabel instance
	 * 
	 * @param unit
	 * @return
	 */
	public static IndexedWord toIndexedWord(DEPNode unit) {
		IndexedWord new_unit = new IndexedWord();
		new_unit.setIndex(unit.id);
		new_unit.setValue(unit.form);
		new_unit.setWord(unit.form);
		new_unit.setTag(unit.pos);
		new_unit.setLemma(unit.lemma);
		new_unit.set(TreeCoreAnnotations.HeadTagAnnotation.class, new TreeGraphNode(new StringLabel(unit.pos)));
		return new_unit;
	}

	/**
	 * Transform a DEPNode instance into a TypedDependency instance
	 * 
	 * @param unit
	 * @return
	 */
	public static TypedDependency toTypedDependency(DEPNode unit) {
		if (!unit.hasHead())
			return null;
		GrammaticalRelation reln = StanfordNlpWrapper.getGrammaticalRelation(unit.getLabel());
		TreeGraphNode gov = new TreeGraphNode(toIndexedWord(unit.getHead()));
		TreeGraphNode dep = new TreeGraphNode(toIndexedWord(unit));
		return new TypedDependency(reln, gov, dep);
	}

	/**
	 * Transform a SRLArc instance and a DEPNode instance into a TypedDependency instance
	 * 
	 * @param sarc
	 * @param unit
	 * @return
	 */
	public static TypedDependency toTypedDependency(SRLArc sarc, DEPNode unit) {
		if (!unit.hasHead())
			return null;
		String label = sarc.getLabel();
		if (!sarc.getFunctionTag().isEmpty())
			label += "_" + sarc.getFunctionTag();
		GrammaticalRelation reln = StanfordNlpWrapper.getGrammaticalRelation(label);
		TreeGraphNode gov = new TreeGraphNode(toIndexedWord(sarc.getNode()));
		TreeGraphNode dep = new TreeGraphNode(toIndexedWord(unit));
		return new TypedDependency(reln, gov, dep);
	}

	/**
	 * Transform a DEPNode instance into a Map instance
	 * 
	 * @param unit
	 * @return
	 */
	public static Map<String, Object> toMap(DEPNode unit) {
		return toMap(unit, 0);
	}

	/**
	 * Transform a DEPNode instance into a Map instance
	 * 
	 * @param unit
	 * @param depth
	 * @return
	 */
	public static Map<String, Object> toMap(DEPNode unit, int depth) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("id", unit.id);
		map.put("form", unit.form);
		map.put("lemma", unit.lemma);
		map.put("pos", unit.pos);
		map.put("depth", depth);
		if (unit.hasHead()) {
			map.put("drel", unit.getLabel());
			map.put("governor", unit.getHead().id);
			map.put("pb", unit.getFeat(DEPLib.FEAT_PB));
			if (unit.getSHead(unit.getHead()) != null) {
				map.put("srel", unit.getSHead(unit.getHead()).getLabel());
				map.put("sfunc", unit.getSHead(unit.getHead()).getFunctionTag());
			}
		}
		return map;
	}

	/**
	 * Transform a tokinized DEPTree instance into a list of Word instances
	 * 
	 * @param units
	 * @return
	 */
	public static List<Word> toWords(DEPTree units) {
		ArrayList<Word> new_units = new ArrayList<Word>();
		for (int i = 1; i < units.size(); i++)
			new_units.add(toWord(units.get(i)));
		return new_units;
	}

	/**
	 * Transform a POS tagged DEPTree instance into a list of TaggedWord instances
	 * 
	 * @param units
	 * @return
	 */
	public static List<TaggedWord> toTaggedWords(DEPTree units) {
		ArrayList<TaggedWord> new_units = new ArrayList<TaggedWord>();
		for (int i = 1; i < units.size(); i++)
			new_units.add(toTaggedWord(units.get(i)));
		return new_units;
	}

	/**
	 * Transform a dependency parsed DEPTree instance into a list of TypedDependency instances
	 * 
	 * @param units
	 * @return
	 */
	public static List<TypedDependency> toTypedDependencies(DEPTree units) {
		ArrayList<TypedDependency> new_units = new ArrayList<TypedDependency>();
		for (int i = 1; i < units.size(); i++)
			new_units.add(toTypedDependency(units.get(i)));
		return new_units;
	}

	/**
	 * Transform a semantic role labelled DEPTree instance into a list of TypedDependency instances
	 * 
	 * @param units
	 * @return
	 */
	public static List<TypedDependency> toSemanticRoleLabels(DEPTree units) {
		ArrayList<TypedDependency> new_units = new ArrayList<TypedDependency>();
		for (int i = 1; i < units.size(); i++)
			for (SRLArc sarc : units.get(i).getSHeads())
				new_units.add(toTypedDependency(sarc, units.get(i)));
		return new_units;
	}

	/**
	 * Extract all the verbs from a SRL labelled sentence
	 * 
	 * @param units
	 * @return
	 */
	public static List<DEPNode> getAllVerbs(DEPTree units) {
		ArrayList<DEPNode> verbs = new ArrayList<DEPNode>();
		for (DEPNode unit : units)
			if (unit.getFeat(DEPLib.FEAT_PB) != null)
				verbs.add(unit);
		return verbs;
	}

	/**
	 * Extract all the dependent units from a DEP parsed sentence
	 * 
	 * @param unit
	 * @return
	 */
	public static List<SRLNode> getDependents(DEPNode unit) {
		return getDependents(unit, false);
	}

	/**
	 * Extract all the dependent units from a DEP parsed sentence
	 * 
	 * @param unit
	 * @param inclSelf
	 * @return
	 */
	public static List<SRLNode> getDependents(DEPNode unit, boolean inclSelf) {
		return getDependents(unit, inclSelf, 1);
	}

	/**
	 * Extract all the dependent units from a DEP parsed sentence
	 * 
	 * @param unit
	 * @param inclSelf
	 * @param limit
	 * @return
	 */
	public static List<SRLNode> getDependents(DEPNode unit, boolean inclSelf, int limit) {
		return getDependents(unit, inclSelf, limit, 0);
	}

	/**
	 * Extract all the dependent units from a DEP parsed sentence
	 * 
	 * @param unit
	 * @param inclSelf
	 * @param limit
	 * @param depth
	 * @return
	 */
	public static List<SRLNode> getDependents(DEPNode unit, boolean inclSelf, int limit, int depth) {
		List<SRLNode> deps = new ArrayList<SRLNode>();
		if (inclSelf)
			deps.add(new SRLNode(unit, depth));

		if (depth + 1 <= limit)
			for (DEPNode dep : unit.getDependentNodeList()) {
				deps.add(new SRLNode(dep, depth + 1));
				deps.addAll(getDependents(dep, false, limit, depth + 1));
			}

		return deps;
	}

	/**
	 * Extract all the dependent units from a DEP parsed sentence
	 * 
	 * @param unit
	 * @return
	 */
	public static List<Map<String, Object>> getDependents2(DEPNode unit) {
		return getDependents2(unit, false);
	}

	/**
	 * Extract all the dependent units from a DEP parsed sentence
	 * 
	 * @param unit
	 * @param inclSelf
	 * @return
	 */
	public static List<Map<String, Object>> getDependents2(DEPNode unit, boolean inclSelf) {
		return getDependents2(unit, inclSelf, 1);
	}

	/**
	 * Extract all the dependent units from a DEP parsed sentence
	 * 
	 * @param unit
	 * @param inclSelf
	 * @param limit
	 * @return
	 */
	public static List<Map<String, Object>> getDependents2(DEPNode unit, boolean inclSelf, int limit) {
		return getDependents2(unit, inclSelf, limit, 0);
	}

	/**
	 * Extract all the dependent units from a DEP parsed sentence
	 * 
	 * @param unit
	 * @param inclSelf
	 * @param limit
	 * @param depth
	 * @return
	 */
	public static List<Map<String, Object>> getDependents2(DEPNode unit, boolean inclSelf, int limit, int depth) {
		List<Map<String, Object>> dep_infos = new ArrayList<Map<String, Object>>();
		if (inclSelf)
			dep_infos.add(toMap(unit, depth));

		if (depth + 1 <= limit)
			for (DEPNode dep : unit.getDependentNodeList()) {
				dep_infos.add(toMap(dep, depth + 1));
				dep_infos.addAll(getDependents2(dep, false, limit, depth + 1));
			}

		return dep_infos;
	}

	/**
	 * Replace old POS tags into new POS tags
	 * 
	 * @param units old POS tags
	 * @param new_units new POS tags
	 */
	public static void replacePos(DEPTree units, List<TaggedWord> new_units) {
		for (int i = 1; i < units.size(); i++)
			units.get(i).pos = new_units.get(i - 1).tag();
	}

	/**
	 * Replace old dependency arcs into new dependency arcs
	 * 
	 * @param units
	 * @param new_units
	 */
	public static void replaceDep(DEPTree units, List<TypedDependency> new_units) {
		for (TypedDependency new_unit : new_units) {
			DEPNode gov = units.get(new_unit.gov().label().index());
			DEPNode dep = units.get(new_unit.dep().label().index());
			String reln = new_unit.reln().toString();
			dep.setHead(new DEPArc(gov, reln));
		}
	}
}
