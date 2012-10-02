package main;

import java.io.IOException;

import natto.NattoIndexing;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

public class Indexing {
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
		String fileName = args[1];
		Boolean init = new Boolean(args[2]);
		NattoIndexing ind = new NattoIndexing(indexPath);
		try {
			// Indexing
			ind.makeIndex(fileName, analyzer, init);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
