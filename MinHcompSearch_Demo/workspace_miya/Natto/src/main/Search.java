package main;

import java.io.IOException;
import java.util.List;

import natto.NattoSearch;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.Version;

public class Search {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Argument Error!!");
			System.exit(-1);
		}
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		String indexPath = args[0];
		String query = args[1];
		int pageCount = new Integer(args[2]);

		NattoSearch search = new NattoSearch(indexPath, analyzer, pageCount);
		// Indexing
		try {
			List<String> lines = search.search(query);
			for (String line : lines) {
				System.out.println(line.split("\t")[0]);
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
