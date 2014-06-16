/**
 * Information Retrieval package
 */
package kr.jihee.irnlp_toolkit.ir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.jihee.irnlp_toolkit.Env;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Wrapper of Lucene 4.8.1<br>
 * - URL : http://lucene.apache.org/core/
 * 
 * @author Jihee
 */
public class LuceneWrapper {

	public FSDirectory index_dir;

	/**
	 * SearchedEntry
	 * 
	 * @author Jihee
	 */
	public static class SearchedEntry {

		public Document doc;
		public float score;

		public SearchedEntry(Document doc, float score) {
			this.doc = doc;
			this.score = score;
		}
	}

	public LuceneWrapper(String index_dir) throws IOException {
		this.index_dir = FSDirectory.open(new File(index_dir));
	}

	public IndexWriter createIndexWriter(boolean create) throws IOException {
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, new StandardAnalyzer(Version.LUCENE_48));
		iwc.setOpenMode(create ? OpenMode.CREATE : OpenMode.CREATE_OR_APPEND);
		iwc.setRAMBufferSizeMB(Env.LUCENE_MEM);
		return new IndexWriter(index_dir, iwc);
	}

	public IndexSearcher createIndexSearcher() throws IOException {
		return new IndexSearcher(DirectoryReader.open(index_dir));
	}

	public QueryParser createQueryParser(String field) {
		return new QueryParser(Version.LUCENE_48, field, new StandardAnalyzer(Version.LUCENE_48));
	}

	public List<SearchedEntry> search(IndexSearcher searcher, Query query, int k) throws IOException {
		ArrayList<SearchedEntry> searched_entries = new ArrayList<SearchedEntry>();
		for (ScoreDoc searched_doc : searcher.search(query, k).scoreDocs) {
			Document doc = searcher.doc(searched_doc.doc);
			float score = searched_doc.score;
			searched_entries.add(new SearchedEntry(doc, score));
		}
		return searched_entries;
	}
}
