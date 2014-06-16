/**
 * Information Retrieval package
 */
package kr.jihee.irnlp_toolkit.ir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import kr.jihee.irnlp_toolkit.ir.LuceneWrapper;
import kr.jihee.irnlp_toolkit.ir.LuceneWrapper.SearchedEntry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;

/**
 * Unit test for functions using Lucene
 * 
 * @author Jihee
 */
public class TestLucene extends TestCase {

	/**
	 * LuceneWrapper Test
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	public void testLuceneWrapper() throws IOException, ParseException {
		System.out.println("\n----- testLuceneWrapper() ------------------------------");
		String index_dir = System.getProperty("user.home") + "/organization.idx";

		LuceneWrapper lucene = new LuceneWrapper(index_dir);
		IndexWriter writer = lucene.createIndexWriter(true);

		ArrayList<String[]> index_entries = new ArrayList<String[]>();
		index_entries.add(new String[] { "Apple", "Apple Company in United States (US)" });
		index_entries.add(new String[] { "Google", "Google Company in United States (US)" });
		index_entries.add(new String[] { "Microsoft", "Microsoft (MS) Company in United States (US)" });
		index_entries.add(new String[] { "MIT", "Massachusetts Institute of Technology (MIT) in United States (US)" });
		index_entries.add(new String[] { "Stanford", "Stanford University in United States (US)" });
		index_entries.add(new String[] { "Cambridge", "Cambridge University in United Kingdom (UK)" });
		index_entries.add(new String[] { "Oxford", "Oxford University in United Kingdom (UK)" });
		index_entries.add(new String[] { "KAIST", "Korea Advanced Institute of Science and Technology (KAIST) in South Korea" });
		index_entries.add(new String[] { "POSTECH", "Pohang University of Science and Technology (POSTECH) in South Korea" });
		index_entries.add(new String[] { "Seoul National University", "Seoul National University (SNU) in South Korea" });
		for (String[] entry : index_entries) {
			Document doc = new Document();
			doc.add(new StringField("label", entry[0], Field.Store.YES));
			doc.add(new TextField("contents", entry[1], Field.Store.YES));
			writer.addDocument(doc);
		}
		writer.close();
		assertTrue(DirectoryReader.indexExists(lucene.index_dir));
		
		IndexSearcher searcher = lucene.createIndexSearcher();
		QueryParser parser = lucene.createQueryParser("contents");

		String query = "Korea AND Science";
		List<SearchedEntry> searched_entries = lucene.search(searcher, parser.parse(query), 20);
		for (SearchedEntry searched_entry : searched_entries)
			System.out.printf("(%.4f) [%s] %s\n", searched_entry.score, searched_entry.doc.get("label"), searched_entry.doc.get("contents"));
		assertEquals(2, searched_entries.size());
	}
}
