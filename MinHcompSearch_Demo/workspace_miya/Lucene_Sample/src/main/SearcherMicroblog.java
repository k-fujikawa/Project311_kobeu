package main;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SearcherMicroblog {
	private static final int HITS_PER_PAGE = 10; // 上位何ヒットを集めるか

	/**
	 * インデックスの場所(pathOfIndex)とクエリ(query)と結果表示数(hitsPerPage)を受け取って，
	 * インデックスを検索し，結果を表示する．
	 * 
	 * @param pathOfIndex
	 *            インデックスの場所を示すpath
	 * @param query
	 *            クエリ
	 * @throws IOException
	 */
	public static void search(String pathOfIndex, Query query, int hitsPerPage)
			throws IOException {
		// 検索器を作る．
		// 下の一文．"true"はreadOnlyってこと，でも非推奨なんだって．
		Directory index = FSDirectory.open(new File(pathOfIndex));
		IndexReader reader = IndexReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		// IndexSearcher searcher = new IndexSearcher(index, true);
		// //非推奨なので上記2文で代用
		// TopScoreDocCollectionはスコア計算するんだって．
		TopScoreDocCollector collector = TopScoreDocCollector.create(
				hitsPerPage, true);
		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;

		// 検索結果表示
		show(hits, searcher);

		reader.close();
		searcher.close();
		index.close();
	}

	/**
	 * 結果表示数が指定されていないとき，定数HITS_PER_PAGEを結果表示数とする．
	 * 
	 * @param pathOfIndexm
	 * @throws IOException
	 */
	public static void search(String pathOfIndex, Query query)
			throws IOException {
		search(pathOfIndex, query, HITS_PER_PAGE);
	}

	// 期間を指定して検索する．
	public static void searchWithTime(String pathOfIndex, Query query,
			int hitsPerPage, Calendar startTime, Calendar endTime)
			throws IOException {
		// 検索器を作る．
		// 下の一文．"true"はreadOnlyってこと，でも非推奨なんだって．
		Directory index = FSDirectory.open(new File(pathOfIndex));
		IndexReader reader = IndexReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		// TopScoreDocCollectionはスコア計算するんだって．
		TopScoreDocCollector collector = TopScoreDocCollector.create(10000,
				true);
		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		int numOfHits = collector.getTotalHits();
		// 規定のヒット数(hitsPerPage)になるまで，検索結果絞り込み
		int numMatchHits = 0; // 時間マッチしたヒット数
		ScoreDoc[] matchHits = new ScoreDoc[hitsPerPage];
		int head = 0;
		while (numMatchHits < hitsPerPage && head < numOfHits) {
			Calendar hitCalender = Calendar.getInstance();
			int docId = hits[head].doc;
			head++;
			Document d = searcher.doc(docId);
			String time = d.get("TIME");
			if (time.equals("null")) { // timeがnullならやり直し
				continue;
			}
			if (!(TimeParser.parseTime(time, hitCalender))) { // 解析失敗
				System.out.println("日付の解析に失敗しました");
				continue;
			}
			// 時間制限にマッチするかチェック
			if (hitCalender.compareTo(startTime) < 0)
				;
			else if (hitCalender.compareTo(endTime) > 0)
				;
			else { // マッチした
				matchHits[numMatchHits] = hits[head - 1];
				numMatchHits++;
			}
		}
		// 検索結果表示
		show(matchHits, searcher, numMatchHits);

		reader.close();
		searcher.close();
		index.close();
	}

	/**
	 * 結果表示
	 * 
	 * @param hits
	 * @param searcher
	 * @param numMatchHits
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	public static void show(ScoreDoc[] hits, IndexSearcher searcher,
			int numMatchHits) throws CorruptIndexException, IOException {
		System.out.println("Found " + numMatchHits + " hits.");
		for (int i = 0; i < hits.length; i++) {
			if (hits[i] == null)
				break;
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			System.out.println(d.get("ID") + "\t" + d.get("TIME") + "\t"
					+ d.get("USER") + "\t" + d.get("TWEET"));
			// System.out.println((i+1) + ". " + d.get("TWEET"));
		}
	}

	/**
	 * 結果表示
	 * 
	 * @param hits
	 * @param searcher
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static void show(ScoreDoc[] hits, IndexSearcher searcher)
			throws CorruptIndexException, IOException {
		show(hits, searcher, hits.length);
	}

}
