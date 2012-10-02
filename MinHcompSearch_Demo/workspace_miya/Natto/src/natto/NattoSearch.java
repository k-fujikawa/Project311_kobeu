package natto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class NattoSearch {
	String indexPath = null;
	Analyzer analyzer = null;
	int pageCount = 10;

	public NattoSearch(String indexPath_, Analyzer analyzer_) {
		indexPath = indexPath_;
		analyzer = analyzer_;
	}

	public NattoSearch(String indexPath_, Analyzer analyzer_, int pageCount_) {
		this(indexPath_, analyzer_);
		pageCount_ = pageCount;
	}

	public List<String> pages2lines(ScoreDoc[] pages, IndexSearcher searcher)
			throws CorruptIndexException, IOException, java.text.ParseException {
		List<String> lines = new ArrayList<String>();
		for (ScoreDoc page : pages) {
			if (page == null) {
				break;
			}
			int docId = page.doc;
			Document d = searcher.doc(docId);
			String line = d.get("ID") + "\t"
					+ NattoUtils.parseID2Date(d.get("ID")) + "\t"
					+ d.get("TEXT");
			lines.add(line);
		}
		return lines;
	}

	public void setPageCount(int pageCount_){
		pageCount = pageCount_;
	}
	
	public List<String> search(String query)
			throws ParseException, IOException, java.text.ParseException {
		Query q = new QueryParser(Version.LUCENE_36, "TEXT", analyzer)
				.parse(query);
		Directory index = FSDirectory.open(new File(indexPath));
		IndexReader reader = IndexReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(pageCount,
				true);
		searcher.search(q, collector);
		ScoreDoc[] pages = collector.topDocs().scoreDocs;
		List<String> lines = pages2lines(pages, searcher);
		reader.close();
		searcher.close();
		index.close();
		return lines;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}
