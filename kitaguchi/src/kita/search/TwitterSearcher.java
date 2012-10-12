package kita.search;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

//import org.ninit.models.bm25.BM25BooleanQuery;
//import org.ninit.models.bm25.BM25Parameters;

public class TwitterSearcher {

  private IndexSearcher searcher;
  private Analyzer analyzer;

  /**
   * コンストラクタ
   * @param searcher
   */
  public TwitterSearcher(IndexSearcher searcher, Analyzer analyzer) {
    setSearcher(searcher);
    setAnalyzer(analyzer);
  }

  /*
  /**
   * BM25のライブラリを用いた検索<br>
   * org.ninit.models.bm25.BM25BooleanQuery に問題があり，利用不可能．<br>
   * コメントアウトを外すと，当メソッドを呼び出さなくても，コンストラクタ呼び出しでエラー．
   * 使い方が悪いのか，バグかは不明．
   * @see http://nlp.uned.es/~jperezi/Lucene-BM25/
   * @param queryStr
   * @param hitsPerPage
   */
  /*
  public void searchByBM25(String queryStr, int hitsPerPage) {
  // BM25のパラメータ読み出し
    String pathOfAverageTweetLength = "/Users/KitaguchiSayaka/Desktop/AverageTweetLength.txt";
    try {
      BM25Parameters.load(pathOfAverageTweetLength);
    } catch (NumberFormatException e) {
      System.err.println("BM25のパラメータ読み出しで例外が発生しました．");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("BM25のパラメータ読み出しで例外が発生しました．");
      e.printStackTrace();
    }
  */
    // 検索
/*  try {
      BM25BooleanQuery query = new BM25BooleanQuery(queryStr, "text", analyzer);
      TopDocs topDocs = searcher.search(query, hitsPerPage);
      ScoreDoc[] hits = topDocs.scoreDocs;*/

      /* -------------テスト用ここから------------------ *//*
      for (int i=0; i < hits.length; i++) {
        int docId = hits[i].doc;
        System.out.println(searcher.doc(docId).get("text") + " : " + hits[i].score);
      }*/
      /* -------------テスト用ここまで------------------ *//*
    } catch (ParseException e) {
      System.err.println("BM25を用いた検索で例外が発生しました．");
      e.printStackTrace();
    } catch (IOException e) {
      System.err.println("BM25を用いた検索で例外が発生しました．");
      e.printStackTrace();
    }
  }
*/

  /**
   *
   * @param query TwitterQueryMakerが作成したクエリ
   * @param hitsPerPage 検索結果の最大数
   * @return 検索結果を格納したResultクラス
   * @throws IOException
   */
  public Result search(Query query, int hitsPerPage) throws IOException {
    TopDocs topDocs = searcher.search(query, hitsPerPage);
    ScoreDoc[] hits = topDocs.scoreDocs;
    Result result = new Result(hits, searcher);
    /* -------------テスト用ここから------------------
    for (int i=0; i < hits.length; i++) {
      int docId = hits[i].doc;
      System.out.println("id:" + docId);
      Document d = searcher.doc(docId);
      System.out.println(d.get("created_at") + " : " + d.get("text") + " : " + hits[i].score);
    }
    -------------テスト用ここまで------------------ */
    return result;
  }

  /**
   *
   * @deprecated TwitterQueryMakerも使い回したいので，このメソッドは非推奨
   * @see TwitterSearcher#search(Query, int)
   * @param queryStr 検索したい文字列
   * @param hitsPerPage 検索結果の最大数
   * @return 検索結果を格納したResultクラス
   * @throws IOException
   */
  public Result search(String queryStr, int hitsPerPage) throws IOException {
    TwitterQueryMaker TQM = new TwitterQueryMaker(analyzer);
    Query query;
    try {
      query = TQM.makeQuery(queryStr);
      return search(query, hitsPerPage);
    } catch (ParseException e) {
      System.err.println("クエリの生成で例外発生");
      e.printStackTrace();
    }
    return null;
  }

  public IndexSearcher getSearcher() {
    return searcher;
  }

  public void setSearcher(IndexSearcher searcher) {
    this.searcher = searcher;
  }

  public Analyzer getAnalyzer() {
    return analyzer;
  }

  public void setAnalyzer(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

}
