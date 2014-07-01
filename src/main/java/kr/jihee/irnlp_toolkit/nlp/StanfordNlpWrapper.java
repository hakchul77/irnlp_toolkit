/**
 * Natural Language Processing package
 */
package kr.jihee.irnlp_toolkit.nlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import kr.jihee.java_toolkit.util.JString;

import com.google.common.collect.ImmutableList;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.BeginIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.EndIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.KBestViterbiParser;
import edu.stanford.nlp.parser.lexparser.Debinarizer;
import edu.stanford.nlp.parser.lexparser.EnglishTreebankParserParams;
import edu.stanford.nlp.parser.lexparser.ExhaustivePCFGParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.time.SUTimeMain;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalRelation.Language;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.Filters;
import edu.stanford.nlp.util.ScoredObject;

/**
 * Wrapper of Stanford CoreNLP 3.3.1<br>
 * = URL : http://nlp.stanford.edu/software/corenlp.shtml
 * 
 * @author Jihee
 */
public class StanfordNlpWrapper {

	public Properties prop;
	public TokenizerFactory<Word> tokenizerFactory;
	public MaxentTagger tagger;
	public LexicalizedParser parser;
	public KBestViterbiParser parserK;
	public StanfordCoreNLP annotator;
	public AnnotationPipeline normalizer;

	private static final List<GrammaticalRelation> COMMON_RELATIONS = Arrays.asList(GrammaticalRelation.ROOT, GrammaticalRelation.DEPENDENT, GrammaticalRelation.GOVERNOR, GrammaticalRelation.KILL);

	/**
	 * Token data
	 * 
	 * @author Jihee
	 */
	public static class Token implements Cloneable, Serializable {

		private static final long serialVersionUID = 6652331644626910117L;

		public String text;
		public TaggedWord word;
		public String lemma;
		public Integer index;
		public String ner;
		public Integer ref;

		public Token() {

		}

		public String toString() {
			return String.format("%s:%s:%s:[%d]:%s:%s", text, word, lemma, index, ner, ref);
		}

		public Token clone() {
			Token that = new Token();
			that.text = this.text;
			that.word = new TaggedWord(this.word.word(), this.word.tag());
			that.lemma = this.lemma;
			that.index = this.index; // 0-based index
			that.ner = this.ner;
			that.ref = this.ref;
			return that;
		}
	}

	/**
	 * Phrase data
	 * 
	 * @author Jihee
	 */
	public static class Phrase implements Cloneable, Serializable {

		private static final long serialVersionUID = -4405136685066833549L;

		public String category;
		public String text;
		public List<TaggedWord> words;
		public Integer beginIndex; // 0-based index
		public Integer endIndex; // 0-based index
		public Integer headIndex; // 0-based index
		public TaggedWord headWord;
		public String ner;

		public Phrase() {

		}

		public String toString() {
			return String.format("%s:%s:%s:[%d,%d):<%d=%s>:%s", category, text, words, beginIndex, endIndex, headIndex, headWord, ner);
		}

		public Phrase clone() {
			Phrase that = new Phrase();
			that.category = this.category;
			that.text = this.text;
			ArrayList<TaggedWord> words = new ArrayList<TaggedWord>();
			for (TaggedWord word : this.words)
				words.add(new TaggedWord(word.word(), word.tag()));
			that.words = ImmutableList.copyOf(words);
			that.beginIndex = this.beginIndex;
			that.endIndex = this.endIndex;
			that.headIndex = this.headIndex;
			that.headWord = new TaggedWord(headWord.word(), headWord.tag());
			that.ner = this.ner;
			return that;
		}
	}

	public StanfordNlpWrapper(String prop_file) throws IOException {
		prop = new Properties();
		prop.loadFromXML(new FileInputStream(prop_file));
	}

	public void loadTokFactory() {
		tokenizerFactory = PTBTokenizer.factory();
	}

	public void loadPosTagger() {
		tagger = new MaxentTagger(prop.getProperty("pos.model"));
	}

	public void loadLexParser() {
		parser = LexicalizedParser.getParserFromFile(prop.getProperty("parse.model"), new Options());
		parserK = new ExhaustivePCFGParser(parser.bg, parser.ug, parser.getLexicon(), parser.getOp(), parser.stateIndex, parser.wordIndex, parser.tagIndex);
	}

	public void loadAll() {
		annotator = new StanfordCoreNLP(prop);
	}

	public void loadAll(String annotator_spec) {
		prop.setProperty("annotators", annotator_spec);
		annotator = new StanfordCoreNLP(prop);
	}

	public void loadTimeAnnotator() throws Exception {
		System.setProperty("pos.model", prop.getProperty("pos.model"));
		normalizer = SUTimeMain.getPipeline(prop, true);
	}

	public List<Word> tokenize(String text) {
		return tokenizerFactory.getTokenizer(new StringReader(text)).tokenize();
	}

	public static List<List<HasWord>> detect(String text) {
		return MaxentTagger.tokenizeText(new StringReader(text));
	}

	public List<TaggedWord> tag(String text) {
		return tag(tokenize(text));
	}

	public List<TaggedWord> tag(List<? extends HasWord> words) {
		return tagger.tagSentence(words);
	}

	public Tree parse(String text) {
		return parser.parse(text);
	}

	public Tree parse(List<? extends HasWord> words) {
		return parser.parse(words);
	}

	public List<Tree> parse(List<? extends HasWord> words, int k) {
		parserK.parse(words);
		ArrayList<Tree> trees = new ArrayList<Tree>();
		for (ScoredObject<Tree> tree : parserK.getKBestParses(k))
			trees.add(new EnglishTreebankParserParams().subcategoryStripper().transformTree(new Debinarizer(false).transformTree(tree.object())));
		return trees;
	}

	public Annotation annotate(String text) {
		Annotation annotation = new Annotation(text);
		try {
			annotator.annotate(annotation);
			return annotation;
		} catch (Exception e) {
			try {
				annotator.annotate(annotation);
				return annotation;
			} catch (Exception e2) {
				return null;
			}
		}
	}

	public String normalizeTime(String text, String date) {
		try {
			return SUTimeMain.textToAnnotatedXml(normalizer, text, date);
		} catch (Exception e) {
			try {
				return SUTimeMain.textToAnnotatedXml(normalizer, text, date);
			} catch (Exception e2) {
				return null;
			}
		}
	}

	/**
	 * Transform an Annotation instance into a pretty string
	 * 
	 * @param annotation
	 * @return
	 */
	public String toPrettyStr(Annotation annotation) {
		StringWriter sw = new StringWriter();
		annotator.prettyPrint(annotation, new PrintWriter(sw));
		return sw.toString().trim();
	}

	/**
	 * Transform an Annotation instance into an XML string
	 * 
	 * @param annotation
	 * @return
	 * @throws IOException
	 */
	public String toXml(Annotation annotation) throws IOException {
		StringWriter sw = new StringWriter();
		annotator.xmlPrint(annotation, new PrintWriter(sw));
		return sw.toString().trim();
	}

	/**
	 * Transform a parse tree into a format
	 * 
	 * @param tree
	 * @param format
	 * @return
	 */
	public static String toTreeString(Tree tree, String format) {
		StringWriter sw = new StringWriter();
		new TreePrint(format).printTree(tree, new PrintWriter(sw));
		return sw.toString().trim();
	}

	/**
	 * Transform a parse tree into a format
	 * 
	 * - example options: basicDependencies=true,includePunctuationDependencies=true
	 * 
	 * @param tree
	 * @param format
	 * @param options
	 * @return
	 */
	public static String toTreeString(Tree tree, String format, String options) {
		StringWriter sw = new StringWriter();
		new TreePrint(format, options, new PennTreebankLanguagePack()).printTree(tree, new PrintWriter(sw));
		return sw.toString().trim();
	}

	/**
	 * Transform a parse tree into a list of TypedDependency instances
	 * 
	 * @param tree
	 * @return
	 */
	public static List<TypedDependency> toTypedDependencies(Tree tree) {
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		Filter<String> filter = Filters.acceptFilter();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory(filter, tlp.typedDependencyHeadFinder());
		GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
		return (List<TypedDependency>) gs.typedDependencies();
	}

	/**
	 * Transform a list of TypedDependency instances into a SemanticGraph instance
	 * 
	 * @param dependencies
	 * @return
	 */
	public static SemanticGraph toSemanticGraph(Collection<TypedDependency> dependencies) {
		return toSemanticGraph(dependencies, true);
	}

	/**
	 * Transform a list of TypedDependency instances into a SemanticGraph instance
	 * 
	 * @param dependencies
	 * @param resetRoots
	 * @return
	 */
	public static SemanticGraph toSemanticGraph(Collection<TypedDependency> dependencies, boolean resetRoots) {
		SemanticGraph sgraph = new SemanticGraph(dependencies);
		if (resetRoots)
			sgraph.resetRoots();
		return sgraph;
	}

	/**
	 * Transform an Annotation instance into a map of coreference clusters
	 * 
	 * @param annotation
	 * @return
	 */
	public static Map<Integer, List<CorefMention>> toCoreferenceMap(Annotation annotation) {
		HashMap<Integer, List<CorefMention>> corefs = new HashMap<Integer, List<CorefMention>>();
		for (CorefChain chain : annotation.get(CorefChainAnnotation.class).values()) {
			CorefMention m1 = chain.getRepresentativeMention();
			corefs.put(m1.corefClusterID, new ArrayList<CorefMention>());
			corefs.get(m1.corefClusterID).add(m1);
			for (CorefMention m2 : chain.getMentionsInTextualOrder())
				if (m2 != m1)
					corefs.get(m2.corefClusterID).add(m2);
		}
		return corefs;
	}

	/**
	 * Transform a list of words into a list of token strings
	 * 
	 * @param words
	 * @return
	 */
	public static List<String> toTokenStrings(List<? extends HasWord> words) {
		ArrayList<String> tokens = new ArrayList<String>();
		for (HasWord w : words)
			tokens.add(w.word());
		return tokens;
	}

	/**
	 * Transform a parse tree into a list of token strings
	 * 
	 * @param root
	 * @return
	 */
	public static List<String> toTokenStrings(Tree root) {
		ArrayList<String> tokens = new ArrayList<String>();
		for (Tree node : root.getLeaves())
			tokens.add(node.value());
		return tokens;
	}

	/**
	 * Transform a CoreMap instance into a list of token strings
	 * 
	 * @param sentence
	 * @return
	 */
	public static List<String> toTokenStrings(CoreMap sentence) {
		ArrayList<String> tokens = new ArrayList<String>();
		for (CoreLabel tokInfo : sentence.get(TokensAnnotation.class))
			tokens.add(tokInfo.word());
		return tokens;
	}

	/**
	 * Transform a CoreMap instance into a list of token strings
	 * 
	 * @param sentence
	 * @return
	 */
	public static List<String> toTokenStrings(CoreMap sentence, String text) {
		ArrayList<String> tokens = new ArrayList<String>();
		for (CoreLabel tokInfo : sentence.get(TokensAnnotation.class))
			tokens.add(text.substring(tokInfo.beginPosition(), tokInfo.endPosition()));
		return tokens;
	}

	/**
	 * Transform a parse tree into a list of TaggedWord instances
	 * 
	 * @param root
	 * @return
	 */
	public static List<TaggedWord> toTaggedWords(Tree root) {
		ArrayList<TaggedWord> units = new ArrayList<TaggedWord>();
		for (Tree node : root.getLeaves())
			units.add(new TaggedWord(node.value(), node.parent(root).value()));
		return units;
	}

	/**
	 * Transform a CoreMap instance into a list of Token instances
	 * 
	 * @param sentence
	 * @param text
	 * @return
	 */
	public static List<Token> toTokens(CoreMap sentence, String text) {
		ArrayList<Token> tokens = new ArrayList<Token>();
		for (CoreLabel tokInfo : sentence.get(TokensAnnotation.class)) {
			Token t = new Token();
			t.text = text.substring(tokInfo.beginPosition(), tokInfo.endPosition());
			t.word = new TaggedWord(tokInfo.word(), tokInfo.tag());
			t.lemma = tokInfo.lemma();
			t.index = tokInfo.index() - 1; // 1-based index to 0-based index
			t.ner = tokInfo.ner();
			t.ref = tokInfo.get(CorefClusterIdAnnotation.class);
			tokens.add(t);
		}
		return tokens;
	}

	/**
	 * Transform a CoreMap instance into a list of Phrase instances
	 * 
	 * @param sentence
	 * @param text
	 * @return
	 */
	public static List<Phrase> toPhrases(CoreMap sentence, String text) {
		Tree root = sentence.get(TreeAnnotation.class);
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		ArrayList<Phrase> phrases = new ArrayList<Phrase>();
		for (Tree node : root.children())
			if (node.isPrePreTerminal() || node.isPreTerminal())
				phrases.add(toPhrase(node, graph, text));
			else if (node.isPhrasal())
				for (Phrase p : toPhrases(node, graph, text))
					phrases.add(p);
		return phrases;
	}

	/**
	 * Transform a parse tree node into a list of Phrase instances
	 * 
	 * @param node
	 * @param graph
	 * @param text
	 * @return
	 */
	public static List<Phrase> toPhrases(Tree node, SemanticGraph graph, String text) {
		ArrayList<Phrase> phrases = new ArrayList<Phrase>();
		for (Tree child : node.children())
			if (child.isPrePreTerminal() || child.isPreTerminal())
				phrases.add(toPhrase(child, graph, text));
			else if (child.isPhrasal())
				for (Phrase p : toPhrases(child, graph, text))
					phrases.add(p);
		return phrases;
	}

	/**
	 * Transform a parse tree node into a Phrase instances
	 * 
	 * @param node
	 * @param graph
	 * @param text
	 * @return
	 */
	public static Phrase toPhrase(Tree node, SemanticGraph graph, String text) {
		if (node.isPrePreTerminal()) {
			CoreLabel phrInfo = (CoreLabel) node.label();
			TreeSet<String> ners = new TreeSet<String>();
			List<TaggedWord> words = new ArrayList<TaggedWord>();
			int minCharPos = Integer.MAX_VALUE;
			int maxCharPos = Integer.MIN_VALUE;
			Integer headIndex = null;
			TaggedWord headWord = null;
			int minLevel = Integer.MAX_VALUE;
			for (Tree child : node.children()) {
				CoreLabel posInfo = (CoreLabel) child.label();
				CoreLabel tokInfo = (CoreLabel) child.getChild(0).label();
				words.add(new TaggedWord(tokInfo, posInfo));
				ners.add(tokInfo.ner());
				minCharPos = Math.min(minCharPos, tokInfo.beginPosition());
				maxCharPos = Math.max(maxCharPos, tokInfo.endPosition());
				IndexedWord nodeByIdx = graph.getNodeByIndexSafe(tokInfo.index()); // 1-based index
				int level = nodeByIdx != null ? graph.getPathToRoot(nodeByIdx).size() : Integer.MAX_VALUE;
				if (level < minLevel) {
					minLevel = level;
					headIndex = tokInfo.index() - 1; // 1-based index to 0-based index
					headWord = new TaggedWord(tokInfo.word(), tokInfo.tag());
				}
			}
			if (ners.size() > 1)
				ners.remove("O");

			Phrase p = new Phrase();
			p.category = phrInfo.category();
			p.text = text.substring(minCharPos, maxCharPos);
			p.words = ImmutableList.copyOf(words);
			p.beginIndex = phrInfo.get(BeginIndexAnnotation.class);
			p.endIndex = phrInfo.get(EndIndexAnnotation.class);
			p.headIndex = headIndex;
			p.headWord = headWord;
			p.ner = JString.join(",", ners);
			return p;
		}

		if (node.isPreTerminal()) {
			CoreLabel posInfo = (CoreLabel) node.label();
			CoreLabel tokInfo = (CoreLabel) node.getChild(0).label();
			Phrase p = new Phrase();
			p.category = "WORD";
			p.text = text.substring(tokInfo.beginPosition(), tokInfo.endPosition());
			p.words = ImmutableList.of(new TaggedWord(tokInfo, posInfo));
			p.beginIndex = posInfo.get(BeginIndexAnnotation.class);
			p.endIndex = posInfo.get(EndIndexAnnotation.class);
			p.headIndex = tokInfo.index() - 1; // 1-based index to 0-based index
			p.headWord = new TaggedWord(tokInfo.word(), tokInfo.tag());
			p.ner = tokInfo.ner();
			return p;
		}

		return null;
	}

	/**
	 * Find a head index between beginning index and ending index
	 * 
	 * @param sentence
	 * @param beginIndex
	 *            beginning index (0-based including index)
	 * @param endIndex
	 *            ending index (0-based non-including index)
	 * @return head index (0-based index)
	 */
	public static Integer findHeadBetween(CoreMap sentence, int beginIndex, int endIndex) {
		SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		Integer headIndex = beginIndex; // 0-based index
		int minLevel = Integer.MAX_VALUE;
		for (int idx = beginIndex; idx < endIndex; idx++) {
			IndexedWord nodeByIdx = graph.getNodeByIndexSafe(idx + 1); // 0-based index to 1-based index
			int level = nodeByIdx != null ? graph.getPathToRoot(nodeByIdx).size() : Integer.MAX_VALUE;
			if (level < minLevel) {
				minLevel = level;
				headIndex = idx; // 0-based index
			}
		}
		return headIndex; // 0-based index
	}

	/**
	 * Get the predefined GrammaticalRelation instance or make a new GrammaticalRelation instance
	 * 
	 * @param name
	 * @return
	 */
	public static GrammaticalRelation getGrammaticalRelation(String name) {
		GrammaticalRelation reln = GrammaticalRelation.valueOf(name, COMMON_RELATIONS);
		if (reln == null)
			reln = GrammaticalRelation.valueOf(Language.English, name);
		return reln;
	}
}
