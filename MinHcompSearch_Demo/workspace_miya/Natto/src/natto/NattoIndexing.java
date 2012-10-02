package natto;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class NattoIndexing {
	String indexPath = null;

	public NattoIndexing(String indexPath_) {
		indexPath = indexPath_;
	}

	public boolean addFileToIndex(String fileName, IndexWriter writer)
			throws IOException {
		List<String> lines = FileUtils.readLines(new File(fileName));
		String id = FilenameUtils.getName(fileName).split(".txt")[0];
		StringBuffer sb = new StringBuffer();
		for (String line : lines) {
			sb.append(line);
		}
		Document document = new Document();
		document.add(new Field("ID", id, Field.Store.YES, Field.Index.NO));
		// document.add(new Field("TIME",
		// NattoUtils.parseID2Date(id).toString(), Field.Store.YES,
		// Field.Index.NO));
		document.add(new Field("TEXT", sb.toString(), Field.Store.YES,
				Field.Index.ANALYZED));
		writer.addDocument(document);
		return true;
	}

	public boolean makeIndex(String fileName, Analyzer analyzer, boolean init)
			throws IOException {
		Directory index = FSDirectory.open(new File(indexPath));
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
				analyzer);
		IndexWriter writer = new IndexWriter(index, config);
		// delete contents of the index
		if (init) {
			writer.deleteAll();
		}
		addFileToIndex(fileName, writer);
		writer.close();
		index.close();
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		String fileName = "./data/20120906230147.txt";
		String indexPath = "./index/";
		String query = "can";
		int pageCount = 20;
		
		NattoIndexing ind = new NattoIndexing(indexPath);
		NattoSearch search = new NattoSearch(indexPath, analyzer, pageCount);
		// Indexing
		try {
			ind.makeIndex(fileName, analyzer, false);
			List<String> lines = search.search(query);
			for(String line: lines){
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
